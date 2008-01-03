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
package org.eclipse.osee.framework.ui.skynet.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.jdk.core.util.AEmail;
import org.eclipse.osee.framework.skynet.core.SkynetAuthentication;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;

/**
 * @author Donald G. Dunne
 */
public class EmailableJob extends Job {

   private Collection<User> notifyUsers = new HashSet<User>();

   /**
    * @param name
    */
   public EmailableJob(String name) {
      super(name);
   }

   /**
    * Called by the extending job that the job is complete
    * 
    * @param htmlBody
    */
   protected void notifyOfCompletion(String subject, String htmlBody) {
      if (notifyUsers.size() > 0) {
         Set<String> emails = new HashSet<String>();
         for (User user : notifyUsers)
            emails.add(user.getEmail());
         AEmail emailMessage =
               new AEmail(emails.toArray(new String[emails.size()]),
                     SkynetAuthentication.getInstance().getAuthenticatedUser().getEmail(),
                     SkynetAuthentication.getInstance().getAuthenticatedUser().getEmail(), subject);
         try {
            emailMessage.setSubject(subject);
            emailMessage.addHTMLBody(htmlBody);
            emailMessage.send();
         } catch (Exception ex) {
            OSEELog.logException(SkynetGuiPlugin.class, "Your Email Message could not be sent.", ex, true);
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   protected IStatus run(IProgressMonitor monitor) {
      return null;
   }

   public Collection<User> getNotifyUsers() {
      return notifyUsers;
   }

   public void setNotifyUsers(Collection<User> notifyUsers) {
      this.notifyUsers = notifyUsers;
   }

}
