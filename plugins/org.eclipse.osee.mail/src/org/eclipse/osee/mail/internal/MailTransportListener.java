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

import java.util.Map;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import org.eclipse.osee.event.EventService;
import org.eclipse.osee.mail.MailConstants;
import org.eclipse.osee.mail.MailEventUtil;

/**
 * @author Roberto E. Escobar
 */
public final class MailTransportListener implements TransportListener {

   private final EventService eventService;

   public MailTransportListener(EventService eventService) {
      this.eventService = eventService;
   }

   @Override
   public void messageDelivered(TransportEvent event) {
      Map<String, String> data = MailEventUtil.createTransportEventData(event);
      eventService.postEvent(MailConstants.MAIL_MESSAGE_DELIVERED, data);
   }

   @Override
   public void messageNotDelivered(TransportEvent event) {
      Map<String, String> data = MailEventUtil.createTransportEventData(event);
      eventService.postEvent(MailConstants.MAIL_MESSAGE_NOT_DELIVERED, data);
   }

   @Override
   public void messagePartiallyDelivered(TransportEvent event) {
      Map<String, String> data = MailEventUtil.createTransportEventData(event);
      eventService.postEvent(MailConstants.MAIL_MESSAGE_PARTIALLY_DELIVERED, data);
   }

}