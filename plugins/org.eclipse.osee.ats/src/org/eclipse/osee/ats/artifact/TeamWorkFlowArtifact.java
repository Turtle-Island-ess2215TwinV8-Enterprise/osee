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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.osee.ats.config.AtsCacheManager;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsBranchManager;
import org.eclipse.osee.ats.util.AtsPriority.PriorityType;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.ReviewManager;
import org.eclipse.osee.ats.util.widgets.XActionableItemsDam;
import org.eclipse.osee.ats.util.widgets.dialog.AICheckTreeDialog;
import org.eclipse.osee.ats.workflow.item.AtsWorkDefinitions.RuleWorkItemId;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;
import org.eclipse.osee.framework.skynet.core.artifact.IATSStateMachineArtifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.util.ChangeType;
import org.eclipse.osee.framework.ui.skynet.widgets.IBranchArtifact;
import org.eclipse.osee.framework.ui.skynet.widgets.XDate;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkPageDefinition;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Donald G. Dunne
 */
public class TeamWorkFlowArtifact extends TaskableStateMachineArtifact implements IBranchArtifact, IATSStateMachineArtifact {

   private XActionableItemsDam actionableItemsDam;
   private boolean targetedErrorLogged = false;
   private final AtsBranchManager branchMgr;
   public static enum DefaultTeamState {
      Endorse,
      Analyze,
      Authorize,
      Implement,
      Completed,
      Cancelled
   }

   public TeamWorkFlowArtifact(ArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, ArtifactType artifactType) throws OseeDataStoreException {
      super(parentFactory, guid, humanReadableId, branch, artifactType);
      registerAtsWorldRelation(AtsRelationTypes.TeamWorkflowToReview_Review);
      branchMgr = new AtsBranchManager(this);
   }

   @Override
   public void getSmaArtifactsOneLevel(StateMachineArtifact smaArtifact, Set<Artifact> artifacts) throws OseeCoreException {
      super.getSmaArtifactsOneLevel(smaArtifact, artifacts);
      try {
         if (getTargetedForVersion() != null) {
            artifacts.add(getTargetedForVersion());
         }
         artifacts.addAll(ReviewManager.getReviews(this));
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
   }

   @Override
   public String getArtifactSuperTypeName() {
      return "Team Workflow";
   }

   @Override
   public void saveSMA(SkynetTransaction transaction) {
      super.saveSMA(transaction);
      try {
         getParentActionArtifact().resetAttributesOffChildren(transaction);
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, "Can't reset Action parent of children", ex);
      }
   }

   @Override
   public String getDescription() {
      try {
         return getSoleAttributeValue(AtsAttributeTypes.Description, "");
      } catch (Exception ex) {
         return "Error: " + ex.getLocalizedMessage();
      }
   }

   @Override
   public boolean isValidationRequired() throws OseeCoreException {
      return getSoleAttributeValue(AtsAttributeTypes.ValidationRequired, false);
   }

   @Override
   public int getWorldViewPercentRework() throws OseeCoreException {
      return getSoleAttributeValue(AtsAttributeTypes.PercentRework, 0);
   }

