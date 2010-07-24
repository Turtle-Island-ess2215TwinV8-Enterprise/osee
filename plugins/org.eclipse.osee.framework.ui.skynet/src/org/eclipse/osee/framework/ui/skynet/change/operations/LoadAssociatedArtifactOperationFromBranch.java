/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.change.operations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.IBranchProvider;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;

public class LoadAssociatedArtifactOperationFromBranch extends AbstractOperation {

   private final IBranchProvider branchProvider;

   public LoadAssociatedArtifactOperationFromBranch(IBranchProvider branchProvider) {
      super("Load Associated Artifact", SkynetGuiPlugin.PLUGIN_ID);
      this.branchProvider = branchProvider;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      branchProvider.getBranch(monitor);
      monitor.worked(calculateWork(0.80));
      BranchManager.getAssociatedArtifact(branchProvider.getBranch(null));
      monitor.worked(calculateWork(0.20));
   }
}