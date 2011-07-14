/*
 * Created on Jul 5, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.notify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import junit.framework.Assert;
import org.eclipse.osee.ats.core.AtsTestUtil;
import org.eclipse.osee.ats.core.AtsTestUtil.AtsTestUtilState;
import org.eclipse.osee.ats.core.action.ActionArtifact;
import org.eclipse.osee.ats.core.action.ActionManager;
import org.eclipse.osee.ats.core.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.review.PeerToPeerReviewManager;
import org.eclipse.osee.ats.core.review.PeerToPeerReviewState;
import org.eclipse.osee.ats.core.review.role.Role;
import org.eclipse.osee.ats.core.review.role.UserRole;
import org.eclipse.osee.ats.core.review.role.UserRoleManager;
import org.eclipse.osee.ats.core.team.TeamState;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.type.AtsRelationTypes;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.core.workdef.ReviewBlockType;
import org.eclipse.osee.ats.core.workflow.ChangeType;
import org.eclipse.osee.ats.core.workflow.transition.TransitionOption;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.support.test.util.DemoUsers;
import org.junit.AfterClass;

/**
 * Test unit for (@link AtsNotificationManager}
 * 
 * @author Donald G. Dunne
 */
public class AtsNotificationManagerTest {

   @AfterClass
   public static void cleanup() throws OseeCoreException {
      User user = UserManager.getUser(DemoUsers.Alex_Kay);
      user.setSoleAttributeValue(CoreAttributeTypes.Email, "");
      user.deleteRelations(AtsRelationTypes.SubscribedUser_Artifact);
      user.persist(AtsNotificationManagerTest.class.getSimpleName());

      AtsNotificationManager.setInTest(true);
      AtsNotificationManager.setIsProduction(false);
      AtsTestUtil.cleanup();
   }

   @org.junit.Test
   public void testOriginatorNotification() throws OseeCoreException {

      //---------------------------------------------------
      // Test that notifications sent if originator changes
      //---------------------------------------------------

      // create a test notification manager
      TestNotificationManager mgr = new TestNotificationManager();
      // restart notification manager with this one and set to NotInTest (cause normally, testing has notification system OFF)
      AtsNotificationManager.start(mgr, true);
      AtsNotificationManager.setInTest(false);
      // create new action which should reset originator cache in notification manager
      AtsTestUtil.cleanupAndReset(AtsNotificationManagerTest.class.getSimpleName());
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();

      // verify no notification events yet
      Assert.assertEquals(0, mgr.getNotificationEvents().size());

      // set valid email for Alex_Kay
      UserManager.getUser(DemoUsers.Alex_Kay).setSoleAttributeValue(CoreAttributeTypes.Email, "alex.kay@boeing.com");
      UserManager.getUser(DemoUsers.Alex_Kay).persist(getClass().getSimpleName());

      // reset the originator
      teamArt.setCreatedBy(UserManager.getUser(DemoUsers.Alex_Kay), false, new Date());
      // persist will kick event which will log the notification event and send
      teamArt.persist("Change originator");

      // verify notification exists now
      Assert.assertEquals(1, mgr.getNotificationEvents().size());
      Assert.assertTrue(mgr.getNotificationEvents().iterator().next().getDescription().startsWith(
         "You have been set as the originator"));

      //---------------------------------------------------
      // Test that NO notifications sent if in test mode
      //---------------------------------------------------

      // reset the originator back to joe smith
      teamArt.setCreatedBy(UserManager.getUser(DemoUsers.Joe_Smith), false, new Date());
      // persist will kick event which will log the notification event and send
      teamArt.persist("Change originator");
      AtsNotificationManager.setInTest(true);
      mgr.clear();

      // verify no notification events yet
      Assert.assertEquals(0, mgr.getNotificationEvents().size());

      // set valid email for Alex_Kay
      UserManager.getUser(DemoUsers.Alex_Kay).setSoleAttributeValue(CoreAttributeTypes.Email, "alex.kay@boeing.com");
      UserManager.getUser(DemoUsers.Alex_Kay).persist(getClass().getSimpleName());

      // reset the originator
      teamArt.setCreatedBy(UserManager.getUser(DemoUsers.Alex_Kay), false, new Date());
      // persist will kick event which will log the notification event and send
      teamArt.persist("Change originator");

      // verify NO notification exists now
      Assert.assertEquals(0, mgr.getNotificationEvents().size());

      //---------------------------------------------------
      // Test that NO notifications sent if user email is invalid
      //---------------------------------------------------

      // reset the originator back to joe smith
      teamArt.setCreatedBy(UserManager.getUser(DemoUsers.Joe_Smith), false, new Date());
      // persist will kick event which will log the notification event and send
      teamArt.persist("Change originator");
      AtsNotificationManager.setInTest(true);
      mgr.clear();

      // verify no notification events yet
      Assert.assertEquals(0, mgr.getNotificationEvents().size());

      // set invalid email for Alex_Kay
      UserManager.getUser(DemoUsers.Alex_Kay).deleteAttributes(CoreAttributeTypes.Email);
      UserManager.getUser(DemoUsers.Alex_Kay).persist(getClass().getSimpleName());

      // reset the originator
      teamArt.setCreatedBy(UserManager.getUser(DemoUsers.Alex_Kay), false, new Date());
      // persist will kick event which will log the notification event and send
      teamArt.persist("Change originator");

      // verify NO notification exists now
      Assert.assertEquals(0, mgr.getNotificationEvents().size());

      AtsTestUtil.cleanup();
   }

