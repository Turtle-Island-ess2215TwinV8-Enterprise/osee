/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.column;

import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.HoursSpentUtil;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class HoursSpentStateTasksColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static HoursSpentStateTasksColumn instance = new HoursSpentStateTasksColumn();

   public static HoursSpentStateTasksColumn getInstance() {
      return instance;
   }

   private HoursSpentStateTasksColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".stateTaskHoursSpent", "State Task Hours Spent", 40, SWT.CENTER,
         false, SortDataType.Float, false,
         "Hours spent in performing the changes for the tasks related to the current state.");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public HoursSpentStateTasksColumn copy() {
      HoursSpentStateTasksColumn newXCol = new HoursSpentStateTasksColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof Artifact) {
            return AtsUtilCore.doubleToI18nString(HoursSpentUtil.getHoursSpentFromStateTasks((Artifact) element));
         }
      } catch (OseeCoreException ex) {
         return LogUtil.getCellExceptionString(ex);
      }
      return "";
   }

}
