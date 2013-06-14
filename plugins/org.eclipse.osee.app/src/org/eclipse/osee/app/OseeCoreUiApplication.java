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

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.eclipse.osee.orcs.OrcsApi;

/**
 * @author Ryan D. Brooks
 */
public final class OseeCoreUiApplication extends Application {
   private OrcsApi orcsApi;

   public void setOrcsApi(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   @Override
   public Set<Class<?>> getClasses() {
      Set<Class<?>> classes = new HashSet<Class<?>>();
      return classes;
   }

   @Override
   public Set<Object> getSingletons() {
      Set<Object> singletons = new HashSet<Object>();
      singletons.add(new JaxRsExceptionMapper());
      singletons.add(new OseeAppsResource(orcsApi));
      singletons.add(new OseeDashboardResource());
      return singletons;
   }
}