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
package org.eclipse.osee.orcs.db.internal.loader;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.executor.admin.HasCancellation;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.ArtifactBuilder;
import org.eclipse.osee.orcs.core.ds.DataLoader;
import org.eclipse.osee.orcs.core.ds.LoadOptions;
import org.eclipse.osee.orcs.db.internal.loader.criteria.CriteriaArtifact;
import org.eclipse.osee.orcs.db.internal.loader.criteria.CriteriaAttribute;
import org.eclipse.osee.orcs.db.internal.loader.criteria.CriteriaOrcsLoad;
import org.eclipse.osee.orcs.db.internal.loader.criteria.CriteriaRelation;
import org.eclipse.osee.orcs.db.internal.loader.executors.AbstractLoadExecutor;

/**
 * @author Roberto E. Escobar
 */
public class DataLoaderImpl implements DataLoader {

   private final LoadOptions loadOptions = new LoadOptions();

   private final Collection<Integer> attributeIds = new HashSet<Integer>();
   private final Collection<IAttributeType> attributeTypes = new HashSet<IAttributeType>();

   private final Collection<Integer> relationIds = new HashSet<Integer>();
   private final Collection<IRelationType> relationTypes = new HashSet<IRelationType>();

   private final Log logger;
   private final AbstractLoadExecutor loadExecutor;

   public DataLoaderImpl(Log logger, AbstractLoadExecutor loadExecutor) {
      this.logger = logger;
      this.loadExecutor = loadExecutor;
   }

   @Override
   public DataLoader resetToDefaults() {
      loadOptions.reset();

      attributeIds.clear();
      attributeTypes.clear();

      relationIds.clear();
      relationTypes.clear();
      return this;
   }

   @Override
   public DataLoader includeDeleted() {
      includeDeleted(true);
      return this;
   }

   @Override
   public DataLoader includeDeleted(boolean enabled) {
      loadOptions.setIncludeDeleted(enabled);
      return this;
   }

   @Override
   public boolean areDeletedIncluded() {
      return loadOptions.areDeletedIncluded();
   }

   @Override
   public DataLoader fromTransaction(int transactionId) {
      loadOptions.setFromTransaction(transactionId);
      return this;
   }

   @Override
   public int getFromTransaction() {
      return loadOptions.getFromTransaction();
   }

   @Override
   public DataLoader headTransaction() {
      loadOptions.setHeadTransaction();
      return this;
   }

   @Override
   public boolean isHeadTransaction() {
      return !loadOptions.isHistorical();
   }

   @Override
   public LoadLevel getLoadLevel() {
      return loadOptions.getLoadLevel();
   }

   @Override
   public DataLoader setLoadLevel(LoadLevel loadLevel) {
      loadOptions.setLoadLevel(loadLevel);
      return this;
   }

   @Override
   public DataLoader loadAttributeType(IAttributeType... attributeType) throws OseeCoreException {
      return loadAttributeTypes(Arrays.asList(attributeType));
   }

   @SuppressWarnings("unused")
   @Override
   public DataLoader loadAttributeTypes(Collection<? extends IAttributeType> attributeTypes) throws OseeCoreException {
      this.attributeTypes.addAll(attributeTypes);
      return this;
   }

   @Override
   public DataLoader loadRelationType(IRelationType... relationType) throws OseeCoreException {
      return loadRelationTypes(Arrays.asList(relationType));
   }

   @SuppressWarnings("unused")
   @Override
   public DataLoader loadRelationTypes(Collection<? extends IRelationType> relationTypes) throws OseeCoreException {
      this.relationTypes.addAll(relationTypes);
      return this;
   }

   @Override
   public DataLoader loadAttributeLocalId(int... attributeIds) throws OseeCoreException {
      return loadAttributeLocalIds(toCollection(attributeIds));
   }

   @SuppressWarnings("unused")
   @Override
   public DataLoader loadAttributeLocalIds(Collection<Integer> attributeIds) throws OseeCoreException {
      this.attributeIds.addAll(attributeIds);
      return this;
   }

   @Override
   public DataLoader loadRelationLocalId(int... relationIds) throws OseeCoreException {
      return loadRelationLocalIds(toCollection(relationIds));
   }

   @SuppressWarnings("unused")
   @Override
   public DataLoader loadRelationLocalIds(Collection<Integer> relationIds) throws OseeCoreException {
      this.relationIds.addAll(relationIds);
      return this;
   }

   private Collection<Integer> toCollection(int... ids) {
      Set<Integer> toReturn = new HashSet<Integer>();
      for (Integer id : ids) {
         toReturn.add(id);
      }
      return toReturn;
   }

   private <T> Collection<T> copy(Collection<T> source) {
      Collection<T> toReturn = new HashSet<T>();
      for (T item : source) {
         toReturn.add(item);
      }
      return toReturn;
   }

   ////////////////////// EXECUTE METHODS
   @Override
   public void load(HasCancellation cancellation, ArtifactBuilder builder) throws OseeCoreException {
      long startTime = 0;

      final LoadOptions options = loadOptions.clone();
      final CriteriaOrcsLoad criteria = createCriteria();
      if (logger.isTraceEnabled()) {
         startTime = System.currentTimeMillis();
         logger.trace("%s [start] - [%s] [%s]", getClass().getSimpleName(), criteria, options);
      }

      loadExecutor.load(cancellation, builder, criteria, options);

      if (logger.isTraceEnabled()) {
         logger.trace("%s [%s] - loaded [%s] [%s]", getClass().getSimpleName(), Lib.getElapseString(startTime),
            criteria, options);
      }
   }

   private CriteriaOrcsLoad createCriteria() {
      CriteriaArtifact artifactCriteria = new CriteriaArtifact();
      CriteriaAttribute attributeCriteria = new CriteriaAttribute(copy(attributeIds), copy(attributeTypes));
      CriteriaRelation relationCriteria = new CriteriaRelation(copy(relationIds), copy(relationTypes));
      return new CriteriaOrcsLoad(artifactCriteria, attributeCriteria, relationCriteria);
   }
}
