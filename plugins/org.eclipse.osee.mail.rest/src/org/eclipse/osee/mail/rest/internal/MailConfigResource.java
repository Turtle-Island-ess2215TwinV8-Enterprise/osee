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
package org.eclipse.osee.mail.rest.internal;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import org.eclipse.osee.mail.MailService;
import org.eclipse.osee.mail.MailServiceConfig;

/**
 * @author Roberto E. Escobar
 */
@Path("config")
public class MailConfigResource {

   private MailService getMailService() {
      return MailApplication.getMailService();
   }

   private MailServiceConfig getConfiguration() {
      return getMailService().getConfiguration();
   }

   @GET
   @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
   public MailServiceConfig getConfig() {
      return getConfiguration();
   }

   @GET
   @Produces(MediaType.TEXT_XML)
   public MailServiceConfig getConfigHtml() {
      return getConfiguration();
   }

   @POST
   @Consumes(MediaType.APPLICATION_XML)
   public Response updateConfig(JAXBElement<MailServiceConfig> jaxConfig) {
      MailServiceConfig config = jaxConfig.getValue();
      return postAndGetResponse(config);
   }

   private Response postAndGetResponse(MailServiceConfig config) {
      getMailService().setConfiguration(config);
      return Response.ok("Mail Service Configuration Updated", MediaType.TEXT_PLAIN).build();
   }
}
