/*
 * Created on Feb 27, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.workitem.assignee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.action.IAtsAction;
import org.eclipse.osee.ats.api.action.IAtsActionService;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.ats.api.workflow.assignee.IAtsAssigneeService;
import org.eclipse.osee.ats.mocks.MockAtsActionService;
import org.eclipse.osee.ats.mocks.MockAtsAssigneeService;
import org.eclipse.osee.ats.mocks.MockAtsTeamWorkflow;
import org.eclipse.osee.ats.mocks.MockAtsUser;
import org.eclipse.osee.ats.mocks.MockAtsWorkItemService;
import org.eclipse.osee.ats.mocks.MockUnAssigned;
import org.eclipse.osee.ats.mocks.MockWorkItem;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @tests ImplementersColumn
 * @author Donald G. Dunne
 */
public class ImplementersColumnTest {

   private final MockAtsUser joe = new MockAtsUser("joe");
   private final MockAtsUser steve = new MockAtsUser("steve");
   private final MockAtsUser alice = new MockAtsUser("alice");
   IAtsActionService actionService = new MockAtsActionService();
   IAtsAssigneeService assigneeService = new MockAtsAssigneeService();
   IAtsWorkItemService workItemService = new MockAtsWorkItemService();

   private ImplementersColumn getImplementersColumn() {
      return new ImplementersColumn(actionService, assigneeService, workItemService);
   }

   @org.junit.Test
   public void testGetImplementersStrFromInWorkWorkflow_null() throws OseeCoreException {
      Assert.assertEquals("", getImplementersColumn().getImplementersStr(null));
   }

   @org.junit.Test
   public void testGetImplementersStrFromInWorkWorkflow_blank() throws OseeCoreException {
      Assert.assertEquals("", getImplementersColumn().getImplementersStr("this"));
   }

   /**
    * Should be blank if in Working state
    */
   @org.junit.Test
   public void testGetImplementersStrFromInWorkWorkflow_workItem() throws OseeCoreException {
      IAtsWorkItem workItem = createWorkItem("this", "InWork", StateType.Working);
      Assert.assertEquals("", getImplementersColumn().getImplementersStr(workItem));
   }

   /**
    * Should be blank if in Working state and Assigned
    */
   @org.junit.Test
   public void testGetImplementersStrFromInWorkWorkflow_blankIfAssigned() throws OseeCoreException {
      IAtsWorkItem workItem = createWorkItem("this", "InWork", StateType.Working);
      assigneeService.setAssigneesForState(workItem, "InWork", Arrays.asList(joe, steve, alice));
      Assert.assertEquals("", getImplementersColumn().getImplementersStr(workItem));
   }

   /**
    * Test no completedBy, no completedFromState and no workItem.getImplementers()
    */
   @org.junit.Test
   public void testGetImplementersStrFromCompletedCancelledWorkflow_blankIfNothingToShow() throws OseeCoreException {
      IAtsWorkItem workItem = createWorkItem("this", "Completed", StateType.Completed);
      Assert.assertEquals("", getImplementersColumn().getImplementersStr(workItem));

      workItem = createWorkItem("this", "Cancelled", StateType.Cancelled);
      Assert.assertEquals("", getImplementersColumn().getImplementersStr(workItem));
   }

   /**
    * Test if CompletedBy set
    */
   @org.junit.Test
   public void testGetImplementersStrFromCompletedCancelledWorkflow_completedBySet() throws OseeCoreException {
      IAtsWorkItem workItem = createWorkItem("this", "Completed", StateType.Completed);
      workItemService.setCompletedBy(workItem, steve);
      Assert.assertEquals("steve", getImplementersColumn().getImplementersStr(workItem));

      workItem = createWorkItem("this", "Cancelled", StateType.Cancelled);
      workItemService.setCancelledBy(workItem, steve);
      Assert.assertEquals("steve", getImplementersColumn().getImplementersStr(workItem));

   }

   /**
    * Test one CompletedBy and assignees from completedFromState
    */
   @org.junit.Test
   public void testGetImplementersStrFromCompletedCancelledWorkflow_completedByAndAssignee() throws OseeCoreException {
      IAtsWorkItem workItem = createWorkItem("this", "Completed", StateType.Completed);
      workItemService.setCompletedBy(workItem, steve);
      workItemService.setCompletedFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneeForState(workItem, "Implement", alice);
      Assert.assertEquals("alice; steve", getImplementersColumn().getImplementersStr(workItem));

      workItem = createWorkItem("this", "Cancelled", StateType.Cancelled);
      workItemService.setCancelledBy(workItem, steve);
      workItemService.setCancelledFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneeForState(workItem, "Implement", alice);
      Assert.assertEquals("alice; steve", getImplementersColumn().getImplementersStr(workItem));
   }

