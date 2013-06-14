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
package org.eclipse.osee.orcs.core.ds;

import java.util.Collection;
import org.eclipse.osee.framework.core.enums.OseeCacheEnum;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.IOseeCache;
import org.eclipse.osee.framework.core.model.cache.TransactionCache;
import org.eclipse.osee.framework.core.services.IBranchCacheService;

/**
 * @author Roberto E. Escobar
 */
public interface TempCachingService extends IBranchCacheService {

   TransactionCache getTransactionCache();

   Collection<?> getCaches();

   IOseeCache<?, ?> getCache(OseeCacheEnum cacheId) throws OseeCoreException;

   void reloadAll() throws OseeCoreException;

   void clearAll();
}
