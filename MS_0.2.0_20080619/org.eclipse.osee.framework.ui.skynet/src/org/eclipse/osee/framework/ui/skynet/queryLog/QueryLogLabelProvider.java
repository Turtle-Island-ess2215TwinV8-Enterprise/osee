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
package org.eclipse.osee.framework.ui.skynet.queryLog;

import static org.eclipse.osee.framework.ui.skynet.queryLog.QueryLogView.DURATION;
import static org.eclipse.osee.framework.ui.skynet.queryLog.QueryLogView.ITEM;
import static org.eclipse.osee.framework.ui.skynet.queryLog.QueryLogView.TIME;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.osee.framework.db.connection.core.query.QueryRecord;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author Robert A. Fisher
 */
public class QueryLogLabelProvider extends XViewerLabelProvider {
   private static final DateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm:ss.SSS a");
   private final ISharedImages sharedImages;

   /**
    * @param viewer
    */
   public QueryLogLabelProvider(XViewer viewer) {
      super(viewer);
      sharedImages = PlatformUI.getWorkbench().getSharedImages();
   }

   protected Image getColumnImage(Object element, XViewerColumn column) {

      if (element instanceof QueryRecord && column.getColumnNum() == 0) {
         if (((QueryRecord) element).getSqlException() != null) {
            return sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
         }
      } else if (element instanceof Exception && column.getColumnNum() == 0) {
         return sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
      } else if (element instanceof String && column.getColumnNum() == 0) {
         return sharedImages.getImage(ISharedImages.IMG_OBJ_ELEMENT);
      }
      return null;
   }

   protected String getColumnText(Object element, XViewerColumn column) {
      String columnName = column.getDisplayName();

      if (element instanceof QueryRecord) {
         QueryRecord record = (QueryRecord) element;

         if (columnName.equals(ITEM)) {
            return record.getSql();
         } else if (columnName.equals(TIME)) {
            return TIME_FORMAT.format(record.getDate());
         } else if (columnName.equals(DURATION)) {
            if (record.getRunDurationMs() != null) {
               return record.getRunDurationMs().toString();
            }
         }
      } else if (columnName.equals(ITEM)) {
         return element.toString();
      }
      return null;
   }

   public void addListener(ILabelProviderListener listener) {
   }

   public void dispose() {
   }

   public boolean isLabelProperty(Object element, String property) {
      return false;
   }

   public void removeListener(ILabelProviderListener listener) {
   }
}
