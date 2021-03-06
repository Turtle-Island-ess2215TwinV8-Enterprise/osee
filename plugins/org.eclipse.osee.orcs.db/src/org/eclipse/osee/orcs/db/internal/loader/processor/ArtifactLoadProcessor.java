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
package org.eclipse.osee.orcs.db.internal.loader.processor;

import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.ArtifactDataHandler;
import org.eclipse.osee.orcs.core.ds.LoadOptions;
import org.eclipse.osee.orcs.core.ds.VersionData;
import org.eclipse.osee.orcs.db.internal.loader.data.ArtifactObjectFactory;

/**
 * @author Ryan D. Brooks
 */
public class ArtifactLoadProcessor extends LoadProcessor<ArtifactData, ArtifactObjectFactory, ArtifactDataHandler> {

   public ArtifactLoadProcessor(ArtifactObjectFactory factory) {
      super(factory);
   }

   @Override
   protected ArtifactData createData(Object conditions, ArtifactObjectFactory factory, IOseeStatement chStmt, LoadOptions options) throws OseeCoreException {
      ArtifactData toReturn = null;

      int artifactId = chStmt.getInt("art_id");
      int branchId = chStmt.getInt("branch_id");

      CreateConditions onCreate = asConditions(conditions);
      if (!onCreate.isSame(branchId, artifactId)) {

         ModificationType modType = ModificationType.getMod(chStmt.getInt("mod_type"));
         // assumption: SQL is returning unwanted deleted artifacts only in the historical case
         if (!options.isHistorical() || options.areDeletedIncluded() || modType != ModificationType.DELETED) {
            long gamma = chStmt.getInt("gamma_id");
            int txId = chStmt.getInt("transaction_id");

            VersionData version = factory.createVersion(branchId, txId, gamma, options.isHistorical());

            if (options.isHistorical()) {
               version.setStripeId(chStmt.getInt("stripe_transaction_id"));
            }

            int typeId = chStmt.getInt("art_type_id");
            String guid = chStmt.getString("guid");
            String hrid = chStmt.getString("human_readable_id");
            toReturn = factory.createArtifactData(version, artifactId, typeId, modType, guid, hrid);
         }
         onCreate.saveConditions(branchId, artifactId);
      }
      return toReturn;
   }

   @Override
   protected Object createPreConditions() {
      return new CreateConditions();
   }

   private CreateConditions asConditions(Object conditions) {
      return (CreateConditions) conditions;
   }

   private static final class CreateConditions {
      int previousArtId = -1;
      int previousBranchId = -1;

      boolean isSame(int branchId, int artifactId) {
         return previousBranchId == branchId && previousArtId == artifactId;
      }

      void saveConditions(int branchId, int artifactId) {
         previousBranchId = branchId;
         previousArtId = artifactId;
      }
   }
}