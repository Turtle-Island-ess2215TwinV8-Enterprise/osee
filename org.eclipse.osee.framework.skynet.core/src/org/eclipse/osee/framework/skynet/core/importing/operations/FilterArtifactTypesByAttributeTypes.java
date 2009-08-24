/*
 * Created on Aug 21, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.importing.operations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactType;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeType;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.attribute.TypeValidityManager;
import org.eclipse.osee.framework.skynet.core.importing.RoughArtifact;
import org.eclipse.osee.framework.skynet.core.internal.Activator;

/**
 * @author Roberto E. Escobar
 */
public class FilterArtifactTypesByAttributeTypes extends AbstractOperation {

   private final Branch branch;
   private final Collection<ArtifactType> selectedArtifactTypes;
   private final RoughArtifactCollector collector;

   public FilterArtifactTypesByAttributeTypes(Branch branch, RoughArtifactCollector collector, Collection<ArtifactType> selectedArtifactTypes) {
      super("Filter Artifact Types", Activator.PLUGIN_ID);
      this.branch = branch;
      this.selectedArtifactTypes = selectedArtifactTypes;
      this.collector = collector;
   }

   /*
    * (non-Javadoc)
    * @see
    * org.eclipse.osee.framework.core.operation.AbstractOperation#doWork(org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      Set<String> names = new HashSet<String>();
      for (RoughArtifact artifact : collector.getRoughArtifacts()) {
         names.addAll(artifact.getURIAttributes().keySet());
         names.addAll(artifact.getAttributes().keySet());
      }
      selectedArtifactTypes.clear();
      Set<AttributeType> requiredTypes = new HashSet<AttributeType>();
      for (String name : names) {
         AttributeType type = AttributeTypeManager.getType(name);
         if (type != null) {
            requiredTypes.add(type);
         }
      }
      for (ArtifactType artifactType : TypeValidityManager.getValidArtifactTypes(branch)) {
         Collection<AttributeType> attributeType =
               TypeValidityManager.getAttributeTypesFromArtifactType(artifactType, branch);
         if (Collections.setComplement(requiredTypes, attributeType).isEmpty()) {
            selectedArtifactTypes.add(artifactType);
         }
      }
   }
}