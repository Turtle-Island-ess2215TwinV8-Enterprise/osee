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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.util.internal.dialog.DialogWithEntry;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * @author Donald G. Dunne
 */
public class ColumnFilterDataUI {

   private final XViewer xViewer;

   public ColumnFilterDataUI(XViewer xViewer) {
      this.xViewer = xViewer;
   }

   public void createWidgets(Composite comp) {
   }

   public void dispose() {
   }

   public void promptSetFilter(String colId) {
      DialogWithEntry ed =
            new DialogWithEntry(Display.getCurrent().getActiveShell(), "Filter Column", null,
                  "Enter filter string for " + colId + ":\n\nNote: !string negates the match", MessageDialog.QUESTION,
                  new String[] {"OK", "Clear", "Clear All", "Cancel"}, 0);
      String str = xViewer.getCustomizeMgr().getColumnFilterData().getFilterText(colId);
      if (str != null && !str.equals("")) {
         ed.setEntry(str);
      }
      int result = ed.open();
      if (result == 0) {
         xViewer.getCustomizeMgr().setColumnFilterText(colId, ed.getEntry());
      } else if (result == 1) {
         xViewer.getCustomizeMgr().setColumnFilterText(colId, null);
      } else if (result == 2) {
         xViewer.getCustomizeMgr().clearAllColumnFilters();
      }
   }

   public void getStatusLabelAddition(StringBuffer sb) {
      for (String colId : xViewer.getCustomizeMgr().getColumnFilterData().getColIds()) {
         sb.append("[" + colId + "=" + xViewer.getCustomizeMgr().getColumnFilterData().getFilterText(colId) + "]");
      }
   }

}
