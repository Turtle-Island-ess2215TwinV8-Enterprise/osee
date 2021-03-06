/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.coverage.model;

import java.util.Collection;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * Simple provider that optimizes how test units are stored by sharing test unit names.
 * 
 * @author Donald G. Dunne
 */
public class SimpleTestUnitProvider implements ITestUnitProvider {

   // Since test units will cover many coverage items (sometimes thousands), it is more cost effective
   // to store single test script name shared by use of string.intern() rather than
   // create a new string for each coverage item.
   final HashCollection<CoverageItem, String> coverageItemToTestUnits = new HashCollection<CoverageItem, String>(true,
      HashCollection.DEFAULT_COLLECTION_TYPE);

   @Override
   public void addTestUnit(CoverageItem coverageItem, String testUnitName) {
      if (!getTestUnits(coverageItem).contains(testUnitName)) {
         coverageItemToTestUnits.put(coverageItem, Strings.intern(testUnitName));
      }
   }

   @Override
   public Collection<String> getTestUnits(CoverageItem coverageItem) {
      if (coverageItemToTestUnits.containsKey(coverageItem)) {
         return coverageItemToTestUnits.getValues(coverageItem);
      }
      return java.util.Collections.emptyList();
   }

   @Override
   public String toXml(CoverageItem coverageItem) {
      return Collections.toString(";", getTestUnits(coverageItem));
   }

   @Override
   public void fromXml(CoverageItem coverageItem, String testUnitNames) {
      if (Strings.isValid(testUnitNames)) {
         for (String testName : testUnitNames.split(";")) {
            addTestUnit(coverageItem, testName);
         }
      }
   }

   @Override
   public void setTestUnits(CoverageItem coverageItem, Collection<String> testUnitNames) {
      coverageItemToTestUnits.removeValues(coverageItem);
      for (String testUnitName : testUnitNames) {
         addTestUnit(coverageItem, testUnitName);
      }
   }

   @Override
   public void removeTestUnit(CoverageItem coverageItem, String testUnitName) {
      coverageItemToTestUnits.removeValue(coverageItem, testUnitName);
   }

   @Override
   public void save(SkynetTransaction transaction) {
      // do nothing cause storeage is in memory only
   }

   @Override
   public void reset() {
      // do nothing
   }

}
