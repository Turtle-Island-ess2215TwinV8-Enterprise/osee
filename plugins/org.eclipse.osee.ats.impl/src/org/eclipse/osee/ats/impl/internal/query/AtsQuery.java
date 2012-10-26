/*
 * Created on Oct 8, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.query.IAtsQuery;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinitionService;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workflow.IAtsWorkData;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.ats.impl.internal.team.AtsTeamDefinitionService;
import org.eclipse.osee.ats.impl.internal.workitem.AtsWorkItemServiceImpl;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public class AtsQuery implements IAtsQuery {

   private final Collection<IAtsWorkItem> items = new ArrayList<IAtsWorkItem>();
   private final IAtsWorkItemService workItemService;
   private final IAtsTeamDefinitionService teamDefService;

   public AtsQuery(Collection<? extends IAtsWorkItem> workItems, IAtsWorkItemService workItemService, IAtsTeamDefinitionService teamDefService) {
      items.addAll(workItems);
      this.workItemService = workItemService;
      this.teamDefService = teamDefService;

   }

   public AtsQuery(Collection<? extends IAtsWorkItem> workItems) {
      this(workItems, AtsWorkItemServiceImpl.get(), AtsTeamDefinitionService.getService());
   }

   @Override
   public IAtsQuery isOfType(IArtifactType... artifactType) throws OseeCoreException {
      boolean found = false;
      for (IAtsWorkItem item : new CopyOnWriteArrayList<IAtsWorkItem>(items)) {
         for (IArtifactType matchType : artifactType) {
            if (workItemService.isOfType(item, matchType)) {
               found = true;
               break;
            }
         }
         if (!found) {
            items.remove(item);
         }
      }
      return this;
   }

   @Override
   public IAtsQuery union(IAtsQuery... atsQuery) throws OseeCoreException {
      for (IAtsQuery query : atsQuery) {
         for (IAtsWorkItem workItem : query.getItems()) {
            items.add(workItem);
         }
      }
      return this;
   }

   @Override
   public IAtsQuery fromTeam(IAtsTeamDefinition teamDef) throws OseeCoreException {
      for (IAtsWorkItem workItem : new CopyOnWriteArrayList<IAtsWorkItem>(items)) {
         IAtsTeamDefinition itemTeamDef = teamDefService.getTeamDefinition(workItem);
         if (!itemTeamDef.getGuid().equals(teamDef.getGuid())) {
            items.remove(workItem);
         }
      }
      return this;
   }

   @Override
   public IAtsQuery isStateType(StateType... stateType) throws OseeCoreException {
      List<StateType> types = new ArrayList<StateType>();
      for (StateType type : stateType) {
         types.add(type);
      }
      for (IAtsWorkItem workItem : new CopyOnWriteArrayList<IAtsWorkItem>(items)) {
         IAtsWorkData workData = workItemService.getWorkData(workItem);
         if (workData.isCompleted() && !types.contains(StateType.Completed)) {
            items.remove(workItem);
         } else if (workData.isCancelled() && !types.contains(StateType.Cancelled)) {
            items.remove(workItem);
         } else if (workData.isInWork() && !types.contains(StateType.Working)) {
            items.remove(workItem);
         }
      }
      return this;
   }

   @Override
   public Collection<IAtsWorkItem> getItems() {
      return items;
   }

   @Override
   public IAtsQuery withOrValue(IAttributeType attributeType, Collection<? extends Object> matchValues) throws OseeCoreException {
      if (matchValues != null && !matchValues.isEmpty()) {
         for (IAtsWorkItem workItem : new CopyOnWriteArrayList<IAtsWorkItem>(items)) {
            Collection<Object> currAttrValues = workItemService.getAttributeValues(workItem, attributeType);
            boolean found = false;
            for (Object matchValue : matchValues) {
               if (currAttrValues.contains(matchValue)) {
                  found = true;
                  break;
               }
            }
            if (!found) {
               items.remove(workItem);
            }
         }
      }
      return this;
   }

}
