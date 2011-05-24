/*
 * Created on May 25, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.config.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.config.demo.config.DemoDbUtil;
import org.eclipse.osee.ats.core.config.AtsBulkLoad;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.review.AbstractReviewArtifact;
import org.eclipse.osee.ats.core.review.DecisionReviewArtifact;
import org.eclipse.osee.ats.core.review.DecisionReviewState;
import org.eclipse.osee.ats.core.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.review.PeerToPeerReviewState;
import org.eclipse.osee.ats.core.review.ReviewManager;
import org.eclipse.osee.ats.core.task.TaskArtifact;
import org.eclipse.osee.ats.core.task.TaskStates;
import org.eclipse.osee.ats.core.team.TeamState;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.type.AtsArtifactTypes;
import org.eclipse.osee.ats.core.type.AtsAttributeTypes;
import org.eclipse.osee.ats.core.workflow.ActionArtifact;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.DemoTestUtil;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.support.test.util.DemoArtifactTypes;
import org.eclipse.osee.support.test.util.DemoSawBuilds;
import org.eclipse.osee.support.test.util.DemoTeam;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Test unit for {@link PopulateDemoActions}
 * 
 * @author Donald G. Dunne
 */
public class PopulateDemoActionsTest {
   @BeforeClass
   public static void validateDbInit() throws OseeCoreException {
      DemoDbUtil.checkDbInitAndPopulateSuccess();
   }

   @Before
   public void setup() {
      AtsBulkLoad.reloadConfig(true);
   }

   @org.junit.Test
   public void testSawBuild2Action1() throws OseeCoreException {
      // {@link DemoDbActionData.getReqSawActionsData()} - 1
      String title = "SAW (committed) Reqt Changes for Diagram View";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      // test teams
      Assert.assertEquals(3, action.getTeams().size());
      TeamWorkFlowArtifact codeTeamArt = null;
      int numTested = 0;
      for (TeamWorkFlowArtifact teamArt : action.getTeams()) {
         teamArt.getActionableItemsDam().getActionableItemGuids();
         if (teamArt.getActionableItemsDam().getActionableItemsStr().contains("Code")) {
            numTested++;
            codeTeamArt = teamArt;
            testTeamContents(teamArt, title, "1", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
               "SAW Code", "Joe Smith", DemoArtifactTypes.DemoCodeTeamWorkflow,
               DemoTestUtil.getTeamDef(DemoTeam.SAW_Code));
         } else if (teamArt.getActionableItemsDam().getActionableItemsStr().contains("Test")) {
            numTested++;
            testTeamContents(teamArt, title, "1", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
               "SAW Test", "Kay Jones", DemoArtifactTypes.DemoTestTeamWorkflow,
               DemoTestUtil.getTeamDef(DemoTeam.SAW_Test));
         } else if (teamArt.getActionableItemsDam().getActionableItemsStr().contains("Req")) {
            numTested++;
            testTeamContents(teamArt, title, "1", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
               "SAW Requirements", "Joe Smith", DemoArtifactTypes.DemoReqTeamWorkflow,
               DemoTestUtil.getTeamDef(DemoTeam.SAW_Requirements));
         }
      }
      Assert.assertEquals(3, numTested);
      Assert.assertNotNull(codeTeamArt);
      // test reviews
      Assert.assertEquals("Should only be two reviews", 2, ReviewManager.getReviews(codeTeamArt).size());
      PeerToPeerReviewArtifact rev1 = null;
      PeerToPeerReviewArtifact rev2 = null;
      for (AbstractReviewArtifact revArt : ReviewManager.getReviews(codeTeamArt)) {
         if (revArt.getName().contains("algorithm")) {
            rev1 = (PeerToPeerReviewArtifact) revArt;
         } else {
            rev2 = (PeerToPeerReviewArtifact) revArt;
         }
      }
      Assert.assertNotNull(rev1);
      Assert.assertNotNull(rev2);
      testReviewContents(rev1, "Peer Review algorithm used in code", PeerToPeerReviewState.Review.getPageName(),
         "Joe Smith; Kay Jones");
      testReviewContents(rev2, "Peer Review first set of code changes", PeerToPeerReviewState.Prepare.getPageName(),
         "Joe Smith");
      // test tasks
      List<String> taskNames = new ArrayList<String>();
      taskNames.addAll(DemoTestUtil.getTaskTitles(true));
      for (TaskArtifact task : codeTeamArt.getTaskArtifacts()) {
         testTaskContents(task, TaskStates.InWork.getPageName(), TeamState.Implement.getPageName());
         taskNames.remove(task.getName());
         Assert.assertEquals("Joe Smith; Kay Jones", task.getStateMgr().getAssigneesStr());
      }
      Assert.assertEquals(
         String.format("Not all tasks exist for [%s]; [%s] remain", codeTeamArt.toStringWithId(), taskNames), 0,
         taskNames.size());
   }

