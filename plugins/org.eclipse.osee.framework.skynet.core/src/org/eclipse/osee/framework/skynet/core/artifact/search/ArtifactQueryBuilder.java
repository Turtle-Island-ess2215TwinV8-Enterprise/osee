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
package org.eclipse.osee.framework.skynet.core.artifact.search;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import static org.eclipse.osee.framework.core.enums.DeletionFlag.INCLUDE_DELETED;
import static org.eclipse.osee.framework.skynet.core.artifact.LoadType.INCLUDE_CACHE;
import static org.eclipse.osee.framework.skynet.core.artifact.LoadType.RELOAD_CACHE;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.HumanReadableId;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoader;
import org.eclipse.osee.framework.skynet.core.artifact.ISearchConfirmer;
import org.eclipse.osee.framework.skynet.core.artifact.LoadType;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.orcs.rest.client.OseeClient;
import org.eclipse.osee.orcs.rest.client.QueryBuilder;
import org.eclipse.osee.orcs.rest.model.search.SearchResponse;
import org.eclipse.osee.orcs.rest.model.search.SearchResult;

/**
 * @author Ryan D. Brooks
 */
public class ArtifactQueryBuilder {
   private List<String> guids;
   private List<String> hrids;
   private String guidOrHrid;
   private final ArtifactSearchCriteria[] criteria;
   private final IOseeBranch branch;
   private int artifactId;
   private Collection<Integer> artifactIds;
   private final Collection<? extends IArtifactType> artifactTypes;
   private final DeletionFlag allowDeleted;
   private final LoadLevel loadLevel;
   private boolean emptyCriteria = false;
   private final TransactionRecord transactionId;

   /**
    * @param allowDeleted set whether deleted artifacts should be included in the resulting artifact list
    */
   public ArtifactQueryBuilder(int artId, IOseeBranch branch, DeletionFlag allowDeleted, LoadLevel loadLevel) {
      this(null, artId, null, null, null, branch, null, allowDeleted, loadLevel);
   }

   /**
    * search for artifacts with the given ids
    * 
    * @param artifactIds list of artifact ids
    * @param allowDeleted set whether deleted artifacts should be included in the resulting artifact list
    */
   public ArtifactQueryBuilder(Collection<Integer> artifactIds, IOseeBranch branch, DeletionFlag allowDeleted, LoadLevel loadLevel) {
      this(artifactIds, 0, null, null, null, branch, null, allowDeleted, loadLevel);
      emptyCriteria = artifactIds.isEmpty();
   }

   public ArtifactQueryBuilder(List<String> guidOrHrids, IOseeBranch branch, LoadLevel loadLevel) {
      this(null, 0, guidOrHrids, null, null, branch, null, EXCLUDE_DELETED, loadLevel);
      emptyCriteria = guidOrHrids.isEmpty();
   }

   public ArtifactQueryBuilder(List<String> guidOrHrids, IOseeBranch branch, DeletionFlag allowDeleted, LoadLevel loadLevel) {
      this(null, 0, guidOrHrids, null, null, branch, null, allowDeleted, loadLevel);
      emptyCriteria = guidOrHrids.isEmpty();
   }

   public ArtifactQueryBuilder(List<String> guidOrHrids, TransactionRecord transactionId, DeletionFlag allowDeleted, LoadLevel loadLevel) throws OseeCoreException {
      this(null, 0, guidOrHrids, null, null, transactionId.getBranch(), transactionId, allowDeleted, loadLevel);
      emptyCriteria = guidOrHrids.isEmpty();
   }

   public ArtifactQueryBuilder(Collection<Integer> artifactIds, TransactionRecord transactionId, DeletionFlag allowDeleted, LoadLevel loadLevel) throws OseeCoreException {
      this(artifactIds, 0, null, null, null, transactionId.getBranch(), transactionId, allowDeleted, loadLevel);
      emptyCriteria = artifactIds.isEmpty();
   }

   public ArtifactQueryBuilder(int artifactId, TransactionRecord transactionId, DeletionFlag allowDeleted, LoadLevel loadLevel) throws OseeCoreException {
      this(null, artifactId, null, null, null, transactionId.getBranch(), transactionId, allowDeleted, loadLevel);
   }

