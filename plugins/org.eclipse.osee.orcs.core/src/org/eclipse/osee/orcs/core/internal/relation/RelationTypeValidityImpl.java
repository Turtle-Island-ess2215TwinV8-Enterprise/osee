/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.relation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.core.util.Conditions;

/**
 * @author Roberto E. Escobar
 */
public class RelationTypeValidityImpl implements RelationTypeValidity {

   private final RelationTypeCache relationTypeCache;

   public RelationTypeValidityImpl(RelationTypeCache relationTypeCache) {
      super();
      this.relationTypeCache = relationTypeCache;
   }

   @Override
   public int getMaximumRelationAllowed(ArtifactType artifactType, IRelationTypeSide relationTypeSide) throws OseeCoreException {
      Conditions.checkNotNull(artifactType, "artifactType");
      Conditions.checkNotNull(relationTypeSide, "relationTypeSide");
      int toReturn = 0;
      RelationType relationType = relationTypeCache.get(relationTypeSide);
      RelationSide relationSide = relationTypeSide.getSide();
      if (relationType.isArtifactTypeAllowed(relationSide, artifactType)) {
         toReturn = relationType.getMultiplicity().getLimit(relationSide);
      }
      return toReturn;
   }

   @Override
   public List<RelationType> getValidRelationTypes(ArtifactType artifactType) throws OseeCoreException {
      Conditions.checkNotNull(artifactType, "artifactType");
      Collection<RelationType> relationTypes = relationTypeCache.getAll();
      List<RelationType> toReturn = new ArrayList<RelationType>();
      for (RelationType relationType : relationTypes) {
         if (isTypeAllowed(artifactType, relationType)) {
            toReturn.add(relationType);
         }
      }
      return toReturn;
   }

   private boolean isTypeAllowed(ArtifactType artifactType, RelationType relationType) throws OseeCoreException {
      boolean result = false;
      for (RelationSide side : RelationSide.values()) {
         int sideMax = getRelationSideMax(artifactType, relationType, side);
         if (sideMax > 0) {
            result = true;
            break;
         }
      }
      return result;
   }

   private int getRelationSideMax(ArtifactType artifactType, RelationType relationType, RelationSide relationSide) throws OseeCoreException {
      int toReturn = 0;
      if (relationType.isArtifactTypeAllowed(relationSide, artifactType)) {
         toReturn = relationType.getMultiplicity().getLimit(relationSide);
      }
      return toReturn;
   }

}