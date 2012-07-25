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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.Graph;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.HasRelations;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.LinkResolver;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Roberto E. Escobar
 */
public class GraphImpl implements Graph {

   private final LinkResolver<IOseeBranch, ArtifactReadable> loader;

   public GraphImpl(LinkResolver<IOseeBranch, ArtifactReadable> loader) {
      super();
      this.loader = loader;
   }

   @Override
   public ArtifactReadable getParent(IOseeBranch branch, HasRelations child) throws OseeCoreException {
      List<ArtifactReadable> toReturn = getRelated(branch, child, CoreRelationTypes.Default_Hierarchical__Parent);
      return toReturn.isEmpty() ? null : toReturn.iterator().next();
   }

   @Override
   public List<ArtifactReadable> getChildren(IOseeBranch branch, HasRelations parent) throws OseeCoreException {
      return getRelated(branch, parent, CoreRelationTypes.Default_Hierarchical__Child);
   }

   @Override
   public List<ArtifactReadable> getRelated(IOseeBranch branch, HasRelations hasRelations, IRelationTypeSide relationTypeSide) throws OseeCoreException {
      RelationCollection linksCollection = hasRelations.getRelations();
      List<Relation> links = linksCollection.getList(relationTypeSide, DeletionFlag.EXCLUDE_DELETED);
      RelationSide side = relationTypeSide.getSide();
      loader.resolve(branch, side, links);

      List<ArtifactReadable> toReturn = new ArrayList<ArtifactReadable>();
      for (Relation link : links) {
         toReturn.add(link.getArtifact(side));
      }
      return toReturn;
   }

   @Override
   public void unrelate(IOseeBranch branch, HasRelations hasRelations) throws OseeCoreException {
      unrelate(branch, hasRelations, true);
   }

   private void unrelate(IOseeBranch branch, HasRelations hasRelations, boolean reorderRelations) throws OseeCoreException {
      if (hasRelations.isDeleteAllowed()) {
         RelationCollection linksCollection = hasRelations.getRelations();
         List<Relation> links = linksCollection.getList(DeletionFlag.EXCLUDE_DELETED);
         loader.resolve(branch, links);

         // This must be done first since the the actual deletion of an
         // artifact clears out the link manager
         for (ArtifactReadable childArtifact : getChildren(branch, hasRelations)) {
            HasRelations child = (HasRelations) childArtifact;
            unrelate(branch, child, false);
         }
         try {
            hasRelations.delete();
            unrelateAll(hasRelations, reorderRelations);
         } catch (OseeCoreException ex) {
            hasRelations.unDelete();
            throw ex;
         }
      }
   }

   private void unrelateAll(HasRelations hasRelations, boolean reorderRelations) throws OseeCoreException {
      List<Relation> links = hasRelations.getRelations().getList(DeletionFlag.INCLUDE_DELETED);

      Map<IRelationType, RelationSide> typesToUpdate = new HashMap<IRelationType, RelationSide>();
      for (Relation link : links) {
         RelationSide side = getSide(link, hasRelations).oppositeSide();
         typesToUpdate.put(link.getRelationType(), side);
         link.delete();
      }

      //TX_TODO Reorder Code
      //      for (Entry<IRelationType, RelationSide> entry : typesToUpdate.entrySet()) {
      //         IRelationType type = entry.getKey();
      //         RelationSide side = entry.getValue();
      //
      //         List<ArtifactReadable> readables = getRelatedArtifacts(hasRelations, type, side);
      //         updateOrderListOnDelete(hasRelations, type, side, readables);
      //      }
   }

   private RelationSide getSide(Relation link, HasRelations hasRelations) {
      return link.getLinkerOnSide(RelationSide.SIDE_A).getLocalId() == hasRelations.getLocalId() ? RelationSide.SIDE_A : RelationSide.SIDE_B;
   }
}