   public ArtifactQueryBuilder(String guidOrHrid, IOseeBranch branch, DeletionFlag allowDeleted, LoadLevel loadLevel) throws OseeCoreException {
      this(null, 0, null, ensureValid(guidOrHrid), null, branch, null, allowDeleted, loadLevel);
   }

   public ArtifactQueryBuilder(IArtifactType artifactType, IOseeBranch branch, LoadLevel loadLevel, DeletionFlag allowDeleted) {
      this(null, 0, null, null, Arrays.asList(artifactType), branch, null, allowDeleted, loadLevel);
   }

   public ArtifactQueryBuilder(Collection<? extends IArtifactType> artifactTypes, IOseeBranch branch, LoadLevel loadLevel, DeletionFlag allowDeleted) {
      this(null, 0, null, null, artifactTypes, branch, null, allowDeleted, loadLevel);
      emptyCriteria = artifactTypes.isEmpty();
   }

   public ArtifactQueryBuilder(IOseeBranch branch, LoadLevel loadLevel, DeletionFlag allowDeleted) {
      this(null, 0, null, null, null, branch, null, allowDeleted, loadLevel);
   }

   public ArtifactQueryBuilder(IOseeBranch branch, LoadLevel loadLevel, DeletionFlag allowDeleted, ArtifactSearchCriteria... criteria) {
      this(null, 0, null, null, null, branch, null, allowDeleted, loadLevel, criteria);
      emptyCriteria = criteria.length == 0;
   }

   public ArtifactQueryBuilder(IOseeBranch branch, LoadLevel loadLevel, List<ArtifactSearchCriteria> criteria) {
      this(null, 0, null, null, null, branch, null, EXCLUDE_DELETED, loadLevel, toArray(criteria));
      emptyCriteria = criteria.isEmpty();
   }

   public ArtifactQueryBuilder(IArtifactType artifactType, IOseeBranch branch, LoadLevel loadLevel, ArtifactSearchCriteria... criteria) {
      this(null, 0, null, null, Arrays.asList(artifactType), branch, null, EXCLUDE_DELETED, loadLevel, criteria);
      emptyCriteria = criteria.length == 0;
   }

   public ArtifactQueryBuilder(IArtifactType artifactType, IOseeBranch branch, LoadLevel loadLevel, List<ArtifactSearchCriteria> criteria) {
      this(null, 0, null, null, Arrays.asList(artifactType), branch, null, EXCLUDE_DELETED, loadLevel,
         toArray(criteria));
      emptyCriteria = criteria.isEmpty();
   }

   private ArtifactQueryBuilder(Collection<Integer> artifactIds, int artifactId, List<String> guidOrHrids, String guidOrHrid, Collection<? extends IArtifactType> artifactTypes, IOseeBranch branch, TransactionRecord transactionId, DeletionFlag allowDeleted, LoadLevel loadLevel, ArtifactSearchCriteria... criteria) {
      this.artifactTypes = artifactTypes;
      this.branch = branch;
      this.criteria = criteria;
      this.loadLevel = loadLevel;
      this.allowDeleted = allowDeleted;
      this.guidOrHrid = guidOrHrid;
      this.artifactId = artifactId;
      this.transactionId = transactionId;
      if (artifactIds != null && !artifactIds.isEmpty()) {
         if (artifactIds.size() == 1) {
            this.artifactId = artifactIds.iterator().next();
         } else {
            this.artifactIds = artifactIds;
         }
      }

      if (guidOrHrids != null && !guidOrHrids.isEmpty()) {
         if (guidOrHrids.size() == 1) {
            this.guidOrHrid = guidOrHrids.get(0);
         } else {
            hrids = new ArrayList<String>();
            guids = new ArrayList<String>();
            for (String id : guidOrHrids) {
               if (GUID.isValid(id)) {
                  guids.add(id);
               } else {
                  hrids.add(id);
               }
            }
         }
      }

   }

   private static ArtifactSearchCriteria[] toArray(List<ArtifactSearchCriteria> criteria) {
      return criteria.toArray(new ArtifactSearchCriteria[criteria.size()]);
   }

