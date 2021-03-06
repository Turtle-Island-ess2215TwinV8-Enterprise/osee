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
package org.eclipse.osee.framework.ui.admin.dbtabletab;

public interface ITaskListViewer {

   /**
    * Update the view to reflect the fact that a task was added to the task list
    */
   public void addTask(DbModel task);

   /**
    * Update the view to reflect the fact that a task was removed from the task list
    */
   public void removeTask(DbModel task);

   /**
    * Update the view to reflect the fact that one of the tasks was modified
    */
   public void updateTask(DbModel task);
}
