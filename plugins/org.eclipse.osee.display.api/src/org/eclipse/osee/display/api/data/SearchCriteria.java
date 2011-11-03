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
package org.eclipse.osee.display.api.data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author John Misinco
 */
public class SearchCriteria {

   private final Map<String, Object> criteria = new HashMap<String, Object>();

   public static final String BRANCH_KEY = "branch";
   public static final String SEARCH_PHRASE_KEY = "search";
   public static final String NAME_ONLY_KEY = "nameOnly";

   public void setCriteria(String key, Object value) {
      criteria.put(key, value);
   }

   public Object getCriteria(String key) {
      return criteria.containsKey(key) ? criteria.get(key) : null;
   }

   public void clearCriteria() {
      criteria.clear();
   }

}
