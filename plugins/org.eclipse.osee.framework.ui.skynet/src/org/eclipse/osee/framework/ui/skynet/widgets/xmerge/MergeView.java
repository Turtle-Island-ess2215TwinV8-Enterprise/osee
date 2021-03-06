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

package org.eclipse.osee.framework.ui.skynet.widgets.xmerge;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.help.ui.OseeHelpContext;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.conflict.Conflict;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IArtifactEventListener;
import org.eclipse.osee.framework.skynet.core.event.listener.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.event.model.EventModType;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.HelpUtil;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.util.SkynetViews;
import org.eclipse.osee.framework.ui.skynet.widgets.GenericViewPart;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * @author Donald G. Dunne
 */
public class MergeView extends GenericViewPart implements IBranchEventListener, IArtifactEventListener {

   public static final String VIEW_ID = "org.eclipse.osee.framework.ui.skynet.widgets.xmerge.MergeView";

   private MergeXWidget mergeXWidget;
   private Branch sourceBranch;
   private Branch destBranch;
   private TransactionRecord transactionId;
   private TransactionRecord commitTrans;
   private boolean showConflicts;

   public static void openView(final Branch sourceBranch, final Branch destBranch, final TransactionRecord tranId) {
      if (Conditions.allNull(sourceBranch, destBranch, tranId)) {
         throw new IllegalArgumentException("Branch's and Transaction ID can't be null");
      }
      openViewUpon(sourceBranch, destBranch, tranId, null, true);
   }

   public static void openView(final TransactionRecord commitTrans) throws OseeCoreException {
      Conditions.checkNotNull(commitTrans, "Commit Transaction ID");
      openViewUpon(null, null, null, commitTrans, true);
   }

   private static void openViewUpon(final Branch sourceBranch, final Branch destBranch, final TransactionRecord tranId, final TransactionRecord commitTrans, final boolean showConflicts) {
      Job job = new Job("Open Merge View") {

         @Override
         protected IStatus run(final IProgressMonitor monitor) {
            Displays.ensureInDisplayThread(new Runnable() {
               @Override
               public void run() {
                  try {
                     IWorkbenchPage page = AWorkbench.getActivePage();
                     IViewPart viewPart =
                        page.showView(
                           MergeView.VIEW_ID,
                           String.valueOf(sourceBranch != null ? sourceBranch.getId() * 100000 + destBranch.getId() : commitTrans.getId()),
                           IWorkbenchPage.VIEW_ACTIVATE);
                     if (viewPart instanceof MergeView) {
                        MergeView mergeView = (MergeView) viewPart;
                        mergeView.showConflicts = showConflicts;
                        mergeView.explore(sourceBranch, destBranch, tranId, commitTrans, showConflicts);
                     }
                  } catch (Exception ex) {
                     OseeLog.log(Activator.class, Level.SEVERE, ex);
                  }
               }
            });

            monitor.done();
            return Status.OK_STATUS;
         }
      };

      Jobs.startJob(job);
   }

   private Conflict[] getConflicts() {
      return mergeXWidget != null ? mergeXWidget.getConflicts() : MergeXViewer.EMPTY_CONFLICTS;
   }

   @Override
   public void dispose() {
      super.dispose();
      OseeEventManager.removeListener(this);
      OseeEventManager.removeListener(this);
   }

   @Override
   public void createPartControl(Composite parent) {
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      layout.verticalSpacing = 0;
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      parent.setLayout(layout);
      parent.setLayoutData(new GridData(GridData.FILL_BOTH));
      mergeXWidget = new MergeXWidget();
      mergeXWidget.setDisplayLabel(false);
      mergeXWidget.createWidgets(parent, 1);

      OseeStatusContributionItemFactory.addTo(this, true);
      //      getSite().registerContextMenu("org.eclipse.osee.framework.ui.skynet.widgets.xmerge.MergeView", menuManager,
      //         mergeXWidget.getXViewer());

      getSite().setSelectionProvider(mergeXWidget.getXViewer());
      HelpUtil.setHelp(parent, OseeHelpContext.MERGE_MANAGER);

      OseeEventManager.addListener(this);

      setFocusWidget(mergeXWidget.getControl());
   }

