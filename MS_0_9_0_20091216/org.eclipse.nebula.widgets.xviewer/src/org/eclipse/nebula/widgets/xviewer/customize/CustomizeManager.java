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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.nebula.widgets.xviewer.Activator;
import org.eclipse.nebula.widgets.xviewer.IXViewerFactory;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerComputedColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.nebula.widgets.xviewer.XViewerSorter;
import org.eclipse.nebula.widgets.xviewer.XViewerTextFilter;
import org.eclipse.nebula.widgets.xviewer.customize.dialog.XViewerCustomizeDialog;
import org.eclipse.nebula.widgets.xviewer.util.XViewerException;
import org.eclipse.nebula.widgets.xviewer.util.internal.XViewerLib;
import org.eclipse.nebula.widgets.xviewer.util.internal.XViewerLog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * This manages the default table column definitions versus the user modified column data, sorter and filters.
 * 
 * @author Donald G. Dunne
 */
public class CustomizeManager {

   private final IXViewerFactory xViewerFactory;
   private final XViewer xViewer;
   private XViewerTextFilter xViewerTextFilter;
   private CustomizeData currentCustData;
   public static String CURRENT_LABEL = "-- Current Table View --";
   public static String TABLE_DEFAULT_LABEL = "-- Table Default --";
   // Added to keep filter, sorter from working till finished loading
   public boolean loading = true;
   public static List<String> REMOVED_COLUMNS_TO_IGNORE = Arrays.asList("Metrics from Tasks");

   public CustomizeManager(XViewer xViewer, IXViewerFactory xViewerFactory) throws XViewerException {
      this.xViewer = xViewer;
      this.xViewerFactory = xViewerFactory;
      // Set customize to be user default, if selected, or table default
      CustomizeData userCustData = xViewerFactory.getXViewerCustomizations().getUserDefaultCustData();
      if (userCustData != null) {
         currentCustData = resolveLoadedCustomizeData(userCustData);
      } else {
         currentCustData = getTableDefaultCustData();
         currentCustData.setNameSpace(xViewerFactory.getNamespace());
      }
      xViewerFactory.getXViewerCustomMenu().init(xViewer);
   }

   private final Map<String, XViewerColumn> oldNameToColumnId = new HashMap<String, XViewerColumn>();

