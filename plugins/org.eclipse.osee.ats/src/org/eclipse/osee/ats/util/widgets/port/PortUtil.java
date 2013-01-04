/*
 * Created on Aug 31, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.port;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.api.version.IAtsVersionService;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.config.AtsVersionService;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.branch.commit.CommitHandler;

public class PortUtil {

   public static PortStatus getPortStatus(TeamWorkFlowArtifact destinationWorkflow, TeamWorkFlowArtifact sourceWorkflow) throws OseeCoreException {
      PortStatus toReturn = PortStatus.NONE;
      // if there is already a branch and it is not committed
      if (AtsBranchManagerCore.isPortBranchInWork(sourceWorkflow)) {
         toReturn = PortStatus.ERROR_PORT_BRANCH_EXISTS;
      } else if (!AtsBranchManagerCore.isCommittedBranchExists(sourceWorkflow)) {
         // TODO from parallel dev meeting - what is the difference between committed and completed - should be reflected here
         toReturn = PortStatus.ERROR_NO_COMMIT_TRANSACTION_FOUND;
      } else {
         // determine if the source work flow has already been ported to the target version of the destination work flow
         IAtsVersionService versionService = AtsVersionService.get();

         IAtsVersion destinationVersion = versionService.getTargetedVersion(destinationWorkflow);
         if (destinationVersion != null) {
            boolean isAlreadyPorted = false;
            List<Artifact> destinations = sourceWorkflow.getRelatedArtifacts(AtsRelationTypes.Port_To);
            for (Artifact artifact : destinations) {
               if (artifact instanceof TeamWorkFlowArtifact) {
                  TeamWorkFlowArtifact destination = (TeamWorkFlowArtifact) artifact;
                  if (!destinationWorkflow.equals(destination)) {
                     IAtsVersion otherDestionationVersion = versionService.getTargetedVersion(destination);
                     isAlreadyPorted = destinationVersion.equals(otherDestionationVersion);
                     if (isAlreadyPorted) {
                        break;
                     }
                  }
               }
            }

            if (isAlreadyPorted) {
               toReturn = PortStatus.ERROR_ALREADY_PORTED_TO_TARGET_VERSION;
            } else {
               Branch branch = destinationWorkflow.getWorkingBranch();
               boolean committed = PortUtil.isPortedToWorkingBranch(sourceWorkflow, branch);
               if (committed) {
                  toReturn = PortStatus.PORTED;
               }
            }
         } else {
            toReturn = PortStatus.ERROR_TARGET_VERSION_NOT_SET;
         }
      }
      return toReturn;
   }

   public static PortAction getPortAction(TeamWorkFlowArtifact destTeamArt, TeamWorkFlowArtifact portFromTeamArt) throws OseeCoreException {
      PortStatus status = getPortStatus(destTeamArt, portFromTeamArt);
      if (status.isError()) {
         return PortAction.RESOLVE_ERROR;
      }
      TeamWorkFlowArtifact nextTeamArt = getNextTeamWfToPort(destTeamArt);
      if (nextTeamArt != null && nextTeamArt.equals(portFromTeamArt)) {
         return PortAction.APPLY_NEXT;
      }
      return PortAction.NONE;
   }

   public static TeamWorkFlowArtifact getNextTeamWfToPort(TeamWorkFlowArtifact destTeamArt) throws OseeCoreException {
      TeamWorkFlowArtifact nextTeamArt = null;
      TransactionRecord nextTransRecord = null;
      Branch branch = destTeamArt.getWorkingBranch();
      for (TeamWorkFlowArtifact teamArt : getPortToTeamWorkflows(destTeamArt)) {
         if (!isPortedToWorkingBranch(teamArt, branch)) {
            TransactionRecord transactionRecord = AtsBranchManagerCore.getEarliestTransactionId(teamArt);
            if (transactionRecord != null) {
               if (nextTransRecord == null || transactionRecord.getTimeStamp().before(nextTransRecord.getTimeStamp())) {
                  nextTransRecord = transactionRecord;
                  nextTeamArt = teamArt;
               }
            }
         }
      }
      return nextTeamArt;
   }

   /**
    * Returns true if portFromTeamArt has ported its changes to the destTeamArt's working branch
    */
   public static boolean isPortedToWorkingBranch(TeamWorkFlowArtifact portFromTeamArt, Branch branch) throws OseeCoreException {
      boolean toReturn = false;
      for (TransactionRecord transRecord : TransactionManager.getTransactionsForBranch(branch)) {
         if (transRecord.getCommit() == portFromTeamArt.getArtId()) {
            toReturn = true;
            break;
         }
      }
      return toReturn;
   }

   public static boolean portWorkflowToWorkingBranch(TeamWorkFlowArtifact portFromTeamArt, Branch branch) throws OseeCoreException {
      boolean branchCommitted = false;
      boolean committed = PortUtil.isPortedToWorkingBranch(portFromTeamArt, branch);
      if (!committed) {
         TransactionRecord transRecord = AtsBranchManagerCore.getEarliestTransactionId(portFromTeamArt);

         // Expand branch from commit tx
         Branch workingBranch =
            BranchManager.createPortBranchFromTx(transRecord,
               String.format("Port [%s] to [%s]", portFromTeamArt.getHumanReadableId(), branch.getShortName()),
               portFromTeamArt);

         // when the branch is committed, the portFromTeamArt will be set as the
         // committing artifact for the working branch, used in the isPortedToWorkingBranch to determine if 
         // we have already ported this team work flow artifact to the given branch

         // Commit into current working branch
         ConflictManagerExternal conflictManager = new ConflictManagerExternal(branch, workingBranch);

         branchCommitted = CommitHandler.commitBranch(conflictManager, false, false);
         if (!branchCommitted) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Error: Branch not committed");
         }
      } else {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP,
            String.format("Error: Branch Already Ported To Working Branch %s", branch.getShortName()));
      }

      // kick events to update porting table
      //TODO cleanup working branch???

      return branchCommitted;
   }

   public static Collection<TeamWorkFlowArtifact> getPortToTeamWorkflows(TeamWorkFlowArtifact destTeamArt) throws OseeCoreException {
      return destTeamArt.getRelatedArtifacts(AtsRelationTypes.Port_From, TeamWorkFlowArtifact.class);
   }
}