   @org.junit.Test
   public void testAddAssigneeNotification() throws OseeCoreException {

      // create a test notification manager
      TestNotificationManager mgr = new TestNotificationManager();
      // restart notification manager with this one and set to NotInTest (cause normally, testing has notification system OFF)
      AtsNotificationManager.setNotificationManager(mgr);
      AtsNotificationManager.setInTest(false);
      AtsNotificationManager.setIsProduction(true);

      // create new action 
      AtsTestUtil.cleanupAndReset(AtsNotificationManagerTest.class.getSimpleName());
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();

      // verify no notification events yet
      Assert.assertEquals(0, mgr.getNotificationEvents().size());

      // set valid email for Alex_Kay
      List<IBasicUser> users = new ArrayList<IBasicUser>();
      User Alex_Kay = UserManager.getUser(DemoUsers.Alex_Kay);
      Alex_Kay.setSoleAttributeValue(CoreAttributeTypes.Email, "alex.kay@boeing.com");
      users.add(Alex_Kay);

      User Jason_Michael = UserManager.getUser(DemoUsers.Jason_Michael);
      users.add(Jason_Michael);

      User Inactive_Steve = UserManager.getUser(DemoUsers.Inactive_Steve);
      Inactive_Steve.setSoleAttributeValue(CoreAttributeTypes.Email, "inactive.steve@boeing.com");
      users.add(Inactive_Steve);

      // current assignee shouldn't be emailed
      UserManager.getUser().setEmail("joe.smith@boeing.com");
      users.add(UserManager.getUser());

      teamArt.getStateMgr().addAssignees(users);

      // verify notification exists now only for active, valid email Alex, not for others
      Assert.assertEquals(1, mgr.getNotificationEvents().size());
      Assert.assertTrue(mgr.getNotificationEvents().iterator().next().getDescription().startsWith(
         "You have been set as the assignee"));
      // but all 4 are now assigned
      Assert.assertEquals(4, teamArt.getStateMgr().getAssignees().size());

      for (IBasicUser user : users) {
         UserManager.getUser(user).reloadAttributesAndRelations();
      }
      AtsTestUtil.cleanup();
   }

