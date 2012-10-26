/*
 * Created on Feb 27, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.mocks;

import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.framework.core.data.Identity;

/**
 * @author Donald G. Dunne
 */
public class MockWorkItem implements IAtsWorkItem {

   private final String name;
   private final String id;

   public MockWorkItem(String name) {
      this.name = name;
      this.id = name;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public String getGuid() {
      return id;
   }

   @Override
   public String getDescription() {
      return name;
   }

   @Override
   public String getHumanReadableId() {
      return id;
   }

   @Override
   public boolean matches(Identity<?>... identities) {
      for (Identity<?> identity : identities) {
         if (equals(identity)) {
            return true;
         }
      }
      return false;
   }

   /*
    * Provide easy way to display/report [hrid][name]
    */
   @Override
   public final String toStringWithId() {
      return String.format("[%s][%s]", getHumanReadableId(), getName());
   }

}
