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
package org.eclipse.osee.ats.util;

import static org.junit.Assert.assertFalse;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.osee.ats.core.action.ActionManager;
import org.eclipse.osee.ats.core.config.ActionableItemArtifact;
import org.eclipse.osee.ats.core.task.TaskArtifact;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.ats.shared.ChangeType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.NullOperationLogger;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.database.operation.PurgeUnusedBackingDataAndTransactions;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.PurgeArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.mocks.DbTestUtil;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.Before;

/**
 * This test is intended to be run against a demo database. It tests the purge logic by counting the rows of the version
 * and txs tables, then adds an Action, Workflow and 30 Tasks, deletes these objects and compares the row count. If
 * purge works properly, all rows should be equal.
 * 
 * @author Donald G. Dunne
 */
public class AtsPurgeTest {

   private final Map<String, Integer> preCreateActionCount = new HashMap<String, Integer>();
   private final Map<String, Integer> postCreateActionCount = new HashMap<String, Integer>();
   private final Map<String, Integer> postPurgeCount = new HashMap<String, Integer>();
   List<String> tables = Arrays.asList("osee_attribute", "osee_artifact", "osee_relation_link", "osee_tx_details",
      "osee_txs");

   @Before
   public void setUp() throws Exception {
      // This test should only be run on test db
      assertFalse(AtsUtil.isProductionDb());
   }

   @org.junit.Test
   public void testPurgeArtifacts() throws Exception {
      // Count rows in tables prior to purge
      txPrune();
      DbTestUtil.getTableRowCounts(preCreateActionCount, tables);

      Set<Artifact> artsToPurge = new HashSet<Artifact>();

      // Create Action, Workflow and Tasks
      SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "Purge Test");
      Artifact actionArt =
         ActionManager.createAction(
            null,
            getClass().getSimpleName(),
            "description",
            ChangeType.Improvement,
            "2",
            false,
            null,
            org.eclipse.osee.framework.jdk.core.util.Collections.castAll(
               ActionableItemArtifact.class,
               ArtifactQuery.getArtifactListFromTypeAndName(AtsArtifactTypes.ActionableItem, "SAW Test",
                  AtsUtil.getAtsBranch())), new Date(), UserManager.getUser(), null, transaction);
      actionArt.persist(transaction);
      transaction.execute();

      artsToPurge.add(actionArt);
      artsToPurge.addAll(ActionManager.getTeams(actionArt));

      for (int x = 0; x < 30; x++) {
         TaskArtifact taskArt =
            ActionManager.getFirstTeam(actionArt).createNewTask(getClass().getSimpleName() + x, new Date(),
               UserManager.getUser());
         taskArt.persist(getClass().getSimpleName());
         artsToPurge.add(taskArt);
      }

      // Count rows and check that increased
      DbTestUtil.getTableRowCounts(postCreateActionCount, tables);
      TestUtil.checkThatIncreased(preCreateActionCount, postCreateActionCount);

      // Purge Action, Workflow and Tasks
      Operations.executeWorkAndCheckStatus(new PurgeArtifacts(artsToPurge));

      // Count rows and check that same as when began
      txPrune();
      DbTestUtil.getTableRowCounts(postPurgeCount, tables);
      TestUtil.checkThatEqual(preCreateActionCount, postPurgeCount);
   }

   private void txPrune() throws OseeCoreException {
      Operations.executeWorkAndCheckStatus(new PurgeUnusedBackingDataAndTransactions(NullOperationLogger.getSingleton()));
   }

}
