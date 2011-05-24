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

package org.eclipse.osee.ats.core.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.core.internal.Activator;
import org.eclipse.osee.ats.core.team.SimpleTeamState;
import org.eclipse.osee.ats.core.team.TeamState;
import org.eclipse.osee.ats.core.type.AtsAttributeTypes;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.core.workdef.StateDefinition;
import org.eclipse.osee.ats.core.workflow.log.LogItem;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.core.util.IWorkPage;
import org.eclipse.osee.framework.core.util.WorkPageType;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;

/**
 * @author Donald G. Dunne
 */
public class StateManager {

   private final XCurrentStateDam currentStateDam;
   private final XStateDam stateDam;
   private final AbstractWorkflowArtifact sma;
   private static final Set<String> notValidAttributeType = new HashSet<String>();

   public StateManager(AbstractWorkflowArtifact sma) {
      super();
      this.sma = sma;
      currentStateDam = new XCurrentStateDam(sma);
      stateDam = new XStateDam(sma);
   }

   /**
    * Get state and create if not there.
    */
   private SMAState getSMAState(IWorkPage state, boolean create) throws OseeCoreException {
      if (currentStateDam.getState().getName().equals(state.getPageName())) {
         return currentStateDam.getState();
      } else {
         return stateDam.getState(state, create);
      }
   }

   public boolean isAnyStateHavePercentEntered() throws OseeCoreException {
      if (currentStateDam.getState().getPercentComplete() > 0) {
         return true;
      }
      for (SMAState state : stateDam.getStates()) {
         if (state.getPercentComplete() > 0) {
            return true;
         }
      }
      return false;
   }

   public boolean isInState(IWorkPage state) {
      return (getCurrentStateName().equals(state.getPageName()));
   }

   /**
    * Discouraged Access. This method should not normally be called except in cases were state data is being manually
    * created.
    */
   public void internalCreateIfNotExists(IWorkPage state) throws OseeCoreException {
      if (isStateVisited(state)) {
         return;
      }
      SMAState smaState = getSMAState(state, true);
      putState(smaState);
   }

   /**
    * @return true if UnAssigned user is currently an assignee
    */
   public boolean isUnAssigned() throws OseeCoreException {
      return getAssignees().contains(UserManager.getUser(SystemUser.UnAssigned));
   }

   public boolean isUnAssignedSolely() throws OseeCoreException {
      return getAssignees().size() == 1 && isUnAssigned();
   }

   /**
    * Return Hours Spent for State
    * 
    * @return hours spent or 0 if none
    */
   public double getHoursSpent(IWorkPage state) throws OseeCoreException {
      SMAState smaState = getSMAState(state, false);
      if (smaState == null) {
         return 0.0;
      }
      return smaState.getHoursSpent();
   }

   public double getHoursSpent() throws OseeCoreException {
      return getHoursSpent(getCurrentState());
   }

   /**
    * Return Percent Complete for State
    * 
    * @return percent complete or 0 if none
    */
   public int getPercentComplete(IWorkPage teamState) throws OseeCoreException {
      if (teamState.getWorkPageType().isCompletedOrCancelledPage()) {
         return 100;
      }
      SMAState state = getSMAState(teamState, false);
      if (state == null) {
         return 0;
      }
      return state.getPercentComplete();

   }

   public int getPercentComplete() throws OseeCoreException {
      return getPercentComplete(getCurrentState());
   }

   public String getCurrentStateName() {
      try {
         return currentStateDam.getState().getName();
      } catch (OseeCoreException ex) {
         return ex.getLocalizedMessage();
      }
   }

   public IWorkPage getCurrentState() {
      return new SimpleTeamState(getCurrentStateName(), getCurrentWorkPageType());
   }