   @Override
   public Set<User> getPrivilegedUsers() {
      Set<User> users = new HashSet<User>();
      try {
         addPriviledgedUsersUpTeamDefinitionTree(getTeamDefinition(), users);

         WorkPageDefinition workPageDefinition = getWorkPageDefinition();

         // Add user if allowing privileged edit to all users
         if (!users.contains(UserManager.getUser()) && (workPageDefinition.hasWorkRule(RuleWorkItemId.atsAllowPriviledgedEditToAll.name()) || getTeamDefinition().hasWorkRule(
            RuleWorkItemId.atsAllowPriviledgedEditToAll.name()))) {
            users.add(UserManager.getUser());
         }

         // Add user if user is team member and rule exists
         if (!users.contains(UserManager.getUser()) && (workPageDefinition.hasWorkRule(RuleWorkItemId.atsAllowPriviledgedEditToTeamMember.name()) || getTeamDefinition().hasWorkRule(
            RuleWorkItemId.atsAllowPriviledgedEditToTeamMember.name()))) {
            if (getTeamDefinition().getMembers().contains(UserManager.getUser())) {
               users.add(UserManager.getUser());
            }
         }

         // Add user if team member is originator and rule exists
         if (!users.contains(UserManager.getUser()) && (workPageDefinition.hasWorkRule(RuleWorkItemId.atsAllowPriviledgedEditToTeamMemberAndOriginator.name()) || getTeamDefinition().hasWorkRule(
            RuleWorkItemId.atsAllowPriviledgedEditToTeamMemberAndOriginator.name()))) {
            if (getOriginator().equals(UserManager.getUser()) && getTeamDefinition().getMembers().contains(
               UserManager.getUser())) {
               users.add(UserManager.getUser());
            }
         }

      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return users;
   }

   @Override
   public String getEditorTitle() throws OseeCoreException {
      try {
         if (getWorldViewTargetedVersion() != null) {
            return getWorldViewType() + ": " + "[" + getWorldViewTargetedVersionStr() + "] - " + getName();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
      return super.getEditorTitle();
   }

   @Override
   public void onInitializationComplete() throws OseeCoreException {
      super.onInitializationComplete();
      initializeSMA();
   }

   @Override
   protected void initializeSMA() throws OseeCoreException {
      super.initializeSMA();
      actionableItemsDam = new XActionableItemsDam(this);
   }

   public ChangeType getChangeType() throws OseeCoreException {
      return ChangeType.getChangeType(getSoleAttributeValue(AtsAttributeTypes.ChangeType, ""));
   }

   public void setChangeType(ChangeType type) throws OseeCoreException {
      setSoleAttributeValue(AtsAttributeTypes.ChangeType, type.name());
   }

   public PriorityType getPriority() throws OseeCoreException {
      return PriorityType.getPriority(getSoleAttributeValue(AtsAttributeTypes.PriorityType, ""));
   }

   public void setPriority(PriorityType type) throws OseeCoreException {
      setSoleAttributeValue(AtsAttributeTypes.PriorityType, type.getShortName());
   }

   /**
    * @return Returns the actionableItemsDam.
    */
   public XActionableItemsDam getActionableItemsDam() {
      return actionableItemsDam;
   }

   public void setTeamDefinition(TeamDefinitionArtifact tda) throws OseeCoreException {
      this.setSoleAttributeValue(AtsAttributeTypes.TeamDefinition, tda.getGuid());
   }

   public TeamDefinitionArtifact getTeamDefinition() throws OseeCoreException, OseeCoreException {
      String guid = this.getSoleAttributeValue(AtsAttributeTypes.TeamDefinition, "");
      if (!Strings.isValid(guid)) {
         throw new OseeArgumentException(
            "TeamWorkflow [" + getHumanReadableId() + "] has no TeamDefinition associated.");
      }
      return AtsCacheManager.getTeamDefinitionArtifact(guid);
   }

   public String getTeamName() {
      try {
         return getTeamDefinition().getName();
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         return XViewerCells.getCellExceptionString(ex);
      }
   }

   @Override
   public String getWorldViewType() {
      return getTeamName() + " Workflow";
   }

   @Override
   public ChangeType getWorldViewChangeType() throws OseeCoreException {
      return ChangeType.getChangeType(getSoleAttributeValue(AtsAttributeTypes.ChangeType, ""));
   }

   @Override
   public String getWorldViewPriority() throws OseeCoreException {
      return PriorityType.getPriority(getSoleAttributeValue(AtsAttributeTypes.PriorityType, "")).getShortName();
   }

   @Override
   public String getWorldViewUserCommunity() throws OseeCoreException {
      return getAttributesToString(AtsAttributeTypes.UserCommunity);
   }

   @Override
   public String getWorldViewActionableItems() throws OseeCoreException {
      return getActionableItemsDam().getActionableItemsStr();
   }

   @Override
   public void atsDelete(Set<Artifact> deleteArts, Map<Artifact, Object> allRelated) throws OseeCoreException {
      super.atsDelete(deleteArts, allRelated);
      for (ReviewSMArtifact reviewArt : ReviewManager.getReviews(this)) {
         reviewArt.atsDelete(deleteArts, allRelated);
      }
   }

   @Override
   public String getWorldViewTeam() {
      return getTeamName();
   }

   @Override
   public TeamWorkFlowArtifact getParentTeamWorkflow() {
      parentTeamArt = this;
      return parentTeamArt;
   }

   @Override
   public Artifact getParentAtsArtifact() throws OseeCoreException {
      return getParentActionArtifact();
   }

   @Override
   public ActionArtifact getParentActionArtifact() throws OseeCoreException {
      if (parentAction != null) {
         return parentAction;
      }
      Collection<ActionArtifact> arts =
         getRelatedArtifacts(AtsRelationTypes.ActionToWorkflow_Action, ActionArtifact.class);
      if (arts.isEmpty()) {
         throw new OseeStateException("Team " + getHumanReadableId() + " has no parent Action");
      } else if (arts.size() > 1) {
         throw new OseeStateException("Team " + getHumanReadableId() + " has multiple parent Actions");
      }
      parentAction = arts.iterator().next();
      return parentAction;
   }

   @Override
   public StateMachineArtifact getParentSMA() {
      return null;
   }

   @Override
   public String getWorldViewTargetedVersionStr() throws OseeCoreException {
      Collection<VersionArtifact> verArts =
         getRelatedArtifacts(AtsRelationTypes.TeamWorkflowTargetedForVersion_Version, VersionArtifact.class);
      if (verArts.isEmpty()) {
         return "";
      }
      if (verArts.size() > 1) {
         String errStr =
            "Workflow " + getHumanReadableId() + " targeted for multiple versions: " + Artifacts.commaArts(verArts);
         OseeLog.log(AtsPlugin.class, Level.SEVERE, errStr, null);
         return XViewerCells.getCellExceptionString(errStr);
      }
      VersionArtifact verArt = verArts.iterator().next();
      if (!isCompleted() && !isCancelled() && verArt.getSoleAttributeValue(AtsAttributeTypes.Released, false)) {
         String errStr =
            "Workflow " + getHumanReadableId() + " targeted for released version, but not completed: " + verArt;
         if (!targetedErrorLogged) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, errStr, null);
            targetedErrorLogged = true;
         }
         return XViewerCells.getCellExceptionString(errStr);
      }
      return verArt.getName();
   }

   @Override
   public VersionArtifact getWorldViewTargetedVersion() throws OseeCoreException {
      if (getRelatedArtifactsCount(AtsRelationTypes.TeamWorkflowTargetedForVersion_Version) > 0) {
         return (VersionArtifact) getRelatedArtifact(AtsRelationTypes.TeamWorkflowTargetedForVersion_Version);
      }
      return null;
   }

   @Override
   public String getHyperName() {
      try {
         return getEditorTitle();
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return getTeamName();
   }

   @Override
   public String getHyperTargetVersion() {
      try {
         return getWorldViewTargetedVersionStr().equals("") ? null : getWorldViewTargetedVersionStr();
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
      return null;
   }

   @Override
   public double getManHrsPerDayPreference() throws OseeCoreException {
      try {
         return getTeamDefinition().getManDayHrsFromItemAndChildren();
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
      return super.getManHrsPerDayPreference();
   }

   public Result editActionableItems() throws OseeCoreException {
      return getParentActionArtifact().editActionableItems();
   }

   public Result convertActionableItems() throws OseeCoreException {
      Result toReturn = Result.FalseResult;
      AICheckTreeDialog diag =
         new AICheckTreeDialog("Convert Impacted Actionable Items",
            "NOTE: This should NOT be the normal path to changing actionable items.\n\nIf a team has " +
            //
            "determined " + "that there is NO impact and that another actionable items IS impacted:\n" +
            //
            "   1) Cancel this operation\n" + "   2) Select \"Edit Actionable Items\" to add/remove " +
            //
            "impacted items \n" + "      which will create new teams as needed.\n" +
            //
            "   3) Then cancel the team that has no impacts.\n   Doing this will show that the original " +
            //
            "team analyzed the impact\n" + "   and determined that there was no change.\n\n" + "However, " +
            //
            "there are some cases where an impacted item was incorrectly chosen\n" + "and the original team " +
            //
            "does not need to do anything, this dialog will purge the\n" + "team from the DB as if it was " +
            //
            "never chosen.\n\n" + "Current Actionable Item(s): " + getWorldViewActionableItems() + "\n" +
            //
            "Current Team: " + getTeamDefinition().getName() + "\n" +
            //
            "Select SINGLE Actionable Item below to convert this workflow to.\n\n" +
            //
            "You will be prompted to confirm this conversion.", Active.Both);

      diag.setInput(ActionableItemArtifact.getTopLevelActionableItems(Active.Both));
      if (diag.open() != 0) {
         return Result.FalseResult;
      }
      if (diag.getChecked().isEmpty()) {
         return new Result("At least one actionable item must must be selected.");
      }
      if (diag.getChecked().size() > 1) {
         return new Result("Only ONE actionable item can be selected for converts");
      }
      ActionableItemArtifact selectedAia = diag.getChecked().iterator().next();
      Collection<TeamDefinitionArtifact> teamDefs =
         ActionableItemArtifact.getImpactedTeamDefs(Arrays.asList(selectedAia));
      if (teamDefs.size() != 1) {
         toReturn = new Result("Single team can not retrieved for " + selectedAia.getName());
      } else {
         TeamDefinitionArtifact newTeamDef = teamDefs.iterator().next();
         if (newTeamDef.equals(getTeamDefinition())) {
            toReturn =
               new Result(
                  "Actionable Item selected belongs to same team as currently selected team.\n" + "Use \"Edit Actionable Items\" instaed.");
         } else {
            StringBuffer sb = new StringBuffer();
            sb.append("Converting...");
            sb.append("\nActionable Item(s): " + getWorldViewActionableItems());
            sb.append("\nTeam: " + getTeamDefinition().getName());
            sb.append("\nto\nActionable Item(s): " + selectedAia);
            sb.append("\nTeam: " + newTeamDef.getName());
            if (MessageDialog.openConfirm(Displays.getActiveShell(), "Confirm Convert", sb.toString())) {
               Set<ActionableItemArtifact> toProcess = new HashSet<ActionableItemArtifact>();
               toProcess.add(selectedAia);
               toReturn = actionableItemsTx(AtsUtil.getAtsBranch(), toProcess, newTeamDef);
            }
         }
      }
      return toReturn;
   }

   @Override
   public String getWorldViewDescription() throws OseeCoreException {
      return getSoleAttributeValue(AtsAttributeTypes.Description, "");
   }

   /**
    * If targeted for version exists, return that estimated date. Else, if attribute is set, return that date. Else
    * null.
    */
   @Override
   public Date getWorldViewEstimatedReleaseDate() throws OseeCoreException {
      Collection<VersionArtifact> vers =
         getRelatedArtifacts(AtsRelationTypes.TeamWorkflowTargetedForVersion_Version, VersionArtifact.class);
      Date date = null;
      if (vers.size() > 0) {
         date = vers.iterator().next().getEstimatedReleaseDate();
         if (date == null) {
            date = getSoleAttributeValue(AtsAttributeTypes.EstimatedReleaseDate, null);
         }
      } else {
         date = getSoleAttributeValue(AtsAttributeTypes.EstimatedReleaseDate, null);
      }
      return date;
   }

   /**
    * If targeted for version exists, return that estimated date. Else, if attribute is set, return that date. Else
    * null.
    */
   @Override
   public Date getWorldViewReleaseDate() throws OseeCoreException {
      Collection<VersionArtifact> vers =
         getRelatedArtifacts(AtsRelationTypes.TeamWorkflowTargetedForVersion_Version, VersionArtifact.class);
      Date date = null;
      if (vers.size() > 0) {
         date = vers.iterator().next().getReleaseDate();
         if (date == null) {
            date = getSoleAttributeValue(AtsAttributeTypes.ReleaseDate, null);
         }
      } else {
         date = getSoleAttributeValue(AtsAttributeTypes.ReleaseDate, null);
      }
      return date;
   }

   @Override
   public Collection<User> getImplementers() throws OseeCoreException {
      return getImplementersByState(DefaultTeamState.Implement.name());
   }

   @Override
   public String getWorldViewDeadlineDateStr() throws OseeCoreException {
      Date date = getWorldViewDeadlineDate();
      if (date != null) {
         return XDate.getDateStr(date, XDate.MMDDYY);
      }
      return "";
   }

   @Override
   public Date getWorldViewDeadlineDate() throws OseeCoreException {
      return getSoleAttributeValue(AtsAttributeTypes.NeedBy, null);
   }

   @Override
   public double getWorldViewWeeklyBenefit() throws OseeCoreException {
      if (isAttributeTypeValid(AtsAttributeTypes.WeeklyBenefit)) {
         return 0;
      }
      String value = getSoleAttributeValue(AtsAttributeTypes.WeeklyBenefit, "");
      if (!Strings.isValid(value)) {
         return 0;
      }
      return new Float(value).doubleValue();
   }

   @Override
   public double getWorldViewAnnualCostAvoidance() throws OseeCoreException {
      double benefit = getWorldViewWeeklyBenefit();
      double remainHrs = getRemainHoursTotal();
      return benefit * 52 - remainHrs;
   }

   private Result actionableItemsTx(Branch branch, Set<ActionableItemArtifact> selectedAlias, TeamDefinitionArtifact teamDefinition) throws OseeCoreException {
      Result workResult = actionableItemsDam.setActionableItems(selectedAlias);
      if (workResult.isTrue()) {
         if (teamDefinition != null) {
            setTeamDefinition(teamDefinition);
         }
         SkynetTransaction transaction = new SkynetTransaction(branch, "Converate Actionable Item");
         getParentActionArtifact().resetAttributesOffChildren(transaction);
         persist(transaction);
         transaction.execute();
      }
      return workResult;
   }

   @Override
   public String getWorldViewBranchStatus() {
      try {
         if (getBranchMgr().isWorkingBranchInWork()) {
            return "Working";
         } else if (getBranchMgr().isCommittedBranchExists()) {
            if (!getBranchMgr().isAllObjectsToCommitToConfigured() || !getBranchMgr().isBranchesAllCommitted()) {
               return "Needs Commit";
            }
            return "Committed";
         }
         return "";
      } catch (Exception ex) {
         return "Exception: " + ex.getLocalizedMessage();
      }
   }

   @Override
   public Artifact getArtifact() {
      return this;
   }

   @Override
   public Branch getWorkingBranch() throws OseeCoreException {
      return getBranchMgr().getWorkingBranch();
   }

   @Override
   public String getWorldViewParentID() throws OseeCoreException {
      return getParentActionArtifact().getHumanReadableId();
   }

   @Override
   public Date getWorldViewEstimatedCompletionDate() throws OseeCoreException {
      Date date = super.getWorldViewEstimatedCompletionDate();
      if (date == null) {
         date = getWorldViewEstimatedReleaseDate();
      }
      return date;
   }

   public AtsBranchManager getBranchMgr() {
      return branchMgr;
   }

   /**
    * 5-9 character short name for UI and display purposes
    */
   public String getArtifactTypeShortName() {
      return "";
   }

   public String getBranchName() {
      String smaTitle = getName();
      if (smaTitle.length() > 40) {
         smaTitle = smaTitle.substring(0, 39) + "...";
      }
      if (Strings.isValid(getArtifactTypeShortName())) {
         return String.format("%s - %s - %s", getHumanReadableId(), getArtifactTypeShortName(), smaTitle);
      } else {
         return String.format("%s - %s", getHumanReadableId(), smaTitle);
      }
   }

}
