/*
 * Created on Aug 31, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.port;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.AbstractOperation;

public class PortApplyAllOperation extends AbstractOperation {

   private final TeamWorkFlowArtifact destTeamArt;

   public PortApplyAllOperation(TeamWorkFlowArtifact destTeamArt) {
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
      Branch branch = destTeamArt.getWorkingBranch();

      TeamWorkFlowArtifact nextTeamWfToPort = PortUtil.getNextTeamWfToPort(destTeamArt);
      boolean success = true;
      while (nextTeamWfToPort != null && success) {
         success = PortUtil.portWorkflowToWorkingBranch(nextTeamWfToPort, branch);
         // loop to do all rest
         nextTeamWfToPort = PortUtil.getNextTeamWfToPort(destTeamArt);
      }

   }
}
