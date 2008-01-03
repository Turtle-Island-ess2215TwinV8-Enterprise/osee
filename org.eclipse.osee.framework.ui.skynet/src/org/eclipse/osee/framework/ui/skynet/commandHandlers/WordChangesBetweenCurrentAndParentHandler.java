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
package org.eclipse.osee.framework.ui.skynet.commandHandlers;

import java.sql.SQLException;
import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.skynet.core.access.AccessControlManager;
import org.eclipse.osee.framework.skynet.core.access.PermissionEnum;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.WordArtifact;
import org.eclipse.osee.framework.skynet.core.revision.ArtifactChange;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.db.schemas.ChangeType;
import org.eclipse.osee.framework.ui.plugin.util.db.schemas.SkynetDatabase;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;

/**
 * @author Paul K. Waldfogel
 * @author Jeff C. Phillips
 */
public class WordChangesBetweenCurrentAndParentHandler extends AbstractSelectionChangedHandler {
   private static final ArtifactPersistenceManager myArtifactPersistenceManager =
         ArtifactPersistenceManager.getInstance();
   private static final String DIFF_ARTIFACT = "DIFF_ARTIFACT";
   private static final AccessControlManager myAccessControlManager = AccessControlManager.getInstance();
   private ArtifactChange artifactChange;

   public WordChangesBetweenCurrentAndParentHandler() {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
    */
   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
      try {
         Artifact secondArtifact =
               myArtifactPersistenceManager.getArtifactFromId(artifactChange.getArtId(),
                     artifactChange.getToTransactionId());
         RendererManager.getInstance().compareInJob(artifactChange.getConflictingModArtifact(), secondArtifact,
               DIFF_ARTIFACT);
      } catch (Exception ex) {
         OSEELog.logException(getClass(), ex, false);
      }
      return null;
   }

   @Override
   public boolean isEnabled() {
      IStructuredSelection structuredSelection =
            (IStructuredSelection) AWorkbench.getActivePage().getActivePart().getSite().getSelectionProvider().getSelection();
      List<ArtifactChange> artifactChanges = Handlers.getArtifactChangesFromStructuredSelection(structuredSelection);

      if (artifactChanges.size() == 0) {
         return false;
      }

      artifactChange = artifactChanges.get(0);
      try {
         Artifact artifact = artifactChange.getArtifact();

         boolean readPermission = myAccessControlManager.checkObjectPermission(artifact, PermissionEnum.READ);
         boolean wordArtifactSelected = artifact instanceof WordArtifact;
         boolean modifiedWordArtifactSelected =
               wordArtifactSelected && artifactChange.getModType() == SkynetDatabase.ModificationType.CHANGE;
         boolean conflictedWordArtifactSelected =
               modifiedWordArtifactSelected && artifactChange.getChangeType() == ChangeType.CONFLICTING;
         return readPermission && conflictedWordArtifactSelected;
      } catch (SQLException ex) {
         OSEELog.logException(getClass(), ex, true);
         return (false);
      }
   }
}