   /**
    * Test one CompletedBy and assignees from completedFromState with unassigned
    */
   @org.junit.Test
   public void testGetImplementersStrFromCompletedCancelledWorkflow_completedByAndAssigneeWithUnassigned() throws OseeCoreException {
      IAtsWorkItem workItem = createWorkItem("this", "Completed", StateType.Completed);
      workItemService.setCompletedBy(workItem, steve);
      workItemService.setCompletedFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneesForState(workItem, "Implement", Arrays.asList(alice, MockUnAssigned.instance));
      Assert.assertEquals("alice; steve", getImplementersColumn().getImplementersStr(workItem));

      workItem = createWorkItem("this", "Cancelled", StateType.Cancelled);
      workItemService.setCancelledBy(workItem, steve);
      workItemService.setCancelledFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneesForState(workItem, "Implement", Arrays.asList(alice, MockUnAssigned.instance));
      Assert.assertEquals("alice; steve", getImplementersColumn().getImplementersStr(workItem));
   }

   /**
    * Test steve as completedBy and completedFrom only registers once in implementersStr
    */
   @org.junit.Test
   public void testGetImplementersStrFromCompletedCancelledWorkflow_duplicatesHandled() throws OseeCoreException {
      IAtsWorkItem workItem = createWorkItem("this", "Completed", StateType.Completed);
      workItemService.setCompletedBy(workItem, steve);
      workItemService.setCompletedFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneeForState(workItem, "Implement", alice);
      Assert.assertEquals("alice; steve", getImplementersColumn().getImplementersStr(workItem));

      workItem = createWorkItem("this", "Cancelled", StateType.Cancelled);
      workItemService.setCancelledBy(workItem, steve);
      workItemService.setCancelledFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneeForState(workItem, "Implement", alice);
      assigneeService.addAssigneeForState(workItem, "Implement", steve);
      Assert.assertEquals("alice; steve", getImplementersColumn().getImplementersStr(workItem));
   }

   /**
    * Test one CompletedBy and assignees from completedFromState and workItem.getImplementers()
    */
   @org.junit.Test
   public void testGetImplementersStrFromCompletedCancelledWorkflow_fromAll() throws OseeCoreException {
      IAtsWorkItem workItem = createWorkItem("this", "Completed", StateType.Completed);
      workItemService.setCompletedBy(workItem, steve);
      workItemService.setCompletedFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneeForState(workItem, "Implement", alice);
      assigneeService.addAssigneeForState(workItem, "Implement", steve);
      ((MockAtsAssigneeService) assigneeService).addImplementer(workItem, joe);
      Assert.assertEquals("alice; joe; steve", getImplementersColumn().getImplementersStr(workItem));

      workItem = createWorkItem("this", "Cancelled", StateType.Cancelled);
      workItemService.setCancelledBy(workItem, steve);
      workItemService.setCancelledFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneeForState(workItem, "Implement", alice);
      assigneeService.addAssigneeForState(workItem, "Implement", steve);
      ((MockAtsAssigneeService) assigneeService).addImplementer(workItem, joe);
      Assert.assertEquals("alice; joe; steve", getImplementersColumn().getImplementersStr(workItem));
   }

   @org.junit.Test
   public void testGetImplementersStrFromCompletedWorkflow_duplicates() throws OseeCoreException {
      IAtsWorkItem workItem = createWorkItem("this", "Completed", StateType.Completed);
      workItemService.setCompletedFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneeForState(workItem, "Implement", alice);
      ((MockAtsAssigneeService) assigneeService).addImplementer(workItem, alice);
      Assert.assertEquals("alice", getImplementersColumn().getImplementersStr(workItem));

      workItem = createWorkItem("this", "Cancelled", StateType.Cancelled);
      workItemService.setCancelledFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneeForState(workItem, "Implement", alice);
      ((MockAtsAssigneeService) assigneeService).addImplementer(workItem, alice);
      Assert.assertEquals("alice", getImplementersColumn().getImplementersStr(workItem));
   }

