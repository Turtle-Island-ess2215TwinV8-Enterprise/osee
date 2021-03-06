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
package org.eclipse.osee.ote.rest.internal.mappers;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.ote.rest.model.ExceptionEntity;

/**
 * @author John Misinco
 */
@Provider
public class OseeCoreExceptionMapper implements ExceptionMapper<OseeCoreException> {

   @Override
   public Response toResponse(OseeCoreException exception) {
      ExceptionEntity entity = new ExceptionEntity(Lib.exceptionToString(exception));
      GenericEntity<ExceptionEntity> ge = new GenericEntity<ExceptionEntity>(entity, ExceptionEntity.class);
      return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(ge).build();
   }

}
