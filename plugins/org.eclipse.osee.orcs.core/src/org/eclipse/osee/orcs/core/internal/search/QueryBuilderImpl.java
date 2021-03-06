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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.executor.admin.CancellableCallable;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.Operator;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.HumanReadableId;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.CriteriaSet;
import org.eclipse.osee.orcs.core.ds.QueryData;
import org.eclipse.osee.orcs.core.ds.QueryOptions;
import org.eclipse.osee.orcs.core.internal.SessionContext;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.AttributeReadable;
import org.eclipse.osee.orcs.data.HasLocalId;
import org.eclipse.osee.orcs.search.Match;
import org.eclipse.osee.orcs.search.QueryBuilder;

/**
 * @author Roberto E. Escobar
 */
public class QueryBuilderImpl implements QueryBuilder {

   private final CallableQueryFactory queryFactory;
   private final CriteriaFactory criteriaFactory;

   private final SessionContext sessionContext;
   private final QueryData queryData;

   public QueryBuilderImpl(CallableQueryFactory queryFactory, CriteriaFactory criteriaFactory, SessionContext sessionContext, QueryData queryData) {
      this.queryFactory = queryFactory;
      this.criteriaFactory = criteriaFactory;
      this.sessionContext = sessionContext;
      this.queryData = queryData;
   }

   private QueryData getQueryData() {
      return queryData;
   }

   private QueryOptions getOptions() {
      return queryData.getOptions();
   }

   @Override
   public QueryBuilder includeCache() {
      includeCache(true);
      return this;
   }

   @Override
   public QueryBuilder includeCache(boolean enabled) {
      getOptions().setIncludeCache(enabled);
      return this;
   }

   @Override
   public boolean isCacheIncluded() {
      return getOptions().isCacheIncluded();
   }

   @Override
   public QueryBuilder includeDeleted() {
      includeDeleted(true);
      return this;
   }

   @Override
   public QueryBuilder includeDeleted(boolean enabled) {
      getOptions().setIncludeDeleted(enabled);
      return this;
   }

   @Override
   public boolean areDeletedIncluded() {
      return getOptions().areDeletedIncluded();
   }

   @Override
   public QueryBuilder includeTypeInheritance() {
      includeTypeInheritance(true);
      return this;
   }

   @Override
   public QueryBuilder includeTypeInheritance(boolean enabled) {
      getOptions().setIncludeTypeInheritance(enabled);
      return this;
   }

   @Override
   public boolean isTypeInheritanceIncluded() {
      return getOptions().isTypeInheritanceIncluded();
   }

   @Override
   public QueryBuilder fromTransaction(int transactionId) {
      getOptions().setFromTransaction(transactionId);
      return this;
   }

   @Override
   public int getFromTransaction() {
      return getOptions().getFromTransaction();
   }

   @Override
   public QueryBuilder headTransaction() {
      getOptions().setHeadTransaction();
      return this;
   }

   @Override
   public boolean isHeadTransaction() {
      return getOptions().isHeadTransaction();
   }

   @Override
   public QueryBuilder excludeCache() {
      includeCache(false);
      return this;
   }

   @Override
   public QueryBuilder excludeDeleted() {
      includeDeleted(false);
      return this;
   }

   @Override
   public QueryBuilder excludeTypeInheritance() {
      includeTypeInheritance(false);
      return this;
   }

   @Override
   public QueryBuilder resetToDefaults() {
      getOptions().reset();
      return this;
   }

   @Override
   public QueryBuilder andLocalId(int... artifactIds) throws OseeCoreException {
      Set<Integer> ids = new HashSet<Integer>();
      for (Integer id : artifactIds) {
         ids.add(id);
      }
      return andLocalIds(ids);
   }

   @Override
   public QueryBuilder andGuidsOrHrids(String... ids) throws OseeCoreException {
      return andGuidsOrHrids(Arrays.asList(ids));
   }

   @Override
   public QueryBuilder andLocalIds(Collection<Integer> artifactIds) throws OseeCoreException {
      Criteria<QueryOptions> criteria = criteriaFactory.createArtifactIdCriteria(artifactIds);
      return addAndCheck(getQueryData(), criteria);
   }

