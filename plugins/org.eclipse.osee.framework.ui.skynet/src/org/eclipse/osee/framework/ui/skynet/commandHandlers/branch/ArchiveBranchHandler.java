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

package org.eclipse.osee.framework.ui.skynet.commandHandlers.branch;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEventType;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.Handlers;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Jeff C. Phillips
 */
public final class ArchiveBranchHandler extends AbstractBranchHandler {

   public ArchiveBranchHandler() {
      super("change archive state", "Archive/Unarchive Branch");
   }

   @Override
   public boolean isEnabledWithException(IStructuredSelection structuredSelection) {
      return !Handlers.getBranchesFromStructuredSelection(structuredSelection).isEmpty();
   }

   @Override
   public void performOperation(List<Branch> branches) throws OseeCoreException {
      Branch common = BranchManager.getCommonBranch();

      Set<Branch> systemBranches = new LinkedHashSet<Branch>();
      List<Branch> toUpdate = new ArrayList<Branch>();
      List<Branch> inProgress = new ArrayList<Branch>();
      for (Branch branch : branches) {
         if (branch.getBranchType().isSystemRootBranch() || branch.equals(common)) {
            systemBranches.add(branch);
         } else {
            BranchArchivedState state = branch.getArchiveState();
            if (state.isBeingArchived() || state.isBeingUnarchived()) {
               inProgress.add(branch);
            } else {
               branch.setArchived(!state.isArchived());
               toUpdate.add(branch);
            }
         }
      }

      if (!toUpdate.isEmpty()) {
         BranchManager.persist(toUpdate);
         for (Branch branch : toUpdate) {
            OseeEventManager.kickBranchEvent(this, new BranchEvent(BranchEventType.Committed, branch.getGuid()),
               branch.getId());
         }
      }

      if (!inProgress.isEmpty()) {
         Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
         MessageDialog.openError(
            shell,
            "Archive Branch",
            "The following branches were not modified since archive/unarchive process is currently in progress. Please try again later.\n" + inProgress);
      }

      if (!systemBranches.isEmpty()) {
         Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
         MessageDialog.openError(shell, "Archive Branch",
            "System critical branches are not archivable. The following branches were not modified:\n" + systemBranches);
      }
   }
}
