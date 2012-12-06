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
package org.eclipse.osee.ats.core.client.workflow;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workflow.ChangeType;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.IAtsWorkData;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemStore;
import org.eclipse.osee.ats.api.workflow.WorkStateFactory;
import org.eclipse.osee.ats.api.workflow.WorkStateProvider;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.core.client.util.WorkItemUtil;
import org.eclipse.osee.ats.core.users.AtsUserService;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;

public class AtsWorkItemStoreServiceImpl implements IAtsWorkItemStore {

   @Override
   public ChangeType getChangeType(IAtsWorkItem workItem) throws OseeCoreException {
      Artifact artifact = WorkItemUtil.get(workItem);
      if (Conditions.notNull(artifact)) {
         return ChangeTypeUtil.getChangeType(artifact);
      }
      return ChangeType.None;
   }

   @Override
   public IAtsTeamWorkflow getParentTeamWorkflow(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getParentTeamWorkflow();
   }

   @Override
   public String getTypeName(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getType();
   }

   @Override
   public String getPcrId(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return TeamWorkFlowManager.getPcrId(awa);
   }

   @Override
   public String getPriorityStr(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getSoleAttributeValue(AtsAttributeTypes.PriorityType, "");
   }

   @Override
   public String getNeedByDateStr(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getSoleAttributeValueAsString(AtsAttributeTypes.NeedBy, "");
   }

   @Override
   public String getCurrentStateName(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getCurrentStateName();
   }

   @Override
   public IAtsUser getCreatedBy(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getCreatedBy();
   }

   @Override
   public Date getCreatedDate(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getCreatedDate();
   }

   @Override
   public String getTeamName(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getParentTeamWorkflow().getTeamName();
   }

   @Override
   public boolean isCancelled(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.isCancelled();
   }

   @Override
   public String getCancelledFromState(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getCancelledFromState();
   }

   @Override
   public String getCancelledReason(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getCancelledReason();
   }

   @Override
   public IAtsWorkItem getParentWorkItem(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getParentAWA();
   }

   @Override
   public String getResolution(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getSoleAttributeValue(AtsAttributeTypes.Resolution, "");
   }