   @org.junit.Test
   public void testSawBuild2Action2() throws OseeCoreException {
      // {@link DemoDbActionData.getReqSawActionsData()} - 2
      String title = "SAW (uncommitted) More Reqt Changes for Diagram View";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(4, action.getTeams().size());
      TeamWorkFlowArtifact codeTeam = null, designTeam = null;
      int numTested = 0;
      for (TeamWorkFlowArtifact teamArt : action.getTeams()) {
         if (teamArt.getActionableItemsDam().getActionableItemsStr().contains("Code")) {
            numTested++;

            codeTeam = teamArt;
            testTeamContents(teamArt, title, "3", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
               "SAW Code", "Joe Smith", DemoArtifactTypes.DemoCodeTeamWorkflow,
               DemoTestUtil.getTeamDef(DemoTeam.SAW_Code));
         } else if (teamArt.getActionableItemsDam().getActionableItemsStr().contains("Test")) {
            numTested++;

            testTeamContents(teamArt, title, "3", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
               "SAW Test", "Kay Jones", DemoArtifactTypes.DemoTestTeamWorkflow,
               DemoTestUtil.getTeamDef(DemoTeam.SAW_Test));
         } else if (teamArt.getActionableItemsDam().getActionableItemsStr().contains("Req")) {
            numTested++;

            testTeamContents(teamArt, title, "3", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
               "SAW Requirements", "Joe Smith", DemoArtifactTypes.DemoReqTeamWorkflow,
               DemoTestUtil.getTeamDef(DemoTeam.SAW_Requirements));
         } else if (teamArt.getActionableItemsDam().getActionableItemsStr().contains("Design")) {
            numTested++;

            designTeam = teamArt;
            testTeamContents(teamArt, title, "3", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
               "SAW SW Design", "Kay Jones", AtsArtifactTypes.TeamWorkflow,
               DemoTestUtil.getTeamDef(DemoTeam.SAW_SW_Design));
         }
      }
      Assert.assertEquals(4, numTested);
      // test code team 1 review and 6 tasks
      //  - test review
      Assert.assertNotNull(codeTeam);
      Collection<AbstractReviewArtifact> reviews = ReviewManager.getReviews(codeTeam);
      Assert.assertEquals(1, reviews.size());
      PeerToPeerReviewArtifact revArt = (PeerToPeerReviewArtifact) reviews.iterator().next();
      testReviewContents(revArt, "Review new logic", PeerToPeerReviewState.Completed.getPageName());

      //  - test tasks
      List<String> taskNames = new ArrayList<String>();
      taskNames.addAll(DemoTestUtil.getTaskTitles(false));
      for (TaskArtifact task : codeTeam.getTaskArtifacts()) {
         testTaskContents(task, TaskStates.InWork.getPageName(), TeamState.Implement.getPageName());
         taskNames.remove(task.getName());
         Assert.assertEquals("Joe Smith", task.getStateMgr().getAssigneesStr());
      }
      Assert.assertEquals(
         String.format("Not all tasks exist for [%s]; [%s] remain", codeTeam.toStringWithId(), taskNames), 0,
         taskNames.size());

      // test sw_design 1 peer and 1 decision review
      testSwDesign1PeerAnd1DecisionReview(designTeam);

   }

