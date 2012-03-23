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
package org.eclipse.osee.orcs.core.internal.search;

import org.eclipse.osee.executor.admin.CancellableCallable;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.QueryData;
import org.eclipse.osee.orcs.core.ds.QueryEngine;
import org.eclipse.osee.orcs.core.internal.OrcsObjectLoader;
import org.eclipse.osee.orcs.core.internal.SessionContext;
import org.eclipse.osee.orcs.core.internal.search.callable.SearchCallable;
import org.eclipse.osee.orcs.core.internal.search.callable.SearchCountCallable;
import org.eclipse.osee.orcs.core.internal.search.callable.SearchMatchesCallable;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.data.ReadableAttribute;
import org.eclipse.osee.orcs.search.Match;

/**
 * @author Roberto E. Escobar
 */
public class CallableQueryFactory {

   private final Log logger;
   private final QueryEngine queryEngine;
   private final OrcsObjectLoader objectLoader;
   private final QueryCollector collector;

   public CallableQueryFactory(Log logger, QueryEngine queryEngine, QueryCollector collector, OrcsObjectLoader objectLoader) {
      super();
      this.logger = logger;
      this.queryEngine = queryEngine;
      this.objectLoader = objectLoader;
      this.collector = collector;
   }

   public CancellableCallable<Integer> createCount(SessionContext sessionContext, QueryData queryData) {
      return new SearchCountCallable(logger, queryEngine, collector, objectLoader, sessionContext, LoadLevel.ATTRIBUTE,
         queryData);
   }

   public CancellableCallable<ResultSet<ReadableArtifact>> createSearch(SessionContext sessionContext, QueryData queryData) {
      return new SearchCallable(logger, queryEngine, collector, objectLoader, sessionContext, LoadLevel.FULL, queryData);
   }

   public CancellableCallable<ResultSet<Match<ReadableArtifact, ReadableAttribute<?>>>> createSearchWithMatches(SessionContext sessionContext, QueryData queryData) {
      return new SearchMatchesCallable(logger, queryEngine, collector, objectLoader, sessionContext, LoadLevel.FULL,
         queryData);
   }
}