   public WorkPageType getCurrentWorkPageType() {
      try {
         if (sma.isAttributeTypeValid(AtsAttributeTypes.CurrentStateType)) {
            // backward compatibility
            if (sma.getSoleAttributeValueAsString(AtsAttributeTypes.CurrentStateType, null) == null) {
               if (getCurrentStateName().equals(TeamState.Completed.getPageName())) {
                  return WorkPageType.Completed;
               } else if (getCurrentStateName().equals(TeamState.Cancelled.getPageName())) {
                  return WorkPageType.Cancelled;
               } else {
                  return WorkPageType.Working;
               }
            } else {
               return WorkPageType.valueOf(sma.getSoleAttributeValueAsString(AtsAttributeTypes.CurrentStateType, null));
            }
         } else {
            // display console error, but only once
            if (!notValidAttributeType.contains(sma.getArtifactTypeName())) {
               notValidAttributeType.add(sma.getArtifactTypeName());
               System.err.println("CurrentStateType not valid for " + sma.getArtifactTypeName());
            }
            // TODO get rid of this once database configured for new types (or leave for backward compatibility?
            if (getCurrentStateName().equals(TeamState.Completed.getPageName())) {
               return WorkPageType.Completed;
            } else if (getCurrentStateName().equals(TeamState.Cancelled.getPageName())) {
               return WorkPageType.Cancelled;
            } else {
               return WorkPageType.Working;
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }

   public String getAssigneesStr() throws OseeCoreException {
      return Artifacts.toString("; ", sma.getStateMgr().getAssignees());
   }

   public String getAssigneesStr(int length) throws OseeCoreException {
      String str = getAssigneesStr();
      if (str.length() > length) {
         return str.substring(0, length - 1) + "...";
      }
      return str;
   }

   public Collection<? extends IBasicUser> getAssignees() throws OseeCoreException {
      return getAssignees(getCurrentState());
   }

   public Collection<? extends IBasicUser> getAssignees(IWorkPage state) throws OseeCoreException {
      SMAState smaState = getSMAState(state, false);
      if (smaState == null) {
         return Collections.emptyList();
      } else {
         return smaState.getAssignees();
      }
   }

   public void updateMetrics(double additionalHours, int percentComplete, boolean logMetrics) throws OseeCoreException {
      updateMetrics(getCurrentState(), additionalHours, percentComplete, logMetrics);
   }

   public void updateMetrics(IWorkPage state, double additionalHours, int percentComplete, boolean logMetrics) throws OseeCoreException {
      if (sma.isInState(state)) {
         currentStateDam.updateMetrics(additionalHours, percentComplete, logMetrics);
      } else {
         stateDam.updateMetrics(state, additionalHours, percentComplete, logMetrics);
      }
   }

   public void setMetrics(double hours, int percentComplete, boolean logMetrics, User user, Date date) throws OseeCoreException {
      setMetrics(getCurrentState(), hours, percentComplete, logMetrics, user, date);
   }

   public void setMetrics(IWorkPage state, double hours, int percentComplete, boolean logMetrics, User user, Date date) throws OseeCoreException {
      if (state.getPageName().equals(getCurrentStateName())) {
         currentStateDam.setMetrics(hours, percentComplete, logMetrics, user, date);
      } else {
         stateDam.setMetrics(state, hours, percentComplete, logMetrics, user, date);
      }
   }

   /**
    * Sets the assignees as attributes and relations AND writes to SMA. Does not persist.
    */
   public void setAssignees(Collection<IBasicUser> assignees) throws OseeCoreException {
      SMAState state = getSMAState(getCurrentState(), false);
      state.setAssignees(assignees);
      putState(state);
   }

   /**
    * Sets the assignee AND writes to SMA. Does not persist.
    */
   public void setAssignee(IWorkPage state, User assignee) throws OseeCoreException {
      if (!isStateVisited(state)) {
         throw new OseeArgumentException("State [%s] does not exist.", state);
      }
      SMAState smaState = getSMAState(state, false);
      smaState.setAssignee(assignee);
      putState(smaState);
   }

   /**
    * Sets the assignee AND writes to SMA. Does not persist.
    */
   public void setAssignee(User assignee) throws OseeCoreException {
      SMAState smaState = getSMAState(getCurrentState(), false);
      smaState.setAssignee(assignee);
      putState(smaState);
   }

   /**
    * Removes the assignee from stateName state AND writes to SMA. Does not persist.
    */
   public void removeAssignee(IWorkPage state, User assignee) throws OseeCoreException {
      if (!isStateVisited(state)) {
         return;
      }
      SMAState smaState = getSMAState(state, false);
      smaState.removeAssignee(assignee);
      putState(smaState);
   }

   /**
    * Removes the assignee AND writes to SMA. Does not persist.
    */
   public void removeAssignee(User assignee) throws OseeCoreException {
      SMAState smaState = getSMAState(getCurrentState(), false);
      smaState.removeAssignee(assignee);
      putState(smaState);
   }

   /**
    * Adds the assignee AND writes to SMA. Does not persist. Will remove UnAssigned user if another assignee exists.
    */
   public void addAssignee(IBasicUser assignee) throws OseeCoreException {
      SMAState smaState = getSMAState(getCurrentState(), false);
      smaState.addAssignee(assignee);
      if (smaState.getAssignees().size() > 1 && smaState.getAssignees().contains(
         UserManager.getUser(SystemUser.UnAssigned))) {
         smaState.removeAssignee(UserManager.getUser(SystemUser.UnAssigned));
      }
      putState(smaState);
   }

   /**
    * Removes ALL assignees AND writes to SMA. Does not persist.
    */
   public void clearAssignees() throws OseeCoreException {
      SMAState smaState = getSMAState(getCurrentState(), false);
      smaState.clearAssignees();
      putState(smaState);
   }

   public boolean isStateVisited(IWorkPage state) {
      return getVisitedStateNames().contains(state.getPageName());
   }

   public void transitionHelper(Collection<? extends IBasicUser> toAssignees, StateDefinition fromState, StateDefinition toState, String cancelReason) throws OseeCoreException {
      // Set XCurrentState info to XState
      stateDam.setState(currentStateDam.getState());

      // Set XCurrentState; If been to this state, copy state info from prev state; else create new
      SMAState previousState = stateDam.getState(toState, false);
      if (previousState == null) {
         currentStateDam.setState(new SMAState(toState, toAssignees));
      } else {
         List<IBasicUser> previousAssignees = new ArrayList<IBasicUser>();
         previousAssignees.addAll(previousState.getAssignees());
         List<IBasicUser> nextAssignees = new ArrayList<IBasicUser>();
         nextAssignees.addAll(toAssignees);
         if (!org.eclipse.osee.framework.jdk.core.util.Collections.isEqual(previousAssignees, nextAssignees)) {
            previousState.setAssignees(nextAssignees);
         }
         currentStateDam.setState(previousState);
      }
      sma.setSoleAttributeValue(AtsAttributeTypes.CurrentStateType, toState.getWorkPageType().name());
   }

   /**
    * Initializes state machine and sets the current state to stateName
    */
   public void initializeStateMachine(IWorkPage state) throws OseeCoreException {
      initializeStateMachine(state, null);
   }

   /**
    * Initializes state machine and sets the current state to stateName
    */
   public void initializeStateMachine(IWorkPage state, Collection<IBasicUser> assignees) throws OseeCoreException {
      SMAState smaState = null;
      if (getVisitedStateNames().contains(state.getPageName())) {
         smaState = getSMAState(state, false);
      } else {
         if (assignees == null) {
            List<IBasicUser> assigned = new ArrayList<IBasicUser>();
            if (state.isWorkingPage()) {
               assigned.add(UserManager.getUser());
            }
            smaState = new SMAState(state, assigned);
         } else {
            smaState = new SMAState(state, assignees);
         }
      }
      currentStateDam.setState(smaState);
      if (sma.isAttributeTypeValid(AtsAttributeTypes.CurrentStateType)) {
         sma.setSoleAttributeValue(AtsAttributeTypes.CurrentStateType, state.getWorkPageType().name());
      }
   }

   private void putState(SMAState state) throws OseeCoreException {
      if (getCurrentStateName().equals(state.getName())) {
         currentStateDam.setState(state);
      } else {
         stateDam.setState(state);
      }
   }

   public Collection<String> getVisitedStateNames() {
      Set<String> names = new HashSet<String>();
      for (SMAState state : stateDam.getStates()) {
         names.add(state.getName());
      }
      names.add(getCurrentStateName());
      return names;
   }

   public long getTimeInState() throws OseeCoreException {
      return getTimeInState(getCurrentState());
   }

   public long getTimeInState(IWorkPage state) throws OseeCoreException {
      if (state == null) {
         return 0;
      }
      LogItem logItem = sma.getStateStartedData(state);
      if (logItem == null) {
         return 0;
      }
      return new Date().getTime() - logItem.getDate().getTime();
   }

   /**
    * return currently assigned state machine artifacts
    */
   public static Set<Artifact> getAssigned(User user) throws OseeCoreException {
      return getAssigned(user, null);
   }

   /**
    * return currently assigned state machine artifacts that match clazz
    * 
    * @param clazz to match or all if null
    */
   public static Set<Artifact> getAssigned(User user, Class<?> clazz) throws OseeCoreException {
      return getAssigned(user.getUserId(), clazz);
   }

   /**
    * return currently assigned state machine artifacts that match clazz
    * 
    * @param clazz to match or all if null
    */
   public static Set<Artifact> getAssigned(String userId, Class<?> clazz) throws OseeCoreException {
      Set<Artifact> assigned = new HashSet<Artifact>();
      for (Artifact artifact : ArtifactQuery.getArtifactListFromAttribute(AtsAttributeTypes.CurrentState,
         "%<" + userId + ">%", AtsUtilCore.getAtsBranch())) {
         if (clazz == null || clazz.isInstance(artifact)) {
            assigned.add(artifact);
         }
      }
      return assigned;

   }

   public static Collection<IBasicUser> getImplementersByState(AbstractWorkflowArtifact workflow, IWorkPage state) throws OseeCoreException {
      Set<IBasicUser> users = new HashSet<IBasicUser>();
      if (workflow.isCancelled()) {
         users.add(workflow.getCancelledBy());
      } else {
         users.addAll(workflow.getStateMgr().getAssignees(state));
         IBasicUser user = workflow.getCompletedBy();
         if (user != null) {
            users.add(user);
         }
      }
      return users;
   }

   public void internalSetCurrentStateName(String stateName) throws OseeCoreException {
      SMAState state = currentStateDam.getState();
      if (state != null && !state.getName().equals(stateName)) {
         state.setName(stateName);
      }
      currentStateDam.setState(state);
   }
}