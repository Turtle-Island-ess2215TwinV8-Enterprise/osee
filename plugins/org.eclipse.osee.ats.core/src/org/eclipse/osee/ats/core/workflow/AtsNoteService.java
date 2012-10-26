/*
 * Created on Jun 21, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.workflow;

import org.eclipse.osee.ats.api.workflow.notes.IAtsNoteService;

/**
 * @author Donald G. Dunne
 */
public class AtsNoteService {

   private static AtsNoteService instance;
   private IAtsNoteService service;

   public static IAtsNoteService get() {
      if (instance == null) {
         throw new IllegalStateException("ATS Note Service has not been activated");
      }
      return instance.service;
   }

   public void setNoteService(IAtsNoteService service) {
      this.service = service;
   }

   public void start() {
      instance = this;
   }

   public static boolean isActive() {
      return instance != null;
   }

}
