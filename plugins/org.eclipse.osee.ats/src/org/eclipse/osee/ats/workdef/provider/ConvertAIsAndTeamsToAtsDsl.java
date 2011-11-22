/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.workdef.provider;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.ats.core.config.ActionableItemArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionManager;
import org.eclipse.osee.ats.core.workflow.ActionableItemManagerCore;
import org.eclipse.osee.ats.dsl.atsDsl.ActionableItemDef;
import org.eclipse.osee.ats.dsl.atsDsl.AtsDsl;
import org.eclipse.osee.ats.dsl.atsDsl.BooleanDef;
import org.eclipse.osee.ats.dsl.atsDsl.TeamDef;
import org.eclipse.osee.ats.dsl.atsDsl.UserByName;
import org.eclipse.osee.ats.dsl.atsDsl.VersionDef;
import org.eclipse.osee.ats.dsl.atsDsl.impl.AtsDslFactoryImpl;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.shared.AtsAttributeTypes;
import org.eclipse.osee.ats.shared.AtsRelationTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * Take existing AIs, TeamDefs and Versions and create AtsDsl
 */
public class ConvertAIsAndTeamsToAtsDsl {

   private final XResultData resultData;
   private AtsDsl atsDsl;
   private final Map<String, TeamDef> dslTeamDefs = new HashMap<String, TeamDef>();
   private final Map<String, ActionableItemDef> dslAIDefs = new HashMap<String, ActionableItemDef>();

   public ConvertAIsAndTeamsToAtsDsl(XResultData resultData) {
      this.resultData = resultData;
   }

   public AtsDsl convert(String definitionName) {
      resultData.log("Converting AIs and Teams to ATS DSL");
      atsDsl = AtsDslFactoryImpl.init().createAtsDsl();

      try {
         // Add all TeamDef definitions
         TeamDef topTeam = convertTeamDef(TeamDefinitionManager.getTopTeamDefinition(), null);
         atsDsl.getTeamDef().add(topTeam);

         // Add all AI definitions
         ActionableItemDef topAi = convertAIDef(ActionableItemManagerCore.getTopActionableItem(), null);
         atsDsl.getActionableItemDef().add(topAi);

      } catch (OseeCoreException ex) {
         resultData.logError("Exception: " + ex.getLocalizedMessage());
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return atsDsl;
   }

   private ActionableItemDef convertAIDef(ActionableItemArtifact aiArt, ActionableItemDef dslParentAIDef) throws OseeCoreException {
      ActionableItemDef dslAIDef = AtsDslFactoryImpl.init().createActionableItemDef();
      if (dslParentAIDef != null) {
         dslParentAIDef.getChildren().add(dslAIDef);
      }
      dslAIDef.setName(aiArt.getName());
      dslAIDefs.put(aiArt.getName(), dslAIDef);
      if (aiArt.getSoleAttributeValue(AtsAttributeTypes.Active, false)) {
         dslAIDef.setActive(BooleanDef.TRUE);
      }
      if (aiArt.getSoleAttributeValue(AtsAttributeTypes.Actionable, false)) {
         dslAIDef.setActionable(BooleanDef.TRUE);
      }
      for (String staticId : aiArt.getAttributesToStringList(CoreAttributeTypes.StaticId)) {
         dslAIDef.getStaticId().add(staticId);
      }
      for (User user : aiArt.getLeads()) {
         dslAIDef.getLead().add(getUserByName(user));
      }
      try {
         Artifact teamDef = aiArt.getRelatedArtifact(AtsRelationTypes.TeamActionableItem_Team);
         if (teamDef != null) {
            dslAIDef.setTeamDef(teamDef.getName());
         }
      } catch (ArtifactDoesNotExist ex) {
         // do nothing
      }
      // process children
      for (Artifact childAiArt : aiArt.getChildren()) {
         convertAIDef((ActionableItemArtifact) childAiArt, dslAIDef);
      }
      return dslAIDef;
   }

   private TeamDef convertTeamDef(TeamDefinitionArtifact teamDef, TeamDef dslParentTeamDef) throws OseeCoreException {
      TeamDef dslTeamDef = AtsDslFactoryImpl.init().createTeamDef();
      if (dslParentTeamDef != null) {
         dslParentTeamDef.getChildren().add(dslTeamDef);
      }

      dslTeamDef.setName(teamDef.getName());
      dslTeamDefs.put(teamDef.getName(), dslTeamDef);
      if (teamDef.getSoleAttributeValue(AtsAttributeTypes.Active, false)) {
         dslTeamDef.setActive(BooleanDef.TRUE);
      }
      if (teamDef.getSoleAttributeValue(AtsAttributeTypes.TeamUsesVersions, false)) {
         dslTeamDef.setUsesVersions(BooleanDef.TRUE);
      }
      for (String staticId : teamDef.getAttributesToStringList(CoreAttributeTypes.StaticId)) {
         dslTeamDef.getStaticId().add(staticId);
      }
      for (IBasicUser user : teamDef.getLeads()) {
         dslTeamDef.getLead().add(getUserByName(user));
      }
      for (IBasicUser user : teamDef.getMembers()) {
         dslTeamDef.getMember().add(getUserByName(user));
      }
      for (User user : teamDef.getPrivilegedMembers()) {
         dslTeamDef.getPriviledged().add(getUserByName(user));
      }
      for (Artifact verArt : teamDef.getVersionsArtifacts()) {
         convertVersionArtifact(dslTeamDef, verArt, teamDef);
      }
      // process children
      for (Artifact childAiArt : teamDef.getChildren()) {
         convertTeamDef((TeamDefinitionArtifact) childAiArt, dslTeamDef);
      }
      return dslTeamDef;
   }

   private void convertVersionArtifact(TeamDef dslTeamDef, Artifact art, TeamDefinitionArtifact teamDef) throws OseeCoreException {
      VersionDef dslVerDef = AtsDslFactoryImpl.init().createVersionDef();
      dslVerDef.setName(art.getName());
      if (art.getSoleAttributeValue(AtsAttributeTypes.NextVersion, false)) {
         dslVerDef.setNext(BooleanDef.TRUE);
      }
      for (String staticId : teamDef.getAttributesToStringList(CoreAttributeTypes.StaticId)) {
         dslVerDef.getStaticId().add(staticId);
      }
      if (art.getSoleAttributeValue(AtsAttributeTypes.Released, false)) {
         dslVerDef.setReleased(BooleanDef.TRUE);
      }
      if (art.getSoleAttributeValue(AtsAttributeTypes.AllowCommitBranch, false)) {
         dslVerDef.setAllowCommitBranch(BooleanDef.TRUE);
      }
      if (art.getSoleAttributeValue(AtsAttributeTypes.AllowCreateBranch, false)) {
         dslVerDef.setAllowCreateBranch(BooleanDef.TRUE);
      }
      if (art.getSoleAttributeValue(AtsAttributeTypes.BaselineBranchGuid, null) != null) {
         dslVerDef.setBaselineBranchGuid((String) art.getSoleAttributeValue(AtsAttributeTypes.BaselineBranchGuid));
      }
      dslTeamDef.getVersion().add(dslVerDef);
   }

   private UserByName getUserByName(IBasicUser user) {
      UserByName userByName = AtsDslFactoryImpl.init().createUserByName();
      userByName.setUserName(user.getName());
      return userByName;
   }
}
