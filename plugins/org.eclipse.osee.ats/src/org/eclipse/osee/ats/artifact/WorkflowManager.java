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
package org.eclipse.osee.ats.artifact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.RuleDefinitionOption;
import org.eclipse.osee.ats.column.CancelledDateColumn;
import org.eclipse.osee.ats.column.CompletedDateColumn;
import org.eclipse.osee.ats.column.CreatedDateColumn;
import org.eclipse.osee.ats.column.StateColumn;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.core.client.util.WorkflowManagerCore;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.client.workflow.ChangeType;
import org.eclipse.osee.ats.core.client.workflow.ChangeTypeUtil;
import org.eclipse.osee.ats.core.client.workflow.HoursSpentUtil;
import org.eclipse.osee.ats.core.client.workflow.PriorityUtil;
import org.eclipse.osee.ats.core.client.workflow.StateManager;
import org.eclipse.osee.ats.core.client.workflow.log.LogItem;
import org.eclipse.osee.ats.core.config.AtsVersionService;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.workdef.StateXWidgetPage;
import org.eclipse.osee.ats.workflow.ATSXWidgetOptionResolver;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Donald G. Dunne
 */
public class WorkflowManager {

   public static boolean isAssigneeEditable(AbstractWorkflowArtifact awa, boolean privilegedEditEnabled) throws OseeCoreException {
      return !awa.isCompletedOrCancelled() && !awa.isReadOnly() &&
      // and access control writeable
      awa.isAccessControlWrite() && //

      (WorkflowManagerCore.isEditable(awa, awa.getStateDefinition(), privilegedEditEnabled) || //
         // page is define to allow anyone to edit
         awa.getStateDefinition().hasRule(RuleDefinitionOption.AllowAssigneeToAll.name()) ||
         // awa is child of TeamWorkflow that has AllowAssigneeToAll rule
         isParentTeamWorklfowCurrentStateAllowAssigneeToAll(awa) ||
      // team definition has allowed anyone to edit
      awa.teamDefHasRule(RuleDefinitionOption.AllowAssigneeToAll));
   }

   private static boolean isParentTeamWorklfowCurrentStateAllowAssigneeToAll(AbstractWorkflowArtifact awa) throws OseeCoreException {
      TeamWorkFlowArtifact parentTeamArt = awa.getParentTeamWorkflow();
      return (parentTeamArt != null && parentTeamArt.getStateDefinition().hasRule(
         RuleDefinitionOption.AllowAssigneeToAll.name()));
   }

   public static List<TeamWorkFlowArtifact> getAllTeamWorkflowArtifacts() throws OseeCoreException {
      List<TeamWorkFlowArtifact> result = new ArrayList<TeamWorkFlowArtifact>();
      for (IArtifactType artType : TeamWorkFlowManager.getTeamWorkflowArtifactTypes()) {
         List<TeamWorkFlowArtifact> teamArts =
            org.eclipse.osee.framework.jdk.core.util.Collections.castAll(ArtifactQuery.getArtifactListFromType(artType,
               AtsUtil.getAtsBranch()));
         result.addAll(teamArts);
      }
      return result;
   }

   public static Collection<AbstractWorkflowArtifact> getCompletedCancelled(Collection<AbstractWorkflowArtifact> awas) throws OseeCoreException {
      List<AbstractWorkflowArtifact> artifactsToReturn = new ArrayList<AbstractWorkflowArtifact>(awas.size());
      for (AbstractWorkflowArtifact awa : awas) {
         if (awa.isCompletedOrCancelled()) {
            artifactsToReturn.add(awa);
         }
      }
      return artifactsToReturn;
   }

   public static Collection<AbstractWorkflowArtifact> getInWork(Collection<AbstractWorkflowArtifact> awas) throws OseeCoreException {
      List<AbstractWorkflowArtifact> artifactsToReturn = new ArrayList<AbstractWorkflowArtifact>(awas.size());
      for (AbstractWorkflowArtifact awa : awas) {
         if (!awa.isCompletedOrCancelled()) {
            artifactsToReturn.add(awa);
         }
      }
      return artifactsToReturn;
   }

   public static Collection<AbstractWorkflowArtifact> filterOutState(Collection<AbstractWorkflowArtifact> awas, Collection<String> stateNames) {
      List<AbstractWorkflowArtifact> artifactsToReturn = new ArrayList<AbstractWorkflowArtifact>(awas.size());
      for (AbstractWorkflowArtifact awa : awas) {
         if (!stateNames.contains(awa.getStateMgr().getCurrentStateName())) {
            artifactsToReturn.add(awa);
         }
      }
      return artifactsToReturn;
   }

