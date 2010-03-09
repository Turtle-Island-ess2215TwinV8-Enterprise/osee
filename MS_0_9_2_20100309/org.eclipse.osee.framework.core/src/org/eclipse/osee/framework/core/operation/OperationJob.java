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
package org.eclipse.osee.framework.core.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Ryan D. Brooks
 */
public class OperationJob extends Job {
   private final IOperation operation;

   /**
    * @param operation the operation that will be executed in this Job
    */
   public OperationJob(IOperation operation) {
      super(operation.getName());
      this.operation = operation;
   }

   @Override
   protected IStatus run(IProgressMonitor monitor) {
      return operation.run(monitor).getStatus();
   }

   @Override
   public boolean belongsTo(Object family) {
      return OperationJob.class.equals(family);
   }
}