   /**
    * Since saved customize data is stored as xml, all the columns need to be resolved to the columns available from the
    * factory
    * 
    * @param loadedCustData
    * @return CustomizeData
    */
   public CustomizeData resolveLoadedCustomizeData(CustomizeData loadedCustData) {
      // Otherwise, have to resolve what was saved with what is valid for this table and available from the factory
      CustomizeData resolvedCustData = new CustomizeData();
      resolvedCustData.setName(loadedCustData.getName());
      resolvedCustData.setPersonal(loadedCustData.isPersonal());
      resolvedCustData.setGuid(loadedCustData.getGuid());
      resolvedCustData.setNameSpace(loadedCustData.getNameSpace());
      /* 
       * Need to resolve columns with what factory has which gets correct class/subclass of XViewerColumn and allows for removal of old and addition of new columns
       */
      List<XViewerColumn> resolvedColumns = new ArrayList<XViewerColumn>();
      for (XViewerColumn storedCol : loadedCustData.getColumnData().getColumns()) {
         XViewerColumn resolvedCol = xViewer.getXViewerFactory().getDefaultXViewerColumn(storedCol.getId());

         // Handle known stored values
         if (resolvedCol == null) {
            String name = storedCol.getName();
            if (name.equals("Impacted Items")) {
               resolvedCol = xViewer.getXViewerFactory().getDefaultXViewerColumn("ats.column.actionableItems");
            } else if (name.equals("State Percent")) {
               resolvedCol = xViewer.getXViewerFactory().getDefaultXViewerColumn("ats.column.statePercentComplete");
            }
         }
         // if not found, may have been stored without namespace; try to resolve for backward compatibility
         if (resolvedCol == null) {
            String name = storedCol.getName().replaceAll(" ", "");
            resolvedCol = oldNameToColumnId.get(name);
            // First try to match by .<oldname>
            if (resolvedCol == null) {
               for (XViewerColumn xCol : xViewer.getXViewerFactory().getDefaultTableCustomizeData().getColumnData().getColumns()) {
                  String colId = xCol.getId().toLowerCase();
                  String oldName = "." + name.toLowerCase();
                  if (colId.endsWith(oldName)) {
                     resolvedCol = xCol;
                     oldNameToColumnId.put(name, resolvedCol);
                     oldNameToColumnId.put(storedCol.getName(), resolvedCol);
                     break;
                  }
               }
            }
            // Then try to match by id endswith name 
            if (resolvedCol == null) {
               for (XViewerColumn xCol : xViewer.getXViewerFactory().getDefaultTableCustomizeData().getColumnData().getColumns()) {
                  if (xCol.getId().endsWith(name)) {
                     resolvedCol = xCol;
                     oldNameToColumnId.put(name, resolvedCol);
                     oldNameToColumnId.put(storedCol.getName(), resolvedCol);
                     break;
                  }
               }
            }
         }

         // Resolve computed columns
         if (resolvedCol == null) {
            for (XViewerComputedColumn xViewerComputedCol : xViewer.getComputedColumns()) {
               if (xViewerComputedCol.isApplicableFor(storedCol.getId())) {
                  resolvedCol = xViewerComputedCol.createFromStored(storedCol);
               }
            }
         }

         // Only handle columns that the factory supports and only resolve shown columns (rest will be loaded later)
         if (resolvedCol != null && resolvedCol.getWidth() > 0) {
            resolvedCol.setWidth(storedCol.getWidth());
            resolvedCol.setName(storedCol.getName());
            resolvedCol.setShow(storedCol.isShow());
            resolvedCol.setSortForward(storedCol.isSortForward());
            resolvedColumns.add(resolvedCol);
         }
         if (resolvedCol == null) {
            // Ignore known removed columns
            if (!REMOVED_COLUMNS_TO_IGNORE.contains(storedCol.getName())) {
               XViewerLog.log(
                     Activator.class,
                     Level.WARNING,
                     "XViewer Conversion for saved Customization \"" + loadedCustData.getName() + "\" dropped unresolved column Name: \"" + storedCol.getName() + "\"  Id: \"" + storedCol.getId() + "\".  Delete customization and re-save to resolve.");
            }
         }
      }
      /*
       * Add extra columns that were added to the table since storage of this custData
       */
      for (XViewerColumn extraCol : xViewer.getXViewerFactory().getDefaultTableCustomizeData().getColumnData().getColumns()) {
         if (!resolvedColumns.contains(extraCol)) {
            // Since column wasn't saved, don't show it
            extraCol.setShow(false);
            resolvedColumns.add(extraCol);
         }
      }
      /*
       * Resolve computed columns, again, to enable source column to get set
       */
      for (XViewerColumn resolveCol : resolvedColumns) {
         if (resolveCol instanceof XViewerComputedColumn) {
            ((XViewerComputedColumn) resolveCol).setSourceXViewerColumnFromColumns(resolvedColumns);
         }
      }
      resolvedCustData.getColumnData().setColumns(resolvedColumns);
      resolvedCustData.getColumnFilterData().setFromXml(loadedCustData.getColumnFilterData().getXml());
      resolvedCustData.getFilterData().setFromXml(loadedCustData.getFilterData().getXml());
      resolvedCustData.getSortingData().setFromXml(loadedCustData.getSortingData().getXml());
      return resolvedCustData;
   }

   public void setFilterText(String text) {
      currentCustData.filterData.setFilterText(text);
      try {
         xViewer.getTree().setRedraw(false);
         xViewerTextFilter.update();
         xViewer.refresh();
      } finally {
         xViewer.getTree().setRedraw(true);
      }
   }

   public String getFilterText() {
      return currentCustData.getFilterData().getFilterText();
   }

   public void setColumnFilterText(String colId, String text) {
      if (text == null || text.equals("")) {
         currentCustData.columnFilterData.removeFilterText(colId);
      } else {
         currentCustData.columnFilterData.setFilterText(colId, text);
      }
      xViewerTextFilter.update();
      xViewer.refresh();
   }

