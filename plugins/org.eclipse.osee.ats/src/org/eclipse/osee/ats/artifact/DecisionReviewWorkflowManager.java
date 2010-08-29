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

import org.eclipse.osee.ats.artifact.StateMachineArtifact.TransitionOption;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.plugin.util.Result;

/**
 * Methods in support of programatically transitioning the Decision Review Workflow through it's states. Only to be used
 * for the DefaultReviewWorkflow of Prepare->Decision->ReWork->Complete
 * 
 * @author Donald G. Dunne
 */
public class DecisionReviewWorkflowManager {

   /**
    * Quickly transition to a state with minimal metrics and data entered. Should only be used for automated
    * transitioning for things such as developmental testing and demos.
    * 
    * @param user User to transition to OR null if should use user of current state
    * @return Result
    * @throws Exception
    */
   public static Result transitionTo(DecisionReviewArtifact reviewArt, DecisionReviewArtifact.DecisionReviewState toState, User user, boolean popup, SkynetTransaction transaction) throws OseeCoreException {
      Result result = Result.TrueResult;
      // If in Prepare state, set data and transition to Decision
      if (reviewArt.getStateMgr().getCurrentStateName().equals(
         DecisionReviewArtifact.DecisionReviewState.Prepare.name())) {
         result = setPrepareStateData(reviewArt, 100, 3, .2);

         if (result.isFalse()) {
            if (popup) {
               result.popup();
            }
            return result;
         }
         result =
            reviewArt.transition(DecisionReviewArtifact.DecisionReviewState.Decision.name(),
               (user != null ? user : reviewArt.getStateMgr().getAssignees().iterator().next()), transaction,
               TransitionOption.None);
      }
      if (result.isFalse()) {
         if (popup) {
            result.popup();
         }
         return result;
      }
      if (toState == DecisionReviewArtifact.DecisionReviewState.Decision) {
         return Result.TrueResult;
      }

      // If desired to transition to follow-up, then decision is false
      boolean decision = toState != DecisionReviewArtifact.DecisionReviewState.Followup;

      result = setDecisionStateData(reviewArt, decision, 100, .2);
      if (result.isFalse()) {
         if (popup) {
            result.popup();
         }
         return result;
      }

      result =
         reviewArt.transition(toState.name(),
            (user != null ? user : reviewArt.getStateMgr().getAssignees().iterator().next()), transaction,
            TransitionOption.None);
      if (result.isFalse()) {
         if (popup) {
            result.popup();
         }
         return result;
      }
      return Result.TrueResult;
   }

   public static Result setPrepareStateData(DecisionReviewArtifact reviewArt, int statePercentComplete, double estimateHours, double stateHoursSpent) throws OseeCoreException {
      if (!reviewArt.getStateMgr().getCurrentStateName().equals(
         DecisionReviewArtifact.DecisionReviewState.Prepare.name())) {
         return new Result("Action not in Prepare state");
      }
      reviewArt.setSoleAttributeValue(AtsAttributeTypes.EstimatedHours, estimateHours);
      reviewArt.getStateMgr().updateMetrics(stateHoursSpent, statePercentComplete, true);
      return Result.TrueResult;
   }

   public static Result setDecisionStateData(DecisionReviewArtifact reviewArt, boolean decision, int statePercentComplete, double stateHoursSpent) throws OseeCoreException {
      if (!reviewArt.getStateMgr().getCurrentStateName().equals(
         DecisionReviewArtifact.DecisionReviewState.Decision.name())) {
         return new Result("Action not in Decision state");
      }
      reviewArt.setSoleAttributeValue(AtsAttributeTypes.Decision, decision ? "Yes" : "No");
      reviewArt.getStateMgr().updateMetrics(stateHoursSpent, statePercentComplete, true);
      return Result.TrueResult;
   }

}
