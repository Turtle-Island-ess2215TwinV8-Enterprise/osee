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
package org.eclipse.osee.framework.manager.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.osee.framework.core.data.OseeServerInfo;
import org.eclipse.osee.framework.core.server.IApplicationServerLookup;
import org.eclipse.osee.framework.core.server.IApplicationServerManager;
import org.eclipse.osee.framework.core.server.UnsecuredOseeHttpServlet;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.logger.Log;

/**
 * @author Roberto E. Escobar
 */
public class ServerLookupServlet extends UnsecuredOseeHttpServlet {

   private static final long serialVersionUID = -7055381632202456561L;

   private final IApplicationServerLookup lookupService;
   private final IApplicationServerManager applicationServerManager;

   public ServerLookupServlet(Log logger, IApplicationServerLookup lookupService, IApplicationServerManager applicationServerManager) {
      super(logger);
      this.lookupService = lookupService;
      this.applicationServerManager = applicationServerManager;
   }

   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      try {
         String version = request.getParameter("version");
         boolean wasBadRequest = false;

         OseeServerInfo info = null;
         if (Strings.isValid(version)) {
            version = version.trim();
            info = lookupService.getServerInfoBy(version);
         } else {
            wasBadRequest = true;
         }

         if (info == null) {
            response.setStatus(wasBadRequest ? HttpServletResponse.SC_BAD_REQUEST : HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
               String.format("Unable to locate application server matching version [%s]", version));
         } else {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            info.write(stream);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.setContentLength(stream.size());
            Lib.inputStreamToOutputStream(new ByteArrayInputStream(stream.toByteArray()), response.getOutputStream());
            response.getOutputStream().flush();
         }
      } catch (Exception ex) {
         getLogger().error(ex, "Failed to process application server lookup request [%s]", request.toString());
         response.getWriter().write(Lib.exceptionToString(ex));
      } finally {
         response.getWriter().flush();
         response.getWriter().close();
      }
   }

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      try {
         boolean isRegistrationToLookupTableRequested = Boolean.valueOf(request.getParameter("registerToLookup"));
         if (isRegistrationToLookupTableRequested) {
            boolean wasSuccessful = applicationServerManager.executeLookupRegistration();
            response.setStatus(wasSuccessful ? HttpServletResponse.SC_ACCEPTED : HttpServletResponse.SC_CONFLICT);
            response.setContentType("txt/plain");
            response.getWriter().write(
               String.format("Registration into server lookup was a [%s]", wasSuccessful ? "success" : "failure"));
         }
      } catch (Exception ex) {
         getLogger().error(ex, "Failed to process application server lookup request [%s]", request.toString());
         response.getWriter().write(Lib.exceptionToString(ex));
      }
      response.getWriter().flush();
      response.getWriter().close();
   }
}
