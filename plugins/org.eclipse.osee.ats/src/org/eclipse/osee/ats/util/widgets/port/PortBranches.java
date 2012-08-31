/*
 * Created on Sep 4, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.port;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public class PortBranches {

   private final TeamWorkFlowArtifact destTeamArt;
   private List<PortBranch> ports = null;

   public PortBranches(TeamWorkFlowArtifact destTeamArt) {
      this.destTeamArt = destTeamArt;
   }

   public PortBranch getPortBranch(TeamWorkFlowArtifact portFromTeamArt) throws OseeCoreException {
      for (PortBranch port : getPorts()) {
         if (port.getFromTeamWorkflowGuid().equals(portFromTeamArt.getGuid())) {
            return port;
         }
      }
      return null;
   }

   public void addPort(TeamWorkFlowArtifact portFromTeamArt, IOseeBranch branch) throws OseeCoreException {
      getPorts().add(new PortBranch(portFromTeamArt.getGuid(), branch.getGuid()));
   }

   public void saveToArtifact() throws OseeCoreException {
      StringBuilder sb = new StringBuilder();
      for (PortBranch port : getPorts()) {
         sb.append(String.format("%s;%s;", port.getFromTeamWorkflowGuid(), port.getPortBranchGuid()));
      }
      destTeamArt.setSoleAttributeValue(AtsAttributeTypes.PortBranches, sb.toString().replaceFirst(";$", ""));
   }

   private List<PortBranch> getPorts() throws OseeCoreException {
      if (ports == null) {
         ports = new ArrayList<PortBranch>();
         String[] split = destTeamArt.getSoleAttributeValue(AtsAttributeTypes.PortBranches, "").split(";");
         if (split.length > 1) {
            for (int x = 0; x < split.length - 1; x += 2) {
               ports.add(new PortBranch(split[x], split[x + 1]));
            }
         }
      }
      return ports;
   }
}
