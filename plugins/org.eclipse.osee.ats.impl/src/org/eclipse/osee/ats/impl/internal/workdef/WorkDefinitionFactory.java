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
import org.eclipse.osee.ats.api.task.IAtsTask;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.util.WorkDefinitionMatch;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionService;
import org.eclipse.osee.ats.api.workdef.WorkDefinitionDefault;
import org.eclipse.osee.ats.api.workflow.IAtsDecisionReview;
import org.eclipse.osee.ats.api.workflow.IAtsGoal;
import org.eclipse.osee.ats.api.workflow.IAtsPeerToPeerReview;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.impl.internal.AtsWorkDefinitionServiceImpl;
import org.eclipse.osee.ats.impl.internal.AtsWorkDefinitionStore;
import org.eclipse.osee.ats.impl.internal.team.AtsTeamDefinitionService;
import org.eclipse.osee.ats.impl.internal.workitem.AtsWorkItemServiceImpl;
import org.eclipse.osee.framework.core.exception.MultipleAttributesExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class WorkDefinitionFactory {

   // Cache the WorkDefinition object for each WorkDefinition id so don't have to reload
   // This grows as WorkDefinitions are requested/loaded
   private static final Map<String, WorkDefinitionMatch> workDefIdToWorkDefintion =
      new HashMap<String, WorkDefinitionMatch>();

   public static void clearCaches() {
      workDefIdToWorkDefintion.clear();
   }

   public static WorkDefinitionMatch getWorkDefinition(String id) {
      if (!workDefIdToWorkDefintion.containsKey(id)) {
         WorkDefinitionMatch match = new WorkDefinitionMatch();
         // Try to get from new DSL provider if configured to use it
         if (!match.isMatched()) {
            try {
               XResultData resultData = new XResultData(false);
               IAtsWorkDefinitionService service = AtsWorkDefinitionServiceImpl.instance;
               if (service == null) {
                  throw new IllegalStateException("ATS Work Definition Service is not found.");
               }
               IAtsWorkDefinition workDef = service.getWorkDef(id, resultData);
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
            } catch (Exception ex) {
               return new WorkDefinitionMatch(null, ex.getLocalizedMessage());
            }
         }
      }
      WorkDefinitionMatch match = workDefIdToWorkDefintion.get(id);
      if (match == null) {
         match = new WorkDefinitionMatch();
      }
      return match;
   }

   private static WorkDefinitionMatch getWorkDefinitionFromArtifactsAttributeValue(IAtsTeamDefinition teamDef) {
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

   private static WorkDefinitionMatch getTaskWorkDefinitionFromArtifactsAttributeValue(IAtsTeamDefinition teamDef) {
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

   private static WorkDefinitionMatch getWorkDefinitionFromArtifactsAttributeValue(IAtsWorkItem workItem) throws OseeCoreException {
      // If this artifact specifies it's own workflow definition, use it
      String workFlowDefId = AtsWorkDefinitionStore.getService().getWorkDefinitionAttribute(workItem);
      if (Strings.isValid(workFlowDefId)) {
         WorkDefinitionMatch match = getWorkDefinition(workFlowDefId);
         if (match.isMatched()) {
            match.addTrace(String.format("from artifact [%s] for id [%s]", workItem, workFlowDefId));
            return match;
         }
      }
      return new WorkDefinitionMatch();
   }

   private static WorkDefinitionMatch getTaskWorkDefinitionFromArtifactsAttributeValue(IAtsWorkItem workItem) throws OseeCoreException {
      // If this artifact specifies it's own workflow definition, use it
      String workFlowDefId = AtsWorkDefinitionStore.getService().getRelatedTaskWorkDefinitionAttribute(workItem);
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
   protected static WorkDefinitionMatch getWorkDefinitionFromTeamDefinitionAttributeInherited(IAtsTeamDefinition teamDef) throws OseeCoreException {
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

   public static WorkDefinitionMatch getWorkDefinitionForTask(IAtsTask task) throws OseeCoreException {
      IAtsTeamWorkflow teamWf = AtsWorkItemServiceImpl.get().getParentTeamWorkflow(task);
      return getWorkDefinitionForTask(teamWf, task);
   }

   /**
    * Return the WorkDefinition that would be assigned to a new Task. This is not necessarily the actual WorkDefinition
    * used because it can be overridden once the Task artifact is created.
    */
   public static WorkDefinitionMatch getWorkDefinitionForTaskNotYetCreated(IAtsTeamWorkflow teamWf) throws OseeCoreException {
      return getWorkDefinitionForTask(teamWf, null);
   }

   /**
    * @param teamWf
    * @param task - if null, returned WorkDefinition will be proposed; else returned will be actual
    */
   private static WorkDefinitionMatch getWorkDefinitionForTask(IAtsTeamWorkflow teamWf, IAtsTask task) throws OseeCoreException {
      WorkDefinitionMatch match = AtsWorkDefinitionStore.getService().getWorkDefinitionFromTaskViaProviders(teamWf);
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
         match =
            getTaskWorkDefinitionFromArtifactsAttributeValue(AtsTeamDefinitionService.getService().getTeamDefinition(
               teamWf));
      }
      if (!match.isMatched()) {
         match = getWorkDefinition(WorkDefinitionDefault.TaskWorkflowDefinitionId);
         match.addTrace("WorkDefinitionFactory - Default Task");
      }
      return match;
   }

   public static WorkDefinitionMatch getWorkDefinition(IAtsWorkItem workItem) throws OseeCoreException {
      WorkDefinitionMatch match = new WorkDefinitionMatch();
      if (AtsWorkItemServiceImpl.get().isOfType(workItem, IAtsTask.class)) {
         match = getWorkDefinitionForTask((IAtsTask) workItem);
      }
      if (!match.isMatched() && AtsWorkItemServiceImpl.get().isOfType(workItem, IAtsWorkItem.class)) {
         // Check extensions for definition handling
         match = AtsWorkDefinitionStore.getService().getWorkDefinitionFromProviders(workItem);
         if (!match.isMatched()) {
            // If this artifact specifies it's own workflow definition, use it
            match = getWorkDefinitionFromArtifactsAttributeValue(workItem);
            if (!match.isMatched()) {
               // Otherwise, use workflow defined by attribute of WorkflowDefinition
               // Note: This is new.  Old TeamDefs got workflow off relation
               if (AtsWorkItemServiceImpl.get().isOfType(workItem, IAtsTeamWorkflow.class)) {
                  IAtsTeamDefinition teamDef = AtsTeamDefinitionService.getService().getTeamDefinition(workItem);
                  match = getWorkDefinitionFromTeamDefinitionAttributeInherited(teamDef);
               } else if (AtsWorkItemServiceImpl.get().isOfType(workItem, IAtsGoal.class)) {
                  match = getWorkDefinition(WorkDefinitionDefault.GoalWorkflowDefinitionId);
                  match.addTrace("WorkDefinitionFactory - GoalWorkflowDefinitionId");
               } else if (AtsWorkItemServiceImpl.get().isOfType(workItem, IAtsPeerToPeerReview.class)) {
                  match = getWorkDefinition(WorkDefinitionDefault.PeerToPeerWorkflowDefinitionId);
                  match.addTrace("WorkDefinitionFactory - PeerToPeerWorkflowDefinitionId");
               } else if (AtsWorkItemServiceImpl.get().isOfType(workItem, IAtsDecisionReview.class)) {
                  match = getWorkDefinition(WorkDefinitionDefault.DecisionWorkflowDefinitionId);
                  match.addTrace("WorkDefinitionFactory - DecisionWorkflowDefinitionId");
               }
            }
         }
      }
      return match;
   }

   public static boolean isTaskOverridingItsWorkDefinition(IAtsTask task) throws MultipleAttributesExist, OseeCoreException {
      return AtsWorkDefinitionStore.getService().getWorkDefinitionAttribute(task) != null;
   }

   public static IAtsWorkDefinition copyWorkDefinition(String newName, IAtsWorkDefinition workDef, XResultData resultData) {
      return AtsWorkDefinitionServiceImpl.instance.copyWorkDefinition(newName, workDef, resultData,
         AtsWorkDefinitionStore.getService().getAttributeResolver(),
         AtsWorkDefinitionStore.getService().getUserResolver());
   }

   public static void addWorkDefinition(IAtsWorkDefinition workDef) {
      WorkDefinitionMatch match =
         new WorkDefinitionMatch(workDef.getId(), "programatically added via WorkDefinitionFactory.addWorkDefinition");
      match.setWorkDefinition(workDef);
      workDefIdToWorkDefintion.put(workDef.getName(), match);
   }

   public static void removeWorkDefinition(IAtsWorkDefinition workDef) {
      workDefIdToWorkDefintion.remove(workDef.getName());
   }

   public static Collection<IAtsWorkDefinition> getLoadedWorkDefinitions() {
      List<IAtsWorkDefinition> workDefs = new ArrayList<IAtsWorkDefinition>();
      for (WorkDefinitionMatch match : workDefIdToWorkDefintion.values()) {
         if (match.getWorkDefinition() != null) {
            workDefs.add(match.getWorkDefinition());
         }
      }
      return workDefs;
   }

}
