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
package org.eclipse.osee.ats.client.integration.tests.ats.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.config.AtsConfigOperation;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.team.TeamState;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.core.client.workflow.ChangeType;
import org.eclipse.osee.ats.core.config.ActionableItems;
import org.eclipse.osee.ats.core.config.AtsVersionService;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.ats.util.AtsBranchManager;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.BranchDoesNotExist;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.revision.ChangeData;
import org.eclipse.osee.framework.skynet.core.revision.ChangeData.KindType;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Run from the ATS Navigator after the DB is configured for either ATS - Dev or Demo
 * 
 * @author Donald G. Dunne
 */
public class AtsBranchConfigurationTest {

   public static final IOseeBranch BRANCH_VIA_TEAM_DEFINITION = TokenFactory.createBranch("AyH_e6damwQgvDhKfAAA",
      "BranchViaTeamDef");
   public static final IOseeBranch BRANCH_VIA_VERSIONS = TokenFactory.createBranch("AyH_e6damwQgvDhKfBBB",
      "BranchViaVersions");
   private final boolean DEBUG = false;

   private static Collection<String> appendToName(IOseeBranch branch, String... postFixes) {
      Collection<String> data = new ArrayList<String>();
      for (String postFix : postFixes) {
         data.add(String.format("%s - %s", branch.getName(), postFix));
      }
      return data;
   }

   private static String asNamespace(IOseeBranch branch) {
      return String.format("org.branchTest.%s", branch.getName().toLowerCase());
   }

   @Before
   public void testSetup() throws Exception {
      if (AtsUtil.isProductionDb()) {
         throw new IllegalStateException("BranchConfigThroughTeamDefTest should not be run on production DB");
      }
   }

