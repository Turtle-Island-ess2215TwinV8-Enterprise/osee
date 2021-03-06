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

package org.eclipse.osee.ats.core.client.team;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.Assert;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.client.workflow.ITeamWorkflowProvider;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionHelper;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionManager;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionOption;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionResults;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.ExtensionDefinedObjects;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * Methods in support of programatically transitioning the DefaultWorkFlow through it's states. Only to be used for the
 * DefaultTeamWorkflow of Endorse->Analyze->Auth->Implement->Complete
 * 
 * @author Donald G. Dunne
 */
public class TeamWorkFlowManager {

   private final TeamWorkFlowArtifact teamArt;
   private final TransitionOption[] transitionOptions;

   public TeamWorkFlowManager(TeamWorkFlowArtifact teamArt, TransitionOption... transitionOptions) {
      this.teamArt = teamArt;
      this.transitionOptions = transitionOptions;
   }

   /**
    * Quickly transition to a state with minimal metrics and data entered. Should only be used for automated transition
    * for things such as developmental testing and demos.
    * 
    * @param user Users to be assigned after transition and that did the current state work
    */
   public Result transitionTo(TeamState toState, IAtsUser user, boolean popup, SkynetTransaction transaction) throws OseeCoreException {
      return transitionTo(toState, user, Arrays.asList(user), popup, transaction);
   }

   /**
    * Quickly transition to a state with minimal metrics and data entered. Should only be used for automated transition
    * for things such as developmental testing and demos.
    * 
    * @param transitionToAssignees Users to be assigned after transition
    * @param currentStateUser User that did work on current state
    */
   public Result transitionTo(TeamState toState, IAtsUser currentStateUser, Collection<IAtsUser> transitionToAssignees, boolean popup, SkynetTransaction transaction) throws OseeCoreException {
      Assert.isNotNull(currentStateUser);
      Conditions.checkNotNullOrEmpty(transitionToAssignees, "transitionToAssignees");
      Date date = new Date();
      if (toState == TeamState.Endorse) {
         if (!teamArt.getCurrentStateName().equals(TeamState.Endorse.getName())) {
            return new Result("Workflow current state [%s] past desired Endorse state", teamArt.getCurrentStateName());
         }
         return Result.TrueResult;
      }
      if (teamArt.isInState(TeamState.Endorse)) {
         Result result =
            processEndorseState(popup, teamArt, currentStateUser, transitionToAssignees, date, transaction);
         if (result.isFalse()) {
            return result;
         }
      }
      if (toState == TeamState.Analyze) {
         return Result.TrueResult;
      }

      if (teamArt.isInState(TeamState.Analyze)) {
         Result result =
            processAnalyzeState(popup, teamArt, currentStateUser, transitionToAssignees, date, transaction);
         if (result.isFalse()) {
            return result;
         }
      }

      if (toState == TeamState.Authorize) {
         return Result.TrueResult;
      }

      if (teamArt.isInState(TeamState.Authorize)) {
         Result result =
            processAuthorizeState(popup, teamArt, currentStateUser, transitionToAssignees, date, transaction);
         if (result.isFalse()) {
            return result;
         }
      }

      if (toState == TeamState.Implement) {
         return Result.TrueResult;
      }

      if (teamArt.isInState(TeamState.Implement)) {
         Result result = transitionToState(popup, teamArt, TeamState.Completed, transitionToAssignees, transaction);
         if (result.isFalse()) {
            return result;
         }
      }
      return Result.TrueResult;

   }

   private Result processAuthorizeState(boolean popup, TeamWorkFlowArtifact teamArt, IAtsUser currentStateUser, Collection<IAtsUser> transitionToAssignees, Date date, SkynetTransaction transaction) throws OseeCoreException {
      Result result = setAuthorizeData(popup, 100, .2, currentStateUser, date);
      if (result.isFalse()) {
         return result;
      }
      result = transitionToState(popup, teamArt, TeamState.Implement, transitionToAssignees, transaction);
      if (result.isFalse()) {
         return result;
      }
      return Result.TrueResult;
   }

