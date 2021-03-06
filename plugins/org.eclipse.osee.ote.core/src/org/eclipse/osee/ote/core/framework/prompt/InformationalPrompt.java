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
package org.eclipse.osee.ote.core.framework.prompt;

import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import org.eclipse.osee.connection.service.IServiceConnector;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.ote.core.IUserSession;
import org.eclipse.osee.ote.core.environment.TestEnvironment;

/**
 * @author Ken J. Aguilar
 */
public class InformationalPrompt extends AbstractRemotePrompt {

   public InformationalPrompt(IServiceConnector connector, String id, String message) throws UnknownHostException {
      super(connector, id, message);
   }

   public void open(final IUserSession session, Executor executor) throws Exception {
      if (executor != null) {
         executor.execute(new Runnable() {

            @Override
            public void run() {
               try {
                  String message = getPromptMessage();
                  if (message != null) {
                     session.initiateInformationalPrompt(getPromptMessage());
                  } else {
                     session.initiateInformationalPrompt("null message");
                  }
               } catch (Exception e) {
                  OseeLog.log(TestEnvironment.class, Level.SEVERE, "exception while performing informational prompt", e);

               }
            }
         });
      } else {
         session.initiateInformationalPrompt(getPromptMessage());
      }
   }

   @Override
   public void close() {

   }

}
