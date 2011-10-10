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
package org.eclipse.osee.display.presenter.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.data.ResultSetList;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.data.ReadableAttribute;
import org.eclipse.osee.orcs.search.Match;
import com.google.common.collect.MapMaker;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactProviderCache {
   private static final ResultSet<Match<ReadableArtifact, ReadableAttribute<?>>> EMPTY_SET =
      new ResultSetList<Match<ReadableArtifact, ReadableAttribute<?>>>();
   private final ConcurrentMap<ReadableArtifact, ReadableArtifact> parentCache;
   private final Set<ReadableArtifact> artifactsWithNoParent = new HashSet<ReadableArtifact>();

   private ResultSet<Match<ReadableArtifact, ReadableAttribute<?>>> searchResults;
   private SearchParameters searchParameters;
   private Future<?> future;

   public ArtifactProviderCache() {
      this.parentCache = new MapMaker()//
      .initialCapacity(500)//
      .expiration(30, TimeUnit.MINUTES)//
      .makeMap();
      clearSearchCache();
   }

   public void cacheParent(ReadableArtifact art, ReadableArtifact parent) {
      if (parent != null) {
         parentCache.put(art, parent);
      } else {
         artifactsWithNoParent.add(art);
      }
   }

   public boolean isParentCached(ReadableArtifact artifact) {
      return parentCache.containsKey(artifact) || artifactsWithNoParent.contains(artifact);
   }

   public ReadableArtifact getParent(ReadableArtifact artifact) {
      return parentCache.get(artifact);
   }

   public void cacheResults(ResultSet<Match<ReadableArtifact, ReadableAttribute<?>>> searchResults) {
      this.searchResults = searchResults;
   }

   public void cacheSearch(SearchParameters searchParameters) {
      this.searchParameters = searchParameters;
   }

   public ResultSet<Match<ReadableArtifact, ReadableAttribute<?>>> getSearchResults() {
      return searchResults;
   }

   public SearchParameters getSearchParameters() {
      return searchParameters;
   }

   public boolean isSearchCached(SearchParameters params) {
      return searchParameters != null && searchParameters.equals(params);
   }

   public void clearSearchCache() {
      cacheSearch(null);
      cacheResults(EMPTY_SET);
      cacheSearchFuture(null);
   }

   public void cacheSearchFuture(Future<?> future) {
      this.future = future;
   }

   public Future<?> getSearchFuture() {
      return future;
   }

   public boolean isSearchInProgress() {
      return future != null && !future.isDone() && !future.isCancelled();
   }
}