   public void clearFilters() {
      xViewer.getFilterDataUI().clear();
      currentCustData.columnFilterData.clear();
      xViewerTextFilter.update();
      xViewer.refresh();
   }

   public void clearAllColumnFilters() {
      currentCustData.columnFilterData.clear();
      xViewerTextFilter.update();
      xViewer.refresh();
   }

   public String getColumnFilterText(String colId) {
      return currentCustData.getColumnFilterData().getFilterText(colId);
   }

   public ColumnFilterData getColumnFilterData() {
      return currentCustData.getColumnFilterData();
   }

   /**
    * Clears out current columns, sorting and filtering and loads table customization
    */
   public void loadCustomization() {
      loadCustomization(currentCustData);
   }

   public void resetDefaultSorter() {
      XViewerSorter sorter = xViewer.getXViewerFactory().createNewXSorter(xViewer);
      xViewer.setSorter(sorter);
   }

   public void clearSorter() {
      currentCustData.getSortingData().clearSorter();
      xViewer.setSorter(null);
      xViewer.updateStatusLabel();
   }

   public void handleTableCustomization() {
      (new XViewerCustomizeDialog(xViewer)).open();
   }

   public void appendToStatusLabel(StringBuffer sb) {
      if (currentCustData != null && currentCustData.getName() != null &&
      //
      !currentCustData.getName().equals(CURRENT_LABEL) &&
      // 
      !currentCustData.getName().equals(TABLE_DEFAULT_LABEL) &&
      //
      currentCustData.getName() != null) {
         sb.append("[Custom: " + currentCustData.getName() + "]");
      }
   }

   /**
    * @return the currentCustData
    */
   public CustomizeData generateCustDataFromTable() {
      CustomizeData custData = new CustomizeData();
      custData.setName(CustomizeManager.CURRENT_LABEL);
      custData.setNameSpace(xViewer.getXViewerFactory().getNamespace());
      List<XViewerColumn> columns = new ArrayList<XViewerColumn>(15);
      for (Integer index : xViewer.getTree().getColumnOrder()) {
         TreeColumn treeCol = xViewer.getTree().getColumn(index);
         XViewerColumn xCol = (XViewerColumn) treeCol.getData();
         xCol.setWidth(treeCol.getWidth());
         xCol.setShow(treeCol.getWidth() > 0);
         columns.add(xCol);
      }
      // Add all columns that are not visible
      for (XViewerColumn xCol : xViewer.getXViewerFactory().getDefaultTableCustomizeData().getColumnData().getColumns()) {
         if (!columns.contains(xCol)) {
            xCol.setShow(false);
            columns.add(xCol);
         }
      }
      custData.columnData.setColumns(columns);
      custData.sortingData.setFromXml(currentCustData.sortingData.getXml());
      custData.filterData.setFromXml(currentCustData.filterData.getXml());
      custData.columnFilterData.setFromXml(currentCustData.columnFilterData.getXml());
      return custData;
   }

   public List<XViewerColumn> getCurrentTableColumns() {
      return currentCustData.getColumnData().getColumns();
   }

   public XViewerColumn getCurrentTableColumn(String id) {
      return currentCustData.getColumnData().getXColumn(id);
   }

   public List<XViewerColumn> getCurrentTableColumnsInOrder() {
      List<XViewerColumn> columns = new ArrayList<XViewerColumn>(15);
      for (Integer index : xViewer.getTree().getColumnOrder()) {
         TreeColumn treeCol = xViewer.getTree().getColumn(index);
         XViewerColumn xCol = (XViewerColumn) treeCol.getData();
         columns.add(xCol);
      }
      return columns;
   }

   public List<XViewerColumn> getCurrentVisibleTableColumns() {
      List<XViewerColumn> columns = new ArrayList<XViewerColumn>(15);
      for (XViewerColumn xCol : getCurrentTableColumns()) {
         if (xCol.isShow()) {
            columns.add(xCol);
         }
      }
      return columns;
   }

   public List<XViewerColumn> getCurrentHiddenTableColumns() {
      List<XViewerColumn> columns = new ArrayList<XViewerColumn>(15);
      for (XViewerColumn xCol : getCurrentTableColumns()) {
         if (!xCol.isShow()) {
            columns.add(xCol);
         }
      }
      return columns;
   }

