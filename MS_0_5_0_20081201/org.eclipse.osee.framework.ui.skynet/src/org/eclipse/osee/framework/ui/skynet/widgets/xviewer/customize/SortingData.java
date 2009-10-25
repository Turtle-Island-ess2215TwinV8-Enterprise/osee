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
package org.eclipse.osee.framework.ui.skynet.widgets.xviewer.customize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.jdk.core.util.AXml;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;

/**
 * @author Donald G. Dunne
 */
public class SortingData {

   private static String XTREESORTER_TAG = "xSorter";
   private static String COL_NAME_TAG = "id";
   private static String OLD_COL_NAME_TAG = "name";
   private final List<String> sortingIds = new ArrayList<String>();
   private final CustomizeData custData;

   public SortingData(CustomizeData custData) {
      this.custData = custData;
   }

   public SortingData(String xml) {
      this.custData = null;
      setFromXml(xml);
   }

   public void clearSorter() {
      sortingIds.clear();
   }

   public boolean isSorting() {
      return sortingIds.size() > 0;
   }

   @Override
   public String toString() {
      return org.eclipse.osee.framework.jdk.core.util.Collections.toString(",", sortingIds);
   }

   public List<XViewerColumn> getSortXCols(Map<String, XViewerColumn> oldNameToColumnId) {
      List<XViewerColumn> cols = new ArrayList<XViewerColumn>();
      for (String id : getSortingIds()) {
         XViewerColumn xCol = custData.getColumnData().getXColumn(id);
         // For backward compatibility, try to resolve column name
         if (xCol == null) {
            XViewerColumn resolvedCol = oldNameToColumnId.get(id);
            if (resolvedCol != null) {
               xCol = custData.getColumnData().getXColumn(resolvedCol.getId());
            }
         }
         if (xCol != null) {
            cols.add(xCol);
         } else {
            // Ignore known removed columns
            if (!CustomizeManager.REMOVED_COLUMNS_TO_IGNORE.contains(id)) {
               OseeLog.log(
                     SkynetGuiPlugin.class,
                     Level.WARNING,
                     "XViewer Conversion for saved Customization \"" + custData.getName() + "\" dropped unresolved SORTING column Name/Id: \"" + id + "\".  Delete customization and re-save to resolve.");
            }
         }
      }
      return cols;
   }

   public void setSortXCols(List<XViewerColumn> sortXCols) {
      sortingIds.clear();
      for (XViewerColumn xCol : sortXCols) {
         sortingIds.add(xCol.getId());
      }
   }

   public String getXml() {
      StringBuffer sb = new StringBuffer("<" + XTREESORTER_TAG + ">");
      // NOTE: Sorting direction is stored as part of the column data
      for (String item : sortingIds)
         sb.append(AXml.addTagData(COL_NAME_TAG, item));
      sb.append("</" + XTREESORTER_TAG + ">");
      return sb.toString();
   }

   public void setFromXml(String xml) {
      // NOTE: Sorting direction is stored as part of the column data
      sortingIds.clear();
      String xmlSortStr = AXml.getTagData(xml, XTREESORTER_TAG);
      Matcher m = Pattern.compile("<" + COL_NAME_TAG + ">(.*?)</" + COL_NAME_TAG + ">").matcher(xmlSortStr);
      while (m.find()) {
         sortingIds.add(m.group(1));
      }
      Matcher mOld = Pattern.compile("<" + OLD_COL_NAME_TAG + ">(.*?)</" + OLD_COL_NAME_TAG + ">").matcher(xmlSortStr);
      while (mOld.find()) {
         sortingIds.add(mOld.group(1));
      }
   }

   /**
    * @return the sortingNames
    */
   public List<String> getSortingIds() {
      return sortingIds;
   }

   public void removeSortingName(String name) {
      this.sortingIds.remove(name);
   }

   public void addSortingName(String name) {
      if (!this.sortingIds.contains(name)) this.sortingIds.add(name);
   }

   /**
    * @param sortingNames the sortingNames to set
    */
   public void setSortingNames(String... xViewerColumnId) {
      this.sortingIds.clear();
      for (String id : xViewerColumnId) {
         this.sortingIds.add(id);
      }
   }

}
