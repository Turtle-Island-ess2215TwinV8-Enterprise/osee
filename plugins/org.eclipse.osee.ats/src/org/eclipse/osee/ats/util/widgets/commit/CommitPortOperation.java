/*
 * Created on Aug 31, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.commit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.api.commit.ICommitConfigArtifact;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsBranchManager;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;

public class CommitPortOperation extends AbstractOperation {

   private final TeamWorkFlowArtifact committingTeamArt;

   public CommitPortOperation(TeamWorkFlowArtifact opTeamArt) {
      super("Commit Port", Activator.PLUGIN_ID);
      this.committingTeamArt = opTeamArt;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {

      ICommitConfigArtifact commitTo =
         AtsBranchManagerCore.getParentBranchConfigArtifactConfiguredToCommitTo(committingTeamArt);
      if (commitTo == null) {
         throw new OseeStateException("Parent Branch not set; Set Targeted Version or configure parent branch");
      }
      String baselineBranch = commitTo.getBaslineBranchGuid();
      Branch commitInto = BranchManager.getBranchByGuid(baselineBranch);

      // TODO should archiveWorkingBranch be set to true? If the branch is going to be committed to multiple child 
      // branches, then having to redo the work on a different artifact seems like it is a waste.
      // currently set to false, indicating that the work should possibly be saved to commit to child branches of the 
      // target version

      AtsBranchManager.commitWorkingBranch(committingTeamArt, true, false, commitInto, false);
   }
}
