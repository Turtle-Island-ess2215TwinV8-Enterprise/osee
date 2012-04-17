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
package org.eclipse.osee.coverage.navigate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.coverage.action.ConfigureCoverageMethodsAction;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.merge.MatchItem;
import org.eclipse.osee.coverage.merge.MergeManager;
import org.eclipse.osee.coverage.model.CoverageItem;
import org.eclipse.osee.coverage.model.CoverageOptionManager;
import org.eclipse.osee.coverage.model.CoverageOptionManagerDefault;
import org.eclipse.osee.coverage.model.CoveragePackage;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.model.ICoverageItemProvider;
import org.eclipse.osee.coverage.model.ICoverageUnitProvider;
import org.eclipse.osee.coverage.store.OseeCoveragePackageStore;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;
import org.eclipse.osee.framework.ui.skynet.results.html.XResultPage.Manipulations;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Donald G. Dunne
 */
public class ImportCoverageMethodsFromPackage extends XNavigateItemAction {
   private final List<String> autoDispos = Arrays.asList(CoverageOptionManagerDefault.Exception_Handling.name,
      CoverageOptionManagerDefault.Not_Covered.name, CoverageOptionManagerDefault.Test_Unit.name);

   public ImportCoverageMethodsFromPackage() {
      super(null, "Import Coverage Methods from Another Package", PluginUiImage.ADMIN);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) {
      if (!MessageDialog.openConfirm(Displays.getActiveShell(), getName(), getName())) {
         return;
      }

      XResultData data = new XResultData(false);
      try {
         Branch branch = BranchManager.getBranchByGuid("GZFSJy6aBHqmWWcD3egA");
         if (!branch.getName().contains("Don -")) {
            AWorkbench.popup("Can't find Don branch");
            return;
         }
         Artifact fromPackageArt = ArtifactQuery.getArtifactFromId("AGCe8afthldTJu8h7ewA", branch);
         if (!fromPackageArt.getName().equals("CND_Coverage")) {
            AWorkbench.popup("Can't find CND_Coverage");
            return;
         }

         Artifact toPackageArt = ArtifactQuery.getArtifactFromId("A_I2_tNqbHEfoJGx9YQA", branch);
         if (!toPackageArt.getName().equals("CND_Coverage_New")) {
            AWorkbench.popup("Can't find CND_Coverage_New");
            return;
         }

         @SuppressWarnings("unused")
         Set<Artifact> artifactLoadCache = ConfigureCoverageMethodsAction.bulkLoadCoveragePackage(fromPackageArt);
         OseeCoveragePackageStore store = new OseeCoveragePackageStore(fromPackageArt);
         CoveragePackage fromPackage = store.getCoveragePackage();

         @SuppressWarnings("unused")
         Set<Artifact> artifactLoadCache2 = ConfigureCoverageMethodsAction.bulkLoadCoveragePackage(toPackageArt);
         store = new OseeCoveragePackageStore(toPackageArt);
         CoveragePackage toPackage = store.getCoveragePackage();

         if (!MessageDialog.openConfirm(Displays.getActiveShell(), getName(),
            String.format("Merging dispositions from [%s] to [%s]\n\n", fromPackage.getName(), toPackage.getName()))) {
            return;
         }

         importDispositionsFromPackage(data, fromPackage, toPackage);

         XResultDataUI.report(data, "Merge Dispositions", Manipulations.HRID_CMD_HYPER, Manipulations.ERROR_RED,
            Manipulations.CONVERT_NEWLINES, Manipulations.WARNING_YELLOW, Manipulations.ERROR_WARNING_HEADER);

      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         data.logErrorWithFormat("Exception [%s] (see log)", ex.getLocalizedMessage());
         XResultDataUI.report(data, "Merge Dispositions - Error");

      }
      AWorkbench.popup("Completed", "Complete");
   }

   public void importDispositionsFromPackage(XResultData data, CoveragePackage fromPackage, CoveragePackage toPackage) throws OseeStateException {
      data.logWithFormat("Merging dispositions from [%s] to [%s]\n\n", fromPackage.getName(), toPackage.getName());
      DispoCounter counter = new DispoCounter();
      processDispositionsRecurse(counter, data, fromPackage, toPackage);
      data.log("\n\nTotals: " + counter.toString());
   }

   public class DispoCounter {
      public int numItems = 0;
      public int numDispo = 0;
      public int numMatch = 0;
      public int numNoMatch = 0;

      @Override
      public String toString() {
         return String.format("Num Items %s; Num Dispo %s; Num Match %s; Num NoMatch %s", numItems, numDispo, numMatch,
            numNoMatch);
      }
   }

   private void processDispositionsRecurse(DispoCounter counter, XResultData data, ICoverage coverage, CoveragePackage toPackage) throws OseeStateException {
      if (coverage instanceof CoverageItem) {
         counter.numItems++;
         CoverageItem item = (CoverageItem) coverage;
         if (isManualDisp(item)) {
            counter.numDispo++;
            data.logWithFormat("%s - Merge disp [%s] ", CoverageUtil.getFullPath(item, false),
               item.getCoverageMethod().name);
            importDisposition(counter, data, item, toPackage);
         }
      }
      if (coverage instanceof ICoverageUnitProvider) {
         ICoverageUnitProvider unitProvider = (ICoverageUnitProvider) coverage;
         for (CoverageUnit unit : unitProvider.getCoverageUnits()) {
            System.out.println("Merging " + unit.getName());
            processDispositionsRecurse(counter, data, unit, toPackage);
         }
      }
      if (coverage instanceof ICoverageItemProvider) {
         ICoverageItemProvider itemProvider = (ICoverageItemProvider) coverage;
         for (CoverageItem unit : itemProvider.getCoverageItems()) {
            processDispositionsRecurse(counter, data, unit, toPackage);
         }
      }
   }

   private void importDisposition(DispoCounter counter, XResultData data, CoverageItem fromItem, CoveragePackage toPackage) throws OseeStateException {
      // First, attempt to find this coverage item
      MatchItem matchItem = MergeManager.getPackageCoverageItem(toPackage, fromItem);
      if (matchItem.isMatch()) {
         counter.numMatch++;
         data.logWithFormat("MATCH [%s][%s]", matchItem.getPackageItem().getOrderNumber(),
            matchItem.getImportItem().getOrderNumber());
         CoverageItem toItem = (CoverageItem) matchItem.getPackageItem();
         if (toItem.getCoverageMethod().name.equals(CoverageOptionManager.Test_Unit.name) || toItem.getCoverageMethod().name.equals(CoverageOptionManager.Exception_Handling.name)) {
            data.logWithFormat(" - KEEP CURRENT [%s]\n", toItem.getCoverageMethod().name);
         } else {
            data.log(" - IMPORTED");
         }

      } else {
         counter.numNoMatch++;
         data.logError("NO MATCH");
      }
   }

   private boolean isManualDisp(CoverageItem item) {
      return !autoDispos.contains(item.getCoverageMethod().name);
   }
}
