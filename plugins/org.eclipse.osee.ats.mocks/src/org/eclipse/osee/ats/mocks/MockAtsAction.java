/*
 * Created on Aug 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.mocks;

import org.eclipse.osee.ats.api.action.IAtsAction;
import org.eclipse.osee.framework.core.data.Identity;

public class MockAtsAction implements IAtsAction {

   private final String name;

   public MockAtsAction(String name) {
      this.name = name;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public String getGuid() {
      return name;
   }

   @Override
   public String getDescription() {
      return name;
   }

   @Override
   public String getHumanReadableId() {
      return "MOCKA";
   }

   @Override
   public boolean matches(Identity<?>... identities) {
      return false;
   }

   @Override
   public final String toStringWithId() {
      return String.format("[%s][%s]", getHumanReadableId(), getName());
   }

}
