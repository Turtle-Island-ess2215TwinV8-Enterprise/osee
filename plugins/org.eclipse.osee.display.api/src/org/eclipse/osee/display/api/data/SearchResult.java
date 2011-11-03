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

import java.util.List;

/**
 * @author Shawn F. Cook
 */
public class SearchResult {

   private final ViewId result;
   private final List<ViewId> crumbs;
   private final List<SearchResultMatch> matches;
   private final String resultTypeName;

   public SearchResult(ViewId result, List<ViewId> crumbs, List<SearchResultMatch> matches, String resultTypeName) {
      this.result = result;
      this.crumbs = crumbs;
      this.matches = matches;
      this.resultTypeName = resultTypeName;
   }

   public ViewId getResult() {
      return result;
   }

   public List<ViewId> getCrumbs() {
      return crumbs;
   }

   public List<SearchResultMatch> getMatches() {
      return matches;
   }

   public String getResultTypeName() {
      return resultTypeName;
   }

}
