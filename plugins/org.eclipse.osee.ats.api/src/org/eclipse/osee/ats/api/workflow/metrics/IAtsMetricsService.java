/*
 * Created on Aug 1, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.workflow.metrics;

import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public interface IAtsMetricsService {

   int getPercentCompleteTotal(IAtsWorkItem workItem) throws OseeCoreException;

   double getHoursSpentTotal(IAtsWorkItem workItem) throws OseeCoreException;

   double getHoursSpent(IAtsWorkItem workItem, String stateName) throws OseeCoreException;

}
