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
package org.eclipse.osee.coverage.editor.xcover;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoverageItem;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.utility.UsersByIds;
import org.eclipse.osee.framework.ui.skynet.FrameworkArtifactImageProvider;
import org.eclipse.osee.framework.ui.skynet.ImageManager;
import org.eclipse.swt.graphics.Image;

public class CoverageLabelProvider extends XViewerLabelProvider {

   private final CoverageXViewer xViewer;

   public CoverageLabelProvider(CoverageXViewer xViewer) {
      super(xViewer);
      this.xViewer = xViewer;
   }

   public static Image getCoverageItemUserImage(ICoverage coverageItem) {
      try {
         if (coverageItem.isAssignable() && Strings.isValid(coverageItem.getAssignees())) {
            return FrameworkArtifactImageProvider.getUserImage(UsersByIds.getUsers(coverageItem.getAssignees()));
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE, ex);
      }
      return null;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException {
      ICoverage coverageItem = (ICoverage) element;
      if (xCol.equals(CoverageXViewerFactory.Assignees_Col)) {
         return getCoverageItemUserImage(coverageItem);
      }
      if (xCol.equals(CoverageXViewerFactory.Name)) return ImageManager.getImage(coverageItem.getOseeImage());
      return null;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException {
      ICoverage coverage = (ICoverage) element;
      if (xCol.equals(CoverageXViewerFactory.Assignees_Col)) {
         return CoverageUtil.getCoverageItemUsersStr(coverage);
      }
      if (xCol.equals(CoverageXViewerFactory.Notes_Col)) return coverage.getNotes();
      if (xCol.equals(CoverageXViewerFactory.Name)) return coverage.getName();
      if (xCol.equals(CoverageXViewerFactory.Coverage_Percent)) {
         return coverage.getCoveragePercentStr();
      }
      if (xCol.equals(CoverageXViewerFactory.Location)) return coverage.getLocation();
      if (xCol.equals(CoverageXViewerFactory.Namespace)) return coverage.getNamespace();
      if (xCol.equals(CoverageXViewerFactory.Guid)) return coverage.getGuid();

      if (coverage instanceof CoverageItem) {
         CoverageItem coverageItem = (CoverageItem) coverage;
         if (xCol.equals(CoverageXViewerFactory.Line_Number)) return coverageItem.getLineNum();
         if (xCol.equals(CoverageXViewerFactory.Coverage_Rationale)) return coverageItem.getCoverageRationale();
         if (xCol.equals(CoverageXViewerFactory.Method_Number)) return coverageItem.getMethodNum();
         if (xCol.equals(CoverageXViewerFactory.Execution_Number)) return coverageItem.getExecuteNum();
         if (xCol.equals(CoverageXViewerFactory.Coverage_Method)) return coverageItem.getCoverageMethod().toString();
         if (xCol.equals(CoverageXViewerFactory.Parent_Coverage_Unit)) return coverageItem.getCoverageUnit().getName();
         if (xCol.equals(CoverageXViewerFactory.Coverage_Test_Units)) return Collections.toString(", ",
               coverageItem.getTestUnits());
         if (xCol.equals(CoverageXViewerFactory.Text)) return Collections.toString(", ", coverageItem.getText());
         return "";
      }
      if (coverage instanceof CoverageUnit) {
         CoverageUnit coverageUnit = (CoverageUnit) coverage;
         if (xCol.equals(CoverageXViewerFactory.Parent_Coverage_Unit)) return coverageUnit.getParentCoverageUnit() == null ? "" : coverageUnit.getParentCoverageUnit().getName();
         if (xCol.equals(CoverageXViewerFactory.Method_Number)) return coverageUnit.getMethodNumber();
      }
      return "";

   }

   public void dispose() {
   }

   public boolean isLabelProperty(Object element, String property) {
      return false;
   }

   public void addListener(ILabelProviderListener listener) {
   }

   public void removeListener(ILabelProviderListener listener) {
   }

   public CoverageXViewer getTreeViewer() {
      return xViewer;
   }

   @Override
   public int getColumnGradient(Object element, XViewerColumn xCol, int columnIndex) throws Exception {
      ICoverage coverageItem = (ICoverage) element;
      if (xCol.equals(CoverageXViewerFactory.Coverage_Percent)) {
         return coverageItem.getCoveragePercent();
      }
      return 0;
   }
}
