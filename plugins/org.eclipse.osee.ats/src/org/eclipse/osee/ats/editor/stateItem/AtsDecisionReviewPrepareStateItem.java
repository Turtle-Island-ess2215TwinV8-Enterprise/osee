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
package org.eclipse.osee.ats.editor.stateItem;

import java.util.Collection;
import org.eclipse.osee.ats.artifact.DecisionReviewArtifact;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.ats.util.widgets.XDecisionOptions;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.ui.plugin.util.Result;

/**
 * @author Donald G. Dunne
 */
public class AtsDecisionReviewPrepareStateItem extends AtsStateItem {

   @Override
   public String getId() {
      return "osee.ats.decisionReview.Prepare";
   }

   @Override
   public Result transitioning(StateMachineArtifact sma, String fromState, String toState, Collection<User> toAssignees) throws OseeCoreException {
      if (fromState.equals(DecisionReviewArtifact.DecisionReviewState.Prepare.name()) && toState.equals(DecisionReviewArtifact.DecisionReviewState.Decision.name())) {
         XDecisionOptions decOptions = new XDecisionOptions(sma);
         return decOptions.validateDecisionOptions();
      }
      return Result.TrueResult;
   }

   @Override
   public String getDescription() {
      return "AtsDecisionReviewPrepareStateItem - Add validation of decision options prior to transitioning.";
   }

}
