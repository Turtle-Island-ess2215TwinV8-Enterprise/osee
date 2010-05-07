/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.osee.ats.artifact.ActionableItemArtifact;
import org.eclipse.osee.ats.artifact.TaskArtifact;
import org.eclipse.osee.ats.artifact.TaskableStateMachineArtifact;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.ArtifactType;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.event.FrameworkTransactionData;
import org.eclipse.osee.framework.skynet.core.event.IArtifactsPurgedEventListener;
import org.eclipse.osee.framework.skynet.core.event.IFrameworkTransactionEventListener;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.Sender;
import org.eclipse.osee.framework.skynet.core.event.FrameworkTransactionData.ChangeType;
import org.eclipse.osee.framework.skynet.core.utility.DbUtil;
import org.eclipse.osee.framework.skynet.core.utility.LoadedArtifacts;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkFlowDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinitionFactory;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkPageDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkRuleDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkWidgetDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinition.WriteType;

/**
 * Common cache storage for ATS configuration artifacts:<br>
 * TeamDefinitionArtifact<br>
 * VersionArtifact<br>
 * ActionableItemArtifact<br>
 * All other artifact types will silently not cached
 * 
 * @author Donald G. Dunne
 */
public class AtsCacheManager implements IArtifactsPurgedEventListener, IFrameworkTransactionEventListener {

   private static Map<TaskableStateMachineArtifact, Collection<TaskArtifact>> teamTasksCache =
         new HashMap<TaskableStateMachineArtifact, Collection<TaskArtifact>>();

   public static void start() {
      new AtsCacheManager();
   }

   private AtsCacheManager() {
      OseeEventManager.addPriorityListener(this);
   }

   public static synchronized void decacheTaskArtifacts(TaskableStateMachineArtifact sma) throws OseeCoreException {
      teamTasksCache.remove(sma);
   }

   public static synchronized Collection<TaskArtifact> getTaskArtifacts(TaskableStateMachineArtifact sma) throws OseeCoreException {
      if (!teamTasksCache.containsKey(sma)) {
         Collection<TaskArtifact> taskArtifacts =
               sma.getRelatedArtifacts(AtsRelationTypes.SmaToTask_Task, TaskArtifact.class);
         if (taskArtifacts.size() == 0) {
            return taskArtifacts;
         }
         teamTasksCache.put(sma, taskArtifacts);
      }
      return teamTasksCache.get(sma);
   }

   public static List<Artifact> getArtifactsByName(IArtifactType artifactType, String name) throws OseeCoreException {
      AtsBulkLoad.loadConfig(true);
      return ArtifactCache.getArtifactsByName(artifactType, name);
   }

   public static ActionableItemArtifact getActionableItemByGuid(String guid) throws OseeCoreException {
      AtsBulkLoad.loadConfig(true);
      return (ActionableItemArtifact) ArtifactCache.getActive(guid, AtsUtil.getAtsBranch().getId());
   }

   public static TeamDefinitionArtifact getTeamDefinitionArtifact(String guid) throws OseeCoreException {
      AtsBulkLoad.loadConfig(true);
      return (TeamDefinitionArtifact) ArtifactCache.getActive(guid, AtsUtil.getAtsBranch().getId());
   }

   public static List<Artifact> getArtifactsByActive(ArtifactType artifactType, Active active) throws OseeCoreException {
      AtsBulkLoad.loadConfig(true);
      return AtsUtil.getActive(ArtifactCache.getArtifactsByType(artifactType), active, null);
   }

   public static Artifact getSoleArtifactByName(IArtifactType artifactType, String name) throws OseeCoreException {
      AtsBulkLoad.loadConfig(true);
      List<Artifact> arts = ArtifactCache.getArtifactsByName(artifactType, name);
      if (arts.size() == 1) {
         return arts.iterator().next();
      }
      return null;
   }

   @Override
   public void handleArtifactsPurgedEvent(Sender sender, LoadedArtifacts loadedArtifacts) throws OseeCoreException {
      if (DbUtil.isDbInit()) {
         OseeEventManager.removeListener(this);
         return;
      }
      try {
         for (Artifact artifact : loadedArtifacts.getLoadedArtifacts()) {
            if (artifact.isOfType(WorkRuleDefinition.ARTIFACT_NAME) || artifact.isOfType(WorkPageDefinition.ARTIFACT_NAME) || artifact.isOfType(CoreArtifactTypes.WorkFlowDefinition) || artifact.isOfType(WorkWidgetDefinition.ARTIFACT_NAME)) {
               WorkItemDefinitionFactory.deCache(artifact);
            }
            if (artifact instanceof TaskArtifact) {
               teamTasksCache.remove(artifact.getParent());
            }
            if (artifact instanceof TaskableStateMachineArtifact) {
               teamTasksCache.remove(artifact);
            }
         }
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
      }
   }

