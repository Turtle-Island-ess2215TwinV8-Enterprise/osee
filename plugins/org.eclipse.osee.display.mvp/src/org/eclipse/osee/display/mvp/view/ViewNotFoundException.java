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
package org.eclipse.osee.display.mvp.view;

import org.eclipse.osee.display.mvp.internal.BaseException;

/**
 * @author Roberto E. Escobar
 */
public class ViewNotFoundException extends BaseException {

   private static final long serialVersionUID = 3353674767911593102L;

   public ViewNotFoundException(String message, Object... args) {
      super(message, args);
   }

   public ViewNotFoundException(Throwable cause, String message, Object... args) {
      super(cause, message, args);
   }

   public ViewNotFoundException(Throwable cause) {
      super(cause);
   }

}