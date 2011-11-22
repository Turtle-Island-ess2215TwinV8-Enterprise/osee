/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.x.ats;

/**
 * @author Roberto E. Escobar
 */
public final class AtsConstants {

   private AtsConstants() {
      // Constants class
   }

   private static final String PREFIX = "org/eclipse/osee/x/ats/event/";

   public static final String REGISTRATION_EVENT = PREFIX + "ATS_SERVICE_REGISTRATION";
   public static final String DEREGISTRATION_EVENT = PREFIX + "ATS_SERVICE_DEREGISTRATION";

}
