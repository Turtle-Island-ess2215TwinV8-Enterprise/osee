/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ote.message.internal;

import java.util.logging.Level;

import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.ote.message.interfaces.IMessageManager;
import org.eclipse.osee.ote.message.interfaces.IRemoteMessageService;
import org.eclipse.osee.ote.message.tool.AbstractMessageToolService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public class MessageWatchActivator extends ServiceTracker {

   private ServiceRegistration registration;

   public MessageWatchActivator(BundleContext context) {
      super(context, IMessageManager.class.getName(), null);
   }

   @Override
   public synchronized IMessageManager addingService(ServiceReference reference) {
      IMessageManager manager = (IMessageManager) super.addingService(reference);
      try {
         AbstractMessageToolService toolService = new AbstractMessageToolService(manager);
         registration = context.registerService(IRemoteMessageService.class.getName(), toolService, null);
      } catch (Exception e) {
         OseeLog.log(MessageWatchActivator.class, Level.SEVERE, "!!!failed to create message tool service", e);
      }
      return manager;
   }

   @Override
   public synchronized void removedService(ServiceReference reference, Object service) {
      disposeToolService();
      super.removedService(reference, service);
   }

   private void disposeToolService() {
      try {
         AbstractMessageToolService toolService =
            (AbstractMessageToolService) context.getService(registration.getReference());
         toolService.terminateService();
      } finally {
         registration.unregister();
         registration = null;
      }
   }

   @Override
   public synchronized void close() {
      if (registration != null) {
         disposeToolService();
      }
      super.close();
   }
}