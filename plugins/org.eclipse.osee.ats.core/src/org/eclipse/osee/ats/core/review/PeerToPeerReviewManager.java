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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import org.eclipse.osee.ats.core.review.defect.ReviewDefectItem;
import org.eclipse.osee.ats.core.review.defect.ReviewDefectManager;
import org.eclipse.osee.ats.core.review.role.UserRole;
import org.eclipse.osee.ats.core.review.role.UserRoleManager;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.core.workdef.ReviewBlockType;
import org.eclipse.osee.ats.core.workflow.transition.TransitionHelper;
import org.eclipse.osee.ats.core.workflow.transition.TransitionManager;
import org.eclipse.osee.ats.core.workflow.transition.TransitionOption;
import org.eclipse.osee.ats.core.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.ats.shared.AtsAttributeTypes;
import org.eclipse.osee.ats.shared.AtsRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.core.util.IWorkPage;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.core.util.WorkPageType;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * Methods in support of programatically transitioning the Peer Review Workflow through it's states. Only to be used for
 * the DefaultReviewWorkflow of Prepare->Review->Complete
 * 
 * @author Donald G. Dunne
 */
public class PeerToPeerReviewManager {

   public static String getDefaultReviewTitle(TeamWorkFlowArtifact teamArt) {
      return "Review \"" + teamArt.getArtifactTypeName() + "\" titled \"" + teamArt.getName() + "\"";
   }

   protected PeerToPeerReviewManager() {
      // private constructor
   }

   /**
    * Quickly transition to a state with minimal metrics and data entered. Should only be used for automated transition
    * for things such as developmental testing and demos.
    * 
    * @param user User to transition to OR null if should use user of current state
    */
   public static Result transitionTo(PeerToPeerReviewArtifact reviewArt, PeerToPeerReviewState toState, Collection<UserRole> roles, Collection<ReviewDefectItem> defects, User user, boolean popup, SkynetTransaction transaction) throws OseeCoreException {
      Result result = setPrepareStateData(popup, reviewArt, roles, "DoThis.java", 100, .2, transaction);
      if (result.isFalse()) {
         return result;
      }
      result =
         transitionToState(PeerToPeerReviewState.Review.getWorkPageType(), popup, reviewArt,
            PeerToPeerReviewState.Review, transaction);
      if (result.isFalse()) {
         return result;
      }
      if (toState == PeerToPeerReviewState.Review) {
         return Result.TrueResult;
      }

      result = setReviewStateData(reviewArt, roles, defects, 100, .2, transaction);
      if (result.isFalse()) {
         return result;
      }

      result =
         transitionToState(PeerToPeerReviewState.Completed.getWorkPageType(), popup, reviewArt,
            PeerToPeerReviewState.Completed, transaction);
      if (result.isFalse()) {
         return result;
      }
      return Result.TrueResult;
   }

   private static Result transitionToState(WorkPageType workPageType, boolean popup, PeerToPeerReviewArtifact reviewArt, IWorkPage toState, SkynetTransaction transaction) throws OseeCoreException {
      TransitionHelper helper =
         new TransitionHelper("Transition to " + toState.getPageName(), Arrays.asList(reviewArt),
            toState.getPageName(),
            Arrays.asList((IBasicUser) reviewArt.getStateMgr().getAssignees().iterator().next()), null,
            TransitionOption.OverrideAssigneeCheck);
      TransitionManager transitionMgr = new TransitionManager(helper, transaction);
      TransitionResults results = transitionMgr.handleAll();
      if (results.isEmpty()) {
         return Result.TrueResult;
      }
      return new Result("Error transitioning [%s]", results);
   }

   public static Result setPrepareStateData(boolean popup, PeerToPeerReviewArtifact reviewArt, Collection<UserRole> roles, String reviewMaterials, int statePercentComplete, double stateHoursSpent, SkynetTransaction transaction) throws OseeCoreException {
      if (!reviewArt.isInState(PeerToPeerReviewState.Prepare)) {
         Result result = new Result("Action not in Prepare state");
         if (result.isFalse() && popup) {
            return result;
         }

      }
      if (roles != null) {
         UserRoleManager roleMgr = new UserRoleManager(reviewArt);
         for (UserRole role : roles) {
            roleMgr.addOrUpdateUserRole(role);
         }
         roleMgr.saveToArtifact(transaction);
      }
      reviewArt.setSoleAttributeValue(AtsAttributeTypes.Location, reviewMaterials);
      reviewArt.getStateMgr().updateMetrics(stateHoursSpent, statePercentComplete, true);
      return Result.TrueResult;
   }

   public static Result setReviewStateData(PeerToPeerReviewArtifact reviewArt, Collection<UserRole> roles, Collection<ReviewDefectItem> defects, int statePercentComplete, double stateHoursSpent, SkynetTransaction transaction) throws OseeCoreException {
      if (roles != null) {
         UserRoleManager roleMgr = new UserRoleManager(reviewArt);
         for (UserRole role : roles) {
            roleMgr.addOrUpdateUserRole(role);
         }
         roleMgr.saveToArtifact(transaction);
      }
      if (defects != null) {
         ReviewDefectManager defectManager = new ReviewDefectManager(reviewArt);
         for (ReviewDefectItem defect : defects) {
            defectManager.addOrUpdateDefectItem(defect);
         }
         defectManager.saveToArtifact(reviewArt);
      }
      reviewArt.getStateMgr().updateMetrics(stateHoursSpent, statePercentComplete, true);
      return Result.TrueResult;
   }

   public static PeerToPeerReviewArtifact createNewPeerToPeerReview(TeamWorkFlowArtifact teamArt, String reviewTitle, String againstState, SkynetTransaction transaction) throws OseeCoreException {
      return createNewPeerToPeerReview(teamArt, reviewTitle, againstState, new Date(), UserManager.getUser(),
         transaction);
   }

   public static PeerToPeerReviewArtifact createNewPeerToPeerReview(TeamWorkFlowArtifact teamArt, String reviewTitle, String againstState, Date createdDate, User createdBy, SkynetTransaction transaction) throws OseeCoreException {
      PeerToPeerReviewArtifact peerToPeerRev =
         (PeerToPeerReviewArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.PeerToPeerReview,
            AtsUtilCore.getAtsBranch(), reviewTitle == null ? "Peer to Peer Review" : reviewTitle);
      // Initialize state machine
      peerToPeerRev.initializeNewStateMachine(null, new Date(), createdBy);

      if (teamArt != null) {
         teamArt.addRelation(AtsRelationTypes.TeamWorkflowToReview_Review, peerToPeerRev);
         if (againstState != null) {
            peerToPeerRev.setSoleAttributeValue(AtsAttributeTypes.RelatedToState, againstState);
         }
      }
      peerToPeerRev.setSoleAttributeValue(AtsAttributeTypes.ReviewBlocks, ReviewBlockType.None.name());
      peerToPeerRev.setSoleAttributeValue(AtsAttributeTypes.ReviewFormalType, ReviewFormalType.InFormal.name());
      if (transaction != null) {
         peerToPeerRev.persist(transaction);
      }
      return peerToPeerRev;
   }

   public static boolean isStandAlongReview(Object object) throws OseeCoreException {
      if (object instanceof PeerToPeerReviewArtifact) {
         PeerToPeerReviewArtifact peerArt = (PeerToPeerReviewArtifact) object;
         return peerArt.isStandAloneReview();
      }
      return false;
   }
}
