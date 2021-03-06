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
package org.eclipse.osee.framework.core.message.internal.translation;

import org.eclipse.osee.framework.core.message.BranchCacheUpdateResponse;
import org.eclipse.osee.framework.core.message.BranchCacheUpdateUtil;
import org.eclipse.osee.framework.core.translation.ITranslator;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;

/**
 * @author Roberto E. Escobar
 * @author Jeff C. Phillips
 */
public class BranchCacheUpdateResponseTranslator implements ITranslator<BranchCacheUpdateResponse> {

   @Override
   public BranchCacheUpdateResponse convert(PropertyStore store) {
      BranchCacheUpdateResponse response = new BranchCacheUpdateResponse();
      BranchCacheUpdateUtil.loadMessage(response, store);
      return response;
   }

   @Override
   public PropertyStore convert(BranchCacheUpdateResponse object) {
      PropertyStore store = new PropertyStore();
      BranchCacheUpdateUtil.loadStore(store, object);
      return store;
   }
}
