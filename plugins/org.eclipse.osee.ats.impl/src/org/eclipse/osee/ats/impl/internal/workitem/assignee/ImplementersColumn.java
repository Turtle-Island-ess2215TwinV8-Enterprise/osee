/*
 * Created on Feb 27, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.workitem.assignee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.action.IAtsAction;
import org.eclipse.osee.ats.api.action.IAtsActionService;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.AtsLib;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.ats.api.workflow.assignee.IAtsAssigneeService;
import org.eclipse.osee.ats.impl.internal.user.AtsUserServiceImpl;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * Implementers for a WorkItem are<br/>
 * <br/>
 * For In Work Item: blank<br/>
 * <br/>
 * For Completed or Cancelled: <br/>
 * 1) Assignees of CompletedFrom or CancelledFrom states <br/>
 * 2) CompletedBy or CancelledBy user of WorkItem <br/>
 * 3) Users identified by object's getImplementers() call, if any <br/>
 * <br/>
 * For ActionGroup, it's the set of users for each case above for each Action
 * 
 * @author Donald G. Dunne
 */
public class ImplementersColumn implements ImplementersStringProvider {

   private final IAtsActionService actionService;
   private final IAtsAssigneeService assigneeService;
   private final IAtsWorkItemService workItemService;

   public ImplementersColumn(IAtsActionService actionService, IAtsAssigneeService assigneeService, IAtsWorkItemService workItemService) {
      this.actionService = actionService;
      this.assigneeService = assigneeService;
      this.workItemService = workItemService;
   }

   @Override
   public String getImplementersStr(Object object) throws OseeCoreException {
      List<IAtsUser> implementers = getImplementers(object);
      return implementers.isEmpty() ? "" : AtsLib.toString("; ", implementers);
   }

   public List<IAtsUser> getImplementers(Object object) throws OseeCoreException {
      List<IAtsUser> implementers = new LinkedList<IAtsUser>();
      if (object instanceof IAtsAction) {
         implementers.addAll(getActionGroupImplementers((IAtsAction) object));
      } else if (object instanceof IAtsWorkItem) {
         implementers.addAll(getWorkItemImplementers((IAtsWorkItem) object));
      }
      implementers.remove(AtsUserServiceImpl.get().getUnAssigned());
      Collections.sort(implementers);
      return implementers;
   }

   public List<IAtsUser> getWorkItemImplementers(IAtsWorkItem workItem) throws OseeCoreException {
      List<IAtsUser> implementers = new ArrayList<IAtsUser>();
      getImplementers_fromWorkItem(workItem, implementers);
      getImplementers_fromCompletedCancelledBy(workItem, implementers);
      getImplementers_fromCompletedCancelledFrom(workItem, implementers);
      return implementers;
   }

   public void getImplementers_fromCompletedCancelledFrom(IAtsWorkItem workItem, List<IAtsUser> implementers) throws OseeCoreException {
      String fromStateName = null;
      if (workItemService.isCompleted(workItem)) {
         fromStateName = workItemService.getCompletedFromState(workItem);
      } else if (workItemService.isCancelled(workItem)) {
         fromStateName = workItemService.getCancelledFromState(workItem);
      }
      if (Strings.isValid(fromStateName)) {
         for (IAtsUser user : assigneeService.getAssigneesForState(workItem, fromStateName)) {
            if (!implementers.contains(user)) {
               implementers.add(user);
            }
         }
      }
   }

   public void getImplementers_fromCompletedCancelledBy(IAtsWorkItem workItem, List<IAtsUser> implementers) throws OseeCoreException {
      if (workItemService.isCompletedOrCancelled(workItem)) {
         if (workItemService.isCompleted(workItem)) {
            IAtsUser completedBy = workItemService.getCompletedBy(workItem);
            if (completedBy != null && !implementers.contains(completedBy)) {
               implementers.add(completedBy);
            }
         }
         if (workItemService.isCancelled(workItem)) {
            IAtsUser cancelledBy = workItemService.getCancelledBy(workItem);
            if (cancelledBy != null && !implementers.contains(cancelledBy)) {
               implementers.add(cancelledBy);
            }
         }
      }
   }

   public void getImplementers_fromWorkItem(IAtsWorkItem workItem, List<IAtsUser> implementers) throws OseeCoreException {
      for (IAtsUser user : assigneeService.getImplementers_fromWorkItem(workItem)) {
         if (!implementers.contains(user)) {
            implementers.add(user);
         }
      }
   }

   public List<IAtsUser> getActionGroupImplementers(IAtsAction action) throws OseeCoreException {
      List<IAtsUser> implementers = new LinkedList<IAtsUser>();
      for (IAtsWorkItem team : actionService.getTeamWorkflows(action)) {
         if (workItemService.isCompletedOrCancelled(team)) {
            for (IAtsUser user : getWorkItemImplementers(team)) {
               if (!implementers.contains(user)) {
                  implementers.add(user);
               }
            }
         }
      }
      return implementers;
   }
}
