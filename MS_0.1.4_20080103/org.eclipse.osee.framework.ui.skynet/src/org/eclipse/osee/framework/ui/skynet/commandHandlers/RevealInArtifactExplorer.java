/*
 * Created on Dec 28, 2007
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.commandHandlers;

import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.ArtifactExplorer;
import org.eclipse.ui.PlatformUI;

/**
 * @author Jeff C. Phillips
 */
public class RevealInArtifactExplorer extends AbstractSelectionChangedHandler {
   private static final BranchPersistenceManager branchPersistenceManager = BranchPersistenceManager.getInstance();
   private Artifact artifact;

   /* (non-Javadoc)
    * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
    */
   @Override
   public Object execute(ExecutionEvent arg0) throws ExecutionException {
      ArtifactExplorer.revealArtifact(artifact);
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
    */
   @Override
   public boolean isEnabled() {
      boolean isEnabled = false;

      if (PlatformUI.getWorkbench().isClosing()) {
         return false;
      }

      ISelectionProvider selectionProvider =
            AWorkbench.getActivePage().getActivePart().getSite().getSelectionProvider();

      if (selectionProvider != null) {
         IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
         List<Artifact> artifacts = Handlers.getArtifactsFromStructuredSelection(structuredSelection);

         if (artifacts.isEmpty()) {
            return false;
         }

         artifact = artifacts.iterator().next();
         isEnabled = artifact.getBranch() == branchPersistenceManager.getDefaultBranch();
      }
      return isEnabled;
   }
}
