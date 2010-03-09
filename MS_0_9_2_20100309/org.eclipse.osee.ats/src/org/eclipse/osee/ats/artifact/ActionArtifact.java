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

package org.eclipse.osee.ats.artifact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.osee.ats.actions.wizard.IAtsTeamWorkflow;
import org.eclipse.osee.ats.artifact.ATSLog.LogType;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact.DefaultTeamState;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.AtsPriority.PriorityType;
import org.eclipse.osee.ats.util.widgets.dialog.AICheckTreeDialog;
import org.eclipse.osee.ats.world.IWorldViewArtifact;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.model.ArtifactType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.util.ChangeType;
import org.eclipse.osee.framework.ui.skynet.widgets.XDate;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public class ActionArtifact extends ATSArtifact implements IWorldViewArtifact {

   public static String ARTIFACT_NAME = "Action";
   public static enum CreateTeamOption {
      Duplicate_If_Exists; // If option exists, then duplication of workflow of same team definition is allowed
   };

   public ActionArtifact(ArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, ArtifactType artifactType) throws OseeDataStoreException {
      super(parentFactory, guid, humanReadableId, branch, artifactType);
   }

   public void resetAttributesOffChildren(SkynetTransaction transaction) throws OseeCoreException {
      resetChangeTypeOffChildren();
      resetPriorityOffChildren();
      resetUserCommunityOffChildren();
      resetTitleOffChildren();
      resetValidationOffChildren();
      resetDescriptionOffChildren();
      persist(transaction);
   }

   public boolean hasAtsWorldChildren() throws OseeCoreException {
      return true;
   }

   public Set<ActionableItemArtifact> getActionableItems() throws OseeCoreException {
      Set<ActionableItemArtifact> aias = new HashSet<ActionableItemArtifact>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         aias.addAll(team.getActionableItemsDam().getActionableItems());
      }
      return aias;
   }

   /**
    * Reset Action title only if all children are titled the same
    */
   private void resetTitleOffChildren() throws OseeCoreException {
      String title = "";
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (title.equals("")) {
            title = team.getName();
         } else if (!title.equals(team.getName())) {
            return;
         }
      }
      if (!title.equals(getName())) {
         setName(title);
      }
   }

   // Set validation to true if any require validation
   private void resetValidationOffChildren() throws OseeCoreException {
      boolean validationRequired = false;
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (team.getSoleAttributeValue(ATSAttributes.VALIDATION_REQUIRED_ATTRIBUTE.getStoreName(), false)) {
            validationRequired = true;
         }
      }
      if (validationRequired != getSoleAttributeValue(ATSAttributes.VALIDATION_REQUIRED_ATTRIBUTE.getStoreName(), false)) {
         setSoleAttributeValue(ATSAttributes.VALIDATION_REQUIRED_ATTRIBUTE.getStoreName(), validationRequired);
      }
   }

   /**
    * Reset Action title only if all children are titled the same
    */
   private void resetDescriptionOffChildren() throws OseeCoreException {
      String desc = "";
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (desc.equals("")) {
            desc = team.getSoleAttributeValue(ATSAttributes.DESCRIPTION_ATTRIBUTE.getStoreName(), "");
         } else if (!desc.equals(team.getSoleAttributeValue(ATSAttributes.DESCRIPTION_ATTRIBUTE.getStoreName(), ""))) {
            return;
         }
      }
      if (!desc.equals(getSoleAttributeValue(ATSAttributes.DESCRIPTION_ATTRIBUTE.getStoreName(), ""))) {
         setSoleAttributeValue(ATSAttributes.DESCRIPTION_ATTRIBUTE.getStoreName(), desc);
      }
      if (desc.equals("")) {
         deleteSoleAttribute(ATSAttributes.DESCRIPTION_ATTRIBUTE.getStoreName());
      }
   }

   private void resetChangeTypeOffChildren() throws OseeCoreException {
      ChangeType changeType = null;
      Collection<TeamWorkFlowArtifact> teamArts = getTeamWorkFlowArtifacts();
      if (teamArts.size() == 1) {
         changeType = teamArts.iterator().next().getChangeType();
      } else {
         for (TeamWorkFlowArtifact team : teamArts) {
            if (!team.isCancelled()) {
               if (changeType == null) {
                  changeType = team.getChangeType();
               } else if (changeType != team.getChangeType()) {
                  return;
               }
            }
         }
      }
      if (changeType != null && getChangeType() != changeType) {
         setChangeType(changeType);
      }
      return;
   }

   private void resetPriorityOffChildren() throws OseeCoreException {
      PriorityType priorityType = null;
      Collection<TeamWorkFlowArtifact> teamArts = getTeamWorkFlowArtifacts();
      if (teamArts.size() == 1) {
         priorityType = teamArts.iterator().next().getPriority();
      } else {
         for (TeamWorkFlowArtifact team : teamArts) {
            if (!team.isCancelled()) {
               if (priorityType == null) {
                  priorityType = team.getPriority();
               } else if (priorityType != team.getPriority()) {
                  return;
               }
            }
         }
      }
      if (priorityType != null && getPriority() != priorityType) {
         setPriority(priorityType);
      }
      return;
   }

   private void resetUserCommunityOffChildren() throws OseeCoreException {
      Set<String> userComs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.isCancelled()) {
            userComs.addAll(team.getAttributesToStringList(ATSAttributes.USER_COMMUNITY_ATTRIBUTE.getStoreName()));
         }
      }
      setAttributeValues(ATSAttributes.USER_COMMUNITY_ATTRIBUTE.getStoreName(), userComs);
   }

   public void setChangeType(ChangeType type) throws OseeCoreException {
      setSoleAttributeValue(ATSAttributes.CHANGE_TYPE_ATTRIBUTE.getStoreName(), type.name());
   }

   public ChangeType getChangeType() throws OseeCoreException {
      return ChangeType.getChangeType(getSoleAttributeValue(ATSAttributes.CHANGE_TYPE_ATTRIBUTE.getStoreName(), ""));
   }

   public PriorityType getPriority() throws OseeCoreException {
      return PriorityType.getPriority(getSoleAttributeValue(ATSAttributes.PRIORITY_TYPE_ATTRIBUTE.getStoreName(), ""));
   }

   public void setPriority(PriorityType type) throws OseeCoreException {
      setSoleAttributeValue(ATSAttributes.PRIORITY_TYPE_ATTRIBUTE.getStoreName(), type.getShortName());
   }

   public Collection<TeamWorkFlowArtifact> getTeamWorkFlowArtifacts() throws OseeCoreException {
      return getRelatedArtifactsUnSorted(AtsRelationTypes.ActionToWorkflow_WorkFlow, TeamWorkFlowArtifact.class);
   }

   public String getWorldViewType() throws OseeCoreException {
      return AtsArtifactTypes.Action.getName();
   }

   public String getWorldViewTitle() throws OseeCoreException {
      return getName();
   }

   public ChangeType getWorldViewChangeType() throws OseeCoreException {
      return ChangeType.getChangeType(getSoleAttributeValue(ATSAttributes.CHANGE_TYPE_ATTRIBUTE.getStoreName(), ""));
   }

   public String getWorldViewBranchStatus() throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.getWorldViewBranchStatus().equals("")) {
            sb.append(team.getWorldViewBranchStatus() + ", ");
         }
      }
      return sb.toString().replaceFirst(", $", "");
   }

   public String getWorldViewPoint() throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.getWorldViewPoint().equals("")) {
            sb.append(team.getWorldViewPoint() + ", ");
         }
      }
      return sb.toString().replaceFirst(", $", "");
   }

   public String getWorldViewNumberOfTasks() throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.getWorldViewNumberOfTasks().equals("")) {
            sb.append(team.getWorldViewNumberOfTasks() + ", ");
         }
      }
      return sb.toString().replaceFirst(", $", "");
   }

   public String getWorldViewNumberOfTasksRemaining() throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.getWorldViewNumberOfTasksRemaining().equals("")) {
            sb.append(team.getWorldViewNumberOfTasksRemaining() + ", ");
         }
      }
      return sb.toString().replaceFirst(", $", "");
   }

   public String getWorldViewState() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewState());
      }
      return Collections.toString(";", strs);
   }

   public String getWorldViewDaysInCurrentState() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewDaysInCurrentState());
      }
      return Collections.toString(";", strs);
   }

   @Override
   public String getWorldViewActivePoc() throws OseeCoreException {
      Set<User> pocs = new HashSet<User>();
      Set<User> implementers = new HashSet<User>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (team.isCancelledOrCompleted()) {
            implementers.addAll(team.getImplementers());
         } else {
            pocs.addAll(team.getStateMgr().getAssignees());
         }
      }
      return Artifacts.toString("; ", pocs) + (implementers.size() > 0 ? "(" + Artifacts.toString("; ", implementers) + ")" : "");
   }

   public String getWorldViewCreatedDateStr() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         Date date = team.getWorldViewCreatedDate();
         if (date == null) {
            strs.add("");
         } else {
            strs.add(XDate.getDateStr(team.getWorldViewCreatedDate(), XDate.MMDDYYHHMM));
         }
      }
      return Collections.toString(";", strs);
   }

   public Date getWorldViewCreatedDate() throws OseeCoreException {
      return getTeamWorkFlowArtifacts().iterator().next().getWorldViewCreatedDate();
   }

   public String getWorldViewID() throws OseeCoreException {
      return getHumanReadableId();
   }

   public String getWorldViewPriority() throws OseeCoreException {
      return PriorityType.getPriority(getSoleAttributeValue(ATSAttributes.PRIORITY_TYPE_ATTRIBUTE.getStoreName(), "")).getShortName();
   }

   public Image getAssigneeImage() throws OseeCoreException {
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         Image image = team.getAssigneeImage();
         if (image != null) {
            return image;
         }
      }
      return null;
   }

   public String getWorldViewUserCommunity() throws OseeCoreException {
      return getAttributesToString(ATSAttributes.USER_COMMUNITY_ATTRIBUTE.getStoreName());
   }

   public String getWorldViewActionableItems() throws OseeCoreException {
      Set<ActionableItemArtifact> aias = new HashSet<ActionableItemArtifact>();
      // Roll up if same for all children
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         aias.addAll(team.getActionableItemsDam().getActionableItems());
      }
      return Artifacts.commaArts(aias);
   }

   @Override
   public void atsDelete(Set<Artifact> deleteArts, Map<Artifact, Object> allRelated) throws OseeCoreException {
      super.atsDelete(deleteArts, allRelated);
      // Delete all products
      for (TeamWorkFlowArtifact art : getRelatedArtifacts(AtsRelationTypes.ActionToWorkflow_WorkFlow,
            TeamWorkFlowArtifact.class)) {
         art.atsDelete(deleteArts, allRelated);
      }
   }

   public String getWorldViewTeam() throws OseeCoreException {
      Set<TeamDefinitionArtifact> teams = new HashSet<TeamDefinitionArtifact>();
      // Roll up if same for all children
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         teams.add(team.getTeamDefinition());
      }
      return Artifacts.commaArts(teams);
   }

   public String getWorldViewOriginator() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewOriginator());
      }
      return Collections.toString(";", strs);
   }

   @Override
   public String getWorldViewCompletedDateStr() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewCompletedDateStr());
      }
      return Collections.toString(";", strs);
   }

   @Override
   public Date getWorldViewCompletedDate() throws OseeCoreException {
      return getTeamWorkFlowArtifacts().iterator().next().getWorldViewCompletedDate();
   }

   public String getWorldViewCancelledDateStr() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewCancelledDateStr());
      }
      return Collections.toString(";", strs);
   }

   public Date getWorldViewCancelledDate() throws OseeCoreException {
      return getTeamWorkFlowArtifacts().iterator().next().getWorldViewCancelledDate();
   }

   public String getWorldViewResolution() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewResolution());
      }
      return Collections.toString(";", strs);
   }

   public double getWorldViewRemainHours() throws OseeCoreException {
      double hours = 0;
      // Add up hours for all children
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         hours += team.getWorldViewRemainHours();
      }
      return hours;
   }

   public double getWorldViewManDaysNeeded() throws OseeCoreException {
      double hours = 0;
      // Add up hours for all children
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         hours += team.getWorldViewManDaysNeeded();
      }
      return hours;
   }

   public double getWorldViewEstimatedHours() throws OseeCoreException {
      double hours = 0;
      // Add up hours for all children
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         hours += team.getWorldViewEstimatedHours();
      }
      return hours;
   }

   public int getWorldViewStatePercentComplete() throws OseeCoreException {
      if (getTeamWorkFlowArtifacts().size() == 1) {
         return getTeamWorkFlowArtifacts().iterator().next().getWorldViewStatePercentComplete();
      } else {
         double percent = 0;
         int items = 0;
         for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
            if (!team.isCancelled()) {
               percent += team.getWorldViewStatePercentComplete();
               items++;
            }
         }
         if (items > 0) {
            Double rollPercent = percent / items;
            return rollPercent.intValue();
         }
      }
      return 0;
   }

   public String getWorldViewRelatedToState() throws OseeCoreException {
      return "";
   }

   @Override
   public String getWorldViewNotes() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewNotes());
      }
      return Collections.toString(";", strs);
   }

   @Override
   public String getWorldViewGroups() throws OseeCoreException {
      Set<Artifact> groups = new HashSet<Artifact>();
      groups.addAll(getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Group));
      // Roll up if same for all children
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         groups.addAll(team.getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Group));
      }
      return Artifacts.toString("; ", groups);
   }

   @Override
   public String getWorldViewGoals() throws OseeCoreException {
      Set<Artifact> groups = new HashSet<Artifact>();
      groups.addAll(getRelatedArtifacts(AtsRelationTypes.Goal_Goal));
      // Roll up if same for all children
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         groups.addAll(team.getRelatedArtifacts(AtsRelationTypes.Goal_Goal));
      }
      return Artifacts.toString("; ", groups);
   }

   public String getWorldViewNumeric1() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewNumeric1());
      }
      return Collections.toString(";", strs);
   }

   public String getWorldViewNumeric2() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewNumeric2());
      }
      return Collections.toString(";", strs);
   }

   public String getWorldViewCategory() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewCategory());
      }
      return Collections.toString(";", strs);
   }

   public String getWorldViewGoalOrderVote() throws OseeCoreException {
      return "";
   }

   public String getWorldViewWorkPackage() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewWorkPackage());
      }
      return Collections.toString(";", strs);
   }

   public String getWorldViewCategory2() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewCategory2());
      }
      return Collections.toString(";", strs);
   }

   public String getWorldViewCategory3() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewCategory3());
      }
      return Collections.toString(";", strs);
   }

   public String getWorldViewTargetedVersionStr() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewTargetedVersionStr());
      }
      return Collections.toString(";", strs);
   }

   public String getHyperName() {
      return getName();
   }

   public String getHyperType() {
      try {
         return getArtifactTypeName();
      } catch (Exception ex) {
         return ex.getLocalizedMessage();
      }
   }

   public String getHyperState() {
      try {
         if (getTeamWorkFlowArtifacts().size() == 1) {
            return getTeamWorkFlowArtifacts().iterator().next().getHyperState();
         }
      } catch (Exception ex) {
         return XViewerCells.getCellExceptionString(ex);
      }
      return "";
   }

   @Override
   public String getHyperTargetVersion() {
      return null;
   }

   public String getHyperAssignee() {
      try {
         if (getTeamWorkFlowArtifacts().size() == 1) {
            return getTeamWorkFlowArtifacts().iterator().next().getHyperAssignee();
         }
      } catch (Exception ex) {
         return XViewerCells.getCellExceptionString(ex);
      }
      return "";
   }

   public Image getHyperAssigneeImage() throws OseeCoreException {
      if (getTeamWorkFlowArtifacts().size() == 1) {
         return getTeamWorkFlowArtifacts().iterator().next().getHyperAssigneeImage();
      }
      return null;
   }

   public Artifact getHyperArtifact() {
      return this;
   }

   public Result editActionableItems() throws OseeCoreException {
      final AICheckTreeDialog diag =
            new AICheckTreeDialog(
                  "Add Impacted Actionable Items",
                  "Select New Impacted Actionable Items\n\n" + "Note: Un-selecting existing items will NOT remove the impact.\n" + "Team Workflow with no impact should be transitioned to Cancelled.",
                  Active.Active);

      diag.setInitialAias(getActionableItems());
      if (diag.open() != 0) {
         return Result.FalseResult;
      }

      // ensure that at least one actionable item exists for each team after aias added/removed
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         Set<ActionableItemArtifact> currentAias = team.getActionableItemsDam().getActionableItems();
         Collection<ActionableItemArtifact> checkedAias = diag.getChecked();
         for (ActionableItemArtifact aia : new CopyOnWriteArrayList<ActionableItemArtifact>(currentAias)) {
            if (!checkedAias.contains(aia)) {
               currentAias.remove(aia);
            }
         }
         if (currentAias.size() == 0) {
            return new Result("Can not remove all actionable items for a team.\n\nActionable Items will go to 0 for [" +
            //
            team.getTeamName() + "][" + team.getHumanReadableId() + "]\n\nCancel team workflow instead.");
         }
      }

      final StringBuffer sb = new StringBuffer();
      SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "Edit Actionable Items");

      // Add new aias
      for (ActionableItemArtifact aia : diag.getChecked()) {
         Result result = addActionableItemToTeamsOrAddTeams(aia, UserManager.getUser(), transaction);
         sb.append(result.getText());
      }
      // Remove unchecked aias
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         for (ActionableItemArtifact aia : team.getActionableItemsDam().getActionableItems()) {
            if (!diag.getChecked().contains(aia)) {
               team.getActionableItemsDam().removeActionableItem(aia);
            }
         }
         team.persist(transaction);
      }

      transaction.execute();
      return new Result(true, sb.toString());
   }

   public Result addActionableItemToTeamsOrAddTeams(ActionableItemArtifact aia, User originator, SkynetTransaction transaction) throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      for (TeamDefinitionArtifact tda : TeamDefinitionArtifact.getImpactedTeamDefs(Arrays.asList(aia))) {
         boolean teamExists = false;
         // Look for team workflow that is associated with this tda
         for (TeamWorkFlowArtifact teamArt : getTeamWorkFlowArtifacts()) {
            // If found
            if (teamArt.getTeamDefinition().equals(tda)) {
               // And workflow doesn't already have this actionable item,
               // ADD it
               if (!teamArt.getActionableItemsDam().getActionableItems().contains(aia)) {
                  teamArt.getActionableItemsDam().addActionableItem(aia);
                  teamArt.saveSMA(transaction);
                  sb.append(aia.getName() + " => added to existing team workflow \"" + tda.getName() + "\"\n");
                  teamExists = true;
               } else {
                  sb.append(aia.getName() + " => already exists in team workflow \"" + tda.getName() + "\"\n");
                  teamExists = true;
               }
            }
         }
         if (!teamExists) {
            TeamWorkFlowArtifact teamArt = createTeamWorkflow(tda, Arrays.asList(aia), tda.getLeads(), transaction);
            if (originator != null) {
               teamArt.getLog().setOriginator(originator);
            }
            teamArt.persist(transaction);
            sb.append(aia.getName() + " => added team workflow \"" + tda.getName() + "\"\n");
         }
      }
      return new Result(true, sb.toString());
   }

   public TeamWorkFlowArtifact createTeamWorkflow(TeamDefinitionArtifact teamDef, Collection<ActionableItemArtifact> actionableItems, Collection<User> assignees, SkynetTransaction transaction, CreateTeamOption... createTeamOption) throws OseeCoreException {
      String teamWorkflowArtifactName = AtsArtifactTypes.TeamWorkflow.getName();
      IAtsTeamWorkflow teamExt = null;

      // Check if any plugins want to create the team workflow themselves
      for (IAtsTeamWorkflow teamExtension : TeamWorkflowExtensions.getInstance().getAtsTeamWorkflowExtensions()) {
         boolean isResponsible = false;
         try {
            isResponsible = teamExtension.isResponsibleForTeamWorkflowCreation(teamDef, actionableItems);
         } catch (Exception ex) {
            OseeLog.log(AtsPlugin.class, Level.WARNING, ex);
         }
         if (isResponsible) {
            teamWorkflowArtifactName = teamExtension.getTeamWorkflowArtifactName(teamDef, actionableItems);
            teamExt = teamExtension;
         }
      }

      // NOTE: The persist of the workflow will auto-email the assignees
      TeamWorkFlowArtifact teamArt =
            createTeamWorkflow(teamDef, actionableItems, assignees,
                  ArtifactTypeManager.getType(teamWorkflowArtifactName), transaction, createTeamOption);
      // Notify extension that workflow was created
      if (teamExt != null) {
         teamExt.teamWorkflowCreated(teamArt);
      }
      return teamArt;
   }

   public TeamWorkFlowArtifact createTeamWorkflow(TeamDefinitionArtifact teamDef, Collection<ActionableItemArtifact> actionableItems, Collection<User> assignees, IArtifactType artifactType, SkynetTransaction transaction, CreateTeamOption... createTeamOption) throws OseeCoreException {
      return createTeamWorkflow(teamDef, actionableItems, assignees, null, null, artifactType, transaction,
            createTeamOption);
   }

   public TeamWorkFlowArtifact createTeamWorkflow(TeamDefinitionArtifact teamDef, Collection<ActionableItemArtifact> actionableItems, Collection<User> assignees, String guid, String hrid, IArtifactType artifactType, SkynetTransaction transaction, CreateTeamOption... createTeamOption) throws OseeCoreException {

      if (!Collections.getAggregate(createTeamOption).contains(CreateTeamOption.Duplicate_If_Exists)) {
         // Make sure team doesn't already exist
         for (TeamWorkFlowArtifact teamArt : getTeamWorkFlowArtifacts()) {
            if (teamArt.getTeamDefinition().equals(teamDef)) {
               AWorkbench.popup("ERROR", "Team already exist");
               throw new OseeArgumentException(
                     "Team \"" + teamDef + "\" already exists for Action " + getHumanReadableId());
            }
         }
      }

      TeamWorkFlowArtifact teamArt = null;
      if (guid == null) {
         teamArt = (TeamWorkFlowArtifact) ArtifactTypeManager.addArtifact(artifactType, AtsUtil.getAtsBranch());
      } else {
         teamArt =
               (TeamWorkFlowArtifact) ArtifactTypeManager.addArtifact(artifactType, AtsUtil.getAtsBranch(), guid, hrid);
      }
      setArtifactIdentifyData(this, teamArt);

      teamArt.getLog().addLog(LogType.Originated, "", "");

      // Relate Workflow to ActionableItems (by guid) if team is responsible
      // for that AI
      for (ActionableItemArtifact aia : actionableItems) {
         if (aia.getImpactedTeamDefs().contains(teamDef)) {
            teamArt.getActionableItemsDam().addActionableItem(aia);
         }
      }

      // Initialize state machine
      teamArt.getStateMgr().initializeStateMachine(DefaultTeamState.Endorse.name(), assignees);
      teamArt.getLog().addLog(LogType.StateEntered, DefaultTeamState.Endorse.name(), "");

      // Relate WorkFlow to Team Definition (by guid due to relation loading
      // issues)
      teamArt.setTeamDefinition(teamDef);

      // Relate Action to WorkFlow
      addRelation(AtsRelationTypes.ActionToWorkflow_WorkFlow, teamArt);

      teamArt.persist(transaction);

      return teamArt;
   }

   /**
    * Set Team Workflow attributes off given action artifact
    * 
    * @param fromAction
    * @param toTeam
    * @throws OseeArgumentException
    */
   public static void setArtifactIdentifyData(ActionArtifact fromAction, TeamWorkFlowArtifact toTeam) throws OseeCoreException {
      String priorityStr = fromAction.getSoleAttributeValue(ATSAttributes.PRIORITY_TYPE_ATTRIBUTE.getStoreName(), "");
      PriorityType priType = null;
      if (priorityStr.equals("")) {
         priType = null;
      } else if (!priorityStr.equals("")) {
         priType = PriorityType.getPriority(priorityStr);
      } else {
         throw new OseeArgumentException("Invalid priority => " + priorityStr);
      }
      setArtifactIdentifyData(toTeam, fromAction.getName(), fromAction.getSoleAttributeValue(
            ATSAttributes.DESCRIPTION_ATTRIBUTE.getStoreName(), ""),
            ChangeType.getChangeType(fromAction.getSoleAttributeValue(
                  ATSAttributes.CHANGE_TYPE_ATTRIBUTE.getStoreName(), "")), priType,
            //            fromAction.getAttributesToStringList(ATSAttributes.USER_COMMUNITY_ATTRIBUTE.getStoreName()),
            fromAction.getSoleAttributeValue(ATSAttributes.VALIDATION_REQUIRED_ATTRIBUTE.getStoreName(), false),
            fromAction.getSoleAttributeValue(ATSAttributes.NEED_BY_ATTRIBUTE.getStoreName(), null, Date.class));
   }

   /**
    * Since there is no shared attribute yet, action and workflow arts are all populate with identify data
    * 
    * @param art
    */
   public static void setArtifactIdentifyData(Artifact art, String title, String desc, ChangeType changeType, PriorityType priority, Boolean validationRequired, Date needByDate) throws OseeCoreException {
      art.setName(title);
      if (!desc.equals("")) {
         art.setSoleAttributeValue(ATSAttributes.DESCRIPTION_ATTRIBUTE.getStoreName(), desc);
      }
      art.setSoleAttributeValue(ATSAttributes.CHANGE_TYPE_ATTRIBUTE.getStoreName(), changeType.name());
      //      art.setAttributeValues(ATSAttributes.USER_COMMUNITY_ATTRIBUTE.getStoreName(), userComms);
      if (priority != null) {
         art.setSoleAttributeValue(ATSAttributes.PRIORITY_TYPE_ATTRIBUTE.getStoreName(), priority.getShortName());
      }
      if (needByDate != null) {
         art.setSoleAttributeValue(ATSAttributes.NEED_BY_ATTRIBUTE.getStoreName(), needByDate);
      }
      if (validationRequired) {
         art.setSoleAttributeValue(ATSAttributes.VALIDATION_REQUIRED_ATTRIBUTE.getStoreName(), true);
      }
   }

   public String getWorldViewDecision() throws OseeCoreException {
      return "";
   }

   public Artifact getParentAtsArtifact() throws OseeCoreException {
      return null;
   }

   public String getWorldViewDescription() throws OseeCoreException {
      try {
         return getSoleAttributeValue(ATSAttributes.DESCRIPTION_ATTRIBUTE.getStoreName(), "");
      } catch (Exception ex) {
         return XViewerCells.getCellExceptionString(ex);
      }
   }

   public String getWorldViewValidationRequiredStr() throws OseeCoreException {
      try {
         return String.valueOf(getSoleAttributeValue(ATSAttributes.VALIDATION_REQUIRED_ATTRIBUTE.getStoreName(), false));
      } catch (Exception ex) {
         return XViewerCells.getCellExceptionString(ex);
      }
   }

   @Override
   public Date getWorldViewEstimatedReleaseDate() throws OseeCoreException {
      return getTeamWorkFlowArtifacts().iterator().next().getWorldViewEstimatedReleaseDate();
   }

   public String getWorldViewEstimatedCompletionDateStr() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewEstimatedCompletionDateStr());
      }
      return Collections.toString(";", strs);
   }

   @Override
   public Date getWorldViewEstimatedCompletionDate() throws OseeCoreException {
      return getTeamWorkFlowArtifacts().iterator().next().getWorldViewEstimatedCompletionDate();
   }

   @Override
   public String getWorldViewReleaseDateStr() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewReleaseDateStr());
      }
      return Collections.toString(";", strs);
   }

   public Date getWorldViewReleaseDate() throws OseeCoreException {
      return getTeamWorkFlowArtifacts().iterator().next().getWorldViewReleaseDate();
   }

   public String getWorldViewEstimatedReleaseDateStr() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewEstimatedReleaseDateStr());
      }
      return Collections.toString(";", strs);
   }

   public Result isWorldViewRemainHoursValid() throws OseeCoreException {
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (team.isWorldViewRemainHoursValid().isFalse()) {
            return team.isWorldViewRemainHoursValid();
         }
      }
      return Result.TrueResult;
   }

   public Result isWorldViewManDaysNeededValid() {
      try {
         for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
            if (team.isWorldViewManDaysNeededValid().isFalse()) {
               return team.isWorldViewManDaysNeededValid();
            }
         }
      } catch (OseeCoreException ex) {
         // Do nothing
      }
      return Result.TrueResult;
   }

   public String getWorldViewChangeTypeStr() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewChangeTypeStr());
      }
      return Collections.toString(";", strs);
   }

   public String getWorldViewImplementer() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewImplementer());
      }
      return Collections.toString(";", strs);
   }

   public Date getWorldViewDeadlineDate() throws OseeCoreException {
      return getTeamWorkFlowArtifacts().iterator().next().getWorldViewDeadlineDate();
   }

   public String getWorldViewDeadlineDateStr() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewDeadlineDateStr());
      }
      return Collections.toString(";", strs);
   }

   public double getWorldViewWeeklyBenefit() throws OseeCoreException {
      double hours = 0;
      // Add up hours for all children
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.isCompleted() && !team.isCancelled()) {
            hours += team.getWorldViewWeeklyBenefit();
         }
      }
      return hours;
   }

   public double getWorldViewAnnualCostAvoidance() throws OseeCoreException {
      double hours = 0;
      // Add up hours for all children
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.isCompleted() && !team.isCancelled()) {
            hours += team.getWorldViewAnnualCostAvoidance();
         }
      }
      return hours;
   }

   public Result isWorldViewAnnualCostAvoidanceValid() throws OseeCoreException {
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         Result result = team.isWorldViewAnnualCostAvoidanceValid();
         if (result.isFalse()) {
            return result;
         }
      }
      return Result.TrueResult;
   }

   public Result isWorldViewDeadlineAlerting() throws OseeCoreException {
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         Result result = team.isWorldViewDeadlineAlerting();
         if (result.isTrue()) {
            return result;
         }
      }
      return Result.FalseResult;
   }

   public String getWorldViewLegacyPCR() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewLegacyPCR());
      }
      return Collections.toString(";", strs);
   }

   public String getWorldViewPercentReworkStr() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         strs.add(team.getWorldViewPercentReworkStr());
      }
      return Collections.toString(";", strs);
   }

   public int getWorldViewPercentRework() throws OseeCoreException {
      return 0;
   }

   public String getWorldViewReviewAuthor() throws OseeCoreException {
      return "";
   }

   public String getWorldViewReviewDecider() throws OseeCoreException {
      return "";
   }

   public String getWorldViewReviewModerator() throws OseeCoreException {
      return "";
   }

   public String getWorldViewReviewReviewer() throws OseeCoreException {
      return "";
   }

   @Override
   public double getWorldViewHoursSpentState() throws OseeCoreException {
      double hours = 0;
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.isCancelled()) {
            hours += team.getWorldViewHoursSpentState();
         }
      }
      return hours;
   }

   @Override
   public double getWorldViewHoursSpentStateReview() throws OseeCoreException {
      double hours = 0;
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.isCancelled()) {
            hours += team.getWorldViewHoursSpentStateReview();
         }
      }
      return hours;
   }

   @Override
   public double getWorldViewHoursSpentStateTask() throws OseeCoreException {
      double hours = 0;
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.isCancelled()) {
            hours += team.getWorldViewHoursSpentStateTask();
         }
      }
      return hours;
   }

   @Override
   public double getWorldViewHoursSpentStateTotal() throws OseeCoreException {
      double hours = 0;
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.isCancelled()) {
            hours += team.getWorldViewHoursSpentStateTotal();
         }
      }
      return hours;
   }

   @Override
   public double getWorldViewHoursSpentTotal() throws OseeCoreException {
      double hours = 0;
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.isCancelled()) {
            hours += team.getWorldViewHoursSpentTotal();
         }
      }
      return hours;
   }

   @Override
   public int getWorldViewPercentCompleteState() throws OseeCoreException {
      double percent = 0;
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.isCancelled()) {
            percent += team.getWorldViewPercentCompleteState();
         }
      }
      if (percent == 0) {
         return 0;
      }
      Double rollPercent = percent / getTeamWorkFlowArtifacts().size();
      return rollPercent.intValue();
   }

   @Override
   public int getWorldViewPercentCompleteStateReview() throws OseeCoreException {
      double percent = 0;
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.isCancelled()) {
            percent += team.getWorldViewPercentCompleteStateReview();
         }
      }
      if (percent == 0) {
         return 0;
      }
      Double rollPercent = percent / getTeamWorkFlowArtifacts().size();
      return rollPercent.intValue();
   }

   @Override
   public int getWorldViewPercentCompleteStateTask() throws OseeCoreException {
      double percent = 0;
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.isCancelled()) {
            percent += team.getWorldViewPercentCompleteStateTask();
         }
      }
      if (percent == 0) {
         return 0;
      }
      Double rollPercent = percent / getTeamWorkFlowArtifacts().size();
      return rollPercent.intValue();
   }

   @Override
   public int getWorldViewPercentCompleteTotal() throws OseeCoreException {
      double percent = 0;
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (!team.isCancelled()) {
            percent += team.getWorldViewPercentCompleteTotal();
         }
      }
      if (percent == 0) {
         return 0;
      }
      Double rollPercent = percent / getTeamWorkFlowArtifacts().size();
      return rollPercent.intValue();
   }

   public String getWorldViewLastUpdated() throws OseeCoreException {
      return XDate.getDateStr(getLastModified(), XDate.MMDDYYHHMM);
   }

   public String getWorldViewLastStatused() throws OseeCoreException {
      return "(see children)";
   }

   @Override
   public VersionArtifact getWorldViewTargetedVersion() throws OseeCoreException {
      return null;
   }

   public String getWorldViewNumberOfReviewIssueDefects() throws OseeCoreException {
      return "";
   }

   public String getWorldViewNumberOfReviewMajorDefects() throws OseeCoreException {
      return "";
   }

   public String getWorldViewNumberOfReviewMinorDefects() throws OseeCoreException {
      return "";
   }

   @Override
   public String getWorldViewActionsIntiatingWorkflow() throws OseeCoreException {
      Date earliestDate = null;
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (earliestDate == null || team.getLog().getCreationDate().before(earliestDate)) {
            earliestDate = team.getLog().getCreationDate();
         }
      }
      List<String> teamNames = new ArrayList<String>();
      for (TeamWorkFlowArtifact team : getTeamWorkFlowArtifacts()) {
         if (team.getLog().getCreationDate().equals(earliestDate)) {
            teamNames.add(team.getTeamName());
         }
      }
      return Collections.toString("; ", teamNames);
   }

   @Override
   public String getWorldViewParentID() throws OseeCoreException {
      return "";
   }

   @Override
   public String getWorldViewParentState() throws OseeCoreException {
      return null;
   }

   @Override
   public String getWorldViewOriginatingWorkflowStr() throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getWorldViewOriginatingWorkflows()) {
         strs.add(team.getWorldViewTeam());
      }
      return Collections.toString(";", strs);
   }

   @Override
   public Collection<TeamWorkFlowArtifact> getWorldViewOriginatingWorkflows() throws OseeCoreException {
      if (getTeamWorkFlowArtifacts().size() == 1) {
         return getTeamWorkFlowArtifacts();
      }
      Collection<TeamWorkFlowArtifact> results = new ArrayList<TeamWorkFlowArtifact>();
      Date origDate = null;
      for (TeamWorkFlowArtifact teamArt : getTeamWorkFlowArtifacts()) {
         if (teamArt.isCancelled()) {
            continue;
         }
         if (origDate == null || teamArt.getWorldViewCreatedDate().before(origDate)) {
            results.clear();
            origDate = teamArt.getWorldViewCreatedDate();
            results.add(teamArt);
         } else if (origDate.equals(teamArt.getWorldViewCreatedDate())) {
            results.add(teamArt);
         }
      }
      return results;
   }

}
