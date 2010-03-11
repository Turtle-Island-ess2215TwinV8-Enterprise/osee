/*
 * Created on Jan 2, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.coverage.model;

import java.util.Collection;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * Simple provider that optimizes how test units are stored by sharing test unit names.
 * 
 * @author Donald G. Dunne
 */
public class SimpleTestUnitProvider implements ITestUnitProvider {

   // Since test units will cover many coverage items (sometimes thousands), it is more cost effective
   // to store single test script name shared by use of string.intern() rather than
   // create a new string for each coverage item.
   final HashCollection<CoverageItem, String> coverageItemToTestUnits = new HashCollection<CoverageItem, String>(1000);

   public SimpleTestUnitProvider() {
   }

   @Override
   public void addTestUnit(CoverageItem coverageItem, String testUnitName) throws OseeCoreException {
      if (!getTestUnits(coverageItem).contains(testUnitName)) {
         coverageItemToTestUnits.put(coverageItem, Strings.intern(testUnitName));
      }
   }

   @Override
   public Collection<String> getTestUnits(CoverageItem coverageItem) throws OseeCoreException {
      if (coverageItemToTestUnits.containsKey(coverageItem)) {
         return coverageItemToTestUnits.getValues(coverageItem);
      }
      return java.util.Collections.emptyList();
   }

   @Override
   public String toXml(CoverageItem coverageItem) throws OseeCoreException {
      return Collections.toString(";", getTestUnits(coverageItem));
   }

   @Override
   public void fromXml(CoverageItem coverageItem, String testUnitNames) throws OseeCoreException {
      if (Strings.isValid(testUnitNames)) {
         for (String testName : testUnitNames.split(";")) {
            addTestUnit(coverageItem, testName);
         }
      }
   }

   @Override
   public void setTestUnits(CoverageItem coverageItem, Collection<String> testUnitNames) throws OseeCoreException {
      coverageItemToTestUnits.removeValues(coverageItem);
      for (String testUnitName : testUnitNames) {
         addTestUnit(coverageItem, testUnitName);
      }
   }

   @Override
   public void removeTestUnit(CoverageItem coverageItem, String testUnitName) throws OseeCoreException {
      coverageItemToTestUnits.removeValue(coverageItem, testUnitName);
   }

}
