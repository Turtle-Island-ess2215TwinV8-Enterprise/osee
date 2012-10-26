/*
 * Created on Aug 9, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.client.workflow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workflow.assignee.IAtsAssigneeStoreService;
import org.eclipse.osee.ats.core.client.util.WorkItemUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;

public class AtsAssigneeStoreImpl implements IAtsAssigneeStoreService {

   @Override
   public Collection<? extends IAtsUser> getAssignees(IAtsObject atsObject) throws OseeCoreException {
      if (atsObject instanceof IAtsWorkItem) {
         IAtsWorkItem workItem = (IAtsWorkItem) atsObject;
         AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) WorkItemUtil.get(workItem);
         if (awa == null) {
            throw new OseeStateException("Work Item could not be found " + workItem.toString());
         }
         return awa.getAssignees();
      }
      return Collections.emptyList();
   }

   @Override
   public List<IAtsUser> getAssigneesForState(IAtsWorkItem workItem, String stateName) throws OseeCoreException {
      AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) WorkItemUtil.get(workItem);
      if (awa == null) {
         throw new OseeStateException("Work Item could not be found " + workItem.toString());
      }
      return awa.getStateMgr().getAssigneesForState(stateName);
   }

   @Override
   public void setAssigneesForState(IAtsWorkItem workItem, String stateName, List<? extends IAtsUser> users) throws OseeCoreException {
      AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) WorkItemUtil.get(workItem);
      if (awa == null) {
         throw new OseeStateException("Work Item could not be found " + workItem.toString());
      }
      awa.getStateMgr().setAssignees(stateName, users);
   }

   @Override
   public void addAssigneeForState(IAtsWorkItem workItem, String stateName, IAtsUser user) throws OseeCoreException {
      AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) WorkItemUtil.get(workItem);
      if (awa == null) {
         throw new OseeStateException("Work Item could not be found " + workItem.toString());
      }
      awa.getStateMgr().addAssignee(stateName, user);
   }

   @Override
   public void addAssigneesForState(IAtsWorkItem workItem, String stateName, List<? extends IAtsUser> users) throws OseeCoreException {
      AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) WorkItemUtil.get(workItem);
      if (awa == null) {
         throw new OseeStateException("Work Item could not be found " + workItem.toString());
      }
      awa.getStateMgr().addAssignees(stateName, users);
   }

   @Override
   public List<? extends IAtsUser> getImplementers_fromWorkItem(IAtsWorkItem workItem) throws OseeCoreException {
      AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) WorkItemUtil.get(workItem);
      if (awa == null) {
         throw new OseeStateException("Work Item could not be found " + workItem.toString());
      }
      return awa.getImplementers();
   }

}
