/*
 * Created on Aug 31, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.port;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.widgets.xviewer.Activator;
import org.eclipse.osee.ats.api.commit.ICommitConfigArtifact;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * Sort by earliest transaction commit
 * 
 * @author Donald G. Dunne
 */
public class XPortSorter extends ViewerSorter {

   private TeamWorkFlowArtifact teamWorkflow;

   public XPortSorter() {
      //
   }

   public void setTeamWorkflow(TeamWorkFlowArtifact teamWorkflow) {
      this.teamWorkflow = teamWorkflow;
   }

   @Override
   public int compare(Viewer viewer, Object o1, Object o2) {
      if (teamWorkflow != null) {
         if (o1 instanceof IAtsTeamWorkflow && o2 instanceof IAtsTeamWorkflow) {
            try {
               TeamWorkFlowArtifact teamWf1 = (TeamWorkFlowArtifact) o1;
               TeamWorkFlowArtifact teamWf2 = (TeamWorkFlowArtifact) o2;
               PortStatus portStatus = PortUtil.getPortStatus(teamWorkflow, teamWf1);
               if (portStatus.isError()) {
                  return -1;
               }
               portStatus = PortUtil.getPortStatus(teamWorkflow, teamWf2);
               if (portStatus.isError()) {
                  return 1;
               }
               ICommitConfigArtifact configArt1 =
                  AtsBranchManagerCore.getParentBranchConfigArtifactConfiguredToCommitTo(teamWf1);
               ICommitConfigArtifact configArt2 =
                  AtsBranchManagerCore.getParentBranchConfigArtifactConfiguredToCommitTo(teamWf2);

               if (configArt1 == null && configArt2 != null) {
                  return -1;
               } else if (configArt1 != null && configArt2 == null) {
                  return 1;
               } else if (configArt1 == null && configArt2 == null) {
                  return 0;
               }

               Collection<TransactionRecord> txIds1 = AtsBranchManagerCore.getTransactionIds(teamWf1, false);
               Collection<TransactionRecord> txIds2 = AtsBranchManagerCore.getTransactionIds(teamWf2, false);

               TransactionRecord tx1 = null;
               TransactionRecord tx2 = null;

               for (TransactionRecord txRec : txIds1) {
                  String txBranchGuid = txRec.getBranch().getGuid();
                  String configArtBranchGuid = configArt1.getBaslineBranchGuid();
                  if (txBranchGuid.compareTo(configArtBranchGuid) == 0) {
                     tx1 = txRec;
                     break;
                  }
               }

               for (TransactionRecord txRec : txIds2) {
                  String txBranchGuid = txRec.getBranch().getGuid();
                  String configArtBranchGuid = configArt1.getBaslineBranchGuid();
                  if (txBranchGuid.compareTo(configArtBranchGuid) == 0) {
                     tx2 = txRec;
                     break;
                  }
               }

               if (tx1 == null && tx2 != null) {
                  return -1;
               } else if (tx1 != null && tx2 == null) {
                  return 1;
               }

               Date date1 = tx1.getTimeStamp();
               Date date2 = tx2.getTimeStamp();

               Timestamp timeStamp1 = new Timestamp(date1.getTime());
               Timestamp timeStamp2 = new Timestamp(date2.getTime());

               return super.compare(viewer, timeStamp1, timeStamp2);
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      }
      return super.compare(viewer, o1, o2);
   }

}
