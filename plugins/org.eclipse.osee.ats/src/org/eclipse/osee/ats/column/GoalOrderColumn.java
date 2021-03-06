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

import org.eclipse.nebula.widgets.xviewer.IAltLeftClickProvider;
import org.eclipse.nebula.widgets.xviewer.IXViewerFactory;
import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.artifact.GoalManager;
import org.eclipse.osee.ats.core.client.artifact.GoalArtifact;
import org.eclipse.osee.ats.goal.GoalXViewerFactory;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldLabelProvider;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class GoalOrderColumn extends XViewerAtsColumn implements IXViewerValueColumn, IAltLeftClickProvider {

   public static GoalOrderColumn instance = new GoalOrderColumn();

   public static GoalOrderColumn getInstance() {
      return instance;
   }

   private GoalOrderColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".goalOrder", "Goal Order", 45, SWT.LEFT, false,
         SortDataType.Integer, true, "Order of item within displayed goal.  Editing this field changes order.");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public GoalOrderColumn copy() {
      GoalOrderColumn newXCol = new GoalOrderColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof Artifact && getXViewer().getLabelProvider() instanceof WorldLabelProvider) {
            WorldLabelProvider worldLabelProvider = (WorldLabelProvider) getXViewer().getLabelProvider();
            GoalArtifact parentGoalArtifact = worldLabelProvider.getParentGoalArtifact();
            if (parentGoalArtifact != null) {
               return GoalManager.getGoalOrder(parentGoalArtifact, (Artifact) element);
            }
            return GoalManager.getGoalOrder((Artifact) element);
         }
      } catch (OseeCoreException ex) {
         LogUtil.getCellExceptionString(ex);
      }
      return "";
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      try {
         XViewer xViewer = getXViewer();
         IXViewerFactory xViewerFactory = xViewer.getXViewerFactory();
         GoalArtifact parentGoalArtifact = null;
         if (xViewerFactory instanceof GoalXViewerFactory) {
            parentGoalArtifact = ((GoalXViewerFactory) xViewerFactory).getSoleGoalArtifact();
         }
         if (parentGoalArtifact == null) {
            parentGoalArtifact = getParentGoalArtifact(treeItem);
         }
         GoalArtifact changedGoal = null;
         if (parentGoalArtifact != null) {
            changedGoal = GoalManager.promptChangeGoalOrder(parentGoalArtifact, (Artifact) treeItem.getData());
         } else {
            changedGoal = GoalManager.promptChangeGoalOrder((Artifact) treeItem.getData());
         }
         if (changedGoal != null) {
            xViewer.refresh(changedGoal);
            xViewer.update(treeItem.getData(), null);
         }
         return true;
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         return false;
      }
   }

   public static GoalArtifact getParentGoalArtifact(TreeItem treeItem) {
      if (Widgets.isAccessible(treeItem) && Widgets.isAccessible(treeItem.getParentItem()) && Artifacts.isOfType(
         treeItem.getParentItem().getData(), AtsArtifactTypes.Goal)) {
         return (GoalArtifact) treeItem.getParentItem().getData();
      }
      return null;
   }
}
