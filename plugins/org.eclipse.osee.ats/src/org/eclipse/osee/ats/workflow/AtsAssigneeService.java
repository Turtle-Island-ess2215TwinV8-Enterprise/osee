/*
 * Created on Aug 2, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.workflow;

import org.eclipse.osee.ats.api.workflow.assignee.IAtsAssigneeService;

public class AtsAssigneeService {

   private static AtsAssigneeService instance;
   private IAtsAssigneeService service;

   public static IAtsAssigneeService get() {
      if (instance == null) {
         throw new IllegalStateException("ATS Assignee Service has not been activated");
      }
      return instance.service;
   }

   public void setAssigneeService(IAtsAssigneeService service) {
      this.service = service;
   }

   public void start() {
      instance = this;
   }

   public static boolean isActive() {
      return instance != null;
   }
}
