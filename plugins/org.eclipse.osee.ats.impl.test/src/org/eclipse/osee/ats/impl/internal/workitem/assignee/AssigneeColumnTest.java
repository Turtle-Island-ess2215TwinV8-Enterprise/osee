/*
 * Created on Feb 27, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.workitem.assignee;

import java.util.Arrays;
import junit.framework.Assert;
import org.eclipse.osee.ats.api.action.IAtsAction;
import org.eclipse.osee.ats.api.action.IAtsActionService;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.ats.api.workflow.assignee.IAtsAssigneeService;
import org.eclipse.osee.ats.mocks.MockAtsActionService;
import org.eclipse.osee.ats.mocks.MockAtsAssigneeService;
import org.eclipse.osee.ats.mocks.MockAtsTeamWorkflow;
import org.eclipse.osee.ats.mocks.MockAtsUser;
import org.eclipse.osee.ats.mocks.MockAtsWorkItemService;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @tests AssigneeColumn
 * @author Donald G. Dunne
 */
public class AssigneeColumnTest {

   private final MockAtsUser joe = new MockAtsUser("joe");
   private final MockAtsUser steve = new MockAtsUser("steve");
   private final MockAtsUser alice = new MockAtsUser("alice");
   IAtsActionService actionService = new MockAtsActionService();
   IAtsAssigneeService assigneeService = new MockAtsAssigneeService();
   IAtsWorkItemService workItemService = new MockAtsWorkItemService();

   private AssigneeColumn getAssigneeColumn() {
      return new AssigneeColumn(actionService, assigneeService, workItemService);
   }

   @org.junit.Test
   public void testGetAssigneeStr_null() throws OseeCoreException {
      Assert.assertEquals("", getAssigneeColumn().getAssigneeStr(null));
   }

   @org.junit.Test
   public void testGetAssigneeStrFromInWorkWorkflow() throws OseeCoreException {
      IAtsTeamWorkflow workItem = createTeamWorkflowItem("this", "Working", StateType.Working);
      Assert.assertEquals("", getAssigneeColumn().getAssigneeStr(workItem));

      assigneeService.setAssigneesForState(workItem, "Working", Arrays.asList(joe, steve, alice));
      Assert.assertEquals("joe; steve; alice", getAssigneeColumn().getAssigneeStr(workItem));

   }

   @org.junit.Test
   public void testGetAssigneeStrFromCompletedWorkflow() throws OseeCoreException {
      IAtsTeamWorkflow workItem = createTeamWorkflowItem("this", "Working", StateType.Working);
      Assert.assertEquals("", getAssigneeColumn().getAssigneeStr(workItem));

      workItem = createTeamWorkflowItem("this", "Completed", StateType.Completed);
      workItemService.setCompletedBy(workItem, steve);
      Assert.assertEquals("(steve)", getAssigneeColumn().getAssigneeStr(workItem));

      // add alice as completedFromState assignee
      workItemService.setCompletedFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneeForState(workItem, "Implement", alice);
      Assert.assertEquals("(alice; steve)", getAssigneeColumn().getAssigneeStr(workItem));

      // add duplicate steve as state assigne and ensure doesn't duplicate in string
      assigneeService.addAssigneeForState(workItem, "Implement", steve);
      Assert.assertEquals("(alice; steve)", getAssigneeColumn().getAssigneeStr(workItem));

   }

   @org.junit.Test
   public void testGetAssigneeStrFromCancelledWorkflow() throws OseeCoreException {
      IAtsTeamWorkflow workItem = createTeamWorkflowItem("this", "Working", StateType.Working);
      Assert.assertEquals("", getAssigneeColumn().getAssigneeStr(workItem));

      workItem = createTeamWorkflowItem("this", "Cancelled", StateType.Cancelled);
      workItemService.setCancelledBy(workItem, steve);
      Assert.assertEquals("(steve)", getAssigneeColumn().getAssigneeStr(workItem));

      // add alice as completedFromState assignee
      workItemService.setCancelledFromState(workItem, "Implement");
      workItemService.addState(workItem, "Implement");
      assigneeService.addAssigneeForState(workItem, "Implement", alice);
      Assert.assertEquals("(alice; steve)", getAssigneeColumn().getAssigneeStr(workItem));

      // add duplicate steve as state assigne and ensure doesn't duplicate in string
      assigneeService.addAssigneeForState(workItem, "Implement", steve);
      Assert.assertEquals("(alice; steve)", getAssigneeColumn().getAssigneeStr(workItem));
   }

