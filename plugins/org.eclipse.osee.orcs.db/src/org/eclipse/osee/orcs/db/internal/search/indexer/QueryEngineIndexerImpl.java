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
package org.eclipse.osee.orcs.db.internal.search.indexer;

import java.io.InputStream;
import java.util.Set;
import org.eclipse.osee.executor.admin.CancellableCallable;
import org.eclipse.osee.framework.core.model.ReadableBranch;
import org.eclipse.osee.framework.core.model.cache.AttributeTypeCache;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.ds.IndexerData;
import org.eclipse.osee.orcs.core.ds.QueryEngineIndexer;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.DeleteTagSetDatabaseTxCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.IndexerDatabaseStatisticsCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.PurgeAllTagsDatabaseCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.producer.IndexAllInQueueCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.producer.IndexBranchesDatabaseCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.callable.producer.XmlStreamIndexerDatabaseCallable;
import org.eclipse.osee.orcs.search.IndexerCollector;

/**
 * @author Roberto E. Escobar
 */
public class QueryEngineIndexerImpl implements QueryEngineIndexer {

   private final Log logger;
   private final IOseeDatabaseService dbService;
   private final AttributeTypeCache attributeTypeCache;
   private final IndexingTaskConsumer consumer;

   public QueryEngineIndexerImpl(Log logger, IOseeDatabaseService dbService, AttributeTypeCache attributeTypeCache, IndexingTaskConsumer indexingConsumer) {
      this.logger = logger;
      this.dbService = dbService;
      this.attributeTypeCache = attributeTypeCache;
      this.consumer = indexingConsumer;
   }

   @Override
   public CancellableCallable<Integer> deleteIndexByQueryId(OrcsSession session, int queueId) {
      return new DeleteTagSetDatabaseTxCallable(logger, dbService, queueId);
   }

   @Override
   public CancellableCallable<Integer> purgeAllIndexes(OrcsSession session) {
      return new PurgeAllTagsDatabaseCallable(logger, dbService);
   }

   @Override
   public CancellableCallable<IndexerData> getIndexerData(OrcsSession session) {
      return new IndexerDatabaseStatisticsCallable(logger, dbService);
   }

   @Override
   public CancellableCallable<?> indexBranches(OrcsSession session, IndexerCollector collector, Set<ReadableBranch> branches, boolean indexOnlyMissing) {
      return new IndexBranchesDatabaseCallable(logger, dbService, consumer, attributeTypeCache, collector, branches,
         indexOnlyMissing);
   }

   @Override
   public CancellableCallable<Integer> indexAllFromQueue(OrcsSession session, IndexerCollector collector) {
      return new IndexAllInQueueCallable(logger, dbService, consumer, collector);
   }

   @Override
   public CancellableCallable<?> indexXmlStream(OrcsSession session, IndexerCollector collector, InputStream inputStream) {
      return new XmlStreamIndexerDatabaseCallable(logger, dbService, consumer, collector,
         IndexerConstants.INDEXER_CACHE_ALL_ITEMS, IndexerConstants.INDEXER_CACHE_LIMIT, inputStream);
   }

}
