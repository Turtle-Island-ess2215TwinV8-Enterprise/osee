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
package org.eclipse.osee.orcs.core.internal.relation.experimental;

import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.orcs.core.ds.OrcsData;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.internal.relation.RelationContainer;
import org.eclipse.osee.orcs.core.internal.relation.RelationContainerImpl;
import org.eclipse.osee.orcs.core.internal.relation.experimental.ArtifactLazyObject.RelatedLoader;
import org.eclipse.osee.orcs.core.internal.util.BranchProvider;
import org.eclipse.osee.orcs.core.internal.util.LazyTypeProvider;
import org.eclipse.osee.orcs.core.internal.util.ValueProvider;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Roberto E. Escobar
 */
public class RelationFactory {

   private final RelationTypeCache relationTypeCache;
   private final BranchCache branchCache;

   public RelationFactory(BranchCache branchCache, RelationTypeCache relationTypeCache) {
      this.branchCache = branchCache;
      this.relationTypeCache = relationTypeCache;
   }

   public RelationContainer createRelationContainer(int artId) {
      return new RelationContainerImpl(artId, relationTypeCache);
   }

   public RelationLinkCollection createCollection() {
      return new RelationLinkCollection();
   }

   public RelationLink createRelationLink(RelationData data, RelatedLoader loader) {
      ValueProvider<Branch, OrcsData> branch = new BranchProvider(branchCache, data);

      //@formatter:off
      ValueProvider<RelationType, RelationData> type = new LazyTypeProvider<RelationType, RelationData>(relationTypeCache, data);
      ValueProvider<ArtifactReadable, RelationData> aArtifact = new ArtifactLazyObject(RelationSide.SIDE_A, branch, data, loader);
      ValueProvider<ArtifactReadable, RelationData> bArtifact = new ArtifactLazyObject(RelationSide.SIDE_B, branch, data, loader);
      //@formatter:on

      RelationLink link = new RelationLink(data, branch, type, aArtifact, bArtifact);
      link.setNotDirty();
      return link;
   }

}
