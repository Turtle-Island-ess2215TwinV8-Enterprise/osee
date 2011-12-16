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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.ui.plugin.util.CommandHandler;
import org.eclipse.osee.framework.ui.skynet.ArtifactExplorer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Roberto E. Escobar
 */
public class OpenArtifactExplorerHandler extends CommandHandler {

   @Override
   public Object executeWithException(ExecutionEvent event) throws OseeCoreException {
      List<? extends IOseeBranch> branches = getSelectedBranches();
      if (branches != null && !branches.isEmpty()) {
         Set<Branch> notExplorable = new HashSet<Branch>();
         Set<Branch> archived = new LinkedHashSet<Branch>();
         for (IOseeBranch branchToken : branches) {
            Branch branch = BranchManager.getBranch(branchToken);
            if (branch.getArchiveState().isArchived()) {
               archived.add(branch);
            } else if (branch.getBranchType().isSystemRootBranch()) {
               notExplorable.add(branch);
            } else {
               ArtifactExplorer.exploreBranch(branch);
            }
         }

         if (!archived.isEmpty() || !notExplorable.isEmpty()) {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            StringBuilder builder = new StringBuilder();
            builder.append("The following branches are not explorable:\n");
            if (!archived.isEmpty()) {
               builder.append("[");
               builder.append(org.eclipse.osee.framework.jdk.core.util.Collections.toString("],\n[", archived));
               builder.append("]\n\nNOTE: Unarchive the branch to enable exploring");
            }

            if (!notExplorable.isEmpty()) {
               builder.append("[");
               builder.append(org.eclipse.osee.framework.jdk.core.util.Collections.toString("],\n[", notExplorable));
               builder.append("]");
               builder.append("\n\nSystem branches are not explorable");
            }
            MessageDialog.openWarning(shell, "Open Artifact Explorer", builder.toString());
         }
      }
      return null;
   }

   @Override
   public boolean isEnabledWithException(IStructuredSelection structuredSelection) {
      return !Handlers.getBranchesFromStructuredSelection(structuredSelection).isEmpty();
   }

   private List<? extends IOseeBranch> getSelectedBranches() throws OseeCoreException {
      List<? extends IOseeBranch> toReturn = Collections.emptyList();
      try {
         IStructuredSelection structuredSelection = getCurrentSelection();
         toReturn = Handlers.getBranchesFromStructuredSelection(structuredSelection);
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return toReturn;
   }
}