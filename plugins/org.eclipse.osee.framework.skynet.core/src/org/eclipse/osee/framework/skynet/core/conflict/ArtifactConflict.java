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

package org.eclipse.osee.framework.skynet.core.conflict;

import org.eclipse.osee.framework.core.enums.ConflictStatus;
import org.eclipse.osee.framework.core.enums.ConflictType;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.BranchMergeException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;

/**
 * @author Theron Virgin
 */
public class ArtifactConflict extends Conflict {
   private static final String CHANGE_ITEM = "Artifact State";
   private static final String ARTIFACT_DELETED = "DELETED";
   private static final String ARTIFACT_MODIFIED = "MODIFIED";
   private final ModificationType sourceModType;
   private final ModificationType destModType;

   public ArtifactConflict(int sourceGamma, int destGamma, int artId, TransactionRecord toTransactionId, Branch mergeBranch, Branch sourceBranch, Branch destBranch, ModificationType sourceModType, ModificationType destModType, int artTypeId) {
      super(sourceGamma, destGamma, artId, toTransactionId, null, mergeBranch, sourceBranch, destBranch);
      this.sourceModType = sourceModType;
      this.destModType = destModType;
   }

   @Override
   public String getArtifactName() throws OseeCoreException {
      if (sourceModType.isDeleted()) {
         return getDestArtifact().getName();
      } else {
         return getSourceArtifact().getName();
      }
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Object getAdapter(Class adapter) {
      if (adapter == null) {
         throw new IllegalArgumentException("adapter can not be null");
      }

      if (adapter.isInstance(this)) {
         return this;
      }

      return null;
   }

   @Override
   public boolean clearValue() {
      return false;
   }

   @Override
   public ConflictStatus computeStatus() throws OseeCoreException {
      return super.computeStatus(getObjectId(), ConflictStatus.INFORMATIONAL);
   }

   @Override
   public int getObjectId() {
      return getArtId();
   }

   @Override
   public String getChangeItem() {
      return CHANGE_ITEM;
   }

   @Override
   public ConflictType getConflictType() {
      return ConflictType.ARTIFACT;
   }

   @Override
   public String getDestDisplayData() {
      return destModType.isDeleted() ? ARTIFACT_DELETED : ARTIFACT_MODIFIED;
   }

   @Override
   public String getMergeDisplayData() {
      return "";
   }

   protected Object getMergeValue() throws OseeCoreException {
      return getArtifact();
   }

   @Override
   public String getSourceDisplayData() {
      return sourceModType.isDeleted() ? ARTIFACT_DELETED : ARTIFACT_MODIFIED;
   }

   @Override
   public boolean mergeEqualsDestination() throws OseeCoreException {
      return getDestArtifact().equals(getMergeValue());
   }

   @Override
   public boolean mergeEqualsSource() throws OseeCoreException {
      return getSourceArtifact().equals(getMergeValue());
   }

   @Override
   public boolean setToDest() {
      return false;
   }

   @Override
   public boolean setToSource() {
      return false;
   }

   @Override
   public boolean sourceEqualsDestination() {
      return false;
   }

   @Override
   public int getMergeGammaId() throws BranchMergeException {
      throw new BranchMergeException("Artifact Conflicts can not be handled they must be reverted on the Source Branch");
   }

   @Override
   public boolean applyPreviousMerge(int mergeBranchId, int destBranchId) {
      return false;
   }

}
