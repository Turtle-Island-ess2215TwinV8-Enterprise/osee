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
package org.eclipse.osee.client.integration.tests.integration.skynet.core;

import static org.eclipse.osee.client.demo.DemoChoice.OSEE_CLIENT_DEMO;
import java.util.List;
import junit.framework.Assert;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeLogMonitorRule;
import org.eclipse.osee.client.test.framework.TestInfo;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.StorageState;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.DeleteBranchOperation;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEventType;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.skynet.core.httpRequests.PurgeBranchHttpRequestOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class BranchEventTest {

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_CLIENT_DEMO);

   @Rule
   public OseeLogMonitorRule monitorRule = new OseeLogMonitorRule();

   @Rule
   public TestInfo method = new TestInfo();

   private IOseeBranch mainBranch;
   private Branch topLevel;

   private BranchEventListener branchEventListener;

   @Before
   public void setup() {
      String topLevelBranchName = String.format("%s_TOP_LEVEL", method.getQualifiedTestName());
      mainBranch = TokenFactory.createBranch(GUID.create(), topLevelBranchName);

      branchEventListener = new BranchEventListener();
   }

   @After
   public void tearDown() throws OseeCoreException {
      if (topLevel != null) {
         boolean pending = OseeEventManager.getPreferences().isPendRunning();
         try {
            OseeEventManager.getPreferences().setPendRunning(true);
            Operations.executeWorkAndCheckStatus(new PurgeBranchHttpRequestOperation(mainBranch, true));
         } finally {
            OseeEventManager.getPreferences().setPendRunning(pending);
         }
      }
   }

   @Test
   public void testRegistration() throws Exception {
      OseeEventManager.removeAllListeners();
      Assert.assertEquals(0, OseeEventManager.getNumberOfListeners());

      OseeEventManager.addListener(branchEventListener);
      Assert.assertEquals(1, OseeEventManager.getNumberOfListeners());

      OseeEventManager.removeListener(branchEventListener);
      Assert.assertEquals(0, OseeEventManager.getNumberOfListeners());
   }

   /**
    * If all branch tests take longer than 20seconds, fail test, something went wrong.
    */
   @Test(timeout = 20000)
   public void testEvents() throws Exception {
      OseeEventManager.removeAllListeners();
      OseeEventManager.addListener(branchEventListener);

      Assert.assertEquals(1, OseeEventManager.getNumberOfListeners());
      boolean pending = OseeEventManager.getPreferences().isPendRunning();
      try {
         OseeEventManager.getPreferences().setPendRunning(true);

         topLevel = testEvents__topLevelAdded();

         Branch workingBranch = testEvents__workingAdded();
         testEvents__workingRenamed(workingBranch);
         testEvents__typeChange(workingBranch);
         testEvents__stateChange(workingBranch);
         testEvents__deleted(workingBranch);
         testEvents__purged();
         Branch committedBranch = testEvents__committed();
         testEvents__changeArchiveState(committedBranch);

      } finally {
         OseeEventManager.getPreferences().setPendRunning(pending);
         OseeEventManager.removeListener(branchEventListener);
      }
   }

   private Branch testEvents__changeArchiveState(Branch committedBranch) throws Exception {
      branchEventListener.reset();

      Assert.assertNotNull(committedBranch);
      final String guid = committedBranch.getGuid();
      Assert.assertEquals(BranchArchivedState.ARCHIVED, committedBranch.getArchiveState());
      BranchManager.updateBranchArchivedState(null, committedBranch.getId(), committedBranch.getGuid(),
         BranchArchivedState.UNARCHIVED);

      verifyReceivedBranchStatesEvent(branchEventListener.getFirstResults(), BranchEventType.ArchiveStateUpdated, guid);

      Assert.assertEquals(BranchArchivedState.UNARCHIVED, committedBranch.getArchiveState());
      Assert.assertFalse(committedBranch.isEditable());
      return committedBranch;
   }

   private Branch testEvents__committed() throws Exception {
      Branch workingBranch =
         BranchManager.createWorkingBranch(mainBranch, method.getQualifiedTestName() + " - to commit");

      Assert.assertNotNull(workingBranch);

      final String guid = workingBranch.getGuid();
      Assert.assertNotNull(workingBranch);
      Artifact newArt = ArtifactTypeManager.addArtifact(CoreArtifactTypes.GeneralData, workingBranch);
      newArt.persist(getClass().getSimpleName());
      ConflictManagerExternal conflictManager = new ConflictManagerExternal(mainBranch, workingBranch);
      branchEventListener.reset();
      BranchManager.commitBranch(null, conflictManager, true, true);

      verifyReceivedBranchStatesEvent(branchEventListener.getFirstResults(), BranchEventType.Committing, guid);
      verifyReceivedBranchStatesEvent(branchEventListener.getSecondResults(), BranchEventType.Committed, guid);

      Assert.assertEquals(BranchState.COMMITTED, workingBranch.getBranchState());
      Assert.assertFalse(workingBranch.isEditable());
      return workingBranch;
   }

   private Branch testEvents__purged() throws Exception {
      Branch workingBranch =
         BranchManager.createWorkingBranch(mainBranch, method.getQualifiedTestName() + " - to purge");

      Assert.assertNotNull(workingBranch);

      final String guid = workingBranch.getGuid();
      Assert.assertNotNull(workingBranch);

      branchEventListener.reset();
      Operations.executeWorkAndCheckStatus(new PurgeBranchHttpRequestOperation(workingBranch, false));

      verifyReceivedBranchStatesEvent(branchEventListener.getFirstResults(), BranchEventType.Purging, guid);
      verifyReceivedBranchStatesEvent(branchEventListener.getSecondResults(), BranchEventType.Purged, guid);

      Assert.assertEquals(BranchState.PURGED, workingBranch.getBranchState());
      Assert.assertEquals(StorageState.PURGED, workingBranch.getStorageState());
      Assert.assertFalse(workingBranch.isEditable());
      Assert.assertFalse("Branch should not exist", BranchManager.branchExists(guid));
      return workingBranch;
   }

   private Branch testEvents__deleted(Branch workingBranch) throws Exception {
      final String guid = workingBranch.getGuid();
      Assert.assertNotNull(workingBranch);
      Assert.assertNotSame(BranchState.DELETED, workingBranch.getBranchState());

      branchEventListener.reset();
      Operations.executeWorkAndCheckStatus(new DeleteBranchOperation(workingBranch));

      verifyReceivedBranchStatesEvent(branchEventListener.getFirstResults(), BranchEventType.Deleting, guid);
      verifyReceivedBranchStatesEvent(branchEventListener.getSecondResults(), BranchEventType.Deleted, guid);

      Assert.assertEquals(BranchState.DELETED, workingBranch.getBranchState());
      return workingBranch;
   }

   private Branch testEvents__stateChange(Branch workingBranch) throws Exception {
      branchEventListener.reset();

      final String guid = workingBranch.getGuid();
      Assert.assertNotNull(workingBranch);
      Assert.assertEquals(BranchState.CREATED, workingBranch.getBranchState());
      BranchManager.updateBranchState(null, workingBranch.getId(), workingBranch.getGuid(), BranchState.MODIFIED);

      verifyReceivedBranchStatesEvent(branchEventListener.getFirstResults(), BranchEventType.StateUpdated, guid);

      Assert.assertEquals(BranchState.MODIFIED, workingBranch.getBranchState());
      return workingBranch;
   }

   private Branch testEvents__typeChange(Branch workingBranch) throws Exception {
      branchEventListener.reset();
      final String guid = workingBranch.getGuid();
      Assert.assertNotNull(workingBranch);
      Assert.assertEquals(BranchType.WORKING, workingBranch.getBranchType());
      BranchManager.updateBranchType(null, workingBranch.getId(), workingBranch.getGuid(), BranchType.BASELINE);

      verifyReceivedBranchStatesEvent(branchEventListener.getFirstResults(), BranchEventType.TypeUpdated, guid);

      Assert.assertEquals(BranchType.BASELINE, workingBranch.getBranchType());
      return workingBranch;
   }

   private Branch testEvents__workingRenamed(Branch workingBranch) throws Exception {
      branchEventListener.reset();

      final String guid = workingBranch.getGuid();
      Assert.assertNotNull(workingBranch);
      String newName = method.getQualifiedTestName() + " - working renamed";
      workingBranch.setName(newName);
      BranchManager.persist(workingBranch);

      verifyReceivedBranchStatesEvent(branchEventListener.getFirstResults(), BranchEventType.Renamed, guid);

      Assert.assertEquals(newName, workingBranch.getName());
      Assert.assertNotNull(BranchManager.getBranchesByName(newName));
      return workingBranch;
   }

   private Branch testEvents__workingAdded() throws Exception {
      branchEventListener.reset();

      Branch workingBranch =
         BranchManager.createWorkingBranch(mainBranch, method.getQualifiedTestName() + " - working");
      Assert.assertNotNull(workingBranch);

      verifyReceivedBranchStatesEvent(branchEventListener.getFirstResults(), BranchEventType.Added, null);

      return workingBranch;
   }

   private Branch testEvents__topLevelAdded() throws Exception {
      branchEventListener.reset();
      Branch branch = BranchManager.createTopLevelBranch(mainBranch);
      Assert.assertNotNull(branch);

      verifyReceivedBranchStatesEvent(branchEventListener.getFirstResults(), BranchEventType.Added,
         mainBranch.getGuid());

      return branch;
   }

   private void verifyReceivedBranchStatesEvent(Pair<Sender, BranchEvent> eventPair, BranchEventType expectedEnumState, String expectedBranchGuid) {
      Sender receivedSender = eventPair.getFirst();
      BranchEvent receivedBranchEvent = eventPair.getSecond();

      Assert.assertEquals(expectedEnumState, receivedBranchEvent.getEventType());
      if (isRemoteTest()) {
         Assert.assertTrue(receivedSender.isRemote());
      } else {
         Assert.assertTrue(receivedSender.isLocal());
      }

      if (Strings.isValid(expectedBranchGuid)) {
         Assert.assertEquals(expectedBranchGuid, receivedBranchEvent.getBranchGuid());
      }
   }

   protected boolean isRemoteTest() {
      return false;
   }

   private class BranchEventListener implements IBranchEventListener {
      private BranchEvent firstBranchEvent, secondBranchEvent;
      private Sender firstSender, secondSender;
      private boolean receivedFirstUpdate = false, receivedSecondUpdate = false;

      public synchronized void reset() {
         firstBranchEvent = null;
         secondBranchEvent = null;
         receivedFirstUpdate = false;
         receivedSecondUpdate = false;
      }

      @Override
      public List<? extends IEventFilter> getEventFilters() {
         return null;
      }

      @Override
      public synchronized void handleBranchEvent(Sender sender, BranchEvent branchEvent) {
         if (this.firstBranchEvent == null) {
            this.firstBranchEvent = branchEvent;
            this.firstSender = sender;
            receivedFirstUpdate = true;
         } else if (this.secondBranchEvent == null) {
            this.secondBranchEvent = branchEvent;
            this.secondSender = sender;
            receivedSecondUpdate = true;
         }
         notify();
      }

      public synchronized Pair<Sender, BranchEvent> getFirstResults() throws InterruptedException {
         while (!receivedFirstUpdate) {
            wait();
         }
         receivedFirstUpdate = false;
         return new Pair<Sender, BranchEvent>(firstSender, firstBranchEvent);
      }

      public synchronized Pair<Sender, BranchEvent> getSecondResults() throws InterruptedException {
         while (!receivedSecondUpdate) {
            wait();
         }
         receivedSecondUpdate = false;
         return new Pair<Sender, BranchEvent>(secondSender, secondBranchEvent);
      }
   };
}