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

package org.eclipse.osee.framework.ui.skynet.widgets.xchange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.xviewer.action.TableCustomizationAction;
import org.eclipse.nebula.widgets.xviewer.action.ViewSelectedCellDataAction;
import org.eclipse.nebula.widgets.xviewer.action.ViewTableReportAction;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.IActionable;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.revision.ChangeManager;
import org.eclipse.osee.framework.ui.plugin.OseeUiActions;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.search.QuickSearchView;
import org.eclipse.osee.framework.ui.skynet.util.SkynetDragAndDrop;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Donald G. Dunne
 * @author Jeff C. Phillips
 */
public class XChangeWidget extends XWidget implements IActionable {

   private ChangeXViewer xChangeViewer;
   private XChangeContentProvider contentProvider;
   public final static String normalColor = "#EEEEEE";
   private static final String LOADING = "Loading ...";
   private static final String NOT_CHANGES = "No changes were found";
   protected Label extraInfoLabel;
   private Branch branch;
   private TransactionRecord transactionId;
   private ToolBar toolBar;

   /**
    * @param label
    */
   public XChangeWidget() {
      super("Change Report");
   }

   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      // Create Text Widgets
      if (isDisplayLabel() && !getLabel().equals("")) {
         labelWidget = new Label(parent, SWT.NONE);
         labelWidget.setText(getLabel() + ":");
         if (getToolTip() != null) {
            labelWidget.setToolTipText(getToolTip());
         }
      }

      Composite mainComp = new Composite(parent, SWT.BORDER);
      mainComp.setLayoutData(new GridData(GridData.FILL_BOTH));
      mainComp.setLayout(ALayout.getZeroMarginLayout());
      if (toolkit != null) {
         toolkit.paintBordersFor(mainComp);
      }

      try {
         createTaskActionBar(mainComp);
      } catch (OseeCoreException ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
      }

      xChangeViewer =
            new ChangeXViewer(mainComp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION, new ChangeXViewerFactory());
      xChangeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
      contentProvider = new XChangeContentProvider();
      xChangeViewer.setContentProvider(contentProvider);
      xChangeViewer.setLabelProvider(new XChangeLabelProvider(xChangeViewer));
      new ActionContributionItem(xChangeViewer.getCustomizeAction()).fill(toolBar, -1);

      if (toolkit != null) {
         toolkit.adapt(xChangeViewer.getStatusLabel(), false, false);
      }
      getXViewer().getMenuManager().add(new Separator());
      getXViewer().getMenuManager().add(new TableCustomizationAction(getXViewer()));
      getXViewer().getMenuManager().add(new ViewTableReportAction(getXViewer()));
      getXViewer().getMenuManager().add(new ViewSelectedCellDataAction(getXViewer()));

      Tree tree = xChangeViewer.getTree();
      GridData gridData = new GridData(GridData.FILL_BOTH);
      gridData.heightHint = 100;
      tree.setLayout(ALayout.getZeroMarginLayout());
      tree.setLayoutData(gridData);
      tree.setHeaderVisible(true);
      tree.setLinesVisible(true);

