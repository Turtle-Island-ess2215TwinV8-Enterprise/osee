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
package org.eclipse.osee.framework.core.data;

import java.util.Properties;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;

/**
 * @author Roberto E. Escobar
 */
public class SqlKey {
   private static Boolean areHintsAllowed = null;

   public static final String SELECT_HISTORICAL_ARTIFACTS = "SELECT_HISTORICAL_ARTIFACTS";
   public static final String SELECT_HISTORICAL_ATTRIBUTES = "SELECT_HISTORICAL_ATTRIBUTES";
   public static final String SELECT_CURRENT_ATTRIBUTES = "SELECT_CURRENT_ATTRIBUTES";
   public static final String SELECT_CURRENT_ATTRIBUTES_WITH_DELETED = "SELECT_CURRENT_ATTRIBUTES_WITH_DELETED";
   public static final String SELECT_RELATIONS = "SELECT_RELATIONS";   
   public static final String SELECT_CURRENT_ARTIFACTS = "SELECT_CURRENT_ARTIFACTS";
   public static final String SELECT_CURRENT_ARTIFACTS_WITH_DELETED = "SELECT_CURRENT_ARTIFACTS_WITH_DELETED";
   
   public static final String SELECT_HISTORICAL_ARTIFACTS_DEFINITION =
         "SELECT%s al1.art_id, txs1.gamma_id, mod_type, txd1.*, art_type_id, guid, human_readable_id, al1.transaction_id as stripe_transaction_id FROM osee_join_artifact al1, osee_artifact art1, osee_artifact_version arv1, osee_txs txs1, osee_tx_details txd1 WHERE al1.query_id = ? AND al1.art_id = art1.art_id AND art1.art_id = arv1.art_id AND arv1.gamma_id = txs1.gamma_id AND txs1.transaction_id <= al1.transaction_id AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = al1.branch_id order by al1.branch_id, art1.art_id, txs1.transaction_id desc";

   public static final String SELECT_HISTORICAL_ATTRIBUTES_DEFINITION =
         "SELECT%s att1.art_id, att1.attr_id, att1.value, att1.gamma_id, att1.attr_type_id, att1.uri, al1.branch_id, txs1.mod_type, txd1.transaction_id, al1.transaction_id as stripe_transaction_id FROM osee_join_artifact al1, osee_attribute att1, osee_txs txs1, osee_tx_details txd1 WHERE al1.query_id = ? AND al1.art_id = att1.art_id AND att1.gamma_id = txs1.gamma_id AND txs1.transaction_id <= al1.transaction_id AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = al1.branch_id order by txd1.branch_id, att1.art_id, att1.attr_id, txd1.transaction_id desc";

   private static final String SELECT_CURRENT_ATTRIBUTES_PREFIX =
         "SELECT%s att1.art_id, att1.attr_id, att1.value, att1.gamma_id, att1.attr_type_id, att1.uri, al1.branch_id, txs1.mod_type FROM osee_join_artifact al1, osee_attribute att1, osee_txs txs1, osee_tx_details txd1 WHERE al1.query_id = ? AND al1.art_id = att1.art_id AND att1.gamma_id = txs1.gamma_id AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = al1.branch_id AND txs1.tx_current ";
   
   private static final String SELECT_CURRENT_ATTRIBUTES_DEFINITION =
         SELECT_CURRENT_ATTRIBUTES_PREFIX + "= 1 order by al1.branch_id, al1.art_id";

   private static final String SELECT_CURRENT_ATTRIBUTES_WITH_DELETED_DEFINITION =
         SELECT_CURRENT_ATTRIBUTES_PREFIX + "IN (1, 3) order by al1.branch_id, al1.art_id";

   private static final String SELECT_RELATIONS_DEFINITION =
      "SELECT%s rel_link_id, a_art_id, b_art_id, rel_link_type_id, a_order, b_order, rel1.gamma_id, rationale, al1.branch_id FROM osee_join_artifact al1, osee_relation_link rel1, osee_txs txs1, osee_tx_details txd1 WHERE al1.query_id = ? AND (al1.art_id = rel1.a_art_id OR al1.art_id = rel1.b_art_id) AND rel1.gamma_id = txs1.gamma_id AND txs1.tx_current=1 AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = al1.branch_id";

