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
package org.eclipse.osee.orcs.db.intergration;

import java.sql.DatabaseMetaData;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.database.core.SupportedDatabase;
import org.eclipse.osee.orcs.core.ds.DataStoreConstants;
import org.eclipse.osee.orcs.db.internal.SqlProvider;
import org.eclipse.osee.orcs.db.internal.accessor.OseeInfoDataAccessor;
import org.eclipse.osee.orcs.db.internal.resource.ResourceConstants;
import org.eclipse.osee.orcs.db.mock.OseeDatabase;
import org.eclipse.osee.orcs.db.mock.OsgiRule;
import org.eclipse.osee.orcs.db.mock.OsgiService;
import org.eclipse.osee.orcs.db.mocks.MockLog;
import org.junit.Assert;
import org.junit.Rule;

/**
 * Test Case for {@link OseeInfoDataAccessor}
 * 
 * @author Roberto E. Escobar
 */
public class OseeInfoDataAccessorTest {

   @Rule
   public OsgiRule osgi = new OsgiRule(this);

   @Rule
   public OseeDatabase db = new OseeDatabase("osee.demo.hsql");

   @OsgiService
   IOseeDatabaseService dbService;

   @org.junit.Test
   public void testGetSetValue() throws OseeCoreException {
      OseeInfoDataAccessor accessor = new OseeInfoDataAccessor();
      accessor.setLogger(new MockLog());
      accessor.setDatabaseService(dbService);

      String value = accessor.getValue("test.data");
      Assert.assertEquals("", value);

      boolean wasSuccessful = accessor.putValue("test.data", "testing 1,2,3");
      Assert.assertTrue(wasSuccessful);

      String value1 = accessor.getValue("test.data");
      Assert.assertEquals("testing 1,2,3", value1);
   }

   @org.junit.Test(expected = OseeStateException.class)
   public void testSetBinaryDataPath() throws OseeCoreException {
      OseeInfoDataAccessor accessor = new OseeInfoDataAccessor();
      accessor.setLogger(new MockLog());
      accessor.setDatabaseService(dbService);

      accessor.putValue(ResourceConstants.BINARY_DATA_PATH, "dummy");
   }

   @org.junit.Test
   public void testGetBinaryDataPath() throws OseeCoreException {
      OseeInfoDataAccessor accessor = new OseeInfoDataAccessor();
      accessor.setLogger(new MockLog());
      accessor.setDatabaseService(dbService);

      String original = accessor.getValue(ResourceConstants.BINARY_DATA_PATH);
      Assert.assertEquals(System.getProperty(ResourceConstants.BINARY_DATA_PATH), original);

      System.setProperty(ResourceConstants.BINARY_DATA_PATH, "");
      try {
         String actual = accessor.getValue(ResourceConstants.BINARY_DATA_PATH);
         Assert.assertEquals(System.getProperty("user.home"), actual);
      } finally {
         System.setProperty(ResourceConstants.BINARY_DATA_PATH, original);
      }
   }

   @org.junit.Test(expected = OseeStateException.class)
   public void testSetDatabaseHintsSupported() throws OseeCoreException {
      OseeInfoDataAccessor accessor = new OseeInfoDataAccessor();
      accessor.setLogger(new MockLog());
      accessor.setDatabaseService(dbService);

      accessor.putValue(SqlProvider.SQL_DATABASE_HINTS_SUPPORTED_KEY, "dummy");
   }

   @org.junit.Test
   public void testGetDatabaseHintsSupported() throws OseeCoreException {
      OseeInfoDataAccessor accessor = new OseeInfoDataAccessor();
      accessor.setLogger(new MockLog());
      accessor.setDatabaseService(dbService);

      String original = accessor.getValue(SqlProvider.SQL_DATABASE_HINTS_SUPPORTED_KEY);

      boolean expected = false;
      OseeConnection connection = dbService.getConnection();
      try {
         DatabaseMetaData metaData = connection.getMetaData();
         expected = SupportedDatabase.isDatabaseType(metaData, SupportedDatabase.oracle);
      } finally {
         connection.close();
      }
      Assert.assertEquals(expected, Boolean.parseBoolean(original));
   }

   @org.junit.Test(expected = OseeStateException.class)
   public void testSetCheckTagQueueOnStartupAllowed() throws OseeCoreException {
      OseeInfoDataAccessor accessor = new OseeInfoDataAccessor();
      accessor.setLogger(new MockLog());
      accessor.setDatabaseService(dbService);

      accessor.putValue(DataStoreConstants.DATASTORE_INDEX_ON_START_UP, "dummy");
   }

   @org.junit.Test
   public void testGetCheckTagQueueOnStartupAllowed() throws OseeCoreException {
      OseeInfoDataAccessor accessor = new OseeInfoDataAccessor();
      accessor.setLogger(new MockLog());
      accessor.setDatabaseService(dbService);

      String original = accessor.getValue(DataStoreConstants.DATASTORE_INDEX_ON_START_UP);
      Assert.assertEquals(System.getProperty(DataStoreConstants.DATASTORE_INDEX_ON_START_UP, "false"), original);

      System.setProperty(DataStoreConstants.DATASTORE_INDEX_ON_START_UP, "true");
      try {
         String actual = accessor.getValue(DataStoreConstants.DATASTORE_INDEX_ON_START_UP);
         Assert.assertEquals("true", actual);
      } finally {
         System.setProperty(DataStoreConstants.DATASTORE_INDEX_ON_START_UP, original);
      }
   }

   @org.junit.Test
   public void testGetKeys() throws OseeCoreException {
      OseeInfoDataAccessor accessor = new OseeInfoDataAccessor();
      accessor.setLogger(new MockLog());
      accessor.setDatabaseService(dbService);
      Assert.assertTrue(!accessor.getKeys().isEmpty());
   }

}