/*
 * Created on Aug 31, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.port;

import java.util.logging.Level;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.widgets.xviewer.Activator;
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

   private final XPortTableWidget portManager;

   public XPortSorter(XPortTableWidget portManager) {
      this.portManager = portManager;
   }

   @Override
   public int compare(Viewer viewer, Object o1, Object o2) {
      if (o1 instanceof IAtsTeamWorkflow && o2 instanceof IAtsTeamWorkflow) {
         try {
            TeamWorkFlowArtifact teamWf1 = (TeamWorkFlowArtifact) o1;
            TeamWorkFlowArtifact teamWf2 = (TeamWorkFlowArtifact) o2;
            PortStatus portStatus = PortUtil.getPortStatus(portManager.getTeamArt(), teamWf1);
            if (portStatus.isError()) {
               return -1;
            }
            portStatus = PortUtil.getPortStatus(portManager.getTeamArt(), teamWf2);
            if (portStatus.isError()) {
               return 1;
            }
            TransactionRecord transId1 = AtsBranchManagerCore.getEarliestTransactionId(teamWf1);
            TransactionRecord transId2 = AtsBranchManagerCore.getEarliestTransactionId(teamWf2);
            if (transId1 == null && transId2 != null) {
               return -1;
            } else if (transId1 != null && transId2 == null) {
               return 1;
            }
            return super.compare(viewer, transId1.getTimeStamp(), transId2.getTimeStamp());
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      return super.compare(viewer, o1, o2);
   }

}