   public static Collection<AbstractWorkflowArtifact> filterOutCompleted(Collection<AbstractWorkflowArtifact> awas) throws OseeCoreException {
      List<AbstractWorkflowArtifact> artifactsToReturn = new ArrayList<AbstractWorkflowArtifact>(awas.size());
      for (AbstractWorkflowArtifact awa : awas) {
         if (!awa.isCompleted()) {
            artifactsToReturn.add(awa);
         }
      }
      return artifactsToReturn;
   }

   public static Collection<AbstractWorkflowArtifact> filterOutCancelled(Collection<AbstractWorkflowArtifact> awas) throws OseeCoreException {
      List<AbstractWorkflowArtifact> artifactsToReturn = new ArrayList<AbstractWorkflowArtifact>(awas.size());
      for (AbstractWorkflowArtifact awa : awas) {
         if (!awa.isCancelled()) {
            artifactsToReturn.add(awa);
         }
      }
      return artifactsToReturn;
   }

   public static Collection<Artifact> filterState(String selectedState, Collection<? extends Artifact> awas) {
      List<Artifact> artifactsToReturn = new ArrayList<Artifact>(awas.size());
      if (!Strings.isValid(selectedState)) {
         artifactsToReturn.addAll(awas);
      } else {
         for (Artifact awa : awas) {
            if (StateColumn.getInstance().getColumnText(awa, null, 0).equals(selectedState)) {
               artifactsToReturn.add(awa);
            }
         }
      }
      return artifactsToReturn;
   }

   public static Collection<AbstractWorkflowArtifact> filterOutTypes(Collection<AbstractWorkflowArtifact> awas, Collection<Class<?>> classes) {
      List<AbstractWorkflowArtifact> artifactsToReturn = new ArrayList<AbstractWorkflowArtifact>(awas.size());
      for (AbstractWorkflowArtifact awa : awas) {
         boolean found = false;
         for (Class<?> clazz : classes) {
            if (clazz.isInstance(awa)) {
               found = true;
            }
         }
         if (!found) {
            artifactsToReturn.add(awa);
         }
      }
      return artifactsToReturn;
   }

