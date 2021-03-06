/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.internal.store;

import java.util.List;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.core.client.internal.config.AtsArtifactConfigCache;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.config.TeamDefinitions;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;

/**
 * @author Donald G. Dunne
 */
public class TeamDefinitionArtifactWriter extends AbstractAtsArtifactWriter<IAtsTeamDefinition> {

   @Override
   public Artifact store(IAtsTeamDefinition teamDef, AtsArtifactConfigCache cache, SkynetTransaction transaction) throws OseeCoreException {
      Artifact artifact = getArtifactOrCreate(cache, AtsArtifactTypes.TeamDefinition, teamDef, transaction);
      store(teamDef, artifact, cache, transaction);
      return artifact;
   }

   @Override
   public Artifact store(IAtsTeamDefinition teamDef, Artifact artifact, AtsArtifactConfigCache cache, SkynetTransaction transaction) throws OseeCoreException {
      artifact.setName(teamDef.getName());
      artifact.setSoleAttributeValue(AtsAttributeTypes.Active, teamDef.isActive());
      boolean actionable = artifact.getSoleAttributeValue(AtsAttributeTypes.Actionable, false);
      if (actionable != teamDef.isActionable()) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.Actionable, teamDef.isActionable());
      }

      boolean allowCommitBranch = artifact.getSoleAttributeValue(AtsAttributeTypes.AllowCreateBranch, false);
      if (allowCommitBranch != teamDef.isAllowCommitBranch()) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.AllowCommitBranch, teamDef.isAllowCommitBranch());
      }

      boolean allowCreateBranch = artifact.getSoleAttributeValue(AtsAttributeTypes.AllowCreateBranch, false);
      if (allowCreateBranch != teamDef.isAllowCreateBranch()) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.AllowCreateBranch, teamDef.isAllowCreateBranch());
      }
      if (Strings.isValid(teamDef.getBaslineBranchGuid())) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.BaselineBranchGuid, teamDef.getBaslineBranchGuid());
      }
      if (Strings.isValid(teamDef.getWorkflowDefinition())) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.WorkflowDefinition, teamDef.getWorkflowDefinition());
      }
      if (Strings.isValid(teamDef.getRelatedTaskWorkDefinition())) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.RelatedTaskWorkDefinition,
            teamDef.getRelatedTaskWorkDefinition());
      }
      if (Strings.isValid(teamDef.getDescription())) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.Description, teamDef.getDescription());
      }
      if (Strings.isValid(teamDef.getFullName())) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.FullName, teamDef.getFullName());
      }

      // set new actionable items if necessary
      for (IAtsActionableItem aia : teamDef.getActionableItems()) {
         Artifact aiaArt = cache.getArtifact(aia);
         if (aiaArt != null && aiaArt.getRelatedArtifact(AtsRelationTypes.TeamActionableItem_Team) != null) {
            aiaArt.addRelation(AtsRelationTypes.TeamActionableItem_Team, artifact);
         }
      }

      // set new children team defs if changed
      List<String> newGuids = AtsObjects.toGuids(teamDef.getChildrenTeamDefinitions());
      List<String> currGuids = Artifacts.toGuids(artifact.getChildren());
      // remove curr children that are not part of new children
      for (Artifact child : artifact.getChildren()) {
         if (child.isOfType(AtsArtifactTypes.TeamDefinition)) {
            if (newGuids.contains(child.getGuid())) {
               artifact.deleteRelation(CoreRelationTypes.Default_Hierarchical__Child, child);
            }
         }
      }
      // add new children that are not part of curr children
      for (String newGuid : newGuids) {
         if (!currGuids.contains(newGuid)) {
            Artifact newArt = null;
            IAtsTeamDefinition newTeamDef = cache.getSoleByGuid(newGuid, IAtsTeamDefinition.class);
            if (newTeamDef != null) {
               newArt = cache.getSoleArtifact(newTeamDef);
            }
            // if not persisted yet, it should be in artifact cache
            if (newArt == null) {
               newArt = ArtifactCache.getActive(newGuid, AtsUtilCore.getAtsBranchToken());
            }
            artifact.addRelation(CoreRelationTypes.Default_Hierarchical__Child, newArt);
         }
      }

      // update relations for versions and users
      setRelationsOfType(cache, artifact, teamDef.getVersions(), AtsRelationTypes.TeamDefinitionToVersion_Version);
      setRelationsOfType(cache, artifact, teamDef.getSubscribed(), AtsRelationTypes.SubscribedUser_User);
      setRelationsOfType(cache, artifact, teamDef.getLeads(), AtsRelationTypes.TeamLead_Lead);
      setRelationsOfType(cache, artifact, teamDef.getMembers(), AtsRelationTypes.TeamMember_Member);
      setRelationsOfType(cache, artifact, teamDef.getPrivilegedMembers(), AtsRelationTypes.PrivilegedMember_Member);

      // update rules if changed
      artifact.setAttributeValues(AtsAttributeTypes.RuleDefinition, teamDef.getRules());

      // update staticIds
      if (!teamDef.getStaticIds().isEmpty()) {
         artifact.setAttributeValues(CoreAttributeTypes.StaticId, teamDef.getStaticIds());
      }

      // set parent artifact to top team def
      if (teamDef.getParentTeamDef() == null && !teamDef.getGuid().equals(
         TeamDefinitions.getTopTeamDefinition().getGuid())) {
         // if parent is null, add to top team definition
         Artifact topTeamDefArt = cache.getSoleArtifact(TeamDefinitions.getTopTeamDefinition());
         topTeamDefArt.addChild(artifact);
         topTeamDefArt.persist(transaction);
      } else {
         // else reset parent if necessary
         Artifact parentTeamDefArt = artifact.getParent();
         if (parentTeamDefArt != null) {
            if (parentTeamDefArt.isOfType(AtsArtifactTypes.TeamDefinition)) {
               if (!parentTeamDefArt.getGuid().equals(teamDef.getParentTeamDef().getGuid())) {
                  Artifact newParentTeamDefArt = cache.getSoleArtifact(teamDef);
                  newParentTeamDefArt.addChild(artifact);
                  newParentTeamDefArt.persist(transaction);
                  parentTeamDefArt.persist(transaction);
               }
            }
         }
      }
      artifact.persist(transaction);
      cache.cache(teamDef);
      return artifact;
   }
}
