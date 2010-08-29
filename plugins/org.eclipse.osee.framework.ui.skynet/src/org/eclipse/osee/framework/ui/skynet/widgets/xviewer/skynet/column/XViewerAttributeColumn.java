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
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.conflict.Conflict;

/**
 * @author Donald G. Dunne
 */
public class XViewerAttributeColumn extends XViewerValueColumn {

   private final IAttributeType attributeType;

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn need to extend this constructor to copy extra stored fields
    * 
    */
   @Override
   public XViewerAttributeColumn copy() {
      return new XViewerAttributeColumn(getId(), getName(), getAttributeType(), getWidth(), getAlign(), isShow(),
         getSortDataType(), isMultiColumnEditable(), getDescription());
   }

   public XViewerAttributeColumn(String id, String name, IAttributeType attributeType, int width, int align, boolean show, SortDataType sortDataType, boolean multiColumnEditable, String description) {
      super(id, name, width, align, show, sortDataType, multiColumnEditable, description);
      this.attributeType = attributeType;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) throws XViewerException {
      try {
         if (element instanceof Artifact) {
            return ((Artifact) element).getAttributesToString(getAttributeType());
         }
         if (element instanceof Change) {
            return ((Change) element).getChangeArtifact().getAttributesToString(getAttributeType());
         }
         if (element instanceof Conflict) {
            return ((Conflict) element).getArtifact().getAttributesToString(getAttributeType());
         }
         return "";
      } catch (OseeCoreException ex) {
         throw new XViewerException(ex);
      }
   }

   public IAttributeType getAttributeType() {
      return attributeType;
   }
}
