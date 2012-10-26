/*
 * Created on Jun 21, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.workdef;

import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionService;

/**
 * @author Donald G. Dunne
 */
public class AtsWorkDefinitionService {

   private static AtsWorkDefinitionService instance;
   private IAtsWorkDefinitionService service;

   public static IAtsWorkDefinitionService get() {
      if (instance == null) {
         throw new IllegalStateException("ATS Work Definition Service has not been activated");
      }
      return instance.service;
   }

   public void setWorkDefinitionService(IAtsWorkDefinitionService service) {
      this.service = service;
   }

   public void start() {
      instance = this;
   }

   public static boolean isActive() {
      return instance != null;
   }

}
