/*
 * Created on Aug 1, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.client.workflow;

import java.util.Collection;
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
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;

public class AtsWorkItemStoreServiceImpl implements IAtsWorkItemStore {

   @Override
   public ChangeType getChangeType(IAtsWorkItem workItem) throws OseeCoreException {
      Artifact artifact = WorkItemUtil.get(workItem);
      if (artifact != null) {
         return ChangeTypeUtil.getChangeType(artifact);
      }
      return ChangeType.None;
   }

   @Override
   public IAtsTeamWorkflow getParentTeamWorkflow(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getParentTeamWorkflow();
      }
      return null;
   }

   @Override
   public String getTypeName(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getType();
      }
      return null;
   }

   @Override
   public String getPcrId(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return TeamWorkFlowManager.getPcrId(awa);
      }
      return null;
   }

   @Override
   public String getPriorityStr(IAtsWorkItem workItem) throws OseeCoreException {
      Artifact artifact = WorkItemUtil.get(workItem);
      if (artifact != null) {
         return artifact.getSoleAttributeValue(AtsAttributeTypes.PriorityType, "");
      }
      return "";
   }

   @Override
   public String getNeedByDateStr(IAtsWorkItem workItem) throws OseeCoreException {
      Artifact artifact = WorkItemUtil.get(workItem);
      if (artifact != null) {
         return artifact.getSoleAttributeValueAsString(AtsAttributeTypes.NeedBy, "");
      }
      return "";
   }

   @Override
   public String getCurrentStateName(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getCurrentStateName();
      }
      return null;
   }

   @Override
   public IAtsUser getCreatedBy(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getCreatedBy();
      }
      return null;
   }

   @Override
   public Date getCreatedDate(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getCreatedDate();
      }
      return null;
   }

   @Override
   public String getTeamName(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getParentTeamWorkflow().getTeamName();
      }
      return null;
   }

   @Override
   public boolean isCancelled(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.isCancelled();
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public String getCancelledFromState(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getCancelledFromState();
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public String getCancelledReason(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getCancelledReason();
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public IAtsWorkItem getParentWorkItem(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getParentAWA();
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public String getResolution(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getSoleAttributeValue(AtsAttributeTypes.Resolution, "");
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public boolean isInState(IAtsWorkItem workItem, IAtsStateDefinition stateDef) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getStateMgr().getCurrentStateName().equals(stateDef.getName());
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public boolean isStateVisited(IAtsWorkItem workItem, IAtsStateDefinition stateDef) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getStateMgr().isStateVisited(stateDef);
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public boolean isCompleted(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.isCompleted();
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public boolean isOfType(IAtsWorkItem workItem, Class<? extends IAtsObject> class1) throws OseeCoreException {
      if (class1.isInstance(workItem)) {
         return true;
      }
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa == null) {
         throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
      }
      if (class1.isInstance(awa)) {
         return true;
      }
      return false;
   }

   @Override
   public IAtsUser getCompletedBy(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         String userId = awa.getSoleAttributeValue(AtsAttributeTypes.CompletedBy, null);
         if (Strings.isValid(userId)) {
            return AtsUserService.get().getUser(userId);
         }
         return null;
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public Date getCompletedDate(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getSoleAttributeValue(AtsAttributeTypes.CompletedDate, null);
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public IAtsUser getCancelledBy(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         String userId = awa.getSoleAttributeValue(AtsAttributeTypes.CancelledBy, null);
         if (Strings.isValid(userId)) {
            return AtsUserService.get().getUser(userId);
         }
         return null;
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public Date getCancelledDate(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getSoleAttributeValue(AtsAttributeTypes.CancelledDate, null);
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public boolean isCompletedOrCancelled(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.isCompletedOrCancelled();
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public boolean isInWork(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.isInWork();
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public String getCompletedFromState(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getCompletedFromState();
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public void setCompletedFromState(IAtsWorkItem workItem, String stateName) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         awa.setSoleAttributeValue(AtsAttributeTypes.CompletedFromState, stateName);
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public void setCancelledFromState(IAtsWorkItem workItem, String stateName) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         awa.setSoleAttributeValue(AtsAttributeTypes.CancelledFromState, stateName);
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public void setStateType(IAtsWorkItem workItem, StateType stateType) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         awa.setSoleAttributeValue(AtsAttributeTypes.CurrentStateType, stateType.name());
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public void setCompletedDate(IAtsWorkItem workItem, Date completedDate) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         awa.setSoleAttributeValue(AtsAttributeTypes.CompletedDate, completedDate);
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public void setCancelledDate(IAtsWorkItem workItem, Date cancelledDate) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         awa.setSoleAttributeValue(AtsAttributeTypes.CancelledDate, cancelledDate);
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public void setCompletedBy(IAtsWorkItem workItem, IAtsUser completedBy) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         awa.setSoleAttributeValue(AtsAttributeTypes.CompletedBy, completedBy);
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public void setCancelledBy(IAtsWorkItem workItem, IAtsUser cancelledBy) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         awa.setSoleAttributeValue(AtsAttributeTypes.CancelledBy, cancelledBy);
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public WorkStateProvider getStateData(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getStateData();
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public WorkStateFactory getStateFactory(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         return awa.getStateMgr();
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public void setAssignees(IAtsWorkItem workItem, List<IAtsUser> users) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         awa.getStateMgr().setAssignees(users);
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public void addState(IAtsWorkItem workItem, String stateName) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         awa.getStateMgr().createState(stateName);
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public ChangeType setChangeType(IAtsWorkItem workItem, ChangeType changeType) throws OseeCoreException {
      AbstractWorkflowArtifact awa = WorkItemUtil.get(workItem, AbstractWorkflowArtifact.class);
      if (awa != null) {
         ChangeTypeUtil.setChangeType(awa, changeType);
      }
      throw new OseeArgumentException("Can't locate Work Item " + workItem.toStringWithId());
   }

   @Override
   public String getAttributeStringValue(IAtsWorkItem workItem, String attributeName) throws OseeCoreException {
      Artifact artifact = WorkItemUtil.get(workItem);
      if (artifact == null) {
         return "Can't Find Artifact matching " + workItem.toStringWithId();
      }
      IAttributeType attrType = AttributeTypeManager.getType(attributeName);
      if (attrType != null) {
         return artifact.getAttributesToString(attrType);
      }
      return String.format("Can't resolve Attribute Type Named [%s]", attributeName);
   }

   @Override
   public IAtsWorkData getWorkData(IAtsWorkItem workItem) throws OseeCoreException {
      return null;
   }

   @Override
   public String getChangeTypeStr(IAtsWorkItem workItem) throws OseeCoreException {
      return null;
   }

   @Override
   public IArtifactType getArtifactType(IAtsWorkItem workItem) throws OseeCoreException {
      return null;
   }

   @Override
   public Collection<Object> getAttributeValues(IAtsWorkItem workItem, IAttributeType attributeType) throws OseeCoreException {
      return null;
   }

   @Override
   public boolean isOfType(IAtsWorkItem item, IArtifactType matchType) throws OseeCoreException {
      return false;
   }

}
