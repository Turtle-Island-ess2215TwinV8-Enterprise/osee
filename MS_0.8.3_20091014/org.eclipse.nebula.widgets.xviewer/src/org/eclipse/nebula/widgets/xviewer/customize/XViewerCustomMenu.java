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

package org.eclipse.nebula.widgets.xviewer.customize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.widgets.xviewer.Activator;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerColumnLabelProvider;
import org.eclipse.nebula.widgets.xviewer.XViewerColumnSorter;
import org.eclipse.nebula.widgets.xviewer.XViewerComputedColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.nebula.widgets.xviewer.XViewerTreeReport;
import org.eclipse.nebula.widgets.xviewer.util.internal.ArrayTreeContentProvider;
import org.eclipse.nebula.widgets.xviewer.util.internal.CollectionsUtil;
import org.eclipse.nebula.widgets.xviewer.util.internal.HtmlUtil;
import org.eclipse.nebula.widgets.xviewer.util.internal.XViewerLib;
import org.eclipse.nebula.widgets.xviewer.util.internal.XViewerLog;
import org.eclipse.nebula.widgets.xviewer.util.internal.dialog.HtmlDialog;
import org.eclipse.nebula.widgets.xviewer.util.internal.dialog.ListDialogSortable;
import org.eclipse.nebula.widgets.xviewer.util.internal.dialog.XCheckFilteredTreeDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Allow for the customization of the xViewer's right-click menus. Full menu can be used or selected Actions accessed
 * for partial implementation in existing menus
 * 
 * @author Donald G. Dunne
 */
public class XViewerCustomMenu {

   protected XViewer xViewer;
   private final Clipboard clipboard = new Clipboard(null);

   protected Action filterByColumn, clearAllSorting, clearAllFilters, tableProperties, viewTableReport,
         columnMultiEdit, removeSelected, removeNonSelected, copySelected, showColumn, addComputedColumn, sumColumn,
         hideColumn, copySelectedCell, viewSelectedCell, uniqueValues;
   private Boolean headerMouseClick = false;

   public XViewerCustomMenu() {
   }

   public XViewerCustomMenu(XViewer xViewer) {
      this.xViewer = xViewer;
   }

