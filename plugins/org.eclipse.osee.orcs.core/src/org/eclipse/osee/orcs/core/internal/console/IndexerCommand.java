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
package org.eclipse.osee.orcs.core.internal.console;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.eclipse.osee.console.admin.Console;
import org.eclipse.osee.console.admin.ConsoleCommand;
import org.eclipse.osee.console.admin.ConsoleParameters;
import org.eclipse.osee.executor.admin.CancellableCallable;
import org.eclipse.osee.framework.core.model.ReadableBranch;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.search.QueryIndexer;
import org.eclipse.osee.orcs.statistics.IndexerStatistics;

/**
 * @author Roberto E. Escobar
 */
public class IndexerCommand implements ConsoleCommand {

   private static enum OpType {
      STATS,
      DROP,
      ALL,
      MISSING_ITEMS_ONLY,
      CANCEL
   }

   private OrcsApi orcsApi;

   public void setOrcsApi(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   public OrcsApi getOrcsApi() {
      return orcsApi;
   }

   @Override
   public String getName() {
      return "index";
   }

   @Override
   public String getDescription() {
      return "Interacts with the data indexer";
   }

   @Override
   public String getUsage() {
      StringBuilder builder = new StringBuilder();
      builder.append("op=<");
      builder.append(Collections.toString("|", (Object[]) OpType.values()));
      builder.append(">]");
      builder.append("\n");
      builder.append("[branchGuids=<BRANCH_GUID,..>]");
      return builder.toString();
   }

   @Override
   public Callable<?> createCallable(Console console, ConsoleParameters params) {
      return new IndexerCommandCallable(console, params);
   }

   private final class IndexerCommandCallable extends CancellableCallable<Boolean> {

      private final Console console;
      private final ConsoleParameters params;

      public IndexerCommandCallable(Console console, ConsoleParameters params) {
         this.console = console;
         this.params = params;
      }

      private OpType toOpType(String value) {
         OpType opType = OpType.STATS;
         if (Strings.isValid(value)) {
            opType = OpType.valueOf(value.toUpperCase());
         }
         return opType;
      }

      @Override
      public Boolean call() throws Exception {
         long startTime = System.currentTimeMillis();
         boolean indexOnlyMissingitems = false;

         Set<ReadableBranch> branches = new HashSet<ReadableBranch>();
         String[] guids = params.getArray("branchGuids");
         if (guids != null & guids.length > 0) {
            BranchCache branchCache = getOrcsApi().getBranchCache();
            for (String guid : guids) {
               branches.add(branchCache.getByGuid(guid));
            }
         }

         QueryIndexer indexer = getOrcsApi().getQueryIndexer(null);

         OpType opType = toOpType(params.get("op"));
         switch (opType) {
            case CANCEL:
               throw new UnsupportedOperationException("Can't cancel index op");
            case DROP:
               if (branches.isEmpty()) {
                  indexer.purgeAllIndexes().call();
               } else {
                  throw new UnsupportedOperationException("Can't selectively drop indexed branches");
               }
               break;
            case MISSING_ITEMS_ONLY:
               indexOnlyMissingitems = true;
            case ALL:
               IndexStatusDisplayCollector collector = new IndexStatusDisplayCollector(console, startTime);
               Callable<?> callable = indexer.indexBranches(collector, branches, indexOnlyMissingitems);
               callable.call();
               break;
            case STATS:
            default:
               IndexerStatistics indexerStats = getOrcsApi().getOrcsPerformance(null).getIndexerStatistics();
               IndexerUtil.writeStatistics(console, indexerStats);
               break;
         }
         return Boolean.TRUE;
      }
   }
   //      sb.append("        tag [<gammaId> <gammaId> ...]- tag individual item\n");
}