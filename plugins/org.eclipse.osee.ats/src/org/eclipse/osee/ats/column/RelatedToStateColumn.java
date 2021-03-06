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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.dialog.StateListDialog;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class RelatedToStateColumn extends XViewerAtsAttributeValueColumn {

   public static RelatedToStateColumn instance = new RelatedToStateColumn();

   public static RelatedToStateColumn getInstance() {
      return instance;
   }

   private RelatedToStateColumn() {
      super(AtsAttributeTypes.RelatedToState, WorldXViewerFactory.COLUMN_NAMESPACE + ".relatedToState",
         AtsAttributeTypes.RelatedToState.getUnqualifiedName(), 80, SWT.LEFT, false, SortDataType.String, true, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public RelatedToStateColumn copy() {
      RelatedToStateColumn newXCol = new RelatedToStateColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   public static boolean promptChangeRelatedToState(AbstractWorkflowArtifact sma, boolean persist) {
      if (sma.isTask()) {
         return promptChangeRelatedToState(Arrays.asList((TaskArtifact) sma), persist);
      } else {
         AWorkbench.popup("Select Tasks to change Related-to-State");
      }
      return false;
   }

   public static boolean promptChangeRelatedToState(final Collection<? extends TaskArtifact> tasks, boolean persist) {
      if (tasks.isEmpty()) {
         AWorkbench.popup("Select Tasks to change Related-to-State");
         return false;
      }
      try {
         final StateListDialog dialog =
            new StateListDialog("Change Related-to-State", "Select new state for task to be worked in.",
               getValidStates(tasks.iterator().next().getParentTeamWorkflow()));
         if (tasks.size() == 1) {
            String state = tasks.iterator().next().getSoleAttributeValue(AtsAttributeTypes.RelatedToState, "");
            if (Strings.isValid(state)) {
               dialog.setInitialSelections(new Object[] {state});
            }
         }
         if (dialog.open() == 0) {
            if (dialog.getSelectedState().isEmpty()) {
               AWorkbench.popup("No Related-to-State selected");
               return false;
            }
            SkynetTransaction transaction = null;
            if (persist) {
               transaction =
                  TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "ATS Prompt Change Related-to-State");
            }
            for (TaskArtifact task : tasks) {
               String state = task.getSoleAttributeValue(AtsAttributeTypes.RelatedToState, "");
               if (!state.equals(dialog.getSelectedState())) {
                  task.setSoleAttributeFromString(AtsAttributeTypes.RelatedToState, dialog.getSelectedState());
                  if (persist) {
                     task.saveSMA(transaction);
                  }
               }
            }
            if (persist) {
               transaction.execute();
            }
         }
         return true;
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Can't change Related-to-State", ex);
         return false;
      }
   }

   private static List<String> getValidStates(TeamWorkFlowArtifact teamArt) {
      List<String> names = new ArrayList<String>();
      names.addAll(AtsClientService.get().getWorkDefinitionAdmin().getStateNames(teamArt.getWorkDefinition()));
      Collections.sort(names);
      return names;
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      try {
         if (Artifacts.isOfType(treeItem, AtsArtifactTypes.Task)) {
            TaskArtifact taskArt = (TaskArtifact) treeItem.getData();
            boolean modified = promptChangeRelatedToState(taskArt, isPersistViewer());
            XViewer xViewer = ((XViewerColumn) treeColumn.getData()).getTreeViewer();
            if (modified && isPersistViewer(xViewer)) {
               taskArt.persist("persist related-to-state via alt-left-click");
            }
            if (modified) {
               xViewer.update(taskArt, null);
               return true;
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return false;
   }

   @Override
   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      Set<TaskArtifact> tasks = new HashSet<TaskArtifact>();
      for (TreeItem item : treeItems) {
         Artifact art = (Artifact) item.getData();
         if (art.isOfType(AtsArtifactTypes.Task)) {
            tasks.add((TaskArtifact) art);
         }
      }
      if (tasks.isEmpty()) {
         AWorkbench.popup("Invalid selection for setting related-to-state.");
         return;
      }
      promptChangeRelatedToState(tasks, true);
   }

}
