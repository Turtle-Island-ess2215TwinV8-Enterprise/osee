/**
 * @author Donald G. Dunne
 */
package org.eclipse.osee.ats.core.workflow.transition;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;
import org.eclipse.osee.ats.core.internal.Activator;
import org.eclipse.osee.ats.core.review.AbstractReviewArtifact;
import org.eclipse.osee.ats.core.review.ReviewManager;
import org.eclipse.osee.ats.core.review.ValidateReviewManager;
import org.eclipse.osee.ats.core.task.AbstractTaskableArtifact;
import org.eclipse.osee.ats.core.task.TaskArtifact;
import org.eclipse.osee.ats.core.team.TeamState;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.type.AtsArtifactTypes;
import org.eclipse.osee.ats.core.type.AtsAttributeTypes;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.core.util.WorkflowManagerCore;
import org.eclipse.osee.ats.core.validator.AtsXWidgetValidateManager;
import org.eclipse.osee.ats.core.validator.WidgetResult;
import org.eclipse.osee.ats.core.workdef.ReviewBlockType;
import org.eclipse.osee.ats.core.workdef.RuleDefinitionOption;
import org.eclipse.osee.ats.core.workdef.StateDefinition;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.workflow.log.LogType;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.core.util.IWorkPage;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

public class TransitionManager {

   private final ITransitionHelper helper;
   private String completedCancellationReason = null;
   private SkynetTransaction transaction;

   public TransitionManager(ITransitionHelper helper) {
      this(helper, null);
   }

   public TransitionManager(ITransitionHelper helper, SkynetTransaction transaction) {
      this.helper = helper;
      this.transaction = transaction;
   }

   public TransitionResults handleAll() {
      TransitionResults results = new TransitionResults();
      handleTransitionPrep(results);
      if (results.isCancelled() || !results.isEmpty()) {
         return results;
      }
      handleTransitionValidation(results);
      if (results.isCancelled() || !results.isEmpty()) {
         return results;
      }
      handleTransitionUi(results);
      if (results.isCancelled() || !results.isEmpty()) {
         return results;
      }
      try {
         for (AbstractWorkflowArtifact awa : helper.getAwas()) {
            awa.setInTransition(true);
         }
         handleTransition(results);
      } finally {
         for (AbstractWorkflowArtifact awa : helper.getAwas()) {
            awa.setInTransition(false);
         }
      }
      return results;
   }

   /**
    * Prepare AbstractWorkflowArtifact for transition including setting users, un-setting system users, saving, etc.
    * 
    * @return Result.isFalse if failure
    */
   public void handleTransitionPrep(TransitionResults results) {
      // Persist must be done prior and separate from transition
      try {
         SkynetTransaction transaction = new SkynetTransaction(AtsUtilCore.getAtsBranch(), "Transition Preparation");
         for (AbstractWorkflowArtifact awa : helper.getAwas()) {
            awa.persist();
         }
         transaction.execute();
      } catch (OseeCoreException ex) {
         results.addResult(new TransitionResult(String.format("Exception while preparing for transition [%s]",
            helper.getName()), ex));
      }
   }

