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
package org.eclipse.osee.display.view.web;

import org.eclipse.osee.display.mvp.MessageType;
import org.eclipse.osee.display.mvp.MessageTypeEnum;
import org.eclipse.osee.display.mvp.view.AbstractView;
import com.vaadin.Application;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Window.Notification;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractVaadinView extends AbstractView {

   protected AbstractVaadinView() {
      super();
   }

   @Override
   protected void onDispose() {
      //
   }

   @Override
   public abstract ComponentContainer getContent();

   protected Application getApplication() {
      return getContent().getApplication();
   }

   @Override
   public void displayMessage(String caption) {
      getApplication().getMainWindow().showNotification(caption);
   }

   @Override
   public void displayMessage(String caption, String description, MessageType messageType) {
      int type = toNotificationType(messageType);
      getApplication().getMainWindow().showNotification(caption, description, type);
   }

   protected int toNotificationType(MessageType messageType) {
      int type = Notification.TYPE_HUMANIZED_MESSAGE;
      MessageTypeEnum typeEnum = MessageTypeEnum.fromMessageType(messageType);
      switch (typeEnum) {
         case ERROR:
            type = Notification.TYPE_ERROR_MESSAGE;
            break;
         case WARNING:
            type = Notification.TYPE_WARNING_MESSAGE;
            break;
         case INFORMATION:
            type = Notification.TYPE_TRAY_NOTIFICATION;
            break;
         default:
            type = Notification.TYPE_HUMANIZED_MESSAGE;
            break;
      }
      return type;
   }
}
