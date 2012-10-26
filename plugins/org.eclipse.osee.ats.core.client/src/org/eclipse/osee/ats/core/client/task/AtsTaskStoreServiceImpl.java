/*
 * Created on Aug 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.client.task;

import java.util.Collection;
import java.util.Collections;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.task.IAtsTask;
import org.eclipse.osee.ats.api.task.IAtsTaskStore;
import org.eclipse.osee.ats.core.client.util.WorkItemUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

public class AtsTaskStoreServiceImpl implements IAtsTaskStore {

   @Override
   public Collection<? extends IAtsTask> getTasks(IAtsWorkItem workItem) throws OseeCoreException {
      Artifact artifact = WorkItemUtil.get(workItem);
      if (artifact != null) {
         if (artifact instanceof AbstractTaskableArtifact) {
            AbstractTaskableArtifact awa = (AbstractTaskableArtifact) artifact;
            return awa.getTaskArtifacts();
         }
      }
      return Collections.emptyList();
   }

   @Override
   public boolean isTaskable(IAtsWorkItem workItem) throws OseeCoreException {
      Artifact artifact = WorkItemUtil.get(workItem);
      if (artifact != null) {
         return (artifact instanceof AbstractTaskableArtifact);
      }
      return false;
   }

}
