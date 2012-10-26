/*
 * Created on Mar 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.mocks;


/**
 * @author Donald G. Dunne
 */
public class MockUnAssigned extends AbstractAtsUser {

   public static MockUnAssigned instance = new MockUnAssigned();

   private MockUnAssigned() {
      super("99999997");
   }

   @Override
   public String getName() {
      return "UnAssigned";
   }

   @Override
   public String getGuid() {
      return "AAABDi1tMx8Al92YWMjeRw";
   }

   @Override
   public String getHumanReadableId() {
      return "7G020";
   }

   @Override
   public boolean isActive() {
      return true;
   }

}
