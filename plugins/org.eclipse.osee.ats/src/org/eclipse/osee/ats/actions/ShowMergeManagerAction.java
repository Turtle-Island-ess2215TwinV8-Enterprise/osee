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
package org.eclipse.osee.ats.actions;

import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class ShowMergeManagerAction extends Action {

   private final TeamWorkFlowArtifact teamArt;

   // Since this service is only going to be added for the Implement state, Location.AllState will
   // work
   public ShowMergeManagerAction(TeamWorkFlowArtifact teamArt) {
      this.teamArt = teamArt;
      setText("Show Merge Manager");
      setToolTipText(getText());
      try {
         setEnabled(teamArt.getBranchMgr().isWorkingBranchInWork() || teamArt.getBranchMgr().isCommittedBranchExists());
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
   }

   @Override
   public void run() {
      teamArt.getBranchMgr().showMergeManager();
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.OUTGOING_MERGED);
   }

}
