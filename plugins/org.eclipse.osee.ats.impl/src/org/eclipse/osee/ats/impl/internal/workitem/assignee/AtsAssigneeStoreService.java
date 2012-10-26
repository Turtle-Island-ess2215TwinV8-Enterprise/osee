/*
 * Created on Aug 2, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.workitem.assignee;

import org.eclipse.osee.ats.api.workflow.assignee.IAtsAssigneeStoreService;

public class AtsAssigneeStoreService {

   private static AtsAssigneeStoreService instance;
   private IAtsAssigneeStoreService service;

   public static IAtsAssigneeStoreService get() {
      if (instance == null) {
         throw new IllegalStateException("ATS Assignee Store Service has not been activated");
      }
      return instance.service;
   }

   public void setAssigneeStoreService(IAtsAssigneeStoreService service) {
      this.service = service;
   }

   public void start() {
      instance = this;
   }
}
