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
package org.eclipse.osee.mail.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportListener;
import javax.mail.internet.MimeMessage;
import org.eclipse.osee.mail.MailMessage;
import org.eclipse.osee.mail.MailUtils;
import org.eclipse.osee.mail.SendMailStatus;
import org.eclipse.osee.mail.SendMailStatus.MailStatus;

/**
 * @author Roberto E. Escobar
 */
public class SendMailCallable implements Callable<SendMailStatus> {

   private final MailMessage email;
   private final MailMessageFactory factory;
   private final TransportListener[] listeners;
   private final long waitForStatus;
   private final TimeUnit timeUnit;

   public SendMailCallable(MailMessageFactory factory, MailMessage email, long waitForStatus, TimeUnit timeUnit, TransportListener... listeners) {
      this.factory = factory;
      this.email = email;
      this.listeners = listeners;
      this.waitForStatus = waitForStatus;
      this.timeUnit = timeUnit;
   }

   @Override
   public SendMailStatus call() throws Exception {
      MailcapCommandMap mc = MailUtils.getMailcapCommandMap();
      CommandMap.setDefaultCommandMap(mc);

      final Session session = factory.createSession();
      final MimeMessage message = factory.createMimeMessage(session, email);
      final Transport transport = factory.createTransport(session);
      final SendMailStatus status = new SendMailStatus();

      StatusTransportListener statusListener = new StatusTransportListener(status);
      transport.addTransportListener(statusListener);
      if (listeners != null) {
         for (TransportListener listener : listeners) {
            transport.addTransportListener(listener);
         }
      }
      try {
         message.saveChanges();
         transport.sendMessage(message, message.getAllRecipients());
         synchronized (statusListener) {
            long waitMillis = timeUnit.toMillis(waitForStatus);
            statusListener.wait(waitMillis);
         }
      } finally {
         if (listeners != null) {
            for (TransportListener listener : listeners) {
               transport.removeTransportListener(listener);
            }
         }
         transport.removeTransportListener(statusListener);
         transport.close();
      }
      if (waitForStatus <= 0 || !statusListener.wasUpdateReceived()) {
         MailStatus mStatus = new MailStatus();
         mStatus.setDateSent(message.getSentDate());
         mStatus.setSubject(message.getSubject());
         mStatus.setUuid(message.getMessageID());
         mStatus.setVerified(false);
         status.add(mStatus);
      }
      return status;
   }
}
