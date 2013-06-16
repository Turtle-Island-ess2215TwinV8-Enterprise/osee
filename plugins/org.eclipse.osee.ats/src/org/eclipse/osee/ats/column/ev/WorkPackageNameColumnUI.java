/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.column.ev;

import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.core.AtsCore;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class WorkPackageNameColumnUI extends AbstractWorkPackageRelatedColumnUI {

   private static WorkPackageNameColumnUI instance = new WorkPackageNameColumnUI();

   public static WorkPackageNameColumnUI getInstance() {
      return instance;
   }

   private WorkPackageNameColumnUI() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".workPackageName", "Work Package Name", 80, SWT.LEFT, false,
         SortDataType.String, true, AtsCore.getColumnUtilities().getWorkPackageNameUtility().getDescription());
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public WorkPackageNameColumnUI copy() {
      WorkPackageNameColumnUI newXCol = new WorkPackageNameColumnUI();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      return AtsCore.getColumnUtilities().getWorkPackageNameUtility().getColumnText(element);
   }
}