   @Override
   public void handleFrameworkTransactionEvent(Sender sender, FrameworkTransactionData transData) throws OseeCoreException {
      if (DbUtil.isDbInit()) {
         OseeEventManager.removeListener(this);
         return;
      }
      if (transData.branchId != AtsUtil.getAtsBranch().getId()) {
         return;
      }
      for (Artifact artifact : transData.cacheDeletedArtifacts) {
         if (artifact.isOfType(WorkRuleDefinition.ARTIFACT_NAME) || artifact.isOfType(WorkPageDefinition.ARTIFACT_NAME) || artifact.isOfType(CoreArtifactTypes.WorkFlowDefinition) || artifact.isOfType(WorkWidgetDefinition.ARTIFACT_NAME)) {
            WorkItemDefinitionFactory.deCache(artifact);
         }
         if (artifact instanceof TaskArtifact) {
            teamTasksCache.remove(artifact.getParent());
         }
         if (artifact instanceof TaskableStateMachineArtifact) {
            teamTasksCache.remove(artifact);
         }
      }
      for (Artifact artifact : transData.cacheAddedArtifacts) {
         if (artifact.isOfType(WorkRuleDefinition.ARTIFACT_NAME)) {
            WorkItemDefinitionFactory.cacheWorkItemDefinitionArtifact(WriteType.Update,
                  new WorkRuleDefinition(artifact), artifact);
         } else if (artifact.isOfType(WorkPageDefinition.ARTIFACT_NAME)) {
            WorkItemDefinitionFactory.cacheWorkItemDefinitionArtifact(WriteType.Update,
                  new WorkPageDefinition(artifact), artifact);
         } else if (artifact.isOfType(WorkWidgetDefinition.ARTIFACT_NAME)) {
            WorkItemDefinitionFactory.cacheWorkItemDefinitionArtifact(WriteType.Update, new WorkWidgetDefinition(
                  artifact), artifact);
         } else if (artifact.isOfType(CoreArtifactTypes.WorkFlowDefinition)) {
            WorkItemDefinitionFactory.cacheWorkItemDefinitionArtifact(WriteType.Update,
                  new WorkFlowDefinition(artifact), artifact);
         }
         if (artifact instanceof TaskArtifact) {
            teamTasksCache.remove(artifact.getParent());
         }
         if (artifact instanceof TaskableStateMachineArtifact) {
            teamTasksCache.remove(artifact);
         }
      }
      for (Artifact artifact : transData.getArtifactsInRelations(ChangeType.All, AtsRelationTypes.SmaToTask_Task)) {
         if (artifact instanceof TaskArtifact) {
            teamTasksCache.remove(artifact.getParent());
         }
         if (artifact instanceof TaskableStateMachineArtifact) {
            teamTasksCache.remove(artifact);
         }
      }
      for (Artifact artifact : transData.getArtifactsInRelations(ChangeType.All, CoreRelationTypes.WorkItem__Child)) {
         if (artifact.isOfType(WorkRuleDefinition.ARTIFACT_NAME)) {
            WorkItemDefinitionFactory.cacheWorkItemDefinitionArtifact(WriteType.Update,
                  new WorkRuleDefinition(artifact), artifact);
         } else if (artifact.isOfType(WorkPageDefinition.ARTIFACT_NAME)) {
            WorkItemDefinitionFactory.cacheWorkItemDefinitionArtifact(WriteType.Update,
                  new WorkPageDefinition(artifact), artifact);
         } else if (artifact.isOfType(WorkWidgetDefinition.ARTIFACT_NAME)) {
            WorkItemDefinitionFactory.cacheWorkItemDefinitionArtifact(WriteType.Update, new WorkWidgetDefinition(
                  artifact), artifact);
         } else if (artifact.isOfType(CoreArtifactTypes.WorkFlowDefinition)) {
            WorkItemDefinitionFactory.cacheWorkItemDefinitionArtifact(WriteType.Update,
                  new WorkFlowDefinition(artifact), artifact);
         }
      }
   }
}
