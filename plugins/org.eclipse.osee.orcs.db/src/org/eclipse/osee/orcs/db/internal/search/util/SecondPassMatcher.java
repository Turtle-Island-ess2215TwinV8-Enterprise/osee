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
package org.eclipse.osee.orcs.db.internal.search.util;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import org.eclipse.osee.framework.core.enums.CaseType;
import org.eclipse.osee.framework.core.enums.MatchTokenCountType;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.jdk.core.type.MatchLocation;
import org.eclipse.osee.orcs.db.internal.search.tagger.StreamMatcher;

/**
 * @author John Misinco
 */
public class SecondPassMatcher implements StreamMatcher {

   private final TokenOrderProcessorFactory processorFactory;

   public SecondPassMatcher(TokenOrderProcessorFactory processorFactory) {
      this.processorFactory = processorFactory;
   }

   private String normalizeCase(CaseType caseType, String token) {
      return caseType.isCaseSensitive() ? token : token.toLowerCase();
   }

   @Override
   public List<MatchLocation> findInStream(InputStream inputStream, String toSearch, boolean findAllMatchLocations, QueryOption... options) {
      CheckedOptions checkedOptions = new CheckedOptions();
      checkedOptions.accept(options);

      TokenOrderProcessor processor = processorFactory.createTokenProcessor(checkedOptions);

      parseSearchString(processor, toSearch, checkedOptions);
      searchStream(processor, inputStream, checkedOptions, findAllMatchLocations);

      return processor.getLocations();
   }

   private void parseSearchString(TokenOrderProcessor processor, String toSearch, CheckedOptions options) {
      Scanner toSearchScanner = new Scanner(toSearch);
      try {
         toSearchScanner.useDelimiter(options.getDelimiter());
         while (toSearchScanner.hasNext()) {
            String next = toSearchScanner.next();
            next = normalizeCase(options.getCaseType(), next);
            processor.acceptTokenToMatch(next);
         }
      } finally {
         toSearchScanner.close();
      }
   }

   private void searchStream(TokenOrderProcessor processor, InputStream inputStream, CheckedOptions options, boolean findAllMatchLocations) {
      Scanner inputStreamScanner = new Scanner(inputStream);
      try {
         inputStreamScanner.useDelimiter(options.getDelimiter());
         int numTokensProcessed = 0;
         boolean isProcessorDone = false;
         while (inputStreamScanner.hasNext()) {
            ++numTokensProcessed;

            if (numTokensProcessed > processor.getTotalTokensToMatch() && MatchTokenCountType.MATCH_TOKEN_COUNT == options.getCountType()) {
               processor.getLocations().clear();
               break;
            }

            /**
             * the purpose of this here is to allow one more token to be read after the processor has signaled that it
             * is complete. the if statement above will catch the case when too many tokens are present.
             */
            if (isProcessorDone && !findAllMatchLocations) {
               break;
            }

            String next = inputStreamScanner.next();
            next = normalizeCase(options.getCaseType(), next);

            MatchResult match = inputStreamScanner.match();
            isProcessorDone = processor.processToken(next, match);
         }

         // Clear if search did not complete
         if (!isProcessorDone) {
            processor.clearAllLocations();
         }
      } finally {
         inputStreamScanner.close();
      }
   }

}
