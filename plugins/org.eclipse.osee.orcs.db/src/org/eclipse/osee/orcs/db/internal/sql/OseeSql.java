/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.sql;

import org.eclipse.osee.framework.core.enums.ConflictStatus;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.TxChange;

/**
 * @author Ryan D. Brooks
 */
public enum OseeSql {

   TX_GET_ALL_TRANSACTIONS("SELECT * FROM osee_tx_details WHERE transaction_id = ?"),
   TX_GET_MAX_AS_LARGEST_TX("SELECT max(transaction_id) as largest_transaction_id FROM osee_tx_details WHERE branch_id = ?"),

   MERGE_GET_ARTIFACTS_FOR_BRANCH("SELECT art.art_id FROM osee_txs txs, osee_artifact art WHERE txs.branch_id = ? and txs.gamma_id = art.gamma_id"),
   MERGE_GET_ATTRIBUTES_FOR_BRANCH("SELECT atr.art_id, atr.attr_id FROM osee_txs txs, osee_attribute atr WHERE txs.branch_id = ? and txs.gamma_id = atr.gamma_id"),
   MERGE_GET_RELATIONS_FOR_BRANCH("SELECT rel.a_art_id, rel.b_art_id FROM osee_txs txs, osee_relation_link rel WHERE txs.branch_id = ? and txs.gamma_id = rel.gamma_id"),

   CONFLICT_GET_ARTIFACTS_DEST("SELECT%s art2.art_type_id, art1.art_id, txs1.mod_type AS source_mod_type, txs1.gamma_id AS source_gamma, txs2.mod_type AS dest_mod_type, txs2.gamma_id AS dest_gamma FROM osee_txs txs1, osee_attribute art1, osee_artifact art2, osee_txs txs2 WHERE txs1.branch_id = ? AND txs1.transaction_id <> ? AND txs1.tx_current in (1,2) AND txs1.gamma_id = art1.gamma_id AND art1.art_id = art2.art_id AND art2.gamma_id = txs2.gamma_id AND txs2.branch_id = ? AND ((txs2.tx_current = 1 AND txs2.gamma_id not in (SELECT txs.gamma_id FROM osee_txs txs WHERE txs.transaction_id = ?)) OR txs2.tx_current = 2)", Strings.HINTS__ORDERED__INDEX__ARTIFACT_CONFLICT),
   CONFLICT_GET_ARTIFACTS_SRC("SELECT%s art1.art_type_id, art1.art_id, txs1.mod_type AS source_mod_type, txs1.gamma_id AS source_gamma, txs2.mod_type AS dest_mod_type, txs2.gamma_id AS dest_gamma FROM osee_txs txs1, osee_artifact art1, osee_attribute art2, osee_txs txs2 WHERE txs1.branch_id = ? AND txs1.transaction_id <> ? AND txs1.tx_current in (1,2) AND txs1.gamma_id = art1.gamma_id AND art1.art_id = art2.art_id AND art2.gamma_id = txs2.gamma_id AND txs2.branch_id = ? AND ((txs2.tx_current = 1 AND txs2.gamma_id not in (SELECT txs.gamma_id FROM osee_txs txs WHERE txs.transaction_id = ?)) OR txs2.tx_current = 2)", Strings.HINTS__ORDERED__INDEX__ARTIFACT_CONFLICT),
   CONFLICT_GET_ATTRIBUTES("SELECT%s atr1.art_id, txs1.mod_type, atr1.attr_type_id, atr1.attr_id, atr1.gamma_id AS source_gamma, atr1.value AS source_value, atr2.gamma_id AS dest_gamma, atr2.value as dest_value, txs2.mod_type AS dest_mod_type FROM osee_txs txs1, osee_attribute atr1, osee_attribute atr2, osee_txs txs2 WHERE txs1.branch_id = ? AND txs1.transaction_id <> ? AND txs1.tx_current in (1,2) AND txs1.gamma_id = atr1.gamma_id AND atr1.attr_id = atr2.attr_id AND atr2.gamma_id = txs2.gamma_id AND txs2.branch_id = ? AND ((txs2.tx_current = 1 AND txs2.gamma_id not in (SELECT txs.gamma_id FROM osee_txs txs WHERE txs.transaction_id = ? )) OR txs2.tx_current = 2) ORDER BY attr_id", Strings.HINTS__ORDERED__INDEX__ATTRIBUTE_CONFLICT),
   CONFLICT_GET_HISTORICAL_ATTRIBUTES("SELECT%s atr.attr_id, atr.art_id, source_gamma_id, dest_gamma_id, attr_type_id, mer.merge_branch_id, mer.dest_branch_id, value as source_value, status FROM osee_merge mer, osee_conflict con, osee_attribute atr Where mer.commit_transaction_id = ? AND mer.merge_branch_id = con.merge_branch_id And con.source_gamma_id = atr.gamma_id AND con.status in (" + ConflictStatus.COMMITTED.getValue() + ", " + ConflictStatus.INFORMATIONAL.getValue() + " ) order by attr_id", true),

