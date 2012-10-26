/*
 * Created on Aug 2, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.task;

import org.eclipse.osee.ats.api.task.IAtsTaskStore;

public class AtsTaskStoreService {

   private static AtsTaskStoreService instance;
   private IAtsTaskStore service;

   public static IAtsTaskStore get() {
      if (instance == null) {
         throw new IllegalStateException("ATS Task Store Service has not been activated");
      }
      return instance.service;
   }

   public void setTaskStoreService(IAtsTaskStore service) {
      this.service = service;
   }

   public void start() {
      instance = this;
   }

}
