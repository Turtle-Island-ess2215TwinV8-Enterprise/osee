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

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.eclipse.osee.x.ats.AtsException;
import org.eclipse.osee.x.ats.AtsGraph;
import org.eclipse.osee.x.ats.data.Product;
import org.eclipse.osee.x.ats.data.Version;

/**
 * @author Roberto E. Escobar
 */
@Path("program")
public class ProgramsResource {

   // Allows to insert contextual objects into the class,
   // e.g. ServletContext, Request, Response, UriInfo
   @Context
   UriInfo uriInfo;
   @Context
   Request request;

   @Path("{uuid}")
   public ProgramResource getProgram(@PathParam("uuid") String id) {
      return new ProgramResource(uriInfo, request, id);
   }

   @GET
   @Produces(MediaType.TEXT_HTML)
   public String getAsHtml() throws AtsException {
      AtsGraph graph = AtsRestApplication.getAtsApi().getGraph();
      List<Product> products = graph.getProducts();

      StringBuilder builder = new StringBuilder();
      builder.append("<html><body>");
      for (Product product : products) {
         builder.append("<br/>Product: ");
         builder.append(product.getName());
         builder.append("<br/>Version:");
         for (Version version : product.getVersions()) {
            builder.append("<br/>");
            builder.append(version.getName());
         }
      }
      builder.append("</body></html>");
      return builder.toString();
   }
}
