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
package org.eclipse.osee.orcs.core.internal.relation.experimental;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.model.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.internal.ArtifactLoader;
import org.eclipse.osee.orcs.core.internal.ArtifactLoaderFactory;
import org.eclipse.osee.orcs.core.internal.SessionContext;
import org.eclipse.osee.orcs.core.internal.relation.RelationFactory;
import org.eclipse.osee.orcs.core.internal.relation.RelationsReadableImpl;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.RelationsReadable;

/**
 * @author Andrew M. Finkbeiner
 */
public class RelationManagerImpl {

   private final Set<RelationLink> loadedLinks = new HashSet<RelationLink>();

   private Log logger;
   private ArtifactLoaderFactory loadFactory;
   private SessionContext sessionContext;
   private RelationFactory relationFactory;

   private final ArtifactTypeCache artifactTypeCache;
   private final RelationTypeCache relationTypeCache;

   public RelationManagerImpl(ArtifactTypeCache artifactTypeCache, RelationTypeCache relationTypeCache) {
      super();
      this.artifactTypeCache = artifactTypeCache;
      this.relationTypeCache = relationTypeCache;
   }

   private void add(RelationData relationData) {

      //      
      //      
      //      if(){
      //         relationData.get
      //      } else {
      //         relationFactory.createRelationLink(relationData);
      //      }
   }

   private void manageRelation(RelationLink relation, RelationLink newRelation) {

   }

   private void checkRelation(RelationData data1, RelationData data2) {
      if (data1.getArtIdA() == data2.getArtIdA() && data1.getArtIdB() == data2.getArtIdB() && //
      data1.getTypeUuid() == data2.getTypeUuid() && //
      data1 != data2) {
         logger.warn("Duplicate relation objects for same relation for RELATION 1 [%s] RELATION 2 [%s]", data1, data2);
      }
   }

   private ArtifactReadable loadArtifact(IOseeBranch branch, int id) throws OseeCoreException {
      ArtifactLoader loader = loadFactory.fromBranchAndArtifactIds(sessionContext, branch, id);
      loader.setLoadLevel(LoadLevel.FULL);
      return loader.getResults().getExactlyOne();
   }

   private List<ArtifactReadable> loadArtifacts(IOseeBranch branch, Collection<Integer> artifactIds) throws OseeCoreException {
      ArtifactLoader loader = loadFactory.fromBranchAndArtifactIds(sessionContext, branch, artifactIds);
      loader.setLoadLevel(LoadLevel.FULL);
      return loader.load();
   }

   //////////////////////////VALIDITY///////////////////////
   public int getMaximumRelationAllowed(IArtifactType artifactType, IRelationTypeSide relationTypeSide) throws OseeCoreException {
      int toReturn = 0;
      ArtifactType artType = asArtifactType(artifactType);
      RelationType relationType = asRelationType(relationTypeSide);
      RelationSide relationSide = relationTypeSide.getSide();
      if (relationType.isArtifactTypeAllowed(relationSide, artType)) {
         toReturn = relationType.getMultiplicity().getLimit(relationSide);
      }
      return toReturn;
   }

   public List<RelationType> getValidRelationTypes(ArtifactReadable artifact) throws OseeCoreException {
      ArtifactType artifactType = asArtifactType(artifact.getArtifactType());
      Collection<RelationType> relationTypes = relationTypeCache.getAll();
      List<RelationType> validRelationTypes = new ArrayList<RelationType>();
      for (RelationType relationType : relationTypes) {
         int sideAMax = getRelationSideMax(relationType, artifactType, RelationSide.SIDE_A);
         int sideBMax = getRelationSideMax(relationType, artifactType, RelationSide.SIDE_B);
         boolean onSideA = sideBMax > 0;
         boolean onSideB = sideAMax > 0;
         if (onSideA || onSideB) {
            validRelationTypes.add(relationType);
         }
      }
      return validRelationTypes;
   }

