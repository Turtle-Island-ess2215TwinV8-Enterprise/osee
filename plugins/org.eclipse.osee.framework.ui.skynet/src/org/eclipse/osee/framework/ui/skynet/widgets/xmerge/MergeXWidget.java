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

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.core.enums.ConflictStatus;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.conflict.Conflict;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.framework.skynet.core.revision.ConflictManagerInternal;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.ListSelectionDialogNoSave;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.change.ChangeUiUtil;
import org.eclipse.osee.framework.ui.skynet.cm.IOseeCmService;
import org.eclipse.osee.framework.ui.skynet.cm.OseeCmEditor;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.internal.ServiceUtil;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.widgets.GenericXWidget;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

/**
 * @author Donald G. Dunne
 * @author Theron Virgin
 */
public class MergeXWidget extends GenericXWidget {
   private static final String COMPLETE_COMMIT_ACTION_ID = "complete.commit.action.id";
   private static final String REFRESH_ACTION_ID = "refresh.action.id";
   private MergeXViewer mergeXViewer;
   private IDirtiableEditor editor;
   public final static String normalColor = "#EEEEEE";
   private static final String LOADING = "Loading ...";
   private static final String NO_CONFLICTS = "No conflicts were found";
   private static final String CONFLICTS_NOT_LOADED = "Cleared on shutdown.  Refresh to Reload.";
   private Label extraInfoLabel;
   private String displayLabelText;
   private Action openAssociatedArtifactAction;
   private Action completeCommitAction;
   private Branch sourceBranch;
   private Branch destBranch;
   private TransactionRecord commitTrans;
   private TransactionRecord tranId;
   private MergeView mergeView;
   private IToolBarManager toolBarManager;
   private final static String CONFLICTS_RESOLVED = "\nAll Conflicts Are Resolved";

