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
package org.eclipse.osee.ats.column;

import java.util.logging.Level;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class WorkPackageColumn extends XViewerAtsAttributeValueColumn {

   public static WorkPackageColumn instance = new WorkPackageColumn();

   public static WorkPackageColumn getInstance() {
      return instance;
   }

   protected WorkPackageColumn() {
      super(AtsAttributeTypes.WorkPackage, WorldXViewerFactory.COLUMN_NAMESPACE + ".workPackage",
         AtsAttributeTypes.WorkPackage.getUnqualifiedName(), 80, SWT.LEFT, false, SortDataType.String, true, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public WorkPackageColumn copy() {
      WorkPackageColumn newXCol = new WorkPackageColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      if (Artifacts.isOfType(element, AtsArtifactTypes.Task)) {
         try {
            return getColumnText(((TaskArtifact) element).getParentTeamWorkflow(), column, columnIndex);
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      return super.getColumnText(element, column, columnIndex);
   }
}
