/*
 * Created on Aug 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.task;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.task.IAtsTask;
import org.eclipse.osee.ats.api.task.IAtsTaskService;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public class AtsTaskServiceImpl implements IAtsTaskService {

   private static AtsTaskServiceImpl instance = new AtsTaskServiceImpl();

   public static AtsTaskServiceImpl get() {
      return instance;
   }

   @Override
   public Collection<? extends IAtsTask> getTasks(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsTaskStoreService.get().getTasks(workItem);
   }

   public boolean isTaskable(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsTaskStoreService.get().isTaskable(workItem);
   }
}
