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

import org.eclipse.osee.display.api.data.SearchCriteria;
import org.eclipse.osee.display.mvp.view.View;

/**
 * @author John Misinco
 */
public interface SearchCriteriaView extends View {

   public interface SearchCriteriaListener {
      void onSearchClicked();

      void onSearchCriteriaChanged(String key, Object value);
   }

   public interface SearchInputValidator {
      boolean validate();
   }

   public enum SearchStatus {
      SEARCH_IN_PROGRESS,
      SEARCH_NOT_IN_PROGRESS,
      SEARCH_CANCELLED,
      SEARCH_FAILED,
      SEARCH_SUCCESS;
   }

   void setSearchCriteriaListener(SearchCriteriaListener listener);

   void setSearchAllowed(boolean allowed);

   boolean isSearchAllowed();

   void setStatus(SearchStatus status);

   SearchStatus getStatus();

   SearchCriteria getCriteria();

   void setCriteria(SearchCriteria criteria);

   void updateCriteria(String key, Object value);
}
