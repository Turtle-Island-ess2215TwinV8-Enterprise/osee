/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.coverage.model;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

public class WorkProductAction {

   String guid;
   String name;
   boolean completed;
   Set<WorkProductTask> tasks = new HashSet<WorkProductTask>();

   public WorkProductAction(Artifact artifact, boolean completed) {
      this(artifact.getGuid(), artifact.getName(), completed);
   }

   public WorkProductAction(String guid, String name, boolean completed) {
      super();
      this.guid = guid;
      this.name = name;
      this.completed = completed;
   }

   public String getGuid() {
      return guid;
   }

   public String getName() {
      return name;
   }

   public boolean isCompleted() {
      return completed;
   }

   public Set<WorkProductTask> getTasks() {
      return tasks;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((guid == null) ? 0 : guid.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      WorkProductAction other = (WorkProductAction) obj;
      if (guid == null) {
         if (other.guid != null) {
            return false;
         }
      } else if (!guid.equals(other.guid)) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return getName() + " - " + (completed ? "[Completed]" : "[InWork]");
   }
}
