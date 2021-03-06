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
package org.eclipse.osee.ats.core.client.action;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.core.client.notify.AtsNotificationManager;
import org.eclipse.osee.ats.core.client.team.CreateTeamOption;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.ChangeType;
import org.eclipse.osee.ats.core.client.workflow.ChangeTypeUtil;
import org.eclipse.osee.ats.core.client.workflow.ITeamWorkflowProvider;
import org.eclipse.osee.ats.core.config.TeamDefinitions;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Donald G. Dunne
 */
public class ActionManager {

   public static ActionArtifact createAction(IProgressMonitor monitor, String title, String desc, ChangeType changeType, String priority, boolean validationRequired, Date needByDate, Collection<IAtsActionableItem> actionableItems, Date createdDate, IAtsUser createdBy, INewActionListener newActionListener, SkynetTransaction transaction) throws OseeCoreException {
      Conditions.checkNotNullOrEmptyOrContainNull(actionableItems, "actionableItems");
      // if "tt" is title, this is an action created for development. To
      // make it easier, all fields are automatically filled in for ATS developer

      if (monitor != null) {
         monitor.subTask("Creating Action");
      }
      ActionArtifact actionArt =
         (ActionArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.Action, AtsUtilCore.getAtsBranch());
      setArtifactIdentifyData(actionArt, title, desc, changeType, priority, validationRequired, needByDate);

      // Retrieve Team Definitions corresponding to selected Actionable Items
      if (monitor != null) {
         monitor.subTask("Creating WorkFlows");
      }
      Collection<IAtsTeamDefinition> teamDefs = TeamDefinitions.getImpactedTeamDefs(actionableItems);
      if (teamDefs.isEmpty()) {
         StringBuffer sb = new StringBuffer("No teams returned for Action's selected Actionable Items\n");
         for (IAtsActionableItem aia : actionableItems) {
            sb.append("Selected AI \"" + aia + "\" " + aia.getHumanReadableId() + "\n");
         }
         throw new OseeStateException(sb.toString());
      }

      // Create team workflow artifacts
      for (IAtsTeamDefinition teamDef : teamDefs) {
         List<IAtsUser> leads = new LinkedList<IAtsUser>(teamDef.getLeads(actionableItems));
         TeamWorkFlowArtifact teamWf =
            createTeamWorkflow(actionArt, teamDef, actionableItems, leads, transaction, createdDate, createdBy,
               newActionListener);
         teamWf.getStateMgr().writeToArtifact();
      }

      // Notify listener of action creation
      if (newActionListener != null) {
         newActionListener.actionCreated(actionArt);
      }

      actionArt.persist(transaction);
      return actionArt;
   }

   public static TeamWorkFlowArtifact createTeamWorkflow(Artifact actionArt, IAtsTeamDefinition teamDef, Collection<IAtsActionableItem> actionableItems, List<? extends IAtsUser> assignees, SkynetTransaction transaction, Date createdDate, IAtsUser createdBy, INewActionListener newActionListener, CreateTeamOption... createTeamOption) throws OseeCoreException {
      ITeamWorkflowProvider teamExt = TeamWorkFlowManager.getTeamWorkflowProvider(teamDef, actionableItems);
      IArtifactType teamWorkflowArtifactType =
         TeamWorkFlowManager.getTeamWorkflowArtifactType(teamDef, actionableItems);

      // NOTE: The persist of the workflow will auto-email the assignees
      TeamWorkFlowArtifact teamArt =
         createTeamWorkflow(actionArt, teamDef, actionableItems, assignees, createdDate, createdBy, null, null,
            teamWorkflowArtifactType, newActionListener, transaction, createTeamOption);
      // Notify extension that workflow was created
      if (teamExt != null) {
         teamExt.teamWorkflowCreated(teamArt);
      }
      return teamArt;
   }

