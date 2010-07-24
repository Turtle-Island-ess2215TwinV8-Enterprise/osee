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
package org.eclipse.osee.framework.messaging.services.internal;

import java.net.URI;
import java.util.logging.Level;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.messaging.ConnectionNode;
import org.eclipse.osee.framework.messaging.services.BaseMessages;
import org.eclipse.osee.framework.messaging.services.ServiceInfoPopulator;
import org.eclipse.osee.framework.messaging.services.messages.ServiceHealth;

/**
 * @author Andrew M. Finkbeiner
 *
 */
public class UpdateStatus implements Runnable {

	private ConnectionNode connectionNode;
	private ServiceInfoPopulator infoPopulator;
	private ServiceHealth health;
	private String errorMsg;
	
	UpdateStatus(ConnectionNode connectionNode, String serviceName, String serviceVersion, String serviceUniqueId, URI broker, int refreshRateInSeconds, ServiceInfoPopulator infoPopulator){
		this.connectionNode = connectionNode;
		health = new ServiceHealth();
		health.setBrokerURI(broker.toASCIIString());
		health.setServiceName(serviceName);
		health.setServiceVersion(serviceVersion);
		health.setServiceUniqueId(serviceUniqueId);
		health.setRefreshRateInSeconds(refreshRateInSeconds);
		health.setStopping(false);
		errorMsg = String.format("Failed to send %s to %s v[%s][%s]", BaseMessages.ServiceHealth.getName(), health.getServiceName(),
				health.getServiceVersion(), health.getServiceUniqueId());
		this.infoPopulator = infoPopulator;
	}
	
	@Override
	public synchronized void run() {
		try {
			health.getServiceDescription().clear();
			infoPopulator.updateServiceInfo(health.getServiceDescription());
			connectionNode.send(BaseMessages.ServiceHealth, health, new OseeMessagingStatusImpl(errorMsg, UpdateStatus.class));
		} catch (Exception ex) {
			OseeLog.log(UpdateStatus.class, Level.SEVERE, ex);
		}
	}

   public synchronized void close() {
      health.setStopping(true);
      run();
   }

}
