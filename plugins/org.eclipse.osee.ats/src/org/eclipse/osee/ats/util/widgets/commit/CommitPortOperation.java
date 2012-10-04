/*
 * Created on Aug 31, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.commit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.widgets.port.PortBranch;
import org.eclipse.osee.ats.util.widgets.port.PortBranches;
import org.eclipse.osee.ats.util.widgets.port.PortStatus;
import org.eclipse.osee.ats.util.widgets.port.PortUtil;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.branch.commit.CommitHandler;

public class CommitPortOperation extends AbstractOperation {

   private final TeamWorkFlowArtifact destTeamArt;

   public CommitPortOperation(TeamWorkFlowArtifact destTeamArt) {
      super("Port Apply All", Activator.PLUGIN_ID);
      this.destTeamArt = destTeamArt;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      for (TeamWorkFlowArtifact portTeamArt : PortUtil.getPortToTeamWorkflows(destTeamArt)) {
         PortStatus status = PortUtil.getPortStatus(destTeamArt, portTeamArt);
         if (status.isError()) {
            throw new OseeStateException("Resolve Errors before Apply All [%s] [%s]", portTeamArt.toStringWithId(),
               status);
         }
      }

      if (AtsBranchManagerCore.isCommittedBranchExists(destTeamArt)) {
         throw new OseeStateException("Working Branch already committed");
      }

      if (AtsBranchManagerCore.getParentBranchConfigArtifactConfiguredToCommitTo(destTeamArt) == null) {
         throw new OseeStateException("Parent Branch not set; Set Targeted Version or configure parent branch");
      }

      // Create working branch if necessary
      if (destTeamArt.getWorkingBranch() == null) {
         AtsBranchManagerCore.createWorkingBranch_Create(destTeamArt);
      }

      PortBranches portBranches = new PortBranches(destTeamArt);

      TeamWorkFlowArtifact nextTeamWfToPort = PortUtil.getNextTeamWfToPort(destTeamArt);
      while (nextTeamWfToPort != null) {

         PortBranch portFromBranch = portBranches.getPortBranch(nextTeamWfToPort);
         if (portFromBranch == null) {
            TransactionRecord transRecord = AtsBranchManagerCore.getEarliestTransactionId(nextTeamWfToPort);

            // Expand branch from commit tx
            Branch workingBranch =
               BranchManager.createWorkingBranchFromTx(
                  transRecord,
                  String.format("Port [%s] to [%s]", nextTeamWfToPort.getHumanReadableId(),
                     destTeamArt.getHumanReadableId()));

            portBranches.addPort(nextTeamWfToPort, workingBranch);
            portBranches.saveToArtifact();
            destTeamArt.persist(getName());
         }
         boolean committed = PortUtil.isPortedToWorkingBranch(destTeamArt, nextTeamWfToPort);
         if (!committed) {
            // Commit into current working branch
            ConflictManagerExternal conflictManager =
               new ConflictManagerExternal(destTeamArt.getWorkingBranch(), nextTeamWfToPort.getWorkingBranch());

            boolean branchCommitted = CommitHandler.commitBranch(conflictManager, false, false);
            if (!branchCommitted) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Error: Branch not committed");
            }
         }

         // kick events to update porting table

         // loop to do all rest
         nextTeamWfToPort = PortUtil.getNextTeamWfToPort(destTeamArt);
      }

   }
}
