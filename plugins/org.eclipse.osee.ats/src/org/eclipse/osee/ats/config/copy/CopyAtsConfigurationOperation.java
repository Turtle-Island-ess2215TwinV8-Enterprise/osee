/*
 * Created on Mar 29, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.config.copy;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.artifact.ActionableItemArtifact;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.skynet.results.XResultData;

public class CopyAtsConfigurationOperation extends AbstractOperation {

   private final ConfigData data;
   protected XResultData resultData;
   Set<Artifact> newArtifacts;
   Set<Artifact> existingArtifacts;
   Set<Artifact> processedFromAis;

   private final Map<TeamDefinitionArtifact, TeamDefinitionArtifact> fromTeamDefToNewTeamDefMap =
      new HashMap<TeamDefinitionArtifact, TeamDefinitionArtifact>();

   public CopyAtsConfigurationOperation(ConfigData data, XResultData resultData) {
      super("Copy ATS Configuration", AtsPlugin.PLUGIN_ID);
      this.data = data;
      this.resultData = resultData;
   }

   protected CopyAtsValidation getCopyAtsValidation() {
      return new CopyAtsValidation(data, resultData);
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      try {
         data.validateData(resultData);
         if (resultData.isErrors()) {
            return;
         }

         getCopyAtsValidation().validate();
         if (resultData.isErrors()) {
            persistOrUndoChanges();
            return;
         }

         if (data.isPersistChanges()) {
            resultData.log("Persisting Changes");
         } else {
            resultData.log("Report-Only, Changes are not persisted");
         }

         newArtifacts = new HashSet<Artifact>(50);
         existingArtifacts = new HashSet<Artifact>(50);
         processedFromAis = new HashSet<Artifact>(10);

         createTeamDefinitions(data.getTeamDef(), data.getParentTeamDef());
         if (resultData.isErrors()) {
            persistOrUndoChanges();
            return;
         }

         createActionableItems(data.getActionableItem(), data.getParentActionableItem());

         if (resultData.isErrors()) {
            persistOrUndoChanges();
            return;
         }

         persistOrUndoChanges();
         resultData.report(getName());
      } finally {
         monitor.subTask("Done");
      }
   }

   /**
    * Has potential of returning null if this fromAi has already been processed.
    */
   protected ActionableItemArtifact createActionableItems(ActionableItemArtifact fromAi, ActionableItemArtifact parentAiArt) throws OseeCoreException {
      if (processedFromAis.contains(fromAi)) {
         resultData.log(String.format("Skipping already processed fromAi [%s]", fromAi));
         return null;
      } else {
         processedFromAis.add(fromAi);
      }
      // Get or create new team definition
      ActionableItemArtifact newAiArt = (ActionableItemArtifact) duplicateTeamDefinitionOrActionableItem(fromAi);
      parentAiArt.addChild(newAiArt);
      existingArtifacts.add(parentAiArt);
      newArtifacts.add(newAiArt);
      // Relate new Ais to their TeamDefs just like other config
      for (Artifact fromTeamDefArt : fromAi.getRelatedArtifacts(AtsRelationTypes.TeamActionableItem_Team,
         TeamDefinitionArtifact.class)) {
         TeamDefinitionArtifact newTeamDefArt = fromTeamDefToNewTeamDefMap.get(fromTeamDefArt);
         if (newTeamDefArt == null) {
            resultData.logWarningWithFormat(
               "No related Team Definition [%s] in scope for AI [%s].  Configure by hand.", fromTeamDefArt, newAiArt);
         } else {
            newAiArt.addRelation(AtsRelationTypes.TeamActionableItem_Team, newTeamDefArt);
         }
      }
      // Handle all children
      for (Artifact childFromAiArt : fromAi.getChildren()) {
         if (childFromAiArt instanceof ActionableItemArtifact) {
            createActionableItems((ActionableItemArtifact) childFromAiArt, newAiArt);
         }
      }
      return newAiArt;
   }

   protected TeamDefinitionArtifact createTeamDefinitions(TeamDefinitionArtifact fromTeamDef, TeamDefinitionArtifact parentTeamDef) throws OseeCoreException {
      // Get or create new team definition
      TeamDefinitionArtifact newTeamDef = (TeamDefinitionArtifact) duplicateTeamDefinitionOrActionableItem(fromTeamDef);
      parentTeamDef.addChild(newTeamDef);
      existingArtifacts.add(parentTeamDef);
      newArtifacts.add(newTeamDef);
      fromTeamDefToNewTeamDefMap.put(fromTeamDef, newTeamDef);
      if (data.isRetainTeamLeads()) {
         duplicateTeamLeadsAndMembers(fromTeamDef, newTeamDef);
      }
      duplicateWorkItems(fromTeamDef, newTeamDef);
      // handle all children
      for (Artifact childFromTeamDef : fromTeamDef.getChildren()) {
         if (childFromTeamDef instanceof TeamDefinitionArtifact) {
            createTeamDefinitions((TeamDefinitionArtifact) childFromTeamDef, newTeamDef);
         }
      }
      return newTeamDef;
   }

   private void duplicateWorkItems(TeamDefinitionArtifact fromTeamDef, TeamDefinitionArtifact newTeamDef) throws OseeCoreException {
      Collection<Artifact> workItems = newTeamDef.getRelatedArtifacts(CoreRelationTypes.WorkItem__Child);
      for (Artifact workChild : fromTeamDef.getRelatedArtifacts(CoreRelationTypes.WorkItem__Child)) {
         if (!workItems.contains(workChild)) {
            existingArtifacts.add(workChild);
            newTeamDef.addRelation(CoreRelationTypes.WorkItem__Child, workChild);
            resultData.log("   - Adding work child " + workChild);
         }
      }
   }

   private void duplicateTeamLeadsAndMembers(TeamDefinitionArtifact fromTeamDef, TeamDefinitionArtifact newTeamDef) throws OseeCoreException {
      Collection<Artifact> leads = newTeamDef.getRelatedArtifacts(AtsRelationTypes.TeamLead_Lead);
      for (Artifact user : fromTeamDef.getRelatedArtifacts(AtsRelationTypes.TeamLead_Lead)) {
         if (!leads.contains(user)) {
            existingArtifacts.add(user);
            newTeamDef.addRelation(AtsRelationTypes.TeamLead_Lead, user);
            resultData.log("   - Relating team lead " + user);
         }
      }
      Collection<Artifact> members = newTeamDef.getRelatedArtifacts(AtsRelationTypes.TeamMember_Member);
      for (Artifact user : fromTeamDef.getRelatedArtifacts(AtsRelationTypes.TeamMember_Member)) {
         if (!members.contains(user)) {
            existingArtifacts.add(user);
            newTeamDef.addRelation(AtsRelationTypes.TeamMember_Member, user);
            resultData.log("   - Relating team member " + user);
         }
      }
      for (Artifact user : fromTeamDef.getRelatedArtifacts(AtsRelationTypes.PrivilegedMember_Member)) {
         if (!members.contains(user)) {
            existingArtifacts.add(user);
            newTeamDef.addRelation(AtsRelationTypes.PrivilegedMember_Member, user);
            resultData.log("   - Relating priviledged member " + user);
         }
      }
   }

   private void persistOrUndoChanges() throws OseeCoreException {
      if (data.isPersistChanges()) {
         SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "Copy ATS Configuration");
         for (Artifact art : newArtifacts) {
            art.persist(transaction);
         }
         transaction.execute();
      } else {
         resultData.log("\n\nCleanup of created / modified artifacts\n\n");
         for (Artifact artifact : newArtifacts) {
            if (artifact.isInDb()) {
               resultData.logErrorWithFormat("Attempt to purge artifact in db [%s]", artifact);
            } else {
               resultData.log("purging " + artifact.toStringWithId());
               artifact.purgeFromBranch();
            }
         }
         for (Artifact artifact : existingArtifacts) {
            if (artifact.isInDb()) {
               resultData.log("undoing changes " + artifact.toStringWithId());
               artifact.reloadAttributesAndRelations();
            } else {
               resultData.logErrorWithFormat("Attempt to reload artifact not in db [%s]", artifact);
            }
         }
      }
   }

   private Artifact duplicateTeamDefinitionOrActionableItem(Artifact fromArtifact) throws OseeCoreException {
      String newName = CopyAtsUtil.getConvertedName(data, fromArtifact.getName());
      if (newName.equals(fromArtifact.getName())) {
         throw new OseeArgumentException("Could not get new name from name conversion.");
      }
      // duplicate all but baseline branch guid
      Artifact newTeamDef =
         fromArtifact.duplicate(AtsUtil.getAtsBranch(), Arrays.asList(AtsAttributeTypes.BaselineBranchGuid));
      newTeamDef.setName(newName);
      resultData.log("Creating new " + newTeamDef.getArtifactTypeName() + ": " + newTeamDef);
      String fullName = newTeamDef.getSoleAttributeValue(AtsAttributeTypes.FullName, null);
      if (fullName != null) {
         String newFullName = CopyAtsUtil.getConvertedName(data, fullName);
         if (!newFullName.equals(fullName)) {
            newTeamDef.setSoleAttributeFromString(AtsAttributeTypes.FullName, newFullName);
            resultData.log("   - Converted \"ats.Full Name\" to " + newFullName);
         }
      }
      newArtifacts.add(newTeamDef);
      return newTeamDef;
   }

}