   public void explore(final Branch sourceBranch, final Branch destBranch, final TransactionRecord transactionId, final TransactionRecord commitTrans, boolean showConflicts) {
      this.sourceBranch = sourceBranch;
      this.destBranch = destBranch;
      this.transactionId = transactionId;
      this.commitTrans = commitTrans;
      try {
         mergeXWidget.setInputData(sourceBranch, destBranch, transactionId, this, commitTrans, showConflicts);
         if (sourceBranch != null) {
            setPartName("Merge Manager: " + sourceBranch.getShortName() + " <=> " + destBranch.getShortName());
         } else if (commitTrans != null) {
            setPartName("Merge Manager: " + commitTrans.getId());
         } else {
            setPartName("Merge Manager");
         }

      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public void init(IViewSite site, IMemento memento) throws PartInitException {
      super.init(site, memento);
      try {
         Integer sourceBranchId = null;
         Integer destBranchId = null;

         if (memento != null) {
            memento = memento.getChild(INPUT);
            if (memento != null) {
               if (SkynetViews.isSourceValid(memento)) {

                  Integer commitTransaction = memento.getInteger(COMMIT_NUMBER);
                  if (commitTransaction != null) {
                     openViewUpon(null, null, null, TransactionManager.getTransactionId(commitTransaction), false);
                     return;
                  }
                  sourceBranchId = memento.getInteger(SOURCE_BRANCH_ID);
                  final Branch sourceBranch = BranchManager.getBranch(sourceBranchId);
                  if (sourceBranch == null) {
                     OseeLog.log(Activator.class, Level.WARNING,
                        "Merge View can't init due to invalid source branch id " + sourceBranchId);
                     mergeXWidget.setLabel("Could not restore this Merge View");
                     return;
                  }
                  destBranchId = memento.getInteger(DEST_BRANCH_ID);
                  final Branch destBranch = BranchManager.getBranch(destBranchId);
                  if (destBranch == null) {
                     OseeLog.log(Activator.class, Level.WARNING,
                        "Merge View can't init due to invalid destination branch id " + sourceBranchId);
                     mergeXWidget.setLabel("Could not restore this Merge View");
                     return;
                  }
                  try {
                     TransactionRecord transactionId =
                        TransactionManager.getTransactionId(memento.getInteger(TRANSACTION_NUMBER));
                     openViewUpon(sourceBranch, destBranch, transactionId, null, false);
                  } catch (OseeCoreException ex) {
                     OseeLog.log(Activator.class, Level.WARNING,
                        "Merge View can't init due to invalid transaction id " + transactionId);
                     mergeXWidget.setLabel("Could not restore this Merge View due to invalid transaction id " + transactionId);
                     return;
                  }
               } else {
                  SkynetViews.closeView(VIEW_ID, getViewSite().getSecondaryId());
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.WARNING, "Merge View error on init", ex);
      }
   }

   private static final String INPUT = "input";
   private static final String SOURCE_BRANCH_ID = "sourceBranchId";
   private static final String DEST_BRANCH_ID = "destBranchId";
   private static final String TRANSACTION_NUMBER = "transactionNumber";
   private static final String COMMIT_NUMBER = "commitTransactionNumber";

   @Override
   public void saveState(IMemento memento) {
      super.saveState(memento);
      memento = memento.createChild(INPUT);
      if (sourceBranch != null) {
         memento.putInteger(SOURCE_BRANCH_ID, sourceBranch.getId());
         memento.putInteger(DEST_BRANCH_ID, destBranch.getId());
         memento.putInteger(TRANSACTION_NUMBER, transactionId.getId());
      } else if (commitTrans != null) {
         memento.putInteger(COMMIT_NUMBER, commitTrans.getId());
      }

      if (sourceBranch != null || commitTrans != null) {
         SkynetViews.addDatabaseSourceId(memento);
      }
   }

   protected void showConflicts(boolean show) {
      showConflicts = show;
   }

   private boolean isApplicableEvent(String branchGuid, Branch mergeBranch) {
      return Conditions.in(branchGuid, mergeBranch.getGuid()) || isApplicableSourceOrDestEvent(branchGuid);
   }

   private boolean isApplicableSourceOrDestEvent(String branchGuid) {
      return Conditions.notNull(sourceBranch, destBranch) && Conditions.in(branchGuid, sourceBranch.getGuid(),
         destBranch.getGuid());
   }

   @Override
   public void handleBranchEvent(final Sender sender, final BranchEvent branchEvent) {
      if (!isApplicableSourceOrDestEvent(branchEvent.getBranchGuid())) {
         return;
      }

      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            switch (branchEvent.getEventType()) {
               case Deleting:
               case Purging:
               case Committing:
               case Deleted:
               case Purged:
               case Committed:
                  getSite().getPage().hideView(MergeView.this);
                  break;
               default:
                  if (mergeXWidget != null && Widgets.isAccessible(mergeXWidget.getXViewer().getTree())) {
                     mergeXWidget.refresh();
                  }
                  break;
            }
         }
      });
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      return null;
   }

   private boolean isDisposed() {
      return Conditions.anyNull(mergeXWidget.getXViewer(), mergeXWidget.getXViewer().getTree()) || mergeXWidget.getXViewer().getTree().isDisposed();
   }

   private boolean conflictInvovlesArtifact(Artifact artifact, Conflict conflict) {
      if (artifact.getArtId() == conflict.getArtId()) {
         IOseeBranch branch = artifact.getBranch();
         return branch.equals(conflict.getSourceBranch()) || branch.equals(conflict.getDestBranch());
      }
      return false;
   }

   @Override
   public void handleArtifactEvent(ArtifactEvent artifactEvent, final Sender sender) {
      if (isDisposed()) {
         OseeEventManager.removeListener(this);
         return;
      }
      Branch mergeBranch = null;
      try {
         mergeBranch = BranchManager.getMergeBranch(sourceBranch, destBranch);
         if (mergeBranch == null || !mergeBranch.getGuid().equals(artifactEvent.getBranchGuid())) {
            return;
         }
         if (!isApplicableEvent(artifactEvent.getBranchGuid(), mergeBranch)) {
            return;
         }
      } catch (OseeCoreException ex1) {
         // Do nothing here
      }
      final Collection<Artifact> modifiedArts =
         artifactEvent.getCacheArtifacts(EventModType.Modified, EventModType.Reloaded);
      final Collection<EventBasicGuidArtifact> deletedPurgedArts = artifactEvent.get(EventModType.Deleted);
      if (modifiedArts.isEmpty() && deletedPurgedArts.isEmpty()) {
         return;
      }
      final MergeView mergeView = this;
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (isDisposed()) {
               return;
            }
            for (Artifact artifact : modifiedArts) {
               try {
                  IOseeBranch branch = artifact.getBranch();
                  if (showConflicts) {
                     Conflict[] conflicts = getConflicts();
                     for (Conflict conflict : conflicts) {
                        if (conflictInvovlesArtifact(artifact, conflict)) {
                           mergeXWidget.setInputData(sourceBranch, destBranch, transactionId, mergeView, commitTrans,
                              "Source Artifact Changed", showConflicts);
                           if (artifact.equals(conflict.getSourceArtifact()) && sender.isLocal()) {
                              new MessageDialog(
                                 Displays.getActiveShell().getShell(),
                                 "Modifying Source artifact while merging",
                                 null,
                                 "Typically changes done while merging should be done on the merge branch.  You should not normally merge on the source branch.",
                                 2, new String[] {"OK"}, 1).open();
                           }
                           return;
                        } else if (artifact.equals(conflict.getArtifact())) {
                           conflict.computeEqualsValues();
                           mergeXWidget.refresh();
                        }
                     }
                     if (conflicts.length > 0 && (branch.equals(conflicts[0].getSourceBranch()) || branch.equals(conflicts[0].getDestBranch()))) {
                        mergeXWidget.setInputData(
                           sourceBranch,
                           destBranch,
                           transactionId,
                           mergeView,
                           commitTrans,
                           branch.equals(conflicts[0].getSourceBranch()) ? "Source Branch Changed" : "Destination Branch Changed",
                           showConflicts);
                     }
                  }
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, ex);
               }
            }
            if (!deletedPurgedArts.isEmpty()) {
               try {
                  Branch branch = BranchManager.getBranch(deletedPurgedArts.iterator().next());
                  Conflict[] conflicts = getConflicts();
                  if (conflicts.length > 0 && (branch.equals(conflicts[0].getSourceBranch()) || branch.equals(conflicts[0].getDestBranch()))) {
                     mergeXWidget.setInputData(
                        sourceBranch,
                        destBranch,
                        transactionId,
                        mergeView,
                        commitTrans,
                        branch.equals(conflicts[0].getSourceBranch()) ? "Source Branch Changed" : "Destination Branch Changed",
                        showConflicts);
                  }
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, ex);
               }
            }
         }

      });

   }

}
