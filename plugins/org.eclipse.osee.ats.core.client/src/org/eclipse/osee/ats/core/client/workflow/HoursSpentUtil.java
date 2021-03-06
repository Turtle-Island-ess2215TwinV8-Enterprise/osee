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
package org.eclipse.osee.ats.core.client.workflow;

import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.review.ReviewManager;
import org.eclipse.osee.ats.core.client.task.AbstractTaskableArtifact;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.SimpleTeamState;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.core.client.util.WorkflowManagerCore;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class HoursSpentUtil {

   /**
    * Return hours spent working states, reviews and tasks (not children SMAs)
    */
   public static double getHoursSpentTotal(Artifact artifact) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.Action)) {
         double hours = 0;
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(artifact)) {
            if (!team.isCancelled()) {
               hours += getHoursSpentTotal(team);
            }
         }
         return hours;
      }
      if (artifact.isOfType(AtsArtifactTypes.AbstractWorkflowArtifact)) {
         AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) artifact;
         return getHoursSpentTotal(artifact, awa.getStateMgr().getCurrentState());
      }
      return 0;
   }

   /**
    * Return hours spent working all states, reviews and tasks (not children SMAs)
    */
   public static double getHoursSpentTotal(Artifact artifact, IStateToken state) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.AbstractWorkflowArtifact)) {
         AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) artifact;
         double hours = 0.0;
         for (String stateName : awa.getStateMgr().getVisitedStateNames()) {
            hours += getHoursSpentStateTotal(awa, new SimpleTeamState(stateName, StateType.Working));
         }
         return hours;
      }
      return 0;
   }

   /**
    * Return hours spent working SMA state, state tasks and state reviews (not children SMAs)
    */
   public static double getHoursSpentStateTotal(Artifact artifact) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.Action)) {
         double hours = 0;
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(artifact)) {
            if (!team.isCancelled()) {
               hours += getHoursSpentStateTotal(team);
            }
         }
         return hours;
      }
      if (artifact.isOfType(AtsArtifactTypes.AbstractWorkflowArtifact)) {
         return getHoursSpentStateTotal(artifact, WorkflowManagerCore.getStateManager(artifact).getCurrentState());
      }
      return 0;
   }

   /**
    * Return hours spent working SMA state, state tasks and state reviews (not children SMAs)
    */
   public static double getHoursSpentStateTotal(Artifact artifact, IStateToken state) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.AbstractWorkflowArtifact)) {
         AbstractWorkflowArtifact awa = WorkflowManagerCore.cast(artifact);
         return getHoursSpentSMAState(awa, state) + getHoursSpentFromStateTasks(awa, state) + getHoursSpentStateReview(
            awa, state);
      }
      return 0;
   }

   /**
    * Return hours spent working ONLY the SMA stateName (not children SMAs)
    */
   public static double getHoursSpentStateReview(Artifact artifact) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.Action)) {
         double hours = 0;
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(artifact)) {
            if (!team.isCancelled()) {
               hours += getHoursSpentStateReview(team);
            }
         }
         return hours;
      }
      if (artifact.isOfType(AtsArtifactTypes.AbstractWorkflowArtifact)) {
         return getHoursSpentStateReview(artifact, WorkflowManagerCore.getStateManager(artifact).getCurrentState());
      }
      return 0;
   }

   /**
    * Return hours spent working ONLY the SMA stateName (not children SMAs)
    */
   public static double getHoursSpentStateReview(Artifact artifact, IStateToken state) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.TeamWorkflow)) {
         return ReviewManager.getHoursSpent(TeamWorkFlowManager.cast(artifact), state);
      }
      return 0;
   }

   /**
    * Return hours spent working ONLY the SMA stateName (not children SMAs)
    */
   public static double getHoursSpentSMAState(Artifact artifact) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.Action)) {
         double hours = 0;
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(artifact)) {
            if (!team.isCancelled()) {
               hours += getHoursSpentSMAState(team);
            }
         }
         return hours;
      }
      if (artifact.isOfType(AtsArtifactTypes.AbstractWorkflowArtifact)) {
         return getHoursSpentSMAState(artifact, WorkflowManagerCore.getStateManager(artifact).getCurrentState());
      }
      return 0;
   }

   /**
    * Return hours spent working ONLY the SMA stateName (not children SMAs)
    */
   public static double getHoursSpentSMAState(Artifact artifact, IStateToken state) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.AbstractWorkflowArtifact)) {
         return WorkflowManagerCore.getStateManager(artifact).getHoursSpent(state.getName());
      }
      return 0;
   }

   /**
    * Return hours spent working ONLY on tasks related to stateName
    */
   public static double getHoursSpentFromStateTasks(Artifact artifact) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.Action)) {
         double hours = 0;
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(artifact)) {
            if (!team.isCancelled()) {
               hours += getHoursSpentFromStateTasks(team);
            }
         }
         return hours;
      }
      if (artifact instanceof AbstractTaskableArtifact) {
         return getHoursSpentFromStateTasks(artifact,
            ((AbstractWorkflowArtifact) artifact).getStateMgr().getCurrentState());
      }
      return 0;
   }

   /**
    * Return Hours Spent for Tasks of "Related to State" stateName
    *
    * @param relatedToState state name of parent workflow's state
    * @return Returns the Hours Spent
    */
   public static double getHoursSpentFromStateTasks(Artifact artifact, IStateToken relatedToState) throws OseeCoreException {
      double spent = 0;
      if (artifact instanceof AbstractTaskableArtifact) {
         for (TaskArtifact taskArt : ((AbstractTaskableArtifact) artifact).getTaskArtifacts(relatedToState)) {
            spent += HoursSpentUtil.getHoursSpentTotal(taskArt);
         }
      }
      return spent;
   }

}
