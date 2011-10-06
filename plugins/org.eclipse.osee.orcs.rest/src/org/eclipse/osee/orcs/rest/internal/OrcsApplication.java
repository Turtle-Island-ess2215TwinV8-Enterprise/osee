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

import java.util.Collections;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.eclipse.osee.orcs.OseeApi;

/**
 * Get application.wadl at this context to get rest documentation
 * 
 * @author Roberto E. Escobar
 */
public class OrcsApplication extends Application {

   private static OseeApi oseeApi;

   public void setOseeApi(OseeApi oseeApi) {
      OrcsApplication.oseeApi = oseeApi;
   }

   public static OseeApi getOseeApi() {
      return oseeApi;
   }

   @Override
   public Set<Class<?>> getClasses() {
      return Collections.<Class<?>> singleton(BranchesResource.class);
   }

}
