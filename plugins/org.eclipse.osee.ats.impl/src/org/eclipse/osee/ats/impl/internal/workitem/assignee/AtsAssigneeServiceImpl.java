/*
 * Created on Aug 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.workitem.assignee;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workflow.assignee.IAtsAssigneeService;
import org.eclipse.osee.ats.impl.internal.action.AtsActionServiceImpl;
import org.eclipse.osee.ats.impl.internal.workitem.AtsWorkItemServiceImpl;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public class AtsAssigneeServiceImpl implements IAtsAssigneeService {

   private static AtsAssigneeServiceImpl instance = new AtsAssigneeServiceImpl();
   private ImplementersColumn impCol;
   private AssigneeColumn assignCol;

   public static AtsAssigneeServiceImpl get() {
      return instance;
   }

   @Override
   public Collection<? extends IAtsUser> getAssignees(IAtsObject atsObject) throws OseeCoreException {
      return AtsAssigneeStoreService.get().getAssignees(atsObject);
   }

   @Override
   public String getAssigneeStr(IAtsObject atsObject) throws OseeCoreException {
      return getAssigneeColum().getAssigneeStr(atsObject);
   }

   @Override
   public List<? extends IAtsUser> getImplementers(IAtsObject atsObject) throws OseeCoreException {
      return getImplementersColum().getImplementers(atsObject);
   }

   @Override
   public List<? extends IAtsUser> getImplementers_fromWorkItem(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsAssigneeStoreService.get().getImplementers_fromWorkItem(workItem);
   }

   @Override
   public String getImplementersStr(IAtsObject atsObject) throws OseeCoreException {
      return getImplementersColum().getImplementersStr(atsObject);
   }

   private ImplementersColumn getImplementersColum() {
      if (impCol == null) {
         impCol =
            new ImplementersColumn(AtsActionServiceImpl.get(), AtsAssigneeServiceImpl.get(),
               AtsWorkItemServiceImpl.get());
      }
      return impCol;
   }

   private AssigneeColumn getAssigneeColum() {
      if (assignCol == null) {
         assignCol = new AssigneeColumn(AtsActionServiceImpl.get(), this, AtsWorkItemServiceImpl.get());
      }
      return assignCol;
   }

   @Override
   public List<IAtsUser> getAssigneesForState(IAtsWorkItem workItem, String stateName) throws OseeCoreException {
      return AtsAssigneeStoreService.get().getAssigneesForState(workItem, stateName);
   }

   @Override
   public void setAssigneesForState(IAtsWorkItem workItem, String stateName, List<? extends IAtsUser> users) throws OseeCoreException {
      AtsAssigneeStoreService.get().setAssigneesForState(workItem, stateName, users);
   }

   @Override
   public void addAssigneeForState(IAtsWorkItem workItem, String stateName, IAtsUser user) throws OseeCoreException {
      AtsAssigneeStoreService.get().addAssigneeForState(workItem, stateName, user);
   }

   @Override
   public void addAssigneesForState(IAtsWorkItem workItem, String stateName, List<? extends IAtsUser> users) throws OseeCoreException {
      AtsAssigneeStoreService.get().addAssigneesForState(workItem, stateName, users);
   }

}
