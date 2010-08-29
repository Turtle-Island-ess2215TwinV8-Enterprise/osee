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
package org.eclipse.osee.ats.artifact;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.osee.ats.artifact.ATSLog.LogType;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.world.search.GoalSearchItem;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.ArtifactListDialog;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Donald G. Dunne
 */
public class GoalArtifact extends StateMachineArtifact {

   public static enum GoalState {
      InWork,
      Completed,
      Cancelled
   };

   public GoalArtifact(ArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, ArtifactType artifactType) throws OseeDataStoreException {
      super(parentFactory, guid, humanReadableId, branch, artifactType);
      registerAtsWorldRelation(AtsRelationTypes.Goal_Member);
   }

   @Override
   public ActionArtifact getParentActionArtifact() {
      return null;
   }

   @Override
   public StateMachineArtifact getParentSMA() throws OseeCoreException {
      List<Artifact> parents = getRelatedArtifacts(AtsRelationTypes.Goal_Goal);
      if (parents.isEmpty()) {
         return null;
      }
      if (parents.size() == 1) {
         return (StateMachineArtifact) parents.iterator().next();
      }
      System.err.println("Two parent goals, what do here?");
      return (StateMachineArtifact) parents.iterator().next();
   }

   @Override
   public TeamWorkFlowArtifact getParentTeamWorkflow() {
      return null;
   }

   @Override
   public Set<User> getPrivilegedUsers() {
      return null;
   }

   @Override
   public boolean isTaskable() {
      return false;
   }

   @Override
   public Date getWorldViewReleaseDate() {
      return null;
   }

   @Override
   public VersionArtifact getWorldViewTargetedVersion() {
      return null;
   }

   @Override
   public String getWorldViewParentID() {
      return null;
   }

   @Override
   public String getHyperTargetVersion() {
      return null;
   }

   public static void getGoals(Artifact artifact, Set<Artifact> goals, boolean recurse) throws OseeCoreException {
      getGoals(Arrays.asList(artifact), goals, recurse);
   }

   public static Collection<Artifact> getGoals(Artifact artifact, boolean recurse) throws OseeCoreException {
      Set<Artifact> goals = new HashSet<Artifact>();
      getGoals(artifact, goals, recurse);
      return goals;
   }

   public static void getGoals(Collection<Artifact> artifacts, Set<Artifact> goals, boolean recurse) throws OseeCoreException {
      for (Artifact art : artifacts) {
         if (art instanceof GoalArtifact) {
            goals.add(art);
         }
         goals.addAll(art.getRelatedArtifacts(AtsRelationTypes.Goal_Goal, GoalArtifact.class));
         if (recurse && art instanceof StateMachineArtifact && ((StateMachineArtifact) art).getParentSMA() != null) {
            getGoals(((StateMachineArtifact) art).getParentSMA(), goals, recurse);
         }
      }
   }

   public static boolean isHasGoal(Artifact artifact) throws OseeCoreException {
      return artifact.getRelatedArtifactsCount(AtsRelationTypes.Goal_Goal) > 0;
   }

   public static String getGoalOrder(Artifact artifact) throws OseeCoreException {
      if (artifact instanceof GoalArtifact) {
         return "";
      }
      if (!isHasGoal(artifact)) {
         return "";
      }
      Collection<Artifact> goals = GoalArtifact.getGoals(artifact, false);
      if (goals.size() > 1) {
         List<Artifact> goalsSorted = new ArrayList<Artifact>(goals);
         Collections.sort(goalsSorted);
         StringBuffer sb = new StringBuffer();
         for (Artifact goal : goalsSorted) {
            sb.append(String.format("%s-[%s] ", getGoalOrder((GoalArtifact) goal, artifact), goal));
         }
         return sb.toString();
      }
      Artifact goal = goals.iterator().next();
      return getGoalOrder((GoalArtifact) goal, artifact);
   }

   public static String getGoalOrder(GoalArtifact goalArtifact, Artifact member) throws OseeCoreException {
      List<Artifact> members = goalArtifact.getMembers();
      if (!members.contains(member)) {
         return "";
      }
      try {
         return String.valueOf(members.indexOf(member) + 1);
      } catch (Exception ex) {
         return XViewerCells.getCellExceptionString(ex);
      }
   }

