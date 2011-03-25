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

package org.eclipse.osee.ats.version;

import java.util.Date;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.artifact.TeamDefinitionManager;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.artifact.VersionArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.dialog.TeamDefinitionDialog;
import org.eclipse.osee.ats.util.widgets.dialog.VersionListDialog;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.MultipleAttributesExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Donald G. Dunne
 */
public class ReleaseVersionItem extends XNavigateItemAction {

   public static String strs[] = new String[] {};
   private final TeamDefinitionArtifact teamDefHoldingVersions;

   /**
    * @param teamDefHoldingVersions Team Definition Artifact that is related to versions or null for popup selection
    */
   public ReleaseVersionItem(XNavigateItem parent, TeamDefinitionArtifact teamDefHoldingVersions) {
      super(parent, "Release " + (teamDefHoldingVersions != null ? teamDefHoldingVersions + " " : "") + "Version",
         FrameworkImage.VERSION);
      this.teamDefHoldingVersions = teamDefHoldingVersions;
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) throws OseeCoreException {
      TeamDefinitionArtifact teamDefHoldingVersions = getReleaseableTeamDefinitionArtifact();
      if (teamDefHoldingVersions == null) {
         return;
      }
      try {
         VersionListDialog ld =
            new VersionListDialog("Select Version", "Select Version to Release",
               teamDefHoldingVersions.getVersionsArtifacts(VersionReleaseType.UnReleased, VersionLockedType.Both));
         int result = ld.open();
         if (result == 0) {
            VersionArtifact verArt = (VersionArtifact) ld.getResult()[0];

            // Validate team lead status
            if (!AtsUtil.isAtsAdmin() && !verArt.getParentTeamDefinition().getLeads().contains(UserManager.getUser())) {
               AWorkbench.popup("ERROR", "Only lead can release version.");
               return;
            }
            // Validate that all Team Workflows are Completed or Cancelled
            String errorStr = null;
            for (TeamWorkFlowArtifact team : verArt.getTargetedForTeamArtifacts()) {
               if (!team.isCancelled() && !team.isCompleted()) {
                  errorStr =
                     "All Team Workflows must be either Completed or " + "Cancelled before releasing a version.\n\n" + team.getHumanReadableId() + " - is in the\"" + team.getStateMgr().getCurrentStateName() + "\" state.";
               }
            }
            if (errorStr != null) {
               AWorkbench.popup("ERROR", errorStr);
            }
            if (errorStr != null && !AtsUtil.isAtsAdmin()) {
               return;
            } else if (errorStr != null && !MessageDialog.openConfirm(Displays.getActiveShell(), "Override",
               "ATS Admin Enabled - Override completed condition and release anyway?")) {
               return;
            }

            verArt.setSoleAttributeValue(AtsAttributeTypes.Released, true);
            verArt.setSoleAttributeValue(AtsAttributeTypes.ReleaseDate, new Date());
            verArt.setNextVersion(false);
            verArt.persist();

            if (MessageDialog.openQuestion(Displays.getActiveShell(), "Select NEW Next Release Version",
               "Release Complete.\n\nSelect NEW Next Release Version?")) {
               ld =
                  new VersionListDialog("Select Next Release Version", "Select New Next Release Version",
                     teamDefHoldingVersions.getVersionsArtifacts());
               result = ld.open();
               if (result == 0) {
                  verArt = (VersionArtifact) ld.getResult()[0];
                  verArt.setNextVersion(true);
                  verArt.persist();
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, "Error releasing version");
      }
   }

   public TeamDefinitionArtifact getReleaseableTeamDefinitionArtifact() throws OseeCoreException {
      if (teamDefHoldingVersions != null) {
         return teamDefHoldingVersions;
      }
      TeamDefinitionDialog ld = new TeamDefinitionDialog("Select Team", "Select Team");
      try {
         ld.setInput(TeamDefinitionManager.getTeamReleaseableDefinitions(Active.Active));
      } catch (MultipleAttributesExist ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
      int result = ld.open();
      if (result == 0) {
         return (TeamDefinitionArtifact) ld.getResult()[0];
      }
      return null;
   }
}