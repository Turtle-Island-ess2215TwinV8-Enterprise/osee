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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Shawn F. Cook
 */
public class ExecuteTaskOpList {
   public Map<TaskMetadata, IStatus> execute(List<TaskMetadata> metadatas, Map<TaskEnum, ITaskOperation> ops, SkynetTransaction transaction) throws OseeCoreException {
      Map<TaskMetadata, IStatus> statusMap = new HashMap<TaskMetadata, IStatus>();

      for (TaskMetadata metadata : metadatas) {
         ITaskOperation operation = ops.get(metadata.getTaskEnum());
         IStatus status = operation.execute(metadata, transaction);
         statusMap.put(metadata, status);
      }

      return statusMap;
   }

}