   @org.junit.Test
   public void testSawBuild2Action3() throws OseeCoreException {
      // {@link DemoDbActionData.getReqSawActionsData()} - 3
      String title = "SAW (no-branch) Even More Requirement Changes for Diagram View";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(4, action.getTeams().size());
      Assert.assertEquals(4, action.getTeams().size());
      TeamWorkFlowArtifact designTeam = null;
      int numTested = 0;
      for (TeamWorkFlowArtifact teamArt : action.getTeams()) {
         if (teamArt.getActionableItemsDam().getActionableItemsStr().contains("Code")) {
            numTested++;

            testTeamContents(teamArt, title, "3", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
               "SAW Code", "Joe Smith", DemoArtifactTypes.DemoCodeTeamWorkflow,
               DemoTestUtil.getTeamDef(DemoTeam.SAW_Code));
         } else if (teamArt.getActionableItemsDam().getActionableItemsStr().contains("Test")) {
            numTested++;

            testTeamContents(teamArt, title, "3", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
               "SAW Test", "Kay Jones", DemoArtifactTypes.DemoTestTeamWorkflow,
               DemoTestUtil.getTeamDef(DemoTeam.SAW_Test));
         } else if (teamArt.getActionableItemsDam().getActionableItemsStr().contains("Req")) {
            numTested++;

            testTeamContents(teamArt, title, "3", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
               "SAW Requirements", "Joe Smith", DemoArtifactTypes.DemoReqTeamWorkflow,
               DemoTestUtil.getTeamDef(DemoTeam.SAW_Requirements));
         } else if (teamArt.getActionableItemsDam().getActionableItemsStr().contains("Design")) {
            numTested++;

            designTeam = teamArt;
            testTeamContents(teamArt, title, "3", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
               "SAW SW Design", "Kay Jones", AtsArtifactTypes.TeamWorkflow,
               DemoTestUtil.getTeamDef(DemoTeam.SAW_SW_Design));
         }
      }
      Assert.assertEquals(4, numTested);
      // test sw_design 1 peer and 1 decision review
      testSwDesign1PeerAnd1DecisionReview(designTeam);

   }

