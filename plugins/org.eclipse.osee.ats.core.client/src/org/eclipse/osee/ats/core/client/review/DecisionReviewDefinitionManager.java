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
package org.eclipse.osee.ats.core.client.review;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.IAtsDecisionReviewDefinition;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.api.workdef.StateEventType;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.client.workflow.log.LogType;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionAdapter;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;

/**
 * Create DecisionReview from transition if defined by StateDefinition.
 * 
 * @author Donald G. Dunne
 */
public class DecisionReviewDefinitionManager extends TransitionAdapter {

   /**
    * Creates decision review if one of same name doesn't already exist
    */
   public static DecisionReviewArtifact createNewDecisionReview(IAtsDecisionReviewDefinition revDef, SkynetTransaction transaction, TeamWorkFlowArtifact teamArt, Date createdDate, IAtsUser createdBy) throws OseeCoreException {
      if (Artifacts.getNames(ReviewManager.getReviews(teamArt)).contains(revDef.getReviewTitle())) {
         // Already created this review
         return null;
      }
      // Add current user if no valid users specified
      List<IAtsUser> users = new LinkedList<IAtsUser>();
      users.addAll(AtsClientService.get().getUserAdmin().getUsersByUserIds(revDef.getAssignees()));
      if (users.isEmpty()) {
         users.add(AtsClientService.get().getUserAdmin().getCurrentUser());
      }
      if (!Strings.isValid(revDef.getReviewTitle())) {
         throw new OseeStateException("ReviewDefinition must specify title for Team Workflow [%s] WorkDefinition [%s]",
            teamArt.toStringWithId(), teamArt.getWorkDefinition());
      }
      DecisionReviewArtifact decArt = null;
      if (revDef.isAutoTransitionToDecision()) {
         decArt =
            DecisionReviewManager.createNewDecisionReviewAndTransitionToDecision(teamArt, revDef.getReviewTitle(),
               revDef.getDescription(), revDef.getRelatedToState(), revDef.getBlockingType(), revDef.getOptions(),
               users, createdDate, createdBy, transaction);
      } else {
         decArt =
            DecisionReviewManager.createNewDecisionReview(teamArt, revDef.getBlockingType(), revDef.getReviewTitle(),
               revDef.getRelatedToState(), revDef.getDescription(), revDef.getOptions(), users, createdDate, createdBy);
      }
      decArt.getLog().addLog(LogType.Note, null, String.format("Review [%s] auto-generated", revDef.getName()));
      for (IReviewProvider provider : ReviewProviders.getAtsReviewProviders()) {
         provider.reviewCreated(decArt);
      }
      decArt.persist(transaction);
      return decArt;
   }

   @Override
   public void transitioned(AbstractWorkflowArtifact sma, IStateToken fromState, IStateToken toState, Collection<? extends IAtsUser> toAssignees, SkynetTransaction transaction) throws OseeCoreException {
      // Create any decision or peerToPeer reviews for transitionTo and transitionFrom
      if (!sma.isTeamWorkflow()) {
         return;
      }
      Date createdDate = new Date();
      IAtsUser createdBy = AtsCoreUsers.SYSTEM_USER;
      TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) sma;

      for (IAtsDecisionReviewDefinition decRevDef : teamArt.getStateDefinition().getDecisionReviews()) {
         if (decRevDef.getStateEventType() != null && decRevDef.getStateEventType().equals(StateEventType.TransitionTo)) {
            DecisionReviewArtifact decArt =
               DecisionReviewDefinitionManager.createNewDecisionReview(decRevDef, transaction, teamArt, createdDate,
                  createdBy);
            if (decArt != null) {
               decArt.persist(transaction);
            }
         }
      }
   }
}
