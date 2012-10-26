/*
 * Created on Feb 28, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.mocks;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.user.IAtsUserService;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class MockAtsUserService implements IAtsUserService {

   public Map<String, IAtsUser> userIdToUser = new HashMap<String, IAtsUser>();
   public static MockAtsUserService instance = new MockAtsUserService();

   public static MockAtsUserService get() {
      return instance;
   }

   public MockAtsUserService() {
      instance = this;
   }

   @Override
   public boolean isSystemUser(IAtsUser user) {
      return MockSystemUser.instance.equals(user);
   }

   @Override
   public boolean isGuestUser(IAtsUser user) {
      return MockGuest.instance.equals(user);
   }

   @Override
   public boolean isUnAssignedUser(IAtsUser user) {
      return MockUnAssigned.instance.equals(user);
   }

   @Override
   public List<IAtsUser> toList(Collection<? extends IAtsUser> users) {
      List<IAtsUser> results = new LinkedList<IAtsUser>();
      for (IAtsUser user : users) {
         results.add(user);
      }
      return results;
   }

   @Override
   public IAtsUser getUser(String userId) {
      if (userId == null) {
         return null;
      }
      IAtsUser user = userIdToUser.get(userId);
      if (user == null) {
         if (userId.equals(MockSystemUser.instance.getUserId())) {
            user = MockSystemUser.instance;
         } else if (userId.equals(MockGuest.instance.getUserId())) {
            user = MockGuest.instance;
         } else if (userId.equals(MockUnAssigned.instance.getUserId())) {
            user = MockUnAssigned.instance;
         }
      }
      return user;
   }

   @Override
   public IAtsUser getGuestUser() {
      return MockGuest.instance;
   }

   @Override
   public Collection<IAtsUser> getUsersByUserIds(Collection<String> userIds) {
      List<IAtsUser> users = new LinkedList<IAtsUser>();
      for (String userId : userIds) {
         IAtsUser user = getUser(userId);
         if (user != null) {
            users.add(user);
         }
      }
      return users;
   }

   @Override
   public IAtsUser getSystemUser() {
      return MockSystemUser.instance;
   }

   @Override
   public IAtsUser getUnAssigned() {
      return MockUnAssigned.instance;
   }

   @Override
   public Collection<IAtsUser> getValidEmailUsers(Collection<? extends IAtsUser> users) throws OseeCoreException {
      Set<IAtsUser> validUsers = new HashSet<IAtsUser>();
      for (IAtsUser user : users) {
         if (isEmailValid(user.getEmail())) {
            validUsers.add(user);
         }
      }
      return validUsers;
   }
   private static Pattern addressPattern = Pattern.compile(".+?@.+?\\.[a-z]+");

   @Override
   public boolean isEmailValid(String email) {
      if (Strings.isValid(email)) {
         return addressPattern.matcher(email).matches();
      }
      return false;
   }

   @Override
   public Collection<IAtsUser> getActiveEmailUsers(Collection<? extends IAtsUser> users) throws OseeCoreException {
      Set<IAtsUser> activeUsers = new HashSet<IAtsUser>();
      for (IAtsUser user : users) {
         if (user.isActive()) {
            activeUsers.add(user);
         }
      }
      return activeUsers;
   }

   @Override
   public void addUser(IAtsUser user) throws OseeCoreException {
      if (user == null) {
         throw new OseeArgumentException("User can't be null");
      }
      userIdToUser.put(user.getUserId(), user);
   }

   @Override
   public void clearCache() {
      userIdToUser.clear();
   }

   @Override
   public Collection<IAtsUser> getUsers() {
      return userIdToUser.values();
   }
}