   private static String ensureValid(String id) throws OseeCoreException {
      boolean guidCheck = GUID.isValid(id);
      boolean hridCheck = HumanReadableId.isValid(id);
      if (!guidCheck && !hridCheck) {
         throw new OseeArgumentException("Invalid hrid/guid detected [%s]", id);
      }
      return id;
   }

   private boolean useServerSearch() {
      return (Conditions.hasValues(artifactTypes) || guidOrHrid != null || Conditions.hasValues(guids) || Conditions.hasValues(hrids) || criteria.length > 0 || (artifactId == 0 && !Conditions.hasValues(artifactIds)));
   }

   private QueryBuilder getQueryBuilder() throws OseeCoreException {
      QueryBuilder toReturn;
      if (useServerSearch()) {
         OseeClient client = ServiceUtil.getOseeClient();
         toReturn = client.createQueryBuilder(branch);
      } else {
         LocalIdQueryBuilder builder = new LocalIdQueryBuilder(branch);

         Class<?>[] types = new Class<?>[] {QueryBuilder.class};
         toReturn = (QueryBuilder) Proxy.newProxyInstance(QueryBuilder.class.getClassLoader(), types, builder);
      }
      return toReturn;
   }

   private QueryBuilder createOrcsQuery() throws OseeCoreException {

      QueryBuilder builder = getQueryBuilder();

      if (allowDeleted == INCLUDE_DELETED) {
         builder.includeDeleted();
      }

      if (artifactId != 0 || Conditions.hasValues(artifactIds)) {
         if (Conditions.hasValues(artifactIds)) {
            builder.andLocalIds(artifactIds);
         } else if (artifactId != 0) {
            builder.andLocalId(artifactId);
         }
      }

      if (Conditions.hasValues(artifactTypes)) {
         builder.andIsOfType(artifactTypes);
      }

      if (guidOrHrid != null) {
         builder.andGuidsOrHrids(guidOrHrid);
      }

      if (Conditions.hasValues(guids)) {
         builder.andGuidsOrHrids(guids);
      }

      if (Conditions.hasValues(hrids)) {
         builder.andGuidsOrHrids(hrids);
      }

      if (criteria.length > 0) {
         for (ArtifactSearchCriteria idx : criteria) {
            idx.addToQueryBuilder(builder);
         }
      }

      boolean isHistorical = transactionId != null;
      if (isHistorical) {
         builder.fromTransaction(transactionId.getId());
      }

      return builder;
   }

   public List<Artifact> getArtifacts(int artifactCountEstimate, ISearchConfirmer confirmer) throws OseeCoreException {
      return internalGetArtifacts(artifactCountEstimate, confirmer, INCLUDE_CACHE);
   }

   public List<Artifact> reloadArtifacts(int artifactCountEstimate) throws OseeCoreException {
      return internalGetArtifacts(artifactCountEstimate, null, RELOAD_CACHE);
   }

   public Artifact reloadArtifact() throws OseeCoreException {
      if (emptyCriteria) {
         throw new ArtifactDoesNotExist("received an empty list in the criteria for this search");
      }
      Collection<Artifact> artifacts = internalGetArtifacts(1, null, RELOAD_CACHE);

      if (artifacts.isEmpty()) {
         throw new ArtifactDoesNotExist(getSoleExceptionMessage(artifacts.size()));
      }
      if (artifacts.size() > 1) {
         throw new MultipleArtifactsExist(getSoleExceptionMessage(artifacts.size()));
      }
      return artifacts.iterator().next();
   }

   private List<Artifact> loadArtifactsFromServerIds(LoadType reload) throws OseeCoreException {
      List<Integer> ids = createOrcsQuery().getSearchResult().getIds();
      List<Artifact> artifacts =
         ArtifactLoader.loadArtifacts(ids, branch, loadLevel, reload, allowDeleted, transactionId);
      return artifacts;
   }

   private List<Artifact> internalGetArtifacts(int artifactCountEstimate, ISearchConfirmer confirmer, LoadType reload) throws OseeCoreException {
      if (emptyCriteria) {
         return java.util.Collections.emptyList();
      }

      List<Artifact> artifactsFromServerIds = loadArtifactsFromServerIds(reload);
      return artifactsFromServerIds;
   }

   public List<Integer> selectArtifacts(int artifactCountEstimate) throws OseeCoreException {
      return createOrcsQuery().getSearchResult().getIds();
   }

