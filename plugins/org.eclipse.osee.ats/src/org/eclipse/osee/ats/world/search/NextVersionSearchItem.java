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
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.core.config.TeamDefinitions;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.widgets.dialog.TeamDefinitionDialog;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;

/**
 * @author Donald G. Dunne
 */
public class NextVersionSearchItem extends WorldUISearchItem {

   private final IAtsTeamDefinition teamDefHoldingVersions;
   private IAtsTeamDefinition selectedTeamDef;
   private IAtsVersion selectedVersionArt;

   public NextVersionSearchItem(IAtsTeamDefinition teamDefHoldingVersions, LoadView loadView) {
      this(null, teamDefHoldingVersions, loadView);
   }

   public NextVersionSearchItem(String name, IAtsTeamDefinition teamDefHoldingVersions, LoadView loadView) {
      super(name != null ? name : "Workflows Targeted-For Next Version", loadView, FrameworkImage.VERSION_NEXT);
      this.teamDefHoldingVersions = teamDefHoldingVersions;
   }

   public NextVersionSearchItem(NextVersionSearchItem nextVersionSearchItem) {
      super(nextVersionSearchItem, FrameworkImage.VERSION);
      this.teamDefHoldingVersions = nextVersionSearchItem.teamDefHoldingVersions;
      this.selectedTeamDef = nextVersionSearchItem.selectedTeamDef;
   }

   @Override
   public String getSelectedName(SearchType searchType) throws OseeCoreException {
      String name = super.getName();
      IAtsTeamDefinition teamDef = getTeamDefinition();
      try {
         if (teamDef != null) {
            name += " - " + teamDef.getName();
            selectedVersionArt = teamDef.getNextReleaseVersion();
            name += selectedVersionArt != null ? " - " + selectedVersionArt.getName() : "";
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return "Exception Occurred - See Log - " + ex.getLocalizedMessage();
      }
      return name;
   }

   private IAtsTeamDefinition getTeamDefinition() {
      if (teamDefHoldingVersions != null) {
         return teamDefHoldingVersions;
      }
      return selectedTeamDef;
   }

   @Override
   public Collection<Artifact> performSearch(SearchType searchType) throws OseeCoreException {
      if (isCancelled()) {
         return EMPTY_SET;
      }
      if (getTeamDefinition().getNextReleaseVersion() == null) {
         AWorkbench.popup("ERROR", "No version marked as Next Release for \"" + getTeamDefinition() + "\"");
         return EMPTY_SET;
      }
      List<Artifact> arts = new ArrayList<Artifact>();
      arts.addAll(AtsClientService.get().getAtsVersionService().getTargetedForTeamWorkflowArtifacts(
         getTeamDefinition().getNextReleaseVersion()));
      if (isCancelled()) {
         return EMPTY_SET;
      }
      return arts;
   }

   @Override
   public void performUI(SearchType searchType) throws OseeCoreException {
      super.performUI(searchType);
      if (teamDefHoldingVersions != null) {
         return;
      }
      if (searchType == SearchType.ReSearch && selectedTeamDef != null) {
         return;
      }
      try {
         TeamDefinitionDialog ld = new TeamDefinitionDialog("Select Team", "Select Team");
         ld.setInput(TeamDefinitions.getTeamReleaseableDefinitions(Active.Active));
         int result = ld.open();
         if (result == 0) {
            selectedTeamDef = (IAtsTeamDefinition) ld.getResult()[0];
            return;
         } else {
            cancelled = true;
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      cancelled = true;
   }

   /**
    * @param selectedTeamDef the selectedTeamDef to set
    */
   public void setSelectedTeamDef(IAtsTeamDefinition selectedTeamDef) {
      this.selectedTeamDef = selectedTeamDef;
   }

   @Override
   public WorldUISearchItem copy() {
      return new NextVersionSearchItem(this);
   }

   /**
    * @return the selectedVersionArt
    */
   public IAtsVersion getSelectedVersionArt() {
      return selectedVersionArt;
   }

}
