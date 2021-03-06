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

package org.eclipse.osee.coverage.editor.xcover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoverageOptionManager;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.model.IWorkProductTaskProvider;
import org.eclipse.osee.coverage.util.ISaveable;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.widgets.GenericXWidget;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author Donald G. Dunne
 */
public class XCoverageViewer extends GenericXWidget {

   protected CoverageXViewer xViewer;
   public final static String normalColor = "#EEEEEE";
   private Label extraInfoLabel;
   private Tree tree;
   private final Collection<TableType> tableTypes;
   private final ISaveable saveable;
   private final CoverageOptionManager coverageOptionManager;
   private final IWorkProductTaskProvider workProductTaskProvider;
   public static enum TableType {
      Package,
      Merge,
      Import
   };

   public XCoverageViewer(ISaveable saveable, CoverageOptionManager coverageOptionManager, IWorkProductTaskProvider workProductTaskProvider, TableType tableType, TableType... types) {
      super("Coverage Items");
      this.saveable = saveable;
      this.coverageOptionManager = coverageOptionManager;
      this.workProductTaskProvider = workProductTaskProvider;
      this.tableTypes = Collections.getAggregate(types);
      this.tableTypes.add(tableType);
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

      createTaskActionBar(mainComp);

      xViewer = new CoverageXViewer(mainComp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION, this);
      xViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

      xViewer.setContentProvider(new CoverageContentProvider(xViewer));
      xViewer.setLabelProvider(new CoverageLabelProvider(xViewer));

      if (toolkit != null) {
         toolkit.adapt(xViewer.getStatusLabel(), false, false);
      }

      // NOTE: Don't adapt the tree using xToolkit cause will loose xViewer's context menu
      updateExtraLabel();
   }

   public void updateExtraLabel() {
      extraInfoLabel.setText("");
   }

   public void setXviewerTree(boolean expand) {
      tree = xViewer.getTree();
      GridData gridData = new GridData(GridData.FILL_BOTH);
      gridData.widthHint = 50;
      tree.setLayout(ALayout.getZeroMarginLayout());
      tree.setLayoutData(gridData);
      tree.setHeaderVisible(true);
      tree.setLinesVisible(true);
   }

   public void createTaskActionBar(Composite parent) {

      // Button composite for state transitions, etc
      Composite composite = new Composite(parent, SWT.NONE);
      composite.setLayout(ALayout.getZeroMarginLayout());
      composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      extraInfoLabel = new Label(composite, SWT.NONE);
      extraInfoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      extraInfoLabel.setText("");
      extraInfoLabel.setForeground(Displays.getSystemColor(SWT.COLOR_RED));

   }

   public ScrolledForm getForm(Composite composite) {
      ScrolledForm form = null;
      if (composite == null) {
         return null;
      }
      if (composite instanceof ScrolledForm) {
         return (ScrolledForm) composite;
      }
      if (!(composite instanceof ScrolledForm)) {
         form = getForm(composite.getParent());
      }
      return form;
   }

   public void loadTable(Collection<ICoverage> items) {
      try {
         xViewer.setInput(items);
         xViewer.refresh();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public ArrayList<ICoverage> getSelectedCoverageItems() {
      ArrayList<ICoverage> items = new ArrayList<ICoverage>();
      if (xViewer == null) {
         return items;
      }
      if (xViewer.getSelection().isEmpty()) {
         return items;
      }
      Iterator<?> i = ((IStructuredSelection) xViewer.getSelection()).iterator();
      while (i.hasNext()) {
         Object obj = i.next();
         items.add((ICoverage) obj);
      }
      return items;
   }

   @Override
   public Control getControl() {
      return xViewer.getTree();
   }

   @Override
   public void dispose() {
      xViewer.dispose();
   }

   @Override
   public void refresh() {
      if (xViewer == null || xViewer.getTree() == null || xViewer.getTree().isDisposed()) {
         return;
      }
      xViewer.refresh();
   }

   @Override
   public String toHTML(String labelFont) {
      if (getXViewer().getTree().getItemCount() == 0) {
         return "";
      }
      StringBuffer html = new StringBuffer();
      html.append(AHTML.addSpace(1) + AHTML.getLabelStr(AHTML.LABEL_FONT, "Coverage"));
      return html.toString();
   }

   public CoverageXViewer getXViewer() {
      return xViewer;
   }

   @Override
   public Object getData() {
      return xViewer.getInput();
   }

   @Override
   public Control getErrorMessageControl() {
      return labelWidget;
   }

   public boolean isType(TableType tableType) {
      if (tableTypes.contains(tableType)) {
         return true;
      }
      return false;
   }

   public ISaveable getSaveable() {
      return saveable;
   }

   public CoverageOptionManager getCoverageOptionManager() {
      return coverageOptionManager;
   }

   public IWorkProductTaskProvider getWorkProductTaskProvider() {
      return workProductTaskProvider;
   }

   @Override
   public boolean isEmpty() {
      return xViewer.getTree().getItemCount() == 0;
   }

}
