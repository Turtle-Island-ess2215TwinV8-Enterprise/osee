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
package org.eclipse.osee.ats.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.RuleDefinitionOption;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.client.workflow.transition.ITransitionHelper;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionHelperAdapter;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionStatusData;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionToOperation;
import org.eclipse.osee.ats.editor.stateItem.AtsStateItemManager;
import org.eclipse.osee.ats.editor.stateItem.IAtsStateItem;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.dialog.TransitionStatusDialog;
import org.eclipse.osee.ats.workdef.StateDefinitionLabelProvider;
import org.eclipse.osee.ats.workdef.StateDefinitionViewSorter;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench.MessageType;
import org.eclipse.osee.framework.ui.skynet.widgets.XComboDam;
import org.eclipse.osee.framework.ui.skynet.widgets.XComboViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.UserCheckTreeDialog;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * @author Donald G. Dunne
 */
public class WETransitionComposite extends Composite {

   private final XComboViewer transitionToStateCombo;
   private final Label transitionAssigneesLabel;
   private final AbstractWorkflowArtifact awa;
   private final SMAWorkFlowSection workflowSection;
   private final SMAEditor editor;

   public WETransitionComposite(Composite parent, SMAWorkFlowSection workflowSection, final SMAEditor editor, final boolean isEditable) throws OseeCoreException {
      super(parent, SWT.NONE);
      this.workflowSection = workflowSection;
      this.editor = editor;

      awa = workflowSection.getSma();
      setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      setLayout(new GridLayout((editor.getWorkFlowTab().isShowTargetedVersion() ? 7 : 5), false));
      setBackground(AtsUtil.ACTIVE_COLOR);

      Button transitionButton = editor.getToolkit().createButton(this, "Transition", SWT.PUSH);
      transitionButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            handleTransitionButtonSelection(editor, isEditable);
         }
      });
      transitionButton.setBackground(AtsUtil.ACTIVE_COLOR);

      Label label = editor.getToolkit().createLabel(this, "to");
      label.setBackground(AtsUtil.ACTIVE_COLOR);

      transitionToStateCombo = new XComboViewer("Transition To State Combo", SWT.NONE);
      transitionToStateCombo.setDisplayLabel(false);
      List<Object> allPages = new ArrayList<Object>();
      for (IAtsStateDefinition nextState : awa.getToStatesWithCompleteCancelReturnStates()) {
         if (!allPages.contains(nextState)) {
            allPages.add(nextState);
         }
      }
      transitionToStateCombo.setInput(allPages);
      transitionToStateCombo.setLabelProvider(new StateDefinitionLabelProvider());
      transitionToStateCombo.setContentProvider(new ArrayContentProvider());
      transitionToStateCombo.setSorter(new StateDefinitionViewSorter());

      transitionToStateCombo.createWidgets(this, 1);

      // Set default page from workflow default
      ArrayList<Object> defaultPage = new ArrayList<Object>();
      if (workflowSection.getPage().getDefaultToPage() != null) {
         defaultPage.add(workflowSection.getPage().getDefaultToPage());
         transitionToStateCombo.setSelected(defaultPage);
      }
      if (workflowSection.getPage().getStateType().isCancelledState() && Strings.isValid(awa.getCancelledFromState())) {
         defaultPage.add(awa.getStateDefinitionByName(awa.getCancelledFromState()));
         transitionToStateCombo.setSelected(defaultPage);
      }
      if (workflowSection.getPage().getStateType().isCompletedState() && Strings.isValid(awa.getCompletedFromState())) {
         defaultPage.add(awa.getStateDefinitionByName(awa.getCompletedFromState()));
         transitionToStateCombo.setSelected(defaultPage);
      }
      // Update transition based on state items
      updateTransitionToState();

      transitionToStateCombo.getCombo().setVisibleItemCount(20);
      transitionToStateCombo.addSelectionChangedListener(new ISelectionChangedListener() {
         @Override
         public void selectionChanged(SelectionChangedEvent event) {
            try {
               updateTransitionToAssignees();
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      });

      if (editor.getWorkFlowTab().isShowTargetedVersion()) {
         SMATargetedVersionHeader smaTargetedVersionHeader = new SMATargetedVersionHeader(this, SWT.NONE, awa, editor);
         smaTargetedVersionHeader.setBackground(AtsUtil.ACTIVE_COLOR);
      }

      Hyperlink assigneesLabelLink = editor.getToolkit().createHyperlink(this, "Next State Assignee(s)", SWT.NONE);
      assigneesLabelLink.addHyperlinkListener(new IHyperlinkListener() {

         @Override
         public void linkEntered(HyperlinkEvent e) {
            // do nothing
         }

         @Override
         public void linkExited(HyperlinkEvent e) {
            // do nothing
         }

         @Override
         public void linkActivated(HyperlinkEvent e) {
            try {
               handleChangeTransitionAssignees(awa);
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }

      });
      assigneesLabelLink.setBackground(AtsUtil.ACTIVE_COLOR);

      transitionAssigneesLabel =
         editor.getToolkit().createLabel(this, Strings.truncate(awa.getTransitionAssigneesStr(), 100, true));
      transitionAssigneesLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      transitionAssigneesLabel.setBackground(AtsUtil.ACTIVE_COLOR);

   }

   private void handleTransitionButtonSelection(final SMAEditor editor, final boolean isEditable) {
      editor.doSave(null);
      final List<AbstractWorkflowArtifact> awas = Arrays.asList(awa);
      final IAtsStateDefinition toStateDef = (IAtsStateDefinition) transitionToStateCombo.getSelected();
      final IAtsStateDefinition fromStateDef = awa.getStateDefinition();
      ITransitionHelper helper = new TransitionHelperAdapter() {

         @Override
         public boolean isPrivilegedEditEnabled() {
            return editor.isPrivilegedEditModeEnabled();
         }

         @Override
         public String getToStateName() {
            return toStateDef.getName();
         }

         @Override
         public Collection<? extends IAtsUser> getToAssignees(AbstractWorkflowArtifact awa) throws OseeCoreException {
            return awa.getTransitionAssignees();
         }

         @Override
         public String getName() {
            return "Workflow Editor Transition";
         }

         @Override
         public Result handleExtraHoursSpent() {
            final Result result = new Result(true, "");
            Displays.ensureInDisplayThread(new Runnable() {

               @Override
               public void run() {
                  boolean resultBool = false;
                  try {
                     resultBool = handlePopulateStateMetrics(fromStateDef, toStateDef);
                  } catch (OseeCoreException ex) {
                     OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
                     result.set(false);
                     result.setText(String.format("Error processing extra hours spent for [%s]",
                        awas.iterator().next().toStringWithId()));
                  }
                  if (!resultBool) {
                     result.setCancelled(true);
                     result.set(false);
                  }
               }
            }, true);
            return result;
         }

         @Override
         public Result getCompleteOrCancellationReason() {
            final Result result = new Result(true, "");
            Displays.ensureInDisplayThread(new Runnable() {

               @Override
               public void run() {
                  IAtsStateDefinition toStateDef =
                     getAwas().iterator().next().getStateDefinitionByName(getToStateName());
                  if (toStateDef.getStateType().isCancelledState()) {
                     EntryDialog cancelDialog = new EntryDialog("Cancellation Reason", "Enter cancellation reason.");
                     if (cancelDialog.open() != 0) {
                        result.setCancelled(true);
                     }
                     result.set(true);
                     result.setText(cancelDialog.getEntry());
                  }

               }
            }, true);
            return result;
         }

         @Override
         public Collection<AbstractWorkflowArtifact> getAwas() {
            return awas;
         }

      };
      final TransitionToOperation operation = new TransitionToOperation(helper);
      Operations.executeAsJob(operation, true, Job.SHORT, new JobChangeAdapter() {

         @Override
         public void done(IJobChangeEvent event) {
            TransitionResults results = operation.getResults();
            if (!results.isEmpty()) {
               String resultStr = results.getResultString();
               results.logExceptions();
               AWorkbench.popup(MessageType.Error, "Transition Failed", resultStr);
            }
         }

      });
   }

   private boolean handlePopulateStateMetrics(IAtsStateDefinition fromStateDefinition, IAtsStateDefinition toStateDefinition) throws OseeCoreException {
      int percent = 0;
      // If state weighting, always 100 cause state is completed
      if (AtsClientService.get().getWorkDefinitionAdmin().isStateWeightingEnabled(awa.getWorkDefinition())) {
         percent = 100;
      } else {
         if (toStateDefinition.getStateType().isCompletedOrCancelledState()) {
            percent = 100;
         } else {
            percent = awa.getSoleAttributeValue(AtsAttributeTypes.PercentComplete, 0);
         }
      }

      double hoursSpent = awa.getStateMgr().getHoursSpent(awa.getCurrentStateName());
      double additionalHours = 0.0;

      if (isRequireStateHoursSpentPrompt(fromStateDefinition) && !toStateDefinition.getStateType().isCancelledState()) {
         // Otherwise, open dialog to ask for hours complete
         String msg =
            awa.getStateMgr().getCurrentStateName() + " State\n\n" + AtsUtilCore.doubleToI18nString(hoursSpent) + " hours already spent on this state.\n" + "Enter the additional number of hours you spent on this state.";
         // Remove after ATS Resolution options is removed 0.9.9_SR5ish
         TransitionStatusData data = new TransitionStatusData(Arrays.asList(awa), false);
         TransitionStatusDialog dialog = new TransitionStatusDialog("Enter Hours Spent", msg, data);
         int result = dialog.open();
         if (result == 0) {
            additionalHours = dialog.getData().getAdditionalHours();
         } else {
            return false;
         }
      }
      awa.getStateMgr().updateMetrics(awa.getStateDefinition(), additionalHours, percent, true);

      return true;
   }

   private boolean isRequireStateHoursSpentPrompt(IAtsStateDefinition stateDefinition) {
      return stateDefinition.hasRule(RuleDefinitionOption.RequireStateHourSpentPrompt.name());
   }

   public void updateTransitionToAssignees() throws OseeCoreException {
      Collection<IAtsUser> assignees = null;
      // Determine if the is an override set of assigness
      for (IAtsStateItem item : AtsStateItemManager.getStateItems()) {
         String decisionValueIfApplicable = null;
         if (awa.isOfType(AtsArtifactTypes.DecisionReview) && workflowSection.getPage().getLayoutData(
            AtsAttributeTypes.Decision.getName()) != null) {
            XComboDam xWidget =
               (XComboDam) workflowSection.getPage().getLayoutData(AtsAttributeTypes.Decision.getName()).getXWidget();
            if (xWidget != null) {
               decisionValueIfApplicable = xWidget.get();
            }
         }
         assignees = item.getOverrideTransitionToAssignees(workflowSection.getSma(), decisionValueIfApplicable);
         if (assignees != null) {
            break;
         }
      }
      // If override set and isn't the same as already selected, update
      if (assignees != null && !awa.getTransitionAssignees().equals(assignees)) {
         awa.setTransitionAssignees(assignees);
         editor.onDirtied();
      }
      refresh();
   }

   public void updateTransitionToState() throws OseeCoreException {
      // Determine if there is a transitionToStateOverride for this page
      String transitionStateOverride = null;
      for (IAtsStateItem item : AtsStateItemManager.getStateItems()) {
         transitionStateOverride = item.getOverrideTransitionToStateName(workflowSection);
         if (transitionStateOverride != null) {
            break;
         }
      }
      if (transitionStateOverride != null) {
         // Return if override state is same as selected
         if (((IAtsStateDefinition) transitionToStateCombo.getSelected()).getName().equals(transitionStateOverride)) {
            return;
         }
         // Find page corresponding to override state name
         for (IAtsStateDefinition toState : awa.getToStates()) {
            if (toState.getName().equals(transitionStateOverride)) {
               // Reset selection
               ArrayList<Object> defaultPage = new ArrayList<Object>();
               defaultPage.add(toState);
               transitionToStateCombo.setSelected(defaultPage);
               return;
            }
         }
      }
   }

   public void refresh() throws OseeCoreException {
      if (Widgets.isAccessible(transitionAssigneesLabel)) {
         IAtsStateDefinition toWorkPage = (IAtsStateDefinition) transitionToStateCombo.getSelected();
         if (toWorkPage == null) {
            transitionAssigneesLabel.setText("");
         } else {
            transitionAssigneesLabel.setText(workflowSection.getPage().getSma().getTransitionAssigneesStr());
         }
         transitionAssigneesLabel.getParent().layout();
      }
   }

   private void handleChangeTransitionAssignees(AbstractWorkflowArtifact aba) throws OseeCoreException {
      IAtsStateDefinition toWorkPage = (IAtsStateDefinition) transitionToStateCombo.getSelected();
      if (toWorkPage == null) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "No Transition State Selected");
         return;
      }
      if (toWorkPage.getStateType().isCompletedOrCancelledState()) {
         AWorkbench.popup("ERROR", "No Assignees in Completed and Cancelled states");
         return;
      }
      UserCheckTreeDialog uld = new UserCheckTreeDialog();
      uld.setMessage("Select users to transition to.");
      uld.setInitialSelections(AtsClientService.get().getUserAdmin().getOseeUsers(aba.getTransitionAssignees()));
      if (awa.getParentTeamWorkflow() != null) {
         uld.setTeamMembers(AtsClientService.get().getUserAdmin().getOseeUsers(awa.getParentTeamWorkflow().getTeamDefinition().getMembersAndLeads()));
      }
      if (uld.open() != 0) {
         return;
      }
      Collection<IAtsUser> users = AtsClientService.get().getUserAdmin().getAtsUsers(uld.getUsersSelected());
      if (users.isEmpty()) {
         AWorkbench.popup("ERROR", "Must have at least one assignee");
         return;
      }
      awa.setTransitionAssignees(users);
      refresh();
      editor.onDirtied();
   }

   public XComboViewer getTransitionToStateCombo() {
      return transitionToStateCombo;
   }

}