   private Result processAnalyzeState(boolean popup, TeamWorkFlowArtifact teamArt, IAtsUser currentStateUser, Collection<IAtsUser> transitionToAssignees, Date date, SkynetTransaction transaction) throws OseeCoreException {
      Result result = setAnalyzeData(popup, null, null, 1, 100, .2, currentStateUser, date);
      if (result.isFalse()) {
         return result;
      }
      result = transitionToState(popup, teamArt, TeamState.Authorize, transitionToAssignees, transaction);
      if (result.isFalse()) {
         return result;
      }
      return Result.TrueResult;
   }

   private Result processEndorseState(boolean popup, TeamWorkFlowArtifact teamArt, IAtsUser currentStateUser, Collection<IAtsUser> transitionToAssignees, Date date, SkynetTransaction transaction) throws OseeCoreException {
      Result result = setEndorseData(popup, null, 100, .2, currentStateUser, date);
      if (result.isFalse()) {
         return result;
      }
      result = transitionToState(popup, teamArt, TeamState.Analyze, transitionToAssignees, transaction);
      if (result.isFalse()) {
         return result;
      }
      return Result.TrueResult;
   }

   private Result transitionToState(boolean popup, TeamWorkFlowArtifact teamArt, IStateToken toState, Collection<IAtsUser> transitionToAssignees, SkynetTransaction transaction) {
      TransitionHelper helper =
         new TransitionHelper("Transition to " + toState.getName(), Arrays.asList(teamArt), toState.getName(),
            transitionToAssignees, null, transitionOptions);
      TransitionManager transitionMgr = new TransitionManager(helper, transaction);
      TransitionResults results = transitionMgr.handleAll();
      if (results.isEmpty()) {
         return Result.TrueResult;
      }
      return new Result("Transition Error %s", results.toString());
   }

   public Result setEndorseData(boolean popup, String propRes, int statePercentComplete, double stateHoursSpent, IAtsUser user, Date date) throws OseeCoreException {
      if (!teamArt.isInState(TeamState.Endorse)) {
         Result result = new Result("Action not in Endorse state");
         if (result.isFalse() && popup) {
            return result;
         }
      }
      teamArt.getStateMgr().setMetrics(TeamState.Endorse, stateHoursSpent, statePercentComplete, true, user, date);
      return Result.TrueResult;
   }

   public Result setAnalyzeData(boolean popup, String problem, String propRes, double hourEstimate, int statePercentComplete, double stateHoursSpent, IAtsUser user, Date date) throws OseeCoreException {
      if (!teamArt.isInState(TeamState.Analyze)) {
         Result result = new Result("Action not in Analyze state");
         if (result.isFalse() && popup) {
            return result;
         }
      }
      teamArt.setSoleAttributeValue(AtsAttributeTypes.EstimatedHours, hourEstimate);
      teamArt.getStateMgr().setMetrics(TeamState.Analyze, stateHoursSpent, statePercentComplete, true, user, date);
      return Result.TrueResult;
   }

   public Result setAuthorizeData(boolean popup, int statePercentComplete, double stateHoursSpent, IAtsUser user, Date date) throws OseeCoreException {
      if (!teamArt.isInState(TeamState.Authorize)) {
         Result result = new Result("Action not in Authorize state");
         if (result.isFalse() && popup) {
            return result;
         }
      }
      teamArt.getStateMgr().setMetrics(TeamState.Authorize, stateHoursSpent, statePercentComplete, true, user, date);
      return Result.TrueResult;
   }

   public Result setImplementData(boolean popup, String resolution, int statePercentComplete, double stateHoursSpent, IAtsUser user, Date date) throws OseeCoreException {
      if (!teamArt.isInState(TeamState.Implement)) {
         Result result = new Result("Action not in Implement state");
         if (result.isFalse() && popup) {
            return result;
         }
      }
      teamArt.getStateMgr().setMetrics(TeamState.Implement, stateHoursSpent, statePercentComplete, true, user, date);
      return Result.TrueResult;
   }

   private static final ITeamWorkflowProviders teamWorkflowProviders = new ITeamWorkflowProviders() {
      private final ExtensionDefinedObjects<ITeamWorkflowProvider> extensionDefinedObjects =
         new ExtensionDefinedObjects<ITeamWorkflowProvider>("org.eclipse.osee.ats.core.client.AtsTeamWorkflowProvider",
            "AtsTeamWorkflowProvider", "classname");

      @Override
      public List<ITeamWorkflowProvider> getTeamWorkflowProviders() {
         return extensionDefinedObjects.getObjects();
      }

      @Override
      public Iterator<ITeamWorkflowProvider> iterator() {
         return getTeamWorkflowProviders().iterator();
      }
   };