   LOAD_REVISION_HISTORY_TRANSACTION_ATTR("SELECT %s txs.transaction_id from osee_attribute arv, osee_txs txs where arv.art_id = ? and arv.gamma_id = txs.gamma_id and txs.branch_id = ? and txs.transaction_id <=?", true),
   LOAD_REVISION_HISTORY_TRANSACTION_REL("SELECT %s txs.transaction_id from osee_relation_link rel, osee_txs txs where (rel.a_art_id = ? or rel.b_art_id = ?) and rel.gamma_id = txs.gamma_id and txs.branch_id = ? and txs.transaction_id <=?", true),

   CHANGE_BRANCH_ATTRIBUTE_WAS("SELECT%s attxs1.attr_id, attxs1.value as was_value, txs1.mod_type FROM osee_join_artifact ja1, osee_attribute attxs1, osee_txs txs1, WHERE txs1.branch_id = ? AND txs1.tx_type = 1 AND attxs1.gamma_id = txs1.gamma_id AND attxs1.art_id = ja1.art_id AND txs1.branch_id = ja1.branch_id AND ja1.query_id = ?", true),
   CHANGE_TX_ATTRIBUTE_WAS("SELECT%s att1.attr_id, att1.value as was_value, txs1.mod_type FROM osee_join_artifact al1, osee_attribute att1, osee_txs txs1 WHERE  al1.art_id = att1.art_id AND att1.gamma_id = txs1.gamma_id AND txs1.transaction_id < ? AND al1.query_id = ? AND txs1.branch_id = al1.branch_id order by txs1.branch_id, att1.art_id, att1.attr_id, txs1.transaction_id desc", true),
   CHANGE_BRANCH_ATTRIBUTE_IS("SELECT%s art1.art_type_id, attr1.art_id, attr1.attr_id, attr1.gamma_id, attr1.attr_type_id, attr1.value as is_value, txs1.mod_type FROM osee_txs txs1, osee_attribute attr1, osee_artifact art1 WHERE txs1.branch_id = ? AND txs1.transaction_id <> = ? AND txs1.tx_current in (" + TxChange.DELETED.getValue() + ", " + TxChange.CURRENT.getValue() + ", " + TxChange.ARTIFACT_DELETED.getValue() + ") AND art1.art_id = attr1.art_id AND attr1.gamma_id = txs1.gamma_id", true),
   CHANGE_TX_ATTRIBUTE_IS("SELECT art.art_type_id, att.art_id, att.attr_id, att.gamma_id, att.attr_type_id, att.value as is_value, txs.mod_type FROM osee_txs txs, osee_attribute att, osee_artifact art WHERE txs.branch_id = ? and txs.transaction_id = ? AND txs.gamma_id = att.gamma_id AND att.art_id = art.art_id"),
   CHANGE_TX_ATTRIBUTE_IS_FOR_SPECIFIC_ARTIFACT(CHANGE_TX_ATTRIBUTE_IS.sql + " and att.art_id =?"),
   CHANGE_BRANCH_RELATION("SELECT%s txs1.mod_type, rel1.gamma_id, rel1.b_art_id, rel1.a_art_id, rel1.rationale, rel1.rel_link_id, rel1.rel_link_type_id, art.art_type_id from osee_txs txs1, osee_relation_link rel1, osee_artifact art where txs1.branch_id = ? AND txs1.transaction_id <> ? AND txs1.tx_current in (" + TxChange.DELETED.getValue() + ", " + TxChange.CURRENT.getValue() + ", " + TxChange.ARTIFACT_DELETED.getValue() + ") AND txs1.gamma_id = rel1.gamma_id AND rel1.a_art_id = art.art_id", true),
   CHANGE_TX_RELATION("SELECT txs.mod_type, rel.gamma_id, rel.b_art_id, rel.a_art_id, rel.rationale, rel.rel_link_id, rel.rel_link_type_id, art.art_type_id from osee_txs txs, osee_relation_link rel, osee_artifact art where txs.branch_id = ? AND txs.transaction_id = ? AND txs.gamma_id = rel.gamma_id AND rel.a_art_id = art.art_id"),
   CHANGE_TX_RELATION_FOR_SPECIFIC_ARTIFACT(CHANGE_TX_RELATION.sql + " and (rel.a_art_id = ? or rel.b_art_id = ?)"),
   CHANGE_BRANCH_ARTIFACT("select%s art1.art_id, art1.art_type_id, art1.gamma_id, txs1.mod_type FROM osee_txs txs1, osee_artifact art1 WHERE txs1.branch_id = ? AND txs1.transaction_id <> ? AND txs1.gamma_id = art1.gamma_id AND txs1.mod_type in (" + ModificationType.DELETED.getValue() + ", " + ModificationType.NEW.getValue() + ", " + ModificationType.INTRODUCED.getValue() + ") ", true),
   CHANGE_TX_ARTIFACT("select art.art_id, art.art_type_id, art.gamma_id, txs.mod_type FROM osee_txs txs, osee_artifact art WHERE txs.branch_id = ? and txs.transaction_id = ? AND txs.gamma_id = art.gamma_id AND txs.mod_type in (" + ModificationType.DELETED.getValue() + ", " + ModificationType.NEW.getValue() + ", " + ModificationType.INTRODUCED.getValue() + ") "),
   CHANGE_TX_ARTIFACT_FOR_SPECIFIC_ARTIFACT(CHANGE_TX_ARTIFACT.sql + " and art.art_id =?"),
   CHANGE_TX_MODIFYING("SELECT arj.art_id, arj.branch_id, txs.transaction_id from osee_join_artifact arj, osee_artifact art, osee_txs txs, osee_branch br where arj.query_id = ? AND arj.art_id = art.art_id AND art.gamma_id = txs.gamma_id AND txs.branch_id = arj.branch_id AND txs.transaction_id <= arj.transaction_id AND txs.branch_id = br.branch_id AND txs.transaction_id <> br.baseline_transaction_id", true),
   CHANGE_BRANCH_MODIFYING("SELECT count(txs.transaction_id) as tx_count, arj.branch_id, arj.art_id FROM osee_join_artifact arj, osee_artifact art, osee_txs txs, osee_branch br where arj.query_id = ? AND arj.art_id = art.art_id AND art.gamma_id = txs.gamma_id AND txs.branch_id = arj.branch_id and txs.branch_id = br.branch_id AND txs.transaction_id <> br.baseline_transaction_id group by arj.art_id, arj.branch_id", true),

   IS_ARTIFACT_ON_BRANCH("SELECT%s count(1) from osee_artifact av1, osee_txs txs1 where av1.art_id = ? and av1.gamma_id = txs1.gamma_id and txs1.branch_id = ?", true),
   QUERY_BUILDER("%s", true);

   private final String sql;
   private final String hints;
   private final boolean isDynamicHint;

   private OseeSql(String sql, String hints) {
      this.sql = sql;
      this.hints = hints;
      this.isDynamicHint = false;
   }

   private OseeSql(String sql) {
      this(sql, null);
   }

   private OseeSql(String sql, boolean isDynamicHint) {
      this.sql = sql;
      this.hints = null;
      this.isDynamicHint = isDynamicHint;
   }

   public String getSql() {
      return sql;
   }

   protected String getHints() {
      return hints;
   }

   protected boolean getIsDynamicHint() {
      return isDynamicHint;
   }

   private static class Strings {
      private static final String HINTS__ORDERED__INDEX__ARTIFACT_CONFLICT =
         " /*+ ordered index(atr1) index(atr2) index(txs2) */";
      private static final String HINTS__ORDERED__INDEX__ATTRIBUTE_CONFLICT =
         " /*+ ordered index(atr1) index(atr2) index(txs2) */";
   }

}