   public MergeXWidget() {
      super("Merge Manager");
   }

   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      Composite mainComp = new Composite(parent, SWT.BORDER);
      Composite taskComp = new Composite(mainComp, SWT.NONE);
      taskComp.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
      taskComp.setLayout(ALayout.getZeroMarginLayout());
      createTextWidgets(parent);
      createMainComposite(mainComp);
      mergeXViewer = new MergeXViewer(mainComp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION, this);
      createMergeXViewer();
      createTaskActionBar(taskComp);
      if (toolkit != null) {
         toolkit.adapt(mergeXViewer.getStatusLabel(), false, false);
      }
      Tree tree = mergeXViewer.getTree();
      createTree(tree);
   }

   private void createTextWidgets(Composite parent) {
      if (isDisplayLabel() && !getLabel().equals("")) {
         labelWidget = new Label(parent, SWT.NONE);
         labelWidget.setText(getLabel() + ":");
         if (getToolTip() != null) {
            labelWidget.setToolTipText(getToolTip());
         }
      }
   }

   private void createMainComposite(Composite mainComp) {
      mainComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      mainComp.setLayout(ALayout.getZeroMarginLayout());
      if (toolkit != null) {
         toolkit.paintBordersFor(mainComp);
      }

   }

   private void createMergeXViewer() {
      mergeXViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
      XMergeLabelProvider labelProvider = new XMergeLabelProvider(mergeXViewer);
      mergeXViewer.addLabelProvider(labelProvider);
      mergeXViewer.setSorter(new MergeXViewerSorter(mergeXViewer, labelProvider));
      mergeXViewer.setContentProvider(new XMergeContentProvider());
      mergeXViewer.setLabelProvider(new XMergeLabelProvider(mergeXViewer));
   }

   private void createTree(Tree tree) {
      GridData gridData = new GridData(GridData.FILL_BOTH);
      gridData.heightHint = 100;
      tree.setLayout(ALayout.getZeroMarginLayout());
      tree.setLayoutData(gridData);
      tree.setHeaderVisible(true);
      tree.setLinesVisible(true);
   }

   public void createTaskActionBar(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayout layout = ALayout.getZeroMarginLayout(2, false);
      layout.marginLeft = 5;
      composite.setLayout(layout);
      composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      extraInfoLabel = new Label(composite, SWT.NONE);
      extraInfoLabel.setAlignment(SWT.LEFT);
      extraInfoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      extraInfoLabel.setText("\n");

      IToolBarManager manager = getToolBarManager();
      ((ToolBarManager) manager).createControl(composite);
      manager.add(new RefreshAction());
      manager.add(new Separator());
      openAssociatedArtifactAction = new OpenAssociatedArtifactAction();
      manager.add(openAssociatedArtifactAction);
      manager.add(new Separator());
      manager.add(new ApplyPriorMergeResultsAction());
      manager.add(new Separator());
      manager.add(new ShowSourceBranchChangeReportAction());
      manager.add(new ShowDestinationBranchChangeReportAction());
      manager.add(new Separator());
      manager.add(mergeXViewer.getCustomizeAction());
      manager.update(true);
   }

   private IToolBarManager getToolBarManager() {
      if (toolBarManager == null) {
         toolBarManager = new ToolBarManager(SWT.FLAT);
      }
      return toolBarManager;
   }

   private void applyPreviousMerge(final int destBranchId) {
      Job job = new Job("Apply Previous Merge") {

         @Override
         protected IStatus run(final IProgressMonitor monitor) {
            Conflict[] conflicts = getConflicts();

            monitor.beginTask("ApplyingPreviousMerge", conflicts.length);
            for (Conflict conflict : conflicts) {
               try {
                  Branch destinationBranch = BranchManager.getBranch(destBranchId);
                  Branch mergeBranch = BranchManager.getMergeBranch(conflict.getSourceBranch(), destinationBranch);
                  conflict.applyPreviousMerge(mergeBranch.getId(), destBranchId);
               } catch (OseeCoreException ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, ex);
               } finally {
                  monitor.worked(1);
               }
            }
            monitor.done();
            return Status.OK_STATUS;
         }
      };

      Jobs.startJob(job, new JobChangeAdapter() {
         @Override
         public void done(IJobChangeEvent event) {
            loadTable();
         }
      });
   }

   public void refreshTable() {
      Job job = new Job("Loading Merge Manager") {
         @Override
         protected IStatus run(IProgressMonitor monitor) {
            try {
               Conflict[] conflicts = getConflicts();
               if (conflicts.length >= 0) {
                  Conflict[] artifactChanges = new Conflict[0];
                  if (conflicts[0].getToTransactionId() != null) {
                     setConflicts(ConflictManagerInternal.getConflictsPerBranch(conflicts[0].getSourceBranch(),
                        conflicts[0].getDestBranch(), conflicts[0].getToTransactionId(), monitor).toArray(
                        artifactChanges));
                  } else {
                     setConflicts(ConflictManagerInternal.getConflictsPerBranch(conflicts[0].getCommitTransactionId(),
                        monitor).toArray(artifactChanges));
                  }
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }

            return Status.OK_STATUS;
         }
      };
      Jobs.startJob(job, new JobChangeAdapter() {
         @Override
         public void done(IJobChangeEvent event) {
            loadTable();
         }
      });
   }

   public void loadTable() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            refresh();
         }
      });
   }

   @Override
   public Control getControl() {
      return mergeXViewer.getTree();
   }

   @Override
   public void dispose() {
      mergeXViewer.dispose();
   }

   public Conflict[] getConflicts() {
      return mergeXViewer != null ? mergeXViewer.getConflicts() : MergeXViewer.EMPTY_CONFLICTS;
   }

   @Override
   public void refresh() {
      mergeXViewer.refresh();
      validate();
      mergeView.showConflicts(true);
      int resolved = 0;
      int informational = 0;
      Conflict[] conflicts = getConflicts();
      if (conflicts.length > 0) {
         for (Conflict conflict : conflicts) {
            ConflictStatus status = conflict.getStatus();
            if (status.isResolved() || status.isCommitted()) {
               resolved++;
            }
            if (status.isInformational()) {
               informational++;
            }
         }
         if (resolved == conflicts.length) {
            extraInfoLabel.setText(displayLabelText + CONFLICTS_RESOLVED);
         } else {
            extraInfoLabel.setText(displayLabelText + "\nConflicts : " + (conflicts.length - informational) + " <=> Resolved : " + resolved + (informational == 0 ? " " : "\nInformational Conflicts : " + informational));
         }
      }
      checkForCompleteCommit();
   }

   private boolean areAllConflictsResolved() {
      int resolved = 0;
      Conflict[] conflicts = getConflicts();
      for (Conflict conflict : conflicts) {
         ConflictStatus status = conflict.getStatus();
         if (status.isConsideredResolved()) {
            resolved++;
         }
      }
      return resolved == conflicts.length;
   }

   @Override
   public IStatus isValid() {
      return Status.OK_STATUS;
   }

   @Override
   public boolean isEmpty() {
      return mergeXViewer.getTree().getItemCount() == 0;
   }

   /**
    * @return Returns the xViewer.
    */
   public MergeXViewer getXViewer() {
      return mergeXViewer;
   }

   @Override
   public Object getData() {
      return getConflicts();
   }

   public IDirtiableEditor getEditor() {
      return editor;
   }

   public void setEditor(IDirtiableEditor editor) {
      this.editor = editor;
   }

   public void setInputData(final Branch sourceBranch, final Branch destBranch, final TransactionRecord tranId, final MergeView mergeView, final TransactionRecord commitTrans, boolean showConflicts) {
      setInputData(sourceBranch, destBranch, tranId, mergeView, commitTrans, "", showConflicts);
   }

   public void setInputData(final Branch sourceBranch, final Branch destBranch, final TransactionRecord tranId, final MergeView mergeView, final TransactionRecord commitTrans, String loadingText, final boolean showConflicts) {
      this.sourceBranch = sourceBranch;
      this.destBranch = destBranch;
      this.tranId = tranId;
      this.mergeView = mergeView;
      this.commitTrans = commitTrans;
      extraInfoLabel.setText(LOADING + loadingText);
      Job job = new Job("Loading Merge Manager") {
         @Override
         protected IStatus run(IProgressMonitor monitor) {
            try {
               if (showConflicts) {
                  Conflict[] conflicts;
                  if (commitTrans == null) {
                     conflicts =
                        ConflictManagerInternal.getConflictsPerBranch(sourceBranch, destBranch, tranId, monitor).toArray(
                           new Conflict[0]);
                  } else {
                     conflicts =
                        ConflictManagerInternal.getConflictsPerBranch(commitTrans, monitor).toArray(new Conflict[0]);
                  }
                  mergeXViewer.setConflicts(conflicts);
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
            return Status.OK_STATUS;
         }
      };
      Jobs.startJob(job, new JobChangeAdapter() {

         @Override
         public void done(IJobChangeEvent event) {
            Displays.ensureInDisplayThread(new Runnable() {
               @Override
               public void run() {
                  if (Widgets.isAccessible(extraInfoLabel)) {
                     if (showConflicts) {
                        Conflict[] conflicts = getConflicts();
                        if (conflicts.length == 0) {
                           extraInfoLabel.setText(NO_CONFLICTS);
                        } else {
                           setConflicts(conflicts);
                           refresh();
                        }
                     } else {
                        extraInfoLabel.setText(CONFLICTS_NOT_LOADED);
                     }
                  }
                  checkForCompleteCommit();
               }
            });
         }

      });
      if (sourceBranch != null) {
         refreshAssociatedArtifactItem(sourceBranch);
      }
   }

   private void refreshAssociatedArtifactItem(Branch sourceBranch) {
      try {
         IArtifact branchAssociatedArtifact = BranchManager.getAssociatedArtifact(sourceBranch);
         if (branchAssociatedArtifact != null) {
            openAssociatedArtifactAction.setImageDescriptor(ArtifactImageManager.getImageDescriptor(branchAssociatedArtifact));
            openAssociatedArtifactAction.setEnabled(true);
         }
      } catch (ArtifactDoesNotExist ex) {
         PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(mergeView);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   public void setConflicts(Conflict[] conflicts) throws IllegalStateException {
      mergeXViewer.setConflicts(conflicts);
      loadTable();
      int resolved = 0;
      int informational = 0;
      for (Conflict conflict : getConflicts()) {
         ConflictStatus status = conflict.getStatus();
         if (status.isResolved() || status.isCommitted()) {
            resolved++;
         }
         if (status.isInformational()) {
            informational++;
         }
      }
      Conflict[] storedConflicts = getConflicts();
      if (storedConflicts.length > 0) {
         StringBuilder builder = new StringBuilder();
         if (sourceBranch != null) {
            builder.append("Source Branch :  ");
            builder.append(sourceBranch.getName());
            builder.append("\nDestination Branch :  ");
            builder.append(destBranch.getName());
         } else {
            builder.append("Commit Transaction ID :  ");
            builder.append(commitTrans);
            builder.append(" ");
            builder.append(commitTrans.getComment());
         }
         displayLabelText = builder.toString();

         if (resolved == storedConflicts.length - informational) {
            extraInfoLabel.setText(displayLabelText + CONFLICTS_RESOLVED);
         } else {
            String message =
               String.format("%s\nConflicts : %s <=> Resolved : %s%s", displayLabelText,
                  (storedConflicts.length - informational), resolved,
                  (informational == 0 ? " " : "\nInformational Conflicts : " + informational));
            extraInfoLabel.setText(message);
         }
      }

   }

   private Action getCompleteCommitAction() {
      if (completeCommitAction == null) {
         completeCommitAction = new CompleteCommitAction();
      }
      return completeCommitAction;
   }

   private Branch getMergeBranch() {
      Conflict[] conflicts = getConflicts();
      return conflicts.length > 0 ? conflicts[0].getMergeBranch() : null;
   }

   private boolean hasMergeBranchBeenCommitted() {
      final Branch mergeBranch = getMergeBranch();
      return mergeBranch != null && !mergeBranch.isEditable();
   }

   private void checkForCompleteCommit() {
      boolean isVisible = !hasMergeBranchBeenCommitted() && areAllConflictsResolved();
      if (sourceBranch != null) {
         try {
            isVisible &=
               sourceBranch.getBranchState().isRebaselineInProgress() && sourceBranch.getParentBranch().equals(
                  destBranch.getParentBranch());
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            isVisible = false;
         }
      }
      setCompleteCommitItemVisible(isVisible);
   }

   private void setCompleteCommitItemVisible(boolean isVisible) {
      IToolBarManager manager = getToolBarManager();
      boolean wasFound = manager.find(COMPLETE_COMMIT_ACTION_ID) != null;
      if (isVisible) {
         if (!wasFound) {
            manager.insertBefore(REFRESH_ACTION_ID, getCompleteCommitAction());
         }
      } else if (wasFound) {
         manager.remove(COMPLETE_COMMIT_ACTION_ID);
      }
      manager.update(true);
   }

   private final class CompleteCommitAction extends Action {
      public CompleteCommitAction() {
         super();
         setImageDescriptor(FrameworkImage.BRANCH_COMMIT.createImageDescriptor());
         setToolTipText("Commit changes into destination branch");
         setId(COMPLETE_COMMIT_ACTION_ID);
      }

      @Override
      public void run() {
         if (sourceBranch.getBranchState().isRebaselineInProgress()) {
            try {
               ConflictManagerExternal conflictManager = new ConflictManagerExternal(destBranch, sourceBranch);
               BranchManager.completeUpdateBranch(conflictManager, true, false);
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      }
   }

   private final class OpenAssociatedArtifactAction extends Action {

      public OpenAssociatedArtifactAction() {
         super();
         setToolTipText("Open Associated Artifact");
         setEnabled(false);
      }

      @Override
      public void run() {
         Conflict[] storedConflicts = getConflicts();
         try {
            Branch sourceBranch = storedConflicts[0].getSourceBranch();
            Artifact branchAssociatedArtifact = BranchManager.getAssociatedArtifact(sourceBranch);
            IOseeCmService cmService = ServiceUtil.getOseeCmService();
            if (cmService.isPcrArtifact(branchAssociatedArtifact)) {
               cmService.openArtifact(branchAssociatedArtifact, OseeCmEditor.CmPcrEditor);
            } else if (!branchAssociatedArtifact.equals(UserManager.getUser(SystemUser.OseeSystem))) {
               RendererManager.open(branchAssociatedArtifact, PresentationType.SPECIALIZED_EDIT);
            } else {
               AWorkbench.popup("ERROR", "Unknown branch association");
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }
   }

   private final class ShowSourceBranchChangeReportAction extends Action {

      public ShowSourceBranchChangeReportAction() {
         super();
         //         setImageDescriptor(SkynetGuiPlugin.getInstance().getImageDescriptor("branch_change_source.gif"));
         setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.BRANCH_CHANGE_SOURCE));
         setToolTipText("Show Source Branch Change Report");
      }

      @Override
      public void run() {
         Conflict[] conflicts = getConflicts();
         if (conflicts.length > 0) {
            Conflict firstConflict = conflicts[0];
            Branch sourceBranch = firstConflict.getSourceBranch();
            if (sourceBranch != null) {
               try {
                  ChangeUiUtil.open(sourceBranch);
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            } else {
               try {
                  ChangeUiUtil.open(firstConflict.getCommitTransactionId());
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         }
      }
   }

   private final class ShowDestinationBranchChangeReportAction extends Action {

      public ShowDestinationBranchChangeReportAction() {
         super();
         setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.BRANCH_CHANGE_DEST));
         setToolTipText("Show Destination Branch Change Report");
      }

      @Override
      public void run() {
         Conflict[] conflicts = getConflicts();
         if (conflicts.length > 0) {
            try {
               ChangeUiUtil.open(conflicts[0].getDestBranch());
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      }
   }

   private final class RefreshAction extends Action {

      public RefreshAction() {
         super();
         setImageDescriptor(ImageManager.getImageDescriptor(PluginUiImage.REFRESH));
         setToolTipText("Refresh");
         setId(REFRESH_ACTION_ID);
      }

      @Override
      public void run() {
         setInputData(sourceBranch, destBranch, tranId, mergeView, commitTrans, true);
      }
   }

   private final class ApplyPriorMergeResultsAction extends Action {
      public ApplyPriorMergeResultsAction() {
         super();
         setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.OUTGOING_MERGED));
         setToolTipText("Apply Merge Results From Prior Merge");
      }

      @Override
      public void run() {
         Conflict[] conflicts = getConflicts();
         if (conflicts.length != 0) {
            if (conflicts[0].getSourceBranch() != null) {
               ArrayList<String> selections = new ArrayList<String>();
               ArrayList<Integer> branchIds = new ArrayList<Integer>();
               try {
                  Collection<Integer> destBranches =
                     ConflictManagerInternal.getDestinationBranchesMerged(sourceBranch.getId());
                  for (Integer integer : destBranches) {
                     if (integer.intValue() != destBranch.getId()) {
                        selections.add(BranchManager.getBranch(integer).getName());
                        branchIds.add(integer);
                     }
                  }
                  if (selections.size() > 0) {
                     ListSelectionDialogNoSave dialog =
                        new ListSelectionDialogNoSave(selections.toArray(), Displays.getActiveShell().getShell(),
                           "Apply Prior Merge Resolution", null,
                           "Select the destination branch that the previous commit was appplied to", 2, new String[] {
                              "Apply",
                              "Cancel"}, 1);
                     if (dialog.open() == 0) {
                        System.out.print("Applying the merge found for Branch " + branchIds.toArray()[dialog.getSelection()]);
                        applyPreviousMerge(branchIds.get(dialog.getSelection()));
                     }
                  }
                  if (selections.isEmpty()) {
                     new MessageDialog(Displays.getActiveShell().getShell(), "Apply Prior Merge Resolution", null,
                        "This Source Branch has had No Prior Merges", 2, new String[] {"OK"}, 1).open();
                  }
               } catch (OseeCoreException ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         }
      }
   }
}
