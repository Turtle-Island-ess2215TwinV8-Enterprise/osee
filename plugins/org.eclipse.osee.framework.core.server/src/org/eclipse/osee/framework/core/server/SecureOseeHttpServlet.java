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
package org.eclipse.osee.framework.core.server;

import javax.servlet.http.HttpServletRequest;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.logger.Log;

/**
 * @author Roberto E. Escobar
 */
public class SecureOseeHttpServlet extends OseeHttpServlet {

   private static final long serialVersionUID = -4034231476048459552L;
   private final ISessionManager sessionManager;

   public SecureOseeHttpServlet(Log logger, ISessionManager sessionManager) {
      super(logger);
      this.sessionManager = sessionManager;
   }

   protected ISessionManager getSessionManager() {
      return sessionManager;
   }

   @Override
   protected void checkAccessControl(HttpServletRequest request) throws OseeCoreException {
      String sessionId = getSessionId(request);
      String interaction =
         String.format("%s %s %s", request.getMethod(), request.getRequestURI(), request.getQueryString());
      sessionManager.updateSessionActivity(sessionId, interaction);
   }

   public boolean isInitializing(HttpServletRequest request) throws OseeCoreException {
      String sessionId = getSessionId(request);
      ISession session = sessionManager.getSessionById(sessionId);
      String userId = session.getUserId();
      return SystemUser.BootStrap.getUserId().equals(userId);
   }

   protected String getSessionId(HttpServletRequest request) {
      return request.getParameter("sessionId");
   }

}
