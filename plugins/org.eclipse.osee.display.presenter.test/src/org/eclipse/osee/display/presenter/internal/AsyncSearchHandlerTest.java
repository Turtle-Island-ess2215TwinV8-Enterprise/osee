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

import java.util.LinkedList;
import java.util.List;
import junit.framework.Assert;
import org.eclipse.osee.display.api.data.DisplayOptions;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.api.view.SearchCriteriaView.SearchStatus;
import org.eclipse.osee.display.presenter.ArtifactProvider;
import org.eclipse.osee.display.presenter.AsyncSearchHandler;
import org.eclipse.osee.display.presenter.mocks.MockArtifact;
import org.eclipse.osee.display.presenter.mocks.MockArtifactProvider;
import org.eclipse.osee.display.presenter.mocks.MockAttribute;
import org.eclipse.osee.display.presenter.mocks.MockLog;
import org.eclipse.osee.display.presenter.mocks.MockMatch;
import org.eclipse.osee.display.presenter.mocks.MockSearchEventBus;
import org.eclipse.osee.display.presenter.mocks.MockSearchResultsView;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.data.ReadableAttribute;
import org.eclipse.osee.orcs.search.Match;
import org.junit.Test;

/**
 * @author John Misinco
 */
public class AsyncSearchHandlerTest {

   private List<Match<ReadableArtifact, ReadableAttribute<?>>> getSearchResults(int size) {
      List<Match<ReadableArtifact, ReadableAttribute<?>>> results =
         new LinkedList<Match<ReadableArtifact, ReadableAttribute<?>>>();

      for (int i = 0; i < size; i++) {
         MockArtifact art = new MockArtifact("guid" + i, "art" + i);
         MockAttribute attr = new MockAttribute(CoreAttributeTypes.Active, "true");
         MockMatch mockMatch = new MockMatch(art, attr);
         results.add(mockMatch);
      }
      return results;
   }

   @Test
   public void testOnSearchComplete() {
      MockArtifactProvider provider = new MockArtifactProvider();
      MockSearchEventBus eventBus = new MockSearchEventBus();
      MockSearchResultsView view = new MockSearchResultsView();
      DisplayOptions options = new DisplayOptions(false, 3);
      view.setDisplayOptions(options);

      SearchResultConverter converter = new SearchResultConverter(provider);
      AsyncSearchHandler handler = new AsyncSearchHandler(converter, null);
      handler.setEventBus(eventBus);
      handler.setView(view);

      //test that all 3 are displayed, current page=0, numPages=1
      List<Match<ReadableArtifact, ReadableAttribute<?>>> results = getSearchResults(3);
      handler.onSearchComplete(results);
      Assert.assertEquals(3, view.getSearchResults().size());
      Assert.assertEquals(1, view.getNumberOfPages());
      Assert.assertEquals(0, view.getCurrentPage());

      //make sure event bus was notified
      Assert.assertEquals(SearchStatus.SEARCH_SUCCESS, eventBus.getStatus());

      //test that all 3 are displayed, current page=0, numPages=4
      results = getSearchResults(10);
      handler.onSearchComplete(results);
      Assert.assertEquals(3, view.getSearchResults().size());
      Assert.assertEquals(4, view.getNumberOfPages());
      Assert.assertEquals(0, view.getCurrentPage());

      //test that page can change to 3 and 1 result is displayed
      view.setCurrentPage(3);
      handler.onSearchComplete(results);
      Assert.assertEquals(1, view.getSearchResults().size());
      Assert.assertEquals(4, view.getNumberOfPages());
      Assert.assertEquals(3, view.getCurrentPage());

      //test that page of 5 is invalid and cause page to reset to 1
      view.setCurrentPage(5);
      handler.onSearchComplete(results);
      Assert.assertEquals(3, view.getSearchResults().size());
      Assert.assertEquals(4, view.getNumberOfPages());
      Assert.assertEquals(0, view.getCurrentPage());
   }

   @Test
   public void testOnSearchCompleteError() {
      SearchResultConverter converter = new MockSearchResultConverter(null);
      AsyncSearchHandler handler = new AsyncSearchHandler(converter, new MockLog());
      MockSearchEventBus eventBus = new MockSearchEventBus();
      handler.setEventBus(eventBus);
      MockSearchResultsView view = new MockSearchResultsView();
      DisplayOptions options = new DisplayOptions(true, 3);
      view.setDisplayOptions(options);
      handler.setView(view);
      List<Match<ReadableArtifact, ReadableAttribute<?>>> results = getSearchResults(3);
      handler.onSearchComplete(results);

      Assert.assertEquals(SearchStatus.SEARCH_FAILED, eventBus.getStatus());
   }

   @Test
   public void testOnSearchCancelled() {
      AsyncSearchHandler handler = new AsyncSearchHandler(null, null);
      MockSearchEventBus eventBus = new MockSearchEventBus();
      handler.setEventBus(eventBus);
      handler.onSearchCancelled();
      Assert.assertEquals(SearchStatus.SEARCH_CANCELLED, eventBus.getStatus());
   }

   @Test
   public void testOnSearchFailed() {
      MockLog logger = new MockLog();
      AsyncSearchHandler handler = new AsyncSearchHandler(null, logger);
      MockSearchEventBus eventBus = new MockSearchEventBus();
      handler.setEventBus(eventBus);
      handler.onSearchFailed(null);
      Assert.assertEquals(SearchStatus.SEARCH_FAILED, eventBus.getStatus());
   }

   private class MockSearchResultConverter extends SearchResultConverter {

      public MockSearchResultConverter(ArtifactProvider artifactProvider) {
         super(artifactProvider);
      }

      @Override
      public List<ViewId> getCrumbs(ReadableArtifact art, boolean isVerbose) throws OseeCoreException {
         throw new OseeCoreException("test");
      }

   }

}
