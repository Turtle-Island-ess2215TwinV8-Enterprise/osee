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

import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.orcs.core.ds.OrcsData;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.LinkResolver;
import org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces.Linker;
import org.eclipse.osee.orcs.core.internal.util.OrcsLazyObject;
import org.eclipse.osee.orcs.core.internal.util.ValueProvider;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactLazyObject extends OrcsLazyObject<ArtifactReadable, RelationData> implements Linker<ArtifactReadable> {

   private final LinkResolver<IOseeBranch, ArtifactReadable> resolver;
   private final ValueProvider<Branch, OrcsData> branch;
   private final RelationSide relationSide;

   public ArtifactLazyObject(RelationSide relationSide, ValueProvider<Branch, OrcsData> branch, RelationData data, LinkResolver<IOseeBranch, ArtifactReadable> resolver) {
      super(data);
      this.relationSide = relationSide;
      this.branch = branch;
      this.resolver = resolver;
   }

   protected RelationSide getRelationSide() {
      return relationSide;
   }

   @Override
   public int getLocalId() {
      RelationData data = getOrcsData();
      int localId = -1;
      if (RelationSide.SIDE_A == relationSide) {
         localId = data.getArtIdA();
      } else {
         localId = data.getArtIdB();
      }
      return localId;
   }

   @Override
   protected ArtifactReadable instance() throws OseeCoreException {
      return resolver.resolve(branch.get(), this);
   }

   @Override
   public void clear() {
      invalidate();
   }
}