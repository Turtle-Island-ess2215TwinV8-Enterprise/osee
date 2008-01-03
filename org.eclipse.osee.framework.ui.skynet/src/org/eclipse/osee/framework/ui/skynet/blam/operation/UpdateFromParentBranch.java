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
package org.eclipse.osee.framework.ui.skynet.blam.operation;

import static org.eclipse.osee.framework.ui.plugin.util.db.schemas.SkynetDatabase.ARTIFACT_TABLE;
import static org.eclipse.osee.framework.ui.plugin.util.db.schemas.SkynetDatabase.ARTIFACT_VERSION_TABLE;
import static org.eclipse.osee.framework.ui.plugin.util.db.schemas.SkynetDatabase.ATTRIBUTE_VERSION_TABLE;
import static org.eclipse.osee.framework.ui.plugin.util.db.schemas.SkynetDatabase.RELATION_LINK_VERSION_TABLE;
import static org.eclipse.osee.framework.ui.plugin.util.db.schemas.SkynetDatabase.TRANSACTIONS_TABLE;
import static org.eclipse.osee.framework.ui.plugin.util.db.schemas.SkynetDatabase.TRANSACTION_DETAIL_TABLE;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionIdManager;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionType;
import org.eclipse.osee.framework.ui.plugin.sql.SQL3DataType;
import org.eclipse.osee.framework.ui.plugin.util.db.ConnectionHandler;
import org.eclipse.osee.framework.ui.plugin.util.db.schemas.LocalAliasTable;
import org.eclipse.osee.framework.ui.skynet.blam.BlamVariableMap;

/**
 * @author Ryan D. Brooks
 */
public class UpdateFromParentBranch implements BlamOperation {
   //   private static final String SELECT_USER_COUNT = "select count(*) from v$session t1 where t1.username='OSEE_CLIENT' and not exists (select null from v$session t2 where t1.machine=t2.machine and t2.sid < t1.sid)";
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(UpdateFromParentBranch.class);

   private static final LocalAliasTable ARTIFACT_VERSION_ALIAS_1 = new LocalAliasTable(ARTIFACT_VERSION_TABLE, "t1");
   private static final LocalAliasTable ARTIFACT_VERSION_ALIAS_2 = new LocalAliasTable(ARTIFACT_VERSION_TABLE, "t2");

   private static final LocalAliasTable ATTRIBUTE_ALIAS_1 = new LocalAliasTable(ATTRIBUTE_VERSION_TABLE, "t3");
   private static final LocalAliasTable ATTRIBUTE_ALIAS_2 = new LocalAliasTable(ATTRIBUTE_VERSION_TABLE, "t4");

   private static final LocalAliasTable LINK_ALIAS_1 = new LocalAliasTable(RELATION_LINK_VERSION_TABLE, "t9");
   private static final LocalAliasTable LINK_ALIAS_2 = new LocalAliasTable(RELATION_LINK_VERSION_TABLE, "t10");

