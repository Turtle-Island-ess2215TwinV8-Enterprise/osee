package org.eclipse.osee.framework.ui.skynet.change;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.IBranchProvider;
import org.eclipse.osee.framework.ui.skynet.branch.BranchSelectionDialog;
import org.eclipse.ui.progress.UIJob;

public final class UiOtherBranchDialogProvider implements IBranchProvider {
   private final ChangeUiData uiData;

   public UiOtherBranchDialogProvider(ChangeUiData uiData) {
      this.uiData = uiData;
   }

   @Override
   public Branch getBranch(IProgressMonitor monitor) throws OseeCoreException {
      final Branch[] selectedBranch = new Branch[1];
      final Collection<Branch> selectable = BranchManager.getBaselineBranches();
      selectable.remove(uiData.getTxDelta().getStartTx().getBranch());

      Job job = new UIJob("Select Branch") {

         @Override
         public IStatus runInUIThread(IProgressMonitor monitor) {
            IStatus status = Status.OK_STATUS;
            BranchSelectionDialog dialog = new BranchSelectionDialog("Select branch to compare against", selectable);
            int result = dialog.open();
            if (result == Window.OK) {
               selectedBranch[0] = dialog.getSelected();
            } else {
               status = Status.CANCEL_STATUS;
            }
            return status;
         }
      };
      try {
         Jobs.startJob(job).join();
      } catch (InterruptedException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      monitor.setCanceled(job.getResult().getSeverity() == IStatus.CANCEL);
      return selectedBranch[0];
   }
}