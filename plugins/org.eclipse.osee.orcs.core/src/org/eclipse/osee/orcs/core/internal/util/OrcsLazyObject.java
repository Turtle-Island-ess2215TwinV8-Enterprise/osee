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

import org.eclipse.osee.framework.core.data.LazyObject;
import org.eclipse.osee.orcs.core.ds.OrcsData;

/**
 * @author Roberto E. Escobar
 */
public abstract class OrcsLazyObject<T, D extends OrcsData> extends LazyObject<T> implements ValueProvider<T, D> {

   private D data;

   public OrcsLazyObject(D data) {
      super();
      this.data = data;
   }

   @Override
   public D getOrcsData() {
      return data;
   }

   @Override
   public void setOrcsData(D data) {
      invalidate();
      this.data = data;
   }

}
