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

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.core.AtsTestUtil;
import org.eclipse.osee.ats.core.action.ActionManager;
import org.eclipse.osee.ats.core.config.ActionableItemArtifact;
import org.eclipse.osee.ats.core.review.DecisionReviewArtifact;
import org.eclipse.osee.ats.core.review.DecisionReviewManager;
import org.eclipse.osee.ats.core.team.TeamState;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.workdef.ReviewBlockType;
import org.eclipse.osee.ats.core.workflow.ActionableItemManagerCore;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.ats.shared.ChangeType;
import org.eclipse.osee.ats.util.AtsDeleteManager.DeleteOption;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.Named;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.jdk.core.type.CountingMap;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.support.test.util.DemoActionableItems;
import org.eclipse.osee.support.test.util.DemoArtifactTypes;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * This test must be run against a demo database. It tests the case where a team workflow or action is deleted or purged
 * and makes sure that all expected related ats objects are deleted also.
 * 
 * @author Donald G. Dunne
 */
public class AtsDeleteManagerTest {

   private enum TestNames {
      TeamArtDeleteOneWorkflow,
      TeamArtDeleteWithTwoWorkflows,
      TeamArtPurge,
      ActionDelete,
      ActionPurge
   };

   @BeforeClass
   public static void testCleanupPre() throws Exception {
      DemoTestUtil.setUpTest();
      cleanup();
   }

   /**
    * Test method for
    * {@link org.eclipse.osee.ats.util.AtsDeleteManager#handleDeletePurgeAtsObject(java.util.Collection, org.eclipse.osee.ats.util.AtsDeleteManager.DeleteOption[])}
    * .
    */
   @org.junit.Test
   public void testTeamArtDeleteOneWorkflow() throws Exception {
      SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "Delete Manager Test");
      // Create Action
      TeamWorkFlowArtifact teamArt =
         createAction(TestNames.TeamArtDeleteOneWorkflow,
            ActionableItemManagerCore.getActionableItems(Arrays.asList(DemoActionableItems.SAW_Code.getName())),
            transaction);
      transaction.execute();

      // Verify exists
      verifyExists(TestNames.TeamArtDeleteOneWorkflow, 1, 1, 0, 2, 1);

      // Delete
      AtsDeleteManager.handleDeletePurgeAtsObject(Arrays.asList(teamArt), true, DeleteOption.Delete);

