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

import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.core.client.action.ActionArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class ParentHridColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static ParentHridColumn instance = new ParentHridColumn();

   public static ParentHridColumn getInstance() {
      return instance;
   }

   private ParentHridColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".parenthrid", "Parent HRID", 75, SWT.LEFT, false,
         SortDataType.String, false, "Human Readable ID of Parent Action or Team Workflow");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public ParentHridColumn copy() {
      ParentHridColumn newXCol = new ParentHridColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (Artifacts.isOfType(element, AtsArtifactTypes.TeamWorkflow)) {
            ActionArtifact parentAction = ((TeamWorkFlowArtifact) element).getParentActionArtifact();
            if (parentAction != null) {
               return parentAction.getHumanReadableId();
            }
         } else if (element instanceof AbstractWorkflowArtifact && ((AbstractWorkflowArtifact) element).getParentAWA() != null) {
            return ((AbstractWorkflowArtifact) element).getParentAWA().getHumanReadableId();
         }

      } catch (OseeCoreException ex) {
         return LogUtil.getCellExceptionString(ex);
      }
      return "";
   }

}
