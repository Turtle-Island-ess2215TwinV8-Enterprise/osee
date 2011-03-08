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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsNotifyUsers;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.ats.util.widgets.XActionableItemsDam;
import org.eclipse.osee.ats.util.widgets.defect.DefectManager;
import org.eclipse.osee.ats.util.widgets.role.UserRole;
import org.eclipse.osee.ats.util.widgets.role.UserRole.Role;
import org.eclipse.osee.ats.util.widgets.role.UserRoleManager;
import org.eclipse.osee.ats.workdef.ReviewBlockType;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractReviewArtifact extends AbstractTaskableArtifact {

   protected DefectManager defectManager;
   protected UserRoleManager userRoleManager;
   private XActionableItemsDam actionableItemsDam;
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
   public void onAttributePersist(SkynetTransaction transaction) {
      super.onAttributePersist(transaction);
      // Since multiple ways exist to change the assignees, notification is performed on the persist
      if (isDeleted()) {
         return;
      }
      try {
         notifyReviewersComplete();
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
   }

   @Override
   public void initalizePreSaveCache() {
      super.initalizePreSaveCache();
      try {
         preSaveReviewRoleComplete = getRoleUsersReviewComplete();
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
   }

   private Collection<UserRole> getRoleUsersReviewComplete() throws OseeCoreException {
      return this.getUserRoleManager().getRoleUsersReviewComplete();
   }

   public void notifyReviewersComplete() throws OseeCoreException {
      UserRoleManager userRoleManager = this.getUserRoleManager();
      //all reviewers are complete; send notification to author/moderator
      if (!preSaveReviewRoleComplete.equals(userRoleManager.getRoleUsersReviewComplete()) && userRoleManager.getUserRoles(
         Role.Reviewer).equals(userRoleManager.getRoleUsersReviewComplete())) {
         AtsNotifyUsers.getInstance().notify(this, AtsNotifyUsers.NotifyType.Reviewed);
      }
      preSaveReviewRoleComplete = userRoleManager.getRoleUsersReviewComplete();
   }

   /**
    * Reset managers for case where artifact is re-loaded/initialized
    * 
    * @see org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact#initialize()
    */
   @Override
   protected void initializeSMA() throws OseeCoreException {
      super.initializeSMA();
      defectManager = new DefectManager(this);
      userRoleManager = new UserRoleManager(this);
      actionableItemsDam = new XActionableItemsDam(this);
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

   public DefectManager getDefectManager() {
      if (defectManager == null) {
         defectManager = new DefectManager(this);
      }
      return defectManager;
   }

   public UserRoleManager getUserRoleManager() {
      if (userRoleManager == null) {
         return userRoleManager = new UserRoleManager(this);
      }
      return userRoleManager;
   }

   @SuppressWarnings("unused")
   // NOPMD by b0727536 on 9/29/10 8:50 AM
   public IStatus isUserRoleValid(String namespace) throws OseeCoreException {
      // Need this cause it removes all error items of this namespace
      return new Status(IStatus.OK, namespace, "");
   }

   public Set<TeamDefinitionArtifact> getCorrespondingTeamDefinitionArtifact() throws OseeCoreException {
      Set<TeamDefinitionArtifact> teamDefs = new HashSet<TeamDefinitionArtifact>();
      if (getParentTeamWorkflow() != null) {
         teamDefs.add(getParentTeamWorkflow().getTeamDefinition());
      }
      if (getActionableItemsDam().getActionableItems().size() > 0) {
         teamDefs.addAll(ActionableItemArtifact.getImpactedTeamDefs(getActionableItemsDam().getActionableItems()));
      }
      return teamDefs;
   }

   /**
    * @return the actionableItemsDam
    */
   public XActionableItemsDam getActionableItemsDam() throws OseeCoreException {
      if (actionableItemsDam == null) {
         actionableItemsDam = new XActionableItemsDam(this);
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
         OseeLog.log(AtsPlugin.class, Level.SEVERE,
            getArtifactTypeName() + " " + getHumanReadableId() + " has multiple parent workflows");
      } else if (!isStandAloneReview() && teams.isEmpty()) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE,
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

}
