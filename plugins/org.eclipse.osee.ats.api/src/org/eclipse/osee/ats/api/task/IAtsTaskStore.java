/*
 * Created on Aug 2, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.task;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public interface IAtsTaskStore {

   Collection<? extends IAtsTask> getTasks(IAtsWorkItem workItem) throws OseeCoreException;

   boolean isTaskable(IAtsWorkItem workItem) throws OseeCoreException;

}