   @org.junit.Test
   public void testSawBuild2Action4() throws OseeCoreException {
      // {@link DemoDbActionData.getReqSawActionsData()} - 4
      String title = "SAW (uncommitted-conflicted) More Requirement Changes for Diagram View";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "3", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
         "SAW Requirements", "Joe Smith", DemoArtifactTypes.DemoReqTeamWorkflow,
         DemoTestUtil.getTeamDef(DemoTeam.SAW_Requirements));
   }

   @org.junit.Test
   public void testWorkaroundForGraphViewBld1Action() throws OseeCoreException {
      String title = "Workaround for Graph View for SAW_Bld_1";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "1", DemoSawBuilds.SAW_Bld_1.getName(), TeamState.Completed.getPageName(),
         "Adapter", "", DemoArtifactTypes.DemoReqTeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.SAW_HW));
   }

   @org.junit.Test
   public void testWorkaroundForGraphViewBld2Action() throws OseeCoreException {
      String title = "Workaround for Graph View for SAW_Bld_2";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "1", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Implement.getPageName(),
         "Adapter", "Jason Michael", DemoArtifactTypes.DemoReqTeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.SAW_HW));
   }

   @org.junit.Test
   public void testWorkaroundForGraphViewBld3Action() throws OseeCoreException {
      String title = "Workaround for Graph View for SAW_Bld_3";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "1", DemoSawBuilds.SAW_Bld_3.getName(), TeamState.Implement.getPageName(),
         "Adapter", "Jason Michael", DemoArtifactTypes.DemoReqTeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.SAW_HW));
   }

   @org.junit.Test
   public void testWorkingWithDiagramTreeBld1Action() throws OseeCoreException {
      String title = "Working with Diagram Tree for SAW_Bld_1";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "3", DemoSawBuilds.SAW_Bld_1.getName(), TeamState.Completed.getPageName(),
         "SAW SW Design", "", AtsArtifactTypes.TeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.SAW_SW_Design));
   }

   @org.junit.Test
   public void testWorkingWithDiagramTreeBld2Action() throws OseeCoreException {
      String title = "Working with Diagram Tree for SAW_Bld_2";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "3", DemoSawBuilds.SAW_Bld_2.getName(), TeamState.Endorse.getPageName(),
         "SAW SW Design", "Kay Jones", AtsArtifactTypes.TeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.SAW_SW_Design));
   }

   @org.junit.Test
   public void testWorkingWithDiagramTreeBld3Action() throws OseeCoreException {
      String title = "Working with Diagram Tree for SAW_Bld_3";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "3", DemoSawBuilds.SAW_Bld_3.getName(), TeamState.Endorse.getPageName(),
         "SAW SW Design", "Kay Jones", AtsArtifactTypes.TeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.SAW_SW_Design));
   }

   @org.junit.Test
   public void testButton2DoesntWorkOnHelpAction() throws OseeCoreException {
      String title = "Button S doesn't work on help";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "3", "", TeamState.Completed.getPageName(), "Reader", "",
         AtsArtifactTypes.TeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.Tools_Team));

      // test decision review
      Collection<AbstractReviewArtifact> reviews = ReviewManager.getReviews(teamArt);
      Assert.assertEquals(1, reviews.size());
      DecisionReviewArtifact revArt = (DecisionReviewArtifact) reviews.iterator().next();
      testReviewContents(revArt, "Is the resolution of this Action valid?", DecisionReviewState.Decision.getPageName(),
         "Joe Smith");

   }

   @org.junit.Test
   public void testButtonWDoesntWorkOnSituationPageAction() throws OseeCoreException {
      String title = "Button W doesn't work on Situation Page";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "3", "", TeamState.Analyze.getPageName(), "CIS Test", "Kay Jones",
         DemoArtifactTypes.DemoTestTeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.CIS_Test));

      // test decision review
      Collection<AbstractReviewArtifact> reviews = ReviewManager.getReviews(teamArt);
      Assert.assertEquals(1, reviews.size());
      DecisionReviewArtifact revArt = (DecisionReviewArtifact) reviews.iterator().next();
      testReviewContents(revArt, "Is the resolution of this Action valid?", DecisionReviewState.Followup.getPageName(),
         "Joe Smith");

   }

   @org.junit.Test
   public void testCantLoadDiagramTreeAction() throws OseeCoreException {
      String title = "Can't load Diagram Tree";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "3", "", TeamState.Endorse.getPageName(), "CIS Test", "Kay Jones",
         DemoArtifactTypes.DemoTestTeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.CIS_Test));
   }

   @org.junit.Test
   public void testCantSeeTheGraphViewAction() throws OseeCoreException {
      String title = "Can't see the Graph View";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "1", "", TeamState.Implement.getPageName(), "Adapter", "Jason Michael",
         DemoArtifactTypes.DemoReqTeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.SAW_HW));

   }

   @org.junit.Test
   public void testProblemInDiagramTreeAction() throws OseeCoreException {
      String title = "Problem in Diagram Tree";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "3", "", TeamState.Endorse.getPageName(), "CIS Test", "Kay Jones",
         DemoArtifactTypes.DemoTestTeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.CIS_Test));

   }

   @org.junit.Test
   public void testProblemWithTheGraphViewAction() throws OseeCoreException {
      String title = "Problem with the Graph View";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "1", "", TeamState.Implement.getPageName(), "Adapter", "Jason Michael",
         DemoArtifactTypes.DemoReqTeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.SAW_HW));

   }

   @org.junit.Test
   public void testProblemWithTheUserWindowAction() throws OseeCoreException {
      String title = "Problem with the user window";
      ActionArtifact action =
         (ActionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.Action, title,
            AtsUtil.getAtsBranchToken());
      Assert.assertNotNull(action);
      Assert.assertEquals(1, action.getTeams().size());
      TeamWorkFlowArtifact teamArt = action.getTeams().iterator().next();

      testTeamContents(teamArt, title, "4", "", TeamState.Implement.getPageName(), "Timesheet", "Jeffery Kay",
         AtsArtifactTypes.TeamWorkflow, DemoTestUtil.getTeamDef(DemoTeam.Tools_Team));

   }

   private static void testReviewContents(AbstractReviewArtifact revArt, String title, String currentStateName, String assigneeStr) throws OseeCoreException {
      Assert.assertEquals(title, revArt.getName());
      Assert.assertEquals(currentStateName, revArt.getCurrentStateName());
      Assert.assertEquals(assigneeStr, revArt.getStateMgr().getAssigneesStr());
      Collection<String> assigneeNames = Artifacts.getNames(revArt.getStateMgr().getAssignees());
      Assert.assertEquals(assigneeNames.size(), assigneeStrs.length);
      for (String assignee : assigneeStrs) {
         if (!assigneeNames.contains(assignee)) {
            Assert.fail(String.format("revArt.getStateMgr().getAssignees(), does not contain user: %s", assignee));
         }
      }
   }

   private static void testTeamContents(TeamWorkFlowArtifact teamArt, String title, String priority, String versionName, String currentStateName, String actionableItemStr, String assigneeStr, IArtifactType artifactType, TeamDefinitionArtifact teamDef) throws OseeCoreException {
      Assert.assertEquals(currentStateName, teamArt.getCurrentStateName());
      Assert.assertEquals(priority, teamArt.getSoleAttributeValue(AtsAttributeTypes.PriorityType, ""));
      // want targeted version, not error/exception
      String targetedVerStr = "";
      if (teamArt.getTargetedVersion() != null) {
         targetedVerStr = teamArt.getTargetedVersion().getName();
      }
      Assert.assertEquals(versionName, targetedVerStr);
      Assert.assertEquals(artifactType, teamArt.getArtifactType());
      Assert.assertEquals(teamDef, teamArt.getTeamDefinition());
      Assert.assertEquals(assigneeStr, teamArt.getStateMgr().getAssigneesStr());
      Assert.assertEquals(actionableItemStr, teamArt.getActionableItemsDam().getActionableItemsStr());
   }

   private void testTaskContents(TaskArtifact task, String currentStateName, String relatedToState) throws OseeCoreException {
      Assert.assertEquals(currentStateName, task.getCurrentStateName());
      Assert.assertEquals(relatedToState, task.getSoleAttributeValue(AtsAttributeTypes.RelatedToState, ""));
   }

   private void testSwDesign1PeerAnd1DecisionReview(TeamWorkFlowArtifact designTeam) throws OseeCoreException {
      Assert.assertNotNull(designTeam);
      PeerToPeerReviewArtifact peerArt = null;
      DecisionReviewArtifact decArt = null;
      for (AbstractReviewArtifact revArt1 : ReviewManager.getReviews(designTeam)) {
         if (revArt1.getName().contains("PeerToPeer")) {
            peerArt = (PeerToPeerReviewArtifact) revArt1;
         } else {
            decArt = (DecisionReviewArtifact) revArt1;
         }
      }
      Assert.assertNotNull(peerArt);
      Assert.assertNotNull(decArt);
      testReviewContents(
         peerArt,
         "Auto-created Decision Review from ruleId atsAddPeerToPeerReview.test.addPeerToPeerReview.Authorize.None.TransitionTo",
         PeerToPeerReviewState.Prepare.getPageName(), "UnAssigned");
      testReviewContents(
         decArt,
         "Auto-created Decision Review from ruleId: atsAddDecisionReview.test.addDecisionReview.Analyze.None.TransitionTo",
         DecisionReviewState.Decision.getPageName(), "UnAssigned");

   }

}