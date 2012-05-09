/*
 * Created on Feb 28, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.client.util;

import org.eclipse.osee.ats.core.model.IAtsUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.User;

public class AtsUser implements IAtsUser {

   private final User user;

   public AtsUser(User user) {
      this.user = user;
   }

   @Override
   public String getName() {
      return user.getName();
   }

   @Override
   public String getGuid() {
      return user.getGuid();
   }

   @Override
   public String getDescription() {
      return user.getDescription();
   }

   @Override
   public String getHumanReadableId() {
      return user.getHumanReadableId();
   }

   @Override
   public int compareTo(Object other) {
      int result = other != null ? -1 : 1;
      if (other instanceof IAtsUser) {
         String otherName = ((IAtsUser) other).getName();
         String thisName = user != null ? getName() : null;
         if (thisName == null && otherName == null) {
            result = 0;
         } else if (thisName != null && otherName == null) {
            result = 1;
         } else if (thisName != null && otherName != null) {
            result = thisName.compareTo(otherName);
         }
      } else if (other instanceof User) {
         result = user != null ? user.compareTo((User) other) : -1;
      }
      return result;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 0;
      int userIdHashCode = 0;
      try {
         userIdHashCode = (getUserId() == null) ? 0 : getUserId().hashCode();
      } catch (OseeCoreException ex) {
         // Do nothing;
      }
      result = prime * result + userIdHashCode;
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      try {
         String objUserId = null;
         if (obj instanceof IAtsUser) {
            objUserId = ((IAtsUser) obj).getUserId();
         } else if (obj instanceof User) {
            objUserId = ((User) obj).getUserId();
         } else {
            return false;
         }
         String thisUserId = getUserId();
         if (thisUserId == null) {
            if (objUserId != null) {
               return false;
            }
         } else if (!thisUserId.equals(objUserId)) {
            return false;
         }
      } catch (OseeCoreException ex) {
         return false;
      }
      return true;
   }

   @Override
   public String getUserId() throws OseeCoreException {
      return user.getUserId();
   }

   @Override
   public String getEmail() throws OseeCoreException {
      return user.getEmail();
   }

   @Override
   public boolean isActive() throws OseeCoreException {
      return user.isActive();
   }

   @Override
   public String toString() {
      try {
         return String.format("%s (%s)", getName(), getUserId());
      } catch (Exception ex) {
         return "Exception: " + ex.getLocalizedMessage();
      }
   }
}