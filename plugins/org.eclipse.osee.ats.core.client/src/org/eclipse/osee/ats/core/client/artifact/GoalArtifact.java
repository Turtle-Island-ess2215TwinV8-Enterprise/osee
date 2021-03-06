/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.artifact;

import java.util.List;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.workflow.IAtsGoal;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.GoalArtifactMembersCache;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class GoalArtifact extends AbstractWorkflowArtifact implements IAtsGoal {

   private List<Artifact> members;
   private volatile boolean firstTime = true;

   public GoalArtifact(String guid, String humanReadableId, Branch branch, IArtifactType artifactType) throws OseeCoreException {
      super(guid, humanReadableId, branch, artifactType);
   }

   @Override
   public Artifact getParentActionArtifact() {
      return null;
   }

   @Override
   public AbstractWorkflowArtifact getParentAWA() throws OseeCoreException {
      List<Artifact> parents = getRelatedArtifacts(AtsRelationTypes.Goal_Goal);
      if (parents.isEmpty()) {
         return null;
      }
      if (parents.size() == 1) {
         return (AbstractWorkflowArtifact) parents.iterator().next();
      }
      // TODO Two parent goals, what do here?
      return (AbstractWorkflowArtifact) parents.iterator().next();
   }

   @Override
   public TeamWorkFlowArtifact getParentTeamWorkflow() {
      return null;
   }

   public List<Artifact> getMembers() throws OseeCoreException {
      return GoalArtifactMembersCache.getMembers(this);
   }

   public void addMember(Artifact artifact) throws OseeCoreException {
      if (!getMembers().contains(artifact)) {
         addRelation(AtsRelationTypes.Goal_Member, artifact);
      }
   }

}
