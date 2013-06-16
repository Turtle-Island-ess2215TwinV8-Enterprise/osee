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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Ryan D. Brooks
 */
@Provider
public final class JaxRsExceptionMapper implements ExceptionMapper<Exception> {

   @Override
   public Response toResponse(Exception ex) {
      HtmlPageCreator html = new HtmlPageCreator();
      html.toExceptionPage(ex);
      return Response.serverError().entity(html.toString()).type(MediaType.TEXT_HTML_TYPE).build();
   }
}