   private int getRelationSideMax(RelationType relationType, ArtifactType artifactType, RelationSide relationSide) throws OseeCoreException {
      int toReturn = 0;
      if (relationType.isArtifactTypeAllowed(relationSide, artifactType)) {
         toReturn = relationType.getMultiplicity().getLimit(relationSide);
      }
      return toReturn;
   }

   ///////////////////////////////////////////////////////////////////////

   private void loadRelatedArtifactIds(ArtifactReadable art, IRelationTypeSide relationTypeSide, Collection<Integer> results) {
      //      RelationContainer container = getRelationContainer(art);
      //      container.getArtifactIds(results, relationTypeSide);
   }

   public RelationsReadable getRelatedArtifacts(IRelationTypeSide relationTypeSide, ArtifactReadable art) throws OseeCoreException {
      List<Integer> artIds = new ArrayList<Integer>();
      loadRelatedArtifactIds(art, relationTypeSide, artIds);
      List<ArtifactReadable> toReturn;
      if (artIds.isEmpty()) {
         toReturn = Collections.emptyList();
      } else {
         toReturn = loadArtifacts(art.getBranch(), artIds);
      }
      return new RelationsReadableImpl(toReturn);
   }

   ///////////////////////////////////////////////////////////////////////

   private RelationType asRelationType(IRelationTypeSide relationTypeSide) throws OseeCoreException {
      return relationTypeCache.get(relationTypeSide);
   }

   private ArtifactType asArtifactType(IArtifactType artifactType) throws OseeCoreException {
      ArtifactType toReturn = null;
      if (artifactType instanceof ArtifactType) {
         toReturn = (ArtifactType) artifactType;
      } else {
         toReturn = artifactTypeCache.get(artifactType);
      }
      return toReturn;
   }

   ///////////////////////////////////////////////////////////////////////

   //   public ArtifactReadable getParent(ArtifactReadable art) throws OseeCoreException {
   //      return getRelatedArtifacts(CoreRelationTypes.Default_Hierarchical__Parent, art).getExactlyOne();
   //   }
   //
   //   public RelationsReadable getChildren(ArtifactReadable art) throws OseeCoreException {
   //      return getRelatedArtifacts(CoreRelationTypes.Default_Hierarchical__Child, art);
   //   }
   //   private RelationContainer getRelationContainer(ArtifactReadable readable) {
   //      RelationContainer toReturn = null;
   //      Object object = readable;
   //      if (object instanceof HasProxiedObject) {
   //         object = ((HasProxiedObject<?>) readable).getProxiedObject();
   //      }
   //      if (object instanceof HasRelationContainer) {
   //         HasRelationContainer proxy = (HasRelationContainer) object;
   //         toReturn = proxy.getRelationContainer();
   //      }
   //      return toReturn;
   //   }
   //
   //   public ArtifactWriteable getWriteableParent(ArtifactReadable otherArtifact) throws OseeCoreException {
   //      // TX_TODO
   //      return null;
   //   }
   //
   //   public RelationsWriteable getWriteableChildren(ArtifactReadable otherArtifact) throws OseeCoreException {
   //      // TX_TODO
   //      return null;
   //   }
   //
   //   public RelationsWriteable getWriteableRelatedArtifacts(IRelationTypeSide relationTypeSide) throws OseeCoreException {
   //      // TX_TODO
   //      return null;
   //   }
   //
   //   public void createRelation(ArtifactReadable aArtifact, IRelationTypeSide relationTypeSide, ArtifactReadable otherArtifact) throws OseeCoreException {
   //      // TX_TODO
   //   }
   //
   //   public void createRelation(ArtifactReadable aArtifact, IRelationSorterId sorterId, IRelationTypeSide relationTypeSide, ArtifactReadable otherArtifact) throws OseeCoreException {
   //      // TX_TODO
   //   }
   //
   //   public void deleteRelation(ArtifactReadable aArtifact, IRelationType relationTypeSide, ArtifactReadable otherArtifact) throws OseeCoreException {
   //      // TX_TODO
   //   }
   //
   //   public void deleteRelations(ArtifactReadable aArtifact, IRelationTypeSide relationTypeSide) throws OseeCoreException {
   //      // TX_TODO
   //   }

}