   @org.junit.Test
   public void testBranchViaVersions() throws Exception {
      SevereLoggingMonitor monitor = TestUtil.severeLoggingStart();
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO, "Running testBranchViaVersions...");
      }

      // Cleanup from previous run
      cleanupBranchTest(BRANCH_VIA_VERSIONS);

      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO,
            "Configuring ATS for team org.branchTest.viaTeamDefs");
      }

      // create team definition and actionable item
      String name = BRANCH_VIA_VERSIONS.getName();
      String namespace = asNamespace(BRANCH_VIA_VERSIONS);
      Collection<String> versions = appendToName(BRANCH_VIA_VERSIONS, "Ver1", "Ver2");
      Collection<String> actionableItems = appendToName(BRANCH_VIA_VERSIONS, "A1", "A2");
      AtsConfigOperation operation = configureAts(namespace, name, versions, actionableItems);

      // create main branch
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO, "Creating root branch");
      }
      // Create SAW_Bld_2 branch off SAW_Bld_1
      Branch viaTeamDefBranch = BranchManager.createTopLevelBranch(BRANCH_VIA_VERSIONS);

      TestUtil.sleep(2000);

      // configure version to use branch and allow create/commit
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO,
            "Configuring version to use branch and allow create/commit");
      }
      IAtsTeamDefinition teamDef = operation.getTeamDefinition();
      IAtsVersion versionToTarget = null;
      String version1Hrid = "", version2Hrid = "";
      for (IAtsVersion vArt : teamDef.getVersions()) {
         if (vArt.getName().contains("Ver1")) {
            versionToTarget = vArt;
            version1Hrid = vArt.getHumanReadableId();
         } else {
            version2Hrid = vArt.getHumanReadableId();
         }
      }
      versionToTarget.setBaselineBranchGuid(viaTeamDefBranch.getGuid());
      versionToTarget.setAllowCommitBranch(true);
      versionToTarget.setAllowCreateBranch(true);

      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranchToken(), getClass().getSimpleName());
      AtsClientService.get().storeConfigObject(versionToTarget, transaction);
      transaction.execute();

      TestUtil.sleep(2000);

      // create action and target for version
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO,
            "Create new Action and target for version " + versionToTarget);
      }

      Collection<IAtsActionableItem> selectedActionableItems =
         ActionableItems.getActionableItems(appendToName(BRANCH_VIA_VERSIONS, "A1"));
      assertFalse(selectedActionableItems.isEmpty());

      transaction = TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Branch Configuration Test");
      Artifact actionArt =
         ActionManager.createAction(null, BRANCH_VIA_VERSIONS.getName() + " Req Changes", "description",
            ChangeType.Problem, "1", false, null, selectedActionableItems, new Date(),
            AtsClientService.get().getUserAdmin().getCurrentUser(), null, transaction);
      TeamWorkFlowArtifact teamWf = ActionManager.getTeams(actionArt).iterator().next();
      AtsVersionService.get().setTargetedVersionAndStore(teamWf, versionToTarget);
      teamWf.persist(transaction);
      transaction.execute();

      TeamWorkFlowManager dtwm = new TeamWorkFlowManager(teamWf);

      // Transition to desired state
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO, "Transitioning to Implement state");
      }

      dtwm.transitionTo(TeamState.Implement, AtsClientService.get().getUserAdmin().getCurrentUser(), false,
         transaction);
      teamWf.persist("Branch Configuration Test");

      SMAEditor.editArtifact(teamWf);

      // create branch
      createBranch(namespace, teamWf);

      // make changes
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO, "Make new requirement artifact");
      }
      Artifact rootArtifact = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(teamWf.getWorkingBranch());
      Artifact blk3MainArt =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, teamWf.getWorkingBranch(),
            BRANCH_VIA_VERSIONS.getName() + " Requirement");
      rootArtifact.addChild(blk3MainArt);
      blk3MainArt.persist(getClass().getSimpleName());

      // commit branch
      commitBranch(teamWf);

      TestUtil.sleep(2000);

      // test change report
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO, "Test change report results");
      }
      ChangeData changeData = AtsBranchManager.getChangeDataFromEarliestTransactionId(teamWf);
      assertFalse("No changes detected", changeData.isEmpty());

      Collection<Artifact> newArts = changeData.getArtifacts(KindType.Artifact, ModificationType.NEW);
      assertTrue("Should be 1 new artifact in change report, found " + newArts.size(), newArts.size() == 1);

      TestUtil.severeLoggingEnd(monitor, Arrays.asList(
         "Version [[" + version1Hrid + "][BranchViaVersions - Ver1]] has no related team defininition",
         "Version [[" + version2Hrid + "][BranchViaVersions - Ver2]] has no related team defininition"));
   }

   @org.junit.Test
   public void testBranchViaTeamDefinition() throws Exception {
      SevereLoggingMonitor monitor = TestUtil.severeLoggingStart();

      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO, "Running testBranchViaTeamDefinition...");
      }

      // Cleanup from previous run
      cleanupBranchTest(BRANCH_VIA_TEAM_DEFINITION);

      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO,
            "Configuring ATS for team org.branchTest.viaTeamDefs");
         // create team definition and actionable item
      }

      String name = BRANCH_VIA_TEAM_DEFINITION.getName();
      String namespace = asNamespace(BRANCH_VIA_TEAM_DEFINITION);
      Collection<String> versions = null;
      Collection<String> actionableItems = appendToName(BRANCH_VIA_TEAM_DEFINITION, "A1", "A2");
      AtsConfigOperation operation = configureAts(namespace, name, versions, actionableItems);

      // create main branch
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO, "Creating root branch");
      }
      // Create SAW_Bld_2 branch off SAW_Bld_1
      Branch viaTeamDefBranch = BranchManager.createTopLevelBranch(BRANCH_VIA_TEAM_DEFINITION);

      TestUtil.sleep(2000);

      // configure team def to use branch
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO,
            "Configuring team def to use branch and allow create/commit");
      }
      IAtsTeamDefinition teamDef = operation.getTeamDefinition();
      teamDef.setBaselineBranchGuid(viaTeamDefBranch.getGuid());
      // setup team def to allow create/commit of branch
      teamDef.setAllowCommitBranch(true);
      teamDef.setAllowCreateBranch(true);
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranchToken(), getClass().getSimpleName());
      AtsClientService.get().storeConfigObject(teamDef, transaction);
      transaction.execute();

      TestUtil.sleep(2000);

      // create action,
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO, "Create new Action");
      }
      Collection<IAtsActionableItem> selectedActionableItems =
         ActionableItems.getActionableItems(appendToName(BRANCH_VIA_TEAM_DEFINITION, "A1"));
      assertFalse(selectedActionableItems.isEmpty());

      transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Test branch via team definition: create action");
      String actionTitle = BRANCH_VIA_TEAM_DEFINITION.getName() + " Req Changes";
      Artifact actionArt =
         ActionManager.createAction(null, actionTitle, "description", ChangeType.Problem, "1", false, null,
            selectedActionableItems, new Date(), AtsClientService.get().getUserAdmin().getCurrentUser(), null,
            transaction);
      transaction.execute();

      final TeamWorkFlowArtifact teamWf = ActionManager.getTeams(actionArt).iterator().next();
      TeamWorkFlowManager dtwm = new TeamWorkFlowManager(teamWf);

      // Transition to desired state
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO, "Transitioning to Implement state");
      }
      dtwm.transitionTo(TeamState.Implement, AtsClientService.get().getUserAdmin().getCurrentUser(), false,
         transaction);
      teamWf.persist("Test branch via team definition: Transition to desired state");

      // create branch
      createBranch(namespace, teamWf);

      // make changes
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO, "Make new requirement artifact");
      }
      Artifact rootArtifact = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(teamWf.getWorkingBranch());
      Artifact blk3MainArt =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, teamWf.getWorkingBranch(),
            BRANCH_VIA_TEAM_DEFINITION.getName() + " Requirement");
      rootArtifact.addChild(blk3MainArt);
      blk3MainArt.persist(getClass().getSimpleName());

      // commit branch
      commitBranch(teamWf);

      // test change report
      if (DEBUG) {
         OseeLog.log(AtsBranchConfigurationTest.class, Level.INFO, "Test change report results");
      }
      ChangeData changeData = AtsBranchManager.getChangeDataFromEarliestTransactionId(teamWf);
      assertTrue("No changes detected", !changeData.isEmpty());

      Collection<Artifact> newArts = changeData.getArtifacts(KindType.Artifact, ModificationType.NEW);
      assertTrue("Should be 1 new artifact in change report, found " + newArts.size(), newArts.size() == 1);

      TestUtil.severeLoggingEnd(monitor);
   }

   public static void cleanupBranchTest(IOseeBranch branch) throws Exception {
      String namespace = "org.branchTest." + branch.getName().toLowerCase();
      Artifact aArt =
         ArtifactQuery.checkArtifactFromTypeAndName(AtsArtifactTypes.Action, branch.getName() + " Req Changes",
            AtsUtil.getAtsBranch());
      if (aArt != null) {
         SkynetTransaction transaction =
            TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Branch Configuration Test");
         for (TeamWorkFlowArtifact teamArt : ActionManager.getTeams(aArt)) {
            SMAEditor.close(Collections.singleton(teamArt), false);
            teamArt.deleteAndPersist(transaction, true);
         }
         aArt.deleteAndPersist(transaction, true);
         transaction.execute();
      }

      // Delete VersionArtifacts
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Branch Configuration Test");
      for (IAtsVersion version : AtsClientService.get().getAtsConfig().get(IAtsVersion.class)) {
         if (version.getName().contains(branch.getName())) {
            Artifact artifact = AtsClientService.get().getConfigArtifact(version);
            if (artifact != null) {
               artifact.deleteAndPersist(transaction);
            }
         }
         AtsClientService.get().getAtsConfig().invalidate(version);
      }
      transaction.execute();

      // Delete Team Definitions
      transaction = TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Branch Configuration Test");
      for (Artifact teamDefArt : ArtifactQuery.getArtifactListFromTypeAndName(AtsArtifactTypes.TeamDefinition,
         branch.getName(), AtsUtil.getAtsBranchToken())) {
         teamDefArt.deleteAndPersist(transaction, false);
         AtsClientService.get().getAtsConfig().invalidate(
            AtsClientService.get().getAtsConfig().getSoleByGuid(teamDefArt.getGuid(), IAtsTeamDefinition.class));
      }
      transaction.execute();

      // Delete AIs
      transaction = TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Branch Configuration Test");
      for (Artifact aiaArt : ArtifactQuery.getArtifactListFromTypeAndName(AtsArtifactTypes.ActionableItem,
         branch.getName(), AtsUtil.getAtsBranchToken())) {
         for (Artifact childArt : aiaArt.getChildren()) {
            childArt.deleteAndPersist(transaction, false);
            AtsClientService.get().getAtsConfig().invalidate(
               AtsClientService.get().getAtsConfig().getSoleByGuid(childArt.getGuid(), IAtsActionableItem.class));
         }
         AtsClientService.get().getAtsConfig().invalidate(
            AtsClientService.get().getAtsConfig().getSoleByGuid(aiaArt.getGuid(), IAtsActionableItem.class));
         transaction.execute();
      }

      // Work Definition
      Collection<Artifact> arts =
         ArtifactQuery.getArtifactListFromType(AtsArtifactTypes.WorkDefinition, AtsUtil.getAtsBranch());
      if (arts.size() > 0) {
         transaction = TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Branch Configuration Test");
         for (Artifact workArt : arts) {
            if (workArt.getName().startsWith(namespace)) {
               workArt.deleteAndPersist(transaction, true);
            }
         }
         transaction.execute();
      }

      try {
         BranchManager.refreshBranches();
         // delete working branches
         for (Branch workingBranch : BranchManager.getBranches(BranchArchivedState.ALL, BranchType.WORKING)) {
            if (workingBranch.getName().contains(branch.getName())) {
               BranchManager.purgeBranch(workingBranch);
               TestUtil.sleep(2000);
            }
         }
         if (BranchManager.branchExists(branch)) {
            BranchManager.purgeBranch(branch);
         }
         TestUtil.sleep(2000);

      } catch (BranchDoesNotExist ex) {
         // do nothing
      }
   }

   public static void commitBranch(TeamWorkFlowArtifact teamWf) throws Exception {
      IOperation op =
         AtsBranchManager.commitWorkingBranch(teamWf, false, true,
            AtsBranchManagerCore.getWorkingBranch(teamWf).getParentBranch(), true);
      Operations.executeWorkAndCheckStatus(op);
   }

   public static void createBranch(String namespace, TeamWorkFlowArtifact teamWf) throws Exception {
      Result result = AtsBranchManagerCore.createWorkingBranch_Validate(teamWf);
      if (result.isFalse()) {
         AWorkbench.popup(result);
         return;
      }
      AtsBranchManagerCore.createWorkingBranch_Create(teamWf);
      TestUtil.sleep(4000);
   }

   @After
   public void tearDown() throws Exception {
      cleanupBranchTest(BRANCH_VIA_VERSIONS);
      cleanupBranchTest(BRANCH_VIA_TEAM_DEFINITION);
   }

   public static AtsConfigOperation configureAts(String workDefinitionName, String teamDefName, Collection<String> versionNames, Collection<String> actionableItems) throws Exception {
      AtsConfigOperation.Display noDisplay = new MockAtsConfigDisplay();
      AtsConfigOperation atsConfigManagerOperation =
         new AtsConfigOperation(noDisplay, workDefinitionName, teamDefName, versionNames, actionableItems);
      Operations.executeWorkAndCheckStatus(atsConfigManagerOperation);
      TestUtil.sleep(2000);
      return atsConfigManagerOperation;
   }

   private static final class MockAtsConfigDisplay implements AtsConfigOperation.Display {
      @Override
      public void openAtsConfigurationEditors(IAtsTeamDefinition teamDef, Collection<IAtsActionableItem> aias, IAtsWorkDefinition workDefinition) {
         // Nothing to do - we have no display during testing
      }
   }
}
