/*
 * Created on Oct 5, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.coverage.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoverageImport;
import org.eclipse.osee.coverage.model.CoveragePackage;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.results.XResultData;

/**
 * @author Donald G. Dunne
 */
public class CoveragePackageImporter {

   private final CoveragePackage coveragePackage;
   private final CoverageImport coverageImport;
   private Collection<? extends ICoverage> allImportItems;
   private Set<ICoverage> imported;

   public CoveragePackageImporter(CoveragePackage coveragePackage, CoverageImport coverageImport) {
      this.coveragePackage = coveragePackage;
      this.coverageImport = coverageImport;
   }

   public XResultData validateItems(Collection<? extends ICoverage> allImportItems, XResultData rd) {
      this.allImportItems = allImportItems;
      if (rd == null) rd = new XResultData(false);
      for (ICoverage importItem : allImportItems) {
         if (!(importItem instanceof CoverageUnit)) {
            rd.logError(String.format("Invalid Item for Import; Don't import [%s]",
                  importItem.getClass().getSimpleName()));
            continue;
         }
         if (allImportItems.contains(importItem)) {
            validateChildrenAreUnique(allImportItems, (CoverageUnit) importItem, rd);
         }
      }
      return rd;
   }

   public void validateChildrenAreUnique(Collection<? extends ICoverage> allImportItems, CoverageUnit coverageUnit, XResultData rd) {
      for (ICoverage importItem1 : coverageUnit.getChildren()) {
         for (ICoverage importItem2 : coverageUnit.getChildren()) {
            if (isConceptuallyEqual(importItem1, importItem2) && importItem1 != importItem2) {
               rd.logError(String.format("CoverageUnit [%s] has two equal children [%s][%s]; Can't import.",
                     coverageUnit, importItem1, importItem2));
            }
         }
      }
      for (ICoverage childItem : coverageUnit.getChildren()) {
         if (childItem instanceof CoverageUnit) {
            if (allImportItems.contains(childItem)) {
               validateChildrenAreUnique(allImportItems, (CoverageUnit) childItem, rd);
            }
         }
      }
   }

   public XResultData importItems(ISaveable saveable, Collection<? extends ICoverage> importItems) throws OseeCoreException {
      XResultData rd = new XResultData(false);

      Result result = saveable.isEditable();
      if (result.isFalse()) {
         rd.logError(result.getText());
         return rd;
      }

      validateItems(importItems, rd);
      if (rd.getNumErrors() > 0) return rd;
      imported = new HashSet<ICoverage>();

      List<ICoverage> parentItems = new ArrayList<ICoverage>();
      for (ICoverage importItem : importItems) {
         if (importItem.getParent() instanceof CoverageImport) {
            parentItems.add(importItem);
         }
      }
      importItemsRecurse(parentItems, rd);

      if (rd.getNumErrors() > 0) {
         AWorkbench.popup(rd.getNumErrors() + " Errors Found; Not Persisting");
         rd.logError(rd.getNumErrors() + " Errors Found; Not Persisting");
      } else {
         result = saveable.save();
         if (result.isTrue()) {
            rd.log("\nChanges Persisted");
         } else {
            rd.logError("\n" + result.getText());
         }
      }

      return rd;
   }

   /**
    * Takes import items from coverageImport and applies them to coveragePackage
    */
   public XResultData importItemsRecurse(Collection<ICoverage> localImportItems, XResultData rd) {
      System.out.println("importItemsRecurse => " + localImportItems);
      try {
         for (ICoverage importItem : localImportItems) {
            rd.log("Processing " + importItem.getName());
            if (!(importItem instanceof CoverageUnit)) {
               rd.logError(String.format("[%s] invalid for Import; Only import CoverageUnits",
                     importItem.getClass().getSimpleName()));
               continue;
            }
            CoverageUnit importCoverageUnit = (CoverageUnit) importItem;
            if (imported.contains(importCoverageUnit)) continue;

            ICoverage packageItem = getPackageCoverageItem(importItem, true);
            // Determine if item already exists first
            if (packageItem != null && !packageItem.isFolder()) {
               rd.logError(String.format("Import Item [%s][%s] matches Package Item [%s][%s]- Not Implemented Yet",
                     importItem, importItem.getParent(), packageItem, packageItem.getParent()));
               rd.log("");
               // save assignees and notes and RATIONALE before child overwrites
               // ((CoverageUnit) importItem).updateAssigneesAndNotes((CoverageUnit) packageItem);
               continue;
            }
            if (packageItem == null || (packageItem != null && !packageItem.isFolder())) {
               // This is new item

               // Check if parent item exists
               ICoverage parentImportItem = importItem.getParent();
               // If null, this is top level item, just add to package
               if (parentImportItem instanceof CoverageImport) {
                  coveragePackage.addCoverageUnit(((CoverageUnit) importItem).copy(true));
                  rd.log(String.format("Added [%s] as top level CoverageUnit", importCoverageUnit));
                  imported.add(importItem);
                  rd.log("");
               } else {
                  // Else, want to add item to same parent
                  CoverageUnit parentCoverageUnit = (CoverageUnit) importItem.getParent();
                  CoverageUnit parentPackageItem = (CoverageUnit) getPackageCoverageItem(parentCoverageUnit, true);
                  parentPackageItem.addCoverageUnit(importCoverageUnit.copy(true));
                  imported.add(importCoverageUnit);
                  rd.log(String.format("Added [%s] to parent [%s]", importCoverageUnit, parentCoverageUnit));
                  rd.log("");
               }
            }

            // Import children that are in import list
            for (ICoverage child : importCoverageUnit.getCoverageUnits()) {
               if (allImportItems.contains(child) && !imported.contains(child)) {
                  importItemsRecurse(Collections.singleton(child), rd);
               }
            }
         }
      } catch (Exception ex) {
         rd.logError("Exception: " + ex.getLocalizedMessage());
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return rd;
   }

   public ICoverage getPackageCoverageItem(ICoverage importItem, boolean recurse) {
      for (ICoverage packageItem : coveragePackage.getChildren(recurse)) {
         if (isConceptuallyEqual(packageItem, importItem)) {
            return packageItem;
         }
      }
      return null;
   }

   public static boolean isConceptuallyEqual(ICoverage packageItem, ICoverage importItem) {
      if (packageItem.equals(importItem)) return true;
      if (packageItem.getClass() != importItem.getClass()) return false;
      if (packageItem.getNamespace() == null && importItem.getNamespace() == null) return true;
      if (packageItem.getNamespace() == null) return false;
      if (importItem.getNamespace() == null) return false;
      if (!packageItem.getNamespace().equals(importItem.getNamespace())) return false;
      if (packageItem instanceof CoverageUnit) {
         if (!((CoverageUnit) packageItem).getMethodNumber().equals(((CoverageUnit) importItem).getMethodNumber())) {
            return false;
         }
      }
      if (packageItem.getName().equals(importItem.getName())) {
         if (packageItem.getParent() instanceof CoveragePackage && importItem.getParent() instanceof CoverageImport) {
            return true;
         } else {
            if (isConceptuallyEqual(packageItem.getParent(), importItem.getParent())) {
               return true;
            } else {
               return false;
            }
         }
      }
      return false;
   }
}
