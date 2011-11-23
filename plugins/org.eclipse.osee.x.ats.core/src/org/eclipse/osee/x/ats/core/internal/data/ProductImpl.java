/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.x.ats.core.internal.data;

import java.util.Collection;
import java.util.Collections;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.x.ats.AtsException;
import org.eclipse.osee.x.ats.AtsGraph;
import org.eclipse.osee.x.ats.data.Product;
import org.eclipse.osee.x.ats.data.Version;

public class ProductImpl extends AbstractAtsData implements Product {

   private final AtsGraph graph;

   public ProductImpl(ReadableArtifact proxiedObject, AtsGraph graph) {
      super(proxiedObject);
      this.graph = graph;
   }

   @Override
   public Collection<Version> getVersions() {
      try {
         return graph.getVersions(this);
      } catch (AtsException ex) {
         ex.printStackTrace();
      }
      return Collections.emptyList();
   }

   @Override
   public Collection<Product> getProducts() {
      return null;
   }

}
