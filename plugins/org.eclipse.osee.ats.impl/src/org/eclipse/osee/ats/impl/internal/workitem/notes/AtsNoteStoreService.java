/*
 * Created on Jul 16, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.workitem.notes;

import org.eclipse.osee.ats.api.workflow.notes.IAtsNoteStore;

public class AtsNoteStoreService {

   private static AtsNoteStoreService instance;
   private IAtsNoteStore service;

   public static IAtsNoteStore get() {
      if (instance == null) {
         throw new IllegalStateException("ATS Note Store Service has not been activated");
      }
      return instance.service;
   }

   public void setNoteStoreService(IAtsNoteStore service) {
      this.service = service;
   }

   public void start() {
      instance = this;
   }

}
