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
package org.eclipse.osee.ats.editor;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionStatusData;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.dialog.TransitionStatusDialog;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;

/**
 * @author Donald G. Dunne
 */
public class SMAPromptChangeStatus {

   private final Collection<? extends AbstractWorkflowArtifact> awas;

   public SMAPromptChangeStatus(AbstractWorkflowArtifact sma) {
      this(Arrays.asList(sma));
   }

   public SMAPromptChangeStatus(final Collection<? extends AbstractWorkflowArtifact> awas) {
      this.awas = awas;
   }

   public static boolean promptChangeStatus(Collection<? extends AbstractWorkflowArtifact> awas, boolean persist) throws OseeCoreException {
      SMAPromptChangeStatus promptChangeStatus = new SMAPromptChangeStatus(awas);
      return promptChangeStatus.promptChangeStatus(persist).isTrue();
   }

   public static Result isValidToChangeStatus(Collection<? extends AbstractWorkflowArtifact> awas) throws OseeCoreException {
      // Don't allow statusing for any canceled tasks
      for (AbstractWorkflowArtifact awa : awas) {
         if (awa.isCancelled()) {
            String error =
               "Can not status a cancelled " + awa.getArtifactTypeName() + ".\n\nTransition out of cancelled first.";
            return new Result(error);
         }

         // If task status is being changed, make sure tasks belong to current state
         if (awa.isOfType(AtsArtifactTypes.Task)) {
            TaskArtifact taskArt = (TaskArtifact) awa;
            if (!taskArt.isRelatedToParentWorkflowCurrentState()) {
               return new Result(
                  String.format(
                     "Task work must be done in \"Related to State\" of parent workflow for Task titled: \"%s\".\n\n" +
                     //
                     "Task work configured to be done in parent's \"%s\" state.\nParent workflow is currently in \"%s\" state.\n\n" +
                     //
                     "Either transition parent workflow or change Task's \"Related to State\" to perform task work.",
                     taskArt.getName(),
                     taskArt.getSoleAttributeValueAsString(AtsAttributeTypes.RelatedToState, "unknown"),
                     taskArt.getParentAWA().getStateMgr().getCurrentStateName()));
            }
         }

      }
      return Result.TrueResult;
   }

   public Result promptChangeStatus(boolean persist) throws OseeCoreException {
      Result result = isValidToChangeStatus(awas);
      if (result.isFalse()) {
         AWorkbench.popup(result);
         return result;
      }

      TransitionStatusData data = new TransitionStatusData(awas, true);
      TransitionStatusDialog dialog =
         new TransitionStatusDialog("Enter Hours Spent",
            "Enter percent complete and number of hours you spent since last status.", data);
      if (dialog.open() == 0) {
         performChangeStatus(awas, null, data.getAdditionalHours(), data.getPercent(), data.isSplitHoursBetweenItems(),
            persist);
         return Result.TrueResult;
      }
      return Result.FalseResult;
   }

   public static void performChangeStatus(Collection<? extends AbstractWorkflowArtifact> awas, String selectedOption, double hours, int percent, boolean splitHours, boolean persist) throws OseeCoreException {
      if (splitHours) {
         hours = hours / awas.size();
      }
      SkynetTransaction transaction = null;
      if (persist) {
         transaction = TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "ATS Prompt Change Status");
      }
      for (AbstractWorkflowArtifact awa : awas) {
         if (awa.getStateMgr().isUnAssigned()) {
            awa.getStateMgr().removeAssignee(AtsCoreUsers.UNASSIGNED_USER);
            awa.getStateMgr().addAssignee(AtsClientService.get().getUserAdmin().getCurrentUser());
         }
         awa.getStateMgr().updateMetrics(awa.getStateDefinition(), hours, percent, true);
         if (persist) {
            awa.persist(transaction);
         }
      }
      if (persist) {
         transaction.execute();
      }

   }
}
