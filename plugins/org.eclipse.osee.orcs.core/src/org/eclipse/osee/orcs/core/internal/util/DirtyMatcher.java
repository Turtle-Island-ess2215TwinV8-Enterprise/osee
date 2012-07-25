/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.util;

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.data.Modifiable;

/**
 * @author Roberto E. Escobar
 */
public class DirtyMatcher<T extends Modifiable> extends DataMatcher<T> {

   public static enum DirtyFlag {
      DIRTY,
      NON_DIRTY,
   }

   private final DirtyFlag dirtyFlag;

   public DirtyMatcher(DirtyFlag includeDirty) {
      this.dirtyFlag = includeDirty;
   }

   @Override
   public boolean accept(T data) throws OseeCoreException {
      boolean result = true;
      if (dirtyFlag == DirtyFlag.DIRTY) {
         result = data.isDirty();
      } else if (dirtyFlag == DirtyFlag.NON_DIRTY) {
         result = !data.isDirty();
      }
      return result;
   }
}
