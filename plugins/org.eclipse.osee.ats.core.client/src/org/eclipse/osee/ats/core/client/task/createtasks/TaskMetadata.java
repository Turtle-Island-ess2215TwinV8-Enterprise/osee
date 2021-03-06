/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.task.createtasks;

import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Shawn F. Cook
 */
public class TaskMetadata {

   private final TeamWorkFlowArtifact parentTeamWf;
   private final TaskArtifact taskArtifact;
   private final TaskEnum taskEnum;
   private final Artifact changedArtifact;

   public TaskMetadata(TeamWorkFlowArtifact parentWorkflow, TaskArtifact taskArtifact, Artifact changedArtifact, TaskEnum taskEnum) {
      super();
      this.parentTeamWf = parentWorkflow;
      this.taskArtifact = taskArtifact;
      this.taskEnum = taskEnum;
      this.changedArtifact = changedArtifact;
   }

   public TeamWorkFlowArtifact getParentTeamWf() {
      return parentTeamWf;
   }

   public TaskArtifact getTaskArtifact() {
      return taskArtifact;
   }

   public TaskEnum getTaskEnum() {
      return taskEnum;
   }

   public Artifact getChangedArtifact() {
      return changedArtifact;
   }

}
