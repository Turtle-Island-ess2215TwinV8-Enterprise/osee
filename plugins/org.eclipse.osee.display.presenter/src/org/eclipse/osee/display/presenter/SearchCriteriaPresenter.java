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

import org.eclipse.osee.display.api.data.SearchCriteria;
import org.eclipse.osee.display.api.view.SearchCriteriaView;
import org.eclipse.osee.display.api.view.SearchCriteriaView.SearchCriteriaListener;
import org.eclipse.osee.display.mvp.BindException;
import org.eclipse.osee.display.mvp.presenter.AbstractPresenter;
import org.eclipse.osee.display.presenter.events.SearchEventBus;

/**
 * @author John Misinco
 */
public class SearchCriteriaPresenter<K extends SearchCriteriaView> extends AbstractPresenter<K, SearchEventBus> implements SearchCriteriaListener {

   @Override
   public void bind() throws BindException {
      if (getView() == null) {
         throw new BindException("View was null");
      }
      getView().setSearchCriteriaListener(this);
   }

   @Override
   public void onSearchClicked() {
      SearchCriteria searchCriteria = getView().getCriteria();
      getEventBus().sendExecuteSearch(searchCriteria);
   }

   @Override
   public void onSearchCriteriaChanged(String key, Object value) {
      // Signal something changed?
   }

   public void setSearchPhrase(String phrase) {
      getView().updateCriteria(SearchCriteria.SEARCH_PHRASE_KEY, phrase);
   }

   public void setNameOnly(boolean nameOnly) {
      getView().updateCriteria(SearchCriteria.NAME_ONLY_KEY, nameOnly);
   }

   public void setBranch(String branchGuid) {
      getView().updateCriteria(SearchCriteria.BRANCH_KEY, branchGuid);
   }
}
