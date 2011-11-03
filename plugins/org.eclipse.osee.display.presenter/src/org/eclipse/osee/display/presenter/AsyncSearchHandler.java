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
package org.eclipse.osee.display.presenter;

import java.util.List;
import org.eclipse.osee.display.api.data.DisplayOptions;
import org.eclipse.osee.display.api.data.SearchResult;
import org.eclipse.osee.display.api.data.SearchResultMatch;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.api.view.SearchCriteriaView.SearchStatus;
import org.eclipse.osee.display.api.view.SearchResultsView;
import org.eclipse.osee.display.mvp.MessageTypeEnum;
import org.eclipse.osee.display.presenter.events.SearchEventBus;
import org.eclipse.osee.display.presenter.internal.SearchResultConverter;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.data.ReadableAttribute;
import org.eclipse.osee.orcs.search.Match;

/**
 * @author John Misinco
 */
public class AsyncSearchHandler {
   private final SearchResultConverter converter;
   private SearchResultsView view;
   private SearchEventBus eventBus;
   private final Log logger;

   public AsyncSearchHandler(SearchResultConverter converter, Log logger) {
      this.converter = converter;
      this.logger = logger;
   }

   public void setView(SearchResultsView view) {
      this.view = view;
   }

   public void setEventBus(SearchEventBus eventBus) {
      this.eventBus = eventBus;
   }

   private void processSearchResults(List<Match<ReadableArtifact, ReadableAttribute<?>>> searchResults) throws OseeCoreException {
      view.clearResults();
      DisplayOptions options = view.getDisplayOptions();

      int resultsPerPage = options.getResultsPerPage();
      int resultSize = searchResults.size();
      int calculatedNumPages = (int) Math.ceil((double) resultSize / (double) resultsPerPage);

      if (view.getNumberOfPages() != calculatedNumPages || view.getCurrentPage() >= calculatedNumPages) {
         view.setNumberOfPages(calculatedNumPages);
         view.setCurrentPage(0);
      }

      int currentPage = view.getCurrentPage();
      int startIndex = currentPage * resultsPerPage;
      int endIndex = startIndex + resultsPerPage;
      boolean isVerbose = options.isVerboseResults();

      if (!searchResults.isEmpty()) {
         for (int i = startIndex; i < endIndex && i < resultSize; i++) {
            Match<ReadableArtifact, ReadableAttribute<?>> match = searchResults.get(i);
            ReadableArtifact matchedArtifact = match.getItem();

            ViewId art = converter.convertToViewId(matchedArtifact);
            List<SearchResultMatch> searchResultMatches = converter.getSearchResultMatches(match, isVerbose);
            List<ViewId> crumbs = converter.getCrumbs(matchedArtifact, isVerbose);
            String typeName = converter.getTypeName(matchedArtifact);
            SearchResult result = new SearchResult(art, crumbs, searchResultMatches, typeName);
            view.addSearchResult(result);
         }
      }
   }

   public void onSearchComplete(List<Match<ReadableArtifact, ReadableAttribute<?>>> results) {
      try {
         processSearchResults(results);
         eventBus.sendSearchStatus(SearchStatus.SEARCH_SUCCESS);
      } catch (OseeCoreException ex) {
         logger.error(ex, "Error processing results");
         eventBus.sendSearchStatus(SearchStatus.SEARCH_FAILED);
         view.displayMessage("Error processing results", Lib.exceptionToString(ex), MessageTypeEnum.ERROR);
      }
   }

   public void onSearchCancelled() {
      eventBus.sendSearchStatus(SearchStatus.SEARCH_CANCELLED);
   }

   public void onSearchFailed(Throwable throwable) {
      logger.error(throwable, "Error while searching");
      eventBus.sendSearchStatus(SearchStatus.SEARCH_FAILED);
   }

}