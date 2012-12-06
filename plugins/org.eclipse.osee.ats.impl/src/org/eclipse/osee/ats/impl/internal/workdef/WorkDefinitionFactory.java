/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.impl.internal.workdef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.task.IAtsTask;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinitionService;
import org.eclipse.osee.ats.api.util.WorkDefinitionMatch;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionService;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionStore;
import org.eclipse.osee.ats.api.workdef.WorkDefinitionDefault;
import org.eclipse.osee.ats.api.workflow.IAtsDecisionReview;
import org.eclipse.osee.ats.api.workflow.IAtsGoal;
import org.eclipse.osee.ats.api.workflow.IAtsPeerToPeerReview;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.framework.core.exception.MultipleAttributesExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class WorkDefinitionFactory {

   private final IAtsWorkItemService workItemService;
   private final IAtsWorkDefinitionService workDefinitionService;
   private final IAtsWorkDefinitionStore workDefinitionStore;
   private final IAtsTeamDefinitionService teamDefService;

   public WorkDefinitionFactory(IAtsWorkItemService workItemService, IAtsWorkDefinitionService workDefinitionService, IAtsWorkDefinitionStore workDefinitionStore, IAtsTeamDefinitionService teamDefService) {
      this.workItemService = workItemService;
      this.workDefinitionService = workDefinitionService;
      this.workDefinitionStore = workDefinitionStore;
      this.teamDefService = teamDefService;
   }
   // Cache the WorkDefinition object for each WorkDefinition id so don't have to reload
   // This grows as WorkDefinitions are requested/loaded
   private final Map<String, WorkDefinitionMatch> workDefIdToWorkDefintion = new HashMap<String, WorkDefinitionMatch>();

   public void clearCaches() {
      workDefIdToWorkDefintion.clear();
   }

   public WorkDefinitionMatch getWorkDefinition(String id) {
      WorkDefinitionMatch workDefinitionMatch = workDefIdToWorkDefintion.get(id);
      if (workDefinitionMatch != null) {
         return workDefinitionMatch;
      }
      WorkDefinitionMatch match = new WorkDefinitionMatch();
      // Try to get from new DSL provider if configured to use it
      try {
         XResultData resultData = new XResultData(false);
         if (workDefinitionService == null) {
            throw new OseeStateException("ATS Work Definition Service is not found.");
         }
         IAtsWorkDefinition workDef = workDefinitionService.getWorkDef(id, resultData);
         if (workDef != null) {
            match.setWorkDefinition(workDef);
            if (!resultData.isEmpty()) {
               match.addTrace((String.format("from DSL provider loaded id [%s] [%s]", id, resultData.toString())));
            } else {
               match.addTrace((String.format("from DSL provider loaded id [%s]", id)));
            }
         }
         if (match.isMatched()) {
            workDefIdToWorkDefintion.put(id, match);
         } else {
            System.out.println(String.format("Unable to load Work Definition [%s]", id));
         }
      } catch (OseeCoreException ex) {
         return new WorkDefinitionMatch(null, ex.getLocalizedMessage());
      }
      return match;
   }

   private WorkDefinitionMatch getWorkDefinitionFromArtifactsAttributeValue(IAtsTeamDefinition teamDef) {
      // If this artifact specifies it's own workflow definition, use it
      String workFlowDefId = teamDef.getWorkflowDefinition();
      if (Strings.isValid(workFlowDefId)) {
         WorkDefinitionMatch match = getWorkDefinition(workFlowDefId);
         if (match.isMatched()) {
            match.addTrace(String.format("from artifact [%s] for id [%s]", teamDef, workFlowDefId));
            return match;
         }
      }
      return new WorkDefinitionMatch();
   }

   private WorkDefinitionMatch getTaskWorkDefinitionFromArtifactsAttributeValue(IAtsTeamDefinition teamDef) {
      // If this artifact specifies it's own workflow definition, use it
      String workFlowDefId = teamDef.getRelatedTaskWorkDefinition();
      if (Strings.isValid(workFlowDefId)) {
         WorkDefinitionMatch match = getWorkDefinition(workFlowDefId);
         if (match.isMatched()) {
            match.addTrace(String.format("from artifact [%s] for id [%s]", teamDef, workFlowDefId));
            return match;
         }
      }
      return new WorkDefinitionMatch();
   }

   private WorkDefinitionMatch getWorkDefinitionFromArtifactsAttributeValue(IAtsWorkItem workItem) throws OseeCoreException {
      // If this artifact specifies it's own workflow definition, use it
      String workFlowDefId = workDefinitionStore.getWorkDefinitionAttribute(workItem);
      if (Strings.isValid(workFlowDefId)) {
         WorkDefinitionMatch match = getWorkDefinition(workFlowDefId);
         if (match.isMatched()) {
            match.addTrace(String.format("from artifact [%s] for id [%s]", workItem, workFlowDefId));
            return match;
         }
      }
      return new WorkDefinitionMatch();
   }

   private WorkDefinitionMatch getTaskWorkDefinitionFromArtifactsAttributeValue(IAtsWorkItem workItem) throws OseeCoreException {
      // If this artifact specifies it's own workflow definition, use it
      String workFlowDefId = workDefinitionStore.getRelatedTaskWorkDefinitionAttribute(workItem);
      if (Strings.isValid(workFlowDefId)) {
         WorkDefinitionMatch match = getWorkDefinition(workFlowDefId);
         if (match.isMatched()) {
            match.addTrace(String.format("from artifact [%s] for id [%s]", workItem, workFlowDefId));
            return match;
         }
      }
      return new WorkDefinitionMatch();
   }

   /**
    * Look at team def's attribute for Work Definition setting, otherwise, walk up team tree for setting
    */
   protected WorkDefinitionMatch getWorkDefinitionFromTeamDefinitionAttributeInherited(IAtsTeamDefinition teamDef) throws OseeCoreException {
      WorkDefinitionMatch match = getWorkDefinitionFromArtifactsAttributeValue(teamDef);
      if (match.isMatched()) {
         return match;
      }
      IAtsTeamDefinition parentArt = teamDef.getParentTeamDef();
      if (parentArt != null) {
         return getWorkDefinitionFromTeamDefinitionAttributeInherited(parentArt);
      }
      return new WorkDefinitionMatch();
   }

   public WorkDefinitionMatch getWorkDefinitionForTask(IAtsTask task) throws OseeCoreException {
      IAtsTeamWorkflow teamWf = workItemService.getParentTeamWorkflow(task);
      return getWorkDefinitionForTask(teamWf, task);
   }

   /**
    * Return the WorkDefinition that would be assigned to a new Task. This is not necessarily the actual WorkDefinition
    * used because it can be overridden once the Task artifact is created.
    */
   public WorkDefinitionMatch getWorkDefinitionForTaskNotYetCreated(IAtsTeamWorkflow teamWf) throws OseeCoreException {
      return getWorkDefinitionForTask(teamWf, null);
   }

   /**
    * @param teamWf
    * @param task - if null, returned WorkDefinition will be proposed; else returned will be actual
    */
   private WorkDefinitionMatch getWorkDefinitionForTask(IAtsTeamWorkflow teamWf, IAtsTask task) throws OseeCoreException {
      WorkDefinitionMatch match = workDefinitionStore.getWorkDefinitionFromTaskViaProviders(teamWf);
      if (!match.isMatched() && task != null) {
         // If task specifies it's own workflow id, use it
         match = getWorkDefinitionFromArtifactsAttributeValue(task);
      }
      if (!match.isMatched()) {
         // Else If parent SMA has a related task definition workflow id specified, use it
         WorkDefinitionMatch match2 = getTaskWorkDefinitionFromArtifactsAttributeValue(teamWf);
         if (match2.isMatched()) {
            match2.addTrace(String.format("from task parent SMA [%s]", teamWf));
            match = match2;
         }
      }
      if (!match.isMatched()) {
         // Else If parent TeamWorkflow's IAtsTeamDefinition has a related task definition workflow id, use it
         match = getTaskWorkDefinitionFromArtifactsAttributeValue(teamDefService.getTeamDefinition(teamWf));
      }
      if (!match.isMatched()) {
         match = getWorkDefinition(WorkDefinitionDefault.TaskWorkflowDefinitionId);
         match.addTrace("WorkDefinitionFactory - Default Task");
      }
      return match;
   }

   /**
    * @return WorkDefinitionMatch for Peer Review either from attribute value or default
    */
   protected WorkDefinitionMatch getWorkDefinitionForPeerToPeerReview(IAtsPeerToPeerReview review) throws OseeCoreException {
      Conditions.notNull(review, getClass().getSimpleName());
      WorkDefinitionMatch match = getWorkDefinitionFromArtifactsAttributeValue(review);
      if (!match.isMatched()) {
         match = getDefaultPeerToPeerWorkflowDefinitionMatch();
      }
      return match;
   }

   public WorkDefinitionMatch getDefaultPeerToPeerWorkflowDefinitionMatch() {
      WorkDefinitionMatch match = getWorkDefinition(WorkDefinitionDefault.PeerToPeerWorkflowDefinitionId);
      match.addTrace("WorkDefinitionFactory - Default PeerToPeer");
      return match;
   }

   /**
    * @return WorkDefinitionMatch for peer review off created teamWf. Will use configured value off team definitions
    * with recurse or return default review work definition
    */
   public WorkDefinitionMatch getWorkDefinitionForPeerToPeerReviewNotYetCreated(IAtsTeamWorkflow teamWf) throws OseeCoreException {
      Conditions.notNull(teamWf, getClass().getSimpleName());
      WorkDefinitionMatch match =
         getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(teamDefService.getTeamDefinition(teamWf));
      if (!match.isMatched()) {
         match = getDefaultPeerToPeerWorkflowDefinitionMatch();
      }
      return match;
   }

   /**
    * @return WorkDefinitionMatch of peer review from team definition related to actionableItem or return default review
    * work definition
    */
   public WorkDefinitionMatch getWorkDefinitionForPeerToPeerReviewNotYetCreatedAndStandalone(IAtsActionableItem actionableItem) throws OseeCoreException {
      Conditions.notNull(actionableItem, getClass().getSimpleName());
      WorkDefinitionMatch match =
         getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(actionableItem.getTeamDefinitionInherited());
      if (!match.isMatched()) {
         match = getDefaultPeerToPeerWorkflowDefinitionMatch();
      }
      return match;
   }

   /**
    * @return WorkDefinitionMatch of teamDefinition configured with RelatedPeerWorkflowDefinition attribute with recurse
    * up to top teamDefinition or will return no match
    */
   protected WorkDefinitionMatch getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(IAtsTeamDefinition teamDefinition) throws OseeCoreException {
      Conditions.notNull(teamDefinition, getClass().getSimpleName());
      WorkDefinitionMatch match = new WorkDefinitionMatch();
      Collection<Object> attrs =
         workItemService.getAttributeValues(teamDefinition, AtsAttributeTypes.RelatedPeerWorkflowDefinition);
      if (attrs.isEmpty()) {
         IAtsTeamDefinition parentTeamDef = teamDefinition.getParentTeamDef();
         if (parentTeamDef != null) {
            match = getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(parentTeamDef);
         }
      } else {
         match = getWorkDefinition((String) attrs.iterator().next());
         match.addTrace("PeerToPeer from Team Definition");
      }
      return match;
   }

   public WorkDefinitionMatch getWorkDefinition(IAtsWorkItem workItem) throws OseeCoreException {
      WorkDefinitionMatch match = new WorkDefinitionMatch();
      if (workItemService.isOfType(workItem, IAtsTask.class)) {
         match = getWorkDefinitionForTask((IAtsTask) workItem);
      } else if (workItemService.isOfType(workItem, IAtsPeerToPeerReview.class)) {
         match = getWorkDefinitionForPeerToPeerReview((IAtsPeerToPeerReview) workItem);
      }
      if (!match.isMatched() && workItemService.isOfType(workItem, IAtsWorkItem.class)) {
         // Check extensions for definition handling
         match = workDefinitionStore.getWorkDefinitionFromProviders(workItem);
         if (!match.isMatched()) {
            // If this artifact specifies it's own workflow definition, use it
            match = getWorkDefinitionFromArtifactsAttributeValue(workItem);
            if (!match.isMatched()) {
               // Otherwise, use workflow defined by attribute of WorkflowDefinition
               // Note: This is new.  Old TeamDefs got workflow off relation
               if (workItemService.isOfType(workItem, IAtsTeamWorkflow.class)) {
                  IAtsTeamDefinition teamDef = teamDefService.getTeamDefinition(workItem);
                  match = getWorkDefinitionFromTeamDefinitionAttributeInherited(teamDef);
               } else if (workItemService.isOfType(workItem, IAtsGoal.class)) {
                  match = getWorkDefinition(WorkDefinitionDefault.GoalWorkflowDefinitionId);
                  match.addTrace("WorkDefinitionFactory - GoalWorkflowDefinitionId");
               } else if (workItemService.isOfType(workItem, IAtsDecisionReview.class)) {
                  match = getWorkDefinition(WorkDefinitionDefault.DecisionWorkflowDefinitionId);
                  match.addTrace("WorkDefinitionFactory - DecisionWorkflowDefinitionId");
               }
            }
         }
      }
      return match;
   }

   public boolean isTaskOverridingItsWorkDefinition(IAtsTask task) throws MultipleAttributesExist, OseeCoreException {
      return workDefinitionStore.getWorkDefinitionAttribute(task) != null;
   }

   public IAtsWorkDefinition copyWorkDefinition(String newName, IAtsWorkDefinition workDef, XResultData resultData) {
      return workDefinitionService.copyWorkDefinition(newName, workDef, resultData,
         workDefinitionStore.getAttributeResolver(), workDefinitionStore.getUserResolver());
   }

   public void addWorkDefinition(IAtsWorkDefinition workDef) {
      WorkDefinitionMatch match =
         new WorkDefinitionMatch(workDef.getId(), "programatically added via WorkDefinitionFactory.addWorkDefinition");
      match.setWorkDefinition(workDef);
      workDefIdToWorkDefintion.put(workDef.getName(), match);
   }

   public void removeWorkDefinition(IAtsWorkDefinition workDef) {
      workDefIdToWorkDefintion.remove(workDef.getName());
   }

   public Collection<IAtsWorkDefinition> getLoadedWorkDefinitions() {
      List<IAtsWorkDefinition> workDefs = new ArrayList<IAtsWorkDefinition>();
      for (WorkDefinitionMatch match : workDefIdToWorkDefintion.values()) {
         if (match.getWorkDefinition() != null) {
            workDefs.add(match.getWorkDefinition());
         }
      }
      return workDefs;
   }

}
