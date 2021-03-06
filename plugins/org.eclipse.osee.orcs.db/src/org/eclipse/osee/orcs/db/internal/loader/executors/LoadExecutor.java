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
package org.eclipse.osee.orcs.db.internal.loader.executors;

import java.util.Collection;
import org.eclipse.osee.executor.admin.HasCancellation;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.ArtifactJoinQuery;
import org.eclipse.osee.framework.database.core.JoinUtility;
import org.eclipse.osee.orcs.core.ds.ArtifactBuilder;
import org.eclipse.osee.orcs.core.ds.LoadOptions;
import org.eclipse.osee.orcs.db.internal.loader.LoadSqlContext;
import org.eclipse.osee.orcs.db.internal.loader.SqlArtifactLoader;
import org.eclipse.osee.orcs.db.internal.loader.criteria.CriteriaOrcsLoad;

/**
 * @author Andrew M. Finkbeiner
 */
public class LoadExecutor extends AbstractLoadExecutor {

   private final String sessionId;
   private final int branchId;
   private final Collection<Integer> artifactIds;

   public LoadExecutor(SqlArtifactLoader loader, IOseeDatabaseService dbService, String sessionId, int branchId, Collection<Integer> artifactIds) {
      super(loader, dbService);
      this.sessionId = sessionId;
      this.branchId = branchId;
      this.artifactIds = artifactIds;
   }

   @Override
   public void load(HasCancellation cancellation, ArtifactBuilder builder, CriteriaOrcsLoad criteria, LoadOptions options) throws OseeCoreException {
      checkCancelled(cancellation);

      ArtifactJoinQuery join = JoinUtility.createArtifactJoinQuery(getDatabaseService());
      Integer transactionId = options.getFromTransaction();
      for (Integer artId : artifactIds) {
         join.add(artId, branchId, transactionId);
      }

      LoadSqlContext loadContext = new LoadSqlContext(sessionId, options);
      int fetchSize = computeFetchSize(artifactIds.size());
      loadFromJoin(join, cancellation, builder, criteria, loadContext, fetchSize);
   }

}
