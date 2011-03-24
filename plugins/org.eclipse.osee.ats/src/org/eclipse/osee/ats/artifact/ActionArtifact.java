/*
 * Created on Mar 24, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.artifact;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;

public class ActionArtifact extends Artifact {

   public ActionArtifact(ArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, IArtifactType artifactType) throws OseeCoreException {
      super(parentFactory, guid, humanReadableId, branch, artifactType);
   }

   public Set<ActionableItemArtifact> getActionableItems() throws OseeCoreException {
      Set<ActionableItemArtifact> aias = new HashSet<ActionableItemArtifact>();
      for (TeamWorkFlowArtifact team : getTeams()) {
         aias.addAll(team.getActionableItemsDam().getActionableItems());
      }
      return aias;
   }

   public Collection<TeamWorkFlowArtifact> getTeams() throws OseeCoreException {
      return getRelatedArtifactsUnSorted(AtsRelationTypes.ActionToWorkflow_WorkFlow, TeamWorkFlowArtifact.class);
   }

   public TeamWorkFlowArtifact getFirstTeam() throws OseeCoreException {
      if (getRelatedArtifactsCount(AtsRelationTypes.ActionToWorkflow_WorkFlow) > 0) {
         return getTeams().iterator().next();
      }
      return null;
   }

}
