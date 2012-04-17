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

import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.internal.ServiceProvider;
import org.eclipse.osee.coverage.model.CoveragePackage;
import org.eclipse.osee.coverage.store.OseeCoveragePackageStore;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.coverage.util.dialog.CoveragePackageArtifactListDialog;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.cm.IOseeCmService;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;
import org.eclipse.osee.framework.ui.skynet.results.html.XResultPage.Manipulations;

/**
 * @author Donald G. Dunne
 */
public class ImportDispositionsFromPackageTest extends XNavigateItemAction {

   public ImportDispositionsFromPackageTest() {
      super(null, "");
   }

   public ImportDispositionsFromPackageTest(XNavigateItem parent) {
      super(parent, "Import Dispositions From Package - Test");
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) {

      XResultData data = new XResultData(false);
      try {
         IOseeCmService cmService = ServiceProvider.getOseeCmService();
         if (cmService == null) {
            AWorkbench.popup("Can not acquire CM Service");
            return;
         }

         if (!CoverageUtil.getBranchFromUser(false)) {
            return;
         }
         IOseeBranch branch = CoverageUtil.getBranch();

         CoveragePackage fromPackage = null;
         fromPackage = getCoveragePackageFromUser(branch, "FROM");
         if (fromPackage == null) {
            return;
         }

         CoveragePackage toPackage = null;
         toPackage = getCoveragePackageFromUser(branch, "FROM");
         if (toPackage == null) {
            return;
         }

         ImportCoverageMethodsFromPackage importer = new ImportCoverageMethodsFromPackage();
         importer.importDispositionsFromPackage(data, fromPackage, toPackage);
         XResultDataUI.report(data, "Merge Dispositions", Manipulations.HRID_CMD_HYPER, Manipulations.ERROR_RED,
            Manipulations.CONVERT_NEWLINES, Manipulations.WARNING_YELLOW, Manipulations.ERROR_WARNING_HEADER);

      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         data.logErrorWithFormat("Exception [%s] (see log)", ex.getLocalizedMessage());
         XResultDataUI.report(data, "Merge Dispositions - Error");

      }

   }

   private CoveragePackage getCoveragePackageFromUser(IOseeBranch branch, String string) throws OseeCoreException {
      CoveragePackage coveragePackage = null;
      CoveragePackageArtifactListDialog dialog =
         new CoveragePackageArtifactListDialog("Open Coverage Package", "Select Coverage Package - " + string);
      dialog.setInput(OseeCoveragePackageStore.getCoveragePackageArtifacts(branch));

      if (dialog.open() == 0) {
         Artifact coveragePackageArtifact = (Artifact) dialog.getResult()[0];
         OseeCoveragePackageStore store = new OseeCoveragePackageStore(coveragePackageArtifact);
         coveragePackage = store.getCoveragePackage();
      }
      return coveragePackage;
   }
}
