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
package org.eclipse.osee.framework.branch.management.exchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.osee.framework.branch.management.ExportOptions;
import org.eclipse.osee.framework.branch.management.exchange.export.AbstractExportItem;
import org.eclipse.osee.framework.branch.management.exchange.export.ManifestExportItem;
import org.eclipse.osee.framework.branch.management.exchange.export.MetadataExportItem;
import org.eclipse.osee.framework.branch.management.exchange.export.RelationalExportItem;
import org.eclipse.osee.framework.branch.management.exchange.export.RelationalExportItemWithType;
import org.eclipse.osee.framework.db.connection.core.ConflictType;
import org.eclipse.osee.framework.db.connection.core.SequenceManager;
import org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase;
import org.eclipse.osee.framework.jdk.core.type.ObjectPair;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.resource.management.Options;

/**
 * @author Roberto E. Escobar
 */
public class ExchangeDb {

   private static final String ARTIFACT_TYPE_ID = "art_type_id";
   private static final String ATTRIBUTE_TYPE_ID = "attr_type_id";
   private static final String RELATION_TYPE_ID = "rel_link_type_id";
   private static final String GAMMA_ID = "gamma_id";
   private static final String TRANSACTION_ID = "transaction_id";
   private static final String ATTRIBUTE_ID = "attr_id";
   private static final String ARTIFACT_ID = "art_id";
   private static final String RELATION_ID = "rel_link_id";
   private static final String BRANCH_ID = "branch_id";
   private static final String CONFLICT_ID = "conflict_id";
   private static final String CONFLICT_TYPE = "conflict_type";

   private static final String[] BRANCH_ID_NEG_ONE_ALIASES = new String[] {"parent_branch_id"};

   private static final String[] BRANCH_ID_REG_ALIASES =
         new String[] {"mapped_branch_id", "source_branch_id", "merge_branch_id", "dest_branch_id"};

   private static final String[] ARTIFACT_ID_NEG_ONE_ALIASES =
         new String[] {"commit_art_id", "associated_art_id", "a_order", "b_order", "author"};

   private static final String[] ARTIFACT_ID_REG_ALIASES = new String[] {"a_art_id", "b_art_id"};

   private static final String[] GAMMA_ID_REG_ALIASES = new String[] {"source_gamma_id", "dest_gamma_id"};

   private static final String[] TRANSACTION_ID_NEG_ONE_ALIASES = new String[] {"commit_transaction_id"};

   private static final String[] ARTIFACT_ID_ALIASES;
   private static final String[] BRANCH_ID_ALIASES;
   private static final String[] GAMMA_ID_ALIASES;
   private static final String[] TRANSACTION_ID_ALIASES;
   static {
      Set<String> artIdAliases = new HashSet<String>();
      artIdAliases.add(ARTIFACT_ID);
      artIdAliases.addAll(Arrays.asList(ARTIFACT_ID_REG_ALIASES));
      artIdAliases.addAll(Arrays.asList(ARTIFACT_ID_NEG_ONE_ALIASES));
      ARTIFACT_ID_ALIASES = artIdAliases.toArray(new String[artIdAliases.size()]);

      Set<String> branchIdAliases = new HashSet<String>();
      branchIdAliases.add(BRANCH_ID);
      branchIdAliases.addAll(Arrays.asList(BRANCH_ID_REG_ALIASES));
      branchIdAliases.addAll(Arrays.asList(BRANCH_ID_NEG_ONE_ALIASES));
      BRANCH_ID_ALIASES = branchIdAliases.toArray(new String[branchIdAliases.size()]);

      Set<String> gammaIdAliases = new HashSet<String>();
      gammaIdAliases.add(GAMMA_ID);
      gammaIdAliases.addAll(Arrays.asList(GAMMA_ID_REG_ALIASES));
      GAMMA_ID_ALIASES = gammaIdAliases.toArray(new String[gammaIdAliases.size()]);

      Set<String> txIdAliases = new HashSet<String>();
      txIdAliases.add(TRANSACTION_ID);
      txIdAliases.addAll(Arrays.asList(TRANSACTION_ID_NEG_ONE_ALIASES));
      TRANSACTION_ID_ALIASES = txIdAliases.toArray(new String[txIdAliases.size()]);
   }

