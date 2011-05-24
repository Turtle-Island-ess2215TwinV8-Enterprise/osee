/*
 * Created on Jun 6, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.core.config.ActionableItemArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.review.AbstractReviewArtifact;
import org.eclipse.osee.ats.core.review.DecisionReviewArtifact;
import org.eclipse.osee.ats.core.review.DecisionReviewManager;
import org.eclipse.osee.ats.core.review.DecisionReviewState;
import org.eclipse.osee.ats.core.review.ReviewManager;
import org.eclipse.osee.ats.core.task.TaskArtifact;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.type.AtsArtifactTypes;
import org.eclipse.osee.ats.core.type.AtsAttributeTypes;
import org.eclipse.osee.ats.core.type.AtsRelationTypes;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.core.version.VersionArtifact;
import org.eclipse.osee.ats.core.workdef.DecisionReviewOption;
import org.eclipse.osee.ats.core.workdef.ReviewBlockType;
import org.eclipse.osee.ats.core.workdef.StateDefinition;
import org.eclipse.osee.ats.core.workdef.WidgetDefinition;
import org.eclipse.osee.ats.core.workdef.WorkDefinition;
import org.eclipse.osee.ats.core.workdef.WorkDefinitionFactory;
import org.eclipse.osee.ats.core.workflow.ActionArtifact;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.core.util.WorkPageType;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.PurgeArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.support.test.util.TestUtil;

/**
 * @author Donald G. Dunne
 */
public class AtsTestUtil {

   private static TeamWorkFlowArtifact teamArt = null;
   private static TeamDefinitionArtifact teamDef = null;
   private static VersionArtifact verArt1 = null, verArt2 = null;
   private static DecisionReviewArtifact decRevArt = null;
   private static TaskArtifact taskArt = null;
   private static ActionableItemArtifact testAi = null;
   private static ActionArtifact actionArt = null;
   private static StateDefinition analyze = null, implement = null, completed = null, cancelled = null;
   private static WorkDefinition workDef = null;
   public static String WORK_DEF_NAME = "Test_Team _Workflow_Definition";
   private static WidgetDefinition estHoursWidgetDef, workPackageWidgetDef;
   private static String postFixName;

   public static void validateArtifactCache() throws OseeStateException {
      if (ArtifactCache.getDirtyArtifacts().size() > 0) {
         for (Artifact artifact : ArtifactCache.getDirtyArtifacts()) {
            System.err.println(String.format("Artifact [%s] is dirty [%s]", artifact.toStringWithId(),
               Artifacts.getDirtyReport(artifact)));
         }
         throw new OseeStateException("[%d] Dirty Artifacts found after populate (see console for details)",
            ArtifactCache.getDirtyArtifacts().size());
      }
   }

   public static WorkDefinition getWorkDef() throws OseeCoreException {
      ensureLoaded();
      return workDef;
   }

   public static StateDefinition getAnalyzeStateDef() throws OseeCoreException {
      ensureLoaded();
      return analyze;
   }

   public static WidgetDefinition getEstHoursWidgetDef() throws OseeCoreException {
      ensureLoaded();
      return estHoursWidgetDef;
   }

   public static WidgetDefinition getWorkPackageWidgetDef() throws OseeCoreException {
      ensureLoaded();
      return workPackageWidgetDef;
   }

   public static StateDefinition getImplementStateDef() throws OseeCoreException {
      ensureLoaded();
      return implement;
   }

   public static StateDefinition getCompletedStateDef() throws OseeCoreException {
      ensureLoaded();
      return completed;
   }

   public static StateDefinition getCancelledStateDef() throws OseeCoreException {
      ensureLoaded();
      return cancelled;
   }

   private static void ensureLoaded() throws OseeCoreException {
      if (workDef == null) {
         throw new OseeStateException("Must call cleanAndReset before using this method");
      }
   }

   private static void clearCaches() {
      if (workDef != null) {
         WorkDefinitionFactory.removeWorkDefinition(workDef);
      }
      analyze = null;
      implement = null;
      completed = null;
      cancelled = null;
      workDef = null;
      estHoursWidgetDef = null;
      workPackageWidgetDef = null;
      teamArt = null;
      teamDef = null;
      taskArt = null;
      testAi = null;
      actionArt = null;
      verArt1 = null;
      verArt2 = null;
   }

   private static String getTitle(String objectName, String postFixName) {
      return String.format("%s - %s [%s]", AtsTestUtil.class.getSimpleName(), objectName, postFixName);
   }

