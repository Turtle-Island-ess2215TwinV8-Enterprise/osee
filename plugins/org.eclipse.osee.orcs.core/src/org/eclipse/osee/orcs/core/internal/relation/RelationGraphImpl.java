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
package org.eclipse.osee.orcs.core.internal.relation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.data.ResultSetList;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.model.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.orcs.core.internal.ArtifactLoaderFactory;
import org.eclipse.osee.orcs.core.internal.SessionContext;
import org.eclipse.osee.orcs.core.internal.proxy.HasProxiedObject;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.GraphReadable;

/**
 * @author Andrew M. Finkbeiner
 */
public class RelationGraphImpl implements GraphReadable {

   private final SessionContext sessionContext;
   private final ArtifactLoaderFactory loader;
   private final ArtifactTypeCache artifactTypeCache;
   private final RelationTypeCache relationTypeCache;
   private final RelationTypeValidity validity;

   public RelationGraphImpl(SessionContext sessionContext, ArtifactLoaderFactory loader, ArtifactTypeCache artifactTypeCache, RelationTypeCache relationTypeCache, RelationTypeValidity validity) {
      super();
      this.sessionContext = sessionContext;
      this.loader = loader;
      this.artifactTypeCache = artifactTypeCache;
      this.relationTypeCache = relationTypeCache;
      this.validity = validity;
   }

   private RelationContainer getRelationContainer(ArtifactReadable readable) {
      RelationContainer toReturn = null;
      Object object = readable;
      if (object instanceof HasProxiedObject) {
         object = ((HasProxiedObject<?>) readable).getProxiedObject();
      }
      if (object instanceof HasRelationContainer) {
         HasRelationContainer proxy = (HasRelationContainer) object;
         toReturn = proxy.getRelationContainer();
      }
      return toReturn;
   }

   private List<ArtifactReadable> loadRelated(IOseeBranch branch, Collection<Integer> artifactIds) throws OseeCoreException {
      return loader.fromBranchAndArtifactIds(sessionContext, branch, artifactIds).setLoadLevel(LoadLevel.FULL).load();
   }

   private void loadRelatedArtifactIds(ArtifactReadable art, IRelationTypeSide relationTypeSide, Collection<Integer> results) {
      RelationContainer container = getRelationContainer(art);
      container.getArtifactIds(results, relationTypeSide);
   }

   @Override
   public ArtifactReadable getParent(ArtifactReadable art) throws OseeCoreException {
      return getRelatedArtifacts(CoreRelationTypes.Default_Hierarchical__Parent, art).getExactlyOne();
   }

   @Override
   public ResultSet<ArtifactReadable> getChildren(ArtifactReadable art) throws OseeCoreException {
      return getRelatedArtifacts(CoreRelationTypes.Default_Hierarchical__Child, art);
   }

   @Override
   public Collection<IRelationTypeSide> getExistingRelationTypes(ArtifactReadable art) {
      RelationContainer container = getRelationContainer(art);
      return container.getExistingRelationTypes();
   }

   @Override
   public ResultSet<ArtifactReadable> getRelatedArtifacts(IRelationTypeSide relationTypeSide, ArtifactReadable art) throws OseeCoreException {
      List<Integer> artIds = new ArrayList<Integer>();
      loadRelatedArtifactIds(art, relationTypeSide, artIds);
      List<ArtifactReadable> toReturn;
      if (artIds.isEmpty()) {
         toReturn = Collections.emptyList();
      } else {
         toReturn = loadRelated(art.getBranch(), artIds);
      }
      return new ResultSetList<ArtifactReadable>(toReturn);
   }

   @Override
   public List<RelationType> getValidRelationTypes(ArtifactReadable art) throws OseeCoreException {
      ArtifactType artifactType = artifactTypeCache.get(art.getArtifactType());
      return validity.getValidRelationTypes(artifactType);
   }

   @Override
   public int getMaximumRelationAllowed(IArtifactType artifactType, IRelationTypeSide relationTypeSide) throws OseeCoreException {
      ArtifactType type = artifactTypeCache.get(artifactType);
      return validity.getMaximumRelationAllowed(type, relationTypeSide);
   }

   @Override
   public RelationType getFullRelationType(IRelationTypeSide relationTypeSide) throws OseeCoreException {
      return relationTypeCache.get(relationTypeSide);
   }

}