   public static final String GET_MAX_TX =
         "SELECT last_sequence FROM osee_sequence WHERE sequence_name = '" + SequenceManager.TRANSACTION_ID_SEQ + "'";

   private static final String BRANCH_TABLE_QUERY =
         "SELECT br1.* FROM osee_branch br1, osee_join_export_import jex1 WHERE br1.branch_id = jex1.id1 AND jex1.query_id=? ORDER BY br1.branch_id";

   private static final String BRANCH_DEFINITION_QUERY =
         "SELECT br1.* FROM osee_branch_definitions br1, osee_join_export_import jex1 WHERE br1.mapped_branch_id = jex1.id1 AND jex1.query_id=? ORDER BY br1.mapped_branch_id";

   private static final String TX_DETAILS_TABLE_QUERY =
         "SELECT txd1.TRANSACTION_ID, txd1.TIME, txd1.AUTHOR, txd1.OSEE_COMMENT, txd1.BRANCH_ID, txd1.COMMIT_ART_ID, txd1.TX_TYPE FROM osee_tx_details txd1, osee_join_export_import jex1 WHERE txd1.branch_id = jex1.id1 AND jex1.query_id=? %s ORDER BY txd1.transaction_id";

   private static final String TXS_TABLE_QUERY =
         "SELECT txs1.GAMMA_ID, txs1.TRANSACTION_ID, txs1.TX_CURRENT, txs1.MOD_TYPE FROM osee_txs txs1, osee_tx_details txd1, osee_join_export_import jex1 WHERE txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = jex1.id1 AND jex1.query_id=? %s";

   private static final String ARTIFACT_TABLE_QUERY =
         "SELECT DISTINCT (art1.art_id), art1.GUID, art1.HUMAN_READABLE_ID, art1.ART_TYPE_ID FROM osee_artifact art1, osee_artifact_version artv1, osee_txs txs1, osee_tx_details txd1, osee_join_export_import jex1 WHERE art1.art_id = artv1.art_id AND artv1.gamma_id = txs1.gamma_id AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = jex1.id1 AND jex1.query_id=? %s";

   private static final String ARTIFACT_VERSION_QUERY =
         "SELECT DISTINCT (artv1.GAMMA_ID), artv1.ART_ID, artv1.MODIFICATION_ID FROM osee_artifact_version artv1, osee_txs txs1, osee_tx_details txd1, osee_join_export_import jex1 WHERE artv1.gamma_id = txs1.gamma_id AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = jex1.id1 AND jex1.query_id=? %s";

   private static final String ATTRIBUTE_TABLE_QUERY =
         "SELECT DISTINCT (attr1.GAMMA_ID), attr1.ATTR_ID, attr1.ART_ID, attr1.MODIFICATION_ID, attr1.VALUE, attr1.ATTR_TYPE_ID, attr1.URI FROM osee_attribute attr1, osee_txs txs1, osee_tx_details txd1, osee_join_export_import jex1 WHERE attr1.gamma_id = txs1.gamma_id AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = jex1.id1 AND jex1.query_id=? %s";

   private static final String RELATION_LINK_TABLE_QUERY =
         "SELECT DISTINCT (rel1.GAMMA_ID), rel1.REL_LINK_ID, rel1.B_ART_ID, rel1.A_ART_ID, rel1.MODIFICATION_ID, rel1.RATIONALE, rel1.REL_LINK_TYPE_ID, rel1.A_ORDER, rel1.B_ORDER FROM osee_relation_link rel1, osee_txs txs1, osee_tx_details txd1, osee_join_export_import jex1 WHERE rel1.gamma_id = txs1.gamma_id AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = jex1.id1 AND jex1.query_id=? %s";

   private static final String ARTIFACT_TYPE_QUERY =
         "SELECT type1.name, type1.art_type_id FROM osee_artifact_type type1, osee_join_export_import jex1 WHERE type1.art_type_id = jex1.id1 AND jex1.query_id = ?";