   /**
    * Clear workDef from cache, clear all objects and create new objects with postFixName in titles
    */
   private static void reset(String postFixName) throws OseeCoreException {
      AtsTestUtil.postFixName = postFixName;
      SkynetTransaction transaction =
         new SkynetTransaction(AtsUtilCore.getAtsBranch(), AtsTestUtil.class.getSimpleName());
      workDef = new WorkDefinition(WORK_DEF_NAME);

      analyze = new StateDefinition("Analyze");
      analyze.setWorkDefinition(workDef);
      analyze.setWorkPageType(WorkPageType.Working);
      analyze.setOrdinal(1);
      workDef.getStates().add(analyze);

      workDef.setStartState(analyze);

      implement = new StateDefinition("Implement");
      implement.setWorkDefinition(workDef);
      implement.setWorkPageType(WorkPageType.Working);
      implement.setOrdinal(2);
      workDef.getStates().add(implement);

      completed = new StateDefinition("Completed");
      completed.setWorkDefinition(workDef);
      completed.setWorkPageType(WorkPageType.Completed);
      completed.setOrdinal(3);
      workDef.getStates().add(completed);

      cancelled = new StateDefinition("Cancelled");
      cancelled.setWorkDefinition(workDef);
      cancelled.setWorkPageType(WorkPageType.Cancelled);
      cancelled.setOrdinal(4);
      workDef.getStates().add(cancelled);

      analyze.setDefaultToState(implement);
      analyze.getToStates().addAll(Arrays.asList(implement, completed, cancelled));
      analyze.getOverrideAttributeValidationStates().addAll(Arrays.asList(cancelled));

      implement.setDefaultToState(completed);
      implement.getToStates().addAll(Arrays.asList(analyze, completed, cancelled));
      implement.getOverrideAttributeValidationStates().addAll(Arrays.asList(cancelled, analyze));

      completed.setDefaultToState(completed);
      completed.getToStates().addAll(Arrays.asList(implement));
      completed.getOverrideAttributeValidationStates().addAll(Arrays.asList(implement));

      cancelled.getToStates().addAll(Arrays.asList(analyze, implement));
      cancelled.getOverrideAttributeValidationStates().addAll(Arrays.asList(analyze, implement));

      estHoursWidgetDef = new WidgetDefinition(AtsAttributeTypes.EstimatedHours.getUnqualifiedName());
      estHoursWidgetDef.setAttributeName(AtsAttributeTypes.EstimatedHours.getName());
      estHoursWidgetDef.setXWidgetName("XFloatDam");

      workPackageWidgetDef = new WidgetDefinition(AtsAttributeTypes.WorkPackage.getUnqualifiedName());
      workPackageWidgetDef.setAttributeName(AtsAttributeTypes.WorkPackage.getName());
      workPackageWidgetDef.setXWidgetName("XTextDam");

      WorkDefinitionFactory.addWorkDefinition(workDef);

      testAi =
         (ActionableItemArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.ActionableItem,
            AtsUtilCore.getAtsBranchToken(), getTitle("AI", postFixName));
      testAi.setSoleAttributeValue(CoreAttributeTypes.Active, true);
      testAi.setSoleAttributeValue(AtsAttributeTypes.Actionable, true);

      teamDef =
         (TeamDefinitionArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.TeamDefinition,
            AtsUtilCore.getAtsBranchToken(), getTitle("Team Def", postFixName));
      teamDef.setSoleAttributeValue(AtsAttributeTypes.WorkflowDefinition, WORK_DEF_NAME);
      teamDef.setSoleAttributeValue(CoreAttributeTypes.Active, true);
      teamDef.setSoleAttributeValue(AtsAttributeTypes.TeamUsesVersions, true);

      testAi.addRelation(AtsRelationTypes.TeamActionableItem_Team, teamDef);

      verArt1 =
         (VersionArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.Version, AtsUtilCore.getAtsBranchToken(),
            getTitle("ver 1.0", postFixName));
      verArt1.addRelation(AtsRelationTypes.TeamDefinitionToVersion_TeamDefinition, teamDef);

      verArt2 =
         (VersionArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.Version, AtsUtilCore.getAtsBranchToken(),
            getTitle("ver 2.0", postFixName));
      verArt2.addRelation(AtsRelationTypes.TeamDefinitionToVersion_TeamDefinition, teamDef);

      teamArt =
         (TeamWorkFlowArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.TeamWorkflow,
            AtsUtilCore.getAtsBranchToken(), getTitle("Team WF", postFixName));
      teamArt.setTeamDefinition(teamDef);
      teamArt.getActionableItemsDam().addActionableItem(testAi);
      teamArt.initializeNewStateMachine(getAnalyzeStateDef(), Arrays.asList((IBasicUser) UserManager.getUser()),
         new Date(), UserManager.getUser());

      actionArt =
         (ActionArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.Action, AtsUtilCore.getAtsBranchToken(),
            "Test Action");
      actionArt.addRelation(AtsRelationTypes.ActionToWorkflow_WorkFlow, teamArt);

      testAi.persist(transaction);
      teamDef.persist(transaction);
      verArt1.persist(transaction);
      verArt2.persist(transaction);
      teamArt.persist(transaction);
      actionArt.persist(transaction);
      transaction.execute();
   }

