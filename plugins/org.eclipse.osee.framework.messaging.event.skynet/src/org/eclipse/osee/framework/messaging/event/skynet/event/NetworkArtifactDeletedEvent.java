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
package org.eclipse.osee.framework.messaging.event.skynet.event;

/**
 * @author Robert A. Fisher
 */
public class NetworkArtifactDeletedEvent extends SkynetArtifactEventBase {
   private static final long serialVersionUID = 568951803773151575L;

   public NetworkArtifactDeletedEvent(int branchId, int transactionId, int artId, int artTypeId, String factoryName, NetworkSender networkSender) {
      super(branchId, transactionId, artId, artTypeId, factoryName, networkSender);
   }

   public NetworkArtifactDeletedEvent(SkynetArtifactEventBase base) {
      super(base);
   }
}