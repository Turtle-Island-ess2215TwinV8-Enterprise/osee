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

import org.eclipse.osee.display.api.data.SearchCriteria;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.api.view.SearchCriteriaView.SearchStatus;
import org.eclipse.osee.display.api.view.SearchResultsView;
import org.eclipse.osee.display.api.view.SearchResultsView.PageNavigation;
import org.eclipse.osee.display.api.view.SearchResultsView.SearchResultsListener;
import org.eclipse.osee.display.mvp.event.annotation.EndPoint;
import org.eclipse.osee.display.mvp.presenter.AbstractPresenter;
import org.eclipse.osee.display.presenter.ArtifactProvider;
import org.eclipse.osee.display.presenter.AsyncSearchHandler;
import org.eclipse.osee.display.presenter.SearchCriteriaParameters;
import org.eclipse.osee.display.presenter.events.SearchEventBus;
import org.eclipse.osee.framework.core.data.TokenFactory;

/**
 * @author John Misinco
 */
public class SearchResultsPresenter extends AbstractPresenter<SearchResultsView, SearchEventBus> implements SearchResultsListener {

   private final AsyncSearchHandler searchHandler;
   protected final ArtifactProvider artifactProvider;
   protected SearchCriteria cachedCriteria;

   public SearchResultsPresenter(ArtifactProvider artifactProvider, AsyncSearchHandler searchHandler) {
      this.artifactProvider = artifactProvider;
      this.searchHandler = searchHandler;
   }

   @Override
   public void setView(SearchResultsView view) {
      super.setView(view);
      searchHandler.setView(getView());
   }

   @Override
   public void setEventBus(SearchEventBus eventBus) {
      super.setEventBus(eventBus);
      searchHandler.setEventBus(getEventBus());
   }

   @EndPoint
   public void onExecuteSearch(SearchCriteria message) {
      SearchCriteriaParameters params = new SearchCriteriaParameters(message);

      if (!params.isValid()) {
         getEventBus().sendSearchStatus(SearchStatus.SEARCH_NOT_IN_PROGRESS);
         return;
      }

      try {
         artifactProvider.getSearchResults(TokenFactory.createBranch(params.getBranchId(), ""), params.isNameOnly(),
            params.getSearchPhrase(), searchHandler);
         cachedCriteria = message;
         getEventBus().sendSearchStatus(SearchStatus.SEARCH_IN_PROGRESS);
      } catch (Exception ex) {
         setErrorMessage("Error loading search results", ex);
      }
   }

   @Override
   public void onOpenResult(ViewId selected) {
      getEventBus().sendResultSelected(selected.getGuid(), selected.getAttribute("branch"));
   }

   @Override
   public void onDisplayOptionsChanged() {
      onExecuteSearch(cachedCriteria);
   }

   @Override
   public void onPageNavigationClicked(PageNavigation navigation) {
      int currentPage = getView().getCurrentPage();
      int numPages = getView().getNumberOfPages();

      switch (navigation) {
         case FIRST_PAGE:
            getView().setCurrentPage(0);
            break;
         case PREVIOUS_PAGE:
            if (currentPage != 0) {
               getView().setCurrentPage(currentPage - 1);
            }
            break;
         case NEXT_PAGE:
            if (currentPage < numPages - 1) {
               getView().setCurrentPage(currentPage + 1);
            }
            break;
         case LAST_PAGE:
            getView().setCurrentPage(numPages - 1);
            break;
      }
      onExecuteSearch(cachedCriteria);
   }

   @Override
   public void onGotoResultsPage(int pageNum) {
      if (pageNum >= 0 && pageNum < getView().getNumberOfPages()) {
         getView().setCurrentPage(pageNum);
         onExecuteSearch(cachedCriteria);
      }
   }

}