   @org.junit.Test
   public void testGetAssigneesStr_hasActions_oneWorkingOneCancelled() throws OseeCoreException {

      IAtsTeamWorkflow workItem1 = createTeamWorkflowItem("this", "Working", StateType.Working);

      IAtsTeamWorkflow workItem2 = createTeamWorkflowItem("that", "Cancelled", StateType.Cancelled);
      ((MockAtsAssigneeService) assigneeService).addImplementer(workItem2, joe);

      IAtsAction action = ((MockAtsActionService) actionService).createAction("action");
      ((MockAtsActionService) actionService).addTeamWorkflow(action, workItem1);
      ((MockAtsActionService) actionService).addTeamWorkflow(action, workItem2);

      Assert.assertEquals("(joe)", getAssigneeColumn().getAssigneeStr(action));
   }

   @org.junit.Test
   public void testGetAssigneesStr_hasActions_duplicateImplementers() throws OseeCoreException {

      IAtsTeamWorkflow workItem1 = createTeamWorkflowItem("this", "Cancelled", StateType.Cancelled);
      ((MockAtsAssigneeService) assigneeService).addImplementer(workItem1, joe);

      IAtsTeamWorkflow workItem2 = createTeamWorkflowItem("that", "Cancelled", StateType.Cancelled);
      ((MockAtsAssigneeService) assigneeService).addImplementer(workItem1, joe);

      IAtsAction action = ((MockAtsActionService) actionService).createAction("action");
      ((MockAtsActionService) actionService).addTeamWorkflow(action, workItem1);
      ((MockAtsActionService) actionService).addTeamWorkflow(action, workItem2);

      Assert.assertEquals("(joe)", getAssigneeColumn().getAssigneeStr(action));
   }

   @org.junit.Test
   public void testGetAssigneesStr_hasActions_twoCancelled() throws OseeCoreException {

      IAtsTeamWorkflow workItem1 = createTeamWorkflowItem("this", "Cancelled", StateType.Cancelled);
      ((MockAtsAssigneeService) assigneeService).addImplementer(workItem1, steve);

      IAtsTeamWorkflow workItem2 = createTeamWorkflowItem("that", "Cancelled", StateType.Cancelled);
      ((MockAtsAssigneeService) assigneeService).addImplementer(workItem1, joe);

      IAtsAction action = ((MockAtsActionService) actionService).createAction("action");
      ((MockAtsActionService) actionService).addTeamWorkflow(action, workItem1);
      ((MockAtsActionService) actionService).addTeamWorkflow(action, workItem2);

      Assert.assertEquals("(joe; steve)", getAssigneeColumn().getAssigneeStr(action));
   }

   @org.junit.Test
   public void testGetAssigneesStr_hasActions_twoWorkingDuplicates() throws OseeCoreException {

      IAtsTeamWorkflow workItem1 = createTeamWorkflowItem("this", "Working", StateType.Working);
      ((MockAtsWorkItemService) workItemService).setCurrentStateName(workItem1, "Implement");
      workItemService.addState(workItem1, "Implement");
      assigneeService.addAssigneeForState(workItem1, "Implement", steve);

      IAtsTeamWorkflow workItem2 = createTeamWorkflowItem("that", "Working", StateType.Working);
      ((MockAtsWorkItemService) workItemService).setCurrentStateName(workItem2, "Implement");
      workItemService.addState(workItem2, "Implement");
      assigneeService.addAssigneeForState(workItem2, "Implement", steve);

      IAtsAction action = ((MockAtsActionService) actionService).createAction("action");
      ((MockAtsActionService) actionService).addTeamWorkflow(action, workItem1);
      ((MockAtsActionService) actionService).addTeamWorkflow(action, workItem2);

      Assert.assertEquals("steve", getAssigneeColumn().getAssigneeStr(action));
   }

   public IAtsTeamWorkflow createTeamWorkflowItem(String name, String currentStateName, StateType stateType) throws OseeCoreException {
      MockAtsTeamWorkflow workItem = new MockAtsTeamWorkflow(name);
      workItemService.addState(workItem, currentStateName);
      workItemService.setStateType(workItem, stateType);
      return workItem;
   }

}
