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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionManager;
import org.eclipse.osee.ats.core.util.AtsCacheManager;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.ats.util.widgets.dialog.TeamDefinitionDialog;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.MultipleAttributesExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;
import org.eclipse.osee.framework.ui.skynet.results.html.XResultPage.Manipulations;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Donald G. Dunne
 */
public class GenerateFullVersionReportItem extends XNavigateItemAction {

   private final TeamDefinitionArtifact teamDef;
   private final String teamDefName;

   public GenerateFullVersionReportItem(XNavigateItem parent) {
      super(parent, "Generate Full Version Report", FrameworkImage.VERSION);
      this.teamDefName = null;
      this.teamDef = null;
   }

   public GenerateFullVersionReportItem(XNavigateItem parent, TeamDefinitionArtifact teamDef) {
      super(parent, "Generate Full Version Report", FrameworkImage.VERSION);
      this.teamDefName = null;
      this.teamDef = teamDef;
   }

   public GenerateFullVersionReportItem(XNavigateItem parent, String teamDefName) {
      super(parent, "Generate Full Version Report", FrameworkImage.VERSION);
      this.teamDefName = teamDefName;
      this.teamDef = null;
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) throws OseeCoreException {
      TeamDefinitionArtifact teamDef = getTeamDefinition();
      if (teamDef == null) {
         return;
      }
      if (!MessageDialog.openConfirm(Displays.getActiveShell(), getName(), getName())) {
         return;
      }
      PublishReportJob job = new PublishReportJob(teamDef);
      job.setUser(true);
      job.setPriority(Job.LONG);
      job.schedule();
   }

   public TeamDefinitionArtifact getTeamDefinition() throws OseeCoreException {
      if (teamDef != null) {
         return teamDef;
      }
      if (Strings.isValid(teamDefName)) {
         TeamDefinitionArtifact teamDef =
            (TeamDefinitionArtifact) AtsCacheManager.getSoleArtifactByName(AtsArtifactTypes.TeamDefinition, teamDefName);

         if (teamDef != null) {
            return teamDef;
         }
      }
      TeamDefinitionDialog ld = new TeamDefinitionDialog("Select Team", "Select Team");
      try {
         ld.setInput(TeamDefinitionManager.getTeamReleaseableDefinitions(Active.Active));
      } catch (MultipleAttributesExist ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      int result = ld.open();
      if (result == 0) {
         return (TeamDefinitionArtifact) ld.getResult()[0];
      }
      return null;
   }

   private static class PublishReportJob extends Job {

      private final TeamDefinitionArtifact teamDef;

      public PublishReportJob(TeamDefinitionArtifact teamDef) {
         super(teamDef.getName() + " as of " + DateUtil.getDateNow());
         this.teamDef = teamDef;
      }

      @Override
      public IStatus run(IProgressMonitor monitor) {
         try {
            String html = VersionReportJob.getFullReleaseReport(teamDef, monitor);
            XResultData rd = new XResultData();
            rd.addRaw(html);
            XResultDataUI.report(rd,getName(), Manipulations.RAW_HTML);
         } catch (Exception ex) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, ex.toString(), ex);
         }

         monitor.done();
         return Status.OK_STATUS;
      }
   }

}
