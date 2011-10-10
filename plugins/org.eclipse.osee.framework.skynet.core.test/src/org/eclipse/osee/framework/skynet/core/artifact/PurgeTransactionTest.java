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
package org.eclipse.osee.framework.skynet.core.artifact;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.NullOperationLogger;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.test.mocks.Asserts;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.operation.PurgeUnusedBackingDataAndTransactions;
import org.eclipse.osee.framework.skynet.core.mocks.DbTestUtil;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.util.FrameworkTestUtil;
import org.eclipse.osee.framework.skynet.core.utility.PurgeTransactionOperationWithListener;
import org.eclipse.osee.support.test.util.DemoSawBuilds;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.BeforeClass;

/**
 * @author Ryan Schmitt
 */
public class PurgeTransactionTest {
   private IOseeBranch branch;
   Collection<Artifact> softArts;
   private SkynetTransaction createTransaction;
   private SkynetTransaction modifyTransaction;
   private int createId;
   private int modifyId;
   private Map<String, Integer> preCreateCount;
   private Map<String, Integer> preModifyCount;
   private Map<String, Integer> postModifyPurgeCount;
   private Map<String, Integer> postCreatePurgeCount;
   private static final List<String> tables = Arrays.asList("osee_attribute", "osee_artifact", "osee_relation_link",
      "osee_tx_details", "osee_txs");

   @BeforeClass
   public static void setUpOnce() throws Exception {
      Operations.executeWorkAndCheckStatus(new PurgeUnusedBackingDataAndTransactions(NullOperationLogger.getSingleton()));
   }

   @org.junit.Test
   public void testPurgeTransaction() throws Exception {
      init();

      createArtifacts();
      int initialTxCurrents = getCurrentRows();

      modifyArtifacts();
      purge(modifyId, postModifyPurgeCount);
      TestUtil.checkThatEqual(preModifyCount, postModifyPurgeCount);

      assertEquals("Purge Transaction did not correctly update tx_current.", initialTxCurrents, getCurrentRows());

      purge(createId, postCreatePurgeCount);
      TestUtil.checkThatEqual(preCreateCount, postCreatePurgeCount);
   }

   private void init() throws Exception {
      branch = DemoSawBuilds.SAW_Bld_2;
      preCreateCount = new HashMap<String, Integer>();
      preModifyCount = new HashMap<String, Integer>();
      postModifyPurgeCount = new HashMap<String, Integer>();
      postCreatePurgeCount = new HashMap<String, Integer>();
   }

   private void createArtifacts() throws Exception {
      DbTestUtil.getTableRowCounts(preCreateCount, tables);
      createTransaction = new SkynetTransaction(branch, "Purge Transaction Test");
      softArts =
         FrameworkTestUtil.createSimpleArtifacts(CoreArtifactTypes.SoftwareRequirement, 10, getClass().getSimpleName(),
            branch);
      for (Artifact softArt : softArts) {
         softArt.persist(createTransaction);
      }
      createId = createTransaction.getTransactionNumber();
      createTransaction.execute();
   }

   private void modifyArtifacts() throws Exception {
      DbTestUtil.getTableRowCounts(preModifyCount, tables);
      modifyTransaction = new SkynetTransaction(branch, "Purge Transaction Test");
      for (Artifact softArt : softArts) {
         softArt.addAttribute(CoreAttributeTypes.StaticId, getClass().getSimpleName());
         softArt.persist(modifyTransaction);
      }
      modifyId = modifyTransaction.getTransactionNumber();
      modifyTransaction.execute();
   }

   private void purge(int transactionId, Map<String, Integer> dbCount) throws Exception {
      IOperation operation = PurgeTransactionOperationWithListener.getPurgeTransactionOperation(transactionId);
      Asserts.testOperation(operation, IStatus.OK);
      Operations.executeWorkAndCheckStatus(new PurgeUnusedBackingDataAndTransactions(NullOperationLogger.getSingleton()));
      DbTestUtil.getTableRowCounts(dbCount, tables);
   }

   private int getCurrentRows() throws OseeCoreException {
      final String query = "select count(*) from osee_txs where transaction_id=? and tx_current=1";
      return ConnectionHandler.runPreparedQueryFetchInt(-1, query, createId);
   }
}
