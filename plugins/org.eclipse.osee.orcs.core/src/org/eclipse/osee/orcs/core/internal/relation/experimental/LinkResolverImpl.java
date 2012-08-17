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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.internal.ArtifactLoader;
import org.eclipse.osee.orcs.core.internal.ArtifactLoaderFactory;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.HasLinkers;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.LinkResolver;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.Linker;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Roberto E. Escobar
 */
public class LinkResolverImpl implements LinkResolver<IOseeBranch, ArtifactReadable> {

   private final BranchCache branchCache;
   private final ArtifactLoaderFactory loadFactory;
   private final OrcsSession session;

   public LinkResolverImpl(OrcsSession session, ArtifactLoaderFactory loadFactory, BranchCache branchCache) {
      super();
      this.session = session;
      this.loadFactory = loadFactory;
      this.branchCache = branchCache;
   }

   private ArtifactReadable getFromCache(IOseeBranch branch, int artId) throws OseeCoreException {
      int branchId = branchCache.getLocalId(branch);
      //      return session.getActive(artId, branchId);
      return null;
   }

   private List<ArtifactReadable> loadArtifacts(IOseeBranch branch, Collection<Integer> artifactIds) throws OseeCoreException {
      ArtifactLoader loader = loadFactory.fromBranchAndArtifactIds(session, branch, artifactIds);
      loader.setLoadLevel(LoadLevel.FULL);
      return loader.load();
   }

   @Override
   public ArtifactReadable resolve(IOseeBranch source, Linker<ArtifactReadable> linker) throws OseeCoreException {
      int artId = linker.getLocalId();
      ArtifactReadable data = getFromCache(source, artId);
      if (data == null) {
         ArtifactLoader loader = loadFactory.fromBranchAndArtifactIds(session, source, artId);
         loader.setLoadLevel(LoadLevel.FULL);
         data = loader.getResults().getExactlyOne();
      }
      linker.set(data);
      linker.setLoaded(true);
      return data;
   }

   @Override
   public void resolve(IOseeBranch branch, HasLinkers<ArtifactReadable>... links) throws OseeCoreException {
      resolve(branch, Arrays.asList(links));
   }

   @Override
   public void resolve(IOseeBranch branch, RelationSide side, HasLinkers<ArtifactReadable>... links) throws OseeCoreException {
      resolve(branch, side, Arrays.asList(links));
   }

   @Override
   public void resolve(IOseeBranch branch, RelationSide side, Collection<? extends HasLinkers<ArtifactReadable>> links) throws OseeCoreException {
      Multimap<Integer, Linker<ArtifactReadable>> toLoad = getItemsToLoad(branch, side, links);
      loadItems(branch, toLoad.asMap());
   }

   @Override
   public void resolve(IOseeBranch branch, Collection<? extends HasLinkers<ArtifactReadable>> links) throws OseeCoreException {
      Multimap<Integer, Linker<ArtifactReadable>> toLoad = getItemsToLoad(branch, links);
      loadItems(branch, toLoad.asMap());
   }

   private Multimap<Integer, Linker<ArtifactReadable>> getItemsToLoad(IOseeBranch branch, RelationSide side, Collection<? extends HasLinkers<ArtifactReadable>> links) throws OseeCoreException {
      Multimap<Integer, Linker<ArtifactReadable>> toLoad = HashMultimap.<Integer, Linker<ArtifactReadable>> create();
      for (HasLinkers<ArtifactReadable> link : links) {
         Linker<ArtifactReadable> linker = link.getLinkerOnSide(side);
         addToLoad(branch, linker, toLoad);
      }
      return toLoad;
   }

   private Multimap<Integer, Linker<ArtifactReadable>> getItemsToLoad(IOseeBranch branch, Collection<? extends HasLinkers<ArtifactReadable>> links) throws OseeCoreException {
      Multimap<Integer, Linker<ArtifactReadable>> toLoad = HashMultimap.<Integer, Linker<ArtifactReadable>> create();
      for (HasLinkers<ArtifactReadable> link : links) {
         for (Linker<ArtifactReadable> linker : link.getLinkers()) {
            addToLoad(branch, linker, toLoad);
         }
      }
      return toLoad;
   }

   private void addToLoad(IOseeBranch branch, Linker<ArtifactReadable> linker, Multimap<Integer, Linker<ArtifactReadable>> toLoad) throws OseeCoreException {
      if (!linker.isLoaded()) {
         int localId = linker.getLocalId();
         ArtifactReadable readable = getFromCache(branch, localId);
         if (readable != null) {
            linker.set(readable);
            linker.setLoaded(true);
         } else {
            toLoad.put(localId, linker);
         }
      }
   }

   private void loadItems(IOseeBranch branch, Map<Integer, Collection<Linker<ArtifactReadable>>> map) throws OseeCoreException {
      List<ArtifactReadable> artifacts = loadArtifacts(branch, map.keySet());
      for (ArtifactReadable result : artifacts) {
         Collection<Linker<ArtifactReadable>> lazies = map.get(result.getLocalId());
         for (Linker<ArtifactReadable> linker : lazies) {
            linker.set(result);
            linker.setLoaded(true);
         }
      }
   }

}