   private static final TransactionIdManager transactionIdManager = TransactionIdManager.getInstance();

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#runOperation(org.eclipse.osee.framework.ui.skynet.blam.BlamVariableMap, org.eclipse.osee.framework.skynet.core.artifact.Branch, org.eclipse.core.runtime.IProgressMonitor)
    */
   public void runOperation(BlamVariableMap variableMap, Branch branch, IProgressMonitor monitor) throws Exception {
      //    <XWidget xwidgetType="XListDropViewer" displayName="artifacts" />
      //    <XWidget xwidgetType="XText" displayName="childBranchName" />      

      monitor.beginTask("Update From Parent Branch", IProgressMonitor.UNKNOWN);

      List<Artifact> artifacts = variableMap.getArtifacts("Parent Branch Artifacts to update to Child Branch");
      String childBranchName = variableMap.getString("Child Branch Name");
      Branch childBranch = BranchPersistenceManager.getInstance().getBranch(childBranchName);

      int baselineTransactionNumber =
            transactionIdManager.getStartEndPoint(childBranch).getKey().getTransactionNumber();

      Collection<Integer> artIdBlock = new ArrayList<Integer>(1000);
      for (Artifact artifact : artifacts) {
         artIdBlock.add(artifact.getArtId());
      }

      Branch parentBranch = childBranch.getParentBranch();
      TransactionId parentTransactionId = transactionIdManager.getEditableTransactionId(parentBranch);
      int parentTransactionNumber = parentTransactionId.getTransactionNumber();
      int parentBranchId = childBranch.getParentBranch().getBranchId();

      String INSERT_UPDATED_ARTIFACTS =
            "INSERT INTO " + TRANSACTIONS_TABLE + "(transaction_id, gamma_id, tx_type) " + "SELECT ?, " + ARTIFACT_VERSION_ALIAS_1.column("gamma_id") + ", ?" + " FROM " + ARTIFACT_TABLE + " , " + ARTIFACT_VERSION_ALIAS_1 + "," + TRANSACTIONS_TABLE + " WHERE " + ARTIFACT_TABLE.column("art_id") + " IN " + Collections.toString(
                  artIdBlock, "(", ",", ")") + " AND " + ARTIFACT_TABLE.column("art_id") + " = " + ARTIFACT_VERSION_ALIAS_1.column("art_id") + " AND " + ARTIFACT_VERSION_ALIAS_1.column("gamma_id") + " = " + TRANSACTIONS_TABLE.column("gamma_id") + " AND " + TRANSACTIONS_TABLE.column("transaction_id") + "=" + "(SELECT " + TRANSACTION_DETAIL_TABLE.max("transaction_id") + " FROM " + ARTIFACT_VERSION_ALIAS_2 + "," + TRANSACTIONS_TABLE + "," + TRANSACTION_DETAIL_TABLE + " WHERE " + ARTIFACT_VERSION_ALIAS_1.column("art_id") + " = " + ARTIFACT_VERSION_ALIAS_2.column("art_id") + " AND " + ARTIFACT_VERSION_ALIAS_2.column("gamma_id") + " = " + TRANSACTIONS_TABLE.column("gamma_id") + " AND " + TRANSACTIONS_TABLE.column("transaction_id") + "=" + TRANSACTION_DETAIL_TABLE.column("transaction_id") + " AND " + TRANSACTION_DETAIL_TABLE.column("transaction_id") + "<=?" + " AND " + TRANSACTION_DETAIL_TABLE.column("branch_id") + "=?)";

      String INSERT_UPDATED_ATTRIBUTES_GAMMAS =
            "INSERT INTO " + TRANSACTIONS_TABLE + "(transaction_id, gamma_id, tx_type) " + "SELECT ?, " + ATTRIBUTE_ALIAS_1.columns("gamma_id") + ", ?" + " FROM " + TRANSACTIONS_TABLE + "," + ATTRIBUTE_ALIAS_1 + " WHERE " + ATTRIBUTE_ALIAS_1.column("art_id") + " IN " + Collections.toString(
                  artIdBlock, "(", ",", ")") + " AND " + ATTRIBUTE_ALIAS_1.column("gamma_id") + " = " + TRANSACTIONS_TABLE.column("gamma_id") + " AND " + TRANSACTIONS_TABLE.column("transaction_id") + "=" + "(SELECT " + TRANSACTION_DETAIL_TABLE.max("transaction_id") + " FROM " + ATTRIBUTE_ALIAS_2 + "," + TRANSACTIONS_TABLE + "," + TRANSACTION_DETAIL_TABLE + " WHERE " + ATTRIBUTE_ALIAS_1.column("attr_id") + " = " + ATTRIBUTE_ALIAS_2.column("attr_id") + " AND " + ATTRIBUTE_ALIAS_2.column("gamma_id") + " = " + TRANSACTIONS_TABLE.column("gamma_id") + " AND " + TRANSACTIONS_TABLE.column("transaction_id") + "=" + TRANSACTION_DETAIL_TABLE.column("transaction_id") + " AND " + TRANSACTION_DETAIL_TABLE.column("transaction_id") + "<=?" + " AND " + TRANSACTION_DETAIL_TABLE.column("branch_id") + "=?)";

      String INSERT_UPDATED_LINKS_GAMMAS =
            "INSERT INTO " + TRANSACTIONS_TABLE + "(transaction_id, gamma_id, tx_type) " + "SELECT ?, " + LINK_ALIAS_1.columns("gamma_id") + ", ?" + " FROM " + TRANSACTIONS_TABLE + "," + LINK_ALIAS_1 + " WHERE (" + LINK_ALIAS_1.column("a_art_id") + " IN " + Collections.toString(
                  artIdBlock, "(", ",", ")") + " OR " + LINK_ALIAS_1.column("b_art_id") + " IN " + Collections.toString(
                  artIdBlock, "(", ",", ")") + ") AND " + LINK_ALIAS_1.column("gamma_id") + " = " + TRANSACTIONS_TABLE.column("gamma_id") + " AND " + TRANSACTIONS_TABLE.column("transaction_id") + "=" + "(SELECT " + TRANSACTION_DETAIL_TABLE.max("transaction_id") + " FROM " + LINK_ALIAS_2 + "," + TRANSACTIONS_TABLE + "," + TRANSACTION_DETAIL_TABLE + " WHERE " + LINK_ALIAS_1.column("rel_link_id") + " = " + LINK_ALIAS_2.column("rel_link_id") + " AND " + LINK_ALIAS_2.column("gamma_id") + " = " + TRANSACTIONS_TABLE.column("gamma_id") + " AND " + TRANSACTIONS_TABLE.column("transaction_id") + "=" + TRANSACTION_DETAIL_TABLE.column("transaction_id") + " AND " + TRANSACTION_DETAIL_TABLE.column("transaction_id") + "<=?" + " AND " + TRANSACTION_DETAIL_TABLE.column("branch_id") + "=?)";

      String DELETE_GAMMAS_FOR_UPDATES =
            "DELETE FROM " + TRANSACTIONS_TABLE + " WHERE " + TRANSACTIONS_TABLE.column("transaction_id") + " = ? AND " + TRANSACTIONS_TABLE.column("gamma_id") + " IN " + "(SELECT " + RELATION_LINK_VERSION_TABLE.column("gamma_id") + " FROM " + RELATION_LINK_VERSION_TABLE + " WHERE " + RELATION_LINK_VERSION_TABLE.column("a_art_id") + " IN " + Collections.toString(
                  artIdBlock, "(", ",", ")") + " OR " + RELATION_LINK_VERSION_TABLE.column("b_art_id") + " IN " + Collections.toString(
                  artIdBlock, "(", ",", ")") + " UNION " + "SELECT " + ATTRIBUTE_VERSION_TABLE.columns("gamma_id") + " FROM " + ATTRIBUTE_VERSION_TABLE + " WHERE " + ATTRIBUTE_VERSION_TABLE.column("art_id") + " IN " + Collections.toString(
                  artIdBlock, "(", ",", ")") + " UNION " + "SELECT " + ARTIFACT_VERSION_TABLE.columns("gamma_id") + " FROM " + ARTIFACT_VERSION_TABLE + " WHERE " + ARTIFACT_VERSION_TABLE.column("art_id") + " IN " + Collections.toString(
                  artIdBlock, "(", ",", ")") + ")";

      int count =
            ConnectionHandler.runPreparedUpdateReturnCount(DELETE_GAMMAS_FOR_UPDATES, SQL3DataType.INTEGER,
                  baselineTransactionNumber);
      logger.log(Level.INFO, "deleted " + count + " gammas");

      count =
            ConnectionHandler.runPreparedUpdateReturnCount(INSERT_UPDATED_ARTIFACTS, SQL3DataType.INTEGER,
                  baselineTransactionNumber, SQL3DataType.INTEGER, TransactionType.BRANCHED.getId(),
                  SQL3DataType.INTEGER, parentTransactionNumber, SQL3DataType.INTEGER, parentBranchId);
      logger.log(Level.INFO, "inserted " + count + " artifacts");

      count =
            ConnectionHandler.runPreparedUpdateReturnCount(INSERT_UPDATED_ATTRIBUTES_GAMMAS, SQL3DataType.INTEGER,
                  baselineTransactionNumber, SQL3DataType.INTEGER, TransactionType.BRANCHED.getId(),
                  SQL3DataType.INTEGER, parentTransactionNumber, SQL3DataType.INTEGER, parentBranchId);
      logger.log(Level.INFO, "inserted " + count + " attributes");

      count =
            ConnectionHandler.runPreparedUpdateReturnCount(INSERT_UPDATED_LINKS_GAMMAS, SQL3DataType.INTEGER,
                  baselineTransactionNumber, SQL3DataType.INTEGER, TransactionType.BRANCHED.getId(),
                  SQL3DataType.INTEGER, parentTransactionNumber, SQL3DataType.INTEGER, parentBranchId);
      logger.log(Level.INFO, "inserted " + count + " relations");

      monitor.done();
   }
}