   public int countArtifacts() throws OseeCoreException {
      if (emptyCriteria) {
         return 0;
      } else {
         return createOrcsQuery().getCount();
      }
   }

   protected Artifact getOrCheckArtifact(QueryType queryType) throws OseeCoreException {
      if (emptyCriteria) {
         throw new ArtifactDoesNotExist("received an empty list in the criteria for this search");
      }
      Collection<Artifact> artifacts = getArtifacts(1, null);

      if (artifacts.isEmpty()) {
         if (queryType.equals(QueryType.CHECK)) {
            return null;
         }
         throw new ArtifactDoesNotExist(getSoleExceptionMessage(artifacts.size()));
      }
      if (artifacts.size() > 1) {
         throw new MultipleArtifactsExist(getSoleExceptionMessage(artifacts.size()));
      }
      return artifacts.iterator().next();
   }

   private String getSoleExceptionMessage(int artifactCount) {
      StringBuilder message = new StringBuilder(250);
      if (artifactCount == 0) {
         message.append("ArtifactQueryBuilder: No artifact found");
      } else {
         message.append(artifactCount);
         message.append(" artifacts found");
      }
      if (artifactTypes != null) {
         message.append(" with type(s): ");
         message.append(artifactTypes);
      }
      if (artifactId != 0) {
         message.append(" with id \"");
         message.append(artifactId);
         message.append("\"");
      }
      if (guidOrHrid != null) {
         message.append(" with id \"");
         message.append(guidOrHrid);
         message.append("\"");
      }
      if (criteria.length > 0) {
         message.append(" with criteria \"");
         message.append(Arrays.deepToString(criteria));
         message.append("\"");
      }
      message.append(" on branch \"");
      message.append(branch.getGuid());
      message.append("\"");
      return message.toString();
   }

   private static final class LocalIdQueryBuilder implements InvocationHandler {

      private final Set<Integer> localIds = new LinkedHashSet<Integer>();
      private DeletionFlag allowDeleted = EXCLUDE_DELETED;
      private int txId = -1;
      private final IOseeBranch branch;

      public LocalIdQueryBuilder(IOseeBranch branch) {
         super();
         this.branch = branch;
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         Object toReturn = null;

         Method localMethod = getMethodFor(getClass(), method);
         if (localMethod != null) {
            toReturn = localMethod.invoke(this, args);
            if (toReturn == null) {
               toReturn = proxy;
            }
         } else {
            throw new UnsupportedOperationException();
         }
         return toReturn;
      }

      private Method getMethodFor(Class<?> clazz, Method method) {
         Method toReturn = null;
         try {
            toReturn = clazz.getMethod(method.getName(), method.getParameterTypes());
         } catch (Exception ex) {
            // Do Nothing;
         }
         return toReturn;
      }

      @SuppressWarnings("unused")
      public void fromTransaction(int transactionId) {
         txId = transactionId;
      }

      @SuppressWarnings("unused")
      public void includeDeleted() {
         includeDeleted(true);
      }

      public void includeDeleted(boolean enabled) {
         allowDeleted = enabled ? INCLUDE_DELETED : EXCLUDE_DELETED;
      }

      @SuppressWarnings("unused")
      public SearchResult getSearchResult() throws OseeCoreException {
         SearchResponse response = new SearchResponse();
         List<Integer> ids = new LinkedList<Integer>(localIds);
         response.setIds(ids);
         return response;
      }

      @SuppressWarnings("unused")
      public int getCount() throws OseeCoreException {
         TransactionRecord tx;
         if (txId == -1) {
            tx = TransactionManager.getHeadTransaction(branch);
         } else {
            tx = TransactionManager.getTransactionId(txId);
         }
         List<Artifact> results =
            ArtifactLoader.loadArtifacts(localIds, branch, LoadLevel.SHALLOW, LoadType.INCLUDE_CACHE, allowDeleted, tx);
         return results.size();
      }

      @SuppressWarnings("unused")
      public void andLocalId(int... artifactId) {
         for (int id : artifactId) {
            localIds.add(id);
         }
      }

      @SuppressWarnings("unused")
      public void andLocalIds(Collection<Integer> artifactIds) {
         localIds.addAll(artifactIds);
      }
   }

}