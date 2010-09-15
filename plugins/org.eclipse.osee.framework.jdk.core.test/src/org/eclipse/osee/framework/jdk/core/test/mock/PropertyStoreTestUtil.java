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
package org.eclipse.osee.framework.jdk.core.test.mock;

import java.util.Map.Entry;
import java.util.Properties;
import org.junit.Assert;

/**
 * @author Roberto E. Escobar
 */
public final class PropertyStoreTestUtil {

   private PropertyStoreTestUtil() {
      // Utility Class
   }

   public static void checkArrays(String[] expArray, String[] actualArray) {
      Assert.assertEquals(expArray.length, actualArray.length);
      for (int index = 0; index < expArray.length; index++) {
         Assert.assertEquals(expArray[index], actualArray[index]);
      }
   }

   public static MockPropertyStore createPropertyStore() {
      MockPropertyStore store = new MockPropertyStore();
      Assert.assertEquals("", store.getId());
      Assert.assertNotNull(store.getItems());
      Assert.assertNotNull(store.getArrays());
      Assert.assertTrue(store.isEmpty());
      return store;
   }

   public static MockPropertyStore createPropertyStore(String id) {
      MockPropertyStore store = new MockPropertyStore(id);
      Assert.assertEquals(id, store.getId());
      Assert.assertNotNull(store.getItems());
      Assert.assertNotNull(store.getArrays());
      return store;
   }

   public static MockPropertyStore createPropertyStore(Properties properties) {
      MockPropertyStore store = new MockPropertyStore(properties);
      Assert.assertEquals(properties, store.getItems());
      Assert.assertEquals(String.valueOf(properties.hashCode()), store.getId());
      Assert.assertNotNull(store.getItems());
      Assert.assertNotNull(store.getArrays());
      return store;
   }

   public static void checkEquals(MockPropertyStore expected, MockPropertyStore actual) {
      Assert.assertEquals(expected.getId(), actual.getId());
      checkPropertiesEqual(expected.getItems(), actual.getItems());
      checkPropertiesEqual(expected.getArrays(), actual.getArrays());
      checkPropertiesEqual(expected.getPropertyStores(), actual.getPropertyStores());
   }

   public static void checkPropertiesEqual(Properties expected, Properties actual) {
      Assert.assertEquals(expected.size(), actual.size());
      for (Entry<Object, Object> expectedEntry : expected.entrySet()) {
         Object expectedValue = expectedEntry.getValue();
         Object actualValue = actual.get(expectedEntry.getKey());
         if (expectedValue instanceof String[]) {
            String[] expArray = (String[]) expectedValue;
            String[] actualArray = (String[]) actualValue;
            checkArrays(expArray, actualArray);
         } else {
            Assert.assertEquals(expectedValue, actualValue);
         }
      }
   }

}