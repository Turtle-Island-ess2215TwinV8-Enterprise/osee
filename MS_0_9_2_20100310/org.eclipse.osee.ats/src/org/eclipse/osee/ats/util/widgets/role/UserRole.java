/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.util.widgets.role;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AXml;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;

/**
 * @author Donald G. Dunne
 */
public class UserRole {

   private Role role = Role.Reviewer;
   private User user;
   private Double hoursSpent = null;
   private String guid = GUID.create();
   private Boolean completed = false;

   public static enum Role {
      Moderator, Reviewer, Author;
      public static Collection<String> strValues() {
         Set<String> values = new HashSet<String>();
         for (Enum<Role> e : values()) {
            values.add(e.name());
         }
         return values;
      }
   };

   public UserRole() throws OseeCoreException {
      this(Role.Reviewer, UserManager.getUser(), null, false);
   }

   public UserRole(Role role, User user) {
      this(role, user, 0.0, false);
   }

   public UserRole(Role role, User user, Double hoursSpent, Boolean completed) {
      this.role = role;
      this.user = user;
      this.hoursSpent = hoursSpent;
      this.completed = completed;
   }

   public UserRole(String xml) {
      fromXml(xml);
   }

   public void update(UserRole dItem) throws OseeCoreException {
      fromXml(dItem.toXml());
   }

   public String toXml() throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      sb.append(AXml.addTagData("role", role.name()));
      sb.append(AXml.addTagData("userId", user.getUserId()));
      sb.append(AXml.addTagData("hoursSpent", hoursSpent == null ? "" : String.valueOf(hoursSpent)));
      sb.append(AXml.addTagData("completed", String.valueOf(completed)));
      sb.append(AXml.addTagData("guid", guid));
      return sb.toString();
   }

   public void fromXml(String xml) {
      try {
         this.role = Role.valueOf(AXml.getTagData(xml, "role"));
         this.user = UserManager.getUserByUserId(AXml.getTagData(xml, "userId"));
         String hoursSpent = AXml.getTagData(xml, "hoursSpent");
         if (Strings.isValid(hoursSpent)) {
            this.hoursSpent = NumberFormat.getInstance().parse(hoursSpent).doubleValue();
         } else {
            this.hoursSpent = null;
         }
         String completedStr = AXml.getTagData(xml, "completed");
         if (Strings.isValid(completedStr)) {
            this.completed = completedStr.equals("true");
         } else {
            this.completed = false;
         }
         this.guid = AXml.getTagData(xml, "guid");
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof UserRole) {
         UserRole userRole = (UserRole) obj;
         return userRole.getGuid().equals(getGuid());
      }
      return false;
   }

   @Override
   public int hashCode() {
      return getGuid().hashCode();
   }

   @Override
   public String toString() {
      return role + " - " + user + " - " + hoursSpent + " - " + (completed ? "Completed" : "InWork");
   }

   /**
    * @return the role
    */
   public Role getRole() {
      return role;
   }

   /**
    * @param role the role to set
    */
   public void setRole(Role role) {
      this.role = role;
   }

   /**
    * @return the user
    */
   public User getUser() {
      return user;
   }

   /**
    * @param user the user to set
    */
   public void setUser(User user) {
      this.user = user;
   }

   /**
    * @return the hoursSpent
    */
   public Double getHoursSpent() {
      return hoursSpent;
   }

   public String getHoursSpentStr() {
      return hoursSpent == null ? "" : AtsUtil.doubleToI18nString(hoursSpent, true);
   }

   /**
    * @param hoursSpent the hoursSpent to set
    */
   public void setHoursSpent(Double hoursSpent) {
      this.hoursSpent = hoursSpent;
   }

   /**
    * @return the guid
    */
   public String getGuid() {
      return guid;
   }

   /**
    * @param guid the guid to set
    */
   public void setGuid(String guid) {
      this.guid = guid;
   }

   /**
    * @return the completed
    */
   public boolean isCompleted() {
      return completed;
   }

   /**
    * @param completed the completed to set
    */
   public void setCompleted(boolean completed) {
      this.completed = completed;
   }

}
