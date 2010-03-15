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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.artifact.massEditor.MassArtifactEditor;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class OpenInMassEditorAction extends Action {

   private final ISelectedAtsArtifacts selectedAtsArtifacts;

   public OpenInMassEditorAction(ISelectedAtsArtifacts selectedAtsArtifacts) {
      this.selectedAtsArtifacts = selectedAtsArtifacts;
      setText("Open Mass Editor");
      setToolTipText(getText());
   }

   @Override
   public void run() {
      try {
         MassArtifactEditor.editArtifacts("", selectedAtsArtifacts.getSelectedSMAArtifacts());
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.ARTIFACT_EDITOR);
   }

   public void updateEnablement() {
      try {
         setEnabled(selectedAtsArtifacts.getSelectedSMAArtifacts().size() > 0);
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
         setEnabled(false);
      }
   }

}
