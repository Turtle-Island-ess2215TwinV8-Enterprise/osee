/*
 * Created on Jul 25, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.user;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public interface IAtsUserService {

   boolean isSystemUser(IAtsUser user);

   boolean isGuestUser(IAtsUser user);

   boolean isUnAssignedUser(IAtsUser user);

   List<IAtsUser> toList(Collection<? extends IAtsUser> users);

   IAtsUser getUser(String userId);

   IAtsUser getGuestUser();

   Collection<IAtsUser> getUsersByUserIds(Collection<String> userIds);

   IAtsUser getSystemUser();

   IAtsUser getUnAssigned();

   Collection<IAtsUser> getValidEmailUsers(Collection<? extends IAtsUser> users) throws OseeCoreException;

   boolean isEmailValid(String email);

   Collection<IAtsUser> getActiveEmailUsers(Collection<? extends IAtsUser> users) throws OseeCoreException;

   void addUser(IAtsUser user) throws OseeCoreException;

   void clearCache();

   Collection<IAtsUser> getUsers();

}
