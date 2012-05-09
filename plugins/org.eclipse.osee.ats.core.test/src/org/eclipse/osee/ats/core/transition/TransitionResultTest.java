/*
 * Created on Mar 20, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.transition;

import org.eclipse.osee.ats.core.workflow.transition.TransitionResult;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link TransitionResult}
 *
 * @author Donald G. Dunne
 */
public class TransitionResultTest {

   @Test
   public void testGetDetails() {
      TransitionResult.CAN_NOT_TRANSITION_AS_SYSTEM_USER.getDetails();
   }

   @Test
   public void testToString() {
      TransitionResult.CAN_NOT_TRANSITION_AS_SYSTEM_USER.toString();
   }

   @Test
   public void testGetException() {
      TransitionResult result = new TransitionResult("details", new OseeStateException("hello"));
      Assert.assertNotNull(result);
      Assert.assertNotNull(result.getException());
      Assert.assertNotNull(result.getDetails());
   }
}