   @Override
   public QueryBuilder andGuidsOrHrids(Collection<String> ids) throws OseeCoreException {
      Set<String> guids = new HashSet<String>();
      Set<String> hrids = new HashSet<String>();
      Set<String> invalids = new HashSet<String>();
      for (String id : ids) {
         if (GUID.isValid(id)) {
            guids.add(id);
         } else {
            String hrid = id.toUpperCase();
            if (HumanReadableId.isValid(hrid)) {
               hrids.add(hrid);
            } else {
               invalids.add(id);
            }
         }
      }
      Conditions.checkExpressionFailOnTrue(!invalids.isEmpty(), "Invalid guids or hrids detected - %s", invalids);
      if (!guids.isEmpty()) {
         Criteria<QueryOptions> guidCriteria = criteriaFactory.createArtifactGuidCriteria(guids);
         addAndCheck(getQueryData(), guidCriteria);
      }

      if (!hrids.isEmpty()) {
         Criteria<QueryOptions> hridCriteria = criteriaFactory.createArtifactHridCriteria(hrids);
         addAndCheck(getQueryData(), hridCriteria);
      }
      return this;
   }

   @Override
   public QueryBuilder andIsOfType(IArtifactType... artifactType) throws OseeCoreException {
      Criteria<QueryOptions> criteria = criteriaFactory.createArtifactTypeCriteria(Arrays.asList(artifactType));
      return addAndCheck(getQueryData(), criteria);
   }

   @Override
   public QueryBuilder andIsOfType(Collection<? extends IArtifactType> artifactType) throws OseeCoreException {
      Criteria<QueryOptions> criteria = criteriaFactory.createArtifactTypeCriteria(artifactType);
      return addAndCheck(getQueryData(), criteria);
   }

   @Override
   public QueryBuilder andExists(IAttributeType... attributeType) throws OseeCoreException {
      return andExists(Arrays.asList(attributeType));
   }

   @Override
   public QueryBuilder andExists(Collection<? extends IAttributeType> attributeTypes) throws OseeCoreException {
      Criteria<QueryOptions> criteria = criteriaFactory.createExistsCriteria(attributeTypes);
      return addAndCheck(getQueryData(), criteria);
   }

   @Override
   public QueryBuilder andExists(IRelationType relationType) throws OseeCoreException {
      Criteria<QueryOptions> criteria = criteriaFactory.createExistsCriteria(relationType);
      return addAndCheck(getQueryData(), criteria);
   }

   @Override
   public QueryBuilder and(IAttributeType attributeType, Operator operator, String value) throws OseeCoreException {
      Criteria<QueryOptions> criteria =
         criteriaFactory.createAttributeCriteria(attributeType, operator, Collections.singleton(value));
      return addAndCheck(getQueryData(), criteria);
   }

   @Override
   public QueryBuilder and(IAttributeType attributeType, Operator operator, Collection<String> values) throws OseeCoreException {
      Criteria<QueryOptions> criteria = criteriaFactory.createAttributeCriteria(attributeType, operator, values);
      return addAndCheck(getQueryData(), criteria);
   }

   @Override
   public QueryBuilder and(IAttributeType attributeType, String value, QueryOption... options) throws OseeCoreException {
      Criteria<QueryOptions> criteria =
         criteriaFactory.createAttributeCriteria(Collections.singleton(attributeType), Collections.singleton(value),
            options);
      return addAndCheck(getQueryData(), criteria);
   }

   @Override
   public QueryBuilder and(Collection<? extends IAttributeType> attributeTypes, String value, QueryOption... options) throws OseeCoreException {
      Criteria<QueryOptions> criteria =
         criteriaFactory.createAttributeCriteria(attributeTypes, Collections.singleton(value), options);
      return addAndCheck(getQueryData(), criteria);
   }

   private QueryBuilder addAndCheck(QueryData queryData, Criteria<QueryOptions> criteria) throws OseeCoreException {
      criteria.checkValid(getOptions());
      queryData.addCriteria(criteria);
      return this;
   }

