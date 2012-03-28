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
package org.eclipse.osee.coverage.util;

import org.eclipse.osee.coverage.editor.CoverageEditor;
import org.eclipse.osee.coverage.editor.CoverageEditorInput;
import org.eclipse.osee.coverage.model.CoverageOptionManagerDefault;
import org.eclipse.osee.coverage.model.CoveragePackage;
import org.eclipse.osee.coverage.model.SimpleWorkProductTaskProvider;
import org.eclipse.osee.coverage.store.OseeCoveragePackageStore;
import org.eclipse.osee.framework.skynet.core.utility.IncrementingNum;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;

/**
 * @author Donald G. Dunne
 */
public class CpCreateAndImportItem extends XNavigateItemAction {

   private final String blamImportName;
   private CoveragePackage coveragePackage;

   public CpCreateAndImportItem(XNavigateItem parent, String name, String blamImportName) {
      super(parent, name);
      this.blamImportName = blamImportName;
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) throws Exception {
      coveragePackage =
         new CoveragePackage(getName() + " - #" + IncrementingNum.get(), CoverageOptionManagerDefault.instance(),
            new SimpleWorkProductTaskProvider());
      OseeCoveragePackageStore store = new OseeCoveragePackageStore(coveragePackage, CoverageTestUtil.getTestBranch());
      store.save(coveragePackage.getName(), coveragePackage.getCoverageOptionManager());
      CoverageEditor.open(new CoverageEditorInput(coveragePackage.getName(), store.getArtifact(false), coveragePackage,
         true));
      // Process Import 1
      CoverageEditor editor = null;
      for (CoverageEditor coverageEditor : CoverageEditor.getEditors()) {
         if (coverageEditor.getCoverageEditorInput().getCoveragePackageBase() instanceof CoveragePackage) {
            CoveragePackage editorPackage =
               (CoveragePackage) coverageEditor.getCoverageEditorInput().getCoveragePackageBase();
            if (editorPackage.getGuid().equals(coveragePackage.getGuid())) {
               editor = coverageEditor;
            }
         }
      }
      if (editor == null) {
         AWorkbench.popup("Can't access opened Editor");
         return;
      }
      editor.simulateImport(blamImportName);
   }

   public CoveragePackage getCoveragePackage() {
      return coveragePackage;
   }
}
