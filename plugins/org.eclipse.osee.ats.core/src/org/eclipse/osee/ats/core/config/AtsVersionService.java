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
package org.eclipse.osee.ats.core.config;

import org.eclipse.osee.ats.api.version.IAtsVersionService;
import org.eclipse.osee.ats.api.version.IAtsVersionServiceProvider;

/**
 * @author Donald G. Dunne
 */
public class AtsVersionService {

   private static IAtsVersionServiceProvider service;

   public static IAtsVersionService get() {
      if (AtsVersionService.service == null) {
         throw new IllegalStateException("ATS Version Service has not been activated");
      }
      return AtsVersionService.service.getAtsVersionService();
   }

   public void setAtsVersionServiceProvider(IAtsVersionServiceProvider service) {
      AtsVersionService.service = service;
   }

}
