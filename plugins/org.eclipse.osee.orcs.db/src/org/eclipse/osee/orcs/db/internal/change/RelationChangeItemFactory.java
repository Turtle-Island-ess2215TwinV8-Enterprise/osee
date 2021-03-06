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
package org.eclipse.osee.orcs.db.internal.change;

import java.util.HashMap;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.core.model.change.RelationChangeItem;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.orcs.db.internal.change.ChangeItemLoader.ChangeItemFactory;

public final class RelationChangeItemFactory implements ChangeItemFactory {
   private static final String SELECT_RELATIONS_BY_GAMMAS =
      "select a_art_id, b_art_id, rel_link_id, rel_link_type_id, rationale, txj.gamma_id from osee_relation_link id, osee_join_transaction txj where id.gamma_id = txj.gamma_id and txj.query_id = ?";

   private final HashMap<Long, ModificationType> changeByGammaId;

   public RelationChangeItemFactory(HashMap<Long, ModificationType> changeByGammaId) {
      super();
      this.changeByGammaId = changeByGammaId;
   }

   @Override
   public String getLoadByGammaQuery() {
      return SELECT_RELATIONS_BY_GAMMAS;
   }

   @Override
   public ChangeItem createItem(IOseeStatement chStmt) throws OseeCoreException {
      int relLinkId = chStmt.getInt("rel_link_id");
      int relTypeId = chStmt.getInt("rel_link_type_id");

      long gammaId = chStmt.getLong("gamma_id");
      ModificationType modType = changeByGammaId.get(gammaId);

      int aArtId = chStmt.getInt("a_art_id");
      int bArtId = chStmt.getInt("b_art_id");
      String rationale = chStmt.getString("rationale");

      return new RelationChangeItem(relLinkId, relTypeId, gammaId, modType, aArtId, bArtId, rationale);
   }

   @Override
   public String getItemIdColumnName() {
      return "rel_link_id";
   }

   @Override
   public String getItemTableName() {
      return "osee_relation_link";
   }

   @Override
   public String getItemValueColumnName() {
      return "rationale";
   }
}