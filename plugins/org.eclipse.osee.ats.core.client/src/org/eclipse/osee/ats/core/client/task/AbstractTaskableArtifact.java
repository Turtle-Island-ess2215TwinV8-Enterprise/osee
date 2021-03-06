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
package org.eclipse.osee.ats.core.client.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.ats.core.client.util.AtsTaskCache;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.client.workflow.PercentCompleteTotalUtil;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractTaskableArtifact extends AbstractWorkflowArtifact {

   public AbstractTaskableArtifact(String guid, String humanReadableId, Branch branch, IArtifactType artifactType) throws OseeCoreException {
      super(guid, humanReadableId, branch, artifactType);
   }

   @Override
   public void getSmaArtifactsOneLevel(AbstractWorkflowArtifact smaArtifact, Set<Artifact> artifacts) throws OseeCoreException {
      super.getSmaArtifactsOneLevel(smaArtifact, artifacts);
      artifacts.addAll(getTaskArtifacts());
   }

   @Override
   public void atsDelete(Set<Artifact> deleteArts, Map<Artifact, Object> allRelated) throws OseeCoreException {
      super.atsDelete(deleteArts, allRelated);
      for (TaskArtifact taskArt : getTaskArtifacts()) {
         taskArt.atsDelete(deleteArts, allRelated);
      }
   }

   @Override
   public void transitioned(IAtsStateDefinition fromState, IAtsStateDefinition toState, Collection<? extends IAtsUser> toAssignees, SkynetTransaction transaction) throws OseeCoreException {
      super.transitioned(fromState, toState, toAssignees, transaction);
      for (TaskArtifact taskArt : getTaskArtifacts()) {
         taskArt.parentWorkFlowTransitioned(fromState, toState, toAssignees, transaction);
      }
   }

   public Collection<TaskArtifact> getTaskArtifacts() throws OseeCoreException {
      return AtsTaskCache.getTaskArtifacts(this);
   }

   public Collection<TaskArtifact> getTaskArtifactsSorted() throws OseeCoreException {
      return AtsTaskCache.getTaskArtifacts(this);
   }

   public Collection<TaskArtifact> getTaskArtifactsFromCurrentState() throws OseeCoreException {
      return getTaskArtifacts(getStateMgr().getCurrentState());
   }

   public Collection<TaskArtifact> getTaskArtifacts(IStateToken state) throws OseeCoreException {
      List<TaskArtifact> arts = new ArrayList<TaskArtifact>();
      for (TaskArtifact taskArt : getTaskArtifacts()) {
         if (taskArt.getSoleAttributeValue(AtsAttributeTypes.RelatedToState, "").equals(state.getName())) {
            arts.add(taskArt);
         }
      }
      return arts;
   }

   public boolean hasTaskArtifacts() throws OseeCoreException {
      return getRelatedArtifactsCount(AtsRelationTypes.TeamWfToTask_Task) > 0;
   }

   public TaskArtifact createNewTask(String title, Date createdDate, IAtsUser createdBy) throws OseeCoreException {
      return createNewTask(Arrays.asList(AtsClientService.get().getUserAdmin().getCurrentUser()), title, createdDate, createdBy);
   }

   public TaskArtifact createNewTask(List<? extends IAtsUser> assignees, String title, Date createdDate, IAtsUser createdBy) throws OseeCoreException {
      TaskArtifact taskArt = null;
      taskArt =
         (TaskArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.Task, AtsUtilCore.getAtsBranch(), title);

      addRelation(AtsRelationTypes.TeamWfToTask_Task, taskArt);
      taskArt.initializeNewStateMachine(assignees, new Date(),
         (createdBy == null ? AtsClientService.get().getUserAdmin().getCurrentUser() : createdBy));

      // Set parent state task is related to
      taskArt.setSoleAttributeValue(AtsAttributeTypes.RelatedToState, getStateMgr().getCurrentStateName());
      AtsTaskCache.decache(this);
      return taskArt;
   }

   public Result areTasksComplete() {
      try {
         for (TaskArtifact taskArt : getTaskArtifacts()) {
            if (taskArt.isInWork()) {
               return new Result(false, "Task " + taskArt.getGuid() + " Not Complete");
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return new Result(false, "Exception " + ex.getLocalizedMessage());
      }
      return Result.TrueResult;
   }

   public Result areTasksComplete(IStateToken state) {
      try {
         for (TaskArtifact taskArt : getTaskArtifacts(state)) {
            if (taskArt.isInWork()) {
               return new Result(false, "Task " + taskArt.getGuid() + " Not Complete");
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return new Result(false, "Exception " + ex.getLocalizedMessage());
      }
      return Result.TrueResult;
   }

   public int getNumTasksInWork() {
      int num = 0;
      try {
         for (TaskArtifact taskArt : getTaskArtifacts()) {
            if (taskArt.isInWork()) {
               num++;
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return num;
   }

   /**
    * Return Estimated Task Hours of "Related to State" stateName
    * 
    * @param relatedToState state name of parent workflow's state
    * @return Returns the Estimated Hours
    */
   @Override
   public double getEstimatedHoursFromTasks(IStateToken relatedToState) throws OseeCoreException {
      double hours = 0;
      for (TaskArtifact taskArt : getTaskArtifacts(relatedToState)) {
         hours += taskArt.getEstimatedHoursTotal();
      }
      return hours;
   }

   /**
    * Return Estimated Hours for all tasks
    */
   @Override
   public double getEstimatedHoursFromTasks() throws OseeCoreException {
      double hours = 0;
      for (TaskArtifact taskArt : getTaskArtifacts()) {
         hours += taskArt.getEstimatedHoursFromArtifact();
      }
      return hours;

   }

   /**
    * Return Remain Task Hours of "Related to State" stateName
    * 
    * @param relatedToState state name of parent workflow's state
    * @return Returns the Remain Hours
    */
   public double getRemainHoursFromTasks(IStateToken relatedToState) throws OseeCoreException {
      double hours = 0;
      for (TaskArtifact taskArt : getTaskArtifacts(relatedToState)) {
         hours += taskArt.getRemainHoursFromArtifact();
      }
      return hours;
   }

   /**
    * Return Remain Hours for all tasks
    */
   public double getRemainHoursFromTasks() throws OseeCoreException {
      double hours = 0;
      for (TaskArtifact taskArt : getTaskArtifacts()) {
         hours += taskArt.getRemainHoursFromArtifact();
      }
      return hours;

   }

   /**
    * Return Total Percent Complete / # Tasks for "Related to State" stateName
    * 
    * @param relatedToState state name of parent workflow's state
    * @return Returns the Percent Complete.
    */
   public int getPercentCompleteFromTasks(IStateToken relatedToState) throws OseeCoreException {
      int spent = 0;
      Collection<TaskArtifact> taskArts = getTaskArtifacts(relatedToState);
      for (TaskArtifact taskArt : taskArts) {
         spent += PercentCompleteTotalUtil.getPercentCompleteTotal(taskArt);
      }
      if (spent == 0) {
         return 0;
      }
      return spent / taskArts.size();
   }

   public Collection<TaskArtifact> createTasks(List<String> titles, List<IAtsUser> assignees, Date createdDate, IAtsUser createdBy, SkynetTransaction transaction) throws OseeCoreException {
      List<TaskArtifact> tasks = new ArrayList<TaskArtifact>();
      for (String title : titles) {
         TaskArtifact taskArt = createNewTask(title, createdDate, createdBy);
         if (assignees != null && !assignees.isEmpty()) {
            Set<IAtsUser> users = new HashSet<IAtsUser>(); // NOPMD by b0727536 on 9/29/10 8:51 AM
            for (IAtsUser art : assignees) {
               users.add(art);
            }
            taskArt.getStateMgr().setAssignees(users);
         }
         tasks.add(taskArt);
         taskArt.persist(transaction);
      }
      return tasks;
   }

}