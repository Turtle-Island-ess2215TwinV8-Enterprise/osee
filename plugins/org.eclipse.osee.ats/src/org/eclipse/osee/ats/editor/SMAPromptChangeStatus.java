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
import java.util.List;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.TaskArtifact;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.dialog.TaskOptionStatusDialog;
import org.eclipse.osee.ats.util.widgets.dialog.TaskResOptionDefinition;
import org.eclipse.osee.framework.core.data.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.ui.PlatformUI;

/**
 * @author Donald G. Dunne
 */
public class SMAPromptChangeStatus {

   private final Collection<? extends AbstractWorkflowArtifact> smas;

   public SMAPromptChangeStatus(AbstractWorkflowArtifact sma) {
      this(Arrays.asList(sma));
   }

   public SMAPromptChangeStatus(final Collection<? extends AbstractWorkflowArtifact> smas) {
      this.smas = smas;
   }

   public static boolean promptChangeStatus(Collection<? extends AbstractWorkflowArtifact> smas, boolean persist) throws OseeCoreException {
      SMAPromptChangeStatus promptChangeStatus = new SMAPromptChangeStatus(smas);
      return promptChangeStatus.promptChangeStatus(persist).isTrue();
   }

   public static Result isValidToChangeStatus(Collection<? extends AbstractWorkflowArtifact> smas) throws OseeCoreException {
      // Don't allow statusing for any canceled tasks
      for (AbstractWorkflowArtifact sma : smas) {
         if (sma.isCancelled()) {
            String error =
               "Can not status a cancelled " + sma.getArtifactTypeName() + ".\n\nTransition out of cancelled first.";
            return new Result(error);
         }
      }
      // If task status is being changed, make sure tasks belong to current state
      for (AbstractWorkflowArtifact sma : smas) {
         if (sma instanceof TaskArtifact) {
            TaskArtifact taskArt = (TaskArtifact) sma;
            if (!taskArt.isRelatedToParentWorkflowCurrentState()) {
               return new Result(
                  String.format(
                     "Task work must be done in \"Related to State\" of parent workflow for Task titled: \"%s\".\n\n" +
                     //
                     "Task work configured to be done in parent's \"%s\" state.\nParent workflow is currently in \"%s\" state.\n\n" +
                     //
                     "Either transition parent workflow or change Task's \"Related to State\" to perform task work.",
                     taskArt.getName(), taskArt.getWorldViewRelatedToState(),
                     taskArt.getParentSMA().getStateMgr().getCurrentStateName()));
            }
         }
      }
      return Result.TrueResult;
   }

   public Result promptChangeStatus(boolean persist) throws OseeCoreException {
      Result result = isValidToChangeStatus(smas);
      if (result.isFalse()) {
         result.popup();
         return result;
      }

      // Access resolution options if object is task
      List<TaskResOptionDefinition> options = null;
      if (smas.iterator().next() instanceof TaskArtifact && ((TaskArtifact) smas.iterator().next()).isUsingTaskResolutionOptions()) {
         options = ((TaskArtifact) smas.iterator().next()).getTaskResolutionOptionDefintions();
      }
      TaskOptionStatusDialog tsd =
         new TaskOptionStatusDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            "Enter State Status",
            true, options,
            smas);
      if (tsd.open() == 0) {
         performChangeStatus(smas, options,
            tsd.getSelectedOptionDef() != null ? tsd.getSelectedOptionDef().getName() : null,
            tsd.getHours().getFloat(), tsd.getPercent().getInt(), tsd.isSplitHours(), persist);
         return Result.TrueResult;
      }
      return Result.FalseResult;
   }

   public static void performChangeStatus(Collection<? extends AbstractWorkflowArtifact> smas, List<TaskResOptionDefinition> options, String selectedOption, double hours, int percent, boolean splitHours, boolean persist) throws OseeCoreException {
      if (splitHours) {
         hours = hours / smas.size();
      }
      SkynetTransaction transaction = null;
      if (persist) {
         transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "ATS Prompt Change Status");
      }
      for (AbstractWorkflowArtifact sma : smas) {
         if (sma.getStateMgr().isUnAssigned()) {
            sma.getStateMgr().removeAssignee(UserManager.getUser(SystemUser.UnAssigned));
            sma.getStateMgr().addAssignee(UserManager.getUser());
         }
         if (options != null) {
            sma.setSoleAttributeValue(AtsAttributeTypes.Resolution, selectedOption);
         }
         if (sma instanceof TaskArtifact) {
            ((TaskArtifact) sma).statusPercentChanged(hours, percent, transaction);
         } else {
            sma.getStateMgr().updateMetrics(hours, percent, true);
         }
         if (persist) {
            sma.persist(transaction);
         }
      }
      if (persist) {
         transaction.execute();
      }

   }

}
