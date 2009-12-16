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

package org.eclipse.nebula.widgets.xviewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.widgets.xviewer.action.TableCustomizationDropDownAction;
import org.eclipse.nebula.widgets.xviewer.column.XViewerDaysTillTodayColumn;
import org.eclipse.nebula.widgets.xviewer.customize.ColumnFilterDataUI;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeManager;
import org.eclipse.nebula.widgets.xviewer.customize.FilterDataUI;
import org.eclipse.nebula.widgets.xviewer.customize.SearchDataUI;
import org.eclipse.nebula.widgets.xviewer.util.internal.XViewerLib;
import org.eclipse.nebula.widgets.xviewer.util.internal.XViewerLog;
import org.eclipse.nebula.widgets.xviewer.util.internal.XViewerMenuDetectListener;
import org.eclipse.nebula.widgets.xviewer.util.internal.XViewerMouseListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Donald G. Dunne
 */
public class XViewer extends TreeViewer {

   public static final String MENU_GROUP_PRE = "XVIEWER MENU GROUP PRE";
   public static final String MENU_GROUP_POST = "XVIEWER MENU GROUP POST";
   private Label statusLabel;
   private final MenuManager menuManager;
   private static boolean ctrlKeyDown = false;
   private static boolean altKeyDown = false;
   protected final IXViewerFactory xViewerFactory;
   private final FilterDataUI filterDataUI;
   private final SearchDataUI searchDataUI;
   private final ColumnFilterDataUI columnFilterDataUI;
   private static boolean ctrlKeyListenersSet = false;
   private XViewerGradient xViewerGradient = null;

   /**
    * @return the columnFilterDataUI
    */
   public ColumnFilterDataUI getColumnFilterDataUI() {
      return columnFilterDataUI;
   }
   private boolean columnMultiEditEnabled = false;
   private CustomizeManager customizeMgr;
   private TreeColumn rightClickSelectedColumn = null;
   private Integer rightClickSelectedColumnNum = null;
   private TreeItem rightClickSelectedItem = null;
   private Color searchColor;

   public XViewer(Composite parent, int style, IXViewerFactory xViewerFactory) {
      this(parent, style, xViewerFactory, false, false);
   }

   public XViewer(Composite parent, int style, IXViewerFactory xViewerFactory, boolean filterRealTime, boolean searchRealTime) {
      super(parent, style);
      this.xViewerFactory = xViewerFactory;
      this.menuManager = new MenuManager();
      this.menuManager.setRemoveAllWhenShown(true);
      this.menuManager.createContextMenu(parent);
      if (xViewerFactory.isFilterUiAvailable()) {
         this.filterDataUI = new FilterDataUI(this, filterRealTime);
      } else {
         this.filterDataUI = null;
      }
      if (xViewerFactory.isSearchUiAvailable()) {
         this.searchDataUI = new SearchDataUI(this, searchRealTime);
      } else {
         this.searchDataUI = null;
      }
      this.columnFilterDataUI = new ColumnFilterDataUI(this);
      try {
         customizeMgr = new CustomizeManager(this, xViewerFactory);
      } catch (Exception ex) {
         XViewerLog.logAndPopup(Activator.class, Level.SEVERE, ex);
      }
      createSupportWidgets(parent);

      Tree tree = getTree();
      tree.setHeaderVisible(true);
      tree.setLinesVisible(true);
      setUseHashlookup(true);
      setupCtrlKeyListener();
      if (xViewerFactory.isCellGradientOn()) {
         xViewerGradient = new XViewerGradient(this);
      }
   }

   private static List<XViewerComputedColumn> computedColumns;

   public Collection<XViewerComputedColumn> getComputedColumns() {
      if (computedColumns == null) {
         computedColumns = new ArrayList<XViewerComputedColumn>();
         computedColumns.add(new XViewerDaysTillTodayColumn());
      }
      return computedColumns;
   }

   public Collection<XViewerComputedColumn> getComputedColumns(XViewerColumn xCol) {
      List<XViewerComputedColumn> matchCols = new ArrayList<XViewerComputedColumn>();
      for (XViewerColumn computedCol : getComputedColumns()) {
         if (((XViewerComputedColumn) computedCol).isApplicableFor(xCol)) {
            matchCols.add((XViewerComputedColumn) computedCol);
         }
      }
      return matchCols;
   }

