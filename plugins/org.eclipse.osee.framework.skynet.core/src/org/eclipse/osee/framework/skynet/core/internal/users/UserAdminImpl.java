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
package org.eclipse.osee.framework.skynet.core.internal.users;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.osee.cache.admin.Cache;
import org.eclipse.osee.cache.admin.CacheAdmin;
import org.eclipse.osee.framework.core.data.IUserToken;
import org.eclipse.osee.framework.core.data.LazyObject;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.exception.UserDataStoreException;
import org.eclipse.osee.framework.core.exception.UserNotInDatabase;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserAdmin;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Roberto E. Escobar
 */
public class UserAdminImpl implements UserAdmin {

   private UserKeysProvider keysProvider;
   private LazyObject<Cache<String, User>> cacheProvider;
   private CurrentUserProvider currentUserProvider;
   private UserDataWriter userWriteAccessor;
   private UserArtifactEventListener userArtifactEventListener;

   private CacheAdmin cacheAdmin;

   public void setCacheAdmin(CacheAdmin cacheAdmin) {
      this.cacheAdmin = cacheAdmin;
   }

   public void start() {
      keysProvider = new UserKeysProvider();
      cacheProvider = new UserCacheProvider(cacheAdmin, new UserDataLoader(), keysProvider);
      userWriteAccessor = new UserDataWriter(cacheProvider);
      currentUserProvider = new CurrentUserProvider(cacheProvider, userWriteAccessor);
      userArtifactEventListener = new UserArtifactEventListener(cacheProvider, keysProvider);

      OseeEventManager.addListener(userArtifactEventListener);
   }

   public void stop() {
      if (userArtifactEventListener != null) {
         OseeEventManager.removeListener(userArtifactEventListener);
         userArtifactEventListener = null;
      }

      if (currentUserProvider != null) {
         currentUserProvider.invalidate();
         currentUserProvider = null;
      }

      if (userWriteAccessor != null) {
         userWriteAccessor = null;
      }

      if (cacheProvider != null) {
         cacheProvider.invalidate();
         cacheProvider = null;
      }

      if (keysProvider != null) {
         keysProvider.invalidate();
         keysProvider = null;
      }
   }

   @Override
   public void reset() {
      keysProvider.invalidate();
      currentUserProvider.invalidate();
      cacheProvider.invalidate();
   }

   private Cache<String, User> getCache() throws OseeCoreException {
      return cacheProvider.get();
   }

   @Override
   public boolean isDuringCurrentUserCreation() {
      return currentUserProvider.isDuringCurrentUserCreation();
   }

   @Override
   public User getCurrentUser() throws OseeCoreException {
      User currentUser = currentUserProvider.get();
      if (!currentUser.isActive()) {
         currentUser.setActive(true);
         currentUser.persist("Set current user active");
      }
      return currentUser;
   }

   @Override
   public void releaseCurrentUser() {
      currentUserProvider.invalidate();
   }

   @Override
   public User getUserByUserId(String userId) throws OseeCoreException {
      Conditions.checkNotNullOrEmpty(userId, "userId");
      Cache<String, User> cache = getCache();
      User user = null;
      try {
         user = cache.get(userId);
      } catch (Exception ex) {
         UserDataStoreException userEx = unwrapUserException(ex);
         if(userEx != null){
            throw userEx;
         } else {
            OseeExceptions.wrapAndThrow(ex);
         }
      }
      return user;
   }

   private UserDataStoreException unwrapUserException(Throwable ex) {
      UserDataStoreException toReturn = null;
      Throwable cause = ex.getCause();
      if (cause instanceof UserDataStoreException) {
         toReturn = (UserDataStoreException) cause;
      } else if (cause != null) {
         toReturn = unwrapUserException(cause);
      }
      return toReturn;
   }

   @Override
   public List<User> getActiveUsers() throws OseeCoreException {
      Cache<String, User> cache = getCache();
      List<User> activeUsers = new ArrayList<User>();
      Iterable<User> all = null;
      try {
         all = cache.getAll();
      } catch (Exception e) {
         OseeExceptions.wrapAndThrow(e);
      }
      for (User user : all) {
         if (user.isActive()) {
            activeUsers.add(user);
         }
      }
      return activeUsers;
   }

   @Override
   public List<User> getUsersAll() throws OseeCoreException {
      Cache<String, User> cache = getCache();
      List<User> users = new ArrayList<User>();
      Iterable<User> all = null;
      try {
         all = cache.getAll();
      } catch (Exception e) {
         OseeExceptions.wrapAndThrow(e);
      }
      for (User user : all) {
         users.add(user);
      }
      return users;
   }

   @Override
   public List<User> getActiveUsersSortedByName() throws OseeCoreException {
      List<User> users = getActiveUsers();
      Collections.sort(users);
      return users;
   }

   @Override
   public List<User> getUsersAllSortedByName() throws OseeCoreException {
      List<User> users = getUsersAll();
      Collections.sort(users);
      return users;
   }

   @Override
   public String[] getUserNames() throws OseeCoreException {
      List<User> allUsers = getUsersAll();
      String[] userNames = new String[allUsers.size()];
      int index = 0;
      for (User user : allUsers) {
         userNames[index++] = user.getName();
      }
      return userNames;
   }

   /**
    * This is not the preferred way to get a user. Most likely getUserByUserId() or getUserByArtId() should be used
    * 
    * @return the first user found with the given name
    */
   @Override
   public User getUserByName(String name) throws OseeCoreException {
      Conditions.checkNotNullOrEmpty(name, "user name");

      for (User tempUser : getUsersAll()) {
         if (name.equals(tempUser.getName())) {
            return tempUser;
         }
      }
      throw new UserNotInDatabase("User requested by name [%s] was not found.", name);
   }

   @Override
   public User getUser(IUserToken user) throws OseeCoreException {
      Conditions.checkNotNull(user, "user data");
      return getUserByUserId(user.getUserId());
   }

   @Override
   public String getUserNameById(int userArtifactId) throws OseeCoreException {
      User user = getUserByArtId(userArtifactId);
      if (user == null) {
         throw new UserNotInDatabase("Unable to find user with artId[%s]", userArtifactId);
      }
      String name = user.getName();
      return name;
   }

   @Override
   public String getSafeUserNameById(int userArtifactId) {
      String name;
      try {
         name = getUserNameById(userArtifactId);
      } catch (Exception ex) {
         name = String.format("Could not resolve user with artId[%s]", userArtifactId);
      }
      return name;
   }

   @Override
   public User getUserByArtId(int userArtifactId) throws OseeCoreException {
      User toReturn = null;
      if (userArtifactId <= 0) {
         toReturn = getUser(SystemUser.OseeSystem);
      } else {
         for (User tempUser : getUsersAll()) {
            if (userArtifactId == tempUser.getArtId()) {
               toReturn = tempUser;
               break;
            }
         }
      }
      return toReturn;
   }

   @Override
   public User createUser(IUserToken userToken, String comment) throws OseeCoreException {
      return userWriteAccessor.createUser(userToken, comment);
   }

   @Override
   public User createUser(IUserToken userToken, SkynetTransaction transaction) throws OseeCoreException {
      return userWriteAccessor.createUser(userToken, transaction);
   }

}
