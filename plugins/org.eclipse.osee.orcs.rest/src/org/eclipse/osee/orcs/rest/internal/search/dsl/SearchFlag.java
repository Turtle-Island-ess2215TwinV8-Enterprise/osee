/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.rest.internal.search.dsl;

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;

/**
 * @author John R. Misinco
 * @author Roberto E. Escobar
 */
public enum SearchFlag {
   TOKENIZED("t"),
   IGNORE_CASE("i"),
   MATCH_CASE("m"),
   TOKENIZED_ANY("t_any"),
   TOKENIZED_ORDERED("t_ordered"),
   INCLUDE_TYPE_INHERITANCE("iti");

   private final String token;

   private SearchFlag(String token) {
      this.token = token;
   }

   public String getToken() {
      return token;
   }

   public static SearchFlag fromString(String value) throws OseeCoreException {
      SearchFlag toReturn = null;
      for (SearchFlag op : SearchFlag.values()) {
         if (op.getToken().equals(value)) {
            toReturn = op;
            break;
         }
      }
      Conditions.checkNotNull(toReturn, "SearchFlag", "Invalid flag [%s]", value);
      return toReturn;
   }
}