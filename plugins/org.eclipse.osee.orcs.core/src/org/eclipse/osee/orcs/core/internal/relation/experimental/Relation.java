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

import java.util.List;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.orcs.core.ds.HasOrcsData;
import org.eclipse.osee.orcs.core.ds.OrcsData;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.HasLinkers;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.Linker;
import org.eclipse.osee.orcs.core.internal.util.ValueProvider;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.HasDeleteState;
import org.eclipse.osee.orcs.data.Modifiable;

/**
 * @author Roberto E. Escobar
 */
public class Relation implements HasOrcsData<RelationData>, HasDeleteState, Modifiable, HasLinkers<ArtifactReadable> {

   private final ValueProvider<Branch, OrcsData> branchProvider;
   private final ValueProvider<RelationType, RelationData> relTypeProvider;
   private final List<ArtifactLazyObject> linkers;

   private RelationData relationData;
   private boolean isDirty;

   private static final boolean SET_DIRTY = true;

   //   private static final boolean SET_NOT_DIRTY = false;

   public Relation(RelationData relationData, ValueProvider<Branch, OrcsData> branchProvider, ValueProvider<RelationType, RelationData> relTypeProvider, List<ArtifactLazyObject> linkers) {
      super();
      this.relationData = relationData;
      this.branchProvider = branchProvider;
      this.relTypeProvider = relTypeProvider;
      this.linkers = linkers;
   }

   @Override
   public RelationData getOrcsData() {
      return relationData;
   }

   @Override
   public void setOrcsData(RelationData data) {
      this.relationData = data;
      branchProvider.setOrcsData(data);
      relTypeProvider.setOrcsData(data);
      for (HasOrcsData<RelationData> item : getLinkers()) {
         item.setOrcsData(data);
      }
   }

   public RelationType getRelationType() throws OseeCoreException {
      return relTypeProvider.get();
   }

   public ModificationType getModificationType() {
      return getOrcsData().getModType();
   }

   @Override
   public boolean isDeleted() {
      return getModificationType().isDeleted();
   }

   public void delete() {
      getOrcsData().setModType(ModificationType.DELETED);
   }

   public Branch getBranch() throws OseeCoreException {
      return branchProvider.get();
   }

   @Override
   public boolean isDirty() {
      return isDirty;
   }

   public void setNotDirty() {
      setDirtyFlag(false);
   }

   public void setDirty() {
      setDirtyFlag(true);
   }

   private void setDirtyFlag(boolean dirty) {
      this.isDirty = dirty;
   }

   public String getRationale() {
      return getOrcsData().getRationale();
   }

   public boolean isOfType(IRelationType oseeType) throws OseeCoreException {
      return getRelationType().equals(oseeType);
   }

   public void setRationale(String rationale) {
      String toSet = rationale;
      if (toSet == null) {
         toSet = "";
      }
      if (!toSet.equals(getOrcsData().getRationale())) {
         getOrcsData().setRationale(rationale);
         markedAsChanged(ModificationType.MODIFIED, SET_DIRTY);
      }
   }

   private void markedAsChanged(ModificationType modificationType, boolean setDirty) {
      //Because deletes can reorder links and we want the final modification type to be delete and not modify.
      if (modificationType != ModificationType.DELETED || modificationType != ModificationType.ARTIFACT_DELETED) {
         getOrcsData().setModType(modificationType);
      }
      if (setDirty) {
         setDirty();
      } else {
         setNotDirty();
      }
   }

   ////////////////////////////////////////////// ARTIFACT STUFF ///////////////
   @Override
   public Iterable<ArtifactLazyObject> getLinkers() {
      return linkers;
   }

   @Override
   public Linker<ArtifactReadable> getLinkerOnSide(RelationSide relationSide) {
      return linkers.get(relationSide.ordinal());
   }

   public ArtifactReadable getArtifactA() throws OseeCoreException {
      return getArtifact(RelationSide.SIDE_A);
   }

   public ArtifactReadable getArtifactB() throws OseeCoreException {
      return getArtifact(RelationSide.SIDE_B);
   }

   public ArtifactReadable getArtifact(RelationSide relationSide) throws OseeCoreException {
      return getLinkerOnSide(relationSide).get();
   }
}
