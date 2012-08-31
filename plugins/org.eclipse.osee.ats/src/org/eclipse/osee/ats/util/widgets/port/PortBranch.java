/*
 * Created on Sep 4, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.port;

import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.core.data.IOseeBranch;

public class PortBranch {

   private String fromTeamWorkflowGuid;
   private String portBranchGuid;

   public PortBranch(IAtsTeamWorkflow fromTeamWorkflow) {
      this(fromTeamWorkflow, null);
   }

   public PortBranch(IAtsTeamWorkflow fromTeamWorkflow, IOseeBranch portBranch) {
      this.fromTeamWorkflowGuid = fromTeamWorkflow.getGuid();
      this.portBranchGuid = portBranch.getGuid();
   }

   public PortBranch(String fromTeamWorkflowGuid, String portBranchGuid) {
      this.fromTeamWorkflowGuid = fromTeamWorkflowGuid;
      this.portBranchGuid = portBranchGuid;
   }

   public String getFromTeamWorkflowGuid() {
      return fromTeamWorkflowGuid;
   }

   public void setFromTeamWorkflowGuid(String fromTeamWorkflowGuid) {
      this.fromTeamWorkflowGuid = fromTeamWorkflowGuid;
   }

   public String getPortBranchGuid() {
      return portBranchGuid;
   }

   public void setPortBranchGuid(String portBranchGuid) {
      this.portBranchGuid = portBranchGuid;
   }

}