   public void init(final XViewer xviewer) {
      this.xViewer = xviewer;
      setupActions();
      xViewer.getTree().addKeyListener(new KeySelectedListener());
      xViewer.getTree().addDisposeListener(new DisposeListener() {
         public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
            if (clipboard != null) clipboard.dispose();
         };
      });
      xViewer.getMenuManager().addMenuListener(new IMenuListener() {
         public void menuAboutToShow(IMenuManager manager) {
            if (headerMouseClick) {
               setupMenuForHeader();
               xviewer.updateMenuActionsForHeader();
            } else {
               setupMenuForTable();
               xViewer.updateMenuActionsForTable();
            }
         }
      });
      xViewer.getTree().addListener(SWT.MenuDetect, new Listener() {
         public void handleEvent(Event event) {
            Point pt = Display.getCurrent().map(null, xViewer.getTree(), new Point(event.x, event.y));
            Rectangle clientArea = xViewer.getTree().getClientArea();
            headerMouseClick = clientArea.y <= pt.y && pt.y < (clientArea.y + xViewer.getTree().getHeaderHeight());
         }
      });
   }

   protected void setupMenuForHeader() {
      TreeColumn selTreeCol = xViewer.getRightClickSelectedColumn();
      XViewerColumn selXCol = (XViewerColumn) selTreeCol.getData();

      MenuManager mm = xViewer.getMenuManager();
      mm.add(showColumn);
      mm.add(hideColumn);
      mm.add(addComputedColumn);
      mm.add(copySelectedCell);
      mm.add(new Separator());
      mm.add(filterByColumn);
      mm.add(clearAllFilters);
      mm.add(clearAllSorting);
      mm.add(new Separator());
      mm.add(sumColumn);
      mm.add(uniqueValues);
   }

   protected void setupMenuForTable() {
      MenuManager mm = xViewer.getMenuManager();
      mm.add(new GroupMarker(XViewer.MENU_GROUP_PRE));
      mm.add(new Separator());
      mm.add(tableProperties);
      mm.add(viewTableReport);
      if (xViewer.isColumnMultiEditEnabled()) {
         mm.add(columnMultiEdit);
      }
      mm.add(viewSelectedCell);
      mm.add(copySelected);
      mm.add(copySelectedCell);
      mm.add(new Separator());
      mm.add(filterByColumn);
      mm.add(clearAllFilters);
      mm.add(clearAllSorting);
      mm.add(new Separator());
      mm.add(removeSelected);
      mm.add(removeNonSelected);
      mm.add(new GroupMarker(XViewer.MENU_GROUP_POST));
   }

   public void createTableCustomizationMenuItem(Menu popupMenu) {
      final MenuItem item = new MenuItem(popupMenu, SWT.CASCADE);
      item.setText("Table Customization");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            xViewer.getCustomizeMgr().handleTableCustomization();
         }
      });
   }

   public void createViewTableReportMenuItem(Menu popupMenu) {
      final MenuItem item = new MenuItem(popupMenu, SWT.CASCADE);
      item.setText("View Table Report");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            performViewTableReport();
         }
      });
   }

   public void addFilterMenuBlock(Menu popupMenu) {
      createFilterByColumnMenuItem(popupMenu);
      createClearAllFiltersMenuItem(popupMenu);
      createClearAllSortingMenuItem(popupMenu);
   }

   public void createFilterByColumnMenuItem(Menu popupMenu) {
      final MenuItem item = new MenuItem(popupMenu, SWT.CASCADE);
      item.setText("Filter By Column");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            performFilterByColumn();
         }
      });
   }

   public void createClearAllFiltersMenuItem(Menu popupMenu) {
      final MenuItem item = new MenuItem(popupMenu, SWT.CASCADE);
      item.setText("Clear All Filters");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            xViewer.getCustomizeMgr().clearFilters();
         }
      });
   }

   public void createClearAllSortingMenuItem(Menu popupMenu) {
      final MenuItem item = new MenuItem(popupMenu, SWT.CASCADE);
      item.setText("Clear All Sorting");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            xViewer.getCustomizeMgr().clearSorter();
         }
      });
   }

   public void addCopyViewMenuBlock(Menu popupMenu) {
      createViewSelectedCellMenuItem(popupMenu);
      createCopyRowsMenuItem(popupMenu);
      createCopyCellsMenuItem(popupMenu);
   }

   public void createCopyRowsMenuItem(Menu popupMenu) {
      final MenuItem item = new MenuItem(popupMenu, SWT.CASCADE);
      item.setText("Copy Selected Row(s)- Ctrl-C");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            performCopy();
         }
      });
   }

   public void createCopyCellsMenuItem(Menu popupMenu) {
      final MenuItem item = new MenuItem(popupMenu, SWT.CASCADE);
      item.setText("Copy Selected Column - Ctrl-Shift-C");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            performCopyCell();
         }
      });
   }

   public void createViewSelectedCellMenuItem(Menu popupMenu) {
      final MenuItem item = new MenuItem(popupMenu, SWT.CASCADE);
      item.setText("View Selected Cell Data");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            performViewCell();
         }
      });
   }

   private static PatternFilter patternFilter = new PatternFilter();

   protected void handleShowColumn() {
      TreeColumn insertTreeCol = xViewer.getRightClickSelectedColumn();
      XViewerColumn insertXCol = (XViewerColumn) insertTreeCol.getData();
      XCheckFilteredTreeDialog dialog =
            new XCheckFilteredTreeDialog("Show Column", "Select Columns to Show", patternFilter,
                  new ArrayTreeContentProvider(), new XViewerColumnLabelProvider(), new XViewerColumnSorter());
      dialog.setInput(xViewer.getCustomizeMgr().getCurrentHiddenTableColumns());
      if (dialog.open() == 0) {
         //         System.out.println("Selected " + dialog.getChecked());
         //         System.out.println("Selected column to add before " + insertXCol);
         CustomizeData custData = xViewer.getCustomizeMgr().generateCustDataFromTable();
         List<XViewerColumn> xCols = custData.getColumnData().getColumns();
         List<XViewerColumn> newXCols = new ArrayList<XViewerColumn>();
         for (XViewerColumn currXCol : xCols) {
            if (currXCol.equals(insertXCol)) {
               for (Object obj : dialog.getChecked()) {
                  XViewerColumn newXCol = (XViewerColumn) obj;
                  newXCol.setShow(true);
                  newXCols.add(newXCol);
               }
            }
            newXCols.add(currXCol);
         }
         custData.getColumnData().setColumns(newXCols);
         xViewer.getCustomizeMgr().loadCustomization(custData);
         xViewer.refresh();
      }
   }

   protected void handleAddComputedColumn() {
      TreeColumn insertTreeCol = xViewer.getRightClickSelectedColumn();
      XViewerColumn insertXCol = (XViewerColumn) insertTreeCol.getData();
      XCheckFilteredTreeDialog dialog =
            new XCheckFilteredTreeDialog("Add Computed Column", String.format("Column to compute against [%s]",
                  insertXCol.getName() + "(" + insertXCol.getId() + ")") + "\n\nSelect Columns to Add", patternFilter,
                  new ArrayTreeContentProvider(), new XViewerColumnLabelProvider(), new XViewerColumnSorter());
      Collection<XViewerComputedColumn> computedCols = xViewer.getComputedColumns(insertXCol);
      if (computedCols.size() == 0) {
         XViewerLib.popup("ERROR", "Selected column has no applicable computed columns");
         return;
      }
      dialog.setInput(computedCols);
      if (dialog.open() == 0) {
         //         System.out.println("Selected " + dialog.getChecked());
         //         System.out.println("Selected column to add before " + insertXCol);
         CustomizeData custData = xViewer.getCustomizeMgr().generateCustDataFromTable();
         List<XViewerColumn> xCols = custData.getColumnData().getColumns();
         List<XViewerColumn> newXCols = new ArrayList<XViewerColumn>();
         for (XViewerColumn currXCol : xCols) {
            if (currXCol.equals(insertXCol)) {
               for (Object obj : dialog.getChecked()) {
                  XViewerComputedColumn newComputedXCol = ((XViewerComputedColumn) obj).copy();
                  newComputedXCol.setShow(true);
                  newComputedXCol.setSourceXViewerColumn(insertXCol);
                  newComputedXCol.setXViewer(xViewer);
                  newXCols.add(newComputedXCol);
               }
            }
            newXCols.add(currXCol);
         }
         custData.getColumnData().setColumns(newXCols);
         xViewer.getCustomizeMgr().loadCustomization(custData);
         xViewer.refresh();
      }
   }

   protected void handleUniqeValuesColumn() {
      TreeColumn treeCol = xViewer.getRightClickSelectedColumn();
      XViewerColumn xCol = (XViewerColumn) treeCol.getData();

      TreeItem[] items = xViewer.getTree().getSelection();
      if (items.length == 0) {
         items = xViewer.getTree().getItems();
      }
      if (items.length == 0) {
         XViewerLib.popup("ERROR", "No items to sum");
         return;
      }
      Set<String> values = new HashSet<String>();
      for (TreeItem item : items) {
         for (int x = 0; x < xViewer.getTree().getColumnCount(); x++) {
            if (xViewer.getTree().getColumn(x).equals(treeCol)) {
               values.add(((XViewerLabelProvider) xViewer.getLabelProvider()).getColumnText(item.getData(), x));
            }
         }
      }
      String html = HtmlUtil.simplePage(HtmlUtil.textToHtml(CollectionsUtil.toString("\n", values)));
      new HtmlDialog("Unique Values", String.format("Unique Values for column [%s]", xCol.getName()), html).open();
   }

   protected void handleSumColumn() {
      TreeColumn treeCol = xViewer.getRightClickSelectedColumn();
      XViewerColumn xCol = (XViewerColumn) treeCol.getData();
      if (!xCol.isSummable()) return;

      TreeItem[] items = xViewer.getTree().getSelection();
      if (items.length == 0) {
         items = xViewer.getTree().getItems();
      }
      if (items.length == 0) {
         XViewerLib.popup("ERROR", "No items to sum");
         return;
      }
      List<String> values = new ArrayList<String>();
      for (TreeItem item : items) {
         for (int x = 0; x < xViewer.getTree().getColumnCount(); x++) {
            if (xViewer.getTree().getColumn(x).equals(treeCol)) {
               values.add(((XViewerLabelProvider) xViewer.getLabelProvider()).getColumnText(item.getData(), x));
            }
         }
      }
      XViewerLib.popup("Sum", xCol.sumValues(values));
   }

   protected void handleHideColumn() {
      TreeColumn insertTreeCol = xViewer.getRightClickSelectedColumn();
      XViewerColumn insertXCol = (XViewerColumn) insertTreeCol.getData();
      //      System.out.println("Hide column " + insertXCol);
      CustomizeData custData = xViewer.getCustomizeMgr().generateCustDataFromTable();
      List<XViewerColumn> xCols = custData.getColumnData().getColumns();
      List<XViewerColumn> newXCols = new ArrayList<XViewerColumn>();
      for (XViewerColumn currXCol : xCols) {
         if (currXCol.equals(insertXCol)) {
            currXCol.setShow(false);
         }
         newXCols.add(currXCol);
      }
      custData.getColumnData().setColumns(newXCols);
      xViewer.getCustomizeMgr().loadCustomization(custData);
      xViewer.refresh();
   }

   protected void setupActions() {
      showColumn = new Action("Show Column") {
         @Override
         public void run() {
            handleShowColumn();
         };
      };
      addComputedColumn = new Action("Add Computed Column") {
         @Override
         public void run() {
            handleAddComputedColumn();
         };
      };
      sumColumn = new Action("Sum Selected for Column") {
         @Override
         public void run() {
            handleSumColumn();
         };
      };
      uniqueValues = new Action("Unique Values") {
         @Override
         public void run() {
            handleUniqeValuesColumn();
         };
      };
      hideColumn = new Action("Hide Column") {
         @Override
         public void run() {
            handleHideColumn();
         };
      };
      removeSelected = new Action("Remove Selected from View") {
         @Override
         public void run() {
            performRemoveSelectedRows();
         };
      };
      removeNonSelected = new Action("Remove Non-Selected from View") {
         @Override
         public void run() {
            performRemoveNonSelectedRows();
         };
      };
      copySelected = new Action("Copy Selected Row(s)- Ctrl-C") {
         @Override
         public void run() {
            performCopy();
         };
      };
      viewSelectedCell = new Action("View Selected Cell Data") {
         @Override
         public void run() {
            performViewCell();
         };
      };
      copySelectedCell = new Action("Copy Selected Column - Ctrl-Shift-C") {
         @Override
         public void run() {
            performCopyCell();
         };
      };
      clearAllSorting = new Action("Clear All Sorting") {
         @Override
         public void run() {
            xViewer.getCustomizeMgr().clearSorter();
         };
      };
      clearAllFilters = new Action("Clear All Filters") {
         @Override
         public void run() {
            xViewer.getCustomizeMgr().clearFilters();
         };
      };
      filterByColumn = new Action("Filter By Column") {
         @Override
         public void run() {
            performFilterByColumn();
         };
      };
      tableProperties = new Action("Table Customization") {
         @Override
         public void run() {
            xViewer.getCustomizeMgr().handleTableCustomization();
         }
      };
      viewTableReport = new Action("View Table Report") {
         @Override
         public void run() {
            performViewTableReport();
         }
      };
      columnMultiEdit = new Action("Column Multi Edit") {
         @Override
         public void run() {
            Set<TreeColumn> editableColumns = new HashSet<TreeColumn>();
            Collection<TreeItem> selectedTreeItems = Arrays.asList(xViewer.getTree().getSelection());
            for (TreeColumn treeCol : xViewer.getTree().getColumns()) {
               if (xViewer.isColumnMultiEditable(treeCol, selectedTreeItems)) {
                  editableColumns.add(treeCol);
               }
            }
            if (editableColumns.size() == 0) {
               XViewerLib.popup("ERROR", "No Columns Are Multi-Editable");
               return;
            }
            ListDialogSortable ld = new ListDialogSortable(new XViewerColumnSorter(), xViewer.getTree().getShell());
            ld.setMessage("Select Column to Edit");
            ld.setInput(editableColumns);
            ld.setLabelProvider(treeColumnLabelProvider);
            ld.setContentProvider(new ArrayContentProvider());
            ld.setTitle("Select Column to Edit");
            int result = ld.open();
            if (result != 0) return;
            xViewer.handleColumnMultiEdit((TreeColumn) ld.getResult()[0], selectedTreeItems);
         }
      };
   }

   private void performViewTableReport() {
      if (xViewer.getXViewerFactory().getXViewerTreeReport(xViewer) != null) {
         xViewer.getXViewerFactory().getXViewerTreeReport(xViewer).open();
      } else {
         new XViewerTreeReport(xViewer).open();
      }
   }

   private class KeySelectedListener implements KeyListener {
      public void keyPressed(KeyEvent e) {
      }

      public void keyReleased(KeyEvent e) {
         if (e.keyCode == 'c' && e.stateMask == (SWT.CONTROL | SWT.SHIFT)) {
            performCopyCell();
         } else if (e.keyCode == 'c' && e.stateMask == SWT.CONTROL) {
            performCopy();
         }
      }
   }

   private void performRemoveSelectedRows() {
      try {
         TreeItem[] items = xViewer.getTree().getSelection();
         if (items.length == 0) {
            XViewerLib.popup("ERROR", "No items to copy");
            return;
         }
         Set<Object> objs = new HashSet<Object>();
         for (TreeItem item : items) {
            objs.add(item.getData());
         }
         xViewer.remove(objs);
      } catch (Exception ex) {
         XViewerLog.logAndPopup(Activator.class, Level.SEVERE, ex);
      }
   }

   private void performRemoveNonSelectedRows() {
      try {
         TreeItem[] items = xViewer.getTree().getSelection();
         if (items.length == 0) {
            XViewerLib.popup("ERROR", "No items to copy");
            return;
         }
         Set<Object> keepObjects = new HashSet<Object>();
         for (TreeItem item : items) {
            keepObjects.add(item.getData());
         }
         xViewer.load(keepObjects);
      } catch (Exception ex) {
         XViewerLog.logAndPopup(Activator.class, Level.SEVERE, ex);
      }
   }

   private void performViewCell() {
      try {
         TreeColumn treeCol = xViewer.getRightClickSelectedColumn();
         TreeItem treeItem = xViewer.getRightClickSelectedItem();
         if (treeCol != null) {
            XViewerColumn xCol = (XViewerColumn) treeCol.getData();
            String data =
                  ((XViewerLabelProvider) xViewer.getLabelProvider()).getColumnText(treeItem.getData(), xCol,
                        xViewer.getRightClickSelectedColumnNum());
            if (data != null && !data.equals("")) {
               String html = HtmlUtil.simplePage(HtmlUtil.pre(HtmlUtil.textToHtml(data)));
               new HtmlDialog(treeCol.getText() + " Data", treeCol.getText() + " Data", html).open();
            }
         }
      } catch (Exception ex) {
         XViewerLog.logAndPopup(Activator.class, Level.SEVERE, ex);
      }
   }

   private void performFilterByColumn() {
      Set<TreeColumn> visibleColumns = new HashSet<TreeColumn>();
      for (TreeColumn treeCol : xViewer.getTree().getColumns())
         if (treeCol.getWidth() > 0) visibleColumns.add(treeCol);
      if (visibleColumns.size() == 0) {
         XViewerLib.popup("ERROR", "No Columns Are Available");
         return;
      }
      ListDialog ld = new ListDialog(xViewer.getTree().getShell()) {
         @Override
         protected Control createDialogArea(Composite container) {
            Control control = super.createDialogArea(container);
            getTableViewer().setSorter(treeColumnSorter);
            return control;
         }
      };
      ld.setMessage("Select Column to Filter");
      ld.setInput(visibleColumns);
      ld.setLabelProvider(treeColumnLabelProvider);
      ld.setContentProvider(new ArrayContentProvider());
      ld.setTitle("Select Column to Filter");
      int result = ld.open();
      if (result != 0) return;
      TreeColumn treeCol = (TreeColumn) ld.getResult()[0];
      String colId = ((XViewerColumn) treeCol.getData()).getId();
      xViewer.getColumnFilterDataUI().promptSetFilter(colId);

   }

   private void performCopyCell() {
      Set<TreeColumn> visibleColumns = new HashSet<TreeColumn>();
      TreeItem[] items = xViewer.getTree().getSelection();
      if (items.length == 0) {
         XViewerLib.popup("ERROR", "Select items to copy");
         return;
      }
      ArrayList<String> textTransferData = new ArrayList<String>();
      ITableLabelProvider labelProv = (ITableLabelProvider) xViewer.getLabelProvider();
      for (TreeColumn treeCol : xViewer.getTree().getColumns())
         if (treeCol.getWidth() > 0) visibleColumns.add(treeCol);
      if (visibleColumns.size() == 0) {
         XViewerLib.popup("ERROR", "No Columns Are Available");
         return;
      }
      ListDialog ld = new ListDialog(xViewer.getTree().getShell()) {
         @Override
         protected Control createDialogArea(Composite container) {
            Control control = super.createDialogArea(container);
            getTableViewer().setSorter(treeColumnSorter);
            return control;
         }
      };
      ld.setMessage("Select Column to Copy");
      ld.setInput(visibleColumns);
      ld.setLabelProvider(treeColumnLabelProvider);
      ld.setContentProvider(new ArrayContentProvider());
      ld.setTitle("Select Column to Copy");
      int result = ld.open();
      if (result != 0) return;
      TreeColumn treeCol = (TreeColumn) ld.getResult()[0];
      StringBuffer sb = new StringBuffer();
      for (TreeItem item : items) {
         for (int x = 0; x < xViewer.getTree().getColumnCount(); x++) {
            if (xViewer.getTree().getColumn(x).equals(treeCol)) {
               sb.append(labelProv.getColumnText(item.getData(), x) + "\n");
            }
         }
      }
      textTransferData.add(sb.toString());

      if (textTransferData.size() > 0) clipboard.setContents(new Object[] {CollectionsUtil.toString(textTransferData,
            null, ", ", null)}, new Transfer[] {TextTransfer.getInstance()});
   }

   private void performCopy() {
      TreeItem[] items = xViewer.getTree().getSelection();
      if (items.length == 0) {
         XViewerLib.popup("ERROR", "No items to copy");
         return;
      }
      ArrayList<String> textTransferData = new ArrayList<String>();
      ITableLabelProvider labelProv = (ITableLabelProvider) xViewer.getLabelProvider();
      if (items != null && items.length > 0) {
         StringBuffer sb = new StringBuffer();
         for (TreeItem item : items) {
            List<String> strs = new ArrayList<String>();
            for (int x = 0; x < xViewer.getTree().getColumnCount(); x++) {
               if (xViewer.getTree().getColumn(x).getWidth() > 0) {
                  String data = labelProv.getColumnText(item.getData(), x);
                  if (data != null) strs.add(data);
               }
            }
            sb.append(CollectionsUtil.toString("\t", strs) + "\n");
         }
         textTransferData.add(sb.toString());

         if (textTransferData.size() > 0) clipboard.setContents(new Object[] {CollectionsUtil.toString(
               textTransferData, null, ", ", null)}, new Transfer[] {TextTransfer.getInstance()});
      }
   }

   static LabelProvider treeColumnLabelProvider = new LabelProvider() {
      @Override
      public String getText(Object element) {
         if (element instanceof TreeColumn) {
            return ((TreeColumn) element).getText();
         }
         return "Unknown element type";
      }
   };

   static ViewerSorter treeColumnSorter = new ViewerSorter() {
      @SuppressWarnings("unchecked")
      @Override
      public int compare(Viewer viewer, Object e1, Object e2) {
         return getComparator().compare(((TreeColumn) e1).getText(), ((TreeColumn) e2).getText());
      }
   };

}
