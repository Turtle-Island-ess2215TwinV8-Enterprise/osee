/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.x.ats.core.internal;

import org.eclipse.osee.event.EventService;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.x.ats.AtsApi;
import org.eclipse.osee.x.ats.AtsReportFactory;
import org.eclipse.osee.x.ats.query.AtsQuery;

/**
 * @author Roberto E. Escobar
 */
public class AtsApiImpl implements AtsApi {

   private Log logger;
   private OrcsApi orcsApi;
   private EventService eventService;

   public void setLogger(Log logger) {
      this.logger = logger;
   }

   public Log getLogger() {
      return logger;
   }

   public void setOrcsApi(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   public OrcsApi getOrcsApi() {
      return this.orcsApi;
   }

   public void setEventService(EventService eventService) {
      this.eventService = eventService;
   }

   public EventService getEventService() {
      return eventService;
   }

   public void start() {
      //
      getLogger().info("AtsApi started");
   }

   public void stop() {
      //
      getLogger().info("AtsApi stopped");
   }

   @Override
   public AtsQuery getQuery() {
      OrcsApi orcsApi = getOrcsApi();
      return new AtsQueryImpl(orcsApi);
   }

   @Override
   public AtsReportFactory getReportFactory() {
      return null;
   }

}
