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
package org.eclipse.osee.ote.core.environment;

import java.util.Properties;
import org.eclipse.osee.framework.messaging.EndpointReceive;
import org.eclipse.osee.framework.messaging.Message;

/**
 * @author Andrew M. Finkbeiner
 */
public class OteServerSideEndprointRecieve extends EndpointReceive {

   @Override
   public void start(Properties properties) {
   }

   public void recievedMessage(Message message) {
      onReceive(message);
   }

   @Override
   public void dispose() {
   }

}
