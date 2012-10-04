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
package org.eclipse.osee.ats.util.widgets.port;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public class XPortLabelProvider extends XViewerLabelProvider {

   private final PortXViewer portXViewer;
   private final PortController controller;

   public XPortLabelProvider(PortXViewer portXViewer, PortController controller) {
      super(portXViewer);
      this.portXViewer = portXViewer;
      this.controller = controller;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException {
      Image image = null;
      if (element instanceof TeamWorkFlowArtifact) {
         TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) element;
         if (xCol.equals(PortXManagerFactory.Action_Col)) {
            PortStatus status = controller.getSourceWorkflowStatus(teamArt);
            if (status.isError()) {
               image = ImageManager.getImage(FrameworkImage.ERROR);
            } else {
               PortAction action = controller.getSourceWorkflowAction(teamArt);
               if (action == PortAction.APPLY_NEXT) {
                  image = ImageManager.getImage(AtsImage.RIGHT_ARROW_SM);
               }
            }
         } else if (xCol.equals(PortXManagerFactory.Remove_Col)) {
            image = ImageManager.getImage(FrameworkImage.REMOVE);
         } else if (xCol.equals(PortXManagerFactory.Status_Col)) {
            PortStatus status = controller.getSourceWorkflowStatus(teamArt);
            if (status.isError()) {
               image = ImageManager.getImage(FrameworkImage.ERROR);
            }
         }
      }
      return image;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException {
      String toReturn = "unhandled element type " + element;
      if (element instanceof TeamWorkFlowArtifact) {
         TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) element;
         if (xCol.equals(PortXManagerFactory.Title_Col)) {
            toReturn = teamArt.getName();
         } else if (xCol.equals(PortXManagerFactory.Status_Col)) {
            PortStatus portStatus = controller.getSourceWorkflowStatus(teamArt);
            toReturn = portStatus.getDisplayName();
         } else if (xCol.equals(PortXManagerFactory.Action_Col)) {
            PortAction portAction = controller.getSourceWorkflowAction(teamArt);
            toReturn = portAction.getDisplayName();
         } else if (xCol.equals(PortXManagerFactory.Remove_Col)) {
            toReturn = "Remove";
         } else if (xCol.equals(PortXManagerFactory.Commit_Date_Col)) {
            TransactionRecord earliestTransactionId = AtsBranchManagerCore.getEarliestTransactionId(teamArt);
            if (earliestTransactionId != null) {
               toReturn = DateUtil.get(earliestTransactionId.getTimeStamp(), DateUtil.MMDDYYHHMM);
            }
         }
      }

      if (element instanceof String) {
         if (xCol.equals(PortXManagerFactory.Title_Col)) {
            toReturn = (String) element;
         } else {
            toReturn = "";
         }
      }
      return toReturn;
   }

   @Override
   public void dispose() {
      // do nothing
   }

   @Override
   public boolean isLabelProperty(Object element, String property) {
      return false;
   }

   @Override
   public void addListener(ILabelProviderListener listener) {
      // do nothing
   }

   @Override
   public void removeListener(ILabelProviderListener listener) {
      // do nothing
   }

   public PortXViewer getTreeViewer() {
      return portXViewer;
   }

}
