/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.datastore;

import java.util.Collections;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.datastore.cache.IBranchUpdateEvent;
import org.eclipse.osee.framework.core.datastore.internal.Activator;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.AbstractDbTxOperation;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Ryan D. Brooks
 */
public class BranchMoveOperation extends AbstractDbTxOperation {
   protected static final int NULL_PARENT_BRANCH_ID = -1;

   private static final String INSERT_ADDRESSING =
      "insert into %s (transaction_id, gamma_id, tx_current, mod_type, branch_id) select transaction_id, gamma_id, tx_current, mod_type, branch_id from %s where branch_id = ?";

   private static final String UPDATE_BRANCH_ARCHIVED = "update osee_branch SET archived = ? where branch_id = ?";

   public static final String DELETE_ADDRESSING = "delete from %s where branch_id = ?";

   private final IBranchUpdateEvent eventSender;
   private final boolean archive;
   private final Branch branch;

   public BranchMoveOperation(IBranchUpdateEvent eventSender, IOseeDatabaseService databaseService, boolean archive, Branch branch) {
      super(databaseService, "Branch Move", Activator.PLUGIN_ID);
      this.eventSender = eventSender;
      this.archive = archive;
      this.branch = branch;
   }

   private Object[] toUpdateValues(Branch branch) {
      return new Object[] {branch.getArchiveState().getValue(), branch.getId()};
   }

   @Override
   protected void doTxWork(IProgressMonitor monitor, OseeConnection connection) throws OseeCoreException {
      BranchArchivedState inProgressState =
         archive ? BranchArchivedState.ARCHIVED_IN_PROGRESS : BranchArchivedState.UNARCHIVED_IN_PROGRESS;
      updateBranchArchivedState(inProgressState);

      String sourceTableName = archive ? "osee_txs" : "osee_txs_archived";
      String destinationTableName = archive ? "osee_txs_archived" : "osee_txs";

      String sql = String.format(INSERT_ADDRESSING, destinationTableName, sourceTableName);
      getDatabaseService().runPreparedUpdate(connection, sql, branch.getId());

      sql = String.format(DELETE_ADDRESSING, sourceTableName);
      getDatabaseService().runPreparedUpdate(connection, sql, branch.getId());

      BranchArchivedState finalState = archive ? BranchArchivedState.ARCHIVED : BranchArchivedState.UNARCHIVED;
      updateBranchArchivedState(finalState);
   }

   @Override
   protected void handleTxException(IProgressMonitor monitor, Exception ex) {
      BranchArchivedState currentState = branch.getArchiveState();
      if (currentState.isBeingArchived() || currentState.isBeingUnarchived()) {
         BranchArchivedState clearState = archive ? BranchArchivedState.UNARCHIVED : BranchArchivedState.ARCHIVED;
         try {
            updateBranchArchivedState(clearState);
         } catch (OseeCoreException ex1) {
            OseeLog.logf(Activator.class, Level.SEVERE, ex1, "Error setting archived state to %s", clearState);
         }
      }
   }

   private void updateBranchArchivedState(BranchArchivedState newState) throws OseeCoreException {
      if (branch.getArchiveState() != newState) {
         synchronized (branch) {
            branch.internalSetArchivedState(newState);
            branch.clearDirty();
         }
         getDatabaseService().runPreparedUpdate(UPDATE_BRANCH_ARCHIVED, toUpdateValues(branch));
         try {
            eventSender.send(Collections.singleton(branch));
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, "Error creating branch update relay", ex);
         }
      }
   }
}