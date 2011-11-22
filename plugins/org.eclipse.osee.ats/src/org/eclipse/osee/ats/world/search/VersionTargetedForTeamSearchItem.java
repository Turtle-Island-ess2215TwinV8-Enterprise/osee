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

package org.eclipse.osee.ats.world.search;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.osee.ats.core.action.ActionArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionManager;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.version.VersionArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.shared.VersionLockedType;
import org.eclipse.osee.ats.shared.VersionReleaseType;
import org.eclipse.osee.ats.util.widgets.dialog.TeamDefinitionDialog;
import org.eclipse.osee.ats.util.widgets.dialog.VersionListDialog;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;

/**
 * @author Donald G. Dunne
 */
public class VersionTargetedForTeamSearchItem extends WorldUISearchItem {
   private final VersionArtifact versionArt;
   private VersionArtifact selectedVersionArt;
   private final boolean returnAction;
   private final TeamDefinitionArtifact teamDef;

   public VersionTargetedForTeamSearchItem(TeamDefinitionArtifact teamDef, VersionArtifact versionArt, boolean returnAction, LoadView loadView) {
      this(null, teamDef, versionArt, returnAction, loadView);
   }

   public VersionTargetedForTeamSearchItem(String name, TeamDefinitionArtifact teamDef, VersionArtifact versionArt, boolean returnAction, LoadView loadView) {
      super(name != null ? name : (returnAction ? "Actions" : "Workflows") + " Targeted-For Version", loadView,
         FrameworkImage.VERSION);
      this.teamDef = teamDef;
      this.versionArt = versionArt;
      this.returnAction = returnAction;
   }

   public VersionTargetedForTeamSearchItem(VersionTargetedForTeamSearchItem versionTargetedForTeamSearchItem) {
      super(versionTargetedForTeamSearchItem, FrameworkImage.VERSION);
      this.versionArt = versionTargetedForTeamSearchItem.versionArt;
      this.returnAction = versionTargetedForTeamSearchItem.returnAction;
      this.teamDef = versionTargetedForTeamSearchItem.teamDef;
   }

   @Override
   public String getSelectedName(SearchType searchType) throws OseeCoreException {
      if (getSearchVersionArtifact() != null) {
         return super.getName() + " - " + getSearchVersionArtifact();
      }
      return "";
   }

   public VersionArtifact getSearchVersionArtifact() {
      if (versionArt != null) {
         return versionArt;
      }
      return selectedVersionArt;
   }

   @Override
   public Collection<Artifact> performSearch(SearchType searchType) throws OseeCoreException {

      if (getSearchVersionArtifact() == null) {
         throw new OseeArgumentException("Invalid release version");
      }

      ArrayList<Artifact> arts = new ArrayList<Artifact>();
      for (Artifact art : getSearchVersionArtifact().getTargetedForTeamArtifacts()) {
         if (returnAction) {
            ActionArtifact parentAction = ((TeamWorkFlowArtifact) art).getParentActionArtifact();
            if (parentAction != null) {
               arts.add(parentAction);
            }
         } else {
            arts.add(art);
         }
      }
      if (isCancelled()) {
         return EMPTY_SET;
      }
      return arts;
   }

   @Override
   public void performUI(SearchType searchType) throws OseeCoreException {
      super.performUI(searchType);
      if (searchType == SearchType.ReSearch && selectedVersionArt != null) {
         return;
      }
      if (versionArt != null) {
         return;
      }
      try {
         TeamDefinitionArtifact selectedTeamDef = teamDef;
         if (versionArt == null && selectedTeamDef == null) {
            TeamDefinitionDialog ld = new TeamDefinitionDialog("Select Team", "Select Team");
            ld.setInput(TeamDefinitionManager.getTeamReleaseableDefinitions(Active.Both));
            int result = ld.open();
            if (result == 0) {
               selectedTeamDef = (TeamDefinitionArtifact) ld.getResult()[0];
            } else {
               cancelled = true;
            }
         }
         if (versionArt == null && selectedTeamDef != null) {
            final VersionListDialog vld =
               new VersionListDialog("Select Version", "Select Version", selectedTeamDef.getVersionsArtifacts(
                  VersionReleaseType.Both, VersionLockedType.Both));
            if (vld.open() == 0) {
               selectedVersionArt = (VersionArtifact) vld.getResult()[0];
               return;
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      cancelled = true;
   }

   /**
    * @param selectedVersionArt the selectedVersionArt to set
    */
   public void setSelectedVersionArt(VersionArtifact selectedVersionArt) {
      this.selectedVersionArt = selectedVersionArt;
   }

   @Override
   public WorldUISearchItem copy() {
      return new VersionTargetedForTeamSearchItem(this);
   }

}
