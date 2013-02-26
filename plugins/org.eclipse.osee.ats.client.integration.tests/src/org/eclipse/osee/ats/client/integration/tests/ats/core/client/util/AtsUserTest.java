/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import junit.framework.Assert;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.client.demo.DemoUsers;
import org.eclipse.osee.ats.core.client.util.AtsUser;
import org.eclipse.osee.ats.core.client.util.AtsUsersClient;
import org.eclipse.osee.framework.core.data.Identity;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;

/**
 * @author Donald G. Dunne
 */
public class AtsUserTest {

   @org.junit.Test
   public void testGetUserId() throws OseeCoreException {
      User user = UserManager.getUser();
      AtsUser atsUser = new AtsUser(user);
      Assert.assertEquals(user.getUserId(), atsUser.getUserId());
   }

   @org.junit.Test
   public void testGetName() throws OseeCoreException {
      User user = UserManager.getUser();
      AtsUser atsUser = new AtsUser(user);
      Assert.assertEquals(user.getName(), atsUser.getName());
   }

   @org.junit.Test
   public void testGetEmail() throws OseeCoreException {
      User user = UserManager.getUser();
      AtsUser atsUser = new AtsUser(user);
      Assert.assertEquals(user.getEmail(), atsUser.getEmail());
   }

   @org.junit.Test
   public void testEquals() throws OseeCoreException {
      User user = UserManager.getUser();
      AtsUser atsUser = new AtsUser(user);
      Assert.assertEquals(atsUser, user);

      IAtsUser atsUser2 = getAtsUser(user.getName(), user.getUserId());
      Assert.assertEquals(atsUser, atsUser2);
   }

   @org.junit.Test
   public void testRemove() throws OseeCoreException {
      Collection<IAtsUser> assignees = new HashSet<IAtsUser>();
      assignees.add(AtsUsersClient.getUserFromToken(DemoUsers.Alex_Kay));
      assignees.add(AtsUsersClient.getUserFromToken(DemoUsers.Joe_Smith));
      Assert.assertTrue(Collections.isEqual(
         assignees,
         Arrays.asList(AtsUsersClient.getUserFromToken(DemoUsers.Alex_Kay),
            AtsUsersClient.getUserFromToken(DemoUsers.Joe_Smith))));

      assignees.remove(AtsUsersClient.getUser());
      Assert.assertTrue(Collections.isEqual(assignees,
         Arrays.asList(AtsUsersClient.getUserFromToken(DemoUsers.Alex_Kay))));
   }

   public IAtsUser getAtsUser(final String name, final String userId) {
      return new IAtsUser() {

         @Override
         public int compareTo(Object o) {
            return 0;
         }

         @Override
         public String getName() {
            return name;
         }

         @Override
         public String getHumanReadableId() {
            return null;
         }

         @Override
         public String getGuid() {
            return null;
         }

         @Override
         public String getDescription() {
            return null;
         }

         @Override
         public boolean isActive() {
            return false;
         }

         @Override
         public String getUserId() {
            return userId;
         }

         @Override
         public String getEmail() {
            return null;
         }

         @Override
         public boolean matches(Identity<?>... identities) {
            return false;
         }
      };
   }

}