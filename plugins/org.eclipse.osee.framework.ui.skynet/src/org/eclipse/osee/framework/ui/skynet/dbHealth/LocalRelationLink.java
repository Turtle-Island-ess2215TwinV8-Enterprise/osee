/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.dbHealth;

public class LocalRelationLink {
   public int relLinkId, gammaId, relTransId, branchId, aArtId, bArtId, transIdForArtifactDeletion, commitTrans,
      modType;

   public LocalRelationLink(int relLinkId, int gammaId, int transactionId, int branchId, int aArtId, int bArtId, int transIdForArtifactDeletion, int commitTrans, int modType) {
      this.aArtId = aArtId;
      this.bArtId = bArtId;
      this.branchId = branchId;
      this.gammaId = gammaId;
      this.relLinkId = relLinkId;
      this.relTransId = transactionId;
      this.transIdForArtifactDeletion = transIdForArtifactDeletion;
      this.commitTrans = commitTrans;
      this.modType = modType;
   }

   @Override
   public String toString() {
      return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", gammaId, relTransId, relLinkId, branchId, aArtId,
         bArtId, transIdForArtifactDeletion, commitTrans, modType);
   }
}