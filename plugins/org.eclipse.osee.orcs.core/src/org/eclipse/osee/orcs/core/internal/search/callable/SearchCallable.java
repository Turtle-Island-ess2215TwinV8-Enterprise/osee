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
package org.eclipse.osee.orcs.core.internal.search.callable;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.data.ResultSetList;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.CriteriaSet;
import org.eclipse.osee.orcs.core.ds.LoadOptions;
import org.eclipse.osee.orcs.core.ds.QueryContext;
import org.eclipse.osee.orcs.core.ds.QueryEngine;
import org.eclipse.osee.orcs.core.ds.QueryOptions;
import org.eclipse.osee.orcs.core.ds.QueryPostProcessor;
import org.eclipse.osee.orcs.core.internal.OrcsObjectLoader;
import org.eclipse.osee.orcs.core.internal.SessionContext;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.data.ReadableAttribute;
import org.eclipse.osee.orcs.search.Match;

/**
 * @author Roberto E. Escobar
 */
public class SearchCallable extends AbstractSearchCallable<ResultSet<ReadableArtifact>> {

   private QueryContext queryContext;

   public SearchCallable(Log logger, QueryEngine queryEngine, OrcsObjectLoader objectLoader, SessionContext sessionContext, LoadLevel loadLevel, CriteriaSet criteriaSet, QueryOptions options) {
      super(logger, queryEngine, objectLoader, sessionContext, loadLevel, criteriaSet, options);
   }

   @Override
   protected ResultSet<ReadableArtifact> innerCall() throws Exception {
      QueryContext queryContext = queryEngine.create(sessionContext.getSessionId(), criteriaSet, options);
      LoadOptions loadOptions = new LoadOptions(options.isHistorical(), options.areDeletedIncluded(), loadLevel);
      checkForCancelled();
      List<ReadableArtifact> artifacts = objectLoader.load(this, queryContext, loadOptions, sessionContext);

      List<ReadableArtifact> results;
      if (!queryContext.getPostProcessors().isEmpty()) {
         results = new ArrayList<ReadableArtifact>();
         for (QueryPostProcessor processor : queryContext.getPostProcessors()) {
            processor.setItemsToProcess(artifacts);
            checkForCancelled();
            List<Match<ReadableArtifact, ReadableAttribute<?>>> matches = processor.call();
            for (Match<ReadableArtifact, ReadableAttribute<?>> match : matches) {
               checkForCancelled();
               results.add(match.getItem());
            }
         }
      } else {
         results = artifacts;
      }
      return new ResultSetList<ReadableArtifact>(results);
   }

   @Override
   public void setCancel(boolean isCancelled) {
      super.setCancel(isCancelled);
      if (queryContext != null && !queryContext.getPostProcessors().isEmpty()) {
         for (QueryPostProcessor processor : queryContext.getPostProcessors()) {
            processor.setCancel(true);
         }
      }
   }

}