   /**
    * Return index of XColumn to original column index on creation of table. Since table allows drag re-ordering of
    * columns, this index will provide the map back to the original column index. Used for label providers
    * getColumnText(object, index)
    * 
    * @return index
    */
   public Map<XViewerColumn, Integer> getCurrentTableColumnsIndex() {
      int[] index = xViewer.getTree().getColumnOrder();
      Map<XViewerColumn, Integer> xColToColumnIndex = new HashMap<XViewerColumn, Integer>(index.length);
      for (int x = 0; x < index.length; x++) {
         TreeColumn treeCol = xViewer.getTree().getColumn(index[x]);
         XViewerColumn xCol = (XViewerColumn) treeCol.getData();
         xColToColumnIndex.put(xCol, index[x]);
      }
      return xColToColumnIndex;
   }

   public int getColumnNumFromXViewerColumn(XViewerColumn xCol) {
      for (Integer index : xViewer.getTree().getColumnOrder()) {
         TreeColumn treeCol = xViewer.getTree().getColumn(index);
         XViewerColumn treeXCol = (XViewerColumn) treeCol.getData();
         if (xCol.equals(treeXCol)) return index;
      }
      return 0;
   }

   /**
    * @return the defaultCustData
    */
   public CustomizeData getTableDefaultCustData() {
      CustomizeData custData = xViewer.getXViewerFactory().getDefaultTableCustomizeData();
      if (custData.getName() == null || custData.getName().equals("")) {
         custData.setName(TABLE_DEFAULT_LABEL);
      }
      custData.setNameSpace(xViewer.getViewerNamespace());
      return custData;
   }

   public void getSortingStr(StringBuffer sb) {
      if (currentCustData.getSortingData().isSorting()) {
         List<XViewerColumn> cols = getSortXCols();
         if (cols.size() == 0) return;
         sb.append("Sort: ");
         for (XViewerColumn col : getSortXCols()) {
            if (col != null) {
               sb.append("[" + col.getName());
               sb.append(col.isSortForward() ? " (FWD)] " : " (REV)] ");
            }
         }
      }
   }

   public int getDefaultWidth(String id) {
      XViewerColumn xCol = xViewerFactory.getDefaultXViewerColumn(id);
      if (xCol == null)
         return 75;
      else
         return xCol.getWidth();
   }

   public boolean isCustomizationUserDefault(CustomizeData custData) {
      return xViewerFactory.getXViewerCustomizations().isCustomizationUserDefault(custData);
   }

   public List<XViewerColumn> getSortXCols() {
      // return sort columns depending on default/customize
      return currentCustData.getSortingData().getSortXCols(oldNameToColumnId);
   }

   public boolean isLoading() {
      return loading;
   }

   public List<CustomizeData> getSavedCustDatas() throws Exception {
      List<CustomizeData> custDatas = new ArrayList<CustomizeData>();
      for (CustomizeData savedCustData : xViewerFactory.getXViewerCustomizations().getSavedCustDatas()) {
         custDatas.add(resolveLoadedCustomizeData(savedCustData));
      }
      return custDatas;
   }

   public void saveCustomization(CustomizeData custData) throws Exception {
      xViewerFactory.getXViewerCustomizations().saveCustomization(custData);
   }

   /**
    * Set to newName or clear if newName == ""
    * 
    * @param xCol
    * @param newName
    */
   public void customizeColumnName(XViewerColumn xCol, String newName) {
      if (newName == "") {
         XViewerColumn defaultXCol = xViewerFactory.getDefaultXViewerColumn(xCol.getId());
         if (defaultXCol == null) {
            XViewerLib.popup("ERROR", "Column not defined.  Can't retrieve default name.");
            return;
         }
         xCol.setName(xCol.getName());
      } else {
         xCol.setName(newName);
      }
   }

   public void setUserDefaultCustData(CustomizeData newCustData, boolean set) throws Exception {
      xViewerFactory.getXViewerCustomizations().setUserDefaultCustData(newCustData, set);
   }

   public void deleteCustomization(CustomizeData custData) throws Exception {
      xViewerFactory.getXViewerCustomizations().deleteCustomization(custData);

   }

