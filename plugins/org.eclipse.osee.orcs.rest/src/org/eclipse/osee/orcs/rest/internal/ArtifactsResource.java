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
package org.eclipse.osee.orcs.rest.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactsResource {

   // Allows to insert contextual objects into the class, 
   // e.g. ServletContext, Request, Response, UriInfo
   @Context
   UriInfo uriInfo;
   @Context
   Request request;

   String branchUuid;

   public ArtifactsResource(UriInfo uriInfo, Request request, String branchUuid) {
      this.uriInfo = uriInfo;
      this.request = request;
      this.branchUuid = branchUuid;
   }

   @Path("{uuid}")
   public ArtifactResource getArtifact(@PathParam("uuid") String artifactUuid) {
      return new ArtifactResource(uriInfo, request, branchUuid, artifactUuid);
   }

   @GET
   @Produces(MediaType.TEXT_PLAIN)
   public String getAllBranchesAsText() {
      return String.format("All artifacts at branch uuid[%s]", branchUuid);
   }
}