   public static TeamWorkFlowArtifact createTeamWorkflow(Artifact actionArt, IAtsTeamDefinition teamDef, Collection<IAtsActionableItem> actionableItems, List<? extends IAtsUser> assignees, Date createdDate, IAtsUser createdBy, String guid, String hrid, IArtifactType artifactType, INewActionListener newActionListener, SkynetTransaction transaction, CreateTeamOption... createTeamOption) throws OseeCoreException {

      if (!Collections.getAggregate(createTeamOption).contains(CreateTeamOption.Duplicate_If_Exists)) {
         // Make sure team doesn't already exist
         for (TeamWorkFlowArtifact teamArt : ActionManager.getTeams(actionArt)) {
            if (teamArt.getTeamDefinition().equals(teamDef)) {
               throw new OseeArgumentException("Team [%s] already exists for Action [%s]", teamDef,
                  actionArt.getHumanReadableId());
            }
         }
      }

      TeamWorkFlowArtifact teamArt = null;
      if (guid == null) {
         teamArt = (TeamWorkFlowArtifact) ArtifactTypeManager.addArtifact(artifactType, AtsUtilCore.getAtsBranch());
      } else {
         teamArt =
            (TeamWorkFlowArtifact) ArtifactTypeManager.addArtifact(artifactType, AtsUtilCore.getAtsBranch(), guid, hrid);
      }

      setArtifactIdentifyData(actionArt, teamArt);

      // Relate Workflow to ActionableItems (by guid) if team is responsible
      // for that AI
      for (IAtsActionableItem aia : actionableItems) {
         IAtsTeamDefinition teamDefinitionInherited = aia.getTeamDefinitionInherited();
         if (teamDefinitionInherited != null && teamDef.getGuid().equals(teamDefinitionInherited.getGuid())) {
            teamArt.getActionableItemsDam().addActionableItem(aia);
         }
      }

      // Relate WorkFlow to Team Definition (by guid due to relation loading issues)
      teamArt.setTeamDefinition(teamDef);

      // If work def id is specified by listener, set as attribute
      if (newActionListener != null) {
         String overrideWorkDefId = newActionListener.getOverrideWorkDefinitionId(teamArt);
         if (Strings.isValid(overrideWorkDefId)) {
            teamArt.setSoleAttributeValue(AtsAttributeTypes.WorkflowDefinition, overrideWorkDefId);
         }
      }

      // Initialize state machine
      teamArt.initializeNewStateMachine(assignees, createdDate, createdBy);

      // Notify listener of team creation
      if (newActionListener != null) {
         newActionListener.teamCreated((ActionArtifact) actionArt, teamArt, transaction);
      }

      // Relate Action to WorkFlow
      actionArt.addRelation(AtsRelationTypes.ActionToWorkflow_WorkFlow, teamArt);

      teamArt.persist(transaction);
      AtsNotificationManager.notifySubscribedByTeamOrActionableItem(teamArt);

      return teamArt;
   }

   /**
    * Set Team Workflow attributes off given action artifact
    */
   public static void setArtifactIdentifyData(Artifact fromAction, TeamWorkFlowArtifact toTeam) throws OseeCoreException {
      setArtifactIdentifyData(toTeam, fromAction.getName(),
         fromAction.getSoleAttributeValue(AtsAttributeTypes.Description, ""), ChangeTypeUtil.getChangeType(fromAction),
         fromAction.getSoleAttributeValue(AtsAttributeTypes.PriorityType, ""),
         //            fromAction.getAttributesToStringList(AtsAttributeTypes.ATS_USER_COMMUNITY),
         fromAction.getSoleAttributeValue(AtsAttributeTypes.ValidationRequired, false),
         fromAction.getSoleAttributeValue(AtsAttributeTypes.NeedBy, (Date) null));
   }

   /**
    * Since there is no shared attribute yet, action and workflow arts are all populate with identify data
    */
   public static void setArtifactIdentifyData(Artifact art, String title, String desc, ChangeType changeType, String priority, Boolean validationRequired, Date needByDate) throws OseeCoreException {
      art.setName(title);
      if (!Strings.emptyString().equals(desc)) {
         art.setSoleAttributeValue(AtsAttributeTypes.Description, desc);
      }
      ChangeTypeUtil.setChangeType(art, changeType);
      //      art.setAttributeValues(ATSAttributes.USER_COMMUNITY_ATTRIBUTE.getStoreName(), userComms);
      if (Strings.isValid(priority)) {
         art.setSoleAttributeValue(AtsAttributeTypes.PriorityType, priority);
      }
      if (needByDate != null) {
         art.setSoleAttributeValue(AtsAttributeTypes.NeedBy, needByDate);
      }
      if (validationRequired) {
         art.setSoleAttributeValue(AtsAttributeTypes.ValidationRequired, true);
      }
   }

   public static Collection<TeamWorkFlowArtifact> getTeams(Object object) throws OseeCoreException {
      if (object instanceof ActionArtifact) {
         return ((ActionArtifact) object).getTeams();
      }
      return java.util.Collections.emptyList();
   }

   public static TeamWorkFlowArtifact getFirstTeam(Object object) throws OseeCoreException {
      if (object instanceof ActionArtifact) {
         return ((ActionArtifact) object).getFirstTeam();
      }
      return null;
   }

}
