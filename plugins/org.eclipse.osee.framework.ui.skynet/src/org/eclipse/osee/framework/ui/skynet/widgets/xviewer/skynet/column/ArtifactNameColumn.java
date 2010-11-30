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
public class ArtifactNameColumn extends XViewerValueColumn {

   public ArtifactNameColumn(boolean show) {
      this("framework.artifact.name", "Name", 150, SWT.LEFT, show, SortDataType.String, false, null);
   }

   public ArtifactNameColumn(String id, String name, int width, int align, boolean show, SortDataType sortDataType, boolean multiColumnEditable, String description) {
      super(id, name, width, align, show, sortDataType, multiColumnEditable, description);
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public ArtifactNameColumn copy() {
      ArtifactNameColumn newXCol = new ArtifactNameColumn(isShow());
      copy(this, newXCol);
      return newXCol;
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