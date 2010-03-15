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
package org.eclipse.osee.ats.workflow.editor.parts;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.workflow.editor.actions.EditAction;
import org.eclipse.osee.ats.workflow.editor.model.ReturnTransitionConnection;
import org.eclipse.osee.ats.workflow.editor.model.WorkPageShape;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemAttributes;

/**
 * @author Donald G. Dunne
 */
public class WorkPageEditPart extends ShapeEditPart {

   private final WorkPageShape workPageShape;
   private RightAnchor returnAnchor;
   private Label label;

   public WorkPageEditPart(WorkPageShape workPageShape) {
      this.workPageShape = workPageShape;
   }

   @Override
   protected IFigure createFigure() {
      IFigure f = super.createFigure();
      f.setLayoutManager(new GridLayout());
      try {
         if (workPageShape.isCompletedState()) {
            f.setBackgroundColor(ColorConstants.darkGreen);
         } else if (workPageShape.isCancelledState()) {
            f.setBackgroundColor(ColorConstants.lightGray);
         } else if (workPageShape.isStartPage()) {
            f.setBackgroundColor(ColorConstants.yellow);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
      label = new Label(workPageShape.getName());
      f.add(label);
      f.setToolTip(new Label(workPageShape.getToolTip()));
      return f;
   }

   @Override
   protected void refreshVisuals() {
      super.refreshVisuals();
      label.setText(workPageShape.getName());
   }

   @Override
   public void performRequest(Request req) {
      super.performRequest(req);
      System.out.println(req);
      if (req instanceof SelectionRequest) {
         (new EditAction()).run();
      }
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      super.propertyChange(evt);
      String prop = evt.getPropertyName();
      if (WorkPageShape.START_PAGE.equals(prop)) {
         if (workPageShape.isStartPage()) {
            getFigure().setBackgroundColor(ColorConstants.yellow);
         } else {
            getFigure().setBackgroundColor(ColorConstants.green);
         }
      }
      if (WorkItemAttributes.WORK_PAGE_NAME.getAttributeTypeName().equals(prop)) {
         refreshVisuals();
      }
   }

   @Override
   public ConnectionAnchor getTargetConnectionAnchor(Request request) {
      // TODO implement sending back returnAnchor if appropriate
      return super.getTargetConnectionAnchor(request);
   }

   @Override
   public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
      if (connection.getModel() instanceof ReturnTransitionConnection) {
         if (returnAnchor == null) {
            returnAnchor = new RightAnchor(getFigure());
         }
         return returnAnchor;
      }
      return super.getTargetConnectionAnchor(connection);
   }

   @Override
   public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
      if (connection.getModel() instanceof ReturnTransitionConnection) {
         if (returnAnchor == null) {
            returnAnchor = new RightAnchor(getFigure());
         }
         return returnAnchor;
      }
      return super.getTargetConnectionAnchor(connection);
   }

   @Override
   public ConnectionAnchor getSourceConnectionAnchor(Request request) {
      // TODO implement sending back returnAnchor if appropriate
      return super.getSourceConnectionAnchor(request);
   }

}
