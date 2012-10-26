/*
 * Created on Mar 16, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.mocks.MockAtsUser;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class AtsUsersTest {

   private final MockAtsUser joe = new MockAtsUser("joe");
   private final MockAtsUser steve = new MockAtsUser("steve");
   private final MockAtsUser alice = new MockAtsUser("alice");

   @Test
   public void testConstructor() {
      new AtsUserServiceImpl();
   }

   @Test
   public void testIsSystemUser() {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      Assert.assertFalse(service.isSystemUser(null));
      Assert.assertTrue(service.isSystemUser(SystemUser.instance));
      Assert.assertFalse(service.isSystemUser(UnAssigned.instance));
   }

   @Test
   public void testIsGuestUser() {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      Assert.assertFalse(service.isGuestUser(null));
      Assert.assertTrue(service.isGuestUser(Guest.instance));
      Assert.assertFalse(service.isGuestUser(UnAssigned.instance));
   }

   @Test
   public void testIsUnAssignedUser() {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      Assert.assertFalse(service.isUnAssignedUser(null));
      Assert.assertTrue(service.isUnAssignedUser(UnAssigned.instance));
      Assert.assertFalse(service.isUnAssignedUser(Guest.instance));
   }

   @Test
   public void testToList() {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      Set<IAtsUser> users = new HashSet<IAtsUser>();
      users.add(joe);
      users.add(steve);
      users.add(alice);
      List<IAtsUser> list = service.toList(users);
      Assert.assertNotNull(list);
      Assert.assertEquals(list.size(), 3);
      Assert.assertTrue(list.contains(joe));
      Assert.assertTrue(list.contains(alice));
      Assert.assertTrue(list.contains(steve));
   }

   @Test
   public void testGetUser() {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      Assert.assertEquals(SystemUser.instance, service.getUser(SystemUser.instance.getUserId()));
      Assert.assertNull(service.getUser("2345"));
      Assert.assertNull(service.getUser(null));

      Assert.assertEquals(SystemUser.instance, service.getUser(SystemUser.instance.getUserId()));
      Assert.assertEquals(Guest.instance, service.getUser(Guest.instance.getUserId()));
      Assert.assertEquals(UnAssigned.instance, service.getUser(UnAssigned.instance.getUserId()));
   }

   @Test
   public void testGetGuestUser() {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      Assert.assertEquals(Guest.instance, service.getGuestUser());
   }

   @Test
   public void testClearCache() throws OseeCoreException {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      service.clearCache();
      Assert.assertTrue(service.getUsers().isEmpty());

      service.addUser(joe);
      Assert.assertFalse(service.getUsers().isEmpty());

      service.clearCache();
      Assert.assertTrue(service.getUsers().isEmpty());
   }

   @Test
   public void testGetUsersByUserIds() throws OseeCoreException {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      service.clearCache();
      service.addUser(joe);
      service.addUser(steve);
      List<String> userIds = new ArrayList<String>();
      userIds.add(joe.getUserId());
      userIds.add(steve.getUserId());
      userIds.add(alice.getUserId());
      Collection<IAtsUser> users = service.getUsersByUserIds(userIds);
      Assert.assertNotNull(users);
      Assert.assertEquals(users.size(), 2);
      Assert.assertTrue(users.contains(joe));
      Assert.assertTrue(users.contains(steve));
      service.addUser(alice);
      users = service.getUsersByUserIds(userIds);
      Assert.assertNotNull(users);
      Assert.assertEquals(users.size(), 3);
      Assert.assertTrue(users.contains(joe));
      Assert.assertTrue(users.contains(steve));
      Assert.assertTrue(users.contains(alice));
   }

   @Test
   public void testGetUsers() throws OseeCoreException {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      service.clearCache();
      service.addUser(joe);
      service.addUser(steve);
      Collection<IAtsUser> users = service.getUsers();
      Assert.assertNotNull(users);
      Assert.assertEquals(users.size(), 2);
      Assert.assertTrue(users.contains(joe));
      Assert.assertTrue(users.contains(steve));
   }

   @Test
   public void testGetSystemUser() {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      Assert.assertEquals(SystemUser.instance, service.getSystemUser());
   }

   @Test
   public void testGetUnAssigned() {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      Assert.assertEquals(UnAssigned.instance, service.getUnAssigned());
   }

   @Test
   public void testGetValidEmailUsers() throws OseeCoreException {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      Set<IAtsUser> users = new HashSet<IAtsUser>();
      users.add(joe);
      users.add(steve);
      users.add(alice);
      Assert.assertTrue(service.getValidEmailUsers(users).isEmpty());

      joe.setEmail("b@b.com");
      steve.setEmail("asdf");
      alice.setEmail(null);

      Assert.assertEquals(1, service.getValidEmailUsers(users).size());
      Assert.assertEquals(joe, service.getValidEmailUsers(users).iterator().next());
   }

   @Test
   public void testIsEmailValid() {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      Assert.assertTrue(service.isEmailValid("b@b.com"));
      Assert.assertFalse(service.isEmailValid("asdf"));
      Assert.assertFalse(service.isEmailValid(null));
   }

   @Test
   public void testGetActiveEmailUsers() throws OseeCoreException {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      Set<IAtsUser> users = new HashSet<IAtsUser>();
      users.add(joe);
      users.add(steve);
      users.add(alice);
      joe.setEmail("b@b.com");
      joe.setActive(true);
      steve.setEmail("b@b.com");
      steve.setActive(false);
      alice.setEmail("b@b.com");
      alice.setActive(true);

      Collection<IAtsUser> activeEmailUsers = service.getActiveEmailUsers(users);
      Assert.assertEquals(2, activeEmailUsers.size());
      Assert.assertTrue(activeEmailUsers.contains(joe));
      Assert.assertTrue(activeEmailUsers.contains(alice));
   }

   @Test
   public void testAddUser() throws OseeCoreException {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      service.clearCache();
      Assert.assertTrue(service.getUsers().isEmpty());
      service.addUser(joe);
      Assert.assertFalse(service.getUsers().isEmpty());
   }

   @Test(expected = OseeArgumentException.class)
   public void testAddUser_null() throws OseeCoreException {
      AtsUserServiceImpl service = new AtsUserServiceImpl();
      service.clearCache();
      Assert.assertTrue(service.getUsers().isEmpty());
      service.addUser(null);
      Assert.assertTrue(service.getUsers().isEmpty());
   }
}
