/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.search.engine.internal.search;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.search.engine.IAttributeTaggerProviderManager;
import org.eclipse.osee.framework.search.engine.ISearchEngine;
import org.eclipse.osee.framework.search.engine.MatchLocation;
import org.eclipse.osee.framework.search.engine.SearchOptions;
import org.eclipse.osee.framework.search.engine.SearchOptions.SearchOptionsEnum;
import org.eclipse.osee.framework.search.engine.SearchResult;
import org.eclipse.osee.framework.search.engine.attribute.AttributeData;
import org.eclipse.osee.framework.search.engine.data.AttributeSearch;
import org.eclipse.osee.framework.search.engine.internal.Activator;
import org.eclipse.osee.framework.search.engine.utility.TagProcessor;

/**
 * @author Roberto E. Escobar
 */
public class SearchEngine implements ISearchEngine {

   private final SearchStatistics statistics;
   private final TagProcessor tagProcessor;
   private final IAttributeTaggerProviderManager taggingManager;

   public SearchEngine(SearchStatistics statistics, TagProcessor tagProcessor, IAttributeTaggerProviderManager taggingManager) {
      this.statistics = statistics;
      this.tagProcessor = tagProcessor;
      this.taggingManager = taggingManager;
   }

   @Override
   public SearchResult search(String searchString, int branchId, SearchOptions options, AttributeType... attributeTypes) throws Exception {
      SearchResult results = new SearchResult();

      long startTime = System.currentTimeMillis();

      AttributeSearch attributeSearch =
         new AttributeSearch(tagProcessor, searchString, branchId, options, attributeTypes);

      Collection<AttributeData> tagMatches = attributeSearch.getMatchingAttributes();
      long timeAfterPass1 = System.currentTimeMillis() - startTime;
      long secondPass = System.currentTimeMillis();

      boolean bypassSecondPass = !options.getBoolean(SearchOptionsEnum.match_word_order.asStringOption());
      if (bypassSecondPass) {
         for (AttributeData attributeData : tagMatches) {
            results.add(attributeData.getBranchId(), attributeData.getArtId(), attributeData.getGammaId());
         }
      } else {
         for (AttributeData attributeData : tagMatches) {
            try {
               List<MatchLocation> locations = taggingManager.find(attributeData, searchString, options);
               if (!locations.isEmpty()) {
                  results.add(attributeData.getBranchId(), attributeData.getArtId(), attributeData.getGammaId(),
                     locations);
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, String.format("Error processing: [%s]", attributeData));
            }
         }
      }
      secondPass = System.currentTimeMillis() - secondPass;

      String firstPassMsg =
         String.format("Pass 1: [%d items in %d ms]);", bypassSecondPass ? results.size() : tagMatches.size(),
            timeAfterPass1);
      String secondPassMsg = String.format(" Pass 2: [%d items in %d ms]", results.size(), secondPass);

      System.out.println(String.format("Search for [%s] - %s%s", searchString, firstPassMsg,
         bypassSecondPass ? "" : secondPassMsg));
      statistics.addEntry(searchString, branchId, options, results.size(), System.currentTimeMillis() - startTime);
      return results;
   }

   @Override
   public void clearStatistics() {
      this.statistics.clear();
   }

   @Override
   public SearchStatistics getStatistics() {
      try {
         return this.statistics.clone();
      } catch (CloneNotSupportedException ex) {
         return SearchStatistics.EMPTY_STATS;
      }
   }
}