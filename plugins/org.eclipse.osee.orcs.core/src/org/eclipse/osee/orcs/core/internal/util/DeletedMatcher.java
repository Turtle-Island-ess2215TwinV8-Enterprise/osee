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

import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.orcs.data.HasDeleteState;

/**
 * @author Roberto E. Escobar
 */
public class DeletedMatcher<T extends HasDeleteState> extends DataMatcher<T> {

   private final boolean checkNeeded;

   public DeletedMatcher(DeletionFlag includeDeleted) {
      this.checkNeeded = !includeDeleted.areDeletedAllowed();
   }

   @Override
   public boolean accept(T data) {
      boolean result = true;
      if (checkNeeded) {
         result = !data.isDeleted();
      }
      return result;
   }
}
