/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.workflow.editor.parts;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.osee.ats.workflow.editor.model.Shape;
import org.eclipse.osee.ats.workflow.editor.model.WorkflowDiagram;
import org.eclipse.osee.ats.workflow.editor.model.commands.ShapeDeleteCommand;

/**
 * This edit policy enables the removal of a Shapes instance from its container.
 * 
 * @see ShapeEditPart#createEditPolicies()
 * @see ShapeTreeEditPart#createEditPolicies()
 * @author Donald G. Dunne
 */
class ShapeComponentEditPolicy extends ComponentEditPolicy {

   @Override
   protected Command createDeleteCommand(GroupRequest deleteRequest) {
      Object parent = getHost().getParent().getModel();
      Object child = getHost().getModel();
      if (parent instanceof WorkflowDiagram && child instanceof Shape) {
         return new ShapeDeleteCommand((WorkflowDiagram) parent, (Shape) child);
      }
      return super.createDeleteCommand(deleteRequest);
   }
}