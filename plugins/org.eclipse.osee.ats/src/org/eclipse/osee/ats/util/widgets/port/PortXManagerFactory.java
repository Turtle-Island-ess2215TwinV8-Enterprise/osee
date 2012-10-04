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
package org.eclipse.osee.ats.util.widgets.port;

import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn.SortDataType;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.SkynetXViewerFactory;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class PortXManagerFactory extends SkynetXViewerFactory {

   public static XViewerColumn Title_Col = new XViewerColumn("osee.port.title", "Title", 250, SWT.LEFT, true,
      SortDataType.String, false, null);
   public static XViewerColumn Commit_Date_Col = new XViewerColumn("osee.port.commitDate", "Committed Date", 180,
      SWT.LEFT, true, SortDataType.Date, false, null);
   public static XViewerColumn Action_Col = new XViewerColumn("osee.port.action", "Action", 90, SWT.LEFT, true,
      SortDataType.String, false, null);
   public static XViewerColumn Remove_Col = new XViewerColumn("osee.port.remove", "Remove", 90, SWT.LEFT, true,
      SortDataType.String, false, null);
   public static XViewerColumn Status_Col = new XViewerColumn("osee.port.status", "Status", 200, SWT.LEFT, true,
      SortDataType.String, false, null);

   public PortXManagerFactory() {
      super("osee.skynet.gui.PortXViewer");
      registerColumns(Title_Col, Commit_Date_Col, Action_Col, Remove_Col, Status_Col);
   }

   @Override
   public boolean isFilterUiAvailable() {
      return false;
   }

   @Override
   public boolean isHeaderBarAvailable() {
      return false;
   }

   @Override
   public boolean isLoadedStatusLabelAvailable() {
      return false;
   }

   @Override
   public boolean isSearchUiAvailable() {
      return false;
   }

}
