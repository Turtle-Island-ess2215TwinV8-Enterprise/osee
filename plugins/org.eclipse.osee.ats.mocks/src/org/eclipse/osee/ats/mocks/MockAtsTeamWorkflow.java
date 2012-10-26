package org.eclipse.osee.ats.mocks;

import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;

public class MockAtsTeamWorkflow extends MockWorkItem implements IAtsTeamWorkflow {

   public MockAtsTeamWorkflow(String name) {
      super(name);
   }

   public MockAtsTeamWorkflow(String name, String currentStateName, StateType StateType) {
      super(name);
   }

}
