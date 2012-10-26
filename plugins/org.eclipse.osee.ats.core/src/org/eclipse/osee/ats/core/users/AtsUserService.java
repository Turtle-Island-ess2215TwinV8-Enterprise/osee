/*
 * Created on Jun 21, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.users;

import org.eclipse.osee.ats.api.user.IAtsUserService;

/**
 * @author Donald G. Dunne
 */
public class AtsUserService {

   private static AtsUserService instance;
   private IAtsUserService service;

   public static IAtsUserService get() {
      if (instance == null) {
         throw new IllegalStateException("ATS User Service has not been activated");
      }
      return instance.service;
   }

   public void setUserService(IAtsUserService service) {
      this.service = service;
   }

   public void start() {
      instance = this;
   }

   public static boolean isActive() {
      return instance != null;
   }

}
