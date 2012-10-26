/*
 * Created on Aug 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workflow.assignee.IAtsAssigneeService;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.type.CompositeKeyHashMap;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;

public class MockAtsAssigneeService implements IAtsAssigneeService {

   Map<IAtsObject, List<IAtsUser>> implementers = new HashMap<IAtsObject, List<IAtsUser>>();
   String currentStateName = null;
   CompositeKeyHashMap<IAtsObject, String, List<IAtsUser>> implementersByStateName =
      new CompositeKeyHashMap<IAtsObject, String, List<IAtsUser>>();
   CompositeKeyHashMap<IAtsObject, String, List<IAtsUser>> assigneesByStateName =
      new CompositeKeyHashMap<IAtsObject, String, List<IAtsUser>>();

   @Override
   public Collection<? extends IAtsUser> getAssignees(IAtsObject atsObject) {
      if (atsObject instanceof IAtsWorkItem && Strings.isValid(currentStateName)) {
         IAtsWorkItem workItem = (IAtsWorkItem) atsObject;
         return getAssigneesForState(workItem, currentStateName);
      }
      return java.util.Collections.emptyList();
   }

   @Override
   public String getAssigneeStr(IAtsObject atsObject) {
      return Collections.toString("; ", getAssignees(atsObject));
   }

   @Override
   public List<? extends IAtsUser> getImplementers(IAtsObject atsObject) {
      List<? extends IAtsUser> results = implementers.get(atsObject);
      if (results != null) {
         return results;
      }
      return java.util.Collections.emptyList();
   }

   @Override
   public String getImplementersStr(IAtsObject atsObject) {
      return Collections.toString("; ", getImplementers(atsObject));
   }

   public void addImplementer(IAtsObject atsObject, IAtsUser atsUser) {
      List<IAtsUser> users = implementers.get(atsObject);
      if (users == null) {
         users = new ArrayList<IAtsUser>();
         implementers.put(atsObject, users);
      }
      users.add(atsUser);
   }

   @Override
   public List<IAtsUser> getAssigneesForState(IAtsWorkItem workItem, String stateName) {
      List<IAtsUser> assignees = assigneesByStateName.get(workItem, stateName);
      if (assignees != null) {
         return assignees;
      }
      return java.util.Collections.emptyList();
   }

   @Override
   public void setAssigneesForState(IAtsWorkItem workItem, String stateName, List<? extends IAtsUser> users) {
      List<IAtsUser> assignees = assigneesByStateName.get(workItem, stateName);
      if (assignees == null) {
         assignees = new ArrayList<IAtsUser>();
         assigneesByStateName.put(workItem, stateName, assignees);
      }
      assignees.clear();
      assignees.addAll(users);
      currentStateName = stateName;
   }

   @Override
   public void addAssigneeForState(IAtsWorkItem workItem, String stateName, IAtsUser user) {
      List<IAtsUser> assignees = assigneesByStateName.get(workItem, stateName);
      if (assignees == null) {
         assignees = new ArrayList<IAtsUser>();
         assigneesByStateName.put(workItem, stateName, assignees);
      }
      assignees.add(user);
      currentStateName = stateName;
   }

   @Override
   public void addAssigneesForState(IAtsWorkItem workItem, String stateName, List<? extends IAtsUser> users) {
      List<IAtsUser> assignees = assigneesByStateName.get(workItem, stateName);
      if (assignees == null) {
         assignees = new ArrayList<IAtsUser>();
         assigneesByStateName.put(workItem, stateName, assignees);
      }
      assignees.addAll(users);
      currentStateName = stateName;
   }

   public String getCurrentStateName() {
      return currentStateName;
   }

   public void setCurrentStateName(String currentStateName) {
      this.currentStateName = currentStateName;
   }

   @Override
   public List<? extends IAtsUser> getImplementers_fromWorkItem(IAtsWorkItem workItem) {
      List<IAtsUser> assignees = implementers.get(workItem);
      if (Conditions.allNull(assignees)) {
         return java.util.Collections.emptyList();
      }
      return assignees;
   }
}