   public static TaskArtifact getOrCreateTask() throws OseeCoreException {
      ensureLoaded();
      if (taskArt == null) {
         taskArt = teamArt.createNewTask(getTitle("Task", postFixName), new Date(), UserManager.getUser());
         taskArt.setSoleAttributeValue(AtsAttributeTypes.RelatedToState, teamArt.getCurrentStateName());
         taskArt.persist("AtsTestUtil - addTask");
      }
      return taskArt;
   }

   public static DecisionReviewArtifact getOrCreateDecisionReview(ReviewBlockType reviewBlockType, String relatedToState) throws OseeCoreException {
      ensureLoaded();
      if (decRevArt == null) {
         List<DecisionReviewOption> options = new ArrayList<DecisionReviewOption>();
         options.add(new DecisionReviewOption(DecisionReviewState.Completed.getPageName(), false, null));
         options.add(new DecisionReviewOption(DecisionReviewState.Followup.getPageName(), true,
            Arrays.asList(UserManager.getUser().getUserId())));
         decRevArt =
            DecisionReviewManager.createNewDecisionReview(teamArt, reviewBlockType,
               AtsTestUtil.class.getSimpleName() + " Test Decision Review", relatedToState, "Decision Review", options,
               Arrays.asList((IBasicUser) UserManager.getUser()), new Date(), UserManager.getUser());
      }
      return decRevArt;
   }

   public static TeamWorkFlowArtifact getTeamWf() throws OseeCoreException {
      ensureLoaded();
      return teamArt;
   }

   public static ActionableItemArtifact getTestAi() throws OseeCoreException {
      ensureLoaded();
      return testAi;

   }

   public static TeamDefinitionArtifact getTestTeamDef() throws OseeCoreException {
      ensureLoaded();
      return teamDef;
   }

   /**
    * All team defs, AIs, action and workflows will be deleted and new ones created with "name" as part of object
    * names/titles. In addition, ArtifactCache will validate that it is not dirty or display errors if it is.
    * 
    * @throws OseeCoreException
    */
   public static void cleanupAndReset(String name) throws OseeCoreException {
      cleanup();
      reset(name);
   }

   /**
    * Cleanup all artifacts and confirm that ArtifactCache has no dirty artifacts. Should be called at beginning at end
    * of each test.
    */
   public static void cleanup() throws OseeCoreException {
      SkynetTransaction transaction =
         new SkynetTransaction(AtsUtilCore.getAtsBranch(), AtsTestUtil.class.getSimpleName() + " - cleanup");
      if (decRevArt != null) {
         decRevArt.deleteAndPersist(transaction);
      }
      if (taskArt != null) {
         taskArt.deleteAndPersist(transaction);
      }
      if (teamArt != null) {
         for (TaskArtifact taskArt : teamArt.getTaskArtifacts()) {
            taskArt.deleteAndPersist(transaction);
         }
         for (AbstractReviewArtifact revArt : ReviewManager.getReviews(teamArt)) {
            revArt.deleteAndPersist(transaction);
         }
         teamArt.deleteAndPersist(transaction);
      }
      if (actionArt != null) {
         actionArt.deleteAndPersist(transaction);
      }
      transaction.execute();
      transaction = new SkynetTransaction(AtsUtilCore.getAtsBranch(), AtsTestUtil.class.getSimpleName() + " - cleanup");
      if (testAi != null) {
         testAi.deleteAndPersist(transaction);
      }
      if (verArt1 != null) {
         verArt1.deleteAndPersist(transaction);
      }
      if (verArt2 != null) {
         verArt2.deleteAndPersist(transaction);
      }
      if (teamDef != null) {
         teamDef.deleteAndPersist(transaction);
      }
      transaction.execute();

      clearCaches();

      // validate that there are no dirty artifacts in cache
      AtsTestUtil.validateArtifactCache();
   }

   public static VersionArtifact getVerArt1() {
      return verArt1;
   }

   public static VersionArtifact getVerArt2() {
      return verArt2;
   }

   /**
    * Deletes any artifact with name that starts with title
    */
   public static void cleanupSimpleTest(String title) throws Exception {
      cleanupSimpleTest(Arrays.asList(title));
   }

   /**
    * Deletes all artifacts with names that start with any title given
    */
   public static void cleanupSimpleTest(Collection<String> titles) throws Exception {
      List<Artifact> artifacts = new ArrayList<Artifact>();
      for (String title : titles) {
         artifacts.addAll(ArtifactQuery.getArtifactListFromName(title + "%", AtsUtilCore.getAtsBranch(),
            EXCLUDE_DELETED));
      }
      new PurgeArtifacts(artifacts).execute();
      TestUtil.sleep(4000);
   }

}