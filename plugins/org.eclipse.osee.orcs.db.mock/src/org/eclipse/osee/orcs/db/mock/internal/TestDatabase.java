/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.mock.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.eclipse.osee.framework.core.data.IDatabaseInfo;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.IDatabaseInfoProvider;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.h2.H2DbServer;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.network.PortUtil;
import org.eclipse.osee.orcs.db.mock.OseeDatabase;
import org.eclipse.osee.orcs.db.mock.OsgiUtil;
import org.junit.Assert;
import org.junit.runners.model.FrameworkMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Roberto E. Escobar
 */
public class TestDatabase {

   private final FrameworkMethod method;
   private final Object target;

   private ServiceRegistration<?> registration;
   private File tempFolder;
   private final String connectionId;

   public TestDatabase(String connectionId, FrameworkMethod method, Object target) {
      this.connectionId = connectionId;
      this.method = method;
      this.target = target;
   }

   private File createTempFolder(FrameworkMethod method, Object target) {
      String tempDir = System.getProperty("user.home");
      String folderName =
         String.format("%s_%s_%s", target.getClass().getSimpleName(), method.getName(), Lib.getDateTimeString());
      File tempFolder = new File(tempDir, folderName);
      tempFolder.mkdir();
      return tempFolder;
   }

   public void initialize() throws Exception {
      Bundle bundle = FrameworkUtil.getBundle(OseeDatabase.class);
      Assert.assertNotNull("Bundle cannot be null", bundle);

      tempFolder = createTempFolder(method, target);
      Assert.assertNotNull("TempFolder cannot be null", tempFolder);

      addResource(tempFolder, bundle, "data/h2.zip");
      addResource(tempFolder, bundle, "data/binary_data.zip");

      checkExist(tempFolder, "h2");
      checkExist(tempFolder, "attr");

      String dbPath = getDbHomePath(tempFolder, "h2");

      int port = PortUtil.getInstance().getValidPort() + 1;
      IDatabaseInfo databaseInfo = new DbInfo(connectionId, port, dbPath);
      TestDbProvider provider = new TestDbProvider(databaseInfo);

      System.setProperty("osee.db.embedded.server", "");
      System.setProperty("osee.application.server.data", tempFolder.getAbsolutePath());
      BundleContext context = getContext(bundle);
      registration = context.registerService(IDatabaseInfoProvider.class, provider, null);

      IOseeDatabaseService dbService = OsgiUtil.getService(IOseeDatabaseService.class);
      Assert.assertNotNull(dbService);

      H2DbServer.startServer("0.0.0.0", port);

      OseeConnection connection = dbService.getConnection();
      try {
         Assert.assertNotNull(connection);
      } finally {
         connection.close();
      }
   }

   private String getDbHomePath(File tempFolder, String dbFolder) {
      return String.format("~/%s/%s", tempFolder.getName(), dbFolder);
   }

   private BundleContext getContext(Bundle bundle) throws BundleException {
      int state = bundle.getState();
      if (state != Bundle.STARTING || state != Bundle.ACTIVE) {
         bundle.start();
      }
      return bundle.getBundleContext();
   }

   private void checkExist(File tempFolder, String name) {
      File toCheck = new File(tempFolder, name);
      Assert.assertTrue(String.format("%s does not exist", name), toCheck.exists());
   }

   private void addResource(File targetDirectory, Bundle bundle, String resource) throws IOException {
      URL resourceURL = bundle.getResource(resource);
      InputStream inputStream = null;
      try {
         inputStream = new BufferedInputStream(resourceURL.openStream());
         Lib.decompressStream(inputStream, targetDirectory);
      } finally {
         Lib.close(inputStream);
      }
   }

   public void cleanup() {
      //TODO issue shutdown command to server;

      if (registration != null) {
         registration.unregister();
      }

      System.setProperty("osee.application.server.data", "");
      System.setProperty("osee.db.embedded.server", "");
      H2DbServer.stopServer();
      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
            if (tempFolder != null) {
               Lib.deleteDir(tempFolder);
            }
         }
      });
   }
}