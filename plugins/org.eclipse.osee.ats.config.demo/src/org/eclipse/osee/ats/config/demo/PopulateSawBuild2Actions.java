/*
 * Created on Jun 2, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.config.demo;

import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.ats.artifact.ActionManager;
import org.eclipse.osee.ats.config.demo.config.DemoDbAIs;
import org.eclipse.osee.ats.config.demo.config.DemoDbUtil;
import org.eclipse.osee.ats.config.demo.config.DemoDbUtil.SoftwareRequirementStrs;
import org.eclipse.osee.ats.config.demo.internal.OseeAtsConfigDemoActivator;
import org.eclipse.osee.ats.core.config.ActionableItemArtifact;
import org.eclipse.osee.ats.core.review.AbstractReviewArtifact;
import org.eclipse.osee.ats.core.review.ReviewManager;
import org.eclipse.osee.ats.core.team.TeamState;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.core.type.AtsArtifactTypes;
import org.eclipse.osee.ats.core.type.AtsAttributeTypes;
import org.eclipse.osee.ats.core.type.AtsRelationTypes;
import org.eclipse.osee.ats.core.workdef.ReviewBlockType;
import org.eclipse.osee.ats.core.workflow.ActionArtifact;
import org.eclipse.osee.ats.core.workflow.ChangeType;
import org.eclipse.osee.ats.core.workflow.transition.TransitionOption;
import org.eclipse.osee.ats.util.AtsBranchManager;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.support.test.util.DemoSawBuilds;

/**
 * @author Donald G. Dunne
 */
public class PopulateSawBuild2Actions {

   private static String versionStr = DemoSawBuilds.SAW_Bld_2.toString();
   private static TeamState toState = TeamState.Implement;
   private static boolean DEBUG = false;

   public static void run() throws OseeCoreException {
      // Create SAW_Bld_2 Actions
      SkynetTransaction transaction =
         new SkynetTransaction(AtsUtil.getAtsBranch(), "Populate Demo DB - PopulateSawBuild2Actions");

      ActionArtifact committedAction = createCommittedAction(transaction);
      ActionArtifact unCommittedAction = createUnCommittedAction(transaction);
      createNoBranchAction(transaction);
      ActionArtifact conflictedAction = createUnCommittedConflictedAction(transaction);
      transaction.execute();

      // Sleep to wait for the persist of the actions
      DemoDbUtil.sleep(3000);

      // Working Branch off SAW_Bld_2, Make Changes, Commit
      makeCommittedActionChanges(committedAction);

      // Working Branch off SAW_Bld_2, Make Changes, DON'T Commit
      makeUnCommittedActionChanges(unCommittedAction);

      // Working Branch off SAW_Bld_2, Make Conflicted Changes, DON'T Commit
      makeConflictedActionChanges(conflictedAction);

   }

