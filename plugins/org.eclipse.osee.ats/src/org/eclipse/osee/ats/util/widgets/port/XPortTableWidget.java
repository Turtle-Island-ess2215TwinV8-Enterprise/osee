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

package org.eclipse.osee.ats.util.widgets.port;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.skynet.widgets.GenericXWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.IArtifactWidget;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.FontManager;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

/**
 * @author Donald G. Dunne
 */
public class XPortTableWidget extends GenericXWidget implements IArtifactWidget, IBranchEventListener {

   private PortXViewer portXViewer;
   private IDirtiableEditor editor;
   public final static String normalColor = "#EEEEEE";
   private TeamWorkFlowArtifact teamArt;
   private static final int paddedTableHeightHint = 2;
   private Label extraInfoLabel;
   public static final String WIDGET_NAME = "XPortTableWidget";
   public static final String NAME = "Port Manager";
   public static final String DESCRIPTION = "Drag in Team Workflows to port to this Baseline Branch.";
   private int lastDefectListSize = 0;
   private Composite mainComp;
   private Composite parentComp;

   public XPortTableWidget() {
      super(NAME);
      OseeEventManager.addListener(this);
   }

   @Override
   public TeamWorkFlowArtifact getArtifact() {
      return teamArt;
   }

   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      // parentComp needs to be created and remain intact; mainComp will be disposed and re-created as necessary
      parentComp = new Composite(parent, SWT.FLAT);
      parentComp.setLayoutData(new GridData(GridData.FILL_BOTH));
      parentComp.setLayout(ALayout.getZeroMarginLayout());

