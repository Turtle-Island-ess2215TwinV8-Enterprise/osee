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
package org.eclipse.osee.framework.ui.skynet.widgets.xchange;

import java.sql.SQLException;
import java.util.ArrayList;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeType;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerSorter;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.SkynetXViewerFactory;

/**
 * @author Donald G. Dunne
 */
public class ChangeXViewerFactory extends SkynetXViewerFactory {

   /**
    * 
    */
   public ChangeXViewerFactory() {
   }

   public XViewerSorter createNewXSorter(XViewer xViewer) {
      return new XViewerSorter(xViewer);
   }

   public CustomizeData getDefaultTableCustomizeData(XViewer xViewer) {
      CustomizeData custData = new CustomizeData();
      int x = 0;
      ArrayList<XViewerColumn> cols = new ArrayList<XViewerColumn>();
      for (ChangeColumn atsXCol : ChangeColumn.values()) {
         XViewerColumn newCol = atsXCol.getXViewerColumn(atsXCol);
         newCol.setOrderNum(x++);
         newCol.setTreeViewer(xViewer);
         cols.add(newCol);
      }
      try {
         for (AttributeType attributeType : AttributeTypeManager.getTypes(getBranch(xViewer))) {
            System.out.println("Attribute " + attributeType);
         }
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, false);
      }
      custData.getColumnData().setColumns(cols);
      return custData;
   }

   private Branch getBranch(XViewer xViewer) throws OseeCoreException, SQLException {
      Branch branch = ((ChangeXViewer) xViewer).getXChangeViewer().getBranch();
      if (branch == null) {
         TransactionId transId = ((ChangeXViewer) xViewer).getXChangeViewer().getTransactionId();
         if (transId != null) return transId.getBranch();
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see osee.skynet.gui.widgets.xviewer.IXViewerFactory#getDefaultXViewerColumn()
    */
   public XViewerColumn getDefaultXViewerColumn(String name) {
      for (ChangeColumn atsXCol : ChangeColumn.values()) {
         if (atsXCol.getName().equals(name)) {
            return atsXCol.getXViewerColumn(atsXCol);
         }
      }
      return null;
   }

}
