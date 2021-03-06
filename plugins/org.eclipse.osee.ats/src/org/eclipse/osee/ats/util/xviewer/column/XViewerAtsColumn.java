/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.util.xviewer.column;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.column.IPersistAltLeftClickProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Eventually, when all ATS columns are converted to value columns, this class should implement IAltLeftClickProvider,
 * IXViewerValueColumn. Until then, just provide IXViewerValueColumn methods needed for subclasses to not have to
 * implement each.
 * 
 * @author Donald G. Dunne
 */
public abstract class XViewerAtsColumn extends XViewerColumn {

   protected XViewerAtsColumn() {
      super();
   }

   public XViewerAtsColumn(String id, String name, int width, int align, boolean show, SortDataType sortDataType, boolean multiColumnEditable, String description) {
      super(id, name, width, align, show, sortDataType, multiColumnEditable, description);
   }

   public XViewerAtsColumn(XViewer xViewer, String xml) {
      super(xViewer, xml);
   }

   protected boolean isPersistViewer(TreeColumn treeColumn) {
      return isPersistViewer(((XViewerColumn) treeColumn.getData()).getTreeViewer());
   }

   protected boolean isPersistViewer() {
      return isPersistViewer(getXViewer());
   }

   protected boolean isPersistViewer(XViewer xViewer) {
      return xViewer != null && //
      xViewer instanceof IPersistAltLeftClickProvider //
         && ((IPersistAltLeftClickProvider) xViewer).isAltLeftClickPersist();
   }

   public Image getColumnImage(Object element, XViewerColumn column, int columnIndex) {
      return null;
   }

   public Color getBackground(Object element, XViewerColumn xCol, int columnIndex) {
      return null;
   }

   public Color getForeground(Object element, XViewerColumn xCol, int columnIndex) {
      return null;
   }

   public StyledString getStyledText(Object element, XViewerColumn viewerColumn, int columnIndex) {
      return null;
   }

   public Font getFont(Object element, XViewerColumn viewerColumn, int columnIndex) {
      return null;
   }

   protected boolean isPersistAltLeftClick() {
      XViewer xViewer = getXViewer();
      if (xViewer instanceof IPersistAltLeftClickProvider) {
         return ((IPersistAltLeftClickProvider) xViewer).isAltLeftClickPersist();
      }
      return false;
   }

   /**
    * Returns the backing data object for operations like sorting
    */
   public Object getBackingData(Object element, XViewerColumn xCol, int columnIndex) throws Exception {
      return null;
   }

}