   public static ITeamWorkflowProviders getTeamWorkflowProviders() {
      return teamWorkflowProviders;
   }

   /**
    * Assigned or computed Id that will show at the top of the editor
    */
   public static String getPcrId(AbstractWorkflowArtifact awa) throws OseeCoreException {
      TeamWorkFlowArtifact teamArt = awa.getParentTeamWorkflow();
      if (teamArt != null) {
         for (ITeamWorkflowProvider atsTeamWorkflow : getTeamWorkflowProviders()) {
            if (atsTeamWorkflow.isResponsibleFor(awa)) {
               String pcrId = atsTeamWorkflow.getPcrId(teamArt);
               if (Strings.isValid(pcrId)) {
                  return pcrId;
               }
            }
         }
         return teamArt.getTeamName() + " " + awa.getHumanReadableId();
      }
      return "";
   }

   public static String getArtifactTypeShortName(TeamWorkFlowArtifact teamArt) {
      for (ITeamWorkflowProvider atsTeamWorkflow : getTeamWorkflowProviders()) {
         String typeName = atsTeamWorkflow.getArtifactTypeShortName(teamArt);
         if (Strings.isValid(typeName)) {
            return typeName;
         }
      }
      return null;
   }

   public static TeamWorkFlowArtifact cast(Artifact artifact) {
      if (artifact instanceof AbstractWorkflowArtifact) {
         return (TeamWorkFlowArtifact) artifact;
      }
      return null;
   }

   public static String getBranchName(TeamWorkFlowArtifact teamArt) {
      for (ITeamWorkflowProvider teamExtension : getTeamWorkflowProviders()) {
         String name = teamExtension.getBranchName(teamArt);
         if (Strings.isValid(name)) {
            return name;
         }
      }
      return teamArt.getBranchName();
   }

   public static ITeamWorkflowProvider getTeamWorkflowProvider(IAtsTeamDefinition teamDef, Collection<IAtsActionableItem> actionableItems) {
      for (ITeamWorkflowProvider teamExtension : getTeamWorkflowProviders()) {
         boolean isResponsible = false;
         try {
            isResponsible = teamExtension.isResponsibleForTeamWorkflowCreation(teamDef, actionableItems);
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.WARNING, ex);
         }
         if (isResponsible) {
            return teamExtension;
         }
      }
      return null;
   }

   public static IArtifactType getTeamWorkflowArtifactType(IAtsTeamDefinition teamDef, Collection<IAtsActionableItem> actionableItems) throws OseeCoreException {
      IArtifactType teamWorkflowArtifactType = AtsArtifactTypes.TeamWorkflow;
      ITeamWorkflowProvider teamWorkflowProvider = getTeamWorkflowProvider(teamDef, actionableItems);
      if (teamWorkflowProvider != null) {
         teamWorkflowArtifactType = teamWorkflowProvider.getTeamWorkflowArtifactType(teamDef, actionableItems);
      }
      return teamWorkflowArtifactType;
   }

   /**
    * Uses artifact type inheritance to retrieve all TeamWorkflow artifact types
    */
   public static Set<IArtifactType> getTeamWorkflowArtifactTypes() throws OseeCoreException {
      Set<IArtifactType> artifactTypes = new HashSet<IArtifactType>();
      getTeamWorkflowArtifactTypesRecursive(ArtifactTypeManager.getType(AtsArtifactTypes.TeamWorkflow), artifactTypes);
      return artifactTypes;
   }

   private static void getTeamWorkflowArtifactTypesRecursive(ArtifactType artifactType, Set<IArtifactType> allArtifactTypes) throws OseeCoreException {
      allArtifactTypes.add(artifactType);
      for (IArtifactType child : artifactType.getFirstLevelDescendantTypes()) {
         getTeamWorkflowArtifactTypesRecursive(ArtifactTypeManager.getType(child), allArtifactTypes);
      }
   }

   public static TeamWorkFlowArtifact getTeamWorkflowArt(IAtsTeamWorkflow teamWf) throws OseeCoreException {
      return (TeamWorkFlowArtifact) ArtifactQuery.getArtifactFromId(teamWf.getGuid(), AtsUtilCore.getAtsBranchToken());
   }

}
