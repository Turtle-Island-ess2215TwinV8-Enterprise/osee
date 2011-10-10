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
package org.eclipse.osee.orcs.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.executor.admin.HasCancellation;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.ArtifactRowHandler;
import org.eclipse.osee.orcs.core.ds.AttributeContainer;
import org.eclipse.osee.orcs.core.ds.AttributeRowHandler;
import org.eclipse.osee.orcs.core.ds.AttributeRowHandlerFactory;
import org.eclipse.osee.orcs.core.ds.DataLoader;
import org.eclipse.osee.orcs.core.ds.LoadOptions;
import org.eclipse.osee.orcs.core.ds.QueryContext;
import org.eclipse.osee.orcs.core.ds.RelationContainer;
import org.eclipse.osee.orcs.core.ds.RelationRowHandler;
import org.eclipse.osee.orcs.core.ds.RelationRowHandlerFactory;
import org.eclipse.osee.orcs.core.internal.artifact.Artifact;
import org.eclipse.osee.orcs.core.internal.artifact.ArtifactCollector;
import org.eclipse.osee.orcs.core.internal.artifact.ArtifactFactory;
import org.eclipse.osee.orcs.core.internal.artifact.ArtifactRowMapper;
import org.eclipse.osee.orcs.core.internal.attribute.AttributeFactory;
import org.eclipse.osee.orcs.core.internal.attribute.AttributeRowMapper;
import org.eclipse.osee.orcs.core.internal.relation.RelationRowMapper;
import org.eclipse.osee.orcs.data.ReadableArtifact;

/**
 * @author Andrew M. Finkbeiner
 */
public class OrcsObjectLoader {

   private final DataLoader dataLoader;
   private final Log logger;
   private final ArtifactTypeCache artifactTypeCache;
   private final BranchCache branchCache;
   private final ArtifactFactory artifactFactory;
   private final AttributeFactory attributeFactory;

   public OrcsObjectLoader(Log logger, DataLoader dataLoader, ArtifactFactory artifactFactory, AttributeFactory attributeFactory, ArtifactTypeCache artifactTypeCache, BranchCache branchCache) {
      super();
      this.logger = logger;
      this.dataLoader = dataLoader;
      this.artifactFactory = artifactFactory;
      this.attributeFactory = attributeFactory;

      this.artifactTypeCache = artifactTypeCache;
      this.branchCache = branchCache;
   }

   public int countObjects(HasCancellation cancellation, QueryContext queryContext) throws OseeCoreException {
      int count = -1;
      long startTime = 0;
      if (logger.isTraceEnabled()) {
         startTime = System.currentTimeMillis();
      }

      count = dataLoader.countArtifacts(cancellation, queryContext);

      if (logger.isTraceEnabled()) {
         logger.trace("Counted objects in [%s]", Lib.getElapseString(startTime));
      }
      return count;
   }

   public List<ReadableArtifact> load(HasCancellation cancellation, IOseeBranch branch, Collection<Integer> ids, LoadOptions loadOptions, SessionContext sessionContext) throws OseeCoreException {
      long startTime = 0;
      if (logger.isTraceEnabled()) {
         startTime = System.currentTimeMillis();
      }

      List<ReadableArtifact> artifacts = new ArrayList<ReadableArtifact>();

      ArtifactCollectorImpl artifactHandler = new ArtifactCollectorImpl(logger, attributeFactory, artifacts);

      ArtifactRowHandler artifactRowHandler =
         new ArtifactRowMapper(sessionContext, branchCache, artifactTypeCache, artifactFactory, artifactHandler);

      Branch fullBranch = branchCache.get(branch);

      dataLoader.loadArtifacts(cancellation, artifactRowHandler, fullBranch.getId(), ids, loadOptions, artifactHandler,
         artifactHandler);
      if (logger.isTraceEnabled()) {
         logger.trace("Objects from ids loaded in [%s]", Lib.getElapseString(startTime));
      }
      return artifacts;
   }

   public List<ReadableArtifact> load(HasCancellation cancellation, QueryContext queryContext, LoadOptions loadOptions, SessionContext sessionContext) throws OseeCoreException {
      long startTime = 0;
      if (logger.isTraceEnabled()) {
         startTime = System.currentTimeMillis();
      }

      List<ReadableArtifact> artifacts = new ArrayList<ReadableArtifact>();

      ArtifactCollectorImpl artifactHandler = new ArtifactCollectorImpl(logger, attributeFactory, artifacts);

      ArtifactRowHandler artifactRowHandler =
         new ArtifactRowMapper(sessionContext, branchCache, artifactTypeCache, artifactFactory, artifactHandler);

      dataLoader.loadArtifacts(cancellation, artifactRowHandler, queryContext, loadOptions, artifactHandler,
         artifactHandler);

      if (logger.isTraceEnabled()) {
         logger.trace("Objects from query loaded in [%s]", Lib.getElapseString(startTime));
      }
      return artifacts;
   }

   private static class ArtifactCollectorImpl implements ArtifactCollector, RelationRowHandlerFactory, AttributeRowHandlerFactory {

      private final Map<Integer, RelationContainer> relationContainers = new HashMap<Integer, RelationContainer>();;
      private final Map<Integer, AttributeContainer> attributeContainers = new HashMap<Integer, AttributeContainer>();

      private final List<ReadableArtifact> artifacts;

      private final Log logger;
      private final AttributeFactory attributeFactory;

      public ArtifactCollectorImpl(Log logger, AttributeFactory attributeFactory, List<ReadableArtifact> artifacts) {
         this.logger = logger;
         this.attributeFactory = attributeFactory;
         this.artifacts = artifacts;
      }

      @Override
      public AttributeRowHandler createAttributeRowHandler() {
         return new AttributeRowMapper(logger, attributeFactory, attributeContainers);
      }

      @Override
      public RelationRowHandler createRelationRowHandler() {
         return new RelationRowMapper(relationContainers);
      }

      @Override
      public void onArtifact(ReadableArtifact artifact, boolean isArtifactAlreadyLoaded) {
         artifacts.add(artifact);
         relationContainers.put(artifact.getId(), ((Artifact) artifact).getRelationContainer());
         attributeContainers.put(artifact.getId(), ((Artifact) artifact).getAttributeContainer());
      }

   }

}