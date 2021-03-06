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

import java.util.Collection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.cache.BranchFilter;

/**
 * @author Roberto E. Escobar
 */
@Path("branch")
public class BranchesResource {

   // Allows to insert contextual objects into the class, 
   // e.g. ServletContext, Request, Response, UriInfo
   @Context
   UriInfo uriInfo;
   @Context
   Request request;

   @Path("{uuid}")
   public BranchResource getBranch(@PathParam("uuid") String id) {
      return new BranchResource(uriInfo, request, id);
   }

   @GET
   @Produces(MediaType.TEXT_HTML)
   public String getAsHtml() throws OseeCoreException {
      Collection<Branch> branches =
         OrcsApplication.getOrcsApi().getBranchCache().getBranches(
            new BranchFilter(BranchArchivedState.UNARCHIVED, BranchType.BASELINE, BranchType.WORKING));//getAll();
      HtmlWriter writer = new HtmlWriter(uriInfo);
      return writer.toHtml(branches);
   }
}
