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
package org.eclipse.osee.ats.core.review;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.internal.Activator;
import org.eclipse.osee.ats.core.review.role.UserRole;
import org.eclipse.osee.ats.core.review.role.UserRoleManager;
import org.eclipse.osee.ats.core.task.AbstractTaskableArtifact;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.type.AtsArtifactTypes;
import org.eclipse.osee.ats.core.type.AtsAttributeTypes;
import org.eclipse.osee.ats.core.type.AtsRelationTypes;
import org.eclipse.osee.ats.core.workdef.ReviewBlockType;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.workflow.ActionableItemManagerCore;
import org.eclipse.osee.ats.core.workflow.StateManager;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractReviewArtifact extends AbstractTaskableArtifact {

   private ActionableItemManagerCore actionableItemsDam;
   private Collection<UserRole> preSaveReviewRoleComplete;
   private Boolean standAlone = null;

   public AbstractReviewArtifact(ArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, IArtifactType artifactType) throws OseeCoreException {
      super(parentFactory, guid, humanReadableId, branch, artifactType);
   }

   @Override
   public void onInitializationComplete() throws OseeCoreException {
      super.onInitializationComplete();
      initializeSMA();
   };

   @Override
   public void initalizePreSaveCache() {
      super.initalizePreSaveCache();
      try {
         preSaveReviewRoleComplete = getRoleUsersReviewComplete();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   @Override
   public Collection<IBasicUser> getImplementers() throws OseeCoreException {
      if (this.isOfType(AtsArtifactTypes.DecisionReview)) {
         return StateManager.getImplementersByState(this, DecisionReviewState.Decision);
      } else {
         Collection<IBasicUser> users = StateManager.getImplementersByState(this, PeerToPeerReviewState.Review);
         for (UserRole role : UserRoleManager.getUserRoles(this)) {
            users.add(role.getUser());
         }
         return users;
      }
   }

   private Collection<UserRole> getRoleUsersReviewComplete() throws OseeCoreException {
      return new UserRoleManager(this).getRoleUsersReviewComplete();
   }

   public void notifyReviewersComplete() throws OseeCoreException {
      UserRoleManager userRoleManager = new UserRoleManager(this);
      //all reviewers are complete; send notification to author/moderator
      // TODO Add this back in once move to ats.core
      //      if (!preSaveReviewRoleComplete.equals(userRoleManager.getRoleUsersReviewComplete()) && userRoleManager.getUserRoles(
      //         Role.Reviewer).equals(userRoleManager.getRoleUsersReviewComplete())) {
      //         AtsNotifyUsers.getInstance().notify(this, AtsNotifyUsers.NotifyType.Reviewed);
      //      }
      preSaveReviewRoleComplete = userRoleManager.getRoleUsersReviewComplete();
   }

   /**
    * Reset managers for case where artifact is re-loaded/initialized
    * 
    * @see org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact#initialize()
    */
   @Override
   protected void initializeSMA() throws OseeCoreException {
      super.initializeSMA();
      actionableItemsDam = new ActionableItemManagerCore(this);
   }

   @Override
   public String getArtifactSuperTypeName() {
      return "Review";
   }

   public boolean isBlocking() throws OseeCoreException {
      return getReviewBlockType() != ReviewBlockType.None;
   }

   public ReviewBlockType getReviewBlockType() throws OseeCoreException {
      String typeStr = getSoleAttributeValue(AtsAttributeTypes.ReviewBlocks, null);
      if (typeStr == null) {
         // Check old attribute value
         if (getSoleAttributeValue(AtsAttributeTypes.BlockingReview, false)) {
            return ReviewBlockType.Transition;
         }
         return ReviewBlockType.None;
      }
      return ReviewBlockType.valueOf(typeStr);
   }

   public Set<TeamDefinitionArtifact> getCorrespondingTeamDefinitionArtifact() throws OseeCoreException {
      Set<TeamDefinitionArtifact> teamDefs = new HashSet<TeamDefinitionArtifact>();
      if (getParentTeamWorkflow() != null) {
         teamDefs.add(getParentTeamWorkflow().getTeamDefinition());
      }
      if (getActionableItemsDam().getActionableItems().size() > 0) {
         teamDefs.addAll(ActionableItemManagerCore.getImpactedTeamDefs(getActionableItemsDam().getActionableItems()));
      }
      return teamDefs;
   }

   /**
    * @return the actionableItemsDam
    */
   public ActionableItemManagerCore getActionableItemsDam() throws OseeCoreException {
      if (actionableItemsDam == null) {
         actionableItemsDam = new ActionableItemManagerCore(this);
      }
      return actionableItemsDam;
   }

   @Override
   public AbstractWorkflowArtifact getParentAWA() throws OseeCoreException {
      if (isStandAloneReview()) {
         return null;
      }
      if (parentAwa != null) {
         return parentAwa;
      }
      parentAwa = getParentTeamWorkflow();
      return parentAwa;
   }

   @Override
   public Artifact getParentActionArtifact() throws OseeCoreException {
      if (isStandAloneReview()) {
         return null;
      }
      if (parentAction != null) {
         return parentAction;
      }
      parentTeamArt = getParentTeamWorkflow();
      if (parentTeamArt != null) {
         parentAction = parentTeamArt.getParentActionArtifact();
      }
      return parentAction;
   }

   @Override
   public TeamWorkFlowArtifact getParentTeamWorkflow() throws OseeCoreException {
      if (isStandAloneReview()) {
         return null;
      }
      if (parentTeamArt != null) {
         return parentTeamArt;
      }
      List<TeamWorkFlowArtifact> teams =
         getRelatedArtifacts(AtsRelationTypes.TeamWorkflowToReview_Team, TeamWorkFlowArtifact.class);
      if (teams.size() > 1) {
         OseeLog.log(Activator.class, Level.SEVERE,
            getArtifactTypeName() + " " + getHumanReadableId() + " has multiple parent workflows");
      } else if (!isStandAloneReview() && teams.isEmpty()) {
         OseeLog.log(Activator.class, Level.SEVERE,
            getArtifactTypeName() + " " + getHumanReadableId() + " has no parent workflow");
      }
      if (!teams.isEmpty()) {
         parentTeamArt = teams.iterator().next();
      }
      return parentTeamArt;
   }

   public boolean isStandAloneReview() throws OseeCoreException {
      if (standAlone == null) {
         standAlone = getActionableItemsDam().getActionableItemGuids().size() > 0;
      }
      return standAlone;
   }

   @Override
   public double getWorldViewWeeklyBenefit() {
      return 0;
   }

   public Artifact getArtifact() {
      return this;
   }

   public static AbstractReviewArtifact cast(Artifact artifact) {
      if (artifact instanceof AbstractReviewArtifact) {
         return (AbstractReviewArtifact) artifact;
      }
      return null;
   }

}