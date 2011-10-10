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
package org.eclipse.osee.orcs.core.internal.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.orcs.core.ds.CriteriaSet;
import org.eclipse.osee.orcs.core.ds.QueryOptions;
import org.eclipse.osee.orcs.core.internal.SessionContext;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;

/**
 * @author Roberto E. Escobar
 */
public class QueryFactoryImpl implements QueryFactory {

   private final SessionContext context;
   private final CriteriaFactory criteriaFctry;
   private final CallableQueryFactory queryExecutor;

   public QueryFactoryImpl(SessionContext context, CriteriaFactory criteriaFctry, CallableQueryFactory queryExecutor) {
      super();
      this.context = context;
      this.criteriaFctry = criteriaFctry;
      this.queryExecutor = queryExecutor;
   }

   @SuppressWarnings("unused")
   private QueryBuilder createBuilder(IOseeBranch branch) throws OseeCoreException {
      QueryOptions options = new QueryOptions();
      CriteriaSet criteriaSet = new CriteriaSet(branch);
      QueryBuilder builder = new QueryBuilderImpl(queryExecutor, criteriaFctry, context, criteriaSet, options);
      return builder;
   }

   @Override
   public QueryBuilder fromBranch(IOseeBranch branch) throws OseeCoreException {
      return createBuilder(branch);
   }

   @Override
   public QueryBuilder fromArtifacts(Collection<? extends ReadableArtifact> artifacts) throws OseeCoreException {
      Conditions.checkNotNullOrEmpty(artifacts, "artifacts");
      ReadableArtifact artifact = artifacts.iterator().next();
      IOseeBranch branch = artifact.getBranch();
      Set<String> guids = new HashSet<String>();
      for (ReadableArtifact art : artifacts) {
         guids.add(art.getGuid());
      }
      return fromBranch(branch).andGuidsOrHrids(guids);
   }

   @Override
   public QueryBuilder fromArtifactTypeAllBranches(IArtifactType artifactType) throws OseeCoreException {
      QueryBuilder builder = createBuilder(null);
      builder.andIsOfType(artifactType);
      return builder;
   }
}