   public static Collection<AbstractWorkflowArtifact> getOpenAtDate(Date date, Collection<AbstractWorkflowArtifact> artifacts) throws OseeCoreException {
      List<AbstractWorkflowArtifact> awas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : artifacts) {
         Date createDate = CreatedDateColumn.getDate(awa);
         Date completedCancelDate = null;
         if (awa.isCompletedOrCancelled()) {
            if (awa.isCancelled()) {
               completedCancelDate = CancelledDateColumn.getDate(awa);
            } else {
               completedCancelDate = CompletedDateColumn.getDate(awa);
            }
         }
         if (createDate.before(date) && (completedCancelDate == null || completedCancelDate.after(date))) {
            awas.add(awa);
         }
      }
      return awas;
   }

   public static Collection<AbstractWorkflowArtifact> getCompletedCancelledBetweenDate(Date startDate, Date endDate, Collection<AbstractWorkflowArtifact> artifacts) throws OseeCoreException {
      List<AbstractWorkflowArtifact> awas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : artifacts) {
         Date completedCancelDate = null;
         if (awa.isCompletedOrCancelled()) {
            if (awa.isCancelled()) {
               completedCancelDate = CancelledDateColumn.getDate(awa);
            } else {
               completedCancelDate = CompletedDateColumn.getDate(awa);
            }
         }
         if (completedCancelDate == null) {
            continue;
         }
         if (completedCancelDate.after(startDate) && completedCancelDate.before(endDate)) {
            awas.add(awa);
         }
      }
      return awas;
   }

   public static Double getHoursSpent(Collection<AbstractWorkflowArtifact> artifacts) throws OseeCoreException {
      Double hoursSpent = 0.0;
      for (AbstractWorkflowArtifact awa : artifacts) {
         hoursSpent += HoursSpentUtil.getHoursSpentTotal(awa);
      }
      return hoursSpent;
   }

   public static Collection<AbstractWorkflowArtifact> getStateAtDate(Date date, Collection<String> states, Collection<AbstractWorkflowArtifact> artifacts) throws OseeCoreException {
      List<AbstractWorkflowArtifact> awas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : artifacts) {
         Date createDate = CreatedDateColumn.getDate(awa);
         if (createDate.after(date)) {
            continue;
         }
         // Find state at date
         String currentState = awa.getStateMgr().getCurrentStateName();
         for (LogItem item : awa.getLog().getLogItems()) {
            if (item.getDate().before(date)) {
               currentState = item.getState();
            }
         }
         if (states.contains(currentState)) {
            awas.add(awa);
         }
      }
      return awas;
   }

   /**
    * Returns awa if change type, or parent team workflow's change type is in specified set
    */
   public static Collection<AbstractWorkflowArtifact> getChangeType(Collection<ChangeType> changeTypes, Collection<AbstractWorkflowArtifact> artifacts) throws OseeCoreException {
      List<AbstractWorkflowArtifact> awas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : artifacts) {
         TeamWorkFlowArtifact teamArt = awa.getParentTeamWorkflow();
         if (changeTypes.contains(ChangeTypeUtil.getChangeType(teamArt))) {
            awas.add(awa);
         }
      }
      return awas;

   }

   /**
    * Returns awa if priority type, or parent team workflow's priority type is in specified set
    */
   public static Collection<AbstractWorkflowArtifact> getPriorityType(Collection<String> priorityTypes, Collection<AbstractWorkflowArtifact> artifacts) throws OseeCoreException {
      List<AbstractWorkflowArtifact> awas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : artifacts) {
         TeamWorkFlowArtifact teamArt = awa.getParentTeamWorkflow();
         if (priorityTypes.contains(PriorityUtil.getPriorityStr(teamArt))) {
            awas.add(awa);
         }
      }
      return awas;

   }

   public static Collection<AbstractWorkflowArtifact> getTeamDefinitionWorkflows(Collection<? extends Artifact> artifacts, Collection<IAtsTeamDefinition> teamDefs) throws OseeCoreException {
      List<AbstractWorkflowArtifact> returnawas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : getAwas(artifacts)) {
         if (awa.getParentTeamWorkflow() == null) {
            continue;
         }
         if (teamDefs.contains(awa.getParentTeamWorkflow().getTeamDefinition())) {
            returnawas.add(awa);
         }
      }
      return returnawas;
   }

   public static Collection<AbstractWorkflowArtifact> getVersionWorkflows(Collection<? extends Artifact> artifacts, Collection<IAtsVersion> versionArts) throws OseeCoreException {
      List<AbstractWorkflowArtifact> returnawas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : getAwas(artifacts)) {
         if (awa.getParentTeamWorkflow() == null) {
            continue;
         }
         if (!AtsVersionService.get().hasTargetedVersion(awa)) {
            continue;
         }
         if (versionArts.contains(AtsVersionService.get().getTargetedVersion(awa))) {
            returnawas.add(awa);
         }
      }
      return returnawas;
   }

   public static Collection<AbstractWorkflowArtifact> getAwas(Collection<? extends Artifact> artifacts) {
      return Collections.castMatching(AbstractWorkflowArtifact.class, artifacts);
   }

   public static StateManager getStateManager(Artifact artifact) {
      return cast(artifact).getStateMgr();
   }

   public static AbstractWorkflowArtifact cast(Artifact artifact) {
      if (artifact instanceof AbstractWorkflowArtifact) {
         return (AbstractWorkflowArtifact) artifact;
      }
      return null;
   }

   public static StateXWidgetPage getCurrentAtsWorkPage(AbstractWorkflowArtifact awa) {
      for (StateXWidgetPage statePage : getStatePagesOrderedByDefaultToState(awa)) {
         if (awa.getStateMgr().getCurrentStateName().equals(statePage.getName())) {
            return statePage;
         }
      }
      return null;
   }

   public static List<StateXWidgetPage> getStatePagesOrderedByOrdinal(AbstractWorkflowArtifact awa) {
      List<StateXWidgetPage> statePages = new ArrayList<StateXWidgetPage>();
      for (IAtsStateDefinition stateDefinition : AtsClientService.get().getWorkDefinitionAdmin().getStatesOrderedByOrdinal(
         awa.getWorkDefinition())) {
         try {
            StateXWidgetPage statePage =
               new StateXWidgetPage(awa.getWorkDefinition(), stateDefinition, null,
                  ATSXWidgetOptionResolver.getInstance());
            statePages.add(statePage);
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      return statePages;

   }

   public static List<StateXWidgetPage> getStatePagesOrderedByDefaultToState(AbstractWorkflowArtifact awa) {
      List<StateXWidgetPage> statePages = new ArrayList<StateXWidgetPage>();
      for (IAtsStateDefinition stateDefinition : AtsClientService.get().getWorkDefinitionAdmin().getStatesOrderedByDefaultToState(
         awa.getWorkDefinition())) {
         try {
            StateXWidgetPage statePage =
               new StateXWidgetPage(awa.getWorkDefinition(), stateDefinition, null,
                  ATSXWidgetOptionResolver.getInstance());
            statePages.add(statePage);
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      return statePages;
   }
}
