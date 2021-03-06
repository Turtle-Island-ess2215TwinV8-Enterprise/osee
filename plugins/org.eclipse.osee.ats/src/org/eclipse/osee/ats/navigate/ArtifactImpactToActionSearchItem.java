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
package org.eclipse.osee.ats.navigate;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.INCLUDE_DELETED;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.search.QueryOptions;
import org.eclipse.osee.framework.skynet.core.revision.ChangeManager;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.ArtifactCheckTreeDialog;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialogWithBranchSelect;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Donald G. Dunne
 */
public class ArtifactImpactToActionSearchItem extends XNavigateItemAction {

   private final static String TITLE = "Search Artifact Impact to Action";

   public ArtifactImpactToActionSearchItem(XNavigateItem parent) {
      super(parent, TITLE, FrameworkImage.FLASHLIGHT);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) {
      EntryDialogWithBranchSelect ed =
         new EntryDialogWithBranchSelect(getName(), "Enter Artifact Name (or string) to search (no wildcards)");
      if (ed.open() == 0) {
         ActionToArtifactImpactJob job = new ActionToArtifactImpactJob(ed.getEntry(), ed.getBranch());
         job.setUser(true);
         job.setPriority(Job.LONG);
         job.schedule();
      }
   }

   public static class ActionToArtifactImpactJob extends Job {
      private IProgressMonitor monitor;
      private final String artifactName;
      private final XResultData rd = new XResultData();
      private final IOseeBranch branch;

      public ActionToArtifactImpactJob(String artifactName, IOseeBranch branch) {
         super("Searching \"" + artifactName + "\"...");
         this.artifactName = artifactName;
         this.branch = branch;
      }

      @Override
      public IStatus run(IProgressMonitor monitor) {
         this.monitor = monitor;
         try {
            getMatrixItems();
            XResultDataUI.report(rd, TITLE + " - \"" + artifactName + "\"");
            return Status.OK_STATUS;
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, ex.getMessage(), ex);
         }
      }

      private void getMatrixItems() throws OseeCoreException {
         final Collection<Artifact> srchArts =
            ArtifactQuery.getArtifactListFromName(artifactName, branch, INCLUDE_DELETED,
               QueryOptions.CONTAINS_MATCH_OPTIONS);
         final Set<Artifact> processArts = new HashSet<Artifact>();
         if (srchArts.isEmpty()) {
            return;
         }
         if (srchArts.size() > 1) {
            Displays.pendInDisplayThread(new Runnable() {
               @Override
               public void run() {
                  ArtifactCheckTreeDialog dialog = new ArtifactCheckTreeDialog(srchArts);
                  dialog.setTitle(TITLE);
                  dialog.setMessage("Select Artifacts to Search");
                  if (dialog.open() == 0) {
                     processArts.addAll(dialog.getSelection());
                  }
               }
            });

         } else {
            processArts.addAll(srchArts);
         }
         int x = 1;
         rd.log("Artifact Impact to Action for artifact(s) on branch \"" + branch.getName() + "\"");

         HashCollection<Artifact, TransactionRecord> transactionMap =
            ChangeManager.getModifingTransactions(processArts);
         HashCollection<Artifact, Branch> branchMap = ChangeManager.getModifingBranches(processArts);
         for (Artifact srchArt : processArts) {
            String str = String.format("Processing %d/%d - %s ", x++, processArts.size(), srchArt.getName());
            // System.out.println(str);
            rd.log("\n" + AHTML.bold(srchArt.getName()));
            monitor.subTask(str);
            int y = 1;
            rd.addRaw(AHTML.beginMultiColumnTable(95, 1));
            rd.addRaw(AHTML.addHeaderRowMultiColumnTable(new String[] {"Type", "Status", "HRID", "Title"}));

            // Check for changes on working branches
            boolean workingBranchesFound = false;

            Collection<Branch> branches = branchMap.getValues(srchArt);
            if (branches != null) {
               for (Branch branch : branches) {
                  Artifact assocArt = BranchManager.getAssociatedArtifact(branch);
                  if (assocArt != null && !assocArt.equals(AtsCoreUsers.SYSTEM_USER)) {
                     rd.addRaw(AHTML.addRowMultiColumnTable(new String[] {
                        assocArt.getArtifactTypeName(),
                        "Working",
                        XResultDataUI.getHyperlink(assocArt),
                        assocArt.getName()}));
                  } else {
                     rd.addRaw(AHTML.addRowMultiColumnTable(new String[] {"Branch", "", branch.getName()}));
                  }
                  workingBranchesFound = true;
               }
            }
            if (!workingBranchesFound) {
               rd.addRaw(AHTML.addRowSpanMultiColumnTable("No Impacting Working Branches Found", 3));
            }
            // Add committed changes
            boolean committedChanges = false;
            Collection<TransactionRecord> transactions = transactionMap.getValues(srchArt);
            if (transactions != null) {
               for (TransactionRecord transactionId : transactions) {
                  String transStr = String.format("Tranaction %d/%d", y++, transactions.size());
                  // System.out.println(transStr);
                  monitor.subTask(transStr);
                  if (transactionId.getCommit() > 0) {
                     Artifact assocArt =
                        ArtifactQuery.getArtifactFromId(transactionId.getCommit(), BranchManager.getCommonBranch());
                     if (assocArt.isOfType(AtsArtifactTypes.TeamWorkflow)) {
                        rd.addRaw(AHTML.addRowMultiColumnTable(new String[] {
                           assocArt.getArtifactTypeName(),
                           "Committed",
                           assocArt.getHumanReadableId(),
                           assocArt.getName()}));
                        committedChanges = true;
                     }
                  }
               }
            }
            if (!committedChanges) {
               rd.addRaw(AHTML.addRowSpanMultiColumnTable("No Impacting Actions Found", 3));
            }
            rd.addRaw(AHTML.endMultiColumnTable());
         }
      }
   }
}
