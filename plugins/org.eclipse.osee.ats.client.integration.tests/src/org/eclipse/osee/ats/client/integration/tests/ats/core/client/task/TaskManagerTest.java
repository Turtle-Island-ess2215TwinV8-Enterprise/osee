/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.task;

import java.util.Arrays;
import junit.framework.Assert;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.task.TaskManager;
import org.eclipse.osee.ats.core.client.task.TaskStates;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;

import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.HoursSpentUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test unit for {@link TaskManager}
 * 
 * @author Donald G. Dunne
 */
public class TaskManagerTest extends TaskManager {

   @BeforeClass
   @AfterClass
   public static void cleanup() throws OseeCoreException {
      AtsTestUtil.cleanup();
   }

   /**
    * Test can move task between teamWfs that have same Task WorkDefinition
    */
   @org.junit.Test
   public void testMoveTasks_sameWorkDefinitions() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("testMoveTasks - sameWorkDefs");
      TaskArtifact taskToMove = AtsTestUtil.getOrCreateTaskOffTeamWf1();
      TeamWorkFlowArtifact teamWf2 = AtsTestUtil.getTeamWf2();

      IAtsWorkDefinition taskWorkDef =
         AtsClientService.get().getWorkDefinitionAdmin().getWorkDefinitionForTask(taskToMove).getWorkDefinition();
      IAtsWorkDefinition newTaskWorkDef =
         AtsClientService.get().getWorkDefinitionAdmin().getWorkDefinitionForTaskNotYetCreated(teamWf2).getWorkDefinition();
      Assert.assertNotNull(taskWorkDef);

      Assert.assertEquals(taskWorkDef, newTaskWorkDef);
      Result result = TaskManager.moveTasks(teamWf2, Arrays.asList(taskToMove));

      Assert.assertTrue("This failed: " + result.getText(), result.isTrue());
   }

   /**
    * Test can't move task between teamWfs with differing Task WorkDefinitions
    */
   @org.junit.Test
   public void testMoveTasks_differentWorkDefinitions() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("testMoveTasks - diffWorkDefs");
      TaskArtifact taskToMove = AtsTestUtil.getOrCreateTaskOffTeamWf1();
      TeamWorkFlowArtifact teamWf2 = AtsTestUtil.getTeamWf2();
      IAtsWorkDefinition taskWorkDef =
         AtsClientService.get().getWorkDefinitionAdmin().getWorkDefinitionForTask(taskToMove).getWorkDefinition();

      // create new task work def
      XResultData resultData = new XResultData();
      AtsClientService.get().getWorkDefinitionAdmin();
      IAtsWorkDefinition differentTaskWorkDef =
         AtsClientService.get().getWorkDefinitionAdmin().copyWorkDefinition(taskWorkDef.getName() + "2",
            taskWorkDef, resultData);
      Assert.assertFalse("Should be no errors", resultData.isErrors());
      AtsClientService.get().getWorkDefinitionAdmin().addWorkDefinition(differentTaskWorkDef);

      // set teamWf2 to use that work def for tasks
      IAtsTeamDefinition teamDef = teamWf2.getTeamDefinition();
      teamDef.setRelatedTaskWorkDefinition(differentTaskWorkDef.getName());

      IAtsWorkDefinition newTaskWorkDef =
         AtsClientService.get().getWorkDefinitionAdmin().getWorkDefinitionForTaskNotYetCreated(teamWf2).getWorkDefinition();
      Assert.assertNotNull(taskWorkDef);
      Assert.assertNotSame("Should be different", taskWorkDef, newTaskWorkDef);
      Result result = TaskManager.moveTasks(teamWf2, Arrays.asList(taskToMove));

      Assert.assertTrue("This should failed: " + result.getText(), result.isFalse());
   }

   /**
    * Test can move task to teamWf if it defines/overrides i t's own WorkDefinition; eg. WorkDef specified in Task
    * attribute
    */
   @org.junit.Test
   public void testMoveTasks_diffWorkDefinitionsButTaskOverride() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("testMoveTasks - diffWorkDefinitionsButTaskOverride");
      TaskArtifact taskToMove = AtsTestUtil.getOrCreateTaskOffTeamWf1();
      TeamWorkFlowArtifact teamWf2 = AtsTestUtil.getTeamWf2();
      IAtsWorkDefinition taskWorkDef =
         AtsClientService.get().getWorkDefinitionAdmin().getWorkDefinitionForTask(taskToMove).getWorkDefinition();

      // create new task work def
      XResultData resultData = new XResultData();
      AtsClientService.get().getWorkDefinitionAdmin();
      IAtsWorkDefinition differentTaskWorkDef =
         AtsClientService.get().getWorkDefinitionAdmin().copyWorkDefinition(taskWorkDef.getName() + "2",
            taskWorkDef, resultData);
      Assert.assertFalse("Should be no errors", resultData.isErrors());
      AtsClientService.get().getWorkDefinitionAdmin().addWorkDefinition(differentTaskWorkDef);

      // set work definition override on task; move should go through
      taskToMove.setSoleAttributeValue(AtsAttributeTypes.WorkflowDefinition, differentTaskWorkDef.getName());
      taskToMove.persist("testMoveTasks - set workDef attribute on task");

      IAtsWorkDefinition newTaskWorkDef =
         AtsClientService.get().getWorkDefinitionAdmin().getWorkDefinitionForTaskNotYetCreated(teamWf2).getWorkDefinition();
      Assert.assertNotNull(taskWorkDef);
      Assert.assertSame("Should be same", taskWorkDef, newTaskWorkDef);
      Result result = TaskManager.moveTasks(teamWf2, Arrays.asList(taskToMove));

      Assert.assertTrue("This should pass: " + result.getText(), result.isTrue());
   }

   @org.junit.Test
   public void testTransitionToCompletedThenInWork() throws OseeCoreException {

      AtsTestUtil.cleanupAndReset("TaskManagerTest - TransitionToCompleted");

      TaskArtifact taskArt = AtsTestUtil.getOrCreateTaskOffTeamWf1();

      // ensure nothing dirty
      AtsTestUtil.validateArtifactCache();

      // transition to Completed
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtilCore.getAtsBranch(),
            getClass().getSimpleName() + " testTransitionToCompletedThenInWork() 1");
      Result result = TaskManager.transitionToCompleted(taskArt, 0.0, 3, transaction);
      Assert.assertEquals(Result.TrueResult, result);
      transaction.execute();

      Assert.assertEquals(TaskStates.Completed.getName(), taskArt.getCurrentStateName());
      Assert.assertEquals(3.0, HoursSpentUtil.getHoursSpentTotal(taskArt));
      Assert.assertEquals("", taskArt.getStateMgr().getAssigneesStr());

      // ensure nothing dirty
      AtsTestUtil.validateArtifactCache();

      // transition back to InWork
      transaction =
         TransactionManager.createTransaction(AtsUtilCore.getAtsBranch(),
            getClass().getSimpleName() + " testTransitionToCompletedThenInWork() 2");
      result = TaskManager.transitionToInWork(taskArt, AtsClientService.get().getUserAdmin().getCurrentUser(), 45, .5, transaction);
      Assert.assertEquals(Result.TrueResult, result);
      transaction.execute();
      Assert.assertEquals(TaskStates.InWork.getName(), taskArt.getCurrentStateName());
      Assert.assertEquals(3.5, HoursSpentUtil.getHoursSpentTotal(taskArt));
      Assert.assertEquals("Joe Smith", taskArt.getStateMgr().getAssigneesStr());
   }

}
