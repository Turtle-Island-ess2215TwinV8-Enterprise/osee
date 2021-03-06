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
package org.eclipse.osee.framework.ui.skynet.commandHandlers.branch.commit;

import java.util.Iterator;
import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.jdk.core.type.MutableInteger;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.framework.ui.plugin.util.CommandHandler;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.Handlers;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.widgets.xBranch.BranchOptionsEnum;
import org.eclipse.osee.framework.ui.skynet.widgets.xmerge.MergeView;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Jeff C. Phillips
 * @author Ryan D. Brooks
 */
public abstract class CommitHandler extends CommandHandler {
   protected final boolean useParentBranch;

   public CommitHandler(boolean useParentBranch) {
      this.useParentBranch = useParentBranch;
   }

   public static boolean commitBranch(final ConflictManagerExternal conflictManager, boolean archiveSourceBranch) throws OseeCoreException {
      final Branch sourceBranch = conflictManager.getSourceBranch();
      final Branch destinationBranch = conflictManager.getDestinationBranch();
      final TransactionRecord transactionId = sourceBranch.getBaseTransaction();
      boolean branchCommitted = false;

      if (!conflictManager.getRemainingConflicts().isEmpty()) {
         String message =
            "Commit stopped due to unresolved conflicts\n\nPossible Resolutions:\n  Cancel commit and resolve at a later time\n  Launch the Merge Manager to resolve conflicts";
         final String fMessage;
         final String[] choices;
         if (AccessControlManager.isOseeAdmin()) {
            fMessage = message + "\n  Force the commit";
            choices = new String[] {"Cancel", "Launch Merge Manager", "Force Commit (Admin Only)"};
         } else {
            fMessage = message;
            choices = new String[] {"Cancel", "Launch Merge Manager"};
         }

         final MutableInteger dialogResult = new MutableInteger(0);
         Displays.pendInDisplayThread(new Runnable() {
            @Override
            public void run() {
               MessageDialog dialog =
                  new MessageDialog(Displays.getActiveShell(), "Unresolved Conflicts", null, fMessage,
                     MessageDialog.QUESTION, choices, 0);
               dialogResult.setValue(dialog.open());
               if (dialogResult.getValue() == 1) {
                  MergeView.openView(sourceBranch, destinationBranch, transactionId);
               }
            }
         });

         if (dialogResult.getValue() == 2) {
            BranchManager.commitBranch(null, conflictManager, archiveSourceBranch, true);
            branchCommitted = true;
         }
      } else {
         final StringBuilder message =
            new StringBuilder(
               "Commit branch\n\n\"" + sourceBranch + "\"\n\n onto destination branch\n\n\"" + destinationBranch + "\"\n");
         int numOriginalConflicts = conflictManager.getOriginalConflicts().size();
         if (numOriginalConflicts > 0) {
            message.append("\nwith " + numOriginalConflicts + " conflicts resolved.\n");
         } else {
            message.append("\n(no conflicts found)\n");
         }
         message.append("\nCommit?");

         final MutableInteger dialogResult = new MutableInteger(0);
         Displays.pendInDisplayThread(new Runnable() {
            @Override
            public void run() {
               try {
                  if (conflictManager.getOriginalConflicts().isEmpty()) {
                     MessageDialog dialog =
                        new MessageDialog(Displays.getActiveShell(), "Commit Branch", null, message.toString(),
                           MessageDialog.QUESTION, new String[] {"Ok", "Cancel"}, 0);
                     dialogResult.setValue(dialog.open());
                  } else {
                     MessageDialog dialog =
                        new MessageDialog(Displays.getActiveShell(), "Commit Branch", null, message.toString(),
                           MessageDialog.QUESTION, new String[] {"Ok", "Launch Merge Manager", "Cancel"}, 0);
                     dialogResult.setValue(dialog.open());
                     if (dialogResult.getValue() == 1) {
                        MergeView.openView(sourceBranch, destinationBranch, transactionId);
                     }
                  }
               } catch (OseeCoreException ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         });

         if (dialogResult.getValue() == 0) {
            BranchManager.commitBranch(null, conflictManager, archiveSourceBranch, false);
            branchCommitted = true;
         }
      }
      return branchCommitted;
   }

   @Override
   public Object executeWithException(ExecutionEvent event, IStructuredSelection selection) throws OseeCoreException {
      try {
         List<Branch> branches = Handlers.getBranchesFromStructuredSelection(selection);
         Iterator<Branch> iterator = branches.iterator();
         if (iterator.hasNext()) {
            Branch sourceBranch = iterator.next();

            Branch destinationBranch = null;
            if (useParentBranch) {
               destinationBranch = sourceBranch.getParentBranch();
            } else {
               destinationBranch =
                  BranchManager.getBranch(Integer.parseInt(event.getParameter(BranchOptionsEnum.BRANCH_ID.origKeyName)));
            }
            Jobs.startJob(new CommitJob(sourceBranch, destinationBranch,
               Boolean.parseBoolean(event.getParameter(CommitBranchParameter.ARCHIVE_PARENT_BRANCH))));
         }
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return null;
   }

   @Override
   public boolean isEnabledWithException(IStructuredSelection structuredSelection) throws OseeCoreException {
      List<Branch> branches = Handlers.getBranchesFromStructuredSelection(structuredSelection);

      if (branches.size() == 1) {
         Branch branch = branches.iterator().next();
         return useParentBranchValid(branch) || !useParentBranch && AccessControlManager.isOseeAdmin();
      }
      return false;
   }

   protected boolean useParentBranchValid(Branch branch) throws OseeCoreException {
      return branch.hasParentBranch() && useParentBranch && !BranchManager.isChangeManaged(branch) && !branch.getArchiveState().isArchived();
   }
   protected class CommitJob extends Job {
      private final Branch sourceBranch;
      private final Branch destinationBranch;
      private final boolean archiveSourceBranch;

      public CommitJob(Branch sourceBranch, Branch destinationBranch, boolean archiveSourceBranch) {
         super("Commit Branch");
         this.destinationBranch = destinationBranch;
         this.sourceBranch = sourceBranch;
         this.archiveSourceBranch = archiveSourceBranch;
      }

      @Override
      protected IStatus run(IProgressMonitor monitor) {
         try {
            commitBranch(new ConflictManagerExternal(destinationBranch, sourceBranch), archiveSourceBranch);
         } catch (OseeCoreException ex) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getLocalizedMessage(), ex);
         }
         return Status.OK_STATUS;
      }
   }
}
