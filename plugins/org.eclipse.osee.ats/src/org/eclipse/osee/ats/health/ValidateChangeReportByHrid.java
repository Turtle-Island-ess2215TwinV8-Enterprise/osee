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
package org.eclipse.osee.ats.health;

import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.database.core.OseeInfo;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.skynet.results.XResultData;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryCheckDialog;

/**
 * @author Donald G. Dunne
 */
public class ValidateChangeReportByHrid extends XNavigateItemAction {

   public ValidateChangeReportByHrid(XNavigateItem parent) throws OseeArgumentException {
      super(parent, "Validate Change Reports by HRID", PluginUiImage.ADMIN);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) {
      EntryCheckDialog ed = new EntryCheckDialog(getName(), "Enter HRID", "Display Was/Is data in Results View.");
      if (ed.open() == 0) {
         String hrid = ed.getEntry();
         if (hrid != null && !hrid.equals("")) {
            Jobs.startJob(new Report(getName(), hrid, ed.isChecked()), true);
         }
      }
   }

   public static class Report extends Job {

      private final String hrid;
      private final boolean exportWasIs;

      public Report(String name, String hrid, boolean exportWasIs) {
         super(name);
         this.hrid = hrid;
         this.exportWasIs = exportWasIs;
      }

      @Override
      protected IStatus run(IProgressMonitor monitor) {
         try {
            final XResultData rd = new XResultData();
            try {
               TeamWorkFlowArtifact teamArt =
                     (TeamWorkFlowArtifact) ArtifactQuery.getArtifactFromId(hrid, AtsUtil.getAtsBranch());
               String currentDbGuid = OseeInfo.getDatabaseGuid();
               ValidateChangeReports.changeReportValidated(currentDbGuid, teamArt, rd, exportWasIs);
            } catch (Exception ex) {
               rd.logError(ex.getLocalizedMessage());
            }
            rd.report(getName());
         } catch (Exception ex) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
            return new Status(Status.ERROR, AtsPlugin.PLUGIN_ID, -1, ex.getMessage(), ex);
         }
         monitor.done();
         return Status.OK_STATUS;
      }
   }

}
