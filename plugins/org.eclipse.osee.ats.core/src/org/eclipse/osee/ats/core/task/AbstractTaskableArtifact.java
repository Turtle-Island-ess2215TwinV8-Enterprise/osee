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
package org.eclipse.osee.ats.core.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.core.internal.Activator;
import org.eclipse.osee.ats.core.util.AtsCacheManager;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.core.workdef.StateDefinition;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.workflow.PercentCompleteTotalUtil;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.ats.shared.AtsAttributeTypes;
import org.eclipse.osee.ats.shared.AtsRelationTypes;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.core.util.IWorkPage;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractTaskableArtifact extends AbstractWorkflowArtifact {

   public AbstractTaskableArtifact(ArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, IArtifactType artifactType) throws OseeCoreException {
      super(parentFactory, guid, humanReadableId, branch, artifactType);
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
   public void transitioned(StateDefinition fromState, StateDefinition toState, Collection<? extends IBasicUser> toAssignees, SkynetTransaction transaction) throws OseeCoreException {
      super.transitioned(fromState, toState, toAssignees, transaction);
      for (TaskArtifact taskArt : getTaskArtifacts()) {
         taskArt.parentWorkFlowTransitioned(fromState, toState, toAssignees, transaction);
      }
   }

   public Collection<TaskArtifact> getTaskArtifacts() throws OseeCoreException {
      return AtsCacheManager.getTaskArtifacts(this);
   }

   public Collection<TaskArtifact> getTaskArtifactsSorted() throws OseeCoreException {
      return AtsCacheManager.getTaskArtifacts(this);
   }

   public Collection<TaskArtifact> getTaskArtifactsFromCurrentState() throws OseeCoreException {
      return getTaskArtifacts(getStateMgr().getCurrentState());
   }

   public Collection<TaskArtifact> getTaskArtifacts(IWorkPage state) throws OseeCoreException {
      List<TaskArtifact> arts = new ArrayList<TaskArtifact>();
      for (TaskArtifact taskArt : getTaskArtifacts()) {
         if (taskArt.getSoleAttributeValue(AtsAttributeTypes.RelatedToState, "").equals(state.getPageName())) {
            arts.add(taskArt);
         }
      }
      return arts;
   }

   public boolean hasTaskArtifacts() {
      return getRelatedArtifactsCount(AtsRelationTypes.SmaToTask_Task) > 0;
   }

   public TaskArtifact createNewTask(String title, Date createdDate, IBasicUser createdBy) throws OseeCoreException {
      return createNewTask(Arrays.asList((IBasicUser) UserManager.getUser()), title, createdDate, createdBy);
   }

   public TaskArtifact createNewTask(Collection<IBasicUser> assignees, String title, Date createdDate, IBasicUser createdBy) throws OseeCoreException {
      TaskArtifact taskArt = null;
      taskArt =
         (TaskArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.Task, AtsUtilCore.getAtsBranch(), title);

      addRelation(AtsRelationTypes.SmaToTask_Task, taskArt);
      taskArt.initializeNewStateMachine(assignees, new Date(), (createdBy == null ? UserManager.getUser() : createdBy));

      // Set parent state task is related to
      taskArt.setSoleAttributeValue(AtsAttributeTypes.RelatedToState, getStateMgr().getCurrentStateName());

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

   public Result areTasksComplete(IWorkPage state) {
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
   public double getEstimatedHoursFromTasks(IWorkPage relatedToState) throws OseeCoreException {
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
   public double getRemainHoursFromTasks(IWorkPage relatedToState) throws OseeCoreException {
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
   public int getPercentCompleteFromTasks(IWorkPage relatedToState) throws OseeCoreException {
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

   public Collection<TaskArtifact> createTasks(List<String> titles, List<IBasicUser> assignees, Date createdDate, IBasicUser createdBy, SkynetTransaction transaction) throws OseeCoreException {
      List<TaskArtifact> tasks = new ArrayList<TaskArtifact>();
      for (String title : titles) {
         TaskArtifact taskArt = createNewTask(title, createdDate, createdBy);
         if (assignees != null && !assignees.isEmpty()) {
            Set<IBasicUser> users = new HashSet<IBasicUser>(); // NOPMD by b0727536 on 9/29/10 8:51 AM
            for (IBasicUser art : assignees) {
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