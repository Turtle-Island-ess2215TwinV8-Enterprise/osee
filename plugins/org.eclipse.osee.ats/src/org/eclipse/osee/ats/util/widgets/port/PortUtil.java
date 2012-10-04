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
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;

public class PortUtil {

   public static PortStatus getPortStatus(TeamWorkFlowArtifact destinationWorkflow, TeamWorkFlowArtifact sourceWorkflow) throws OseeCoreException {
      PortStatus toReturn = PortStatus.NONE;
      if (AtsBranchManagerCore.isWorkingBranchInWork(sourceWorkflow)) {
         toReturn = PortStatus.ERROR_WORKING_BRANCH_EXISTS;
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
               boolean committed = PortUtil.isPortedToWorkingBranch(destinationWorkflow, sourceWorkflow);
               if (committed) {
                  toReturn = PortStatus.PORTED;
               } else {
                  PortBranches portBranches = new PortBranches(destinationWorkflow);
                  if (portBranches.getPortBranch(sourceWorkflow) != null) {
                     toReturn = PortStatus.PORT_FROM_BRANCH_CREATED;
                  }
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
      for (TeamWorkFlowArtifact teamArt : getPortToTeamWorkflows(destTeamArt)) {
         if (!isPortedToWorkingBranch(destTeamArt, teamArt)) {
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
   public static boolean isPortedToWorkingBranch(TeamWorkFlowArtifact destTeamArt, TeamWorkFlowArtifact portFromTeamArt) throws OseeCoreException {
      if (destTeamArt.getWorkingBranch() != null) {
         for (TransactionRecord transRecord : TransactionManager.getTransactionsForBranch(destTeamArt.getWorkingBranch())) {
            if (transRecord.getCommit() == portFromTeamArt.getArtId()) {
               return true;
            }
         }
      }
      return false;
   }

   public static Collection<TeamWorkFlowArtifact> getPortToTeamWorkflows(TeamWorkFlowArtifact destTeamArt) throws OseeCoreException {
      return destTeamArt.getRelatedArtifacts(AtsRelationTypes.Port_From, TeamWorkFlowArtifact.class);
   }
}
