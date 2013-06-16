/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.app;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.osee.orcs.OrcsApi;

/**
 * @author Ryan D. Brooks
 */
@Path("apps")
public final class OseeAppsResource {
   private final OrcsApi orcsApi;

   public OseeAppsResource(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   @GET
   @Produces(MediaType.TEXT_HTML)
   public String getAppletList() {
      return "list all applets here as links";
   }

   @Path("{appName}")
   public OseeAppResource getApplet(@PathParam("appName") String appletName) {
      return new OseeAppResource(appletName, orcsApi);
   }
}