   private static ActionArtifact createUnCommittedConflictedAction(SkynetTransaction transaction) throws OseeCoreException {
      String title = "SAW (uncommitted-conflicted) More Requirement Changes for Diagram View";
      Collection<ActionableItemArtifact> aias =
         DemoDbUtil.getActionableItems(new String[] {DemoDbAIs.SAW_Requirements.getAIName()});
      Date createdDate = new Date();
      User createdBy = UserManager.getUser();
      String priority = "3";

      ActionArtifact actionArt =
         ActionManager.createAction(null, title, "Problem with the Diagram View", ChangeType.Problem, priority, false,
            null, aias, createdDate, createdBy, null, transaction);
      for (TeamWorkFlowArtifact teamWf : ActionManager.getTeams(actionArt)) {

         TeamWorkFlowManager dtwm =
            new TeamWorkFlowManager(teamWf, TransitionOption.OverrideAssigneeCheck,
               TransitionOption.OverrideTransitionValidityCheck);

         // Transition to desired state
         Result result = dtwm.transitionTo(toState, null, false, transaction);
         if (result.isFalse()) {
            throw new OseeCoreException("Error transitioning [%s] to state [%s]: [%s]", teamWf.toStringWithId(),
               toState.getPageName(), result.getText());
         }

         if (!teamWf.isCompletedOrCancelled()) {
            // Reset assignees that may have been overwritten during transition 
            teamWf.getStateMgr().setAssignees(teamWf.getTeamDefinition().getLeads());
         }

         teamWf.persist(transaction);
         Artifact verArt =
            ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Version, versionStr, AtsUtil.getAtsBranch());
         teamWf.addRelation(AtsRelationTypes.TeamWorkflowTargetedForVersion_Version, verArt);
         teamWf.persist(transaction);
      }
      return actionArt;
   }

   private static ActionArtifact createNoBranchAction(SkynetTransaction transaction) throws OseeCoreException {
      String title = "SAW (no-branch) Even More Requirement Changes for Diagram View";
      Collection<ActionableItemArtifact> aias =
         DemoDbUtil.getActionableItems(new String[] {
            DemoDbAIs.SAW_Code.getAIName(),
            DemoDbAIs.SAW_SW_Design.getAIName(),
            DemoDbAIs.SAW_Requirements.getAIName(),
            DemoDbAIs.SAW_Test.getAIName()});
      Date createdDate = new Date();
      User createdBy = UserManager.getUser();
      String priority = "3";

      ActionArtifact actionArt =
         ActionManager.createAction(null, title, "Problem with the Diagram View", ChangeType.Problem, priority, false,
            null, aias, createdDate, createdBy, null, transaction);
      for (TeamWorkFlowArtifact teamWf : ActionManager.getTeams(actionArt)) {

         boolean isSwDesign = teamWf.getTeamDefinition().getName().contains("SW Design");

         TeamWorkFlowManager dtwm = new TeamWorkFlowManager(teamWf, TransitionOption.OverrideAssigneeCheck);

         if (isSwDesign) {
            // transition to analyze
            Result result = dtwm.transitionTo(TeamState.Analyze, null, false, transaction);
            if (result.isFalse()) {
               throw new OseeCoreException("Error transitioning [%s] to Analyze state: [%s]", teamWf.toStringWithId(),
                  toState.getPageName(), result.getText());
            }
            // set reviews to non-blocking
            for (AbstractReviewArtifact reviewArt : ReviewManager.getReviews(teamWf)) {
               reviewArt.setSoleAttributeValue(AtsAttributeTypes.ReviewBlocks, ReviewBlockType.None.name());
            }

            // transition to authorize
            result = dtwm.transitionTo(TeamState.Authorize, null, false, transaction);
            if (result.isFalse()) {
               throw new OseeCoreException("Error transitioning [%s] to Authorize state: [%s]",
                  teamWf.toStringWithId(), toState.getPageName(), result.getText());
            }
            // set reviews to non-blocking
            for (AbstractReviewArtifact reviewArt : ReviewManager.getReviews(teamWf)) {
               reviewArt.setSoleAttributeValue(AtsAttributeTypes.ReviewBlocks, ReviewBlockType.None.name());
            }
         }
         // Transition to final state
         Result result = dtwm.transitionTo(toState, null, false, transaction);
         if (result.isFalse()) {
            throw new OseeCoreException("Error transitioning [%s] to state [%s]: [%s]", teamWf.toStringWithId(),
               toState.getPageName(), result.getText());
         }

         if (!teamWf.isCompletedOrCancelled()) {
            // Reset assignees that may have been overwritten during transition 
            teamWf.getStateMgr().setAssignees(teamWf.getTeamDefinition().getLeads());
         }

         teamWf.persist(transaction);
         Artifact verArt =
            ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Version, versionStr, AtsUtil.getAtsBranch());
         teamWf.addRelation(AtsRelationTypes.TeamWorkflowTargetedForVersion_Version, verArt);
         teamWf.persist(transaction);
      }
      return actionArt;
   }

   private static ActionArtifact createUnCommittedAction(SkynetTransaction transaction) throws OseeCoreException {
      String title = "SAW (uncommitted) More Reqt Changes for Diagram View";
      Collection<ActionableItemArtifact> aias =
         DemoDbUtil.getActionableItems(new String[] {
            DemoDbAIs.SAW_Code.getAIName(),
            DemoDbAIs.SAW_SW_Design.getAIName(),
            DemoDbAIs.SAW_Requirements.getAIName(),
            DemoDbAIs.SAW_Test.getAIName()});
      Date createdDate = new Date();
      User createdBy = UserManager.getUser();
      String priority = "3";

      ActionArtifact actionArt =
         ActionManager.createAction(null, title, "Problem with the Diagram View", ChangeType.Problem, priority, false,
            null, aias, createdDate, createdBy, null, transaction);
      for (TeamWorkFlowArtifact teamWf : ActionManager.getTeams(actionArt)) {

         boolean isSwDesign = teamWf.getTeamDefinition().getName().contains("SW Design");

         TeamWorkFlowManager dtwm =
            new TeamWorkFlowManager(teamWf, TransitionOption.OverrideAssigneeCheck, TransitionOption.None);

         if (isSwDesign) {
            // transition to analyze
            Result result = dtwm.transitionTo(TeamState.Analyze, null, false, transaction);
            if (result.isFalse()) {
               throw new OseeCoreException("Error transitioning [%s] to Analyze state: [%s]", teamWf.toStringWithId(),
                  toState.getPageName(), result.getText());
            }
            // set reviews to non-blocking
            for (AbstractReviewArtifact reviewArt : ReviewManager.getReviews(teamWf)) {
               reviewArt.setSoleAttributeValue(AtsAttributeTypes.ReviewBlocks, ReviewBlockType.None.name());
            }

            // transition to authorize
            result = dtwm.transitionTo(TeamState.Authorize, null, false, transaction);
            if (result.isFalse()) {
               throw new OseeCoreException("Error transitioning [%s] to Authorize state: [%s]",
                  teamWf.toStringWithId(), toState.getPageName(), result.getText());
            }
            // set reviews to non-blocking
            for (AbstractReviewArtifact reviewArt : ReviewManager.getReviews(teamWf)) {
               reviewArt.setSoleAttributeValue(AtsAttributeTypes.ReviewBlocks, ReviewBlockType.None.name());
            }
         }

         // Transition to final state
         Result result = dtwm.transitionTo(toState, null, false, transaction);
         if (result.isFalse()) {
            throw new OseeCoreException("Error transitioning [%s] to state [%s]: [%s]", teamWf.toStringWithId(),
               toState.getPageName(), result.getText());
         }

         if (!teamWf.isCompletedOrCancelled()) {
            // Reset assignees that may have been overwritten during transition 
            teamWf.getStateMgr().setAssignees(teamWf.getTeamDefinition().getLeads());
         }

         teamWf.persist(transaction);
         Artifact verArt =
            ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Version, versionStr, AtsUtil.getAtsBranch());
         teamWf.addRelation(AtsRelationTypes.TeamWorkflowTargetedForVersion_Version, verArt);

         teamWf.persist(transaction);
      }
      return actionArt;
   }

   private static ActionArtifact createCommittedAction(SkynetTransaction transaction) throws OseeCoreException {
      String title = "SAW (committed) Reqt Changes for Diagram View";
      Collection<ActionableItemArtifact> aias =
         DemoDbUtil.getActionableItems(new String[] {
            DemoDbAIs.SAW_Requirements.getAIName(),
            DemoDbAIs.SAW_Code.getAIName(),
            DemoDbAIs.SAW_Test.getAIName()});
      Date createdDate = new Date();
      User createdBy = UserManager.getUser();
      String priority = "1";

      ActionArtifact actionArt =
         ActionManager.createAction(null, title, "Problem with the Diagram View", ChangeType.Problem, priority, false,
            null, aias, createdDate, createdBy, null, transaction);
      for (TeamWorkFlowArtifact teamWf : ActionManager.getTeams(actionArt)) {

         TeamWorkFlowManager dtwm =
            new TeamWorkFlowManager(teamWf, TransitionOption.OverrideAssigneeCheck,
               TransitionOption.OverrideTransitionValidityCheck);

         // Transition to desired state
         Result result = dtwm.transitionTo(toState, null, false, transaction);
         if (result.isFalse()) {
            throw new OseeCoreException("Error transitioning [%s] to state [%s]: [%s]", teamWf.toStringWithId(),
               toState.getPageName(), result.getText());
         }

         if (!teamWf.isCompletedOrCancelled()) {
            // Reset assignees that may have been overwritten during transition 
            teamWf.getStateMgr().setAssignees(teamWf.getTeamDefinition().getLeads());
         }

         teamWf.persist(transaction);
         Artifact verArt =
            ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Version, versionStr, AtsUtil.getAtsBranch());
         teamWf.addRelation(AtsRelationTypes.TeamWorkflowTargetedForVersion_Version, verArt);
         teamWf.persist(transaction);
      }
      return actionArt;
   }

   private static void makeCommittedActionChanges(Artifact actionArt) throws OseeCoreException {
      if (DEBUG) {
         OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO, "Making Action 1 Requirement Changes");
      }
      TeamWorkFlowArtifact reqTeam = null;
      for (TeamWorkFlowArtifact team : ActionManager.getTeams(actionArt)) {
         if (team.getTeamDefinition().getName().contains("Req")) {
            reqTeam = team;
         }
      }

      if (reqTeam == null) {
         throw new OseeArgumentException("Can't locate Req team.");
      }
      if (DEBUG) {
         OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO, "Creating working branch");
      }
      Result result = AtsBranchManager.createWorkingBranch(reqTeam, null, false);
      if (result.isFalse()) {
         throw new OseeArgumentException(
            new StringBuilder("Error creating working branch: ").append(result.getText()).toString());
      }

      DemoDbUtil.sleep(5000);

      for (Artifact art : DemoDbUtil.getSoftwareRequirements(DEBUG, SoftwareRequirementStrs.Robot,
         reqTeam.getWorkingBranch())) {
         if (DEBUG) {
            OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO,
               new StringBuilder("Modifying artifact => ").append(art).toString());
         }
         art.setSoleAttributeValue(CoreAttributeTypes.Csci, DemoCscis.Navigation.name());
         art.setSoleAttributeValue(CoreAttributeTypes.Subsystem, DemoSubsystems.Navigation.name());
         Artifact navArt =
            ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.Component, DemoSubsystems.Navigation.name(),
               reqTeam.getWorkingBranch());
         art.addRelation(CoreRelationTypes.Allocation__Component, navArt);
         art.persist();
      }

      for (Artifact art : DemoDbUtil.getSoftwareRequirements(DEBUG, SoftwareRequirementStrs.Event,
         reqTeam.getWorkingBranch())) {
         if (DEBUG) {
            OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO,
               new StringBuilder("Modifying artifact => ").append(art).toString());
         }
         art.setSoleAttributeValue(CoreAttributeTypes.Csci, DemoCscis.Interface.name());
         art.setSoleAttributeValue(CoreAttributeTypes.Subsystem, DemoSubsystems.Communications.name());
         Artifact robotArt =
            ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.Component, DemoSubsystems.Robot_API.name(),
               reqTeam.getWorkingBranch());
         art.addRelation(CoreRelationTypes.Allocation__Component, robotArt);
         art.persist();
      }

      // Delete two artifacts
      for (Artifact art : DemoDbUtil.getSoftwareRequirements(DEBUG, SoftwareRequirementStrs.daVinci,
         reqTeam.getWorkingBranch())) {
         if (DEBUG) {
            OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO,
               new StringBuilder("Deleting artifact => ").append(art).toString());
         }
         art.deleteAndPersist();
      }

      // Add three new artifacts
      Artifact parentArt = DemoDbUtil.getInterfaceInitializationSoftwareRequirement(DEBUG, reqTeam.getWorkingBranch());
      for (int x = 1; x < 4; x++) {
         String name = "Robot Interface Init " + x;
         if (DEBUG) {
            OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO, "Adding artifact => " + name);
         }
         Artifact newArt =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, parentArt.getBranch(), name);
         newArt.setSoleAttributeValue(CoreAttributeTypes.Subsystem, DemoSubsystems.Communications.name());
         newArt.persist();
         parentArt.addChild(newArt);
         parentArt.persist();
      }

      DemoDbUtil.sleep(2000L);
      if (DEBUG) {
         OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO, "Committing branch");
      }
      Job job =
         AtsBranchManager.commitWorkingBranch(reqTeam, false, true, reqTeam.getTargetedVersion().getParentBranch(),
            true);
      try {
         job.join();
      } catch (InterruptedException ex) {
         OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO, "Completing Action");
      }
      if (DEBUG) {
         OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO, "Completing Action");
      }
   }

   private static void makeConflictedActionChanges(Artifact actionArt) throws OseeCoreException {
      TeamWorkFlowArtifact reqTeam = null;
      for (TeamWorkFlowArtifact team : ActionManager.getTeams(actionArt)) {
         if (team.getTeamDefinition().getName().contains("Req")) {
            reqTeam = team;
         }
      }

      if (reqTeam == null) {
         throw new OseeArgumentException("Can't locate Req team.");
      }
      if (DEBUG) {
         OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO, "Creating working branch");
      }
      Result result = AtsBranchManager.createWorkingBranch(reqTeam, null, false);
      if (result.isFalse()) {
         throw new OseeArgumentException(
            new StringBuilder("Error creating working branch: ").append(result.getText()).toString());
      }

      DemoDbUtil.sleep(5000);

      Artifact branchArtifact =
         DemoDbUtil.getArtTypeRequirements(DEBUG, CoreArtifactTypes.SoftwareRequirement,
            DemoDbUtil.HAPTIC_CONSTRAINTS_REQ, reqTeam.getWorkingBranch()).iterator().next();
      if (DEBUG) {
         OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO,
            new StringBuilder("Modifying branch artifact => ").append(branchArtifact).toString());
      }
      branchArtifact.setSoleAttributeValue(CoreAttributeTypes.Csci, DemoCscis.Interface.name());
      branchArtifact.setSoleAttributeValue(CoreAttributeTypes.Subsystem, DemoSubsystems.Communications.name());
      Artifact comArt =
         ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.Component, DemoSubsystems.Robot_API.name(),
            reqTeam.getWorkingBranch());
      branchArtifact.addRelation(CoreRelationTypes.Allocation__Component, comArt);
      branchArtifact.persist();

      Artifact parentArtifact =
         DemoDbUtil.getArtTypeRequirements(DEBUG, CoreArtifactTypes.SoftwareRequirement,
            DemoDbUtil.HAPTIC_CONSTRAINTS_REQ, reqTeam.getWorkingBranch()).iterator().next();
      if (DEBUG) {
         OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO,
            new StringBuilder("Modifying parent artifact => ").append(parentArtifact).toString());
      }
      parentArtifact.setSoleAttributeValue(CoreAttributeTypes.Csci, DemoCscis.Navigation.name());
      parentArtifact.setSoleAttributeValue(CoreAttributeTypes.Subsystem,
         DemoSubsystems.Cognitive_Decision_Aiding.name());
      parentArtifact.persist();

   }

   private static void makeUnCommittedActionChanges(Artifact actionArt) throws OseeCoreException {
      TeamWorkFlowArtifact reqTeam = null;
      for (TeamWorkFlowArtifact team : ActionManager.getTeams(actionArt)) {
         if (team.getTeamDefinition().getName().contains("Req")) {
            reqTeam = team;
         }
      }

      if (reqTeam == null) {
         throw new OseeArgumentException("Can't locate Req team.");
      }
      if (DEBUG) {
         OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO, "Creating working branch");
      }
      Result result = AtsBranchManager.createWorkingBranch(reqTeam, null, false);
      if (result.isFalse()) {
         throw new OseeArgumentException(
            new StringBuilder("Error creating working branch: ").append(result.getText()).toString());
      }

      DemoDbUtil.sleep(5000);

      for (Artifact art : DemoDbUtil.getSoftwareRequirements(DEBUG, SoftwareRequirementStrs.Functional,
         reqTeam.getWorkingBranch())) {
         if (DEBUG) {
            OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO,
               new StringBuilder("Modifying artifact => ").append(art).toString());
         }
         art.setSoleAttributeValue(CoreAttributeTypes.Csci, DemoCscis.Interface.name());
         art.setSoleAttributeValue(CoreAttributeTypes.Subsystem, DemoSubsystems.Communications.name());
         Artifact comArt =
            ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.Component, DemoSubsystems.Robot_API.name(),
               reqTeam.getWorkingBranch());

         art.addRelation(CoreRelationTypes.Allocation__Component, comArt);
         art.persist();
      }

      // Delete one artifacts
      for (Artifact art : DemoDbUtil.getSoftwareRequirements(DEBUG, SoftwareRequirementStrs.CISST,
         reqTeam.getWorkingBranch())) {
         if (DEBUG) {
            OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO,
               new StringBuilder("Deleting artifact => ").append(art).toString());
         }
         art.deleteAndPersist();
      }

      // Add two new artifacts
      Artifact parentArt = DemoDbUtil.getInterfaceInitializationSoftwareRequirement(DEBUG, reqTeam.getWorkingBranch());
      for (int x = 15; x < 17; x++) {
         String name = "Claw Interface Init " + x;
         if (DEBUG) {
            OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO, "Adding artifact => " + name);
         }
         Artifact newArt =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, parentArt.getBranch(), name);
         newArt.setSoleAttributeValue(CoreAttributeTypes.Subsystem, DemoSubsystems.Communications.name());
         parentArt.addChild(newArt);

         newArt.persist();
      }

   }

}