   @org.junit.Test
   public void testSetAssigneeNotification() throws OseeCoreException {

      // create a test notification manager
      TestNotificationManager mgr = new TestNotificationManager();
      // restart notification manager with this one and set to NotInTest (cause normally, testing has notification system OFF)
      AtsNotificationManager.setNotificationManager(mgr);
      AtsNotificationManager.setInTest(false);
      AtsNotificationManager.setIsProduction(true);

      // create new action 
      AtsTestUtil.cleanupAndReset(AtsNotificationManagerTest.class.getSimpleName());
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();
      Assert.assertEquals("Joe should be assigned; currently = " + teamArt.getStateMgr().getAssigneesStr(), 1,
         teamArt.getStateMgr().getAssignees().size());

      // set valid email for Alex_Kay and add as assignee
      User Alex_Kay = UserManager.getUser(DemoUsers.Alex_Kay);
      Alex_Kay.setSoleAttributeValue(CoreAttributeTypes.Email, "alex.kay@boeing.com");
      Alex_Kay.persist(getClass().getSimpleName());
      teamArt.getStateMgr().addAssignee(Alex_Kay);
      teamArt.persist(getClass().getSimpleName());
      Assert.assertEquals("Alex and Joe should be assigned; currently = " + teamArt.getStateMgr().getAssigneesStr(), 2,
         teamArt.getStateMgr().getAssignees().size());
      mgr.clear();

      // verify no notification events yet
      Assert.assertEquals(0, mgr.getNotificationEvents().size());

      List<IBasicUser> usersToSet = new ArrayList<IBasicUser>();
      User Jason_Michael = UserManager.getUser(DemoUsers.Jason_Michael);
      Jason_Michael.setEmail("jason.michael@boeing.com");
      usersToSet.add(Jason_Michael);

      User Inactive_Steve = UserManager.getUser(DemoUsers.Inactive_Steve);
      Inactive_Steve.setSoleAttributeValue(CoreAttributeTypes.Email, "inactive.steve@boeing.com");
      usersToSet.add(Inactive_Steve);

      // current assignee and Alex_Kay shouldn't be emailed cause they were already assigned
      UserManager.getUser().setEmail("joe.smith@boeing.com");
      usersToSet.add(UserManager.getUser());
      usersToSet.add(Alex_Kay);

      teamArt.getStateMgr().setAssignees(usersToSet);

      // verify notification exists now only for Jason_Michael, not for others
      Assert.assertEquals(1, mgr.getNotificationEvents().size());
      Assert.assertTrue(mgr.getNotificationEvents().iterator().next().getDescription().startsWith(
         "You have been set as the assignee"));
      // but all 4 are now assigned
      Assert.assertEquals(4, teamArt.getStateMgr().getAssignees().size());

      for (IBasicUser user : usersToSet) {
         UserManager.getUser(user).reloadAttributesAndRelations();
      }
      AtsTestUtil.cleanup();
   }

   @org.junit.Test
   public void testCompletedNotification() throws OseeCoreException {

      // create a test notification manager
      TestNotificationManager mgr = new TestNotificationManager();
      // restart notification manager with this one and set to NotInTest (cause normally, testing has notification system OFF)
      AtsNotificationManager.setNotificationManager(mgr);
      AtsNotificationManager.setInTest(false);
      AtsNotificationManager.setIsProduction(true);

      // create new action 
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName());

      // set originator as Alex Kay
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();
      teamArt.setCreatedBy(UserManager.getUser(DemoUsers.Alex_Kay), false, new Date());
      teamArt.persist(getClass().getSimpleName() + " - set originator");

      // set alex kay having valid email address
      User user = UserManager.getUser(DemoUsers.Alex_Kay);
      user.setSoleAttributeValue(CoreAttributeTypes.Email, "alex.kay@boeing.com");
      user.persist(getClass().getSimpleName() + "- set alex email address");
      mgr.clear();

      // verify no notification events yet
      Assert.assertEquals(0, mgr.getNotificationEvents().size());
      SkynetTransaction transaction = new SkynetTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      Result result =
         AtsTestUtil.transitionTo(AtsTestUtilState.Completed, UserManager.getUser(), transaction,
            TransitionOption.OverrideAssigneeCheck, TransitionOption.OverrideTransitionValidityCheck);
      Assert.assertEquals(Result.TrueResult, result);
      Assert.assertEquals(teamArt.getCurrentStateName(), TeamState.Completed.getPageName());
      transaction.execute();

