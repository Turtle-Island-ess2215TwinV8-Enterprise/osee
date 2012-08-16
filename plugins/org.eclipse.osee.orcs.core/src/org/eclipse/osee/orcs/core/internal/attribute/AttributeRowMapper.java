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
package org.eclipse.osee.orcs.core.internal.attribute;

import java.util.Map;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.AttributeDataHandler;

/**
 * @author Roberto E. Escobar
 */
public class AttributeRowMapper implements AttributeDataHandler {

   private final AttributeFactory factory;
   private final Map<Integer, ? extends AttributeManager> attributeContainers;
   private final Log logger;

   public AttributeRowMapper(Log logger, AttributeFactory attributeFactory, Map<Integer, ? extends AttributeManager> attributeContainers) {
      this.logger = logger;
      this.attributeContainers = attributeContainers;
      this.factory = attributeFactory;
   }

   private AttributeManager getContainer(AttributeData current) {
      AttributeManager container = attributeContainers.get(current.getArtifactId());
      if (container == null) {
         logger.warn("Orphaned attribute detected - [%s]", current);
      }
      return container;
   }

   @Override
   public void onData(AttributeData data) throws OseeCoreException {
      AttributeManager container = getContainer(data);
      if (container == null) {
         return; // If the artifact is null, it means the attributes are orphaned.
      }
      factory.createAttribute(container, data);
   }

   //   private void handleMultipleVersions(AttributeRow previous, AttributeRow current) {
   //      // Do not warn about skipping on historical loading, because the most recent
   //      // transaction is used first due to sorting on the query
   //      if (!previous.isHistorical() && !current.isHistorical()) {
   //         logger.warn("Multiple attribute versions detected - \n\t[%s]\n\t[%s]", current, previous);
   //      }
   //   }
}
