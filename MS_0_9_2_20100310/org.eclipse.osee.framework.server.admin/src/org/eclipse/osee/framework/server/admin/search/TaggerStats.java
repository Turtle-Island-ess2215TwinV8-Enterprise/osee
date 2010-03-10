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
package org.eclipse.osee.framework.server.admin.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.search.engine.ISearchEngineTagger;
import org.eclipse.osee.framework.search.engine.ITagItemStatistics;
import org.eclipse.osee.framework.search.engine.ITaggerStatistics;
import org.eclipse.osee.framework.server.admin.BaseServerCommand;
import org.eclipse.osee.framework.server.admin.internal.Activator;

/**
 * @author Roberto E. Escobar
 */
class TaggerStats extends BaseServerCommand {

   protected TaggerStats() {
      super("Tag Engine Stats");
   }

   private String toString(ITagItemStatistics task) {
      return String.format("id: [%d] - processed [%d] tags in [%d] ms", task.getGammaId(), task.getTotalTags(),
            task.getProcessingTime());
   }

   @Override
   protected void doCommandWork(IProgressMonitor monitor) throws Exception {
      ISearchEngineTagger tagger = Activator.getInstance().getSearchTagger();

      ITaggerStatistics stats = tagger.getStatistics();

      StringBuffer buffer = new StringBuffer();
      buffer.append("\n----------------------------------------------\n");
      buffer.append("                  Tagger Stats                \n");
      buffer.append("----------------------------------------------\n");
      buffer.append(String.format("Query Id Processing Time  - avg: [%s] ms - longest: [%s] ms\n",
            stats.getAverageQueryIdProcessingTime(), stats.getLongestQueryIdProcessingTime()));
      buffer.append(String.format("Query Id Wait Time        - avg: [%s] ms - longest: [%s] ms\n",
            stats.getAverageQueryIdWaitTime(), stats.getLongestQueryIdWaitTime()));

      buffer.append(String.format("Attribute Processing Time - avg: [%s] ms - longest: [%s] ms\n",
            stats.getAverageAttributeProcessingTime(), stats.getLongestAttributeProcessingTime()));
      buffer.append(String.format("Attribute with longest processing time - %s\n", toString(stats.getLongestTask())));
      buffer.append(String.format("Attribute with most tags - %s\n", toString(stats.getMostTagsTask())));
      buffer.append(String.format("Total - QueryIds: [%d] Attributes: [%d] Tags: [%d]\n",
            stats.getTotalQueryIdsProcessed(), stats.getTotalAttributesProcessed(), stats.getTotalTags()));
      buffer.append(String.format("Total Query Ids Waiting to be Processed - [%d]\n", tagger.getWorkersInQueue()));
      buffer.append(String.format("Total Query Ids in Tag Queue Table - [%d]\n", stats.getTotalQueryIdsInQueue()));
      buffer.append(String.format("Total Tags in System - [%d]\n\n", stats.getTagsInSystem()));

      println(buffer.toString());
   }
}
