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
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Donald G. Dunne
 */
@Path("dashboard")
public final class OseeDashboardResource {

   @GET
   @Produces(MediaType.TEXT_HTML)
   public String getDashboard() throws Exception {
      HtmlPageCreator html = new HtmlPageCreator();
      addUser(html);
      html.addSubstitution(new NavigativeItemRule("navListItems"));
      html.addSubstitution(new ServerStatusRule("serverStatus"));
      return html.realizePageAsString("ui/Dashboard.html");
   }

   private void addUser(HtmlPageCreator html) {
      // TODO retrieve user from application server / authentication
      String user = System.getProperty("user.name");
      html.addStringSubstitution("user", user);
   }
}