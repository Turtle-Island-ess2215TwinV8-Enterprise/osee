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

import java.util.Collection;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.FontManager;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tree;

/**
 * @author Donald G. Dunne
 */
public class PortTableDisplay {

   private static final int paddedTableHeightHint = 2;

   private final XPortTableWidget master;
   private int lastDefectListSize = 0;
   private Composite mainComp;
   private Composite parentComp;
   private XPortSorter sorter;

   private PortXViewer portXViewer;
   private ToolTip errorToolTip;
   private Composite stackComposite;
   private StackLayout stackLayout;
   private Label warningLabel;
   private Composite warningComposite;
   private Composite tableArea;
   private Button applyAllButton;
   private Button revertAllButton;

   public PortTableDisplay(XPortTableWidget ptw) {
      master = ptw;
   }

   public Control getControl() {
      return portXViewer != null ? portXViewer.getTree() : null;
   }

   public void dispose() {
      if (portXViewer != null) {
         portXViewer.dispose();
      }
   }

   protected void createControls(Composite parent, int horizontalSpan) {
      parentComp = new Composite(parent, SWT.FLAT);
      parentComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      parentComp.setLayout(ALayout.getZeroMarginLayout());

      redrawComposite();
   }

   public void redrawComposite() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(parentComp)) {
               if (mainComp != null && Widgets.isAccessible(mainComp)) {
                  mainComp.dispose();
                  portXViewer.dispose();
                  portXViewer = null;
               }

               mainComp = new Composite(parentComp, SWT.FLAT);
               mainComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
               mainComp.setLayout(new GridLayout(1, true));

               master.paintBordersFor(mainComp);

               stackComposite = new Composite(mainComp, SWT.NONE);
               stackLayout = new StackLayout();
               stackComposite.setLayout(stackLayout);
               stackComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

               warningComposite = createMessageArea(stackComposite);
               tableArea = createTableArea(stackComposite);

               parentComp.layout();

               master.getPortController().updatePortArea();
            }
         }
      });
   }

   private Composite createMessageArea(Composite parent) {
      Composite composite = new Composite(parent, SWT.BORDER);
      composite.setLayout(new GridLayout(2, false));
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      composite.setBackground(Displays.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

      Label image = new Label(composite, SWT.NONE);
      image.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
      image.setImage(ImageManager.getImage(FrameworkImage.LOCKED_KEY));
      image.setBackground(Displays.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

      warningLabel = new Label(composite, SWT.NONE);
      warningLabel.setFont(FontManager.getFont("Courier New", 10, SWT.BOLD));
      warningLabel.setForeground(Displays.getSystemColor(SWT.COLOR_DARK_RED));
      warningLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
      warningLabel.setText("None");
      warningLabel.setBackground(Displays.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
      return composite;
   }

   private Composite createTableArea(Composite parent) {
      Composite tableComp = new Composite(parent, SWT.BORDER);
      tableComp.setLayout(ALayout.getZeroMarginLayout());
      tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

      master.paintBordersFor(tableComp);
      master.adapt(tableComp);

      master.doLabelWidget(tableComp);

      portXViewer = new PortXViewer(tableComp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION, master.getPortController());
      portXViewer.setContentProvider(new XPortContentProvider());
      portXViewer.setLabelProvider(new XPortLabelProvider(portXViewer, master.getPortController()));
      sorter = new XPortSorter();
      portXViewer.setSorter(sorter);

      Tree tree = portXViewer.getTree();
      tree.setLayout(ALayout.getZeroMarginLayout());
      tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      tree.setHeaderVisible(true);
      tree.setLinesVisible(true);

      errorToolTip = new ToolTip(portXViewer.getTree().getShell(), SWT.ICON_WARNING);

      Composite buttonBar = new Composite(tableComp, SWT.NONE);
      buttonBar.setLayout(new GridLayout(3, false));
      buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

      master.adapt(buttonBar);

      applyAllButton = master.createButton(buttonBar, "Apply All", SWT.PUSH);
      applyAllButton.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            master.getPortController().doApplyAll();
         }
      });

      revertAllButton = master.createButton(buttonBar, "Revert All", SWT.PUSH);
      revertAllButton.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            master.getPortController().doRevertAll();
         }
      });

      if (portXViewer.getStatusLabel() != null) {
         master.adapt(portXViewer.getStatusLabel(), false, false);
      }
      new PortTableDragAndDrop(portXViewer.getTree(), XPortTableWidget.WIDGET_NAME, master.getPortController());

      return tableComp;
   }

   private void resizeTable() {
      Tree tree = portXViewer.getTree();
      int defectListSize = portXViewer.getTree().getItemCount();
      if (defectListSize != lastDefectListSize) {
         lastDefectListSize = defectListSize;
         GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);

         int treeItemHeight = portXViewer.getTree().getItemHeight();
         gd.heightHint = treeItemHeight * (paddedTableHeightHint + defectListSize);

         tree.setLayoutData(gd);
         master.reflow(true);
      }
   }

   private void createTaskActionBar(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      composite.setLayout(new GridLayout(2, false));
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      master.adapt(composite);

      ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);
      toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
      master.adapt(toolBar);

      ToolItem item = new ToolItem(toolBar, SWT.PUSH);
      item.setImage(ImageManager.getImage(PluginUiImage.REFRESH));
      item.setToolTipText("Refresh");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            master.getPortController().load();
         }
      });
   }

   public void refresh() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (portXViewer != null && portXViewer.getTree() != null && !portXViewer.getTree().isDisposed()) {
               resizeTable();
            }
         }
      });
   }

   public void showWarningAreaMessage(final String message) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(warningLabel)) {
               warningLabel.setText(message);
            }
         }
      });
   }

   public void showWarningArea() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(stackComposite)) {
               Control control = warningComposite;
               warningLabel.update();
               warningComposite.update();
               stackLayout.topControl = control;
               stackComposite.layout();
               stackComposite.getParent().layout();
            }
         }
      });
   }

   public void showPortData() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(stackComposite)) {
               Control control = tableArea;
               tableArea.update();
               stackLayout.topControl = control;
               stackComposite.layout();
               stackComposite.getParent().layout();
            }
         }
      });
   }

   // used by the drag and drop to show the correct message
   public void showErrorToolTip(final String text, final String message) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(errorToolTip)) {
               errorToolTip.setText(text);
               errorToolTip.setMessage(message);
               errorToolTip.setVisible(true);
               // TODO: the first entry into the area has the wrong bounds - validate bounds somehow?
               Control control = getControl().getParent();
               Point location = control.toDisplay(control.getLocation());
               int x = location.x + control.getSize().x / 3;
               int y = location.y + control.getSize().y / 2;
               Point newLocation = new Point(x, y);
               errorToolTip.setLocation(newLocation);
            }
         }
      });
   }

   public void hideErrorToolTip() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(errorToolTip)) {
               errorToolTip.setVisible(false);
            }
         }
      });
   }

   public void setPortData(Collection<TeamWorkFlowArtifact> workflowData) {
      setTableData(workflowData);
   }

   public void showPortDataMessage(String message) {
      setTableData(message);
   }

   private void setTableData(final Object data) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (portXViewer != null) {
               portXViewer.setInput(data);
               portXViewer.refresh();
            }
         }
      });
   }

   public void setApplyAllEnabled(final boolean enabled) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(applyAllButton)) {
               applyAllButton.setEnabled(enabled);
            }
         }
      });
   }

   public void setRevertAllEnabled(final boolean enabled) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(revertAllButton)) {
               revertAllButton.setEnabled(enabled);
            }
         }
      });
   }
}
