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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.ui.skynet.results.XResultData;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactIdWithoutVersionsCheck extends DatabaseHealthOperation {

   private static final String GET_INVALID_A_ART_IDS =
         "select item.a_art_id as artId, item.rel_link_id as itemId from osee_relation_link item where NOT EXISTS (select oav.art_id from osee_artifact_version oav where oav.art_id = item.a_art_id)";

   private static final String GET_INVALID_B_ART_IDS =
         "select item.b_art_id as artId, item.rel_link_id as itemId from osee_relation_link item where NOT EXISTS (select oav.art_id from osee_artifact_version oav where oav.art_id = item.b_art_id)";

   private static final String GET_INVALID_ATTR_IDS_ART_IDS =
         "select item.art_id as artId, item.attr_id as itemId from osee_attribute item where NOT EXISTS (select oav.art_id from osee_artifact_version oav where oav.art_id = item.art_id)";

   private static final String GET_INVALID_ART_IDS =
         "select item.art_id from osee_artifact item where NOT EXISTS (select oav.art_id from osee_artifact_version oav where oav.art_id = item.art_id)";

   /**
    * @param operationName
    */
   public ArtifactIdWithoutVersionsCheck() {
      super("Artifact Id Without Version Errors");
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.dbHealth.DatabaseHealthOperation#doHealthCheck(org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   protected void doHealthCheck(IProgressMonitor monitor) throws Exception {
      Set<Integer> allInvalidArtIds = new HashSet<Integer>();
      List<ItemEntry> itemsToDelete = new ArrayList<ItemEntry>();

      itemsToDelete.add(new ItemEntry("osee_relation_link", "rel_link_id", "a_art_id", // 
            getInvalidEntries(monitor, allInvalidArtIds, GET_INVALID_A_ART_IDS, true)));

      itemsToDelete.add(new ItemEntry("osee_relation_link", "rel_link_id", "b_art_id", //
            getInvalidEntries(monitor, allInvalidArtIds, GET_INVALID_B_ART_IDS, true)));

      itemsToDelete.add(new ItemEntry("osee_attribute", "attr_id", "art_id", //
            getInvalidEntries(monitor, allInvalidArtIds, GET_INVALID_ATTR_IDS_ART_IDS, true)));

      int beforeArtifactCheck = allInvalidArtIds.size();
      itemsToDelete.add(new ItemEntry("osee_artifact", "art_id", "art_id", //
            getInvalidEntries(monitor, allInvalidArtIds, GET_INVALID_ART_IDS, false)));

      setItemsToFix(allInvalidArtIds.size());
      if (isShowDetailsEnabled()) {
         createReport(monitor, beforeArtifactCheck, getItemsToFixCount(), itemsToDelete);
      } else {
         monitor.worked(calculateWork(0.10));
      }

      if (false && isFixOperationEnabled() && getItemsToFixCount() > 0) {
         for (ItemEntry entry : itemsToDelete) {
            if (!entry.invalids.isEmpty()) {
               String deleteSql = String.format("delete from %s where %s = ?", entry.table, entry.itemIdName);
               List<Object[]> dataList = new ArrayList<Object[]>();
               for (Integer item : entry.invalids) {
                  dataList.add(new Object[] {item});
               }
               ConnectionHandler.runBatchUpdate(deleteSql, dataList);
            }
         }
      }
      getSummary().append(String.format("Found [%s] invalid artIds referencedBy", getItemsToFixCount()));
      monitor.worked(calculateWork(0.50));
   }

   private XResultData createReport(IProgressMonitor monitor, int totalBeforeCheck, int totalArtIds, List<ItemEntry> itemsToDelete) {
      monitor.subTask(String.format("Create [%s] Report", getName()));
      StringBuffer sbFull = new StringBuffer(AHTML.beginMultiColumnTable(100, 1));
      sbFull.append(AHTML.beginMultiColumnTable(100, 1));
      sbFull.append(AHTML.addHeaderRowMultiColumnTable(new String[] {"TABLE", "REFERENCED_BY", "TOTAL INVALIDS"}));
      for (ItemEntry entry : itemsToDelete) {
         sbFull.append(AHTML.addRowMultiColumnTable(new String[] {entry.table, entry.invalidField,
               String.valueOf(entry.invalids.size())}));
      }
      sbFull.append(AHTML.endMultiColumnTable());
      XResultData rd = new XResultData();
      rd.addRaw(sbFull.toString());
      monitor.worked(calculateWork(0.10));
      return rd;
   }

   private Set<Integer> getInvalidEntries(IProgressMonitor monitor, Set<Integer> allInvalidArtIds, String query, boolean hasItemId) throws OseeDataStoreException {
      Set<Integer> toReturn = new HashSet<Integer>();
      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
      try {
         chStmt.runPreparedQuery(query);
         while (chStmt.next()) {
            if (hasItemId) {
               toReturn.add(chStmt.getInt("itemId"));
            }
            allInvalidArtIds.add(chStmt.getInt("artId"));
         }
      } finally {
         chStmt.close();
      }
      monitor.worked(calculateWork(0.10));
      checkForCancelledStatus(monitor);
      return hasItemId ? toReturn : allInvalidArtIds;
   }

   private final class ItemEntry {
      private final String table;
      private final String itemIdName;
      private final String invalidField;
      private final Set<Integer> invalids;

      public ItemEntry(String table, String itemIdName, String invalidField, Set<Integer> invalids) {
         super();
         this.table = table;
         this.itemIdName = itemIdName;
         this.invalidField = invalidField;
         this.invalids = invalids;
      }

   }
}
