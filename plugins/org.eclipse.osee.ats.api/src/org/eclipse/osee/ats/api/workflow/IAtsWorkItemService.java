/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.workflow;

import java.util.Collection;
import java.util.Date;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;

/**
 * @author Donald G. Dunne
 */
public interface IAtsWorkItemService {

   IAtsWorkData getWorkData(IAtsWorkItem workItem) throws OseeCoreException;

   // Change Type
   ChangeType getChangeType(IAtsWorkItem workItem) throws OseeCoreException;

   IArtifactType getArtifactType(IAtsWorkItem workItem) throws OseeCoreException;

   ChangeType setChangeType(IAtsWorkItem workItem, ChangeType changeType) throws OseeCoreException;

   Collection<Object> getAttributeValues(IAtsObject atsObject, IAttributeType attributeType) throws OseeCoreException;

   String getChangeTypeStr(IAtsWorkItem workItem) throws OseeCoreException;

   // Parent 
   IAtsTeamWorkflow getParentTeamWorkflow(IAtsWorkItem workItem) throws OseeCoreException;

   IAtsWorkItem getParentWorkItem(IAtsWorkItem workItem) throws OseeCoreException;

   // Misc
   String getPriorityStr(IAtsWorkItem workItem) throws OseeCoreException;

   String getNeedByDateStr(IAtsWorkItem workItem) throws OseeCoreException;

   String getTypeName(IAtsWorkItem workItem) throws OseeCoreException;

   String getPcrId(IAtsWorkItem workItem) throws OseeCoreException;

   String getResolution(IAtsWorkItem workItem) throws OseeCoreException;

   String getTeamName(IAtsWorkItem workItem) throws OseeCoreException;

   // HTML Report
   XResultData getPrintHtml(IAtsWorkItem workItem) throws OseeCoreException;

   String getOverviewHtml(IAtsWorkItem workItem) throws OseeCoreException;

   boolean isOfType(IAtsWorkItem workItem, Class<? extends IAtsObject> class1) throws OseeCoreException;

   // State
   void setStateType(IAtsWorkItem workItem, StateType StateType) throws OseeCoreException;

   String getCurrentStateName(IAtsWorkItem workItem) throws OseeCoreException;

   boolean isCompleted(IAtsWorkItem workItem) throws OseeCoreException;

   IAtsUser getCompletedBy(IAtsWorkItem workItem) throws OseeCoreException;

   Date getCompletedDate(IAtsWorkItem workItem) throws OseeCoreException;

   boolean isCancelled(IAtsWorkItem workItem) throws OseeCoreException;

   IAtsUser getCancelledBy(IAtsWorkItem workItem) throws OseeCoreException;

   Date getCancelledDate(IAtsWorkItem workItem) throws OseeCoreException;

   String getCancelledReason(IAtsWorkItem workItem) throws OseeCoreException;

   boolean isCompletedOrCancelled(IAtsWorkItem workItem) throws OseeCoreException;

   boolean isInWork(IAtsWorkItem workItem) throws OseeCoreException;

   String getCompletedFromState(IAtsWorkItem workItem) throws OseeCoreException;

   String getCancelledFromState(IAtsWorkItem workItem) throws OseeCoreException;

   void setCompletedFromState(IAtsWorkItem workItem, String string) throws OseeCoreException;

   void setCancelledFromState(IAtsWorkItem workItem, String string) throws OseeCoreException;

   void setCompletedDate(IAtsWorkItem workItem, Date completedDate) throws OseeCoreException;

   void setCancelledDate(IAtsWorkItem workItem, Date cancelledDate) throws OseeCoreException;

   void setCompletedBy(IAtsWorkItem workItem, IAtsUser completedBy) throws OseeCoreException;

   void setCancelledBy(IAtsWorkItem workItem, IAtsUser cancelledBy) throws OseeCoreException;

   IAtsUser getCreatedBy(IAtsWorkItem workItem) throws OseeCoreException;

   Date getCreatedDate(IAtsWorkItem workItem) throws OseeCoreException;

   // TODO Replace with methods in WorkItemService or AssigneeService or MetricsService
   WorkStateProvider getStateData(IAtsWorkItem workItem) throws OseeCoreException;

   WorkStateFactory getStateFactory(IAtsWorkItem workItem) throws OseeCoreException;

   void addState(IAtsWorkItem workItem, String stateName) throws OseeCoreException;

   boolean isOfType(IAtsWorkItem item, IArtifactType matchType) throws OseeCoreException;
}
