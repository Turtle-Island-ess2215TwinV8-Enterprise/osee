/*
 * Created on Mar 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.mocks;


/**
 * @author Donald G. Dunne
 */
public class MockSystemUser extends AbstractAtsUser {

   public static MockSystemUser instance = new MockSystemUser();

   private MockSystemUser() {
      super("99999999");
   }

   @Override
   public String getName() {
      return "OSEE System";
   }

   @Override
   public String getGuid() {
      return "AAABDBYPet4AGJyrc9dY1w";
   }

   @Override
   public String getDescription() {
      return "System User";
   }

   @Override
   public String getHumanReadableId() {
      return "FTNT9";
   }

   @Override
   public boolean isActive() {
      return true;
   }

}