   private static final String ATTRIBUTE_TYPE_QUERY =
         "SELECT type1.name, type1.attr_type_id FROM osee_attribute_type type1, osee_join_export_import jex1 WHERE type1.attr_type_id = jex1.id1 AND jex1.query_id = ?";

   private static final String RELATION_TYPE_QUERY =
         "SELECT type1.type_name, type1.rel_link_type_id FROM osee_relation_link_type type1, osee_join_export_import jex1 WHERE type1.rel_link_type_id = jex1.id1 AND jex1.query_id = ?";

   private static final String MERGE_TABLE_QUERY =
         "SELECT om1.* FROM osee_merge om1, osee_join_export_import jex1 WHERE (om1.dest_branch_id = jex1.id1 OR om1.merge_branch_id = jex1.id1) AND jex1.query_id=?;";

   private static final String CONFLICT_TABLE_QUERY =
         "SELECT oc1.* FROM osee_conflict oc1, osee_join_export_import jex1 WHERE oc1.merge_branch_id = jex1.id1 AND jex1.query_id=?;";

   static List<AbstractExportItem> createTaskList() {
      List<AbstractExportItem> items = new ArrayList<AbstractExportItem>();
      items.add(new ManifestExportItem(0, "export.manifest", items));
      items.add(new MetadataExportItem(0, "export.db.schema", items));
      items.add(new RelationalExportItem(1, "osee.branch.data", SkynetDatabase.BRANCH_TABLE.toString(),
            BRANCH_TABLE_QUERY));
      items.add(new RelationalExportItem(2, "osee.branch.definitions", SkynetDatabase.BRANCH_DEFINITIONS.toString(),
            BRANCH_DEFINITION_QUERY));
      items.add(new RelationalExportItem(3, "osee.tx.details.data", SkynetDatabase.TRANSACTION_DETAIL_TABLE.toString(),
            TX_DETAILS_TABLE_QUERY));
      items.add(new RelationalExportItem(4, "osee.txs.data", SkynetDatabase.TRANSACTIONS_TABLE.toString(),
            TXS_TABLE_QUERY));
      items.add(new RelationalExportItemWithType(5, "osee.artifact.data", SkynetDatabase.ARTIFACT_TABLE.toString(),
            ARTIFACT_TYPE_ID, ARTIFACT_TABLE_QUERY, ARTIFACT_TYPE_QUERY));
      items.add(new RelationalExportItem(6, "osee.artifact.version.data",
            SkynetDatabase.ARTIFACT_VERSION_TABLE.toString(), ARTIFACT_VERSION_QUERY));
      items.add(new RelationalExportItemWithType(7, "osee.attribute.data",
            SkynetDatabase.ATTRIBUTE_VERSION_TABLE.toString(), ATTRIBUTE_TYPE_ID, ATTRIBUTE_TABLE_QUERY,
            ATTRIBUTE_TYPE_QUERY));
      items.add(new RelationalExportItemWithType(8, "osee.relation.link.data",
            SkynetDatabase.RELATION_LINK_VERSION_TABLE.toString(), RELATION_TYPE_ID, RELATION_LINK_TABLE_QUERY,
            RELATION_TYPE_QUERY));

      items.add(new RelationalExportItem(9, "osee.merge.data", SkynetDatabase.OSEE_MERGE_TABLE.toString(),
            MERGE_TABLE_QUERY));
      items.add(new RelationalExportItem(10, "osee.conflict.data", SkynetDatabase.OSEE_CONFLICT_TABLE.toString(),
            CONFLICT_TABLE_QUERY));
      return items;
   }

   static List<IndexCollector> createCheckList() {
      List<IndexCollector> items = new ArrayList<IndexCollector>();
      items.add(new IndexCollector("osee.txs.data", GAMMA_ID, GAMMA_ID_REG_ALIASES));
      items.add(new IndexCollector("osee.tx.details.data", TRANSACTION_ID, new String[0],
            TRANSACTION_ID_NEG_ONE_ALIASES));
      items.add(new IndexCollector("osee.artifact.data", ARTIFACT_ID, ARTIFACT_ID_REG_ALIASES,
            ARTIFACT_ID_NEG_ONE_ALIASES));
      items.add(new IndexCollector("osee.branch.data", BRANCH_ID, BRANCH_ID_REG_ALIASES, BRANCH_ID_NEG_ONE_ALIASES));
      return items;
   }

