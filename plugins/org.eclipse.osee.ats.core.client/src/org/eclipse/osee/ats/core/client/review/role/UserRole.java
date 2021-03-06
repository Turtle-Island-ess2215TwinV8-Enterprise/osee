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
package org.eclipse.osee.ats.core.client.review.role;

import java.text.NumberFormat;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AXml;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;

/**
 * @author Donald G. Dunne
 */
public class UserRole {

   private Role role = Role.Reviewer;
   private String userId;
   private Double hoursSpent = null;
   private String guid = GUID.create();
   private Boolean completed = false;

   public UserRole() throws OseeCoreException {
      this(Role.Reviewer, AtsClientService.get().getUserAdmin().getCurrentUser().getUserId(), null, false);
   }

   public UserRole(Role role, IAtsUser user) throws OseeCoreException {
      this(role, user.getUserId());
   }

   public UserRole(Role role, String userId) {
      this(role, userId, 0.0, false);
   }

   public UserRole(Role role, IAtsUser user, Double hoursSpent, Boolean completed) throws OseeCoreException {
      this(role, user.getUserId(), hoursSpent, completed);
   }

   public UserRole(Role role, String userId, Double hoursSpent, Boolean completed) {
      this.role = role;
      this.userId = userId;
      this.hoursSpent = hoursSpent;
      this.completed = completed;
   }

   public UserRole(String xml) {
      fromXml(xml);
   }

   public void update(UserRole dItem) {
      fromXml(dItem.toXml());
   }

   public String toXml() {
      StringBuffer sb = new StringBuffer();
      sb.append(AXml.addTagData("role", role.name()));
      sb.append(AXml.addTagData("userId", userId));
      sb.append(AXml.addTagData("hoursSpent", hoursSpent == null ? "" : String.valueOf(hoursSpent)));
      sb.append(AXml.addTagData("completed", String.valueOf(completed)));
      sb.append(AXml.addTagData("guid", guid));
      return sb.toString();
   }

   public void fromXml(String xml) {
      try {
         this.role = Role.valueOf(AXml.getTagData(xml, "role"));
         this.userId = AXml.getTagData(xml, "userId");
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
         OseeLog.log(Activator.class, Level.SEVERE, ex);
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
      return role + " - " + userId + " - " + hoursSpent + " - " + (completed ? "Completed" : "InWork");
   }

   public Role getRole() {
      return role;
   }

   public void setRole(Role role) {
      this.role = role;
   }

   public IAtsUser getUser() throws OseeCoreException {
      return AtsClientService.get().getUserAdmin().getUserById(userId);
   }

   public void setUser(User user) throws OseeCoreException {
      this.userId = user.getUserId();
   }

   public Double getHoursSpent() {
      return hoursSpent;
   }

   public String getHoursSpentStr() {
      return hoursSpent == null ? "" : AtsUtilCore.doubleToI18nString(hoursSpent, true);
   }

   public void setHoursSpent(Double hoursSpent) {
      this.hoursSpent = hoursSpent;
   }

   public String getGuid() {
      return guid;
   }

   public void setGuid(String guid) {
      this.guid = guid;
   }

   public boolean isCompleted() {
      return completed;
   }

   public void setCompleted(boolean completed) {
      this.completed = completed;
   }

   public String getUserId() {
      return userId;
   }
}