      new ChangeDragAndDrop(tree, ChangeXViewerFactory.NAMESPACE);
   }

   public void createTaskActionBar(Composite parent) throws OseeCoreException {

      // Button composite for state transitions, etc
      Composite composite = new Composite(parent, SWT.NONE);
      // bComp.setBackground(mainSComp.getDisplay().getSystemColor(SWT.COLOR_CYAN));
      composite.setLayout(new GridLayout(2, false));
      composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      Composite leftComp = new Composite(composite, SWT.NONE);
      leftComp.setLayout(new GridLayout());
      leftComp.setLayoutData(new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL));

      extraInfoLabel = new Label(leftComp, SWT.NONE);
      extraInfoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      extraInfoLabel.setText("\n");

      Composite rightComp = new Composite(composite, SWT.NONE);
      rightComp.setLayout(new GridLayout());
      rightComp.setLayoutData(new GridData(GridData.END));

      toolBar = new ToolBar(rightComp, SWT.FLAT | SWT.RIGHT);
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      toolBar.setLayoutData(gd);
      ToolItem item = null;

      item = new ToolItem(toolBar, SWT.PUSH);
      item.setImage(ImageManager.getImage(PluginUiImage.REFRESH));
      item.setToolTipText("Refresh");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            setInputData(branch, transactionId, true);
         }
      });

      associatedArtifactToolItem = new ToolItem(toolBar, SWT.PUSH);
      associatedArtifactToolItem.setImage(ImageManager.getImage(FrameworkImage.EDIT));
      associatedArtifactToolItem.setToolTipText("Open Associated Artifact");
      associatedArtifactToolItem.setEnabled(false);
      associatedArtifactToolItem.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            try {
               Artifact associatedArtifact = null;
               if (branch != null) {
                  associatedArtifact = (Artifact) branch.getAssociatedArtifact().getFullArtifact();
               } else if (transactionId != null) {
                  associatedArtifact =
                        ArtifactQuery.getArtifactFromId(transactionId.getCommit(), BranchManager.getCommonBranch());
               }
               if (associatedArtifact == null) {
                  AWorkbench.popup("ERROR", "Can not access associated artifact.");
               } else {
                  RendererManager.openInJob(associatedArtifact, PresentationType.GENERALIZED_EDIT);
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
            }
         }
      });

      openQuickSearchActionToolItem = new ToolItem(toolBar, SWT.PUSH);
      openQuickSearchActionToolItem.setImage(ImageManager.getImage(FrameworkImage.ARTIFACT_SEARCH));
      openQuickSearchActionToolItem.setToolTipText("Open Quick Search");
      openQuickSearchActionToolItem.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            try {
               IViewPart viewPart =
                     PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                           QuickSearchView.VIEW_ID);
               if (viewPart != null) {
                  Branch branch =
                        getBranch() != null ? getBranch() : getTransactionId() != null ? getTransactionId().getBranch() : null;
                  if (branch != null) {
                     ((QuickSearchView) viewPart).setBranch(branch);
                  }
               }
            } catch (Exception ex) {
               OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });

      OseeUiActions.addButtonToEditorToolBar(this, SkynetGuiPlugin.getInstance(), toolBar, ChangeView.VIEW_ID,
            "Change Report");
   }
   private ToolItem associatedArtifactToolItem;
   private ToolItem openQuickSearchActionToolItem;

   public void loadTable() {
      refresh();
   }

   @SuppressWarnings("unchecked")
   public ArrayList<Branch> getSelectedBranches() {
      ArrayList<Branch> items = new ArrayList<Branch>();
      if (xChangeViewer == null) {
         return items;
      }
      if (xChangeViewer.getSelection().isEmpty()) {
         return items;
      }
      Iterator i = ((IStructuredSelection) xChangeViewer.getSelection()).iterator();
      while (i.hasNext()) {
         Object obj = i.next();
         items.add((Branch) obj);
      }
      return items;
   }

   @Override
   public Control getControl() {
      return xChangeViewer.getTree();
   }

   @Override
   public void dispose() {
      xChangeViewer.dispose();
   }

   @Override
   public void setFocus() {
      xChangeViewer.getTree().setFocus();
   }

   @Override
   public void refresh() {
      xChangeViewer.refresh();
      validate();
   }

   @Override
   public IStatus isValid() {
      return Status.OK_STATUS;
   }

   @Override
   public String toHTML(String labelFont) {
      return AHTML.simplePage("Unhandled");
   }

   /**
    * @return Returns the xViewer.
    */
   public ChangeXViewer getXViewer() {
      return xChangeViewer;
   }

   @Override
   public Object getData() {
      return xChangeViewer.getInput();
   }

   public void setInputData(final Branch branch, final TransactionRecord transactionId, final boolean loadChangeReport) {
      this.branch = branch;
      this.transactionId = transactionId;

      extraInfoLabel.setText(LOADING);

      ChangeData changeData = new ChangeData();
      final Job uiJob = new UpdateChangeView(branch, transactionId, loadChangeReport, changeData);

      if (loadChangeReport) {
         final IOperation operation = new LoadChangesOperation(branch, transactionId, changeData);
         Operations.executeAsJob(operation, true, Job.LONG, new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
               Jobs.startJob(uiJob);
            }
         });
      } else {
         Jobs.startJob(uiJob);
      }
   }

   @Override
   public String getReportData() {
      return null;
   }

   @Override
   public String getXmlData() {
      return null;
   }

   @Override
   public void setXmlData(String str) {
   }
   public class ChangeDragAndDrop extends SkynetDragAndDrop {

      public ChangeDragAndDrop(Tree tree, String viewId) {
         super(tree, viewId);
      }

      @Override
      public void performDragOver(DropTargetEvent event) {
         event.detail = DND.DROP_NONE;
      }

      @Override
      public Artifact[] getArtifacts() {
         IStructuredSelection selection = (IStructuredSelection) xChangeViewer.getSelection();
         ArrayList<Artifact> artifacts = new ArrayList<Artifact>();

         if (selection != null && !selection.isEmpty()) {
            for (Object object : selection.toArray()) {

               if (object instanceof IAdaptable) {
                  Artifact artifact = (Artifact) ((IAdaptable) object).getAdapter(Artifact.class);

                  if (artifact != null) {
                     artifacts.add(artifact);
                  }
               }
            }
         }
         return artifacts.toArray(new Artifact[artifacts.size()]);
      }
   }

   @Override
   public String getActionDescription() {
      StringBuffer sb = new StringBuffer();
      if (branch != null) {
         sb.append("\nBranch: " + branch);
      }
      if (transactionId != null) {
         sb.append("\nTransaction Id: " + transactionId.getId());
      }
      return sb.toString();
   }

   public TransactionRecord getTransactionId() {
      return transactionId;
   }

   public Branch getBranch() {
      return branch;
   }

   private final static class ChangeData {
      private final Collection<Change> changes;
      private Artifact artifact;

      public ChangeData() {
         changes = new ArrayList<Change>();
      }

      public Collection<Change> getChanges() {
         return changes;
      }

      public void setAssociatedArtifact(Artifact artifact) {
         this.artifact = artifact;
      }

      public Artifact getAssociatedArtifact() {
         return artifact;
      }
   }

   private final static class LoadChangesOperation extends AbstractOperation {
      private final ChangeData changeData;
      private final Branch branch;
      private final TransactionRecord transactionId;

      public LoadChangesOperation(Branch branch, TransactionRecord transactionId, ChangeData changeData) {
         super("Load Change Data", SkynetGuiPlugin.PLUGIN_ID);
         this.changeData = changeData;
         this.branch = branch;
         this.transactionId = transactionId;
      }

      @Override
      protected void doWork(IProgressMonitor monitor) throws Exception {
         boolean hasBranch = branch != null;
         boolean isRebaselined = hasBranch ? branch.getBranchState().equals(BranchState.REBASELINED) : false;
         if (!isRebaselined) {
            Collection<Change> changes = changeData.getChanges();
            changes.clear();
            IOperation subOp;
            if (hasBranch) {
               subOp = ChangeManager.comparedToParent(branch, changes);
            } else {
               subOp = ChangeManager.comparedToPreviousTx(transactionId, changes);
            }
            doSubWork(subOp, monitor, 0.80);
         }
         Artifact artifact = null;
         if (hasBranch) {
            artifact = (Artifact) branch.getAssociatedArtifact().getFullArtifact();
         } else if (transactionId != null && transactionId.getCommit() != 0) {
            artifact = ArtifactQuery.getArtifactFromId(transactionId.getCommit(), BranchManager.getCommonBranch());
         }
         changeData.setAssociatedArtifact(artifact);
         monitor.worked(calculateWork(0.20));
      }
   };

   private final class UpdateChangeView extends UIJob {
      private final ChangeData changeData;
      private final Branch branch;
      private final TransactionRecord transactionId;
      private final boolean loadChangeReport;

      public UpdateChangeView(Branch branch, TransactionRecord transactionId, boolean loadChangeReport, ChangeData changeData) {
         super("Update Change View");
         this.changeData = changeData;
         this.branch = branch;
         this.transactionId = transactionId;
         this.loadChangeReport = loadChangeReport;
      }

      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
         boolean hasBranch = branch != null;
         boolean isRebaselined = hasBranch ? branch.getBranchState().equals(BranchState.REBASELINED) : false;

         if (loadChangeReport && !isRebaselined) {
            if (changeData.getChanges().isEmpty()) {
               extraInfoLabel.setText(NOT_CHANGES);
            } else {
               try {
                  String infoLabel =
                        String.format(
                              "Changes %s to branch: %s\n%s",
                              hasBranch || transactionId.getComment() == null ? "made" : "committed",
                              hasBranch ? branch : "(" + transactionId.getId() + ") " + transactionId.getBranch(),
                              hasBranch || transactionId.getComment() == null ? "" : "Comment: " + transactionId.getComment());
                  extraInfoLabel.setText(infoLabel);
               } catch (OseeCoreException ex) {
                  OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
               }
            }
            xChangeViewer.setInput(changeData.getChanges());

            Artifact associatedArtifact = changeData.getAssociatedArtifact();
            if (associatedArtifact != null && !(associatedArtifact instanceof User)) {
               associatedArtifactToolItem.setImage(ArtifactImageManager.getImage(associatedArtifact));
               associatedArtifactToolItem.setEnabled(true);
            }
         } else {
            if (isRebaselined) {
               extraInfoLabel.setText(branch.getShortName() + "has been updated from parent and cannot be refreshed. Please close down and re-open this change report.");
               xChangeViewer.setEnabled(false);
            } else {
               extraInfoLabel.setText("Cleared on shut down - press refresh to reload");
            }
         }
         return Status.OK_STATUS;
      }

   }
}