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
package org.eclipse.osee.display.api.view;

import org.eclipse.osee.display.api.data.DisplayOptions;
import org.eclipse.osee.display.api.data.SearchResult;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.mvp.view.View;

/**
 * @author John Misinco
 */
public interface SearchResultsView extends View {

   public interface SearchResultsListener {
      void onOpenResult(ViewId selected);

      void onDisplayOptionsChanged();

      void onPageNavigationClicked(PageNavigation navigation);

      void onGotoResultsPage(int pageNum);
   }

   public enum PageNavigation {
      FIRST_PAGE,
      PREVIOUS_PAGE,
      NEXT_PAGE,
      LAST_PAGE
   }

   void clearResults();

   void addSearchResult(SearchResult result);

   void setViewListener(SearchResultsListener listener);

   DisplayOptions getDisplayOptions();

   void setDisplayOptions(DisplayOptions options);

   int getCurrentPage();

   void setCurrentPage(int page);

   int getNumberOfPages();

   void setNumberOfPages(int numPages);

}