   static List<BaseTranslator> createTranslators() {
      List<BaseTranslator> translators = new ArrayList<BaseTranslator>();
      translators.add(new IdTranslator(SequenceManager.GAMMA_ID_SEQ, GAMMA_ID_ALIASES));
      translators.add(new IdTranslator(SequenceManager.TRANSACTION_ID_SEQ, TRANSACTION_ID_ALIASES));
      translators.add(new IdTranslator(SequenceManager.BRANCH_ID_SEQ, BRANCH_ID_ALIASES));
      translators.add(new IdTranslator(SequenceManager.ART_TYPE_ID_SEQ, ARTIFACT_TYPE_ID));
      translators.add(new IdTranslator(SequenceManager.ATTR_TYPE_ID_SEQ, ATTRIBUTE_TYPE_ID));
      translators.add(new IdTranslator(SequenceManager.REL_LINK_TYPE_ID_SEQ, RELATION_TYPE_ID));

      Map<ConflictType, IdTranslator> translatorMap = new HashMap<ConflictType, IdTranslator>();
      translatorMap.put(ConflictType.ARTIFACT, new IdTranslator(SequenceManager.ART_ID_SEQ, ARTIFACT_ID_ALIASES));
      translatorMap.put(ConflictType.ATTRIBUTE, new IdTranslator(SequenceManager.ATTR_ID_SEQ, ATTRIBUTE_ID));
      translatorMap.put(ConflictType.RELATION, new IdTranslator(SequenceManager.REL_LINK_ID_SEQ, RELATION_ID));

      translators.addAll(translatorMap.values());
      translators.add(new EnumBaseTranslator<ConflictType>(CONFLICT_TYPE, translatorMap, CONFLICT_ID));
      return translators;
   }

   public static ObjectPair<String, Object[]> getQueryWithOptions(String originalQuery, int queryId, Options options) throws Exception {
      if (originalQuery.contains("%s") && originalQuery.contains("txd1")) {
         List<Object> dataArray = new ArrayList<Object>();
         dataArray.add(queryId);
         StringBuilder optionString = new StringBuilder();
         if (options.getBoolean(ExportOptions.EXCLUDE_BASELINE_TXS.name())) {
            optionString.append(" AND txd1.TX_TYPE = 0");
         }

         long minTxs = getMinTransaction(options);
         long maxTxs = getMaxTransaction(options);

         if (minTxs != Long.MIN_VALUE) {
            optionString.append(" AND txd1.transaction_id >= ?");
            dataArray.add(minTxs);
         }

         if (maxTxs != Long.MIN_VALUE) {
            optionString.append(" AND txd1.transaction_id <= ?");
            dataArray.add(maxTxs);
         }

         if (minTxs > maxTxs) {
            throw new Exception(String.format("Invalid transaction range: min - %d >  max - %d", minTxs, maxTxs));
         }

         return new ObjectPair<String, Object[]>(String.format(originalQuery, optionString),
               dataArray.toArray(new Object[dataArray.size()]));
      }
      return new ObjectPair<String, Object[]>(originalQuery, new Object[] {queryId});
   }

   static Long getMaxTransaction(Options options) {
      return getTransactionNumber(options, ExportOptions.MAX_TXS.name());
   }

   static Long getMinTransaction(Options options) {
      return getTransactionNumber(options, ExportOptions.MIN_TXS.name());
   }

   private static Long getTransactionNumber(Options options, String exportOption) {
      String transactionNumber = options.getString(exportOption);
      long toReturn = Long.MIN_VALUE;
      if (Strings.isValid(transactionNumber)) {
         toReturn = Long.valueOf(transactionNumber);
      }
      return toReturn;
   }

   private ExchangeDb() {
   }

}
