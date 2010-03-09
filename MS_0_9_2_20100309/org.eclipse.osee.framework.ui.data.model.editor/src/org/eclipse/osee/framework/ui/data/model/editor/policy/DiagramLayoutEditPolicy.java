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

import java.util.List;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.osee.framework.ui.data.model.editor.command.ChangeBoundsCommand;
import org.eclipse.osee.framework.ui.data.model.editor.command.CreateNodeCommand;
import org.eclipse.osee.framework.ui.data.model.editor.model.DataType;
import org.eclipse.osee.framework.ui.data.model.editor.model.NodeModel;
import org.eclipse.osee.framework.ui.data.model.editor.model.ODMDiagram;

/**
 * @author Roberto E. Escobar
 */
public class DiagramLayoutEditPolicy extends XYLayoutEditPolicy {

   protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
      Rectangle bounds = (Rectangle) constraint;
      return new ChangeBoundsCommand((NodeModel) child.getModel(), bounds.getLocation(), bounds.width);
   }

   protected EditPolicy createChildEditPolicy(EditPart child) {
      ResizableEditPolicy childPolicy = new ResizableEditPolicy();
      childPolicy.setResizeDirections(PositionConstants.EAST_WEST);
      return childPolicy;
   }

   protected Command getCreateCommand(CreateRequest request) {
      Object newObj = request.getNewObject();
      if (newObj instanceof DataType) {
         Rectangle constraint = (Rectangle) getConstraintFor(request);
         return new CreateNodeCommand((DataType) newObj, (ODMDiagram) getHost().getModel(), constraint.getLocation(),
               constraint.width);
      } else if (newObj instanceof List) {
         //List views = (List) newObj;
         //         List businessModels = (List) request.getExtendedData().get(OutlineToDiagramTransfer.TYPE_NAME);
         //         if (!(businessModels.get(businessModels.size() - 1) instanceof ProcessedMarker)) {
         //            processSelection(businessModels, views);
         //            // Once this list has been processed, we append a marker to it so that it's
         //            // not processed again
         //            businessModels.add(new ProcessedMarker());
         //         }
         //         if (views.isEmpty()) return UnexecutableCommand.INSTANCE;
         //         CompoundCommand command = new CompoundCommand("Drag from Outline");
         //         Point loc = request.getLocation().getCopy();
         //         getHostFigure().translateToRelative(loc);
         //         for (int i = 0; i < views.size(); i++) {
         //            Object view = views.get(i);
         //            if (view instanceof NamedElementView) {
         //               command.add(new CreateNodeCommand((Node) view, (Diagram) getHost().getModel(), loc.getTranslated(i * 40,
         //                     i * 40)));
         //            } else if (view instanceof LinkInfoHolder) {
         //               LinkInfoHolder info = (LinkInfoHolder) view;
         //               command.add(new TransferLinkCommand(info.link, info.src, info.target));
         //            } else if (view instanceof ShowOppositeMarker) {
         //               command.add(new ShowOppositeCommand(((ShowOppositeMarker) view).refView));
         //            }
         //         }
         //         return command;
      }
      return UnexecutableCommand.INSTANCE;
   }

   protected Command getDeleteDependantCommand(Request request) {
      return null;
   }

   protected IFigure getFeedbackLayer() {
      return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
   }
   /*
      private void processSelection(List models, List views) {
         List newModels = new ArrayList();
         List newViews = new ArrayList();
         //      for (int i = 0; i < models.size(); i++) {
         //         Object model = models.get(i);
         //         if (Utilities.findViewFor(model, getHost().getModel()) != null) continue;
         //         if (model instanceof EReference) {
         //            EReference ref = (EReference) model;
         //            ReferenceView oppView = (ReferenceView) Utilities.findViewFor(ref.getEOpposite(), getHost().getModel());
         //            int oppIndex = newModels.indexOf(ref.getEOpposite());
         //            if (oppView != null || oppIndex != -1) {
         //               // the opposite was found
         //               newModels.add(model);
         //               if (oppView != null) {
         //                  newViews.add(new ShowOppositeMarker(oppView));
         //               } else {
         //                  newViews.add(new ShowOppositeMarker((ReferenceView) ((LinkInfoHolder) newViews.get(oppIndex)).link));
         //               }
         //               continue;
         //            }
         //            Node srcView = (Node) Utilities.findViewFor(ref.getEContainingClass(), getHost().getModel());
         //            Node destView = (Node) Utilities.findViewFor(ref.getEReferenceType(), getHost().getModel());
         //            if (srcView == null) {
         //               int index = models.indexOf(ref.getEContainingClass());
         //               srcView = index >= 0 ? (Node) views.get(index) : null;
         //            }
         //            if (destView == null) {
         //               int index = models.indexOf(ref.getEReferenceType());
         //               destView = index >= 0 ? (Node) views.get(index) : null;
         //            }
         //            if (srcView != null && destView != null) {
         //               ReferenceView refView = (ReferenceView) views.get(i);
         //               refView.setEReference(ref);
         //               newModels.add(model);
         //               newViews.add(new LinkInfoHolder(refView, srcView, destView));
         //            }
         //         } else if (model instanceof InheritanceModel) {
         //            InheritanceModel link = (InheritanceModel) model;
         //            Node srcView = (Node) Utilities.findViewFor(link.getSubType(), getHost().getModel());
         //            Node destView = (Node) Utilities.findViewFor(link.getSuperType(), getHost().getModel());
         //            if (srcView == null) {
         //               int index = models.indexOf(link.getSubType());
         //               srcView = index >= 0 ? (Node) views.get(index) : null;
         //            }
         //            if (destView == null) {
         //               int index = models.indexOf(link.getSuperType());
         //               destView = index >= 0 ? (Node) views.get(index) : null;
         //            }
         //            if (srcView != null && destView != null) {
         //               newModels.add(model);
         //               newViews.add(new LinkInfoHolder((Link) views.get(i), srcView, destView));
         //            }
         //         } else if (model instanceof ENamedElement) {
         //            NamedElementView classView = (NamedElementView) views.get(i);
         //            classView.setENamedElement((ENamedElement) model);
         //            newModels.add(0, model);
         //            newViews.add(0, classView);
         //         }
         //      }
         models.clear();
         views.clear();
         models.addAll(newModels);
         views.addAll(newViews);
      }

      private static class LinkInfoHolder {
         private ConnectionModel link;
         private NodeModel src;
         private NodeModel target;

         private LinkInfoHolder(ConnectionModel link, NodeModel src, NodeModel target) {
            this.link = link;
            this.src = src;
            this.target = target;
         }
      }
   */
   //   private static class ShowOppositeMarker {
   //      private ReferenceView refView;
   //
   //      private ShowOppositeMarker(ReferenceView view) {
   //         refView = view;
   //      }
   //   }
   //
   //   private static class ProcessedMarker {
   //   }
}
