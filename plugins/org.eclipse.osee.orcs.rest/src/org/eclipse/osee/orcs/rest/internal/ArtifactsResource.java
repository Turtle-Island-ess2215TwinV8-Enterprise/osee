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

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.GraphReadable;
import org.eclipse.osee.orcs.search.QueryFactory;

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

   @Path("search")
   public ArtifactSearchResource getSearch() {
      return new ArtifactSearchResource(uriInfo, request, branchUuid);
   }

   @Path("{uuid}")
   public ArtifactResource getArtifact(@PathParam("uuid") String artifactUuid) {
      return new ArtifactResource(uriInfo, request, branchUuid, artifactUuid);
   }

   @GET
   @Produces(MediaType.TEXT_HTML)
   public String getAsHtml() throws OseeCoreException {
      IOseeBranch branch = TokenFactory.createBranch(branchUuid, "");
      QueryFactory factory = OrcsApplication.getOrcsApi().getQueryFactory(null);
      ResultSet<ArtifactReadable> results =
         factory.fromBranch(branch).andNameEquals(DEFAULT_HIERARCHY_ROOT_NAME).getResults();
      ArtifactReadable rootArtifact = results.getExactlyOne();

      GraphReadable graph = OrcsApplication.getOrcsApi().getGraph(null);
      List<ArtifactReadable> arts = graph.getChildren(rootArtifact).getList();
      HtmlWriter writer = new HtmlWriter(uriInfo);
      return writer.toHtml(arts);
   }
   private static final String DEFAULT_HIERARCHY_ROOT_NAME = "Default Hierarchy Root";
}
