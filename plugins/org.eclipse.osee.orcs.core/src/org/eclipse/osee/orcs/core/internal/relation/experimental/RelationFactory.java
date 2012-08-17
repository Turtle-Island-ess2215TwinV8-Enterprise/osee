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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.ds.OrcsData;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.internal.ArtifactLoaderFactory;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.Graph;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.LinkResolver;
import org.eclipse.osee.orcs.core.internal.util.ValueProvider;
import org.eclipse.osee.orcs.core.internal.util.ValueProviderFactory;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Roberto E. Escobar
 */
public class RelationFactory {

   private final BranchCache branchCache;
   private final ValueProviderFactory providerFactory;

   public RelationFactory(ValueProviderFactory providerFactory, BranchCache branchCache) {
      this.providerFactory = providerFactory;
      this.branchCache = branchCache;
   }

   public Relation createRelationLink(RelationData data, LinkResolver<IOseeBranch, ArtifactReadable> loader) {
      ValueProvider<Branch, OrcsData> branch = providerFactory.createBranchProvider(data);
      ValueProvider<RelationType, RelationData> type = providerFactory.createTypeProvider(data);

      List<ArtifactLazyObject> linkers = new ArrayList<ArtifactLazyObject>(RelationSide.values().length);
      initLinker(linkers, new ArtifactLazyObject(RelationSide.SIDE_A, branch, data, loader));
      initLinker(linkers, new ArtifactLazyObject(RelationSide.SIDE_B, branch, data, loader));

      Relation link = new Relation(data, branch, type, linkers);
      link.setNotDirty();
      return link;
   }

   private void initLinker(List<ArtifactLazyObject> linkers, ArtifactLazyObject data) {
      linkers.add(data.getRelationSide().ordinal(), data);
   }

   public RelationCollection createCollection() {
      return new RelationCollection();
   }

   public Graph createGraph(OrcsSession session, ArtifactLoaderFactory loadFactory) {
      LinkResolver<IOseeBranch, ArtifactReadable> loader =
         new LinkResolverImpl(session, loadFactory, branchCache);
      return new GraphImpl(loader);
   }

}
