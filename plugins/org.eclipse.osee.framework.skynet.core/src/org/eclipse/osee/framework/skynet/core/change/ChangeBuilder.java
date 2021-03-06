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
package org.eclipse.osee.framework.skynet.core.change;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionDelta;

/**
 * @author Jeff C. Phillips
 */
public abstract class ChangeBuilder {
   private final int sourceGamma;
   private final int artId;
   private final TransactionDelta txDelta;
   private ModificationType modType;
   private final Branch branch;
   private final IArtifactType artifactType;
   private final boolean isHistorical;

   public ChangeBuilder(Branch branch, IArtifactType artifactType, int sourceGamma, int artId, TransactionDelta txDelta, ModificationType modType, boolean isHistorical) {
      super();
      this.sourceGamma = sourceGamma;
      this.artId = artId;
      this.txDelta = txDelta;
      this.modType = modType;
      this.branch = branch;
      this.artifactType = artifactType;
      this.isHistorical = isHistorical;
   }

   public int getSourceGamma() {
      return sourceGamma;
   }

   public int getArtId() {
      return artId;
   }

   public TransactionDelta getTxDelta() {
      return txDelta;
   }

   public ModificationType getModType() {
      return modType;
   }

   public void setModType(ModificationType modType) {
      this.modType = modType;
   }

   public Branch getBranch() {
      return branch;
   }

   public IArtifactType getArtifactType() {
      return artifactType;
   }

   public boolean isHistorical() {
      return isHistorical;
   }

}
