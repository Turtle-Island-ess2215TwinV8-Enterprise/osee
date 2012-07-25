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

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.OrcsData;
import org.eclipse.osee.orcs.core.internal.artifact.ArtifactImpl;
import org.eclipse.osee.orcs.core.internal.artifact.ArtifactVisitor;
import org.eclipse.osee.orcs.core.internal.attribute.AttributeFactory;
import org.eclipse.osee.orcs.core.internal.relation.RelationTypeValidity;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.Graph;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.HasRelations;
import org.eclipse.osee.orcs.core.internal.util.ValueProvider;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Roberto E. Escobar
 */
public class RelatedArtifactImpl extends ArtifactImpl implements HasRelations {

   private RelationTypeValidity validity;
   private RelationCollection collection;
   private Graph graph;

   public RelatedArtifactImpl(ArtifactData artifactData, AttributeFactory attributeFactory, ValueProvider<Branch, OrcsData> branchProvider, ValueProvider<ArtifactType, ArtifactData> artifactTypeProvider) {
      super(artifactData, attributeFactory, null, branchProvider, artifactTypeProvider);
   }

   public List<RelationType> getValidRelationTypes() throws OseeCoreException {
      return validity.getValidRelationTypes(getArtifactType());
   }

   public int getMaximumRelationTypeAllowed(IRelationTypeSide relationTypeSide) throws OseeCoreException {
      return validity.getMaximumRelationAllowed(getArtifactType(), relationTypeSide);
   }

   public Collection<? extends IRelationType> getExistingRelationTypes() throws OseeCoreException {
      return getRelations().getExistingTypes(DeletionFlag.EXCLUDE_DELETED);
   }

   public boolean hasDirtyRelations() {
      return getRelations().hasDirty();
   }

   public ArtifactReadable getParent() throws OseeCoreException {
      return graph.getParent(getBranch(), this);
   }

   public List<ArtifactReadable> getChildren() throws OseeCoreException {
      return graph.getChildren(getBranch(), this);
   }

   public List<ArtifactReadable> getRelated(IRelationTypeSide relationTypeSide) throws OseeCoreException {
      return graph.getRelated(getBranch(), this, relationTypeSide);
   }

   @Override
   public void accept(ArtifactVisitor visitor) throws OseeCoreException {
      super.accept(visitor);
      for (Relation link : getRelations().getAll()) {
         visitor.visit(link);
      }
   }

   @Override
   public boolean isDirty() {
      return hasDirtyRelations() || super.isDirty();
   }

   @Override
   public RelationCollection getRelations() {
      return collection;
   }

   @Override
   public void delete() throws OseeCoreException {
      //         if (!overrideDeleteCheck) {
      //             // Confirm artifacts are fit to delete
      //            for (IArtifactCheck check : ArtifactChecks.getArtifactChecks()) {
      //               IStatus result = check.isDeleteable(artifacts);
      //               if (!result.isOK()) {
      //                  throw new OseeStateException(result.getMessage());
      //               }
      //            }
      //         }
      super.delete();
      graph.unrelate(getBranch(), this);
   }
}
