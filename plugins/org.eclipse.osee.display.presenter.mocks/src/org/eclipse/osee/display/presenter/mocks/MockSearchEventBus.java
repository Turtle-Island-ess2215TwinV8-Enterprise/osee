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

import org.eclipse.osee.display.api.data.SearchCriteria;
import org.eclipse.osee.display.api.view.SearchCriteriaView.SearchStatus;
import org.eclipse.osee.display.presenter.events.SearchEventBus;

/**
 * @author John Misinco
 */
public class MockSearchEventBus implements SearchEventBus {

   private SearchStatus status = null;
   private String selectedArtGuid, selectedBranchGuid;
   private SearchCriteria searchCriteria;

   public SearchCriteria getSearchCriteria() {
      return searchCriteria;
   }

   public SearchStatus getStatus() {
      return status;
   }

   public String getSelectedArtGuid() {
      return selectedArtGuid;
   }

   public String getSelectedBranchGuid() {
      return selectedBranchGuid;
   }

   @Override
   public void sendExecuteSearch(SearchCriteria message) {
      searchCriteria = message;
   }

   @Override
   public void sendSearchStatus(SearchStatus message) {
      status = message;
   }

   @Override
   public void sendResultSelected(String artifactGuid, String branchGuid) {
      selectedArtGuid = artifactGuid;
      selectedBranchGuid = branchGuid;
   }

}