   @Override
   public boolean isInState(IAtsWorkItem workItem, IAtsStateDefinition stateDef) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getStateMgr().getCurrentStateName().equals(stateDef.getName());
   }

   @Override
   public boolean isStateVisited(IAtsWorkItem workItem, IAtsStateDefinition stateDef) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getStateMgr().isStateVisited(stateDef);
   }

   @Override
   public boolean isCompleted(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.isCompleted();
   }

   @Override
   public boolean isOfType(IAtsWorkItem workItem, Class<? extends IAtsObject> class1) throws OseeCoreException {
      if (class1.isInstance(workItem)) {
         return true;
      }
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      if (class1.isInstance(awa)) {
         return true;
      }
      return false;
   }

   @Override
   public IAtsUser getCompletedBy(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      String userId = awa.getSoleAttributeValue(AtsAttributeTypes.CompletedBy, null);
      if (Strings.isValid(userId)) {
         return AtsUserService.get().getUser(userId);
      }
      return null;
   }

   @Override
   public Date getCompletedDate(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getSoleAttributeValue(AtsAttributeTypes.CompletedDate, null);
   }

   @Override
   public IAtsUser getCancelledBy(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      String userId = awa.getSoleAttributeValue(AtsAttributeTypes.CancelledBy, null);
      if (Strings.isValid(userId)) {
         return AtsUserService.get().getUser(userId);
      }
      return null;
   }

   @Override
   public Date getCancelledDate(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getSoleAttributeValue(AtsAttributeTypes.CancelledDate, null);
   }

   @Override
   public boolean isCompletedOrCancelled(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.isCompletedOrCancelled();
   }

   @Override
   public boolean isInWork(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.isInWork();
   }

   @Override
   public String getCompletedFromState(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getCompletedFromState();
   }

   @Override
   public void setCompletedFromState(IAtsWorkItem workItem, String stateName) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      awa.setSoleAttributeValue(AtsAttributeTypes.CompletedFromState, stateName);
   }

   @Override
   public void setCancelledFromState(IAtsWorkItem workItem, String stateName) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      awa.setSoleAttributeValue(AtsAttributeTypes.CancelledFromState, stateName);
   }

   @Override
   public void setStateType(IAtsWorkItem workItem, StateType stateType) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      awa.setSoleAttributeValue(AtsAttributeTypes.CurrentStateType, stateType.name());
   }

   @Override
   public void setCompletedDate(IAtsWorkItem workItem, Date completedDate) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      awa.setSoleAttributeValue(AtsAttributeTypes.CompletedDate, completedDate);
   }

   @Override
   public void setCancelledDate(IAtsWorkItem workItem, Date cancelledDate) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      awa.setSoleAttributeValue(AtsAttributeTypes.CancelledDate, cancelledDate);
   }

   @Override
   public void setCompletedBy(IAtsWorkItem workItem, IAtsUser completedBy) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      awa.setSoleAttributeValue(AtsAttributeTypes.CompletedBy, completedBy);
   }

   @Override
   public void setCancelledBy(IAtsWorkItem workItem, IAtsUser cancelledBy) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      awa.setSoleAttributeValue(AtsAttributeTypes.CancelledBy, cancelledBy);
   }

   @Override
   public WorkStateProvider getStateData(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getStateData();
   }

   @Override
   public WorkStateFactory getStateFactory(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      return awa.getStateMgr();
   }

   @Override
   public void setAssignees(IAtsWorkItem workItem, List<IAtsUser> users) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      awa.getStateMgr().setAssignees(users);
   }

   @Override
   public void addState(IAtsWorkItem workItem, String stateName) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      awa.getStateMgr().createState(stateName);
   }

   @Override
   public ChangeType setChangeType(IAtsWorkItem workItem, ChangeType changeType) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      Conditions.checkNotNull(awa, "workItem", "Can't locate Work Item %s", awa.toStringWithId());
      ChangeTypeUtil.setChangeType(awa, changeType);
      return changeType;
   }

   @Override
   public String getAttributeStringValue(IAtsWorkItem workItem, String attributeName) throws OseeCoreException {
      Artifact artifact = WorkItemUtil.get(workItem);
      Conditions.checkNotNull(artifact, "workItem", "Can't locate Work Item %s", artifact.toStringWithId());
      IAttributeType attrType = AttributeTypeManager.getType(attributeName);
      if (Conditions.notNull(attrType)) {
         return artifact.getAttributesToString(attrType);
      }
      return String.format("Can't resolve Attribute Type Named [%s] for artifact [%s]", attributeName,
         artifact.toStringWithId());
   }

   @Override
   public IAtsWorkData getWorkData(IAtsWorkItem workItem) throws OseeCoreException {
      Artifact artifact = WorkItemUtil.get(workItem);
      Conditions.checkNotNull(artifact, "workItem", "Can't locate Work Item %s", artifact.toStringWithId());
      if (artifact instanceof AbstractWorkflowArtifact) {
         return new AtsWorkData((AbstractWorkflowArtifact) artifact);
      }
      return null;
   }

   @Override
   public String getChangeTypeStr(IAtsWorkItem workItem) throws OseeCoreException {
      return getChangeType(workItem).name();
   }

   @Override
   public IArtifactType getArtifactType(IAtsWorkItem workItem) throws OseeCoreException {
      Artifact artifact = WorkItemUtil.get(workItem);
      Conditions.checkNotNull(artifact, "workItem", "Can't locate Work Item %s", artifact.toStringWithId());
      return artifact.getArtifactTypeToken();
   }

   @Override
   public Collection<Object> getAttributeValues(IAtsObject atsObject, IAttributeType attributeType) throws OseeCoreException {
      Artifact artifact = null;
      try {
         artifact = WorkItemUtil.get(atsObject);
      } catch (ArtifactDoesNotExist ex) {
         // if atsObject not represented as persisted artifact, return empty set
         return Collections.emptyList();
      }
      Conditions.checkNotNull(artifact, "workItem", "Can't Find Artifact matching " + atsObject.toStringWithId());
      return artifact.getAttributeValues(attributeType);
   }

   @Override
   public boolean isOfType(IAtsWorkItem workItem, IArtifactType matchType) throws OseeCoreException {
      Artifact artifact = WorkItemUtil.get(workItem);
      Conditions.checkNotNull(artifact, "workItem", "Can't Find Artifact matching " + workItem.toStringWithId());
      return artifact.isOfType(matchType);
   }

}
