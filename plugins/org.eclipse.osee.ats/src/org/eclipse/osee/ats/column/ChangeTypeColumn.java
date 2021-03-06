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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.client.workflow.ChangeType;
import org.eclipse.osee.ats.core.client.workflow.ChangeTypeUtil;
import org.eclipse.osee.ats.core.config.AtsVersionService;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.ats.workflow.ChangeTypeDialog;
import org.eclipse.osee.ats.workflow.ChangeTypeToSwtImage;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class ChangeTypeColumn extends XViewerAtsAttributeValueColumn {

   public static ChangeTypeColumn instance = new ChangeTypeColumn();

   public static ChangeTypeColumn getInstance() {
      return instance;
   }

   private ChangeTypeColumn() {
      super(AtsAttributeTypes.ChangeType, 22, SWT.CENTER, true, SortDataType.String, true, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public ChangeTypeColumn copy() {
      ChangeTypeColumn newXCol = new ChangeTypeColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   public static boolean promptChangeType(AbstractWorkflowArtifact sma, boolean persist) {
      if (sma.isTeamWorkflow()) {
         return promptChangeType(Arrays.asList((TeamWorkFlowArtifact) sma), persist);
      }
      return false;
   }

   public static boolean promptChangeType(final Collection<? extends TeamWorkFlowArtifact> teams, boolean persist) {

      try {
         for (TeamWorkFlowArtifact team : teams) {
            if (AtsVersionService.get().isReleased(team) || AtsVersionService.get().isVersionLocked(team)) {
               AWorkbench.popup("ERROR",
                  "Team Workflow\n \"" + team.getName() + "\"\n version is locked or already released.");
               return false;
            }
         }
         final ChangeTypeDialog dialog = new ChangeTypeDialog(Displays.getActiveShell());
         if (teams.size() == 1) {
            ChangeType changeType = ChangeTypeUtil.getChangeType(teams.iterator().next());
            if (changeType != null) {
               dialog.setSelected(changeType);
            }
         }
         if (dialog.open() == 0) {

            SkynetTransaction transaction = null;
            if (persist) {
               transaction = TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "ATS Prompt Change Type");
            }

            ChangeType newChangeType = dialog.getSelection();
            for (TeamWorkFlowArtifact team : teams) {
               ChangeType currChangeType = ChangeTypeUtil.getChangeType(team);
               if (currChangeType != newChangeType) {
                  ChangeTypeUtil.setChangeType(team, newChangeType);
                  if (persist) {
                     team.saveSMA(transaction);
                  }
               }
            }
            if (persist) {
               transaction.execute();
            }
         }
         return true;
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Can't change priority", ex);
         return false;
      }
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      try {
         // Only prompt change for sole attribute types
         if (AttributeTypeManager.getMaxOccurrences(getAttributeType()) != 1) {
            return false;
         }
         if (treeItem.getData() instanceof Artifact) {
            Artifact useArt = (Artifact) treeItem.getData();
            if (useArt.isOfType(AtsArtifactTypes.Action)) {
               if (ActionManager.getTeams(useArt).size() == 1) {
                  useArt = ActionManager.getFirstTeam(useArt);
               } else {
                  return false;
               }
            }
            if (!(useArt.isOfType(AtsArtifactTypes.TeamWorkflow))) {
               return false;
            }
            boolean modified = promptChangeType(Arrays.asList((TeamWorkFlowArtifact) useArt), isPersistViewer());
            XViewer xViewer = ((XViewerColumn) treeColumn.getData()).getTreeViewer();
            if (modified && isPersistViewer(xViewer)) {
               useArt.persist("persist change type via alt-left-click");
            }
            if (modified) {
               xViewer.update(useArt, null);
               return true;
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

      return false;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn column, int columnIndex) {
      try {
         Artifact useArt = getParentTeamWorkflowOrArtifact(element);
         if (useArt != null) {
            ChangeType changeType = ChangeTypeUtil.getChangeType(useArt);
            if (changeType != null) {
               return ChangeTypeToSwtImage.getImage(changeType);
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }

   @Override
   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      Set<TeamWorkFlowArtifact> awas = new HashSet<TeamWorkFlowArtifact>();
      for (TreeItem item : treeItems) {
         Artifact art = (Artifact) item.getData();
         if (art.isOfType(AtsArtifactTypes.TeamWorkflow)) {
            awas.add((TeamWorkFlowArtifact) art);
         }
      }
      promptChangeType(awas, true);
      getXViewer().update(awas.toArray(), null);
   }

}
