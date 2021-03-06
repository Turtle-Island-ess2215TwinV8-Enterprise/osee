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
package org.eclipse.osee.orcs.db.internal.callable;

import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.osee.database.schema.DatabaseCallable;
import org.eclipse.osee.executor.admin.CancellableCallable;
import org.eclipse.osee.framework.core.enums.TransactionVersion;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionDelta;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.TransactionRecordFactory;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.TransactionCache;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.db.internal.change.ComputeNetChangeCallable;
import org.eclipse.osee.orcs.db.internal.change.LoadDeltasBetweenBranches;
import org.eclipse.osee.orcs.db.internal.change.MissingChangeItemFactory;

/**
 * @author Roberto E. Escobar
 */
public class CommitBranchDatabaseCallable extends DatabaseCallable<TransactionRecord> {

   private final TransactionRecordFactory txFactory;
   private final TransactionCache txCache;
   private final BranchCache branchCache;
   private final ArtifactReadable committer;
   private final Branch source;
   private final Branch destination;
   private final MissingChangeItemFactory missingChangeItemFactory;
   private final String sessionId;

   public CommitBranchDatabaseCallable(Log logger, IOseeDatabaseService service, BranchCache branchCache, TransactionCache txCache, TransactionRecordFactory txFactory, ArtifactReadable committer, Branch source, Branch destination, MissingChangeItemFactory missingChangeItemFactory, String sessionId) {
      super(logger, service);
      this.branchCache = branchCache;
      this.txCache = txCache;
      this.txFactory = txFactory;
      this.committer = committer;
      this.source = source;
      this.destination = destination;
      this.missingChangeItemFactory = missingChangeItemFactory;
      this.sessionId = sessionId;
   }

   private TransactionCache getTxCache() {
      return txCache;
   }

   private BranchCache getBranchCache() {
      return branchCache;
   }

   private TransactionRecordFactory getTxFactory() {
      return txFactory;
   }

   private TransactionRecord getHeadTx(Branch branch) throws OseeCoreException {
      return getTxCache().getTransaction(branch, TransactionVersion.HEAD);
   }

   private Branch getMergeBranch(Branch sourceBranch, Branch destinationBranch) throws OseeCoreException {
      return getBranchCache().findMergeBranch(sourceBranch, destinationBranch);
   }

   private TransactionRecord getMergeTx(Branch mergeBranch) throws OseeCoreException {
      return mergeBranch != null ? getTxCache().getTransaction(mergeBranch, TransactionVersion.HEAD) : null;
   }

   private int getUserArtId() {
      return committer != null ? committer.getLocalId() : -1;
   }

   private List<ChangeItem> callComputeChanges(TransactionDelta txDelta, TransactionRecord mergeTx) throws Exception {
      Callable<List<ChangeItem>> loadChanges =
         new LoadDeltasBetweenBranches(getLogger(), getDatabaseService(), txDelta, mergeTx);
      List<ChangeItem> changes = callAndCheckForCancel(loadChanges);

      TransactionRecord sourceTx = getHeadTx(source);
      TransactionRecord destTx = getHeadTx(destination);

      changes.addAll(missingChangeItemFactory.createMissingChanges(changes, sourceTx, destTx, sessionId));

      Callable<List<ChangeItem>> computeChanges = new ComputeNetChangeCallable(changes);
      return callAndCheckForCancel(computeChanges);
   }

   @Override
   public TransactionRecord call() throws Exception {
      TransactionRecord sourceTx = getHeadTx(source);
      TransactionRecord destinationTx = getHeadTx(destination);

      TransactionDelta txDelta = new TransactionDelta(sourceTx, destinationTx);

      Branch mergeBranch = getMergeBranch(source, destination);
      TransactionRecord mergeTx = getMergeTx(mergeBranch);

      List<ChangeItem> changes = callComputeChanges(txDelta, mergeTx);

      CancellableCallable<TransactionRecord> commitCallable =
         new CommitBranchDatabaseTxCallable(getLogger(), getDatabaseService(), getBranchCache(), getUserArtId(),
            source, destination, mergeBranch, changes, getTxFactory());
      TransactionRecord commitTransaction = callAndCheckForCancel(commitCallable);

      getTxCache().cache(commitTransaction);

      return commitTransaction;
   }

}
