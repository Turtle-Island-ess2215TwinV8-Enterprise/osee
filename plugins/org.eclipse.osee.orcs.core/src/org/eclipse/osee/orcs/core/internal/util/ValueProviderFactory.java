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
package org.eclipse.osee.orcs.core.internal.util;

import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.OrcsData;
import org.eclipse.osee.orcs.core.ds.RelationData;

/**
 * @author Roberto E. Escobar
 */
public class ValueProviderFactory {

   private final ArtifactTypeCache artifactTypeCache;
   private final RelationTypeCache relationTypeCache;
   private final BranchCache branchCache;

   public ValueProviderFactory(BranchCache branchCache, ArtifactTypeCache artifactTypeCache, RelationTypeCache relationTypeCache) {
      super();
      this.artifactTypeCache = artifactTypeCache;
      this.branchCache = branchCache;
      this.relationTypeCache = relationTypeCache;
   }

   public ValueProvider<ArtifactType, ArtifactData> createTypeProvider(ArtifactData artifactData) {
      return new LazyTypeProvider<ArtifactType, ArtifactData>(artifactTypeCache, artifactData);
   }

   public ValueProvider<Branch, OrcsData> createBranchProvider(OrcsData data) {
      return new BranchProvider(branchCache, data);
   }

   public ValueProvider<RelationType, RelationData> createTypeProvider(RelationData relationData) {
      return new LazyTypeProvider<RelationType, RelationData>(relationTypeCache, relationData);
   }

}