   @Override
   public QueryBuilder andNameEquals(String artifactName) throws OseeCoreException {
      return and(CoreAttributeTypes.Name, Operator.EQUAL, artifactName);
   }

   @Override
   public QueryBuilder andIds(IArtifactToken... artifactToken) throws OseeCoreException {
      return andIds(Arrays.asList(artifactToken));
   }

   @Override
   public QueryBuilder andIds(Collection<? extends IArtifactToken> artifactTokens) throws OseeCoreException {
      Set<String> guids = new HashSet<String>();
      for (IArtifactToken token : artifactTokens) {
         guids.add(token.getGuid());
      }
      return andGuidsOrHrids(guids);
   }

   @Override
   public QueryBuilder andRelatedTo(IRelationTypeSide relationTypeSide, ArtifactReadable... artifacts) throws OseeCoreException {
      return andRelatedTo(relationTypeSide, Arrays.asList(artifacts));
   }

   @Override
   public QueryBuilder andRelatedTo(IRelationTypeSide relationTypeSide, Collection<? extends ArtifactReadable> artifacts) throws OseeCoreException {
      Set<Integer> ids = new HashSet<Integer>();
      for (HasLocalId token : artifacts) {
         ids.add(token.getLocalId());
      }
      return andRelatedToLocalIds(relationTypeSide, ids);
   }

   @Override
   public QueryBuilder andRelatedToLocalIds(IRelationTypeSide relationTypeSide, int... artifactIds) throws OseeCoreException {
      Set<Integer> ids = new HashSet<Integer>();
      for (Integer id : artifactIds) {
         ids.add(id);
      }
      return andRelatedToLocalIds(relationTypeSide, ids);
   }

   @Override
   public QueryBuilder andRelatedToLocalIds(IRelationTypeSide relationTypeSide, Collection<Integer> artifactIds) throws OseeCoreException {
      Criteria<QueryOptions> criteria = criteriaFactory.createRelatedToCriteria(relationTypeSide, artifactIds);
      return addAndCheck(getQueryData(), criteria);
   }

   @Override
   public ResultSet<ArtifactReadable> getResults() throws OseeCoreException {
      ResultSet<ArtifactReadable> result = null;
      try {
         result = createSearch().call();
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return result;
   }

   @Override
   public ResultSet<Match<ArtifactReadable, AttributeReadable<?>>> getMatches() throws OseeCoreException {
      ResultSet<Match<ArtifactReadable, AttributeReadable<?>>> result = null;
      try {
         result = createSearchWithMatches().call();
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return result;
   }

   @Override
   public int getCount() throws OseeCoreException {
      Integer result = -1;
      try {
         result = createCount().call();
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return result;
   }

   @Override
   public ResultSet<HasLocalId> getResultsAsLocalIds() throws OseeCoreException {
      ResultSet<HasLocalId> result = null;
      try {
         result = createSearchResultsAsLocalIds().call();
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return result;
   }

   @Override
   public CancellableCallable<ResultSet<ArtifactReadable>> createSearch() throws OseeCoreException {
      return queryFactory.createSearch(sessionContext, checkAndCloneQueryData());
   }

   @Override
   public CancellableCallable<ResultSet<Match<ArtifactReadable, AttributeReadable<?>>>> createSearchWithMatches() throws OseeCoreException {
      return queryFactory.createSearchWithMatches(sessionContext, checkAndCloneQueryData());
   }

   @Override
   public CancellableCallable<ResultSet<HasLocalId>> createSearchResultsAsLocalIds() throws OseeCoreException {
      return queryFactory.createLocalIdSearch(sessionContext, checkAndCloneQueryData());
   }

   private QueryData checkAndCloneQueryData() throws OseeCoreException {
      QueryData queryData = getQueryData().clone();
      CriteriaSet criteriaSet = queryData.getCriteriaSet();
      if (criteriaSet.getBranch() != null && criteriaSet.getCriterias().isEmpty()) {
         addAndCheck(queryData, criteriaFactory.createAllArtifactsCriteria());
      }
      return queryData;
   }

   @Override
   public CancellableCallable<Integer> createCount() throws OseeCoreException {
      return queryFactory.createCount(sessionContext, checkAndCloneQueryData());
   }
}