   private static final String SELECT_CURRENT_ARTIFACTS_PREFIX =
      "SELECT%s al1.art_id, txs1.gamma_id, mod_type, txd1.*, art_type_id, guid, human_readable_id FROM osee_join_artifact al1, osee_artifact art1, osee_artifact_version arv1, osee_txs txs1, osee_tx_details txd1 WHERE al1.query_id = ? AND al1.art_id = art1.art_id AND art1.art_id = arv1.art_id AND arv1.gamma_id = txs1.gamma_id AND txd1.branch_id = al1.branch_id AND txd1.transaction_id = txs1.transaction_id AND txs1.tx_current ";

   private static final String SELECT_CURRENT_ARTIFACTS_DEFINITION =
      SELECT_CURRENT_ARTIFACTS_PREFIX + "= 1";

   private static final String SELECT_CURRENT_ARTIFACTS_WITH_DELETED_DEFINITION =
      SELECT_CURRENT_ARTIFACTS_PREFIX + "in (1, 2)";

   
   public static final String ORDERED_HINT = " /*+ ordered */";
   public static final String ORDERED_HINT_AND_TXS_INDEX = " /*+ ordered INDEX(txs1) */";
   public static final String HINTS__ORDERED__TXS_IDX__REL_IDX = " /*+ ordered INDEX(txs1) INDEX(rel1) */";
   public static final String HINTS__ORDERED__TXS_IDX__ART_IDX__ARTV_IDX__TXD_IDX = " /*+ ordered INDEX(txs1) INDEX(art1) INDEX(arv1) INDEX(txd1) */";
   public static final String HINTS__ORDERED__FIRST_ROWS = " /*+ ordered FIRST_ROWS */";

   private SqlKey() {
   }

   public static Properties getSqlProperties() throws OseeDataStoreException {
      Properties sqlProperties = new Properties();
      sqlProperties.put(SqlKey.SELECT_HISTORICAL_ARTIFACTS,
            getFormattedSql(SqlKey.SELECT_HISTORICAL_ARTIFACTS_DEFINITION, HINTS__ORDERED__FIRST_ROWS));
      
      sqlProperties.put(SqlKey.SELECT_HISTORICAL_ATTRIBUTES,
            getFormattedSql(SqlKey.SELECT_HISTORICAL_ATTRIBUTES_DEFINITION, HINTS__ORDERED__FIRST_ROWS));
      
      sqlProperties.put(SqlKey.SELECT_CURRENT_ATTRIBUTES,
            getFormattedSql(SqlKey.SELECT_CURRENT_ATTRIBUTES_DEFINITION, HINTS__ORDERED__FIRST_ROWS));
      
      sqlProperties.put(SqlKey.SELECT_CURRENT_ATTRIBUTES_WITH_DELETED,
            getFormattedSql(SqlKey.SELECT_CURRENT_ATTRIBUTES_WITH_DELETED_DEFINITION, HINTS__ORDERED__FIRST_ROWS));
      
      sqlProperties.put(SqlKey.SELECT_RELATIONS,
            getFormattedSql(SqlKey.SELECT_RELATIONS_DEFINITION, HINTS__ORDERED__FIRST_ROWS));
      
      sqlProperties.put(SqlKey.SELECT_CURRENT_ARTIFACTS,
            getFormattedSql(SqlKey.SELECT_CURRENT_ARTIFACTS_DEFINITION, HINTS__ORDERED__FIRST_ROWS));
      
      sqlProperties.put(SqlKey.SELECT_CURRENT_ARTIFACTS_WITH_DELETED,
            getFormattedSql(SqlKey.SELECT_CURRENT_ARTIFACTS_WITH_DELETED_DEFINITION, HINTS__ORDERED__FIRST_ROWS));
      
      return sqlProperties;
   }

   public static String getFormattedSql(String sql, String sqlHints) throws OseeDataStoreException {
      if (areHintsAllowed == null) {
         areHintsAllowed = ConnectionHandler.areHintsSupported();
      }
      return String.format(sql, areHintsAllowed ? sqlHints : "");
   }
}
