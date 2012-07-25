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
import org.eclipse.osee.framework.core.model.AbstractOseeType;
import org.eclipse.osee.framework.core.model.cache.AbstractOseeCache;
import org.eclipse.osee.orcs.core.ds.OrcsData;

/**
 * @author Roberto E. Escobar
 */
public class LazyTypeProvider<T extends AbstractOseeType<Long>, D extends OrcsData> extends OrcsLazyObject<T, D> {

   private final AbstractOseeCache<Long, T> cache;

   public LazyTypeProvider(AbstractOseeCache<Long, T> cache, D data) {
      super(data);
      this.cache = cache;
   }

   @Override
   protected T instance() throws OseeCoreException {
      return cache.getByGuid(getOrcsData().getTypeUuid());
   }

}
