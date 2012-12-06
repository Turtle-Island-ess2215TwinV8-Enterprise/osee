/*
 * Created on Oct 8, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.workflow;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public interface IAtsWorkItemStore {

   IAtsWorkData getWorkData(IAtsWorkItem workItem) throws OseeCoreException;

   ChangeType getChangeType(IAtsWorkItem workItem) throws OseeCoreException;

   String getChangeTypeStr(IAtsWorkItem workItem) throws OseeCoreException;

   IArtifactType getArtifactType(IAtsWorkItem workItem) throws OseeCoreException;

   IAtsTeamWorkflow getParentTeamWorkflow(IAtsWorkItem workItem) throws OseeCoreException;

   Collection<Object> getAttributeValues(IAtsObject atsObject, IAttributeType attributeType) throws OseeCoreException;

   String getTypeName(IAtsWorkItem workItem) throws OseeCoreException;

   boolean isOfType(IAtsWorkItem item, IArtifactType matchType) throws OseeCoreException;

   String getPcrId(IAtsWorkItem workItem) throws OseeCoreException;

   String getPriorityStr(IAtsWorkItem workItem) throws OseeCoreException;

   String getNeedByDateStr(IAtsWorkItem workItem) throws OseeCoreException;

   String getCurrentStateName(IAtsWorkItem workItem) throws OseeCoreException;

   IAtsUser getCreatedBy(IAtsWorkItem workItem) throws OseeCoreException;

   Date getCreatedDate(IAtsWorkItem workItem) throws OseeCoreException;

   String getTeamName(IAtsWorkItem workItem) throws OseeCoreException;

   boolean isCancelled(IAtsWorkItem workItem) throws OseeCoreException;

   String getCancelledFromState(IAtsWorkItem workItem) throws OseeCoreException;

   String getCancelledReason(IAtsWorkItem workItem) throws OseeCoreException;

   IAtsWorkItem getParentWorkItem(IAtsWorkItem workItem) throws OseeCoreException;

   String getResolution(IAtsWorkItem workItem) throws OseeCoreException;

   boolean isInState(IAtsWorkItem workItem, IAtsStateDefinition stateDef) throws OseeCoreException;

   boolean isStateVisited(IAtsWorkItem workItem, IAtsStateDefinition stateDef) throws OseeCoreException;

   boolean isCompleted(IAtsWorkItem workItem) throws OseeCoreException;

   boolean isOfType(IAtsWorkItem workItem, Class<? extends IAtsObject> class1) throws OseeCoreException;

   IAtsUser getCompletedBy(IAtsWorkItem workItem) throws OseeCoreException;

   Date getCompletedDate(IAtsWorkItem workItem) throws OseeCoreException;

   IAtsUser getCancelledBy(IAtsWorkItem workItem) throws OseeCoreException;

   Date getCancelledDate(IAtsWorkItem workItem) throws OseeCoreException;

   boolean isCompletedOrCancelled(IAtsWorkItem workItem) throws OseeCoreException;

   boolean isInWork(IAtsWorkItem workItem) throws OseeCoreException;

   String getCompletedFromState(IAtsWorkItem workItem) throws OseeCoreException;

   void setCompletedFromState(IAtsWorkItem workItem, String stateName) throws OseeCoreException;

   void setCancelledFromState(IAtsWorkItem workItem, String stateName) throws OseeCoreException;

   void setStateType(IAtsWorkItem workItem, StateType StateType) throws OseeCoreException;

   void setCompletedDate(IAtsWorkItem workItem, Date completedDate) throws OseeCoreException;

   void setCancelledDate(IAtsWorkItem workItem, Date cancelledDate) throws OseeCoreException;

   void setCompletedBy(IAtsWorkItem workItem, IAtsUser completedBy) throws OseeCoreException;

   void setCancelledBy(IAtsWorkItem workItem, IAtsUser cancelledBy) throws OseeCoreException;

   WorkStateProvider getStateData(IAtsWorkItem workItem) throws OseeCoreException;

   WorkStateFactory getStateFactory(IAtsWorkItem workItem) throws OseeCoreException;

   void setAssignees(IAtsWorkItem workItem, List<IAtsUser> users) throws OseeCoreException;

   void addState(IAtsWorkItem workItem, String stateName) throws OseeCoreException;

   ChangeType setChangeType(IAtsWorkItem workItem, ChangeType changeType) throws OseeCoreException;

   String getAttributeStringValue(IAtsWorkItem workItem, String attributeName) throws OseeCoreException;
}