      // verify notification to originator
      Assert.assertEquals(1, mgr.getNotificationEvents().size());
      Assert.assertTrue(mgr.getNotificationEvents().iterator().next().getDescription().endsWith("is [Completed]"));

      AtsTestUtil.cleanup();
   }

   @org.junit.Test
   public void testCancelledNotification() throws OseeCoreException {

      // create a test notification manager
      TestNotificationManager mgr = new TestNotificationManager();
      // restart notification manager with this one and set to NotInTest (cause normally, testing has notification system OFF)
      AtsNotificationManager.setNotificationManager(mgr);
      AtsNotificationManager.setInTest(false);
      AtsNotificationManager.setIsProduction(true);

      // create new action 
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName());

      // set originator as Alex Kay
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();
      teamArt.setCreatedBy(UserManager.getUser(DemoUsers.Alex_Kay), false, new Date());
      teamArt.persist(getClass().getSimpleName() + " - set originator");

      // set alex kay having valid email address
      User user = UserManager.getUser(DemoUsers.Alex_Kay);
      user.setSoleAttributeValue(CoreAttributeTypes.Email, "alex.kay@boeing.com");
      user.persist(getClass().getSimpleName() + "-set key email address");
      mgr.clear();

      // verify no notification events yet
      Assert.assertEquals(0, mgr.getNotificationEvents().size());
      SkynetTransaction transaction = new SkynetTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      Result result =
         AtsTestUtil.transitionTo(AtsTestUtilState.Cancelled, UserManager.getUser(), transaction,
            TransitionOption.OverrideAssigneeCheck, TransitionOption.OverrideTransitionValidityCheck);
      Assert.assertEquals(Result.TrueResult, result);
      Assert.assertEquals(teamArt.getCurrentStateName(), TeamState.Cancelled.getPageName());
      transaction.execute();

      // verify notification to originator
      Assert.assertEquals(1, mgr.getNotificationEvents().size());
      Assert.assertTrue(mgr.getNotificationEvents().iterator().next().getDescription().startsWith(
         "[Team Workflow] titled [AtsTestUtil - Team WF [AtsNotificationManagerTest]] was [Cancelled] from the [Analyze]"));

      AtsTestUtil.cleanup();
   }

   @org.junit.Test
   public void testSubscribedTeam() throws OseeCoreException {

      // create a test notification manager
      TestNotificationManager mgr = new TestNotificationManager();
      // restart notification manager with this one and set to NotInTest (cause normally, testing has notification system OFF)
      AtsNotificationManager.setNotificationManager(mgr);
      AtsNotificationManager.setInTest(false);
      AtsNotificationManager.setIsProduction(true);

      // create new action 
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName());

      // setup alex email and subscribe for team definition
      User alex = UserManager.getUser(DemoUsers.Alex_Kay);
      alex.setSoleAttributeValue(CoreAttributeTypes.Email, "alex.kay@boeing.com");
      alex.persist(getClass().getSimpleName() + "- set alex email address");

      AtsTestUtil.getTestTeamDef().setRelations(AtsRelationTypes.SubscribedUser_User, Arrays.asList(alex));
      AtsTestUtil.getTestTeamDef().persist(getClass().getSimpleName() + " - add teamDef subscription");

      mgr.clear();

      // create another action
      SkynetTransaction transaction = new SkynetTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      ActionArtifact actionArt =
         ActionManager.createAction(null, getClass().getSimpleName() + " - testSubscribedTeam", "description",
            ChangeType.Improvement, "1", false, null, Arrays.asList(AtsTestUtil.getTestAi()), new Date(),
            UserManager.getUser(), null, transaction);

      // verify notification to subscriber
      Assert.assertEquals(1, mgr.getNotificationEvents().size());
      Assert.assertTrue(mgr.getNotificationEvents().iterator().next().getDescription().startsWith(
         "You have subscribed for email notification for Team "));

      actionArt.getTeams().iterator().next().deleteAndPersist();
      actionArt.deleteAndPersist();
      User user = UserManager.getUser(DemoUsers.Alex_Kay);
      user.setSoleAttributeValue(CoreAttributeTypes.Email, "");
      user.deleteRelations(AtsRelationTypes.SubscribedUser_Artifact);
      user.persist(AtsNotificationManagerTest.class.getSimpleName());

      AtsTestUtil.cleanup();
   }

   @org.junit.Test
   public void testSubscribedActionableItem() throws OseeCoreException {

      // create a test notification manager
      TestNotificationManager mgr = new TestNotificationManager();
      // restart notification manager with this one and set to NotInTest (cause normally, testing has notification system OFF)
      AtsNotificationManager.setNotificationManager(mgr);
      AtsNotificationManager.setInTest(false);
      AtsNotificationManager.setIsProduction(true);

      // create new action 
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName());

      // setup alex email and subscribe for AI
      User alex = UserManager.getUser(DemoUsers.Alex_Kay);
      alex.setSoleAttributeValue(CoreAttributeTypes.Email, "alex.kay@boeing.com");
      alex.persist(getClass().getSimpleName() + "- set alex email address");

      AtsTestUtil.getTestAi().setRelations(AtsRelationTypes.SubscribedUser_User, Arrays.asList(alex));
      AtsTestUtil.getTestAi().persist(getClass().getSimpleName() + " - add AI subscription");

      mgr.clear();

      // create another action
      SkynetTransaction transaction = new SkynetTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      ActionArtifact actionArt =
         ActionManager.createAction(null, getClass().getSimpleName() + " - testSubscribedAI", "description",
            ChangeType.Improvement, "1", false, null, Arrays.asList(AtsTestUtil.getTestAi()), new Date(),
            UserManager.getUser(), null, transaction);

      // verify notification to subscriber
      Assert.assertEquals(1, mgr.getNotificationEvents().size());
      Assert.assertTrue(mgr.getNotificationEvents().iterator().next().getDescription().startsWith(
         "You have subscribed for email notification for Actionable Item "));

      actionArt.getTeams().iterator().next().deleteAndPersist();
      actionArt.deleteAndPersist();
      User user = UserManager.getUser(DemoUsers.Alex_Kay);
      user.setSoleAttributeValue(CoreAttributeTypes.Email, "");
      user.deleteRelations(AtsRelationTypes.SubscribedUser_Artifact);
      user.persist(AtsNotificationManagerTest.class.getSimpleName());

      AtsTestUtil.cleanup();
   }

   @org.junit.Test
   public void testSubscribedWorkflow() throws OseeCoreException {

      // create a test notification manager
      TestNotificationManager mgr = new TestNotificationManager();
      // restart notification manager with this one and set to NotInTest (cause normally, testing has notification system OFF)
      AtsNotificationManager.setNotificationManager(mgr);
      AtsNotificationManager.setInTest(false);
      AtsNotificationManager.setIsProduction(true);

      // create new action 
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName());

      // setup alex email and subscribe for AI
      User alex = UserManager.getUser(DemoUsers.Alex_Kay);
      alex.setSoleAttributeValue(CoreAttributeTypes.Email, "alex.kay@boeing.com");
      alex.persist(getClass().getSimpleName() + "- set alex email address");

      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();

      teamArt.setRelations(AtsRelationTypes.SubscribedUser_User, Arrays.asList(alex));
      teamArt.persist(getClass().getSimpleName() + " - add Workflow subscription");

      mgr.clear();

      SkynetTransaction transaction = new SkynetTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      Result result =
         AtsTestUtil.transitionTo(AtsTestUtilState.Implement, UserManager.getUser(), transaction,
            TransitionOption.OverrideAssigneeCheck, TransitionOption.OverrideTransitionValidityCheck);
      Assert.assertEquals(Result.TrueResult, result);
      transaction.execute();

      // verify notification to workflow subscriber
      Assert.assertEquals(1, mgr.getNotificationEvents().size());
      Assert.assertEquals(
         "[Team Workflow] titled [AtsTestUtil - Team WF [AtsNotificationManagerTest]] transitioned to [Implement] and you subscribed for notification.",
         mgr.getNotificationEvents().iterator().next().getDescription());

      AtsTestUtil.cleanup();
   }

   @org.junit.Test
   public void testReviewersCompleted() throws OseeCoreException {

      // create a test notification manager
      TestNotificationManager mgr = new TestNotificationManager();
      // restart notification manager with this one and set to NotInTest (cause normally, testing has notification system OFF)
      AtsNotificationManager.setNotificationManager(mgr);
      AtsNotificationManager.setInTest(false);
      AtsNotificationManager.setIsProduction(true);

      // create new action 
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName());

      // setup alex email and subscribe for AI
      User alex = UserManager.getUser(DemoUsers.Alex_Kay);
      alex.setSoleAttributeValue(CoreAttributeTypes.Email, "alex.kay@boeing.com");
      alex.persist(getClass().getSimpleName() + "- set alex email address");

      User kay = UserManager.getUser(DemoUsers.Kay_Jones);
      kay.setSoleAttributeValue(CoreAttributeTypes.Email, "kay.jones@boeing.com");
      kay.persist(getClass().getSimpleName() + "- set kay email address");

      SkynetTransaction transaction = new SkynetTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      PeerToPeerReviewArtifact peerArt =
         AtsTestUtil.getOrCreatePeerReview(ReviewBlockType.None, AtsTestUtilState.Analyze, transaction);
      List<UserRole> roles = new ArrayList<UserRole>();
      UserRole author = new UserRole(Role.Author, alex);
      roles.add(author);
      UserRole moderator = new UserRole(Role.Moderator, kay);
      roles.add(moderator);
      UserRole reviewer1 = new UserRole(Role.Reviewer, UserManager.getUser());
      roles.add(reviewer1);
      UserRole reviewer2 = new UserRole(Role.Reviewer, UserManager.getUser(DemoUsers.Jason_Michael));
      roles.add(reviewer2);

      Result result =
         PeerToPeerReviewManager.transitionTo(peerArt, PeerToPeerReviewState.Review, roles, null,
            UserManager.getUser(), false, transaction);
      Assert.assertEquals(Result.TrueResult, result);
      peerArt.persist(transaction);
      transaction.execute();
      mgr.clear();

      // complete reviewer1 role
      transaction =
         new SkynetTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName() + " - update reviewer 1");
      UserRoleManager roleMgr = new UserRoleManager(peerArt);
      reviewer1.setHoursSpent(1.0);
      reviewer1.setCompleted(true);
      roleMgr.addOrUpdateUserRole(reviewer1);
      roleMgr.saveToArtifact(transaction);
      transaction.execute();

      // no notifications sent
      Assert.assertEquals(0, mgr.getNotificationEvents().size());

      // complete reviewer2 role
      transaction =
         new SkynetTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName() + " - update reviewer 2");
      reviewer2.setHoursSpent(1.0);
      reviewer2.setCompleted(true);
      roleMgr.addOrUpdateUserRole(reviewer2);
      roleMgr.saveToArtifact(transaction);
      peerArt.persist(transaction);
      transaction.execute();

      // notification sent to author/moderator
      Assert.assertEquals(1, mgr.getNotificationEvents().size());
      Assert.assertTrue(mgr.getNotificationEvents().iterator().next().getDescription().equals(
         "You are Author/Moderator of [PeerToPeer Review] titled [AtsTestUtil Test Decision Review] which has been reviewed by all reviewers"));
      // email both moderator and author
      Assert.assertEquals(2, mgr.getNotificationEvents().iterator().next().getUsers().size());
      peerArt.deleteAndPersist();
      AtsTestUtil.cleanup();
   }
}