/*
 * Created on Aug 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.client.action;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.action.IAtsActionStoreService;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.core.client.util.WorkItemUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

public class AtsActionStoreService implements IAtsActionStoreService {

   @Override
   public Collection<? extends IAtsTeamWorkflow> getTeamWorkflows(IAtsWorkItem workItem) throws OseeCoreException {
      Artifact art = WorkItemUtil.get(workItem);
      if (art != null && art instanceof ActionArtifact) {
         return ActionManager.getTeams(art);
      }
      return null;
   }

   @Override
   public IAtsTeamWorkflow getFirstTeamWorkflow(IAtsWorkItem workItem) throws OseeCoreException {
      Collection<? extends IAtsTeamWorkflow> teamWorkflows = getTeamWorkflows(workItem);
      if (!teamWorkflows.isEmpty()) {
         return teamWorkflows.iterator().next();
      }
      return null;
   }

}
