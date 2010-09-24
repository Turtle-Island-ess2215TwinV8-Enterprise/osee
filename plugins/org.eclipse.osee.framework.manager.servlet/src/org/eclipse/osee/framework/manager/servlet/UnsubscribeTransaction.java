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
package org.eclipse.osee.framework.manager.servlet;

import java.sql.Timestamp;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.branch.management.commit.UpdatePreviousTxCurrent;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.AbstractDbTxOperation;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.framework.manager.servlet.internal.Activator;

public final class UnsubscribeTransaction extends AbstractDbTxOperation {
   private final static String SELECT_RELATION_LINK =
      "select txs.gamma_id, rel.rel_link_id, txs.mod_type from osee_relation_link rel, osee_txs txs where rel.a_art_id = ? and rel.b_art_id = ? and rel.rel_link_type_id = ? and rel.gamma_id=txs.gamma_id and txs.branch_id = ? and txs.tx_current <> ? order by txs.tx_current";
   private final static String INSERT_INTO_TX_DETAILS =
      "insert into osee_tx_details (branch_id, transaction_id, osee_comment, time, author, tx_type) values (?,?,?,?,?,?)";
   private final static String INSERT_INTO_TXS =
      "insert into osee_txs (mod_type, tx_current, transaction_id, gamma_id, branch_id) values (?, ?, ?, ?, ?)";

   private Branch common;
   private int relationId;
   private int currentGammaId;
   private final UnsubscribeRequest unsubscribeData;
   private final IOseeCachingService cacheService;
   private String completionMethod;

   public UnsubscribeTransaction(IOseeDatabaseService databaseService, IOseeCachingService cacheService, UnsubscribeRequest unsubscribeData) {
      super(databaseService, "Delete Relation", Activator.PLUGIN_ID);
      this.unsubscribeData = unsubscribeData;
      this.cacheService = cacheService;
   }

   @Override
   protected void doTxWork(IProgressMonitor monitor, OseeConnection connection) throws OseeCoreException {
      if (getRelationTxData()) {
         UpdatePreviousTxCurrent txc = new UpdatePreviousTxCurrent(common, connection);
         txc.addRelation(relationId);
         txc.updateTxNotCurrents();

         createNewTxAddressing(connection);
      }
   }

   private boolean getRelationTxData() throws OseeCoreException {
      common = cacheService.getBranchCache().getCommonBranch();
      RelationType relationType = cacheService.getRelationTypeCache().get(CoreRelationTypes.Users_Artifact);
      IOseeStatement chStmt = getDatabaseService().getStatement();

      try {
         chStmt.runPreparedQuery(1, SELECT_RELATION_LINK, unsubscribeData.getGroupId(), unsubscribeData.getUserId(),
            relationType.getId(), common.getId(), TxChange.NOT_CURRENT.getValue());
         if (chStmt.next()) {
            currentGammaId = chStmt.getInt("gamma_id");
            relationId = chStmt.getInt("rel_link_id");
            int modType = chStmt.getInt("mod_type");
            return ensureNotAlreadyDeleted(modType);
         } else {
            throw new OseeStateException(
               "No existing relation (deleted or otherwise) was found for group [%s] and user [%s].",
               unsubscribeData.getGroupId(), unsubscribeData.getUserId());
         }
      } finally {
         Lib.close(chStmt);
      }
   }

   private boolean ensureNotAlreadyDeleted(int modType) {
      if (modType == ModificationType.ARTIFACT_DELETED.getValue() || modType == ModificationType.DELETED.getValue()) {
         completionMethod =
            String.format("<br/>You have already been removed from the group.<br/>  group [%s] user [%s]",
               unsubscribeData.getGroupId(), unsubscribeData.getUserId());
         return false;
      } else {
         completionMethod = String.format("<br/>You have been successfully unsubscribed.");
         return true;
      }
   }

   @SuppressWarnings("unchecked")
   private void createNewTxAddressing(OseeConnection connection) throws OseeCoreException {
      int transactionId = getDatabaseService().getSequence().getNextTransactionId();
      String comment =
         String.format("User %s requested unsubscribe from group %s", unsubscribeData.getUserId(),
            unsubscribeData.getGroupId());
      Timestamp timestamp = GlobalTime.GreenwichMeanTimestamp();
      int txType = TransactionDetailsType.NonBaselined.getId();

      getDatabaseService().runPreparedUpdate(connection, INSERT_INTO_TX_DETAILS, common.getId(), transactionId,
         comment, timestamp, unsubscribeData.getUserId(), txType);
      getDatabaseService().runPreparedUpdate(connection, INSERT_INTO_TXS, ModificationType.DELETED.getValue(),
         TxChange.DELETED.getValue(), transactionId, currentGammaId, common.getId());
   }

   public String getCompletionMessage() {
      return completionMethod;
   }
}