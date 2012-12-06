/*
 * Created on Aug 7, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.mocks;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workflow.ChangeType;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.IAtsWorkData;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.ats.api.workflow.WorkStateFactory;
import org.eclipse.osee.ats.api.workflow.WorkStateProvider;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;

public class MockAtsWorkItemService implements IAtsWorkItemService {

   private final Map<IAtsWorkItem, String> currentStateNames = new HashMap<IAtsWorkItem, String>();
   private final Map<IAtsWorkItem, String> priorityStrs = new HashMap<IAtsWorkItem, String>();
   private final Map<IAtsWorkItem, StateType> stateTypes = new HashMap<IAtsWorkItem, StateType>();
   private final Map<IAtsWorkItem, ChangeType> changeTypes = new HashMap<IAtsWorkItem, ChangeType>();
   private final Map<IAtsWorkItem, IAtsTeamWorkflow> parentTeamWfs = new HashMap<IAtsWorkItem, IAtsTeamWorkflow>();
   private final Map<IAtsWorkItem, String> completedFromStates = new HashMap<IAtsWorkItem, String>();
   private final Map<IAtsWorkItem, Date> completedDates = new HashMap<IAtsWorkItem, Date>();
   private final Map<IAtsWorkItem, IAtsUser> completedBys = new HashMap<IAtsWorkItem, IAtsUser>();
   private final Map<IAtsWorkItem, String> cancelledFromStates = new HashMap<IAtsWorkItem, String>();
   private final Map<IAtsWorkItem, Date> cancelledDates = new HashMap<IAtsWorkItem, Date>();
   private final Map<IAtsWorkItem, IAtsUser> cancelledBys = new HashMap<IAtsWorkItem, IAtsUser>();
   private final Map<IAtsWorkItem, String> cancelledReasons = new HashMap<IAtsWorkItem, String>();
   private final Map<IAtsWorkItem, Date> createdDates = new HashMap<IAtsWorkItem, Date>();
   private final Map<IAtsWorkItem, IAtsUser> createdBys = new HashMap<IAtsWorkItem, IAtsUser>();

   public MockAtsWorkItemService() {
   }

   @Override
   public ChangeType getChangeType(IAtsWorkItem workItem) {
      return changeTypes.get(workItem);
   }

   @Override
   public String getChangeTypeStr(IAtsWorkItem workItem) {
      ChangeType changeType = getChangeType(workItem);
      if (changeType != ChangeType.None) {
         return changeType.name();
      }
      return "";
   }

   @Override
   public IAtsTeamWorkflow getParentTeamWorkflow(IAtsWorkItem workItem) {
      return parentTeamWfs.get(workItem);
   }

   public void setParentTeamWorkflow(IAtsWorkItem workItem, IAtsTeamWorkflow parentTeamWf) {
      parentTeamWfs.put(workItem, parentTeamWf);
   }

   @Override
   public String getPriorityStr(IAtsWorkItem workItem) {
      return priorityStrs.get(workItem);
   }

   public void setPriorityStr(IAtsWorkItem workItem, String priorityStr) {
      priorityStrs.put(workItem, priorityStr);
   }

   @Override
   public String getNeedByDateStr(IAtsWorkItem workItem) {
      return null;
   }

   @Override
   public String getTypeName(IAtsWorkItem workItem) {
      return null;
   }

   @Override
   public String getPcrId(IAtsWorkItem workItem) {
      return null;
   }

   @Override
   public XResultData getPrintHtml(IAtsWorkItem workItem) {
      return null;
   }

   @Override
   public String getOverviewHtml(IAtsWorkItem workItem) {
      return null;
   }

   @Override
   public String getResolution(IAtsWorkItem workItem) {
      return null;
   }

   @Override
   public IAtsWorkItem getParentWorkItem(IAtsWorkItem workItem) {
      return null;
   }

   @Override
   public String getCancelledReason(IAtsWorkItem workItem) {
      return cancelledReasons.get(workItem);
   }

   @Override
   public String getTeamName(IAtsWorkItem workItem) {
      return null;
   }

   @Override
   public boolean isOfType(IAtsWorkItem workItem, Class<? extends IAtsObject> class1) {
      return false;
   }

   @Override
   public String getCurrentStateName(IAtsWorkItem workItem) {
      return currentStateNames.get(workItem);
   }

   @Override
   public boolean isCompleted(IAtsWorkItem workItem) {
      return getStateType(workItem) == StateType.Completed;
   }

   @Override
   public IAtsUser getCompletedBy(IAtsWorkItem workItem) {
      return completedBys.get(workItem);
   }

   @Override
   public void setCompletedBy(IAtsWorkItem workItem, IAtsUser user) {
      completedBys.put(workItem, user);
   }

   @Override
   public Date getCompletedDate(IAtsWorkItem workItem) {
      return completedDates.get(workItem);
   }

   @Override
   public void setCompletedDate(IAtsWorkItem workItem, Date completedDate) {
      completedDates.put(workItem, completedDate);
   }

   @Override
   public boolean isCancelled(IAtsWorkItem workItem) {
      return getStateType(workItem) == StateType.Cancelled;
   }

   @Override
   public IAtsUser getCancelledBy(IAtsWorkItem workItem) {
      return cancelledBys.get(workItem);
   }

   @Override
   public Date getCancelledDate(IAtsWorkItem workItem) {
      return cancelledDates.get(workItem);
   }

   @Override
   public boolean isCompletedOrCancelled(IAtsWorkItem workItem) {
      return isCompleted(workItem) || isCancelled(workItem);
   }

   @Override
   public boolean isInWork(IAtsWorkItem workItem) {
      return getStateType(workItem) == StateType.Working;
   }

   @Override
   public String getCompletedFromState(IAtsWorkItem workItem) {
      return completedFromStates.get(workItem);
   }

   @Override
   public String getCancelledFromState(IAtsWorkItem workItem) {
      return cancelledFromStates.get(workItem);
   }

   @Override
   public void setCompletedFromState(IAtsWorkItem workItem, String stateName) {
      completedFromStates.put(workItem, stateName);
   }

   @Override
   public void setCancelledFromState(IAtsWorkItem workItem, String stateName) {
      cancelledFromStates.put(workItem, stateName);
   }

   @Override
   public void setStateType(IAtsWorkItem workItem, StateType stateType) {
      stateTypes.put(workItem, stateType);
   }

   public StateType getStateType(IAtsWorkItem workItem) {
      return stateTypes.get(workItem);
   }

   @Override
   public void setCancelledDate(IAtsWorkItem workItem, Date cancelledDate) {
      cancelledDates.put(workItem, cancelledDate);
   }

   @Override
   public void setCancelledBy(IAtsWorkItem workItem, IAtsUser cancelledBy) {
      cancelledBys.put(workItem, cancelledBy);
   }

   @Override
   public IAtsUser getCreatedBy(IAtsWorkItem workItem) {
      return createdBys.get(workItem);
   }

   public void setCreatedBy(IAtsWorkItem workItem, IAtsUser user) {
      createdBys.put(workItem, user);
   }

   @Override
   public Date getCreatedDate(IAtsWorkItem workItem) {
      return createdDates.get(workItem);
   }

   @Override
   public WorkStateProvider getStateData(IAtsWorkItem workItem) {
      return null;
   }

   @Override
   public WorkStateFactory getStateFactory(IAtsWorkItem workItem) {
      return null;
   }

   public void setCurrentStateName(IAtsWorkItem workItem, String stateName) {
      currentStateNames.put(workItem, stateName);
   }

   @Override
   public void addState(IAtsWorkItem workItem, String stateName) {
   }

   @Override
   public ChangeType setChangeType(IAtsWorkItem workItem, ChangeType changeType) {
      return changeTypes.put(workItem, changeType);
   }

   @Override
   public IAtsWorkData getWorkData(IAtsWorkItem workItem) throws OseeCoreException {
      return null;
   }

   @Override
   public IArtifactType getArtifactType(IAtsWorkItem workItem) throws OseeCoreException {
      return null;
   }

   @Override
   public boolean isOfType(IAtsWorkItem item, IArtifactType matchType) throws OseeCoreException {
      return false;
   }

   @Override
   public Collection<Object> getAttributeValues(IAtsObject atsObject, IAttributeType attributeType) throws OseeCoreException {
      return null;
   }

}