      redrawComposite();
   }

   private void redrawComposite() {
      if (parentComp == null || !Widgets.isAccessible(parentComp)) {
         return;
      }
      if (mainComp != null && Widgets.isAccessible(mainComp)) {
         mainComp.dispose();
         portXViewer = null;
      }
      mainComp = new Composite(parentComp, SWT.FLAT);
      mainComp.setLayoutData(new GridData(GridData.FILL_BOTH));
      mainComp.setLayout(new GridLayout(1, true));
      if (toolkit != null) {
         toolkit.paintBordersFor(mainComp);
      }

      labelWidget = new Label(mainComp, SWT.NONE);
      labelWidget.setText(getLabel() + ":");
      if (getToolTip() != null) {
         labelWidget.setToolTipText(getToolTip());
      }

      //      try {
      if (teamArt.isWorkingBranchCreationInProgress()) {
         labelWidget.setText(getLabel() + ": Branch Creation in Progress");
      } else {

         Composite tableComp = new Composite(mainComp, SWT.BORDER);
         tableComp.setLayoutData(new GridData(GridData.FILL_BOTH));
         tableComp.setLayout(ALayout.getZeroMarginLayout());
         if (toolkit != null) {
            toolkit.paintBordersFor(tableComp);
         }

         createTaskActionBar(tableComp);

         labelWidget.setText(getLabel() + ": ");

         portXViewer = new PortXViewer(tableComp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION, this);
         portXViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

         portXViewer.setContentProvider(new XPortContentProvider());
         portXViewer.setLabelProvider(new XPortLabelProvider(portXViewer));
         portXViewer.setSorter(new XPortSorter(this));

         createButtons(tableComp);

         if (toolkit != null && portXViewer.getStatusLabel() != null) {
            toolkit.adapt(portXViewer.getStatusLabel(), false, false);
         }
         new PortDragAndDrop(this);
         setXviewerTree();
         loadTable();
      }
      //      } catch (OseeCoreException ex) {
      //         OseeLog.log(Activator.class, Level.SEVERE, ex);
      //      }
      // reset bold for label
      SMAEditor.setLabelFonts(labelWidget, FontManager.getDefaultLabelFont());

      parentComp.layout();
   }

   private void createButtons(Composite tableComp) {
      Composite buttonBar = new Composite(mainComp, SWT.NONE);
      buttonBar.setLayoutData(new GridData(SWT.None, SWT.None, false, false));
      buttonBar.setLayout(new GridLayout(3, false));
      toolkit.adapt(buttonBar);

      Button button = toolkit.createButton(buttonBar, "Apply All", SWT.PUSH);
      button.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            PortApplyAllOperation operation = new PortApplyAllOperation(portXViewer, teamArt);
            try {
               operation.doWork(null);
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });

      button = toolkit.createButton(buttonBar, "Revert All", SWT.PUSH);
   }

   public void setXviewerTree() {
      Tree tree = portXViewer.getTree();
      tree.setLayout(ALayout.getZeroMarginLayout());
      tree.setLayoutData(new GridData(GridData.FILL_BOTH));
      tree.setHeaderVisible(true);
      tree.setLinesVisible(true);
   }

   private void resizeTable() {
      Tree tree = portXViewer.getTree();
      int defectListSize = portXViewer.getTree().getItemCount();
      if (defectListSize == lastDefectListSize) {
         return;
      }
      lastDefectListSize = defectListSize;
      int treeItemHeight = portXViewer.getTree().getItemHeight();
      GridData gridData = new GridData(GridData.FILL_BOTH);
      gridData.heightHint = treeItemHeight * (paddedTableHeightHint + defectListSize);
      tree.setLayoutData(gridData);
      portXViewer.getXPortTableWidget().getManagedForm().reflow(true);
   }

   public void createTaskActionBar(Composite parent) {

      // Button composite for state transitions, etc
      Composite bComp = new Composite(parent, SWT.NONE);
      // bComp.setBackground(mainSComp.getDisplay().getSystemColor(SWT.COLOR_CYAN));
      bComp.setLayout(new GridLayout(2, false));
      bComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      Composite leftComp = new Composite(bComp, SWT.NONE);
      leftComp.setLayout(new GridLayout());
      leftComp.setLayoutData(new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL));

      extraInfoLabel = new Label(leftComp, SWT.NONE);
      extraInfoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      extraInfoLabel.setText("");
      extraInfoLabel.setForeground(Displays.getSystemColor(SWT.COLOR_RED));

      Composite rightComp = new Composite(bComp, SWT.NONE);
      rightComp.setLayout(new GridLayout());
      rightComp.setLayoutData(new GridData(GridData.END));

      ToolBar toolBar = new ToolBar(rightComp, SWT.FLAT | SWT.RIGHT);
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      toolBar.setLayoutData(gd);
      ToolItem item = null;

      item = new ToolItem(toolBar, SWT.PUSH);
      item.setImage(ImageManager.getImage(PluginUiImage.REFRESH));
      item.setToolTipText("Refresh");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            loadTable();
         }
      });

   }

   public void loadTable() {
      try {
         if (portXViewer != null && teamArt != null && portXViewer.getContentProvider() != null) {
            Collection<TeamWorkFlowArtifact> portingTeamWorkflows = getStoredTeamWorkflows();
            if (!portingTeamWorkflows.isEmpty()) {
               portXViewer.setInput(portingTeamWorkflows);
            } else {
               portXViewer.setInput("Drop Team Workflows to Port");
            }
            portXViewer.refresh();
            resizeTable();
            refresh();
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @SuppressWarnings("rawtypes")
   public List<Branch> getSelectedBranches() {
      List<Branch> items = new ArrayList<Branch>();
      if (portXViewer == null) {
         return items;
      }
      if (portXViewer.getSelection().isEmpty()) {
         return items;
      }
      Iterator i = ((IStructuredSelection) portXViewer.getSelection()).iterator();
      while (i.hasNext()) {
         Object obj = i.next();
         items.add((Branch) obj);
      }
      return items;
   }

   @Override
   public Control getControl() {
      if (portXViewer == null) {
         return null;
      }
      return portXViewer.getTree();
   }

   @Override
   public void dispose() {
      if (portXViewer != null) {
         portXViewer.dispose();
      }
      OseeEventManager.removeListener(this);
   }

   @Override
   public void refresh() {
      if (portXViewer == null || portXViewer.getTree() == null || portXViewer.getTree().isDisposed()) {
         return;
      }
      setXviewerTree();
   }

   private void updateExtraInfoLabel(final int color, final String infoStr) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(extraInfoLabel)) {
               String currentString = extraInfoLabel.getText();
               if ((infoStr == null && currentString != null) || //
               (infoStr != null && currentString == null) || //
               (infoStr != null && currentString != null && !infoStr.equals(currentString))) {
                  extraInfoLabel.setText("Double-click item to open workflow");
               }
               extraInfoLabel.setForeground(Displays.getSystemColor(color));
            }
         }
      });
   }

   @Override
   public IStatus isValid() {
      Status returnStatus = new Status(IStatus.OK, getClass().getSimpleName(), "");
      //      try {
      int backgroundColor = SWT.COLOR_BLACK;
      String infoStr = "Double-click item to open Action";
      updateExtraInfoLabel(backgroundColor, infoStr);
      //      } catch (OseeCoreException ex) {
      //         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      //         return new Status(IStatus.ERROR, getClass().getSimpleName(), ex.getLocalizedMessage());
      //      }
      return returnStatus;
   }

   public PortXViewer getXViewer() {
      return portXViewer;
   }

   @Override
   public Object getData() {
      return portXViewer.getInput();
   }

   public IDirtiableEditor getEditor() {
      return editor;
   }

   public void setEditor(IDirtiableEditor editor) {
      this.editor = editor;
   }

   @Override
   public void setArtifact(Artifact artifact) throws OseeCoreException {
      if (!(artifact.isOfType(AtsArtifactTypes.TeamWorkflow))) {
         throw new OseeStateException("Must be TeamWorkflowArtifact, set was a [%s]", artifact.getArtifactTypeName());
      }
      this.teamArt = TeamWorkFlowManager.cast(artifact);
      loadTable();
   }

   @Override
   public Result isDirty() {
      try {
         List<TeamWorkFlowArtifact> storedTeamWfs = getStoredTeamWorkflows();
         List<TeamWorkFlowArtifact> widgetTeamWfs = getWidgetTeamWorkflows();
         if (!Collections.isEqual(storedTeamWfs, widgetTeamWfs)) {
            return Result.TrueResult;
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return new Result(false, "Port Widget is Dirty");
   }

   @SuppressWarnings("unchecked")
   public List<TeamWorkFlowArtifact> getWidgetTeamWorkflows() {
      if (getXViewer() != null) {
         Object input = getXViewer().getInput();
         if (input instanceof Collection) {
            return (List<TeamWorkFlowArtifact>) getXViewer().getInput();
         }
      }
      return java.util.Collections.emptyList();
   }

   public List<TeamWorkFlowArtifact> getStoredTeamWorkflows() throws OseeCoreException {
      return this.teamArt.getRelatedArtifacts(AtsRelationTypes.Port_From, TeamWorkFlowArtifact.class);
   }

   @Override
   public void revert() {
      // do nothing
   }

   @Override
   public void saveToArtifact() {
      // do nothing - auto-save on drop
   }

   public TeamWorkFlowArtifact getTeamArt() {
      return teamArt;
   }

   @Override
   public Control getErrorMessageControl() {
      return labelWidget;
   }

   @Override
   public String toString() {
      return String.format("%s", getLabel());
   }

   @Override
   public void handleBranchEvent(Sender sender, final BranchEvent branchEvent) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            redrawComposite();
         }
      });

   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      return null;
   }

   @Override
   public boolean isEmpty() {
      return portXViewer.getXPortTableWidget().getXViewer().getTree().getItemCount() == 0;
   }

}
