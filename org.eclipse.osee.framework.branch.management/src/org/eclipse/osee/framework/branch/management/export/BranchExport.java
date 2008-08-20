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
package org.eclipse.osee.framework.branch.management.export;

import java.io.File;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.eclipse.osee.framework.branch.management.ExportOptions;
import org.eclipse.osee.framework.branch.management.IBranchExport;
import org.eclipse.osee.framework.db.connection.core.JoinUtility;
import org.eclipse.osee.framework.db.connection.core.JoinUtility.ExportImportJoinQuery;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.resource.management.Options;

/**
 * @author Roberto E. Escobar
 */
public class BranchExport implements IBranchExport {
   private static final String ZIP_EXTENSION = ".zip";

   private static final String BRANCH_TABLE_QUERY =
         "SELECT br1.* FROM osee_define_branch br1, osee_join_export_import jex1 WHERE br1.branch_id = jnart1.branch_id AND jnart1.query_id=?";

   private static final String ATTRIBUTE_TABLE_QUERY =
         "SELECT attr1.* FROM osee_define_attribute attr1, osee_define_txs txs1, osee_define_tx_details txd1, osee_join_export_import jex1 WHERE attr1.gamma_id = txs1.gamma_id AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = jex1.id1 AND jex1.query_id=?";

   private static final String RELATION_LINK_TABLE_QUERY =
         "SELECT rel1.* FROM osee_define_rel_link rel1, osee_define_txs txs1, osee_define_tx_details txd1, osee_join_export_import jex1 WHERE rel1.gamma_id = txs1.gamma_id AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = jex1.id1 AND jex1.query_id=?";

   private static final String TX_DETAILS_TABLE_QUERY =
         "SELECT txd1.* FROM osee_define_tx_details txd1, osee_join_export_import jex1 WHERE txd1.branch_id = jex1.id1 AND jex1.query_id=?";

   private static final String TXS_TABLE_QUERY =
         "SELECT txs1.* FROM osee_define_txs txs1, osee_define_tx_details txd1, osee_join_export_import jex1 WHERE txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = jex1.id1 AND jex1.query_id=?";

   private static final String ARTIFACT_TABLE_QUERY =
         "SELECT art1.* FROM osee_define_artifact art1, osee_define_artifact_version artv1, osee_define_txs txs1, osee_define_tx_details txd1, osee_join_export_import jex1 WHERE art1.art_id = artv1.art_id AND artv1.gamma_id = txs1.gamma_id AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = jex1.id1 AND jex1.query_id=?";

   private static final String ARTIFACT_VERSION_QUERY =
         "SELECT artv1.* FROM osee_define_artifact_version artv1, osee_define_txs txs1, osee_define_tx_details txd1, osee_join_export_import jex1 WHERE artv1.gamma_id = txs1.gamma_id AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = jex1.id1 AND jex1.query_id=?";

   private static final String ARTIFACT_TYPE_QUERY =
         "SELECT type1.name, type1.art_type_id FROM osee_define_artifact_type type1, osee_join_export_import jex1 WHERE type1.art_type_id = jex1.id1 AND jex1.query_id = ?";

   private static final String ATTRIBUTE_TYPE_QUERY =
         "SELECT type1.name, type1.art_type_id FROM osee_define_attribute_type type1, osee_join_export_import jex1 WHERE type1.art_type_id = jex1.id1 AND jex1.query_id = ?";

   private static final String RELATION_TYPE_QUERY =
         "SELECT type1.name, type1.rel_link_type_id FROM osee_define_rel_link_type type1, osee_join_export_import jex1 WHERE type1.rel_link_type_id = jex1.id1 AND jex1.query_id = ?";

   private ExecutorService executorService;
   private Map<Integer, FutureTask<Object>> futureTasks;

   public BranchExport() {
      this.executorService = Executors.newSingleThreadExecutor();
      this.futureTasks = Collections.synchronizedMap(new HashMap<Integer, FutureTask<Object>>());
   }

   public void export(String exportName, Options options, int... branchIds) throws Exception {
      ExportController controller = new ExportController(exportName, options, branchIds);
      submitTask(controller.getExportQueryId(), controller);
      synchronized (controller) {
         controller.wait();
      }
   }

   private void submitTask(int exportQueryId, Runnable runnable) {
      FutureTask<Object> futureTask = new FutureTask<Object>(runnable, null);
      this.futureTasks.put(exportQueryId, futureTask);
      this.executorService.submit(futureTask);
   }

   private FutureTask<Object> getFuture(int queryId) {
      return futureTasks.get(queryId);
   }

   private List<AbstractExportItem> getTaskList() {
      List<AbstractExportItem> exportItems = new ArrayList<AbstractExportItem>();
      exportItems.add(new ManifestExportItem("index.xml", 0, exportItems));

      exportItems.add(new RelationalExportItem("osee.define.branch", 1, BRANCH_TABLE_QUERY));
      exportItems.add(new RelationalExportItem("osee.define.tx.details", 2, TX_DETAILS_TABLE_QUERY));
      exportItems.add(new RelationalExportItem("osee.define.txs", 3, TXS_TABLE_QUERY));
      exportItems.add(new RelationalExportItem("osee.define.artifact.version", 5, ARTIFACT_VERSION_QUERY));
      exportItems.add(new RelationalExportItemWithType("osee.define.artifact", 4, "art_type_id", ARTIFACT_TABLE_QUERY,
            ARTIFACT_TYPE_QUERY));
      exportItems.add(new RelationalExportItemWithType("osee.define.attribute", 6, "attr_type_id",
            ATTRIBUTE_TABLE_QUERY, ATTRIBUTE_TYPE_QUERY));
      exportItems.add(new RelationalExportItemWithType("osee.define.rel.link", 7, "rel_link",
            RELATION_LINK_TABLE_QUERY, RELATION_TYPE_QUERY));
      return exportItems;
   }

