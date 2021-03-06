/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.task;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.ats.core.client.team.TeamState;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionHelper;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionManager;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionOption;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.core.workdef.WorkDefinitionMatch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;

/**
 * @author Donald G. Dunne
 */
public class TaskManager {

   public static TaskArtifact cast(Artifact artifact) {
      if (artifact instanceof TaskArtifact) {
         return (TaskArtifact) artifact;
      }
      return null;
   }

   public static Result transitionToCompleted(TaskArtifact taskArt, double estimatedHours, double additionalHours, SkynetTransaction transaction) {
      if (taskArt.isInState(TeamState.Completed)) {
         return Result.TrueResult;
      }
      // Assign current user if unassigned
      try {
         if (taskArt.getStateMgr().isUnAssigned()) {
            taskArt.getStateMgr().setAssignee(AtsClientService.get().getUserAdmin().getCurrentUser());
         }
         taskArt.getStateMgr().updateMetrics(taskArt.getStateDefinition(), additionalHours, 100, true);
         if (estimatedHours > 0.0) {
            taskArt.setSoleAttributeValue(AtsAttributeTypes.EstimatedHours, estimatedHours);
         }
         TransitionHelper helper =
            new TransitionHelper("Transition to Completed", Arrays.asList(taskArt), TaskStates.Completed.getName(),
               null, null);
         TransitionManager transitionMgr = new TransitionManager(helper, transaction);
         TransitionResults results = transitionMgr.handleAll();
         if (results.isEmpty()) {
            return Result.TrueResult;
         }
         return new Result("Transition Error %s", results.toString());
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return new Result(
            "Exception occurred while transitioning to completed (see error log) " + ex.getLocalizedMessage());
      }
   }

   public static Result transitionToInWork(TaskArtifact taskArt, IAtsUser toUser, int percentComplete, double additionalHours, SkynetTransaction transaction) throws OseeCoreException {
      if (taskArt.isInState(TaskStates.InWork)) {
         return Result.TrueResult;
      }
      TransitionHelper helper =
         new TransitionHelper("Transition to InWork", Arrays.asList(taskArt), TaskStates.InWork.getName(),
            Arrays.asList(toUser), null, TransitionOption.OverrideAssigneeCheck);
      TransitionManager transitionMgr = new TransitionManager(helper, transaction);
      TransitionResults results = transitionMgr.handleAll();
      if (!results.isEmpty()) {
         return new Result("Transition Error %s", results.toString());
      }
      if (taskArt.getStateMgr().getPercentComplete(taskArt.getCurrentStateName()) != percentComplete || additionalHours > 0) {
         taskArt.getStateMgr().updateMetrics(taskArt.getStateDefinition(), additionalHours, percentComplete, true);
      }
      if (transaction != null) {
         taskArt.saveSMA(transaction);
      }
      return Result.TrueResult;
   }

   public static Result moveTasks(TeamWorkFlowArtifact newParent, List<TaskArtifact> taskArts) throws OseeCoreException {
      for (TaskArtifact taskArt : taskArts) {
         // task dropped on same awa as current parent; do nothing
         if (taskArt.getParentAWA().equals(newParent)) {
            return Result.FalseResult;
         }

         // Validate able to move tasks; WorkDefinitions must match
         boolean taskOverridesItsWorkDefinition =
            AtsClientService.get().getWorkDefinitionAdmin().isTaskOverridingItsWorkDefinition(taskArt);
         WorkDefinitionMatch match =
            AtsClientService.get().getWorkDefinitionAdmin().getWorkDefinitionForTaskNotYetCreated(newParent);
         if (!taskOverridesItsWorkDefinition && !taskArt.getWorkDefinition().equals(match.getWorkDefinition())) {
            return new Result(
               "Desitination Task WorkDefinition does not match current Task WorkDefintion; Move Aborted");
         }
      }

      // Move Tasks
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtilCore.getAtsBranch(), "Drop Add Tasks");
      for (Artifact art : taskArts) {
         if (art.isOfType(AtsArtifactTypes.Task)) {
            TaskArtifact taskArt = (TaskArtifact) art;
            taskArt.clearCaches();
            if (taskArt.getParentAWA() != null) {
               taskArt.deleteRelation(AtsRelationTypes.TeamWfToTask_TeamWf, taskArt.getParentAWA());
            }
            taskArt.addRelation(AtsRelationTypes.TeamWfToTask_TeamWf, newParent);
            taskArt.persist(transaction);
            taskArt.clearCaches();
         }
      }
      transaction.execute();
      return Result.TrueResult;
   }

}
