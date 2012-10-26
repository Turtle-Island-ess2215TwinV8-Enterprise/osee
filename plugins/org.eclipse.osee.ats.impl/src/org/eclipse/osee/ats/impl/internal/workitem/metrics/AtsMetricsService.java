/*
 * Created on Aug 2, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.workitem.metrics;

import org.eclipse.osee.ats.api.workflow.metrics.IAtsMetricsService;

public class AtsMetricsService {

   private static AtsMetricsService instance;
   private IAtsMetricsService service;

   public static IAtsMetricsService get() {
      if (instance == null) {
         throw new IllegalStateException("ATS Metrics Service has not been activated");
      }
      return instance.service;
   }

   public void setMetricsService(IAtsMetricsService service) {
      this.service = service;
   }

   public void start() {
      instance = this;
   }
}
