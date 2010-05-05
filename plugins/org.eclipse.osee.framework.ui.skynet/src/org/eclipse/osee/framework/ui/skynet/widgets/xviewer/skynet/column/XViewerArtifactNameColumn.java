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
package org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.column;

import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.util.XViewerException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class XViewerArtifactNameColumn extends XViewerValueColumn {

   public XViewerArtifactNameColumn(String name) {
      this("framework.artifact.name." + name, name, 150, SWT.LEFT, true, SortDataType.String, false, null);
   }

   public XViewerArtifactNameColumn(String name, boolean multiColumnEditable) {
      this("framework.artifact.name." + name, name, 150, SWT.LEFT, true, SortDataType.String, multiColumnEditable, null);
   }

   public XViewerArtifactNameColumn(String id, String name, int width, int align, boolean show, SortDataType sortDataType, boolean multiColumnEditable, String description) {
      super(id, name, width, align, show, sortDataType, multiColumnEditable, description);
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    * 
    * @param col
    */
   @Override
   public XViewerArtifactNameColumn copy() {
      return new XViewerArtifactNameColumn(getId(), getName(), getWidth(), getAlign(), isShow(), getSortDataType(),
            isMultiColumnEditable(), getDescription());
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) throws XViewerException {
      if (element instanceof Artifact) {
         return ((Artifact) element).getName();
      } else if (element instanceof String) {
         return "";
      }
      return super.getColumnText(element, column, columnIndex);
   }

}