   public void dispose() {
      if (searchDataUI != null) searchDataUI.dispose();
      if (filterDataUI != null) filterDataUI.dispose();
      columnFilterDataUI.dispose();
   }

   @Override
   public void setLabelProvider(IBaseLabelProvider labelProvider) {
      if (!(labelProvider instanceof XViewerLabelProvider) && !(labelProvider instanceof XViewerStyledTextLabelProvider)) {
         throw new IllegalArgumentException("Label Provider must extend XViewerLabelProvider");
      }
      super.setLabelProvider(labelProvider);
   }

   public void addCustomizeToViewToolbar(final ViewPart viewPart) {
      addCustomizeToViewToolbar(viewPart.getViewSite().getActionBars().getToolBarManager());
   }

   public void updateMenuActionsForTable() {
   }

   public void updateMenuActionsForHeader() {
   }

   public Action getCustomizeAction() {
      return new TableCustomizationDropDownAction(this);
   }

   public void addCustomizeToViewToolbar(IToolBarManager toolbarManager) {
      toolbarManager.add(new TableCustomizationDropDownAction(this));
   }

   protected void createSupportWidgets(Composite parent) {

      searchColor = Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);

      Composite comp = null;
      if (searchDataUI != null || filterDataUI != null || xViewerFactory.isLoadedStatusLabelAvailable()) {
         comp = new Composite(parent, SWT.NONE);
         comp.setLayout(XViewerLib.getZeroMarginLayout(11, false));
         comp.setLayoutData(new GridData(SWT.FILL, SWT.None, true, false));

         if (filterDataUI != null) {
            filterDataUI.createWidgets(comp);
            Label sep1 = new Label(comp, SWT.SEPARATOR);
            GridData gd = new GridData(SWT.RIGHT, SWT.NONE, false, false);
            gd.heightHint = 16;
            sep1.setLayoutData(gd);
         }

         if (searchDataUI != null) {
            searchDataUI.createWidgets(comp);
            Label sep2 = new Label(comp, SWT.SEPARATOR);
            GridData gd = new GridData(SWT.RIGHT, SWT.NONE, false, false);
            gd.heightHint = 16;
            sep2.setLayoutData(gd);
         }

         if (xViewerFactory.isLoadedStatusLabelAvailable()) {
            statusLabel = new Label(comp, SWT.NONE);
            statusLabel.setText(" ");
            statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
         }
      }

      this.addDoubleClickListener(new IDoubleClickListener() {
         public void doubleClick(org.eclipse.jface.viewers.DoubleClickEvent event) {
            ISelection sel = event.getSelection();
            System.out.println(sel);
            handleDoubleClick();
         };
      });
      getTree().addMouseListener(new XViewerMouseListener(this));
      getTree().addListener(SWT.MenuDetect, new XViewerMenuDetectListener(this));
      getTree().setMenu(getMenuManager().getMenu());
      columnFilterDataUI.createWidgets();

