/*
 * Created on Nov 20, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.coverage.demo.examples.import10;

import org.eclipse.osee.coverage.demo.CoverageBranches;
import org.eclipse.osee.coverage.model.CoverageItem;
import org.eclipse.osee.coverage.model.CoverageOptionManager;
import org.eclipse.osee.coverage.model.CoveragePackage;
import org.eclipse.osee.coverage.model.CoveragePackageBase;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.store.OseeCoveragePackageStore;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.coverage.util.dialog.CoveragePackageArtifactListDialog;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

public class CoveragePackageTestUtil {

   public static Artifact getSelectedCoveragePackageFromDialog() throws OseeCoreException {
      CoveragePackageArtifactListDialog dialog =
         new CoveragePackageArtifactListDialog("Open Coverage Package", "Select Coverage Package");
      if (!CoverageUtil.getBranchFromUser(false)) {
         return null;
      }
      dialog.setInput(OseeCoveragePackageStore.getCoveragePackageArtifacts(CoverageUtil.getBranch()));
      if (dialog.open() != 0) {
         return null;
      }
      Artifact coveragePackageArtifact = (Artifact) dialog.getResult()[0];
      return coveragePackageArtifact;
   }

   public static ICoverage getFirstCoverageByNameEquals(CoveragePackageBase coveragePackageBase, String name) {
      for (ICoverage coverage : coveragePackageBase.getChildren(true)) {
         if (coverage.getName().equals(name)) {
            return coverage;
         }
      }
      return null;
   }

   public static CoverageItem getNavigateButton2getTextLine3CoverageItem(CoveragePackage coveragePackage) {
      ICoverage coverage = getFirstCoverageByNameEquals(coveragePackage, "NavigationButton2.java");
      CoverageUnit coverageUnit = (CoverageUnit) coverage;
      for (CoverageUnit childCU : coverageUnit.getCoverageUnits()) {
         if (childCU.getName().equals("getText")) {
            for (CoverageItem item : childCU.getCoverageItems()) {
               if (item.getOrderNumber().equals("3")) {
                  return item;
               }
            }
         }
      }
      return null;
   }

   public static CoverageItem getNavigateButton2getImageLine3CoverageItem(CoveragePackage coveragePackage) {
      ICoverage coverage = getFirstCoverageByNameEquals(coveragePackage, "NavigationButton2.java");
      CoverageUnit coverageUnit = (CoverageUnit) coverage;
      for (CoverageUnit childCU : coverageUnit.getCoverageUnits()) {
         if (childCU.getName().equals("getImage")) {
            for (CoverageItem item : childCU.getCoverageItems()) {
               if (item.getOrderNumber().equals("3")) {
                  return item;
               }
            }
         }
      }
      return null;
   }

   public static Result setupCoveragePackageForImport10(CoveragePackage coveragePackage, boolean testWithDb) {
      String errStr = null;

      CoverageItem item = getNavigateButton2getTextLine3CoverageItem(coveragePackage);
      if (item == null) {
         errStr = "NavigationButton.java/getText/line 3 not found\n";
      } else {
         item.setCoverageMethod(CoverageOptionManager.Deactivated_Code);
      }

      item = getNavigateButton2getImageLine3CoverageItem(coveragePackage);
      if (item == null) {
         errStr += "NavigationButton.java/getImage/line 3 not found";
      } else {
         item.setCoverageMethod(CoverageOptionManager.Deactivated_Code);
         item.setRationale("This is the rationale");
      }

      if (Strings.isValid(errStr)) {
         return new Result(errStr);
      }

      if (testWithDb) {
         OseeCoveragePackageStore packageStore =
            new OseeCoveragePackageStore(coveragePackage, CoverageBranches.SAW_Bld_1);
         packageStore.save(coveragePackage.getName(), coveragePackage.getCoverageOptionManager());
      }

      return Result.TrueResult;
   }
}