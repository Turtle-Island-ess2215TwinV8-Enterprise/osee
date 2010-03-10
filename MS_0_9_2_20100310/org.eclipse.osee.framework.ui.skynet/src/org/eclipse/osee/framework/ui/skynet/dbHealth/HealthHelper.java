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
package org.eclipse.osee.framework.ui.skynet.dbHealth;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.ui.skynet.results.table.ResultsEditorTableTab;
import org.eclipse.osee.framework.ui.skynet.results.table.ResultsXViewerRow;

/**
 * @author Theron Virgin
 */
public class HealthHelper {
   public static final String ALL_BACKING_GAMMAS =
         "(SELECT gamma_id FROM osee_arts UNION SELECT gamma_id FROM osee_attribute UNION SELECT gamma_id FROM osee_relation_link)";

   private static final String[] NO_TX_CURRENT_SET =
         {
               "SELECT distinct t1.",
               ", txs1.branch_id FROM osee_txs txs1, ",
               " t1 WHERE txs1.gamma_id = t1.gamma_id AND txs1.tx_current = 0 %s SELECT distinct t2.",
               ", txs2.branch_id FROM osee_txs txs2, ",
               " t2 WHERE txs2.gamma_id = t2.gamma_id AND txs2.tx_current != 0"};

   private static final String[] MULTIPLE_TX_CURRENT_SET =
         {
               "SELECT resulttable.branch_id, resulttable.",
               ", COUNT(resulttable.branch_id) AS numoccurrences FROM (SELECT txs1.branch_id, t1.",
               " FROM osee_txs txs1, ",
               " t1 WHERE txs1.gamma_id = t1.gamma_id AND txs1.tx_current != 0) resulttable GROUP BY resulttable.branch_id, resulttable.",
               " HAVING(COUNT(resulttable.branch_id) > 1) order by branch_id"};

   private static final String[] NO_TX_CURRENT_CLEANUP =
         {
               "UPDATE osee_txs SET tx_current = CASE WHEN mod_type = 3 THEN 2 WHEN mod_type = 5 THEN 3 ELSE 1 END WHERE (gamma_id, transaction_id) = (SELECT txs1.gamma_id, txs1.transaction_id FROM osee_txs txs1, ",
               " t1 WHERE t1.",
               " = ? AND t1.gamma_id = txs1.gamma_id AND txs1.transaction_id = (SELECT max(txs.transaction_id) FROM osee_txs txs, ",
               " t2 WHERE txs.branch_id = ? AND txs.gamma_id = t2.gamma_id AND t2.",
               " = ?))"};

   private static final String[] DUPLICATE_TX_CURRENT_CLEANUP =
         {
               "UPDATE osee_txs SET tx_current = 0 WHERE (gamma_id, transaction_id) in (SELECT txs1.gamma_id, txs1.transaction_id FROM osee_txs txs1, ",
               " t1 WHERE t1.",
               " = ? AND t1.gamma_id = txs1.gamma_id AND txs1.transaction_id != (SELECT max(txs.transaction_id) FROM osee_txs txs, ",
               " t2 WHERE txs.branch_id = ? AND txs.tx_current != 0 AND txs.gamma_id = t2.gamma_id AND t2.",
               " = ?)AND txs1.branch_id = ?)"};

   private static final boolean DEBUG =
         "TRUE".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.osee.framework.ui.skynet/debug/Blam"));

   public static void displayForCleanUp(String header, Appendable detailedReport, Appendable summary, boolean verify, List<Object[]> set, String toPrint) throws IOException {
      int count = 0;
      detailedReport.append(AHTML.addHeaderRowMultiColumnTable(new String[] {header}));
      detailedReport.append(AHTML.addRowSpanMultiColumnTable(header + toPrint, 1));
      for (Object[] value : set) {
         count++;
         detailedReport.append(AHTML.addRowMultiColumnTable(new String[] {value[0].toString()}));
      }
      summary.append(verify ? "Found " : "Fixed ");
      summary.append(String.valueOf(count));
      summary.append(" ");
      summary.append(header);
      summary.append(toPrint);
   }

   public static List<Object[]> runSingleResultQuery(String sql, String dbColumn) throws OseeCoreException {
      List<Object[]> foundItems = new LinkedList<Object[]>();
      IOseeStatement chStmt = ConnectionHandler.getStatement();
      try {
         chStmt.runPreparedQuery(sql);
         while (chStmt.next()) {
            foundItems.add(new Object[] {chStmt.getInt(dbColumn)});
         }
      } finally {
         chStmt.close();
      }
      return foundItems;
   }