   private final class ExportController implements IExportListener, Runnable {
      private String exportName;
      private Options options;
      private int[] branchIds;
      private ExportImportJoinQuery joinQuery;

      public ExportController(String exportName, Options options, int... branchIds) throws Exception {
         if (branchIds == null || branchIds.length <= 0) {
            throw new Exception("No branch selected for export.");
         }
         this.exportName = exportName;
         this.options = options;
         this.branchIds = branchIds;
         this.joinQuery = JoinUtility.createExportImportJoinQuery();
      }

      public int getExportQueryId() {
         return joinQuery != null ? joinQuery.getQueryId() : -1;
      }

      private void cleanUp(List<AbstractExportItem> taskList) {
         for (AbstractExportItem exportItem : taskList) {
            exportItem.cleanUp();
         }
         try {
            if (joinQuery != null) {
               joinQuery.delete();
               joinQuery = null;
            }
         } catch (SQLException ex) {
            onException("Export Clean-Up", ex);
         }
      }

      private File createTempFolder() {
         String userHome = System.getProperty("user.home");
         File rootDirectory = new File(userHome + File.separator + "export." + new Date().getTime() + File.separator);
         rootDirectory.mkdirs();
         return rootDirectory;
      }

      private void setUp(List<AbstractExportItem> taskList, File tempFolder) throws SQLException {
         joinQuery = JoinUtility.createExportImportJoinQuery();
         for (int branchId : branchIds) {
            joinQuery.add(branchId, -1);
         }
         joinQuery.store();
         for (AbstractExportItem exportItem : taskList) {
            exportItem.setOptions(options);
            exportItem.setWriteLocation(tempFolder);
            if (exportItem instanceof RelationalExportItem) {
               RelationalExportItem relationalExportItem = ((RelationalExportItem) exportItem);
               relationalExportItem.setJoinQueryId(joinQuery.getQueryId());
            }
            exportItem.addExportListener(this);
         }
      }

      /* (non-Javadoc)
       * @see java.lang.Runnable#run()
       */
      @Override
      public void run() {
         long startTime = System.currentTimeMillis();
         List<AbstractExportItem> taskList = getTaskList();
         try {
            File tempFolder = createTempFolder();
            setUp(taskList, tempFolder);

            for (AbstractExportItem exportItem : taskList) {
               submitTask(joinQuery.getQueryId(), exportItem);
            }

            String zipTargetName = exportName + ZIP_EXTENSION;
            System.out.println(String.format("Compressing Branch Export Data - [%s]", zipTargetName));
            File zipTarget = new File(tempFolder.getParent(), zipTargetName);
            Lib.compressDirectory(tempFolder, zipTarget.getAbsolutePath(), true);
            Lib.deleteDir(tempFolder);
         } catch (Exception ex) {

         } finally {
            cleanUp(taskList);
         }

         System.out.println(String.format("Exported [%s] branches in [%s]", branchIds != null ? branchIds.length : 0,
               Lib.getElapseString(startTime)));
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.branch.management.export.IExportListener#onException(java.lang.String, java.lang.Throwable)
       */
      @Override
      synchronized public void onException(String name, Throwable ex) {
         System.err.println(String.format("Export Error in: [%s]\n", name));
         ex.printStackTrace(System.err);
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.branch.management.export.IExportListener#onExportItemCompleted(java.lang.String, long)
       */
      @Override
      synchronized public void onExportItemCompleted(String name, long timeToProcess) {
         System.out.println(String.format("Exported: [%s] in [%s] ms", name, timeToProcess));
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.branch.management.export.IExportListener#onExportItemStarted(java.lang.String)
       */
      @Override
      public void onExportItemStarted(String name) {
         System.out.println(String.format("Exporting: [%s] ", name));
      }
   }

   private final class ManifestExportItem extends AbstractExportItem {
      private List<AbstractExportItem> exportItems;

      public ManifestExportItem(String name, int priority, List<AbstractExportItem> exportItems) {
         super(name, priority);
         this.exportItems = exportItems;
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.skynet.core.export.AbstractExportItem#doWork(java.io.File, java.io.Writer, int)
       */
      @Override
      protected void doWork(Writer writer) throws Exception {
         for (AbstractExportItem relationalItem : exportItems) {
            if (!relationalItem.equals(this)) {
               writer.write(String.format("<entry id=\"%s\" priority=\"%s\" />\n", relationalItem.getName(),
                     relationalItem.getPriority()));
            }

            StringBuffer buffer = new StringBuffer();
            for (ExportOptions exportOptions : ExportOptions.values()) {
               buffer.append(String.format("%s=\"%s\" ", exportOptions.name(), getOptions().getBoolean(
                     exportOptions.name())));
            }
            writer.write(String.format("<options %s />\n", buffer.toString()));
         }
      }
   }
}
