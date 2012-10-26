/*
 * Created on Feb 27, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.workitem.assignee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.action.IAtsAction;
import org.eclipse.osee.ats.api.action.IAtsActionService;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.AtsLib;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.ats.api.workflow.assignee.IAtsAssigneeService;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * Return current list of assignees sorted if in Working state or string of implementors surrounded by ()
 * 
 * @author Donald G. Dunne
 */
public class AssigneeColumn {

   private final ImplementersStringProvider implementStrProvider;
   private final IAtsActionService actionService;
   private final IAtsAssigneeService assigneeService;
   private final IAtsWorkItemService workItemService;

   public AssigneeColumn(IAtsActionService actionService, IAtsAssigneeService assigneeService, IAtsWorkItemService workItemService) {
      this.actionService = actionService;
      this.assigneeService = assigneeService;
      this.workItemService = workItemService;
      this.implementStrProvider = new ImplementersColumn(actionService, assigneeService, workItemService);
   }

   public String getAssigneeStr(Object object) throws OseeCoreException {
      if (object instanceof IAtsAction) {
         IAtsAction action = (IAtsAction) object;
         // ensure consistent order by using lists
         List<IAtsUser> pocs = new ArrayList<IAtsUser>();
         List<IAtsUser> implementers = new ArrayList<IAtsUser>();
         for (IAtsWorkItem teamWf : actionService.getTeamWorkflows(action)) {
            if (workItemService.isCompletedOrCancelled(teamWf)) {
               for (IAtsUser user : assigneeService.getImplementers_fromWorkItem(teamWf)) {
                  if (!implementers.contains(user)) {
                     implementers.add(user);
                  }
               }
            } else {
               for (IAtsUser user : assigneeService.getAssignees(teamWf)) {
                  if (!pocs.contains(user)) {
                     pocs.add(user);
                  }
               }
            }
         }
         Collections.sort(pocs);
         Collections.sort(implementers);
         return AtsLib.toString("; ", pocs) + (implementers.isEmpty() ? "" : "(" + AtsLib.toString("; ", implementers) + ")");
      } else if (object instanceof IAtsWorkItem) {
         IAtsWorkItem workItem = (IAtsWorkItem) object;
         if (workItemService.isCompletedOrCancelled(workItem)) {
            String implementers = implementStrProvider.getImplementersStr(workItem);
            if (Strings.isValid(implementers)) {
               return "(" + implementers + ")";
            }
         }
         if (object instanceof IAtsWorkItem) {
            return AtsLib.toString("; ", assigneeService.getAssignees((IAtsWorkItem) object));
         }
      }
      return "";
   }
}
