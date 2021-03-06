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
package org.eclipse.osee.framework.skynet.core.artifact;

import static org.eclipse.osee.framework.core.enums.LoadLevel.ATTRIBUTE;
import static org.eclipse.osee.framework.core.enums.LoadLevel.SHALLOW;
import java.util.Collection;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeSql;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeManager;

/**
 * @author Ryan Schmitt
 */
class RelationLoader {

   public static void loadRelationData(int joinQueryId, Collection<Artifact> artifacts, boolean historical, LoadLevel loadLevel) throws OseeCoreException {
      if (loadLevel == SHALLOW || loadLevel == ATTRIBUTE) {
         return;
      }

      if (historical) {
         return; // TODO: someday we might have a use for historical relations, but not now
      }

      IOseeStatement chStmt = ConnectionHandler.getStatement();
      try {
         String sqlQuery = ClientSessionManager.getSql(OseeSql.LOAD_RELATIONS);
         chStmt.runPreparedQuery(artifacts.size() * 8, sqlQuery, joinQueryId);
         while (chStmt.next()) {
            int relationId = chStmt.getInt("rel_link_id");
            int aArtifactId = chStmt.getInt("a_art_id");
            int bArtifactId = chStmt.getInt("b_art_id");
            Branch branch = BranchManager.getBranch(chStmt.getInt("branch_id"));
            RelationType relationType = RelationTypeManager.getType(chStmt.getInt("rel_link_type_id"));

            int gammaId = chStmt.getInt("gamma_id");
            String rationale = chStmt.getString("rationale");
            ModificationType modificationType = ModificationType.getMod(chStmt.getInt("mod_type"));

            RelationManager.getOrCreate(aArtifactId, bArtifactId, branch, relationType, relationId, gammaId, rationale,
               modificationType);
         }
      } finally {
         chStmt.close();
      }
      for (Artifact artifact : artifacts) {
         artifact.setLinksLoaded(true);
      }
   }
}
