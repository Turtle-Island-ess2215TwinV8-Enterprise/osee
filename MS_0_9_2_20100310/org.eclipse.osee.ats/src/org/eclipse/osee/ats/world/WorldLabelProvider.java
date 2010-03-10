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
package org.eclipse.osee.ats.world;

import java.util.logging.Level;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.osee.ats.artifact.TaskArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.dialog.TaskResOptionDefinition;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class WorldLabelProvider extends XViewerLabelProvider {

   private final WorldXViewer treeViewer;
   protected Font font;

   public WorldLabelProvider(WorldXViewer treeViewer) {
      super(treeViewer);
      this.treeViewer = treeViewer;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) {
      try {
         if (!(element instanceof IWorldViewArtifact)) {
            return null;
         }
         IWorldViewArtifact wva = (IWorldViewArtifact) element;
         if (xCol.equals(WorldXViewerFactory.Change_Type_Col)) {
            return wva.getWorldViewChangeType().getImage();
         } else if (xCol.equals(WorldXViewerFactory.Type_Col)) {
            return ArtifactImageManager.getImage((Artifact) element);
         } else if (xCol.equals(WorldXViewerFactory.Assignees_Col)) {
            return wva.getAssigneeImage();
         } else if (xCol.equals(WorldXViewerFactory.Deadline_Col)) {
            if (wva.isWorldViewDeadlineAlerting().isTrue()) {
               return ImageManager.getImage(FrameworkImage.WARNING);
            }
         } else if (xCol.equals(WorldXViewerFactory.Artifact_Type_Col)) {
            return ArtifactImageManager.getImage((Artifact) wva);
         }
         for (IAtsWorldEditorItem item : AtsWorldEditorItems.getItems()) {
            if (item.isXColumnProvider(xCol)) {
               Image image = item.getColumnImage(element, xCol, columnIndex);
               if (image != null) {
                  return image;
               }
            }
         }
      } catch (Exception ex) {
         // do nothing
      }
      return null;
   }

   @Override
   public Color getForeground(Object element, XViewerColumn xCol, int columnIndex) {
      try {
         if (element instanceof TaskArtifact && xCol.equals(WorldXViewerFactory.Resolution_Col)) {
            TaskArtifact taskArt = (TaskArtifact) element;
            TaskResOptionDefinition def = taskArt.getTaskResolutionOptionDefinition(taskArt.getWorldViewResolution());
            if (def != null) {
               return Display.getCurrent().getSystemColor(def.getColorInt());
            }
         }
         for (IAtsWorldEditorItem item : AtsWorldEditorItems.getItems()) {
            if (item.isXColumnProvider(xCol)) {
               Color color = item.getForeground(element, xCol, columnIndex);
               if (color != null) {
                  return color;
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
      return null;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn xCol, int columnIndex) {
      try {
         // NOTE: HRID, Type, Title are handled by XViewerValueColumn values
         if (!(element instanceof IWorldViewArtifact)) {
            return "";
         }
         IWorldViewArtifact wva = (IWorldViewArtifact) element;
         Artifact art = (Artifact) element;
         if (art.isDeleted()) {
            if (xCol.equals(WorldXViewerFactory.ID_Col)) {
               return art.getHumanReadableId();
            } else if (xCol.equals(WorldXViewerFactory.Title_Col)) {
               return art.getName();
            } else {
               return "<deleted>";
            }
         }
         if (xCol.equals(WorldXViewerFactory.Type_Col)) {
            return wva.getWorldViewType();
         }
         if (xCol.equals(WorldXViewerFactory.State_Col)) {
            return wva.getWorldViewState();
         }
         if (xCol.equals(WorldXViewerFactory.Assignees_Col)) {
            return wva.getWorldViewActivePoc();
         }
         if (xCol.equals(WorldXViewerFactory.Change_Type_Col)) {
            return wva.getWorldViewChangeTypeStr();
         }
         if (xCol.equals(WorldXViewerFactory.Priority_Col)) {
            return wva.getWorldViewPriority();
         }
         if (xCol.equals(WorldXViewerFactory.Actionable_Items_Col)) {
            return wva.getWorldViewActionableItems();
         }
         if (xCol.equals(WorldXViewerFactory.User_Community_Col)) {
            return wva.getWorldViewUserCommunity();
         }
         if (xCol.equals(WorldXViewerFactory.Version_Target_Col)) {
            return wva.getWorldViewTargetedVersionStr();
         }
         if (xCol.equals(WorldXViewerFactory.Notes_Col)) {
            return wva.getWorldViewNotes();
         }

         if (xCol.equals(WorldXViewerFactory.Resolution_Col)) {
            return wva.getWorldViewResolution();
         }
         if (xCol.equals(WorldXViewerFactory.Groups_Col)) {
            return wva.getWorldViewGroups();
         }
         if (xCol.equals(WorldXViewerFactory.Goals_Col)) {
            return wva.getWorldViewGoals();
         }
         if (xCol.equals(WorldXViewerFactory.Legacy_PCR_Col)) {
            return wva.getWorldViewLegacyPCR();
         }
         if (xCol.equals(WorldXViewerFactory.Created_Date_Col)) {
            return wva.getWorldViewCreatedDateStr();
         }
         if (xCol.equals(WorldXViewerFactory.Completed_Date_Col)) {
            return wva.getWorldViewCompletedDateStr();
         }
         if (xCol.equals(WorldXViewerFactory.Cancelled_Date_Col)) {
            return wva.getWorldViewCancelledDateStr();
         }
         if (xCol.equals(WorldXViewerFactory.Team_Col)) {
            return wva.getWorldViewTeam();
         }
         if (xCol.equals(WorldXViewerFactory.Related_To_State_Col)) {
            return wva.getWorldViewRelatedToState();
         }
         if (xCol.equals(WorldXViewerFactory.Originator_Col)) {
            return wva.getWorldViewOriginator();
         }
         if (xCol.equals(WorldXViewerFactory.Branch_Status_Col)) {
            return wva.getWorldViewBranchStatus();
         }
         if (xCol.equals(WorldXViewerFactory.Decision_Col)) {
            return wva.getWorldViewDecision();
         }
         if (xCol.equals(WorldXViewerFactory.Estimated_Hours_Col)) {
            return AtsUtil.doubleToI18nString(wva.getWorldViewEstimatedHours());
         }
         if (xCol.equals(WorldXViewerFactory.Remaining_Hours_Col)) {
            Result result = wva.isWorldViewRemainHoursValid();
            if (result.isFalse()) {
               return result.getText();
            }
            return AtsUtil.doubleToI18nString(wva.getWorldViewRemainHours());
         }
         if (xCol.equals(WorldXViewerFactory.Percent_Complete_State_Col)) {
            return String.valueOf(wva.getWorldViewPercentCompleteState());
         }
         if (xCol.equals(WorldXViewerFactory.Percent_Complete_State_Task_Col)) {
            return String.valueOf(wva.getWorldViewPercentCompleteStateTask());
         }
         if (xCol.equals(WorldXViewerFactory.Percent_Complete_State_Review_Col)) {
            return String.valueOf(wva.getWorldViewPercentCompleteStateReview());
         }
         if (xCol.equals(WorldXViewerFactory.Percent_Complete_Total_Col)) {
            return String.valueOf(wva.getWorldViewPercentCompleteTotal());
         }
         if (xCol.equals(WorldXViewerFactory.Hours_Spent_State_Col)) {
            return AtsUtil.doubleToI18nString(wva.getWorldViewHoursSpentState());
         }
         if (xCol.equals(WorldXViewerFactory.Hours_Spent_State_Task_Col)) {
            return AtsUtil.doubleToI18nString(wva.getWorldViewHoursSpentStateTask());
         }
         if (xCol.equals(WorldXViewerFactory.Hours_Spent_State_Review_Col)) {
            return AtsUtil.doubleToI18nString(wva.getWorldViewHoursSpentStateReview());
         }
         if (xCol.equals(WorldXViewerFactory.Hours_Spent_Total_Col)) {
            return AtsUtil.doubleToI18nString(wva.getWorldViewHoursSpentStateTotal());
         }

         if (xCol.equals(WorldXViewerFactory.Total_Hours_Spent_Col)) {
            return AtsUtil.doubleToI18nString(wva.getWorldViewHoursSpentTotal());
         }

         if (xCol.equals(WorldXViewerFactory.Percent_Rework_Col)) {
            return wva.getWorldViewPercentReworkStr();
         }
         if (xCol.equals(WorldXViewerFactory.Estimated_Release_Date_Col)) {
            return wva.getWorldViewEstimatedReleaseDateStr();
         }
         if (xCol.equals(WorldXViewerFactory.Estimated_Completion_Date_Col)) {
            return wva.getWorldViewEstimatedCompletionDateStr();
         }
         if (xCol.equals(WorldXViewerFactory.Release_Date_Col)) {
            return wva.getWorldViewReleaseDateStr();
         }
         if (xCol.equals(WorldXViewerFactory.Deadline_Col)) {
            return wva.getWorldViewDeadlineDateStr();
         }
         if (xCol.equals(WorldXViewerFactory.Work_Package_Col)) {
            return wva.getWorldViewWorkPackage();
         }
         if (xCol.equals(WorldXViewerFactory.Points_Col)) {
            return wva.getWorldViewPoint();
         }
         if (xCol.equals(WorldXViewerFactory.Numeric1_Col)) {
            return wva.getWorldViewNumeric1();
         }
         if (xCol.equals(WorldXViewerFactory.Numeric2_Col)) {
            return wva.getWorldViewNumeric2();
         }
         if (xCol.equals(WorldXViewerFactory.Goal_Order_Vote_Col)) {
            return wva.getWorldViewGoalOrderVote();
         }
         if (xCol.equals(WorldXViewerFactory.Goal_Order)) {
            return wva.getWorldViewGoalOrder();
         }
         if (xCol.equals(WorldXViewerFactory.Category_Col)) {
            return wva.getWorldViewCategory();
         }
         if (xCol.equals(WorldXViewerFactory.Category2_Col)) {
            return wva.getWorldViewCategory2();
         }
         if (xCol.equals(WorldXViewerFactory.Category3_Col)) {
            return wva.getWorldViewCategory3();
         }
         if (xCol.equals(WorldXViewerFactory.Number_of_Tasks_Col)) {
            return wva.getWorldViewNumberOfTasks();
         }
         if (xCol.equals(WorldXViewerFactory.Number_of_Tasks_Remining_Col)) {
            return wva.getWorldViewNumberOfTasksRemaining();
         }
         if (xCol.equals(WorldXViewerFactory.Review_Issues)) {
            return wva.getWorldViewNumberOfReviewIssueDefects();
         }
         if (xCol.equals(WorldXViewerFactory.Review_Major_Defects)) {
            return wva.getWorldViewNumberOfReviewMajorDefects();
         }
         if (xCol.equals(WorldXViewerFactory.Review_Minor_Defects)) {
            return wva.getWorldViewNumberOfReviewMinorDefects();
         }
         if (xCol.equals(WorldXViewerFactory.Last_Modified_Col)) {
            return wva.getWorldViewLastUpdated();
         }
         if (xCol.equals(WorldXViewerFactory.Last_Statused_Col)) {
            return wva.getWorldViewLastStatused();
         }
         if (xCol.equals(WorldXViewerFactory.Description_Col)) {
            return wva.getWorldViewDescription();
         }
         if (xCol.equals(WorldXViewerFactory.Validation_Required_Col)) {
            return wva.getWorldViewValidationRequiredStr();
         }
         if (xCol.equals(WorldXViewerFactory.Implementor_Col)) {
            return wva.getWorldViewImplementer();
         }
         if (xCol.equals(WorldXViewerFactory.Review_Author_Col)) {
            return wva.getWorldViewReviewAuthor();
         }
         if (xCol.equals(WorldXViewerFactory.Review_Moderator_Col)) {
            return wva.getWorldViewReviewModerator();
         }
         if (xCol.equals(WorldXViewerFactory.Review_Reviewer_Col)) {
            return wva.getWorldViewReviewReviewer();
         }
         if (xCol.equals(WorldXViewerFactory.Review_Decider_Col)) {
            return wva.getWorldViewReviewDecider();
         }
         if (xCol.equals(WorldXViewerFactory.Actions_Initiating_Workflow_Col)) {
            return wva.getWorldViewActionsIntiatingWorkflow();
         }
         if (xCol.equals(WorldXViewerFactory.Parent_ID_Col)) {
            return wva.getWorldViewParentID();
         }
         if (xCol.equals(WorldXViewerFactory.Parent_State_Col)) {
            return wva.getWorldViewParentState();
         }
         if (xCol.equals(WorldXViewerFactory.Days_In_Current_State)) {
            return wva.getWorldViewDaysInCurrentState();
         }
         if (xCol.equals(WorldXViewerFactory.Weekly_Benefit_Hrs_Col)) {
            return AtsUtil.doubleToI18nString(wva.getWorldViewWeeklyBenefit(), true);
         }
         if (xCol.equals(WorldXViewerFactory.Annual_Cost_Avoidance_Col)) {
            Result result = wva.isWorldViewAnnualCostAvoidanceValid();
            if (result.isFalse()) {
               return result.getText();
            }
            return AtsUtil.doubleToI18nString(wva.getWorldViewAnnualCostAvoidance(), true);
         }
         if (xCol.equals(WorldXViewerFactory.Work_Days_Needed_Col)) {
            Result result = wva.isWorldViewManDaysNeededValid();
            if (result.isFalse()) {
               return result.getText();
            }
            return AtsUtil.doubleToI18nString(wva.getWorldViewManDaysNeeded());
         }
         if (xCol.equals(WorldXViewerFactory.Artifact_Type_Col)) {
            return ((Artifact) wva).getArtifactTypeName();
         }
         if (xCol.equals(WorldXViewerFactory.Originating_Workflow)) {
            return wva.getWorldViewOriginatingWorkflowStr();
         }
         for (IAtsWorldEditorItem item : AtsWorldEditorItems.getItems()) {
            if (item.isXColumnProvider(xCol)) {
               String text = item.getColumnText(element, xCol, columnIndex);
               if (text != null) {
                  return text;
               }
            }
         }

         return "Unhandled Column";
      } catch (Exception ex) {
         return XViewerCells.getCellExceptionString(ex);
      }
   }

   public void dispose() {
      if (font != null) {
         font.dispose();
      }
      font = null;
   }

   public boolean isLabelProperty(Object element, String property) {
      return false;
   }

   public void addListener(ILabelProviderListener listener) {
   }

   public void removeListener(ILabelProviderListener listener) {
   }

   public WorldXViewer getTreeViewer() {
      return treeViewer;
   }

}
