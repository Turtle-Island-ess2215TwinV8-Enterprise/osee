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

import java.util.Collections;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.eclipse.osee.x.ats.AtsApi;

/**
 * Get application.wadl at this context to get rest documentation
 * 
 * @author Roberto E. Escobar
 */
public class AtsRestApplication extends Application {

   private static AtsApi atsApi;

   public void setAtsApi(AtsApi atsApi) {
      AtsRestApplication.atsApi = atsApi;
   }

   public static AtsApi getAtsApi() {
      return atsApi;
   }

   @Override
   public Set<Class<?>> getClasses() {
      return Collections.<Class<?>> singleton(ProgramsResource.class);
   }

}
