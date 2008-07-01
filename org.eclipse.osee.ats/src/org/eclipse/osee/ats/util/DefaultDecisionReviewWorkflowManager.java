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

package org.eclipse.osee.ats.util;

import java.sql.SQLException;
import org.eclipse.osee.ats.artifact.ATSAttributes;
import org.eclipse.osee.ats.artifact.DecisionReviewArtifact;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.exception.OseeCoreException;
import org.eclipse.osee.framework.ui.plugin.util.Result;

/**
 * Methods in support of programatically transitioning the Decision Review Workflow through it's states. Only to be used
 * for the DefaultReviewWorkflow of Prepare->Decision->ReWork->Complete
 * 
 * @author Donald G. Dunne
 */
public class DefaultDecisionReviewWorkflowManager {

   /**
    * Quickly transition to a state with minimal metrics and data entered. Should only be used for automated
    * transitioning for things such as developmental testing and demos.
    * 
    * @param toState
    * @param user User to transition to OR null if should use user of current state
    * @param popup
    * @return
    * @throws Exception
    */
   public static Result transitionTo(DecisionReviewArtifact reviewArt, DecisionReviewArtifact.StateNames toState, User user, boolean popup) throws OseeCoreException, SQLException {
      Result result = Result.TrueResult;
      // If in Prepare state, set data and transition to Decision
      if (reviewArt.getSmaMgr().getStateMgr().getCurrentStateName().equals(
            DecisionReviewArtifact.StateNames.Prepare.name())) {
         result = setPrepareStateData(reviewArt, 100, 3, .2);

         if (result.isFalse()) {
            if (popup) result.popup();
            return result;
         }
         result =
               reviewArt.getSmaMgr().transition(DecisionReviewArtifact.StateNames.Decision.name(),
                     (user != null ? user : reviewArt.getSmaMgr().getStateMgr().getAssignees().iterator().next()),
                     false);
      }
      if (result.isFalse()) {
         if (popup) result.popup();
         return result;
      }
      if (toState == DecisionReviewArtifact.StateNames.Decision) return Result.TrueResult;

      // If desired to transition to follow-up, then decision is false
      boolean decision = (toState != DecisionReviewArtifact.StateNames.Followup);

      result = setDecisionStateData(reviewArt, decision, 100, .2);
      if (result.isFalse()) {
         if (popup) result.popup();
         return result;
      }

      result =
            reviewArt.getSmaMgr().transition(toState.name(),
                  (user != null ? user : reviewArt.getSmaMgr().getStateMgr().getAssignees().iterator().next()), false);
      if (result.isFalse()) {
         if (popup) result.popup();
         return result;
      }
      return Result.TrueResult;
   }

   public static Result setPrepareStateData(DecisionReviewArtifact reviewArt, int statePercentComplete, double estimateHours, double stateHoursSpent) throws OseeCoreException, SQLException {
      if (!reviewArt.getSmaMgr().getStateMgr().getCurrentStateName().equals(
            DecisionReviewArtifact.StateNames.Prepare.name())) return new Result("Action not in Prepare state");
      reviewArt.setSoleAttributeValue(ATSAttributes.ESTIMATED_HOURS_ATTRIBUTE.getStoreName(), estimateHours);
      reviewArt.getSmaMgr().getStateMgr().setHoursSpent(stateHoursSpent);
      reviewArt.getSmaMgr().getStateMgr().setPercentComplete(statePercentComplete);
      return Result.TrueResult;
   }

   public static Result setDecisionStateData(DecisionReviewArtifact reviewArt, boolean decision, int statePercentComplete, double stateHoursSpent) throws OseeCoreException, SQLException {
      if (!reviewArt.getSmaMgr().getStateMgr().getCurrentStateName().equals(
            DecisionReviewArtifact.StateNames.Decision.name())) return new Result("Action not in Decision state");
      reviewArt.setSoleAttributeValue(ATSAttributes.DECISION_ATTRIBUTE.getStoreName(), decision ? "Yes" : "No");
      reviewArt.getSmaMgr().getStateMgr().setHoursSpent(stateHoursSpent);
      reviewArt.getSmaMgr().getStateMgr().setPercentComplete(statePercentComplete);
      return Result.TrueResult;
   }

}
