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
package org.eclipse.osee.ats.core.client.util;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.task.AbstractTaskableArtifact;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * Tasks cache for taskable workflows
 * 
 * @author Donald G. Dunne
 */
public class AtsTaskCache {

   private static Map<AbstractTaskableArtifact, Collection<TaskArtifact>> teamTasksCache =
      new ConcurrentHashMap<AbstractTaskableArtifact, Collection<TaskArtifact>>();

   private AtsTaskCache() {
      // Utility class
   }

   public static void decache(Artifact sma) {
      if (sma != null) {
         teamTasksCache.remove(sma);
      }
   }

   public static void decache(AbstractTaskableArtifact sma) {
      if (sma != null) {
         teamTasksCache.remove(sma);
      }
   }

   public static Collection<TaskArtifact> getTaskArtifacts(AbstractTaskableArtifact sma) throws OseeCoreException {
      Collection<TaskArtifact> tasks = teamTasksCache.get(sma);
      if (tasks == null || containsDeleted(tasks)) {
         //         System.out.println("caching tasks for " + sma.toStringWithId());
         // Get and cache tasks
         tasks = sma.getRelatedArtifacts(AtsRelationTypes.TeamWfToTask_Task, TaskArtifact.class);
         teamTasksCache.put(sma, tasks);
      }
      return tasks;
   }

   private static boolean containsDeleted(Collection<TaskArtifact> tasks) {
      boolean result = false;
      for (TaskArtifact task : tasks) {
         if (task.isDeleted()) {
            result = true;
            break;
         }
      }
      return result;
   }

}
