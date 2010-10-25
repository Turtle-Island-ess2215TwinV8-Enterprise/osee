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
package org.eclipse.osee.ats.util.xviewer.column;

import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.util.XViewerException;
import org.eclipse.osee.ats.world.IWorldViewArtifact;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class XViewerSmaCreatedDateColumn extends XViewerValueColumn {

   public XViewerSmaCreatedDateColumn() {
      this("Created Date");
   }

   public XViewerSmaCreatedDateColumn(String name) {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".createdDate", name, 80, SWT.LEFT, true, SortDataType.Date, false,
         "Date this workflow was created.");
   }

   public XViewerSmaCreatedDateColumn(String id, String name, int width, int align, boolean show, SortDataType sortDataType, boolean multiColumnEditable, String description) {
      super(id, name, width, align, show, sortDataType, multiColumnEditable, description);
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn need to extend this constructor to copy extra stored fields
    */
   @Override
   public XViewerSmaCreatedDateColumn copy() {
      return new XViewerSmaCreatedDateColumn(getId(), getName(), getWidth(), getAlign(), isShow(), getSortDataType(),
         isMultiColumnEditable(), getDescription());
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) throws XViewerException {
      try {
         if (element instanceof IWorldViewArtifact) {
            return ((IWorldViewArtifact) element).getWorldViewCreatedDateStr();
         }
         return super.getColumnText(element, column, columnIndex);
      } catch (OseeCoreException ex) {
         throw new XViewerException(ex);
      }
   }

}