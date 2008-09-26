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

package org.eclipse.osee.framework.ui.skynet.widgets.xviewer;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.osee.framework.skynet.core.exception.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public abstract class XViewerLabelProvider implements ITableLabelProvider, ITableColorProvider {


private final XViewer viewer;

   // Store index of columnIndex to XViewerColumns to speed up label providing
   private Map<Integer, XViewerColumn> indexToXViewerColumnMap = new HashMap<Integer, XViewerColumn>();

   private XViewerColumn getTreeColumnOffIndex(int columnIndex) {
      if (!indexToXViewerColumnMap.containsKey(columnIndex)) {
         XViewerColumn xViewerColumn = viewer.getXTreeColumn(columnIndex);
         if (xViewerColumn != null) {
            indexToXViewerColumnMap.put(columnIndex, xViewerColumn);
         }
      }
      return indexToXViewerColumnMap.get(columnIndex);
   }

   // When columns get re-ordered, need to clear out this cache so indexing can be re-computed
   public void clearXViewerColumnIndexCache() {
      indexToXViewerColumnMap.clear();
   }

   /**
    * @param viewer
    */
   public XViewerLabelProvider(final XViewer viewer) {
      super();
      this.viewer = viewer;
   }

   public Image getColumnImage(Object element, int columnIndex) {
      try {
         XViewerColumn xViewerColumn = getTreeColumnOffIndex(columnIndex);
         // If not shown, don't process any further
         if (!xViewerColumn.isShow()) return null;
         if (xViewerColumn != null) {
            if (xViewerColumn instanceof XViewerValueColumn) {
               Image image = ((XViewerValueColumn) xViewerColumn).getColumnImage(element, xViewerColumn, columnIndex);
               if (image != null) return image;
            }
            return getColumnImage(element, xViewerColumn, columnIndex);
         }
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, false);
      }
      return null;
   }

   public String getColumnText(Object element, int columnIndex) {
      try {
         XViewerColumn xViewerColumn = getTreeColumnOffIndex(columnIndex);
         // If not shown, don't process any further
         if (!xViewerColumn.isShow()) return "";
         if (xViewerColumn instanceof XViewerValueColumn) {
            return ((XViewerValueColumn) xViewerColumn).getColumnText(element, xViewerColumn, columnIndex);
         }
         return getColumnText(element, xViewerColumn, columnIndex);
      } catch (Exception ex) {
         return XViewerCells.getCellExceptionString(ex);
      }
   }

   @Override
   public Color getBackground(Object element, int columnIndex) {
      try {
         XViewerColumn xViewerColumn = getTreeColumnOffIndex(columnIndex);
         // If not shown, don't process any further
         if (!xViewerColumn.isShow()) return null;
         if (xViewerColumn instanceof XViewerValueColumn) {
            return ((XViewerValueColumn) xViewerColumn).getBackground(element, xViewerColumn, columnIndex);
         } else {
            return getBackground(element, xViewerColumn, columnIndex);
         }
      } catch (Exception ex) {
         // do nothing
      }
      return null;
   }

   @Override
   public Color getForeground(Object element, int columnIndex) {
      try {
         XViewerColumn xViewerColumn = getTreeColumnOffIndex(columnIndex);
         // If not shown, don't process any further
         if (!xViewerColumn.isShow()) return null;
         if (xViewerColumn instanceof XViewerValueColumn) {
            return ((XViewerValueColumn) xViewerColumn).getForeground(element, xViewerColumn, columnIndex);
         } else {
            return getForeground(element, xViewerColumn, columnIndex);
         }
      } catch (Exception ex) {
         // do nothing
      }
      return null;
   }

   public Color getBackground(Object element, XViewerColumn xCol, int columnIndex) {
      return null;
   }

   public Color getForeground(Object element, XViewerColumn xCol, int columnIndex) {
      return null;
   }

   public abstract Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException, SQLException;

   public abstract String getColumnText(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException, SQLException;
}
