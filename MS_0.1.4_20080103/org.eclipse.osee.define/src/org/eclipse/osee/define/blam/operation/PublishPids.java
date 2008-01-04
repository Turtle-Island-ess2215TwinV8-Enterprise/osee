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
package org.eclipse.osee.define.blam.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.ui.skynet.blam.BlamVariableMap;
import org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation;
import org.eclipse.osee.framework.ui.skynet.render.IRenderer;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;

/**
 * @author Ryan D. Brooks
 */
public class PublishPids implements BlamOperation {

   private static final List<String> xWidgets =
         Arrays.asList("<XWidget xwidgetType=\"XBranchListViewer\" displayName=\"Branch\" />");

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#runOperation(org.eclipse.osee.framework.ui.skynet.blam.BlamVariableMap, org.eclipse.osee.framework.skynet.core.artifact.Branch)
    */
   public void runOperation(BlamVariableMap variableMap, IProgressMonitor monitor) throws Exception {
      Branch branch = variableMap.getBranch("Branch");
      ArtifactPersistenceManager artifactManager = ArtifactPersistenceManager.getInstance();
      RendererManager rendererManager = RendererManager.getInstance();
      Artifact root = artifactManager.getDefaultHierarchyRootArtifact(branch);
      Artifact subsysTopFolder = root.getChild("Subsystem Requirements");

      monitor.subTask("Aquiring Subsystem Requirements"); // bulk load for performance reasons
      artifactManager.getArtifactsFromSubtypeName("Subsystem Requirement", branch);

      for (Artifact subsystem : subsysTopFolder.getChildren()) {
         if (monitor.isCanceled()) {
            return;
         }
         Set<Artifact> children = subsystem.getChildren();
         IRenderer renderer = rendererManager.getRendererById("org.eclipse.osee.framework.ui.skynet.word");
         renderer.preview(new ArrayList<Artifact>(children), "Publish Pids", monitor);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#getXWidgetXml()
    */
   public List<String> getXWidgetXml() {
      return xWidgets;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#getDescriptionUsage()
    */
   public String getDescriptionUsage() {
      return "Select parameters below and click the play button at the top right.";
   }
}