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

import org.eclipse.osee.framework.core.data.TransactionDelta;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.core.model.ArtifactType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.RelationType;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;

/**
 * @author Jeff C. Phillips
 */
public class RelationChangeBuilder extends ChangeBuilder {
   private final int bArtId;
   private final int relLinkId;
   private final String rationale;
   private final RelationType relationType;

   public RelationChangeBuilder(Branch branch, ArtifactType artifactType, int sourceGamma, int artId, TransactionDelta txDelta, ModificationType modType, int bArtId, int relLinkId, String rationale, RelationType relationType, boolean isHistorical) {
      super(branch, artifactType, sourceGamma, artId, txDelta, modType, isHistorical);
      this.bArtId = bArtId;
      this.relLinkId = relLinkId;
      this.rationale = rationale;
      this.relationType = relationType;
   }

   @Override
   public Change build(Branch branch) throws OseeDataStoreException, OseeTypeDoesNotExist, ArtifactDoesNotExist {
      Artifact bArtifact;

      if (isHistorical()) {
         bArtifact = ArtifactCache.getHistorical(bArtId, getTxDelta().getEndTx().getId());
      } else {
         bArtifact = ArtifactCache.getActive(bArtId, branch);
      }
      return new RelationChange(branch, getArtifactType(), getSourceGamma(), getArtId(), getTxDelta(), getModType(),
            bArtId, relLinkId, rationale, relationType, isHistorical(), new ArtifactDelta(loadArtifact(), null),
            bArtifact);
   }

}