   public static HashSet<Pair<Integer, Integer>> getNoTxCurrentSet(String dataColumnName, String tableName, ResultsEditorTableTab resultsTab) throws Exception {
      HashSet<Pair<Integer, Integer>> noneSet = new HashSet<Pair<Integer, Integer>>();
      IOseeStatement chStmt = ConnectionHandler.getStatement();
      String sql =
            NO_TX_CURRENT_SET[0] + dataColumnName + NO_TX_CURRENT_SET[1] + tableName + String.format(
                  NO_TX_CURRENT_SET[2], chStmt.getComplementSql()) + dataColumnName + NO_TX_CURRENT_SET[3] + tableName + NO_TX_CURRENT_SET[4];

      try {
         chStmt.runPreparedQuery(sql);
         int counter = 0;
         while (chStmt.next()) {
            noneSet.add(new Pair<Integer, Integer>(chStmt.getInt(dataColumnName), chStmt.getInt("branch_id")));

            resultsTab.addRow(new ResultsXViewerRow(new String[] {String.valueOf(counter++),
                  String.valueOf(chStmt.getInt(dataColumnName)), String.valueOf(chStmt.getInt("branch_id"))}));
         }
      } finally {
         chStmt.close();
      }
      return noneSet;
   }

   public static HashSet<LocalTxData> getMultipleTxCurrentSet(String dataId, String dataTable, Appendable builder, String data) throws Exception {
      String sql =
            MULTIPLE_TX_CURRENT_SET[0] + dataId + MULTIPLE_TX_CURRENT_SET[1] + dataId + MULTIPLE_TX_CURRENT_SET[2] + dataTable + MULTIPLE_TX_CURRENT_SET[3] + dataId + MULTIPLE_TX_CURRENT_SET[4];
      IOseeStatement chStmt = ConnectionHandler.getStatement();
      HashSet<LocalTxData> multipleSet = new HashSet<LocalTxData>();

      long time = System.currentTimeMillis();
      try {
         chStmt.runPreparedQuery(sql);
         while (chStmt.next()) {
            multipleSet.add(new LocalTxData(chStmt.getInt(dataId), chStmt.getInt("branch_id"),
                  chStmt.getInt("numoccurrences")));
         }
         builder.append("Found ");
         builder.append(String.valueOf(multipleSet.size()));
         builder.append(data);
         builder.append(" that have multiple tx_current values set\n");

      } finally {
         chStmt.close();
      }
      if (DEBUG) {
         System.out.println(String.format("%sTxCurrent: The get%s Query took %s", data, data, Lib.getElapseString(time)));
      }
      return multipleSet;
   }

   public static void cleanMultipleTxCurrent(String dataId, String dataTable, Appendable builder, HashSet<LocalTxData> multipleSet) throws Exception {
      String sql =
            DUPLICATE_TX_CURRENT_CLEANUP[0] + dataTable + DUPLICATE_TX_CURRENT_CLEANUP[1] + dataId + DUPLICATE_TX_CURRENT_CLEANUP[2] + dataTable + DUPLICATE_TX_CURRENT_CLEANUP[3] + dataId + DUPLICATE_TX_CURRENT_CLEANUP[4];

      List<Object[]> insertParameters = new LinkedList<Object[]>();

      for (LocalTxData link : multipleSet) {
         insertParameters.add(new Object[] {link.dataId, link.branchId, link.dataId, link.branchId});
      }
      int total = 0;
      if (insertParameters.size() > 0) {
         total = ConnectionHandler.runBatchUpdate(sql, insertParameters);
      }
      builder.append("Fixed " + total + " Tx_Current duplication errors\n");
   }

   public static void cleanNoTxCurrent(String dataId, String dataTable, Appendable builder, HashSet<Pair<Integer, Integer>> noneSet) throws Exception {
      String sql =
            NO_TX_CURRENT_CLEANUP[0] + dataTable + NO_TX_CURRENT_CLEANUP[1] + dataId + NO_TX_CURRENT_CLEANUP[2] + dataTable + NO_TX_CURRENT_CLEANUP[3] + dataId + NO_TX_CURRENT_CLEANUP[4];

      List<Object[]> insertParameters = new LinkedList<Object[]>();

      for (Pair<Integer, Integer> pair : noneSet) {
         insertParameters.add(new Object[] {pair.getFirst(), pair.getSecond(), pair.getFirst()});
      }
      int total = 0;
      if (insertParameters.size() > 0) {
         total = ConnectionHandler.runBatchUpdate(sql, insertParameters);
      }
      builder.append("Fixed " + total + " Tx_Current not set errors\n");
   }

   public static void dumpDataMultiple(Appendable sbFull, HashSet<LocalTxData> multipleSet) throws IOException {
      int counter = 0;
      for (LocalTxData link : multipleSet) {
         sbFull.append(AHTML.addRowMultiColumnTable(new String[] {String.valueOf(counter++),
               String.valueOf(link.dataId), String.valueOf(link.branchId), String.valueOf(link.number)}));
      }
   }
}
