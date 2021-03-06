/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.accessor;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.IdJoinQuery;
import org.eclipse.osee.framework.database.core.JoinUtility;
import org.eclipse.osee.framework.database.core.OseeConnection;

/**
 * @author Ryan D. Brooks
 */
public class UpdatePreviousTxCurrent {
   private static final String UPDATE_TXS_NOT_CURRENT =
      "update osee_txs SET tx_current = " + TxChange.NOT_CURRENT.getValue() + " where branch_id = ? AND gamma_id = ? and transaction_id = ?";
   private static final String SELECT_TXS_AND_GAMMAS =
      "SELECT txs.transaction_id, txs.gamma_id FROM osee_join_id idj, %s item, osee_txs txs WHERE idj.query_id = ? and idj.id = item.%s AND item.gamma_id = txs.gamma_id AND txs.branch_id = ? AND txs.tx_current <> ?";

   private final IOseeDatabaseService dbService;
   private final Branch branch;
   private final OseeConnection connection;
   private IdJoinQuery artifactJoin;
   private IdJoinQuery attributeJoin;
   private IdJoinQuery relationJoin;

   public UpdatePreviousTxCurrent(IOseeDatabaseService dbService, Branch branch, OseeConnection connection) {
      this.dbService = dbService;
      this.branch = branch;
      this.connection = connection;
   }

   public void addAttribute(int attributeId) {
      if (attributeJoin == null) {
         attributeJoin = JoinUtility.createIdJoinQuery(dbService);
      }
      attributeJoin.add(attributeId);
   }

   public void addArtifact(int artifactId) {
      if (artifactJoin == null) {
         artifactJoin = JoinUtility.createIdJoinQuery(dbService);
      }
      artifactJoin.add(artifactId);
   }

   public void addRelation(int relationId) {
      if (relationJoin == null) {
         relationJoin = JoinUtility.createIdJoinQuery(dbService);
      }
      relationJoin.add(relationId);
   }

   public void updateTxNotCurrents() throws OseeCoreException {
      updateTxNotCurrents("osee_artifact", "art_id", artifactJoin);
      updateTxNotCurrents("osee_attribute", "attr_id", attributeJoin);
      updateTxNotCurrents("osee_relation_link", "rel_link_id", relationJoin);
   }

   private void updateTxNotCurrents(String tableName, String columnName, IdJoinQuery idJoin) throws OseeCoreException {
      if (idJoin != null) {
         idJoin.store(connection);
         updateNoLongerCurrentGammas(tableName, columnName, idJoin.getQueryId());
         idJoin.delete(connection);
      }
   }

   private void updateNoLongerCurrentGammas(String tableName, String columnName, int queryId) throws OseeCoreException {
      String query = String.format(SELECT_TXS_AND_GAMMAS, tableName, columnName);

      List<Object[]> updateData = new ArrayList<Object[]>();
      IOseeStatement chStmt = dbService.getStatement(connection);
      try {
         chStmt.runPreparedQuery(10000, query, queryId, branch.getId(), TxChange.NOT_CURRENT.getValue());
         while (chStmt.next()) {
            updateData.add(new Object[] {branch.getId(), chStmt.getLong("gamma_id"), chStmt.getInt("transaction_id")});
         }
      } finally {
         chStmt.close();
      }

      dbService.runBatchUpdate(connection, UPDATE_TXS_NOT_CURRENT, updateData);
   }
}
