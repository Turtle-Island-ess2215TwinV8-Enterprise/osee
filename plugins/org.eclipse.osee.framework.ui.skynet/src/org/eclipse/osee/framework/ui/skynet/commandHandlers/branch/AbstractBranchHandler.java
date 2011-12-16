/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.commandHandlers.branch;

import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.ui.plugin.util.CommandHandler;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.Handlers;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Karol M. Wilk
 */
public abstract class AbstractBranchHandler extends CommandHandler {

   private final String actionName;
   private final String dialogTitle;

   protected AbstractBranchHandler(String actionName, String dialogTitle) {
      this.actionName = actionName;
      this.dialogTitle = dialogTitle;
   }

   public abstract void performOperation(final List<Branch> branches) throws OseeCoreException;

   @Override
   public Object executeWithException(ExecutionEvent arg0) throws OseeCoreException {
      try {
         IStructuredSelection selections = getCurrentSelection();

         List<Branch> selectedBranches = Handlers.getBranchesFromStructuredSelection(selections);

         MessageDialog dialog =
            new MessageDialog(Displays.getActiveShell(), dialogTitle, null, buildDialogMessage(selectedBranches,
               actionName), MessageDialog.QUESTION,
               new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 1);

         if (dialog.open() == 0) {
            performOperation(selectedBranches);
         }
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return null;
   }

   @Override
   public boolean isEnabledWithException(IStructuredSelection structuredSelection) throws OseeCoreException {
      List<? extends IOseeBranch> branches = Handlers.getBranchesFromStructuredSelection(structuredSelection);
      return !branches.isEmpty() && AccessControlManager.isOseeAdmin();
   }

   private String buildDialogMessage(List<? extends IOseeBranch> selectedBranches, String actionDesc) {
      StringBuilder branchesStatement = new StringBuilder();
      branchesStatement.append(String.format("Are you sure you want to %s branch(es): ", actionDesc));
      branchesStatement.append(Strings.buildStatment(selectedBranches));
      branchesStatement.append(" \u003F");
      return branchesStatement.toString();
   }
}