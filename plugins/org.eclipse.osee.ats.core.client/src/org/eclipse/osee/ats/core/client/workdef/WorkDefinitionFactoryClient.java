/*
 * Created on Aug 2, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.client.workdef;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.task.IAtsTask;
import org.eclipse.osee.ats.api.util.WorkDefinitionMatch;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.client.team.TeamWorkflowProviders;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.ITeamWorkflowProvider;
import org.eclipse.osee.ats.core.workdef.AtsWorkDefinitionService;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.MultipleAttributesExist;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.utility.ElapsedTime;

public class WorkDefinitionFactoryClient {

   // Cache the WorkDefinition used for each AbstractWorkflowId so don't have to recompute each time
   private static final Map<Integer, WorkDefinitionMatch> awaArtIdToWorkDefinition =
      new HashMap<Integer, WorkDefinitionMatch>();
   public static Set<String> errorDisplayed = new HashSet<String>();

   public static void clearCaches() {
      awaArtIdToWorkDefinition.clear();
      AtsWorkDefinitionService.get().clearCaches();
   }

   public static Set<IAtsWorkDefinition> loadAllDefinitions() throws OseeCoreException {
      ElapsedTime time = new ElapsedTime("  - Load All Work Definitions");

      Set<IAtsWorkDefinition> workDefs = new HashSet<IAtsWorkDefinition>();
      // This load is faster than loading each by artifact type
      for (Artifact art : ArtifactQuery.getArtifactListFromType(AtsArtifactTypes.WorkDefinition,
         AtsUtilCore.getAtsBranch(), DeletionFlag.EXCLUDE_DELETED)) {
         try {
            XResultData resultData = new XResultData(false);
            IAtsWorkDefinition workDef = AtsWorkDefinitionService.get().getWorkDef(art.getName(), resultData);
            if (!resultData.isEmpty()) {
               OseeLog.log(Activator.class, Level.SEVERE, resultData.toString());
            }
            if (workDef == null) {
               OseeLog.logf(Activator.class, Level.SEVERE, "Null WorkDef loaded for Artifact [%s]",
                  art.toStringWithId());
            } else {
               workDefs.add(workDef);
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE,
               "Error loading WorkDefinition from artifact " + art.toStringWithId(), ex);
         }
      }
      time.end();
      return workDefs;
   }

   public static void addWorkDefinition(IAtsWorkDefinition workDef) {
      AtsWorkDefinitionService.get().addWorkDefinition(workDef);
   }

   public static void removeWorkDefinition(IAtsWorkDefinition workDef) {
      AtsWorkDefinitionService.get().removeWorkDefinition(workDef);
   }

   public static WorkDefinitionMatch getWorkDefinition(Artifact artifact) throws OseeCoreException {
      if (artifact instanceof IAtsWorkItem) {
         if (!awaArtIdToWorkDefinition.containsKey(artifact.getArtId())) {
            WorkDefinitionMatch match = AtsWorkDefinitionService.get().getWorkDefinition((IAtsWorkItem) artifact);
            awaArtIdToWorkDefinition.put(artifact.getArtId(), match);
         }
         return awaArtIdToWorkDefinition.get(artifact.getArtId());
      }
      throw new OseeArgumentException(
         "Unexpected artifact sent for retrieval of Work Defintion " + artifact.toStringWithId());
   }

   public static Collection<IAtsWorkDefinition> getLoadedWorkDefinitions() {
      return AtsWorkDefinitionService.get().getLoadedWorkDefinitions();
   }

   public static WorkDefinitionMatch getWorkDefinition(String id) {
      return AtsWorkDefinitionService.get().getWorkDefinitionMatch(id);
   }

   public static WorkDefinitionMatch getWorkDefinitionFromTaskViaProviders(IAtsTeamWorkflow teamWf) throws OseeCoreException {
      WorkDefinitionMatch match = new WorkDefinitionMatch();
      for (ITeamWorkflowProvider provider : TeamWorkflowProviders.getAtsTeamWorkflowProviders()) {
         String workFlowDefId = provider.getRelatedTaskWorkflowDefinitionId(teamWf);
         if (Strings.isValid(workFlowDefId)) {
            match = WorkDefinitionFactoryClient.getWorkDefinition(workFlowDefId);
            match.addTrace((String.format("from provider [%s] for id [%s]", provider.getClass().getSimpleName(),
               workFlowDefId)));
            break;
         }
      }
      return match;
   }

   public static WorkDefinitionMatch getWorkDefinitionFromProviders(IAtsWorkItem workItem) throws OseeCoreException {
      WorkDefinitionMatch match = new WorkDefinitionMatch();
      for (ITeamWorkflowProvider provider : TeamWorkflowProviders.getAtsTeamWorkflowProviders()) {
         String workFlowDefId = provider.getWorkflowDefinitionId(workItem);
         if (Strings.isValid(workFlowDefId)) {
            match = WorkDefinitionFactoryClient.getWorkDefinition(workFlowDefId);
         }
      }
      return match;
   }

   public static WorkDefinitionMatch getWorkDefinitionForTask(IAtsTask taskToMove) throws OseeCoreException {
      return AtsWorkDefinitionService.get().getWorkDefinitionForTask(taskToMove);
   }

   public static WorkDefinitionMatch getWorkDefinitionForTaskNotYetCreated(IAtsTeamWorkflow teamWf) throws OseeCoreException {
      return AtsWorkDefinitionService.get().getWorkDefinitionForTaskNotYetCreated(teamWf);
   }

   public static IAtsWorkDefinition copyWorkDefinition(String name, IAtsWorkDefinition workDef, XResultData resultData) throws OseeCoreException {
      return AtsWorkDefinitionService.get().copyWorkDefinition(name, workDef, resultData);
   }

   public static boolean isTaskOverridingItsWorkDefinition(IAtsTask task) throws MultipleAttributesExist, OseeCoreException {
      return AtsWorkDefinitionService.get().isTaskOverridingItsWorkDefinition(task);
   }
}
