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
package org.eclipse.osee.framework.ui.data.model.editor.policy;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * @author Roberto E. Escobar
 */
public class ConnectionModelEndpointEditPolicy extends ConnectionEndpointEditPolicy {

   private static Font BOLD_FONT;

   protected void addSelectionHandles() {
      super.addSelectionHandles();
      getConnectionFigure().setLineWidth(2);
   }

   protected PolylineConnection getConnectionFigure() {
      return (PolylineConnection) getHostFigure();
   }

   protected void hideSelection() {
      super.hideSelection();
      getHostFigure().setFont(null);
      getHostFigure().invalidateTree();
   }

   protected void removeSelectionHandles() {
      super.removeSelectionHandles();
      getConnectionFigure().setLineWidth(0);
   }

   protected void showSelection() {
      super.showSelection();
      if (BOLD_FONT == null) {
         FontData[] data = getConnectionFigure().getFont().getFontData();
         for (int i = 0; i < data.length; i++) {
            if ((data[i].getStyle() & SWT.BOLD) == 0) {
               data[i].setStyle(data[i].getStyle() | SWT.BOLD);
            }
         }
         BOLD_FONT = new Font(null, data);
      }
      getHostFigure().setFont(BOLD_FONT);
      getHostFigure().invalidateTree();
   }

}
