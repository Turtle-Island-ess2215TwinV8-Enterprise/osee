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
package org.eclipse.osee.ats.workdef.viewer;

import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.GENERALIZED_EDIT;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.core.workdef.WorkDefinitionMatch;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.render.DefaultArtifactRenderer;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;

/**
 * @author Donald G. Dunne
 */
public class AtsWorkDefConfigRenderer extends DefaultArtifactRenderer {

   private static final String COMMAND_ID = "org.eclipse.osee.ats.atsworkdefviewer.command";

   public Image getImage(Artifact artifact) {
      return ImageManager.getImage(AtsImage.WORKFLOW_CONFIG);
   }

   @Override
   public List<String> getCommandIds(CommandGroup commandGroup) {
      ArrayList<String> commandIds = new ArrayList<String>(1);

      if (commandGroup.isEdit()) {
         commandIds.add(COMMAND_ID);
      }

      return commandIds;
   }

   @Override
   public String getName() {
      return "ATS Work Definition Viewer";
   }

   @Override
   public AtsWorkDefConfigRenderer newInstance() {
      return new AtsWorkDefConfigRenderer();
   }

   @Override
   public int getApplicabilityRating(PresentationType presentationType, IArtifact artifact) throws OseeCoreException {
      Artifact aArtifact = artifact.getFullArtifact();
      if (aArtifact.isOfType(AtsArtifactTypes.WorkDefinition) && presentationType != GENERALIZED_EDIT) {
         return PRESENTATION_SUBTYPE_MATCH;
      }
      return NO_MATCH;
   }

   @Override
   public ImageDescriptor getCommandImageDescriptor(Command command, Artifact artifact) {
      return ImageManager.getImageDescriptor(AtsImage.WORK_DEFINITION);
   }

   @Override
   public void open(final List<Artifact> artifacts, PresentationType presentationType) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            for (Artifact artifact : artifacts) {
               try {
                  WorkDefinitionMatch match =
                     AtsClientService.get().getWorkDefinitionAdmin().getWorkDefinition(artifact.getName());
                  if (match.isMatched()) {
                     IEditorInput input = new AtsWorkDefConfigEditorInput(match.getWorkDefinition());
                     AWorkbench.getActivePage().openEditor(input, AtsWorkDefConfigEditor.EDITOR_ID);
                  } else {
                     AWorkbench.popup("No Work Definition matches artifact " + artifact.toStringWithId());
                  }
               } catch (CoreException ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         }
      });
   }
}