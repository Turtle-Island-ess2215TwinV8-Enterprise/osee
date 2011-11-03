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
import org.eclipse.osee.framework.jdk.core.util.Strings;

public class SearchCriteriaParameters {

   protected final SearchCriteria criteria;

   public SearchCriteriaParameters(SearchCriteria criteria) {
      this.criteria = criteria;
   }

   public SearchCriteria getSearchCriteria() {
      return criteria;
   }

   public String getBranchId() {
      String branchGuid = "";
      if (criteria != null && criteria.getCriteria(SearchCriteria.BRANCH_KEY) != null) {
         Object obj = criteria.getCriteria(SearchCriteria.BRANCH_KEY);
         branchGuid = obj instanceof String ? (String) obj : "";
      }
      return branchGuid;
   }

   public boolean isNameOnly() {
      boolean nameOnly = true;
      if (criteria != null && criteria.getCriteria(SearchCriteria.NAME_ONLY_KEY) != null) {
         Object obj = criteria.getCriteria(SearchCriteria.NAME_ONLY_KEY);
         nameOnly = obj instanceof Boolean ? (Boolean) obj : false;
      }
      return nameOnly;
   }

   public String getSearchPhrase() {
      String searchPhrase = "";
      if (criteria != null && criteria.getCriteria(SearchCriteria.SEARCH_PHRASE_KEY) != null) {
         Object obj = criteria.getCriteria(SearchCriteria.SEARCH_PHRASE_KEY);
         searchPhrase = obj instanceof String ? (String) obj : "";
      }
      return searchPhrase;
   }

   public void setCriteria(String key, Object value) {
      if (criteria != null) {
         criteria.setCriteria(key, value);
      }
   }

   public boolean isValid() {
      return Strings.isValid(getBranchId()) && Strings.isValid(getSearchPhrase());
   }
}