/*
 * Created on Mar 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.mocks;

/**
 * @author Donald G. Dunne
 */
public class MockAtsUser extends AbstractAtsUser {

   String guid = null;

   public MockAtsUser(String name) {
      super(name);
   }

   @Override
   public String getName() {
      return getUserId();
   }

   @Override
   public String getGuid() {
      return getUserId();
   }

   @Override
   public String getHumanReadableId() {
      return "TBRQV";
   }

}
