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
package org.eclipse.osee.x.ats.rest.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

/**
 * @author Roberto E. Escobar
 */
public class ProgramResource {

   @Context
   UriInfo uriInfo;
   @Context
   Request request;

   String programUuid;

   public ProgramResource(UriInfo uriInfo, Request request, String programUuid) {
      this.uriInfo = uriInfo;
      this.request = request;
      this.programUuid = programUuid;
   }

   @GET
   @Produces(MediaType.TEXT_HTML)
   public String getAsHtml() {
      return "Single program reported here";
   }
}
