/*
 * Created on Mar 16, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junit.framework.Assert;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.util.AtsLib;
import org.eclipse.osee.ats.mocks.MockGuest;
import org.eclipse.osee.ats.mocks.MockSystemUser;
import org.eclipse.osee.ats.mocks.MockUnAssigned;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class AtsLibTest {

   @Test
   public void testConstructor() {
      new AtsLib();
   }

   @Test
   public void testToGuids() {
      List<IAtsObject> objs = new ArrayList<IAtsObject>();
      objs.add(MockSystemUser.instance);
      objs.add(MockGuest.instance);
      Assert.assertEquals(Arrays.asList(MockSystemUser.instance.getGuid(), MockGuest.instance.getGuid()),
         AtsLib.toGuids(objs));
   }

   @Test
   public void testToString() {
      Assert.assertEquals("", AtsLib.toString("; ", Collections.emptyList()));

      List<Object> objs = new ArrayList<Object>();
      objs.add(MockSystemUser.instance);
      objs.add(MockGuest.instance);
      objs.add(MockUnAssigned.instance);
      objs.add("Just a String");
      Assert.assertEquals(String.format("%s; %s; %s; Just a String", MockSystemUser.instance.getName(),
         MockGuest.instance.getName(), MockUnAssigned.instance.getName()), AtsLib.toString("; ", objs));
   }

   @Test
   public void testGetNames() {
      List<IAtsObject> objs = new ArrayList<IAtsObject>();
      objs.add(MockSystemUser.instance);
      objs.add(MockGuest.instance);
      objs.add(MockUnAssigned.instance);
      Assert.assertEquals(
         Arrays.asList(MockSystemUser.instance.getName(), MockGuest.instance.getName(),
            MockUnAssigned.instance.getName()), AtsLib.getNames(objs));
   }

}
