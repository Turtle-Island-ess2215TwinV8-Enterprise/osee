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

import java.util.HashSet;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.core.action.ActionManager;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.swt.SWT;

public class CompletedByColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static CompletedByColumn instance = new CompletedByColumn();

   public static CompletedByColumn getInstance() {
      return instance;
   }

   private CompletedByColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".completedBy", "Completed By", 80, SWT.LEFT, false,
         SortDataType.Date, false, "User transitioning action to completed state.");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public CompletedByColumn copy() {
      CompletedByColumn newXCol = new CompletedByColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof AbstractWorkflowArtifact) {
            IBasicUser user = ((AbstractWorkflowArtifact) element).getCompletedBy();
            if (user != null) {
               return user.getName();
            }
         } else if (Artifacts.isOfType(element, AtsArtifactTypes.Action)) {
            Set<IBasicUser> users = new HashSet<IBasicUser>();
            for (TeamWorkFlowArtifact team : ActionManager.getTeams(element)) {
               IBasicUser user = team.getCompletedBy();
               if (user != null) {
                  users.add(user);
               }
            }
            return Artifacts.toString(";", users);

         }
      } catch (OseeCoreException ex) {
         LogUtil.getCellExceptionString(ex);
      }
      return "";
   }
}
