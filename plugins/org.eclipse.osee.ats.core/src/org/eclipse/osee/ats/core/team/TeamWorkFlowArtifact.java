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

package org.eclipse.osee.ats.core.team;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.core.action.ActionArtifact;
import org.eclipse.osee.ats.core.action.ActionArtifactRollup;
import org.eclipse.osee.ats.core.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.internal.Activator;
import org.eclipse.osee.ats.core.review.AbstractReviewArtifact;
import org.eclipse.osee.ats.core.review.ReviewManager;
import org.eclipse.osee.ats.core.task.AbstractTaskableArtifact;
import org.eclipse.osee.ats.core.util.AtsCacheManager;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.workflow.ActionableItemManagerCore;
import org.eclipse.osee.ats.core.workflow.StateManager;
import org.eclipse.osee.ats.shared.AtsAttributeTypes;
import org.eclipse.osee.ats.shared.AtsRelationTypes;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;
import org.eclipse.osee.framework.skynet.core.artifact.IATSStateMachineArtifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Donald G. Dunne
 */
public class TeamWorkFlowArtifact extends AbstractTaskableArtifact implements IATSStateMachineArtifact {

   private static final Set<Integer> teamArtsWithNoAction = new HashSet<Integer>();
   private final ActionableItemManagerCore actionableItemsDam;
   private boolean creatingWorkingBranch = false;
   private boolean committingWorkingBranch = false;

   public TeamWorkFlowArtifact(ArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, IArtifactType artifactType) throws OseeCoreException {
      super(parentFactory, guid, humanReadableId, branch, artifactType);
      actionableItemsDam = new ActionableItemManagerCore(this);
   }

   @Override
   public void getSmaArtifactsOneLevel(AbstractWorkflowArtifact smaArtifact, Set<Artifact> artifacts) throws OseeCoreException {
      super.getSmaArtifactsOneLevel(smaArtifact, artifacts);
      try {
         if (getTargetedVersion() != null) {
            artifacts.add(getTargetedVersion());
         }
         artifacts.addAll(ReviewManager.getReviews(this));
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
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
         ActionArtifact parentAction = getParentActionArtifact();
         if (parentAction != null) {
            ActionArtifactRollup rollup = new ActionArtifactRollup(parentAction, transaction);
            rollup.resetAttributesOffChildren();
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Can't reset Action parent of children", ex);
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
   public String getEditorTitle() throws OseeCoreException {
      try {
         if (getTargetedVersion() != null) {
            return getType() + ": " + "[" + getTargetedVersionStr() + "] - " + getName();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return super.getEditorTitle();
   }

   public ActionableItemManagerCore getActionableItemsDam() {
      return actionableItemsDam;
   }

   public void setTeamDefinition(TeamDefinitionArtifact tda) throws OseeCoreException {
      this.setSoleAttributeValue(AtsAttributeTypes.TeamDefinition, tda.getGuid());
   }

   public TeamDefinitionArtifact getTeamDefinition() throws OseeCoreException, OseeCoreException {
      String guid = this.getSoleAttributeValue(AtsAttributeTypes.TeamDefinition, "");
      if (!Strings.isValid(guid)) {
         throw new OseeArgumentException("TeamWorkflow [%s] has no TeamDefinition associated.", getHumanReadableId());
      }
      return AtsCacheManager.getTeamDefinitionArtifact(guid);
   }

   public String getTeamName() {
      try {
         return getTeamDefinition().getName();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return "!Error";
      }
   }

   @Override
   public String getType() {
      return getTeamName() + " Workflow";
   }

   @Override
   public void atsDelete(Set<Artifact> deleteArts, Map<Artifact, Object> allRelated) throws OseeCoreException {
      super.atsDelete(deleteArts, allRelated);
      for (AbstractReviewArtifact reviewArt : ReviewManager.getReviews(this)) {
         reviewArt.atsDelete(deleteArts, allRelated);
      }
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
      Collection<Artifact> arts = getRelatedArtifacts(AtsRelationTypes.ActionToWorkflow_Action);
      if (arts.isEmpty()) {
         // Only show exception once in log
         if (!teamArtsWithNoAction.contains(getArtId())) {
            if (!AtsUtilCore.isInTest()) {
               OseeLog.log(Activator.class, Level.SEVERE,
                  String.format("Team Workflow has no parent Action [%s]", toStringWithId()));
            }
            teamArtsWithNoAction.add(getArtId());
         }
      } else if (arts.size() > 1) {
         throw new OseeStateException("Team [%s] has multiple parent Actions", getGuid());
      }
      if (arts.size() > 0) {
         parentAction = (ActionArtifact) arts.iterator().next();
      }
      return parentAction;
   }

   @Override
   public AbstractWorkflowArtifact getParentAWA() {
      return null;
   }

   @Override
   public double getManHrsPerDayPreference() throws OseeCoreException {
      try {
         return getTeamDefinition().getManDayHrsFromItemAndChildren();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return super.getManHrsPerDayPreference();
   }

   @Override
   public List<IBasicUser> getImplementers() throws OseeCoreException {
      return StateManager.getImplementersByState(this, TeamState.Implement);
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

   public Branch getWorkingBranchForceCacheUpdate() throws OseeCoreException {
      return AtsBranchManagerCore.getWorkingBranch(this, true);
   }

   public Branch getWorkingBranch() throws OseeCoreException {
      return AtsBranchManagerCore.getWorkingBranch(this);
   }

   public String getBranchName() {
      String smaTitle = getName();
      if (smaTitle.length() > 40) {
         smaTitle = smaTitle.substring(0, 39) + "...";
      }
      String typeName = TeamWorkFlowManager.getArtifactTypeShortName(this);
      if (Strings.isValid(typeName)) {
         return String.format("%s - %s - %s", getHumanReadableId(), typeName, smaTitle);
      } else {
         return String.format("%s - %s", getHumanReadableId(), smaTitle);
      }
   }

   public boolean isWorkingBranchCreationInProgress() {
      return creatingWorkingBranch;
   }

   public void setWorkingBranchCreationInProgress(boolean inProgress) {
      this.creatingWorkingBranch = inProgress;
   }

   public boolean isWorkingBranchCommitInProgress() {
      return committingWorkingBranch;
   }

   public void setWorkingBranchCommitInProgress(boolean inProgress) {
      this.committingWorkingBranch = inProgress;
   }

}
