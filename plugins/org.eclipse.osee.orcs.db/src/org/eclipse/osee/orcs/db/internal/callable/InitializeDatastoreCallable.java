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
package org.eclipse.osee.orcs.db.internal.callable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.osee.database.schema.DatabaseCallable;
import org.eclipse.osee.database.schema.InitializeSchemaCallable;
import org.eclipse.osee.database.schema.SchemaOptions;
import org.eclipse.osee.database.schema.SchemaResourceProvider;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.SystemPreferences;
import org.eclipse.osee.orcs.core.ds.BranchDataStore;
import org.eclipse.osee.orcs.core.ds.DataStoreConstants;
import org.eclipse.osee.orcs.core.ds.DataStoreInfo;
import org.eclipse.osee.orcs.data.CreateBranchData;
import org.eclipse.osee.orcs.db.internal.loader.RelationalConstants;
import org.eclipse.osee.orcs.db.internal.resource.ResourceConstants;

/**
 * @author Roberto E. Escobar
 */
public class InitializeDatastoreCallable extends DatabaseCallable<DataStoreInfo> {

   private static final String ADD_PERMISSION =
      "INSERT INTO OSEE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) VALUES (?,?)";

   private final SystemPreferences preferences;
   private final SchemaResourceProvider schemaProvider;
   private final SchemaOptions options;
   private final IdentityService identityService;
   private final BranchDataStore branchStore;

   public InitializeDatastoreCallable(Log logger, IOseeDatabaseService dbService, IdentityService identityService, BranchDataStore branchStore, SystemPreferences preferences, SchemaResourceProvider schemaProvider, SchemaOptions options) {
      super(logger, dbService);
      this.identityService = identityService;
      this.branchStore = branchStore;
      this.preferences = preferences;
      this.schemaProvider = schemaProvider;
      this.options = options;
   }

   @Override
   public DataStoreInfo call() throws Exception {
      Conditions.checkExpressionFailOnTrue(getDatabaseService().isProduction(),
         "Error - attempting to initialize a production datastore.");

      Callable<Object> callable =
         new InitializeSchemaCallable(getLogger(), getDatabaseService(), schemaProvider, options);
      callAndCheckForCancel(callable);

      String attributeDataPath = ResourceConstants.getAttributeDataPath(preferences);
      getLogger().info("Deleting application server binary data [%s]...", attributeDataPath);
      Lib.deleteDir(new File(attributeDataPath));

      preferences.putValue(DataStoreConstants.DATASTORE_ID_KEY, GUID.create());

      addDefaultPermissions();

      clearStateCaches();

      //      boolean doesSystemRootExist = cacheService.getBranchCache().existsByGuid(CoreBranches.SYSTEM_ROOT.getGuid());
      //      Conditions.checkExpressionFailOnTrue(doesSystemRootExist, "System Root branch already exists.");

      CreateBranchData systemRootData = getSystemRootData();

      // TODO tie in the session information
      Callable<Branch> createSystemRoot = branchStore.createBranch("sessionId", systemRootData);
      Branch systemRoot = callAndCheckForCancel(createSystemRoot);

      Conditions.checkNotNull(systemRoot, "System Root Branch");

      Callable<DataStoreInfo> fetchCallable =
         new FetchDatastoreInfoCallable(getLogger(), getDatabaseService(), schemaProvider, preferences);
      DataStoreInfo dataStoreInfo = callAndCheckForCancel(fetchCallable);
      return dataStoreInfo;

   }

   private CreateBranchData getSystemRootData() {
      CreateBranchData data = new CreateBranchData();

      data.setGuid(CoreBranches.SYSTEM_ROOT.getGuid());
      data.setName(CoreBranches.SYSTEM_ROOT.getName());
      data.setBranchType(BranchType.SYSTEM_ROOT);

      String creationComment = String.format("%s Creation", CoreBranches.SYSTEM_ROOT.getName());
      data.setCreationComment(creationComment);

      data.setFromTransaction(null);

      data.setMergeAddressingQueryId(RelationalConstants.JOIN_QUERY_ID_SENTINEL);
      data.setMergeDestinationBranchId(RelationalConstants.BRANCH_SENTINEL);
      return data;
   }

   private void clearStateCaches() throws OseeDataStoreException {
      getDatabaseService().getSequence().clear();
      identityService.clear();
   }

   private void addDefaultPermissions() throws OseeCoreException {
      List<Object[]> data = new LinkedList<Object[]>();
      for (PermissionEnum permission : PermissionEnum.values()) {
         data.add(new Object[] {permission.getPermId(), permission.getName()});
      }
      getDatabaseService().runBatchUpdate(ADD_PERMISSION, data);
   }
}
