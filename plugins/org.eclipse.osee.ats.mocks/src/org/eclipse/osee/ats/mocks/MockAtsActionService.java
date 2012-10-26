/*
 * Created on Feb 28, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.mocks;

import java.util.Collection;
import java.util.Collections;
import org.eclipse.osee.ats.api.action.IAtsAction;
import org.eclipse.osee.ats.api.action.IAtsActionService;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;

/**
 * Simple Action Service
 * 
 * @author Donald G. Dunne
 */
public class MockAtsActionService implements IAtsActionService {
   HashCollection<IAtsAction, IAtsTeamWorkflow> actiontoTeamWfs = new HashCollection<IAtsAction, IAtsTeamWorkflow>();

   public MockAtsActionService() {
   }

   public IAtsAction createAction(String name) {
      return new MockAtsAction(name);
   }

   public void addTeamWorkflow(IAtsAction action, IAtsTeamWorkflow teamWf) {
      actiontoTeamWfs.put(action, teamWf);
   }

   @Override
   public Collection<IAtsTeamWorkflow> getTeamWorkflows(IAtsAction action) {
      Collection<IAtsTeamWorkflow> values = actiontoTeamWfs.getValues(action);
      if (values != null) {
         return values;
      }
      return Collections.emptyList();
   }

   @Override
   public IAtsTeamWorkflow getFirstTeamWorkflow(IAtsAction action) {
      Collection<IAtsTeamWorkflow> teamWorkflows = getTeamWorkflows(action);
      if (teamWorkflows.isEmpty()) {
         return null;
      }
      return teamWorkflows.iterator().next();
   }

}
