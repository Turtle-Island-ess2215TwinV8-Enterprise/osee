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

import java.util.List;
import junit.framework.Assert;
import org.eclipse.osee.display.api.data.DisplayOptions;
import org.eclipse.osee.display.api.data.SearchCriteria;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.api.view.SearchCriteriaView.SearchStatus;
import org.eclipse.osee.display.api.view.SearchResultsView.PageNavigation;
import org.eclipse.osee.display.presenter.AsyncSearchHandler;
import org.eclipse.osee.display.presenter.mocks.MockArtifactProvider;
import org.eclipse.osee.display.presenter.mocks.MockSearchEventBus;
import org.eclipse.osee.display.presenter.mocks.MockSearchResultsView;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.data.ReadableAttribute;
import org.eclipse.osee.orcs.search.Match;
import org.junit.Test;

/**
 * @author John Misinco
 */
public class SearchResultsPresenterTest {

   @Test
   public void testOnExecuteSearch() {
      MockSearchResultsProvider provider = new MockSearchResultsProvider();
      SearchResultConverter converter = new SearchResultConverter(provider);
      AsyncSearchHandler listener = new AsyncSearchHandler(converter, null);
      SearchResultsPresenter presenter = new SearchResultsPresenter(provider, listener);
      SearchCriteria criteria = new SearchCriteria();
      String branchGuid = GUID.create();
      String searchPhrase = "don't care";
      boolean nameOnly = true, verbose = false;
      criteria.setCriteria(SearchCriteria.BRANCH_KEY, branchGuid);
      criteria.setCriteria(SearchCriteria.SEARCH_PHRASE_KEY, searchPhrase);
      criteria.setCriteria(SearchCriteria.NAME_ONLY_KEY, nameOnly);

      DisplayOptions options = new DisplayOptions(false, 10);
      MockSearchResultsView view = new MockSearchResultsView();
      view.setDisplayOptions(options);
      presenter.setView(view);

      MockSearchEventBus eventBus = new MockSearchEventBus();
      presenter.setEventBus(eventBus);
      presenter.onExecuteSearch(criteria);

      Assert.assertEquals(searchPhrase, provider.getSearchPhrase());
      Assert.assertEquals(nameOnly, provider.isNameOnly());
      Assert.assertEquals(branchGuid, provider.getBranch().getGuid());
      Assert.assertEquals(SearchStatus.SEARCH_IN_PROGRESS, eventBus.getStatus());
      Assert.assertEquals(verbose, view.getDisplayOptions().isVerboseResults());
   }

   @Test
   public void testOnOpenResult() {
      AsyncSearchHandler listener = new AsyncSearchHandler(null, null);
      SearchResultsPresenter presenter = new SearchResultsPresenter(null, listener);
      MockSearchEventBus eventBus = new MockSearchEventBus();
      presenter.setEventBus(eventBus);

      String artGuid = GUID.create();
      String branchGuid = GUID.create();
      ViewId result = new ViewId(artGuid, "name");
      result.setAttribute("branch", branchGuid);
      presenter.onOpenResult(result);

      Assert.assertEquals(artGuid, eventBus.getSelectedArtGuid());
      Assert.assertEquals(branchGuid, eventBus.getSelectedBranchGuid());
   }

   @Test
   public void testOnPageNavigationClicked() {
      AsyncSearchHandler listener = new AsyncSearchHandler(null, null);
      SearchResultsPresenter presenter = new SearchResultsPresenter(null, listener);
      MockSearchResultsView view = new MockSearchResultsView();
      presenter.setView(view);
      MockSearchEventBus eventBus = new MockSearchEventBus();
      presenter.setEventBus(eventBus);
      view.setNumberOfPages(10);
      view.setCurrentPage(5);

      presenter.onPageNavigationClicked(PageNavigation.FIRST_PAGE);
      Assert.assertEquals(0, view.getCurrentPage());

      presenter.onPageNavigationClicked(PageNavigation.PREVIOUS_PAGE);
      Assert.assertEquals(0, view.getCurrentPage());

      view.setCurrentPage(5);
      presenter.onPageNavigationClicked(PageNavigation.PREVIOUS_PAGE);
      Assert.assertEquals(4, view.getCurrentPage());

      presenter.onPageNavigationClicked(PageNavigation.NEXT_PAGE);
      Assert.assertEquals(5, view.getCurrentPage());

      presenter.onPageNavigationClicked(PageNavigation.LAST_PAGE);
      Assert.assertEquals(9, view.getCurrentPage());

      presenter.onPageNavigationClicked(PageNavigation.NEXT_PAGE);
      Assert.assertEquals(9, view.getCurrentPage());
   }

   @Test
   public void testOnGotoResultsPage() {
      AsyncSearchHandler listener = new AsyncSearchHandler(null, null);
      SearchResultsPresenter presenter = new SearchResultsPresenter(null, listener);
      MockSearchResultsView view = new MockSearchResultsView();
      presenter.setView(view);
      MockSearchEventBus eventBus = new MockSearchEventBus();
      presenter.setEventBus(eventBus);
      view.setNumberOfPages(10);
      view.setCurrentPage(5);

      presenter.onGotoResultsPage(1);
      Assert.assertEquals(1, view.getCurrentPage());

      presenter.onGotoResultsPage(-1);
      Assert.assertEquals(1, view.getCurrentPage());

      presenter.onGotoResultsPage(10);
      Assert.assertEquals(1, view.getCurrentPage());

      presenter.onGotoResultsPage(9);
      Assert.assertEquals(9, view.getCurrentPage());
   }

   private class MockSearchResultsProvider extends MockArtifactProvider {

      private IOseeBranch branch;
      private boolean nameOnly;
      private String searchPhrase;
      private List<Match<ReadableArtifact, ReadableAttribute<?>>> results;

      public void setResults(List<Match<ReadableArtifact, ReadableAttribute<?>>> results) {
         this.results = results;
      }

      public IOseeBranch getBranch() {
         return branch;
      }

      public boolean isNameOnly() {
         return nameOnly;
      }

      public String getSearchPhrase() {
         return searchPhrase;
      }

      @Override
      public void getSearchResults(IOseeBranch branch, boolean nameOnly, String searchPhrase, AsyncSearchHandler callback) {
         this.branch = branch;
         this.nameOnly = nameOnly;
         this.searchPhrase = searchPhrase;
      }
   }
}
