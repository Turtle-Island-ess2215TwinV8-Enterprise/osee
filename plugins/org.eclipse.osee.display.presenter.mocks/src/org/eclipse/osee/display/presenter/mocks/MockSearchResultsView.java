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
package org.eclipse.osee.display.presenter.mocks;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.display.api.data.DisplayOptions;
import org.eclipse.osee.display.api.data.SearchResult;
import org.eclipse.osee.display.api.view.SearchResultsView;
import org.eclipse.osee.display.mvp.MessageType;
import org.eclipse.osee.logger.Log;

/**
 * @author John Misinco
 */
public class MockSearchResultsView implements SearchResultsView {

   private DisplayOptions options;
   private int currentPage = 0;
   private int numPages = 0;
   private final List<SearchResult> results = new LinkedList<SearchResult>();

   public List<SearchResult> getSearchResults() {
      return results;
   }

   @Override
   public Log getLogger() {
      return null;
   }

   @Override
   public void setLogger(Log logger) {
      //
   }

   @Override
   public void displayMessage(String caption) {
      //
   }

   @Override
   public void displayMessage(String caption, String description, MessageType messageType) {
      //
   }

   @Override
   public void dispose() {
      //
   }

   @Override
   public boolean isDisposed() {
      return false;
   }

   @Override
   public void clearResults() {
      results.clear();
   }

   @Override
   public void addSearchResult(SearchResult result) {
      results.add(result);
   }

   @Override
   public void setViewListener(SearchResultsListener listener) {
      //
   }

   @Override
   public DisplayOptions getDisplayOptions() {
      return options;
   }

   @Override
   public void setDisplayOptions(DisplayOptions options) {
      this.options = options;
   }

   @Override
   public int getCurrentPage() {
      return currentPage;
   }

   @Override
   public void setCurrentPage(int page) {
      currentPage = page;
   }

   @Override
   public int getNumberOfPages() {
      return numPages;
   }

   @Override
   public void setNumberOfPages(int numPages) {
      this.numPages = numPages;
   }

   @Override
   public Object getContent() {
      return null;
   }

   @Override
   public void createControl() {
      //
   }

}