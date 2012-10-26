/*
 * Created on Aug 3, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.client.workflow;

import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.workflow.metrics.IAtsMetricsService;
import org.eclipse.osee.ats.core.client.util.WorkItemUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public class AtsMetricsServiceImpl implements IAtsMetricsService {

   @Override
   public int getPercentCompleteTotal(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) WorkItemUtil.get(workItem);
      return PercentCompleteTotalUtil.getPercentCompleteTotal(awa);
   }

   @Override
   public double getHoursSpentTotal(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) WorkItemUtil.get(workItem);
      return HoursSpentUtil.getHoursSpentTotal(awa);
   }

   @Override
   public double getHoursSpent(IAtsWorkItem workItem, String stateName) throws OseeCoreException {
      AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) WorkItemUtil.get(workItem);
      return HoursSpentUtil.getHoursSpentTotal(awa, awa.getStateDefinitionByName(stateName));
   }

}
