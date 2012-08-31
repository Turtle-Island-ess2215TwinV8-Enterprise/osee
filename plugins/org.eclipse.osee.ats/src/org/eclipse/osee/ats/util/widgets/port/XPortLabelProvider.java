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

   public XPortLabelProvider(PortXViewer portXViewer) {
      super(portXViewer);
      this.portXViewer = portXViewer;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException {
      if (element instanceof TeamWorkFlowArtifact) {
         TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) element;
         if (xCol.equals(PortXManagerFactory.Action_Col)) {
            PortStatus status = PortUtil.getPortStatus(portXViewer.getXPortTableWidget().getTeamArt(), teamArt);
            if (status.isError()) {
               return ImageManager.getImage(FrameworkImage.ERROR);
            }
            PortAction action = PortUtil.getPortAction(portXViewer.getXPortTableWidget().getTeamArt(), teamArt);
            if (action == PortAction.APPLY_NEXT) {
               return ImageManager.getImage(AtsImage.RIGHT_ARROW_SM);
            }
         } else if (xCol.equals(PortXManagerFactory.Remove_Col)) {
            return ImageManager.getImage(FrameworkImage.REMOVE);
         } else if (xCol.equals(PortXManagerFactory.Status_Col)) {
            PortStatus status = PortUtil.getPortStatus(portXViewer.getXPortTableWidget().getTeamArt(), teamArt);
            if (status.isError()) {
               return ImageManager.getImage(FrameworkImage.ERROR);
            }
         }
      }
      return null;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException {
      if (element instanceof TeamWorkFlowArtifact) {
         TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) element;
         if (xCol.equals(PortXManagerFactory.Title_Col)) {
            return teamArt.getName();
         } else if (xCol.equals(PortXManagerFactory.Status_Col)) {
            PortStatus portStatus = PortUtil.getPortStatus(portXViewer.getXPortTableWidget().getTeamArt(), teamArt);
            return portStatus.getDisplayName();
         } else if (xCol.equals(PortXManagerFactory.Action_Col)) {
            PortAction portAction = PortUtil.getPortAction(portXViewer.getXPortTableWidget().getTeamArt(), teamArt);
            return portAction.getDisplayName();
         } else if (xCol.equals(PortXManagerFactory.Remove_Col)) {
            return "Remove";
         } else if (xCol.equals(PortXManagerFactory.Commit_Date_Col)) {
            TransactionRecord earliestTransactionId = AtsBranchManagerCore.getEarliestTransactionId(teamArt);
            if (earliestTransactionId != null) {
               return DateUtil.get(earliestTransactionId.getTimeStamp(), DateUtil.MMDDYYHHMM);
            }
            return "";
         }
      } else if (element instanceof String) {
         if (xCol.equals(PortXManagerFactory.Title_Col)) {
            return (String) element;
         }
         return "";
      }
      return "unhandled element type " + element;
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
