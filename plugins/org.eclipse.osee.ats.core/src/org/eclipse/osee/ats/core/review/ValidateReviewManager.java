/*
 * Created on Jun 7, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.review;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;
import org.eclipse.osee.ats.core.internal.Activator;
import org.eclipse.osee.ats.core.team.TeamState;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.type.AtsAttributeTypes;
import org.eclipse.osee.ats.core.workdef.ReviewBlockType;
import org.eclipse.osee.ats.core.workdef.RuleDefinitionOption;
import org.eclipse.osee.ats.core.workdef.StateDefinition;
import org.eclipse.osee.ats.core.workflow.transition.TransitionHelper;
import org.eclipse.osee.ats.core.workflow.transition.TransitionManager;
import org.eclipse.osee.ats.core.workflow.transition.TransitionOption;
import org.eclipse.osee.ats.core.workflow.transition.TransitionResults;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.utility.UsersByIds;

/**
 * Convenience methods used to create a validation decision review if so selected on the new action wizard
 * 
 * @author Donald G. Dunne
 */
public class ValidateReviewManager {

   private final static String VALIDATE_REVIEW_TITLE = "Is the resolution of this Action valid?";

   public static boolean isValidatePage(StateDefinition stateDefinition) {
      if (stateDefinition.hasRule(RuleDefinitionOption.AddDecisionValidateBlockingReview)) {
         return true;
      }
      if (stateDefinition.hasRule(RuleDefinitionOption.AddDecisionValidateNonBlockingReview)) {
         return true;
      }
      return false;
   }

   /**
    * Create a new decision review configured and transitioned to handle action validation
    * 
    * @param force will force the creation of the review without checking that a review should be created
    */
   public static DecisionReviewArtifact createValidateReview(TeamWorkFlowArtifact teamArt, boolean force, Date createdDate, User createdBy, SkynetTransaction transaction) throws OseeCoreException {
      // If not validate page, don't do anything
      if (!force && !isValidatePage(teamArt.getStateDefinition())) {
         return null;
      }
      // If validate review already created for this state, return
      if (!force && ReviewManager.getReviewsFromCurrentState(teamArt).size() > 0) {
         for (AbstractReviewArtifact rev : ReviewManager.getReviewsFromCurrentState(teamArt)) {
            if (rev.getName().equals(VALIDATE_REVIEW_TITLE)) {
               return null;
            }
         }
      }
      // Create validate review
      try {

         DecisionReviewArtifact decRev =
            DecisionReviewManager.createNewDecisionReview(
               teamArt,
               isValidateReviewBlocking(teamArt.getStateDefinition()) ? ReviewBlockType.Transition : ReviewBlockType.None,
               true, createdDate, createdBy);
         decRev.setName(VALIDATE_REVIEW_TITLE);
         decRev.setSoleAttributeValue(AtsAttributeTypes.DecisionReviewOptions,
            "No;Followup;" + getValidateReviewFollowupUsersStr(teamArt) + "\n" + "Yes;Completed;");

         TransitionHelper helper =
            new TransitionHelper("Transition to Decision", Arrays.asList(decRev),
               DecisionReviewState.Decision.getPageName(), Arrays.asList(teamArt.getCreatedBy()), null,
               TransitionOption.None);
         TransitionManager transitionMgr = new TransitionManager(helper, transaction);
         TransitionResults results = transitionMgr.handleAll();
         if (!results.isEmpty()) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, null,
               "Error transitioning Decision review [%s] to Decision %s", decRev.toStringWithId(), results.toString());
         }

         return decRev;

      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return null;
   }

   public static boolean isValidateReviewBlocking(StateDefinition stateDefinition) {
      return stateDefinition.hasRule(RuleDefinitionOption.AddDecisionValidateBlockingReview);
   }

   public static String getValidateReviewFollowupUsersStr(TeamWorkFlowArtifact teamArt) {
      try {
         return UsersByIds.getStorageString(getValidateReviewFollowupUsers(teamArt));
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return ex.getLocalizedMessage();
      }
   }

   public static Collection<IBasicUser> getValidateReviewFollowupUsers(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      Collection<IBasicUser> users = new HashSet<IBasicUser>();
      users.addAll(teamArt.getStateMgr().getAssignees(TeamState.Implement));
      if (users.size() > 0) {
         return users;
      }

      // Else if Team Workflow , return it to the leads of this team
      users.addAll(teamArt.getTeamDefinition().getLeads());
      return users;
   }

}