/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.x.ats.core.internal;

import java.util.Date;
import org.eclipse.osee.ats.shared.LogType;
import org.eclipse.osee.x.ats.data.LogItem;

/**
 * @author Donald G. Dunne
 */
public class LogItemImpl implements LogItem {

   private final Date date;
   private final String userId;
   private final String msg;
   private final String state;
   private final LogType type;

   public LogItemImpl(Date date, String userId, String msg, String state, LogType type) {
      super();
      this.date = date;
      this.userId = userId;
      this.msg = msg;
      this.state = state;
      this.type = type;
   }

   @Override
   public Date getDate() {
      return date;
   }

   @Override
   public String getUserId() {
      return userId;
   }

   @Override
   public String getMessage() {
      return msg;
   }

   @Override
   public String getState() {
      return state;
   }

   @Override
   public LogType getType() {
      return type;
   }

   //   public LogItem(LogType type, Date date, IBasicUser user, String state, String msg, String hrid) throws OseeCoreException {
   //      this(type.name(), String.valueOf(date.getTime()), user.getUserId(), state, msg, hrid);
   //   }
   //
   //   public LogItem(LogType type, String date, String userId, String state, String msg, String hrid) throws OseeCoreException {
   //      Long dateLong = Long.valueOf(date);
   //      this.date = new Date(dateLong.longValue());
   //      this.msg = msg;
   //      this.state = intern(state);
   //      this.userId = intern(userId);
   //      try {
   //         this.user = UserManager.getUserByUserId(userId);
   //      } catch (UserNotInDatabase ex) {
   //         this.user = UserManager.getUser(SystemUser.Guest);
   //         OseeLog.logf(Activator.class, Level.SEVERE,
   //            ex, "Error parsing ATS Log for %s - %s", hrid, ex.getLocalizedMessage());
   //      }
   //      this.type = type;
   //   }
   //
   //   public LogItem(String type, String date, String userId, String state, String msg, String hrid) throws OseeCoreException {
   //      this(LogType.getType(type), date, userId, state, msg, hrid);
   //   }
   //
   //   public Date getDate() {
   //      return date;
   //   }
   //
   //   public String getDate(String pattern) {
   //      if (pattern != null) {
   //         return new SimpleDateFormat(pattern, Locale.US).format(date);
   //      }
   //      return date.toString();
   //   }
   //
   //   public void setDate(Date date) {
   //      this.date = date;
   //   }
   //
   //   public void setMsg(String msg) {
   //      this.msg = msg;
   //   }
   //
   //   @Override
   //   public String toString() {
   //      return String.format("%s (%s)%s by %s on %s", getToStringMsg(), type, getToStringState(), getToStringUser(),
   //         DateUtil.getMMDDYYHHMM(date));
   //   }
   //
   //   private String getToStringUser() {
   //      return user == null ? "unknown" : user.getName();
   //   }
   //
   //   private String getToStringState() {
   //      return state.isEmpty() ? "" : "from " + state;
   //   }
   //
   //   private String getToStringMsg() {
   //      return msg.isEmpty() ? "" : msg;
   //   }
   //
   //   public void setType(LogType type) {
   //      this.type = type;
   //   }
   //
   //   public String toHTML(String labelFont) {
   //      return "NOTE (" + type + "): " + msg + " (" + user.getName() + ")";
   //   }
   //
   //   public void setUser(IBasicUser user) {
   //      this.user = user;
   //   }
   //
   //
   //   public void setState(String state) {
   //      this.state = state;
   //   }

}