   /**
    * Validate AbstractWorkflowArtifact for transition including checking widget validation, rules, assignment, etc.
    * 
    * @return Result.isFalse if failure
    */
   public void handleTransitionValidation(TransitionResults results) {
      try {
         if (helper.getAwas().isEmpty()) {
            results.addResult(TransitionResult.NO_WORKFLOWS_PROVIDED_FOR_TRANSITION);
            return;
         }
         if (helper.getToStateName() == null) {
            results.addResult(TransitionResult.TO_STATE_CANT_BE_NULL);
            return;
         }
         if (helper.isSystemUser()) {
            results.addResult(TransitionResult.CAN_NOT_TRANSITION_AS_SYSTEM_USER);
            return;
         }
      } catch (OseeCoreException ex) {
         results.addResult(new TransitionResult(String.format("Exception while validating transition [%s]",
            helper.getName()), ex));
      }
      for (AbstractWorkflowArtifact awa : helper.getAwas()) {
         try {
            // Validate toState valid
            StateDefinition fromStateDef = awa.getStateDefinition();
            StateDefinition toStateDef = awa.getStateDefinitionByName(helper.getToStateName());
            if (toStateDef == null) {
               results.addResult(
                  awa,
                  new TransitionResult(String.format(
                     "Transition-To State [%s] does not exist for Work Definition [%s]", helper.getToStateName(),
                     awa.getWorkDefinition().getName())));
               continue;
            }

            // Validate transition from fromState and toState
            if (!helper.isOverrideTransitionValidityCheck() && !fromStateDef.getToStates().contains(toStateDef) && !fromStateDef.isCompletedOrCancelledPage()) {
               String errStr =
                  String.format("Work Definition [%s] is not configured to transition from \"[%s]\" to \"[%s]\"",
                     toStateDef.getName(), fromStateDef.getPageName(), toStateDef.getPageName());
               OseeLog.log(Activator.class, Level.SEVERE, errStr);
               results.addResult(awa, new TransitionResult(errStr));
               continue;
            }

            // Validate Editable
            if (!WorkflowManagerCore.isEditable(awa, awa.getStateDefinition(), helper.isPriviledgedEditEnabled()) //
               && !awa.getStateMgr().getAssignees().contains(UserManager.getUser(SystemUser.UnAssigned)) //
               && !helper.isOverrideAssigneeCheck()) {
               results.addResult(awa, TransitionResult.MUST_BE_ASSIGNED);
               continue;
            }

            // Validate Working Branch
            isWorkingBranchTransitionable(results, awa, toStateDef);
            if (results.isCancelled()) {
               continue;
            }

            // Validate Assignees (UnAssigned ok cause will be resolve to current user upon transition
            if (!helper.isOverrideAssigneeCheck() && !toStateDef.isCancelledPage() && helper.isSystemUserAssingee(awa)) {
               results.addResult(awa, TransitionResult.CAN_NOT_TRANSITION_WITH_SYSTEM_USER_ASSIGNED);
               continue;
            }

            // Validate state, widgets, rules unless OverrideAttributeValidation is set or transitioning to cancel
            isStateTransitionable(results, awa, toStateDef);
            if (results.isCancelled()) {
               continue;
            }

            // Validate transition with extensions
            isTransitionValidForExtensions(results, awa, fromStateDef, toStateDef);
            if (results.isCancelled()) {
               continue;
            }

         } catch (OseeCoreException ex) {
            results.addResult(awa,
               new TransitionResult(String.format("Exception while validating transition [%s]", helper.getName()), ex));
         }
      }
   }

   public void isTransitionValidForExtensions(TransitionResults results, AbstractWorkflowArtifact awa, StateDefinition fromStateDef, StateDefinition toStateDef) {
      // Check extension points for valid transition
      for (ITransitionListener listener : TransitionListeners.getListeners()) {
         try {
            listener.transitioning(results, awa, fromStateDef, toStateDef, helper.getToAssignees());
            if (results.isCancelled() || !results.isEmpty()) {
               continue;
            }
         } catch (OseeCoreException ex) {
            results.addResult(
               awa,
               new TransitionResult(String.format("Exception [%s] while validating transition extensions 1 [%s]",
                  ex.getMessage(), helper.getName()), ex));
         }

      }

      // Check again in case first check made changes that would now keep transition from happening
      if (results.isEmpty()) {
         for (ITransitionListener listener : TransitionListeners.getListeners()) {
            try {
               listener.transitioning(results, awa, fromStateDef, toStateDef, helper.getToAssignees());
               if (results.isCancelled() || !results.isEmpty()) {
                  continue;
               }
            } catch (OseeCoreException ex) {
               results.addResult(
                  awa,
                  new TransitionResult(String.format("Exception [%s] while validating transition extensions 2 [%s]",
                     ex.getMessage(), helper.getName()), ex));
            }

         }
      }
   }

   /**
    * Request extra information if transition requires hours spent prompt, cancellation reason, etc.
    * 
    * @return Result.isFalse if failure or Result.isCancelled if canceled
    */
   public void handleTransitionUi(TransitionResults results) {
      Result result = helper.getCompleteOrCancellationReason();
      if (result.isCancelled()) {
         results.setCancelled(true);
         return;
      }
      if (result.isTrue()) {
         completedCancellationReason = result.getText();
      }
      result = helper.handleExtraHoursSpent();
      if (result.isCancelled()) {
         results.setCancelled(true);
      } else if (result.isFalse()) {
         results.addResult(new TransitionResult(result.getText()));
      }
   }

