/*
 * Created on Apr 27, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.event.model;

import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkSender;

public interface HasNetworkSender {

   NetworkSender getNetworkSender();

   void setNetworkSender(NetworkSender sender);
}