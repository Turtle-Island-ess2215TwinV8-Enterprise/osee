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

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;

/**
 * @author Roberto E. Escobar
 */
public class AtsException extends OseeCoreException {

   private static final long serialVersionUID = -7326541420696736796L;

   public AtsException(String message, Object... args) {
      super(formatMessage(message, args));
   }

   public AtsException(Throwable cause, String message) {
      super(formatMessage(message), cause);
   }

   public AtsException(Throwable cause, String message, Object... args) {
      super(formatMessage(message, args), cause);
   }

   public AtsException(Throwable cause) {
      super(cause);
   }

   private static String formatMessage(String message, Object... args) {
      try {
         return String.format(message, args);
      } catch (RuntimeException ex) {
         return String.format(
            "Exception message could not be formatted: [%s] with the following arguments [%s].  Cause [%s]", message,
            Collections.toString(",", args), ex.toString());
      }
   }
}
