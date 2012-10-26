/*
 * Created on Jun 21, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.workitem;

import org.eclipse.osee.ats.api.workflow.IAtsWorkItemStore;

/**
 * @author Donald G. Dunne
 */
public class AtsWorkItemStoreService {

   private static AtsWorkItemStoreService instance;
   private IAtsWorkItemStore service;

   public static IAtsWorkItemStore get() {
      if (instance == null) {
         throw new IllegalStateException("ATS Work Item Store Service has not been activated");
      }
      return instance.service;
   }

   public void setWorkStoreService(IAtsWorkItemStore service) {
      this.service = service;
   }

   public void start() {
      instance = this;
   }
}
