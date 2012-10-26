/*
 * Created on Aug 1, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.workitem;

import java.util.Collection;
import java.util.Date;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workflow.ChangeType;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.IAtsWorkData;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.ats.api.workflow.WorkStateFactory;
import org.eclipse.osee.ats.api.workflow.WorkStateProvider;
import org.eclipse.osee.ats.impl.internal.html.WorkItemToOverviewHtml;
import org.eclipse.osee.ats.impl.internal.html.WorkItemToPrintHtml;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;

public class AtsWorkItemServiceImpl implements IAtsWorkItemService {

   private static AtsWorkItemServiceImpl instance = new AtsWorkItemServiceImpl();

   public static AtsWorkItemServiceImpl get() {
      return instance;
   }

   @Override
   public ChangeType getChangeType(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getChangeType(workItem);
   }

   @Override
   public String getChangeTypeStr(IAtsWorkItem workItem) throws OseeCoreException {
      ChangeType changeType = getChangeType(workItem);
      if (changeType == ChangeType.None) {
         return "";
      }
      return changeType.name();
   }

   @Override
   public IAtsTeamWorkflow getParentTeamWorkflow(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getParentTeamWorkflow(workItem);
   }

   @Override
   public String getPriorityStr(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getPriorityStr(workItem);
   }

   @Override
   public String getNeedByDateStr(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getNeedByDateStr(workItem);
   }

   @Override
   public String getTypeName(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getTypeName(workItem);
   }

   @Override
   public String getPcrId(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getPcrId(workItem);
   }

   @Override
   public XResultData getPrintHtml(IAtsWorkItem workItem) throws OseeCoreException {
      return new WorkItemToPrintHtml(workItem).getResultData();
   }

   @Override
   public String getCurrentStateName(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getCurrentStateName(workItem);
   }

   @Override
   public IAtsUser getCreatedBy(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getCreatedBy(workItem);
   }

   @Override
   public Date getCreatedDate(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getCreatedDate(workItem);
   }

   @Override
   public boolean isOfType(IAtsWorkItem workItem, Class<? extends IAtsObject> class1) throws OseeCoreException {
      return AtsWorkItemStoreService.get().isOfType(workItem, class1);
   }

   @Override
   public String getTeamName(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getTeamName(workItem);
   }

   @Override
   public boolean isCancelled(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().isCancelled(workItem);
   }

   @Override
   public String getCancelledFromState(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getCancelledFromState(workItem);
   }

   @Override
   public String getCancelledReason(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getCancelledReason(workItem);
   }

   @Override
   public IAtsWorkItem getParentWorkItem(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getParentWorkItem(workItem);
   }

   @Override
   public String getOverviewHtml(IAtsWorkItem workItem) throws OseeCoreException {
      WorkItemToOverviewHtml overview = new WorkItemToOverviewHtml(workItem);
      return overview.get();
   }

   @Override
   public String getResolution(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getResolution(workItem);
   }

   public boolean isInState(IAtsWorkItem workItem, IAtsStateDefinition stateDef) throws OseeCoreException {
      return AtsWorkItemStoreService.get().isInState(workItem, stateDef);
   }

   public boolean isStateVisited(IAtsWorkItem workItem, IAtsStateDefinition stateDef) throws OseeCoreException {
      return AtsWorkItemStoreService.get().isStateVisited(workItem, stateDef);
   }

   @Override
   public boolean isCompleted(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().isCompleted(workItem);
   }

   @Override
   public IAtsUser getCompletedBy(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getCompletedBy(workItem);
   }

   @Override
   public Date getCompletedDate(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getCompletedDate(workItem);
   }

   @Override
   public IAtsUser getCancelledBy(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getCancelledBy(workItem);
   }

   @Override
   public Date getCancelledDate(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getCancelledDate(workItem);
   }

   @Override
   public boolean isCompletedOrCancelled(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().isCompletedOrCancelled(workItem);
   }

   @Override
   public boolean isInWork(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().isInWork(workItem);
   }

   @Override
   public String getCompletedFromState(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getCompletedFromState(workItem);
   }

   @Override
   public void setCompletedFromState(IAtsWorkItem workItem, String string) throws OseeCoreException {
      AtsWorkItemStoreService.get().setCompletedFromState(workItem, string);
   }

   @Override
   public void setCancelledFromState(IAtsWorkItem workItem, String string) throws OseeCoreException {
      AtsWorkItemStoreService.get().setCancelledFromState(workItem, string);
   }

   @Override
   public void setStateType(IAtsWorkItem workItem, StateType StateType) throws OseeCoreException {
      AtsWorkItemStoreService.get().setStateType(workItem, StateType);
   }

   @Override
   public void setCompletedDate(IAtsWorkItem workItem, Date completedDate) throws OseeCoreException {
      AtsWorkItemStoreService.get().setCompletedDate(workItem, completedDate);
   }

   @Override
   public void setCancelledDate(IAtsWorkItem workItem, Date cancelledDate) throws OseeCoreException {
      AtsWorkItemStoreService.get().setCancelledDate(workItem, cancelledDate);
   }

   @Override
   public void setCompletedBy(IAtsWorkItem workItem, IAtsUser completedBy) throws OseeCoreException {
      AtsWorkItemStoreService.get().setCompletedBy(workItem, completedBy);
   }

   @Override
   public void setCancelledBy(IAtsWorkItem workItem, IAtsUser cancelledBy) throws OseeCoreException {
      AtsWorkItemStoreService.get().setCompletedBy(workItem, cancelledBy);
   }

   @Override
   public WorkStateProvider getStateData(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getStateData(workItem);
   }

   @Override
   public WorkStateFactory getStateFactory(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getStateFactory(workItem);
   }

   @Override
   public void addState(IAtsWorkItem workItem, String stateName) throws OseeCoreException {
      AtsWorkItemStoreService.get().addState(workItem, stateName);
   }

   @Override
   public ChangeType setChangeType(IAtsWorkItem workItem, ChangeType changeType) throws OseeCoreException {
      return AtsWorkItemStoreService.get().setChangeType(workItem, changeType);
   }

   @Override
   public IAtsWorkData getWorkData(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getWorkData(workItem);
   }

   @Override
   public IArtifactType getArtifactType(IAtsWorkItem workItem) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getArtifactType(workItem);
   }

   @Override
   public Collection<Object> getAttributeValues(IAtsWorkItem workItem, IAttributeType attributeType) throws OseeCoreException {
      return AtsWorkItemStoreService.get().getAttributeValues(workItem, attributeType);
   }

   @Override
   public boolean isOfType(IAtsWorkItem item, IArtifactType matchType) throws OseeCoreException {
      return AtsWorkItemStoreService.get().isOfType(item, matchType);
   }

}
