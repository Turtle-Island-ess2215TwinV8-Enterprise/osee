/*
 * Created on May 16, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.config;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.operation.AbstractOperation;

public class TaskConfigurationAddOperation extends AbstractOperation {

   public TaskConfigurationAddOperation(String operationName, String pluginId) {
      super(operationName, pluginId);
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
   }

}
