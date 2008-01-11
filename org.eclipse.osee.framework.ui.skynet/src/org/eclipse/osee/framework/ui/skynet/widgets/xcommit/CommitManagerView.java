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

package org.eclipse.osee.framework.ui.skynet.widgets.xcommit;

import java.sql.SQLException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.plugin.util.Jobs;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.ats.IActionable;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.IBranchArtifact;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;

/**
 * @see ViewPart
 * @author Donald G. Dunne
 */
public class CommitManagerView extends ViewPart implements IActionable {

   public static final String VIEW_ID = "org.eclipse.osee.framework.ui.skynet.widgets.xcommit.CommitManagerView";
   private static String HELP_CONTEXT_ID = "CommitManagerView";
   private XCommitViewer xCommitViewer;
   private IBranchArtifact branchArtifact;

   /**
    * @author Donald G. Dunne
    */
   public CommitManagerView() {
   }

   public static void openViewUpon(final IBranchArtifact branchArtifact) {
      Job job = new Job("Open Change Manager") {

         @Override
         protected IStatus run(IProgressMonitor monitor) {
            Displays.ensureInDisplayThread(new Runnable() {
               public void run() {
                  try {
                     IWorkbenchPage page = AWorkbench.getActivePage();
                     CommitManagerView commitManagerView =
                           (CommitManagerView) page.showView(VIEW_ID,
                                 String.valueOf(branchArtifact.getWorkingBranch().getBranchId()),
                                 IWorkbenchPage.VIEW_ACTIVATE);
                     commitManagerView.explore(branchArtifact);

                  } catch (Exception ex) {
                     OSEELog.logException(SkynetGuiPlugin.class, ex, true);
                  }
               }
            });

            monitor.done();
            return Status.OK_STATUS;
         }
      };

      Jobs.startJob(job);
   }

   @Override
   public void dispose() {
      super.dispose();
   }

   public void setFocus() {
   }

   /*
    * @see IWorkbenchPart#createPartControl(Composite)
    */
   public void createPartControl(Composite parent) {
      /*
       * Create a grid layout object so the text and treeviewer are layed out the way I want.
       */
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      layout.verticalSpacing = 0;
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      parent.setLayout(layout);
      parent.setLayoutData(new GridData(GridData.FILL_BOTH));

      xCommitViewer = new XCommitViewer();
      xCommitViewer.setDisplayLabel(false);
      xCommitViewer.createWidgets(parent, 1);
      try {
         if (branchArtifact != null) xCommitViewer.setArtifact(branchArtifact.getArtifact(), "");
      } catch (SQLException ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }

      SkynetGuiPlugin.getInstance().setHelp(parent, HELP_CONTEXT_ID);

   }

   public void explore(IBranchArtifact branchArtifact) {
      this.branchArtifact = branchArtifact;
      try {
         if (xCommitViewer != null && branchArtifact != null) xCommitViewer.setArtifact(branchArtifact.getArtifact(),
               "");
         setPartName("Commit Manager: " + branchArtifact.getWorkingBranch().getBranchShortestName());
      } catch (SQLException ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
   }

   public String getActionDescription() {
      return "";
   }

}