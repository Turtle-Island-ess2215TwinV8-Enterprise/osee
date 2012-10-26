/*
 * Created on Jul 25, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.workflow;

import java.util.Date;
import org.eclipse.osee.ats.api.IAtsObject;
import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;

public interface IAtsWorkItemService {

   public abstract IAtsWorkData getWorkData(IAtsWorkItem workItem) throws OseeCoreException;
   // Change Type
   public abstract ChangeType getChangeType(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract IArtifactType getArtifactType(IAtsWorkItem workItem) throws OseeCoreException;
   public abstract ChangeType setChangeType(IAtsWorkItem workItem, ChangeType changeType) throws OseeCoreException;

   public abstract Collection<Object> getAttributeValues(IAtsWorkItem workItem, IAttributeType attributeType) throws OseeCoreException;
   public abstract String getChangeTypeStr(IAtsWorkItem workItem) throws OseeCoreException;

   // Parent 
   public abstract IAtsTeamWorkflow getParentTeamWorkflow(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract IAtsWorkItem getParentWorkItem(IAtsWorkItem workItem) throws OseeCoreException;

   // Misc
   public abstract String getPriorityStr(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract String getNeedByDateStr(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract String getTypeName(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract String getPcrId(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract String getResolution(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract String getTeamName(IAtsWorkItem workItem) throws OseeCoreException;

   // HTML Report
   public abstract XResultData getPrintHtml(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract String getOverviewHtml(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract boolean isOfType(IAtsWorkItem workItem, Class<? extends IAtsObject> class1) throws OseeCoreException;

   // State
   public abstract void setStateType(IAtsWorkItem workItem, StateType StateType) throws OseeCoreException;

   public abstract String getCurrentStateName(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract boolean isCompleted(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract IAtsUser getCompletedBy(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract Date getCompletedDate(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract boolean isCancelled(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract IAtsUser getCancelledBy(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract Date getCancelledDate(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract String getCancelledReason(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract boolean isCompletedOrCancelled(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract boolean isInWork(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract String getCompletedFromState(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract String getCancelledFromState(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract void setCompletedFromState(IAtsWorkItem workItem, String string) throws OseeCoreException;

   public abstract void setCancelledFromState(IAtsWorkItem workItem, String string) throws OseeCoreException;

   public abstract void setCompletedDate(IAtsWorkItem workItem, Date completedDate) throws OseeCoreException;

   public abstract void setCancelledDate(IAtsWorkItem workItem, Date cancelledDate) throws OseeCoreException;

   public abstract void setCompletedBy(IAtsWorkItem workItem, IAtsUser completedBy) throws OseeCoreException;

   public abstract void setCancelledBy(IAtsWorkItem workItem, IAtsUser cancelledBy) throws OseeCoreException;

   public abstract IAtsUser getCreatedBy(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract Date getCreatedDate(IAtsWorkItem workItem) throws OseeCoreException;

   // TODO Replace with methods in WorkItemService or AssigneeService or MetricsService
   public abstract WorkStateProvider getStateData(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract WorkStateFactory getStateFactory(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract void addState(IAtsWorkItem workItem, String stateName) throws OseeCoreException;

   public abstract boolean isOfType(IAtsWorkItem item, IArtifactType matchType) throws OseeCoreException;
}