      // Verify doesn't exist
      verifyExists(TestNames.TeamArtDeleteOneWorkflow, 0, 0, 0, 0, 0);
   }

   /**
    * Test method for
    * {@link org.eclipse.osee.ats.util.AtsDeleteManager#handleDeletePurgeAtsObject(java.util.Collection, org.eclipse.osee.ats.util.AtsDeleteManager.DeleteOption[])}
    * .
    */
   @org.junit.Test
   public void testTeamArtDeleteWithTwoWorkflows() throws Exception {
      SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "Delete Manager Test");
      // Create Action
      TeamWorkFlowArtifact teamArt =
         createAction(TestNames.TeamArtDeleteWithTwoWorkflows,
            ActionableItemManagerCore.getActionableItems(Arrays.asList(DemoActionableItems.SAW_Code.getName(),
               DemoActionableItems.SAW_Requirements.getName())), transaction);
      transaction.execute();

      // Verify exists
      verifyExists(TestNames.TeamArtDeleteWithTwoWorkflows, 1, 1, 1, 2, 1);

      // Delete
      AtsDeleteManager.handleDeletePurgeAtsObject(Arrays.asList(teamArt), true, DeleteOption.Delete);

      // Verify Action and Req Workflow still exist
      verifyExists(TestNames.TeamArtDeleteWithTwoWorkflows, 1, 0, 1, 0, 0);
   }

   @org.junit.Test
   public void testTeamArtPurge() throws Exception {
      SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "Delete Manager Test");
      // Create Action
      TeamWorkFlowArtifact teamArt =
         createAction(TestNames.TeamArtPurge,
            ActionableItemManagerCore.getActionableItems(Arrays.asList(DemoActionableItems.SAW_Code.getName())),
            transaction);
      transaction.execute();

      // Verify exists
      verifyExists(TestNames.TeamArtPurge, 1, 1, 0, 2, 1);

      // Delete
      AtsDeleteManager.handleDeletePurgeAtsObject(Arrays.asList(teamArt), true, DeleteOption.Purge);

      // Verify doesn't exist
      verifyExists(TestNames.TeamArtPurge, 0, 0, 0, 0, 0);
   }

   @org.junit.Test
   public void testActionDelete() throws Exception {
      SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "Delete Manager Test");
      // Create Action
      TeamWorkFlowArtifact teamArt =
         createAction(TestNames.ActionDelete,
            ActionableItemManagerCore.getActionableItems(Arrays.asList(DemoActionableItems.SAW_Code.getName())),
            transaction);
      transaction.execute();

      // Verify exists
      verifyExists(TestNames.ActionDelete, 1, 1, 0, 2, 1);

      // Delete
      AtsDeleteManager.handleDeletePurgeAtsObject(Arrays.asList(teamArt), true, DeleteOption.Delete);

      // Verify doesn't exist
      verifyExists(TestNames.ActionDelete, 0, 0, 0, 0, 0);
   }

   @org.junit.Test
   public void testActionPurge() throws Exception {
      SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "Delete Manager Test");
      // Create Action
      TeamWorkFlowArtifact teamArt =
         createAction(TestNames.ActionPurge,
            ActionableItemManagerCore.getActionableItems(Arrays.asList(DemoActionableItems.SAW_Code.getName())),
            transaction);
      transaction.execute();

      // Verify exists
      verifyExists(TestNames.ActionPurge, 1, 1, 0, 2, 1);

      // Delete
      AtsDeleteManager.handleDeletePurgeAtsObject(Arrays.asList(teamArt), true, DeleteOption.Purge);

      // Verify doesn't exist
      verifyExists(TestNames.ActionPurge, 0, 0, 0, 0, 0);
   }

   private void verifyExists(TestNames testName, int expectedNumActions, int expectedNumCodeWorkflows, int expectedNumReqWorkflows, int expectedNumTasks, int expectedNumReviews) throws OseeCoreException {
      List<Artifact> artifacts =
         ArtifactQuery.getArtifactListFromName(testName + "%", AtsUtil.getAtsBranch(), EXCLUDE_DELETED);
      CountingMap<IArtifactType> countMap = new CountingMap<IArtifactType>();
      for (Artifact artifact : artifacts) {
         countMap.put(artifact.getArtifactType());
      }
      checkExpectedCount(countMap, AtsArtifactTypes.Action, expectedNumActions);
      checkExpectedCount(countMap, DemoArtifactTypes.DemoCodeTeamWorkflow, expectedNumCodeWorkflows);
      checkExpectedCount(countMap, DemoArtifactTypes.DemoReqTeamWorkflow, expectedNumReqWorkflows);
      checkExpectedCount(countMap, AtsArtifactTypes.Task, expectedNumTasks);
      checkExpectedCount(countMap, AtsArtifactTypes.DecisionReview, expectedNumReviews);
   }

   private <T extends Named> void checkExpectedCount(CountingMap<T> map, T key, int expectedCount) {
      int actualCount = map.get(key);
      String message = String.format("%s expected[%s] actual[%s]", key.getName(), expectedCount, actualCount);
      Assert.assertEquals(message, expectedCount, actualCount);
   }

   private TeamWorkFlowArtifact createAction(TestNames testName, Collection<ActionableItemArtifact> actionableItems, SkynetTransaction transaction) throws OseeCoreException {
      Date createdDate = new Date();
      User createdBy = UserManager.getUser();
      Artifact actionArt =
         ActionManager.createAction(null, testName.name(), "Description", ChangeType.Improvement, "2", false, null,
            actionableItems, createdDate, createdBy, null, transaction);

      TeamWorkFlowArtifact teamArt = null;
      for (TeamWorkFlowArtifact team : ActionManager.getTeams(actionArt)) {
         if (team.getTeamDefinition().getName().contains("Code")) {
            teamArt = team;
         }
      }

      teamArt.createTasks(Arrays.asList(testName.name() + "Task 1", testName.name() + "Task 2"), null, createdDate,
         createdBy, transaction);

      DecisionReviewArtifact decRev =
         DecisionReviewManager.createNewDecisionReview(teamArt, ReviewBlockType.None, testName.name(),
            TeamState.Endorse.getPageName(), "Description", DecisionReviewManager.getDefaultDecisionReviewOptions(),
            Arrays.asList((IBasicUser) UserManager.getUser()), createdDate, createdBy);
      decRev.persist(transaction);

      return teamArt;

   }

   @AfterClass
   public static void testCleanupPost() throws Exception {
      cleanup();
   }

   private static void cleanup() throws Exception {
      List<String> names = new ArrayList<String>();
      for (TestNames testName : TestNames.values()) {
         names.add(testName.name());
      }
      AtsTestUtil.cleanupSimpleTest(names);
   }
}