   public boolean isSorting() {
      return currentCustData.getSortingData().isSorting();
   }

   /**
    * Clears out current columns, sorting and filtering and loads table customization
    */
   public void loadCustomization(final CustomizeData newCustData) {
      loading = true;
      if (xViewerTextFilter == null) {
         xViewerTextFilter = xViewer.getXViewerTextFilter();
         xViewer.addFilter(xViewerTextFilter);
      }
      if (xViewer.getTree().isDisposed()) return;
      currentCustData = newCustData;
      if (currentCustData.getName() == null || currentCustData.getName().equals("")) {
         currentCustData.setName(CURRENT_LABEL);
      }
      currentCustData.setNameSpace(xViewer.getViewerNamespace());
      if (currentCustData.getSortingData().isSorting()) {
         xViewer.resetDefaultSorter();
      } else {
         xViewer.setSorter(null);
      }
      if (xViewer.getFilterDataUI() != null) {
         xViewer.getFilterDataUI().update();
      }
      xViewerTextFilter.update();
      // Dispose all existing columns
      for (TreeColumn treeCol : xViewer.getTree().getColumns())
         treeCol.dispose();
      // Create new columns
      addColumns();
      xViewer.updateStatusLabel();
      if (xViewer.getLabelProvider() instanceof XViewerLabelProvider) {
         ((XViewerLabelProvider) xViewer.getLabelProvider()).clearXViewerColumnIndexCache();
      }
      loading = false;
   }

   public void addColumns() {
      for (final XViewerColumn xCol : currentCustData.getColumnData().getColumns()) {
         // Only add visible columns
         if (!xCol.isShow()) continue;
         xCol.setXViewer(xViewer);
         TreeColumn column = new TreeColumn(xViewer.getTree(), xCol.getAlign());
         column.setMoveable(true);
         column.setData(xCol);
         StringBuffer sb = new StringBuffer();
         sb.append(xCol.getName());
         if (xCol.getDescription() != null && !xCol.getDescription().equals("") && !xCol.getDescription().equals(
               xCol.getName())) {
            sb.append("\n" + xCol.getDescription());
         }
         if (xCol.getToolTip() != null && !xCol.getToolTip().equals("") && !xCol.getToolTip().equals(xCol.getName()) && !xCol.getToolTip().equals(
               xCol.getDescription())) {
            sb.append("\n" + xCol.getToolTip());
         }
         sb.append("\n" + xCol.getId());
         column.setToolTipText(sb.toString());
         column.setText(xCol.getName());
         column.setWidth(xCol.getWidth());
         column.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
               super.widgetSelected(e);
               // Add sorter if doesn't exist
               if (xViewer.getSorter() == null) {
                  resetDefaultSorter();
               }
               if (xViewer.isAltKeyDown()) {
                  xViewer.getColumnFilterDataUI().promptSetFilter(xCol.getId());
               } else if (xViewer.isCtrlKeyDown()) {
                  List<XViewerColumn> currSortCols = currentCustData.getSortingData().getSortXCols(oldNameToColumnId);
                  if (currSortCols == null) {
                     currSortCols = new ArrayList<XViewerColumn>();
                     currSortCols.add(xCol);
                  } else {
                     // If already selected this item, reverse the sort
                     if (currSortCols.contains(xCol)) {
                        for (XViewerColumn currXCol : currSortCols)
                           if (currXCol.equals(xCol)) currXCol.reverseSort();
                     } else
                        currSortCols.add(xCol);
                  }
                  currentCustData.getSortingData().setSortXCols(currSortCols);
               } else {

                  List<XViewerColumn> cols = new ArrayList<XViewerColumn>();
                  cols.add(xCol);
                  // If sorter already has this column sorted, reverse the sort
                  List<XViewerColumn> currSortCols = currentCustData.getSortingData().getSortXCols(oldNameToColumnId);
                  if (currSortCols != null && currSortCols.size() == 1 && currSortCols.iterator().next().equals(xCol)) xCol.reverseSort();
                  // Set the newly sorted column
                  currentCustData.getSortingData().setSortXCols(cols);
               }
               xViewer.refresh();
               xViewer.updateStatusLabel();
            }
         });
      }
   }
}