   @org.junit.Test
   public void testGetImplementers_fromCompletedCancelledBy_noDuplicatesIfInImplementersAndCompletedBy() throws OseeCoreException {
      IAtsWorkItem workItem = createWorkItem("this", "Completed", StateType.Completed);
      workItemService.setCompletedFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneeForState(workItem, "Implement", alice);
      List<IAtsUser> implementers = new ArrayList<IAtsUser>();
      implementers.add(alice);
      workItemService.setCompletedBy(workItem, alice);
      getImplementersColumn().getImplementers_fromCompletedCancelledBy(workItem, implementers);
      Assert.assertEquals(implementers.iterator().next(), alice);

      workItem = createWorkItem("this", "Cancelled", StateType.Cancelled);
      workItemService.setCancelledFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneeForState(workItem, "Implement", alice);
      implementers = new ArrayList<IAtsUser>();
      implementers.add(alice);
      workItemService.setCancelledBy(workItem, alice);
      getImplementersColumn().getImplementers_fromCompletedCancelledBy(workItem, implementers);
      Assert.assertEquals(implementers.iterator().next(), alice);
   }

   @org.junit.Test
   public void testGetImplementers_fromWorkItem_noDuplicates() throws OseeCoreException {
      IAtsWorkItem workItem = createWorkItem("this", "Completed", StateType.Completed);
      List<IAtsUser> implementers = new ArrayList<IAtsUser>();
      implementers.add(alice);
      ((MockAtsAssigneeService) assigneeService).addImplementer(workItem, alice);
      getImplementersColumn().getImplementers_fromWorkItem(workItem, implementers);
      Assert.assertEquals(implementers.iterator().next(), alice);

   }

   @org.junit.Test
   public void testGetImplementersFromActionGroup() throws OseeCoreException {

      IAtsTeamWorkflow workItem1 = createTeamWorkflowItem("this", "Working", StateType.Working);
      workItemService.setCancelledBy(workItem1, alice);
      Assert.assertEquals("", getImplementersColumn().getImplementersStr(workItem1));

      IAtsTeamWorkflow workItem2 = createTeamWorkflowItem("that", "Working", StateType.Working);
      workItemService.setCancelledBy(workItem2, steve);
      Assert.assertEquals("", getImplementersColumn().getImplementersStr(workItem2));

      IAtsAction action = ((MockAtsActionService) actionService).createAction("action");
      ((MockAtsActionService) actionService).addTeamWorkflow(action, workItem1);
      ((MockAtsActionService) actionService).addTeamWorkflow(action, workItem2);
      Assert.assertEquals("", getImplementersColumn().getImplementersStr(action));

      workItemService.setStateType(workItem1, StateType.Cancelled);
      workItemService.setCancelledBy(workItem1, alice);

      workItemService.setStateType(workItem2, StateType.Cancelled);
      workItemService.setCancelledBy(workItem2, steve);

      Assert.assertEquals("alice; steve", getImplementersColumn().getImplementersStr(action));
   }

   @org.junit.Test
   public void testGetImplementersFromActionGroup_noDuplicates() throws OseeCoreException {

      IAtsTeamWorkflow workItem1 = createTeamWorkflowItem("this", "Cancelled", StateType.Cancelled);
      workItemService.setCancelledBy(workItem1, steve);

      IAtsTeamWorkflow workItem2 = createTeamWorkflowItem("that", "Cancelled", StateType.Cancelled);
      workItemService.setCancelledBy(workItem2, steve);

      IAtsAction action = ((MockAtsActionService) actionService).createAction("action");
      ((MockAtsActionService) actionService).addTeamWorkflow(action, workItem1);
      ((MockAtsActionService) actionService).addTeamWorkflow(action, workItem2);

      List<IAtsUser> implementers = getImplementersColumn().getActionGroupImplementers(action);
      Assert.assertEquals(1, implementers.size());
      Assert.assertEquals(steve, implementers.iterator().next());
   }

   public IAtsWorkItem createWorkItem(String name, String currentStateName, StateType stateType) throws OseeCoreException {
      MockWorkItem workItem = new MockWorkItem(name);
      workItemService.addState(workItem, currentStateName);
      workItemService.setStateType(workItem, stateType);
      return workItem;
   }

   public IAtsTeamWorkflow createTeamWorkflowItem(String name, String currentStateName, StateType stateType) throws OseeCoreException {
      MockAtsTeamWorkflow workItem = new MockAtsTeamWorkflow(name);
      workItemService.addState(workItem, currentStateName);
      workItemService.setStateType(workItem, stateType);
      return workItem;
   }

}