   /**
    * Process transition and persist changes to given skynet transaction
    * 
    * @return Result.isFalse if failure
    */
   public void handleTransition(TransitionResults results) {
      try {
         if (transaction == null) {
            transaction = new SkynetTransaction(AtsUtilCore.getAtsBranch(), helper.getName());
         }
         for (AbstractWorkflowArtifact awa : helper.getAwas()) {
            try {
               StateDefinition fromState = awa.getStateDefinition();
               StateDefinition toState = awa.getStateDefinitionByName(helper.getToStateName());

               Date transitionDate = new Date();
               User transitionUser = UserManager.getUser();
               // Log transition
               if (toState.isCancelledPage()) {
                  logWorkflowCancelledEvent(awa, awa.getStateMgr().getCurrentStateName(), completedCancellationReason,
                     transitionDate, transitionUser);
               } else if (toState.isCompletedPage()) {
                  logWorkflowCompletedEvent(awa, awa.getStateMgr().getCurrentStateName(), completedCancellationReason,
                     transitionDate, transitionUser);
               } else {
                  logStateCompletedEvent(awa, awa.getStateMgr().getCurrentStateName(), completedCancellationReason,
                     transitionDate, transitionUser);
               }
               if (fromState.isCancelledPage()) {
                  logWorkflowUnCancelledEvent(awa);
               } else if (fromState.isCompletedPage()) {
                  logWorkflowUnCompletedEvent(awa);
               }
               logStateStartedEvent(awa, toState, transitionDate, transitionUser);

               // Get transition to assignees
               Collection<IBasicUser> toAssignees = new HashSet<IBasicUser>();
               if (!toState.isCompletedOrCancelledPage()) {
                  if (helper.getToAssignees() != null) {
                     toAssignees.addAll(helper.getToAssignees());
                  }
                  if (toAssignees.contains(UserManager.getUser(SystemUser.UnAssigned))) {
                     toAssignees.remove(UserManager.getUser(SystemUser.UnAssigned));
                     toAssignees.add(UserManager.getUser());
                  }
                  if (toAssignees.isEmpty()) {
                     toAssignees.add(UserManager.getUser());
                  }
               }

               awa.getStateMgr().transitionHelper(toAssignees, fromState, toState, completedCancellationReason);

               if (awa.isValidationRequired() && awa.isTeamWorkflow()) {
                  ValidateReviewManager.createValidateReview((TeamWorkFlowArtifact) awa, false, transitionDate,
                     transitionUser, transaction);
               }

               // Persist
               awa.persist(transaction);

               awa.transitioned(fromState, toState, helper.getToAssignees(), transaction);

               // Notify extension points of transition
               for (ITransitionListener listener : TransitionListeners.getListeners()) {
                  listener.transitioned(awa, fromState, toState, helper.getToAssignees(), transaction);
               }
            } catch (Exception ex) {
               results.addResult(awa,
                  new TransitionResult(String.format("Exception while transitioning [%s]", helper.getName()), ex));
            }
         }
      } catch (Exception ex) {
         results.addResult(new TransitionResult(String.format("Exception while transitioning [%s]", helper.getName()),
            ex));
      }

   }

   private void isWorkingBranchTransitionable(TransitionResults results, AbstractWorkflowArtifact awa, StateDefinition toStateDef) throws OseeCoreException {
      if (awa.isTeamWorkflow() && helper.isWorkingBranchInWork((TeamWorkFlowArtifact) awa)) {
         if (toStateDef.getPageName().equals(TeamState.Cancelled.getPageName())) {
            results.addResult(awa, TransitionResult.DELETE_WORKING_BRANCH_BEFORE_CANCEL);
         }
         if (helper.isBranchInCommit((TeamWorkFlowArtifact) awa)) {
            results.addResult(awa, TransitionResult.WORKING_BRANCH_BEING_COMMITTED);
         }
         if (!toStateDef.hasRule(RuleDefinitionOption.AllowTransitionWithWorkingBranch)) {
            results.addResult(awa, TransitionResult.WORKING_BRANCH_EXISTS);
         }
      }
   }

   private void isStateTransitionable(TransitionResults results, AbstractWorkflowArtifact awa, StateDefinition toStateDef) throws OseeCoreException {
      boolean isOverrideAttributeValidationState =
         helper.isOverrideTransitionValidityCheck() || awa.getStateDefinition().getOverrideAttributeValidationStates().contains(
            toStateDef);
      if (!toStateDef.isCancelledPage() && !isOverrideAttributeValidationState) {

         // Validate XWidgets for transition
         Collection<WidgetResult> widgetResults =
            AtsXWidgetValidateManager.instance.validateTransition(awa, toStateDef);
         for (WidgetResult widgetResult : widgetResults) {
            if (!widgetResult.isValid()) {
               results.addResult(awa, widgetResult);
            }
         }

         // Loop through this state's tasks to confirm complete
         if (awa instanceof AbstractTaskableArtifact && !awa.isCompletedOrCancelled()) {
            for (TaskArtifact taskArt : ((AbstractTaskableArtifact) awa).getTaskArtifactsFromCurrentState()) {
               if (taskArt.isInWork()) {
                  results.addResult(awa, TransitionResult.TASKS_NOT_COMPLETED);
                  continue;
               }
            }
         }

         // Don't transition without targeted version if so configured
         boolean teamDefRequiresTargetedVersion = awa.teamDefHasRule(RuleDefinitionOption.RequireTargetedVersion);
         boolean pageRequiresTargetedVersion =
            awa.getStateDefinition().hasRule(RuleDefinitionOption.RequireTargetedVersion);

         // Only check this if TeamWorkflow, not for reviews
         if (awa.isOfType(AtsArtifactTypes.TeamWorkflow) && (teamDefRequiresTargetedVersion || pageRequiresTargetedVersion) && //
         awa.getTargetedVersion() == null && //
         !toStateDef.isCancelledPage()) {
            results.addResult(awa, TransitionResult.MUST_BE_TARGETED_FOR_VERSION);
         }

         // Loop through this state's blocking reviews to confirm complete
         if (awa.isTeamWorkflow()) {
            for (AbstractReviewArtifact reviewArt : ReviewManager.getReviewsFromCurrentState((TeamWorkFlowArtifact) awa)) {
               if (reviewArt.getReviewBlockType() == ReviewBlockType.Transition && !reviewArt.isCompletedOrCancelled()) {
                  results.addResult(awa, TransitionResult.COMPLETE_BLOCKING_REVIEWS);
               }
            }
         }
      }
   }

