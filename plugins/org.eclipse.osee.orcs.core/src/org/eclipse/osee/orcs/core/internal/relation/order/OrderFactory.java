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

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.core.internal.artifact.Artifact;
import org.eclipse.osee.orcs.core.internal.relation.sorter.SorterProvider;

/**
 * @author Roberto E. Escobar
 */
public class OrderFactory {

   private final OrderParser parser;
   private final SorterProvider sorterProvider;

   public OrderFactory(OrderParser parser, SorterProvider sorterProvider) {
      super();
      this.parser = parser;
      this.sorterProvider = sorterProvider;
   }

   public OrderManager createOrderManager(Artifact artifact) throws OseeCoreException {
      ArtifactOrderAccessor accessor = new ArtifactOrderAccessor(artifact, parser);
      OrderManager manager = new OrderManager(sorterProvider, accessor);
      manager.load();
      return manager;
   }

}
