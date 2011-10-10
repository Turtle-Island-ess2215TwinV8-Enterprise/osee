/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.editor.log.column;

import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerValueColumn;
import org.eclipse.osee.ats.core.workflow.log.LogItem;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.swt.SWT;

public class LogAuthorColumn extends XViewerValueColumn {
   private static LogAuthorColumn instance = new LogAuthorColumn();

   public static LogAuthorColumn getInstance() {
      return instance;
   }

   public LogAuthorColumn() {
      super("ats.log.Author", "Author", 100, SWT.LEFT, true, SortDataType.String, false, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public LogAuthorColumn copy() {
      LogAuthorColumn newXCol = new LogAuthorColumn();
      copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      if (element instanceof LogItem) {
         try {
            return UserManager.getUser(((LogItem) element).getUser()).getName();
         } catch (OseeCoreException ex) {
            return LogUtil.getCellExceptionString(ex);
         }
      }
      return "";
   }
}