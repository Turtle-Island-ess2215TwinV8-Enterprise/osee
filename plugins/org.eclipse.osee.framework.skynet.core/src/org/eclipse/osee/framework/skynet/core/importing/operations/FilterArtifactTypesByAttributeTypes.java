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
package org.eclipse.osee.framework.skynet.core.importing.operations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.model.ArtifactType;
import org.eclipse.osee.framework.core.model.AttributeType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.importing.RoughArtifact;
import org.eclipse.osee.framework.skynet.core.internal.Activator;

/**
 * @author Roberto E. Escobar
 */
public class FilterArtifactTypesByAttributeTypes extends AbstractOperation {

   private final IOseeBranch branch;
   private final Collection<ArtifactType> selectedArtifactTypes;
   private final RoughArtifactCollector collector;

   public FilterArtifactTypesByAttributeTypes(IOseeBranch branch, RoughArtifactCollector collector, Collection<ArtifactType> selectedArtifactTypes) {
      super("Filter Artifact Types", Activator.PLUGIN_ID);
      this.branch = branch;
      this.selectedArtifactTypes = selectedArtifactTypes;
      this.collector = collector;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      Set<String> names = new HashSet<String>();
      for (RoughArtifact artifact : collector.getRoughArtifacts()) {
         names.addAll(artifact.getURIAttributes().keySet());
         names.addAll(artifact.getAttributes().getKeys());
      }
      selectedArtifactTypes.clear();
      Set<AttributeType> requiredTypes = new HashSet<AttributeType>();
      for (String name : names) {
         AttributeType type = AttributeTypeManager.getType(name);
         if (type != null) {
            requiredTypes.add(type);
         }
      }
      Branch resolvedBranch = BranchManager.getBranch(branch);
      for (ArtifactType artifactType : ArtifactTypeManager.getValidArtifactTypes(resolvedBranch)) {
         if (!artifactType.isAbstract()) {
            Collection<AttributeType> attributeTypes = artifactType.getAttributeTypes(resolvedBranch);
            if (Collections.setComplement(requiredTypes, attributeTypes).isEmpty()) {
               selectedArtifactTypes.add(artifactType);
            }
         }
      }
   }
}