      customizeMgr.loadCustomization();
   }

   /**
    * @param col
    */
   public void handleDoubleClick(TreeColumn col, TreeItem item) {
   }

   public void handleDoubleClick() {
   }

   public int getCurrentColumnWidth(XViewerColumn xCol) {
      for (TreeColumn col : getTree().getColumns()) {
         if (col.getText().equals(xCol.getName())) {
            return col.getWidth();
         }
      }
      return 0;
   }

   @Override
   protected void inputChanged(Object input, Object oldInput) {
      super.inputChanged(input, oldInput);
      updateStatusLabel();
   }

   /**
    * Will be called when Alt-Left-Click is done within table cell
    * 
    * @param treeColumn
    * @param treeItem
    * @return true if handled
    */
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      return false;
   }

   /**
    * Will be called when click is within the first 18 pixels of the cell rectangle where the icon would be. This method
    * will be called in addition to handleLeftClick since both are true.
    * 
    * @param treeColumn
    * @param treeItem
    * @return true if handled
    */
   public boolean handleLeftClickInIconArea(TreeColumn treeColumn, TreeItem treeItem) {
      return false;
   }

   /**
    * Will be called when a cell obtains a mouse left-click. This method will be called in addition to
    * handleLeftClickInIconArea if both are true
    * 
    * @param treeColumn
    * @param treeItem
    * @return true if handled
    */
   public boolean handleLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      return false;
   }

   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
   }

   public boolean isColumnMultiEditable(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      if (!isColumnMultiEditEnabled()) return false;
      if (!(treeColumn.getData() instanceof XViewerColumn)) return false;
      return (!((XViewerColumn) treeColumn.getData()).isMultiColumnEditable());
   }

   public XViewerColumn getXTreeColumn(int columnIndex) {
      return (XViewerColumn) getTree().getColumn(columnIndex).getData();
   }

   // Only create one listener for all XViewers
   private void setupCtrlKeyListener() {
      if (ctrlKeyListenersSet == false) {
         ctrlKeyListenersSet = true;
         Display.getCurrent().addFilter(SWT.KeyDown, displayKeysListener);
         Display.getCurrent().addFilter(SWT.KeyUp, displayKeysListener);
         Display.getCurrent().addFilter(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event event) {
               // Clear when focus is lost
               ctrlKeyDown = false;
               altKeyDown = false;
            }
         });
      }
   }
   Listener displayKeysListener = new Listener() {
      public void handleEvent(org.eclipse.swt.widgets.Event event) {
         if (event.keyCode == SWT.CTRL) {
            if (event.type == SWT.KeyDown) {
               ctrlKeyDown = true;
            } else if (event.type == SWT.KeyUp) {
               ctrlKeyDown = false;
            }
         }
         if (event.keyCode == SWT.ALT) {
            if (event.type == SWT.KeyDown) {
               altKeyDown = true;
            } else if (event.type == SWT.KeyUp) {
               altKeyDown = false;
            }
         }
      }
   };

   public void resetDefaultSorter() {
      customizeMgr.resetDefaultSorter();
   }

   /**
    * Override this method if need to perform other tasks upon remove
    * 
    * @param objects
    */
   public void remove(Collection<Object> objects) {
      for (Object obj : objects) {
         super.remove(obj);
      }
   }

   public void load(Collection<Object> objects) {
      super.setInput(objects);
   }

   @Override
   public void setSorter(ViewerSorter sorter) {
      super.setSorter(sorter);
      updateStatusLabel();
   }

   public MenuManager getMenuManager() {
      return this.menuManager;
   }

   public int getVisibleItemCount(TreeItem items[]) {
      int cnt = items.length;
      for (TreeItem item : items)
         if (item.getExpanded()) cnt += getVisibleItemCount(item.getItems());
      return cnt;
   }

   @Override
   public void refresh() {
      if (getTree() == null || getTree().isDisposed()) return;
      super.refresh();
      updateStatusLabel();
   }

   public boolean isFiltered() {
      return getFilters().length > 0;
   }

   @Override
   public void refresh(boolean updateLabels) {
      super.refresh(updateLabels);
      updateStatusLabel();
   }

   @Override
   public void refresh(Object element, boolean updateLabels) {
      super.refresh(element, updateLabels);
      updateStatusLabel();
   }

   @Override
   public void refresh(Object element) {
      super.refresh(element);
      updateStatusLabel();
   }

   /**
    * Override this to add information to the status string. eg. extra filters etc.
    * 
    * @return string to add
    */
   public String getStatusString() {
      return "";
   }

   public void getStatusLine1(StringBuffer sb) {
      int loadedNum = 0;
      if (getRoot() != null && ((ITreeContentProvider) getContentProvider()) != null) {
         loadedNum = ((ITreeContentProvider) getContentProvider()).getChildren(getRoot()).length;
      }
      sb.append(" " + loadedNum + " Loaded - " + getVisibleItemCount(getTree().getItems()) + " Shown - " + ((IStructuredSelection) getSelection()).size() + " Selected - ");
      customizeMgr.appendToStatusLabel(sb);
      if (filterDataUI != null) {
         filterDataUI.appendToStatusLabel(sb);
      }
      columnFilterDataUI.appendToStatusLabel(sb);
      sb.append(getStatusString());
   }

   public void getStatusLine2(StringBuffer sb) {
      customizeMgr.getSortingStr(sb);
   }

   public void updateStatusLabel() {
      if (!xViewerFactory.isLoadedStatusLabelAvailable()) return;
      if (getTree().isDisposed() || statusLabel.isDisposed()) return;
      StringBuffer sb = new StringBuffer();
      getStatusLine1(sb);
      if (sb.length() > 0) {
         sb.append("\n");
      }
      getStatusLine2(sb);
      String str = sb.toString();
      statusLabel.setText(str);
      statusLabel.getParent().getParent().layout();
      statusLabel.setToolTipText(str);
   }

   public String getViewerNamespace() {
      return getXViewerFactory().getNamespace();
   }

   public IXViewerFactory getXViewerFactory() {
      return xViewerFactory;
   }

   public Label getStatusLabel() {
      return statusLabel;
   }

   /**
    * @return the textFilterComp
    */
   public FilterDataUI getFilterDataUI() {
      return filterDataUI;
   }

   public boolean isColumnMultiEditEnabled() {
      return columnMultiEditEnabled;
   }

   public void setColumnMultiEditEnabled(boolean columnMultiEditEnabled) {
      this.columnMultiEditEnabled = columnMultiEditEnabled;
   }

   public void setEnabled(boolean arg) {
      this.getControl().setEnabled(arg);
   }

   public TreeColumn getRightClickSelectedColumn() {
      return rightClickSelectedColumn;
   }

   public TreeItem getRightClickSelectedItem() {
      return rightClickSelectedItem;
   }

   public Integer getRightClickSelectedColumnNum() {
      return rightClickSelectedColumnNum;
   }

   public CustomizeManager getCustomizeMgr() {
      return customizeMgr;
   }

   public boolean isCtrlKeyDown() {
      return ctrlKeyDown;
   }

   public boolean isAltKeyDown() {
      return altKeyDown;
   }

   boolean searchMatch(String text) {
      if (searchDataUI == null) return false;
      return searchDataUI.match(text);
   }

   Color getSearchMatchColor() {
      return searchColor;
   }

   public boolean isSearch() {
      if (searchDataUI == null) return false;
      return searchDataUI.isSearch();
   }

   public String getColumnText(Object element, int col) {
      String returnVal = "";
      if (getLabelProvider() instanceof XViewerLabelProvider) {
         returnVal = ((XViewerLabelProvider) getLabelProvider()).getColumnText(element, col);
      } else {
         returnVal = ((XViewerStyledTextLabelProvider) getLabelProvider()).getStyledText(element, col).getString();
      }
      return returnVal;
   }

   /**
    * Mouse clicks can happend in table via XViewerMouseListener or in menu area via XViewerMenuDetectListener. Both are
    * processed here to use in UI
    * 
    * @param point
    */
   public void processRightClickMouseEvent(Point point) {
      rightClickSelectedColumn = null;
      rightClickSelectedColumnNum = null;
      rightClickSelectedItem = null;

      rightClickSelectedItem = getItemUnderMouseClick(point);
      rightClickSelectedColumn = getColumnUnderMouseClick(point);
      rightClickSelectedColumnNum = getColumnNumberUnderMouseClick(point);
   }

   public TreeColumn getColumnUnderMouseClick(Point point) {
      int columnNumber = getColumnNumberUnderMouseClick(point);
      TreeColumn column = getTree().getColumn(columnNumber);
      return column;
   }

   public int getColumnNumberUnderMouseClick(Point point) {
      int[] columnOrder = getTree().getColumnOrder();
      int sum = 0;
      int columnCount = 0;
      for (int column : columnOrder) {
         TreeColumn col = getTree().getColumn(column);
         sum = sum + col.getWidth();
         if (sum > point.x) {
            break;
         }
         columnCount++;
      }

      int columnNumber = columnOrder[columnCount];
      return columnNumber;
   }

   public TreeItem getItemUnderMouseClick(Point point) {
      TreeItem itemToReturn = getTree().getItem(point);

      if (itemToReturn == null) {
         int sum;
         sum = 0;
         TreeItem[] allItems = getTree().getItems();
         for (TreeItem item : allItems) {
            sum = sum + getTree().getItemHeight();
            if (sum > point.y) {
               itemToReturn = item;
               break;
            }
         }
      }
      return itemToReturn;
   }

   /**
    * Override to provide extended filter capabilities
    */
   public XViewerTextFilter getXViewerTextFilter() {
      return new XViewerTextFilter(this);
   }

   public XViewerGradient getxViewerGradient() {
      return xViewerGradient;
   }

   public void setxViewerGradient(XViewerGradient xViewerGradient) {
      this.xViewerGradient = xViewerGradient;
   }
}