   public static GoalArtifact createGoal(String title) throws OseeCoreException {
      GoalArtifact goalArt =
         (GoalArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.Goal, AtsUtil.getAtsBranch());
      goalArt.setName(title);
      goalArt.getLog().addLog(LogType.Originated, "", "");

      // Initialize state machine
      goalArt.getStateMgr().initializeStateMachine(GoalState.InWork.name(),
         Collections.singleton(UserManager.getUser()));
      goalArt.getLog().addLog(LogType.StateEntered, GoalState.InWork.name(), "");
      return goalArt;
   }

   public List<Artifact> getMembers() throws OseeCoreException {
      return getRelatedArtifacts(AtsRelationTypes.Goal_Member, false);
   }

   public void addMember(Artifact artifact) throws OseeCoreException {
      if (!getMembers().contains(artifact)) {
         addRelation(AtsRelationTypes.Goal_Member, artifact);
      }
   }

   /**
    * change goal, prompt if member of two goals
    */
   public static GoalArtifact promptChangeGoalOrder(Artifact artifact) throws OseeCoreException {
      if (!isHasGoal(artifact)) {
         AWorkbench.popup(String.format("No Goal set for artifact [%s]", artifact));
         return null;
      }
      Collection<Artifact> goals = getGoals(artifact, false);
      GoalArtifact goal = null;
      if (goals.size() == 1) {
         goal = (GoalArtifact) goals.iterator().next();
      } else if (goals.size() > 1) {
         ArtifactListDialog dialog = new ArtifactListDialog(Displays.getActiveShell());
         dialog.setTitle("Select Goal");
         dialog.setMessage("Artifact has multiple Goals\n\nSelect Goal to change order");
         dialog.setArtifacts(goals);
         if (dialog.open() == 0) {
            goal = (GoalArtifact) dialog.getSelection();
         } else {
            return null;
         }
      }
      return promptChangeGoalOrder(goal, artifact);
   }

   /**
    * change goal order for artifact within given goal
    */
   public static GoalArtifact promptChangeGoalOrder(GoalArtifact goalArtifact, Artifact artifact) throws OseeCoreException {
      return promptChangeGoalOrder(goalArtifact, Arrays.asList(artifact));
   }

   /**
    * change goal order for artifacts within given goal
    */
   public static GoalArtifact promptChangeGoalOrder(GoalArtifact goalArtifact, List<Artifact> artifacts) throws OseeCoreException {
      String currentOrder = "Current Order: ";
      for (Artifact artifact : artifacts) {
         if (artifacts.size() == 1 && !isHasGoal(artifact) || goalArtifact == null) {
            AWorkbench.popup(String.format("No Goal set for artifact [%s]", artifact));
            return null;
         }
         String currIndexStr = getGoalOrder(goalArtifact, artifact);
         currentOrder += currIndexStr + ", ";
      }
      currentOrder = currentOrder.replaceFirst(", $", "");

      List<Artifact> members = goalArtifact.getMembers();
      EntryDialog ed =
         new EntryDialog(
            "Change Goal Order",
            String.format(
               "Goal: %s\n\n%s\n\nEnter New Order Number from 1..%d or %d for last\n\nNote: Goal will be placed before number entered.",
               goalArtifact, currentOrder, members.size(), members.size() + 1));
      ed.setNumberFormat(NumberFormat.getIntegerInstance());

      int result = ed.open();
      if (result == Window.OK) {
         String newIndexStr = ed.getEntry();
         Integer enteredIndex = new Integer(newIndexStr);
         boolean insertLast = enteredIndex == members.size() + 1;
         Integer membersIndex = insertLast ? members.size() - 1 : enteredIndex - 1;
         if (membersIndex > members.size()) {
            AWorkbench.popup(String.format("New Order Number [%s] out of range 1..%d", newIndexStr, members.size()));
            return null;
         }
         for (Artifact artifact : artifacts) {
            Artifact insertTarget = members.get(membersIndex);
            goalArtifact.setRelationOrder(AtsRelationTypes.Goal_Member, insertTarget, insertLast ? true : false,
               artifact);
         }
         goalArtifact.persist();
         return goalArtifact;
      }
      return null;
   }

   public Collection<GoalArtifact> getInWorkGoals() throws OseeCoreException {
      GoalSearchItem searchItem = new GoalSearchItem("", new ArrayList<TeamDefinitionArtifact>(), false, null);
      return org.eclipse.osee.framework.jdk.core.util.Collections.castAll(searchItem.performSearchGetResults());
   }
}