   public static void logWorkflowCancelledEvent(AbstractWorkflowArtifact awa, String fromStateName, String reason, Date cancelDate, User cancelBy) throws OseeCoreException {
      awa.getLog().addLog(LogType.StateCancelled, fromStateName, reason, cancelDate, cancelBy);
      if (awa.isAttributeTypeValid(AtsAttributeTypes.CreatedBy)) {
         awa.setSoleAttributeValue(AtsAttributeTypes.CancelledBy, cancelBy.getUserId());
         awa.setSoleAttributeValue(AtsAttributeTypes.CancelledDate, cancelDate);
         awa.setSoleAttributeValue(AtsAttributeTypes.CancelledReason, reason);
         awa.setSoleAttributeValue(AtsAttributeTypes.CancelledFromState, fromStateName);
      }
   }

   private void logWorkflowUnCancelledEvent(AbstractWorkflowArtifact awa) throws OseeCoreException {
      if (awa.isAttributeTypeValid(AtsAttributeTypes.CreatedBy)) {
         awa.deleteSoleAttribute(AtsAttributeTypes.CancelledBy);
         awa.deleteSoleAttribute(AtsAttributeTypes.CancelledDate);
         awa.deleteSoleAttribute(AtsAttributeTypes.CancelledReason);
         awa.deleteSoleAttribute(AtsAttributeTypes.CancelledFromState);
      }
   }

   private void logWorkflowCompletedEvent(AbstractWorkflowArtifact awa, String fromStateName, String reason, Date cancelDate, User cancelBy) throws OseeCoreException {
      awa.getLog().addLog(LogType.StateComplete, fromStateName, Strings.isValid(reason) ? reason : "", cancelDate,
         cancelBy);
      if (awa.isAttributeTypeValid(AtsAttributeTypes.CreatedBy)) {
         awa.setSoleAttributeValue(AtsAttributeTypes.CompletedBy, cancelBy.getUserId());
         awa.setSoleAttributeValue(AtsAttributeTypes.CompletedDate, cancelDate);
         awa.setSoleAttributeValue(AtsAttributeTypes.CompletedFromState, fromStateName);
      }
   }

   private void logWorkflowUnCompletedEvent(AbstractWorkflowArtifact awa) throws OseeCoreException {
      if (awa.isAttributeTypeValid(AtsAttributeTypes.CreatedBy)) {
         awa.deleteSoleAttribute(AtsAttributeTypes.CompletedBy);
         awa.deleteSoleAttribute(AtsAttributeTypes.CompletedDate);
         awa.deleteSoleAttribute(AtsAttributeTypes.CompletedFromState);
      }
   }

   private void logStateCompletedEvent(AbstractWorkflowArtifact awa, String fromStateName, String reason, Date date, IBasicUser user) throws OseeCoreException {
      awa.getLog().addLog(LogType.StateComplete, fromStateName, Strings.isValid(reason) ? reason : "");
   }

   public static void logStateStartedEvent(AbstractWorkflowArtifact awa, IWorkPage state, Date date, IBasicUser user) throws OseeCoreException {
      awa.getLog().addLog(LogType.StateEntered, state.getPageName(), "");
   }

   public SkynetTransaction getTransaction() {
      return transaction;
   }

   /**
    * Add listener for notification. This is not the recommended method of listening, use
    * org.eclipse.osee.ats.core.AtsTransitionListener extension point.
    */
   public static void addListener(ITransitionListener listener) {
      TransitionListeners.addListener(listener);
   }

   public static void removeListener(ITransitionListener listener) {
      TransitionListeners.removeListener(listener);
   }

}