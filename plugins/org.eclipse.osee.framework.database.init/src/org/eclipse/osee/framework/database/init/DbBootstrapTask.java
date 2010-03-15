/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/

package org.eclipse.osee.framework.database.init;

import java.io.File;
import java.net.Socket;
import java.net.URL;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.core.client.BaseCredentialProvider;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.client.server.HttpUrlBuilderClient;
import org.eclipse.osee.framework.core.data.OseeCredential;
import org.eclipse.osee.framework.core.data.OseeServerContext;
import org.eclipse.osee.framework.core.data.SystemUser;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.database.core.OseeInfo;
import org.eclipse.osee.framework.database.core.SupportedDatabase;
import org.eclipse.osee.framework.database.init.internal.DatabaseInitActivator;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.HttpProcessor;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.ExtensionPoints;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.utility.DbUtil;
import org.osgi.framework.Bundle;

/**
 * @author Andrew M. Finkbeiner
 */
public class DbBootstrapTask implements IDbInitializationTask {
   private static final String ADD_PERMISSION =
         "INSERT INTO OSEE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) VALUES (?,?)";

   private DbInitConfiguration configuration;

   public void setConfiguration(DbInitConfiguration configuration) {
      this.configuration = configuration;
   }

   public void createOseeSchema() throws OseeCoreException {
      OseeConnection connection = ConnectionHandler.getConnection();
      try {
         DatabaseMetaData metaData = connection.getMetaData();
         SqlManager sqlManager = SqlFactory.getSqlManager(metaData);
         DbInit dbInit = new DbInit(sqlManager);

         DatabaseConfigurationData databaseConfigurationData = new DatabaseConfigurationData(getSchemaFiles());
         Map<String, SchemaData> userSpecifiedConfig = databaseConfigurationData.getUserSpecifiedSchemas();
         DatabaseSchemaExtractor schemaExtractor = new DatabaseSchemaExtractor(userSpecifiedConfig.keySet());
         schemaExtractor.extractSchemaData();
         Map<String, SchemaData> currentDatabaseConfig = schemaExtractor.getSchemas();
         Set<String> schemas = userSpecifiedConfig.keySet();

         dbInit.dropIndices(schemas, userSpecifiedConfig, currentDatabaseConfig);
         dbInit.dropTables(schemas, userSpecifiedConfig, currentDatabaseConfig);
         if (SupportedDatabase.isDatabaseType(metaData, SupportedDatabase.postgresql)) {
            dbInit.dropSchema(schemas);
            dbInit.createSchema(schemas);
         }
         dbInit.addTables(schemas, userSpecifiedConfig);
         dbInit.addIndices(schemas, userSpecifiedConfig);
      } finally {
         Lib.close(connection);
      }
   }

   public void run() throws OseeCoreException {
      Conditions.checkNotNull(configuration, "DbInitConfiguration Info");

      DbUtil.setDbInit(true);
      createOseeSchema();
      registerApplicationServer();
      deleteBinaryBackingData();

      OseeInfo.putValue(OseeInfo.DB_ID_KEY, GUID.create());
      addDefaultPermissions();

      List<String> oseeTypes = configuration.getOseeTypeExtensionIds();
      Conditions.checkExpressionFailOnTrue(oseeTypes.isEmpty(), "osee types cannot be empty");

      BranchManager.createSystemRootBranch();
      OseeTypesSetup oseeTypesSetup = new OseeTypesSetup();
      oseeTypesSetup.execute(oseeTypes);
   }

   private static void registerApplicationServer() throws OseeCoreException {
      try {
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("registerToLookup", "true");
         String url =
               HttpUrlBuilderClient.getInstance().getOsgiServletServiceUrl(OseeServerContext.LOOKUP_CONTEXT, parameters);
         String response = HttpProcessor.post(new URL(url));
         OseeLog.log(DatabaseInitActivator.class, Level.INFO, response);
      } catch (Exception ex1) {
         throw new OseeDataStoreException(ex1);
      }

      ClientSessionManager.authenticate(new BaseCredentialProvider() {

         @Override
         public OseeCredential getCredential() throws OseeCoreException {
            OseeCredential credential = new OseeCredential();
            credential.setUserName(SystemUser.BootStrap.getName());
            return credential;
         }
      });
   }

   private static void deleteBinaryBackingData() throws OseeCoreException {
      boolean displayWarning = false;
      String server = HttpUrlBuilderClient.getInstance().getApplicationServerPrefix();
      try {
         URL serverUrl = new URL(server);
         Socket socket = new Socket(serverUrl.getHost(), serverUrl.getPort());
         if (socket.getInetAddress().isLoopbackAddress()) {
            OseeLog.log(DatabaseInitActivator.class, Level.INFO, "Deleting binary data from application server...");
            String binaryDataPath = ClientSessionManager.getDataStorePath();
            Lib.deleteDir(new File(binaryDataPath + File.separator + "attr"));
            Lib.deleteDir(new File(binaryDataPath + File.separator + "snapshot"));
         } else {
            displayWarning = true;
         }
      } catch (Exception ex) {
         displayWarning = true;
      }

      if (displayWarning) {
         OseeLog.log(DatabaseInitActivator.class, Level.WARNING, "Unable to delete binary data from application server");
      }
   }

   private List<URL> getSchemaFiles() throws OseeCoreException {
      List<URL> toReturn = new ArrayList<URL>();
      List<IConfigurationElement> list =
            ExtensionPoints.getExtensionElements("org.eclipse.osee.framework.skynet.core.OseeDbSchema", "Schema");
      for (IConfigurationElement element : list) {
         String fileName = element.getAttribute("SchemaFile");
         String bundleName = element.getContributor().getName();
         String initRuleClassName = element.getAttribute("DbInitRule");

         if (Strings.isValid(bundleName) && Strings.isValid(fileName)) {
            if (false != fileName.endsWith(DbConfigFileInformation.getSchemaFileExtension())) {
               Bundle bundle = Platform.getBundle(bundleName);

               boolean isAllowed = true;
               if (Strings.isValid(initRuleClassName)) {
                  isAllowed = false;
                  try {
                     Class<?> taskClass = bundle.loadClass(initRuleClassName);
                     IDbInitializationRule rule = (IDbInitializationRule) taskClass.newInstance();
                     isAllowed = rule.isAllowed();
                  } catch (Exception ex) {
                     OseeLog.log(DatabaseInitActivator.class, Level.SEVERE, ex);
                  }
               }

               if (isAllowed) {
                  URL url = bundle.getEntry(fileName);
                  if (url != null) {
                     System.out.println("Adding Schema: [" + fileName + "]");
                     toReturn.add(url);
                  }
               }
            }
         }
      }
      return toReturn;
   }

   /**
    * @throws OseeDataStoreException
    */
   @SuppressWarnings("unchecked")
   private void addDefaultPermissions() throws OseeDataStoreException {
      for (PermissionEnum permission : PermissionEnum.values()) {
         ConnectionHandler.runPreparedUpdate(ADD_PERMISSION, permission.getPermId(), permission.getName());
      }
   }
}
