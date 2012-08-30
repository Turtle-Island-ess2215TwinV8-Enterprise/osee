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
package org.eclipse.osee.orcs.core.internal.relation.order;

import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.core.internal.artifact.Artifact;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactOrderAccessor implements OrderAccessor {

   private final OrderParser parser;
   private final Artifact artifact;

   public ArtifactOrderAccessor(Artifact artifact, OrderParser parser) {
      this.artifact = artifact;
      this.parser = parser;
   }

   @Override
   public void load(HasOrderData data) throws OseeCoreException {
      data.clear();
      String value = artifact.getSoleAttributeAsString(CoreAttributeTypes.RelationOrder, Strings.emptyString());
      parser.loadFromXml(data, value);
   }

   @Override
   public void store(HasOrderData data, OrderChange changeType) throws OseeCoreException {
      if (changeType != OrderChange.NoChange) {
         if (!artifact.isDeleted()) {
            if (!data.isEmpty()) {
               String value = parser.toXml(data);
               artifact.setSoleAttributeFromString(CoreAttributeTypes.RelationOrder, value);
            } else {
               artifact.deleteSoleAttribute(CoreAttributeTypes.RelationOrder);
            }
         }
      }

      // TX_TODO Event
      //         IOseeBranch branch = getArtifact().getBranch();
      //         String branchGuid = branch.getGuid();
      //         DefaultBasicGuidArtifact guidArtifact =
      //            new DefaultBasicGuidArtifact(branchGuid, getArtifact().getArtifactType().getGuid(),
      //               getIArtifact().getGuid());
      //
      //         DefaultBasicGuidRelationReorder reorder =
      //            new DefaultBasicGuidRelationReorder(relationOrderModType, branchGuid, type.getGuid(), guidArtifact);
   }
}
