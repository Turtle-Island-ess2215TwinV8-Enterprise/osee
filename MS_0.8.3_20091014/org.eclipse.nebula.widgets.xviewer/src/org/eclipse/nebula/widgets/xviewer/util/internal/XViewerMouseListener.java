/*
 * Created on Aug 10, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.nebula.widgets.xviewer.util.internal;

import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class XViewerMouseListener implements MouseListener {

   private final XViewer xViewer;

   public XViewerMouseListener(XViewer xViewer) {
      this.xViewer = xViewer;
   }

   @Override
   public void mouseDoubleClick(MouseEvent event) {
      Point point = new Point(event.x, event.y);
      TreeColumn column = xViewer.getColumnUnderMouseClick(point);
      TreeItem itemToReturn = xViewer.getItemUnderMouseClick(point);

      xViewer.handleDoubleClick(column, itemToReturn);
   }

   @Override
   public void mouseDown(MouseEvent event) {
      if (isRightClick(event)) {
         xViewer.processRightClickMouseEvent(new Point(event.x, event.y));
      }
   }

   private boolean isRightClick(MouseEvent event) {
      return event.button == 3;
   }

   @Override
   public void mouseUp(MouseEvent event) {
      TreeItem item = xViewer.getItemUnderMouseClick(new Point(event.x, event.y));
      if (item == null) return;

      TreeColumn column = xViewer.getColumnUnderMouseClick(new Point(event.x, event.y));

      if (isLeftClick(event) && controlNotBeingHeld(event)) {
         if (altIsBeingHeld(event)) {
            // System.out.println("Column " + colNum);
            xViewer.handleAltLeftClick(column, item);
         } else if (clickOccurredInIconArea(event, item)) {
            xViewer.handleLeftClickInIconArea(column, item);
         } else {
            // System.out.println("Column " + colNum);
            xViewer.handleLeftClick(column, item);
         }
      }
      xViewer.updateStatusLabel();

   }

   private boolean clickOccurredInIconArea(MouseEvent event, TreeItem item) {
      int columnNumber = xViewer.getColumnNumberUnderMouseClick(new Point(event.x, event.y));
      Rectangle rect = item.getBounds(columnNumber);
      return (event.x <= (rect.x + 18));
   }

   private boolean isLeftClick(MouseEvent event) {
      return event.button == 1;
   }

   private boolean altIsBeingHeld(MouseEvent event) {
      return ((event.stateMask & SWT.MODIFIER_MASK) == SWT.ALT);
   }

   private boolean controlNotBeingHeld(MouseEvent event) {
      return !((event.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL);
   }

}
