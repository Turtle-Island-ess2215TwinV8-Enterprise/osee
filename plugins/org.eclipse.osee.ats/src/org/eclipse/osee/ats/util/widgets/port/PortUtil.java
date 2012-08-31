/*
 * Created on Aug 31, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.port;

import java.util.Collection;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;

public class PortUtil {

   public static PortStatus getPortStatus(TeamWorkFlowArtifact destTeamArt, TeamWorkFlowArtifact portFromTeamArt) throws OseeCoreException {
      if (AtsBranchManagerCore.isWorkingBranchInWork(portFromTeamArt)) {
         return PortStatus.ERROR_WORKING_BRANCH_EXISTS;
      } else if (!AtsBranchManagerCore.isCommittedBranchExists(portFromTeamArt)) {
         return PortStatus.ERROR_NO_COMMIT_TRANSACTION_FOUND;
      }
      boolean committed = PortUtil.isPortedToWorkingBranch(destTeamArt, portFromTeamArt);
      if (committed) {
         return PortStatus.PORTED;
      }
      PortBranches portBranches = new PortBranches(destTeamArt);
      if (portBranches.getPortBranch(portFromTeamArt) != null) {
         return PortStatus.PORT_FROM_BRANCH_CREATED;
      }
      return PortStatus.NONE;
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
