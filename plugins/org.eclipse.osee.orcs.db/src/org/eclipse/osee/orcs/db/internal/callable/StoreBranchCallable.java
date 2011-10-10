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
package org.eclipse.osee.orcs.db.internal.callable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.eclipse.osee.event.EventService;
import org.eclipse.osee.executor.admin.ExecutorAdmin;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.model.AbstractOseeType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.BranchField;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.orcs.OrcsConstants;
import org.eclipse.osee.orcs.db.internal.util.DatabaseTxCallable;

/**
 * @author Ryan D. Brooks
 */
public class StoreBranchCallable extends DatabaseTxCallable {
   protected static final int NULL_PARENT_BRANCH_ID = -1;

   private static final String INSERT_BRANCH =
      "INSERT INTO osee_branch (branch_id, branch_guid, branch_name, parent_branch_id, parent_transaction_id, archived, associated_art_id, branch_type, branch_state, baseline_transaction_id) VALUES (?,?,?,?,?,?,?,?,?,?)";

   private static final String UPDATE_BRANCH =
      "update osee_branch SET branch_name = ?, parent_branch_id = ?, parent_transaction_id = ?, archived = ?, associated_art_id = ?, branch_type = ?, branch_state = ?, baseline_transaction_id = ? where branch_id = ?";

   private static final String DELETE_BRANCH = "DELETE from osee_branch where branch_id = ?";

   private final Collection<Branch> branches;
   private final ExecutorAdmin executorAdmin;
   private final EventService eventService;

   public StoreBranchCallable(IOseeDatabaseService dbService, ExecutorAdmin executorAdmin, EventService eventService, Collection<Branch> branches) {
      super(dbService, "Branch Archive Operation");
      this.executorAdmin = executorAdmin;
      this.eventService = eventService;
      this.branches = branches;
   }

   private EventService getEventService() {
      return eventService;
   }

   private ExecutorAdmin getExecutorAdmin() {
      return executorAdmin;
   }

   @Override
   public void handleTxWork(OseeConnection connection) throws OseeCoreException {
      List<Object[]> insertData = new ArrayList<Object[]>();
      List<Object[]> updateData = new ArrayList<Object[]>();
      List<Object[]> deleteData = new ArrayList<Object[]>();

      for (Branch branch : branches) {
         if (isDataDirty(branch)) {
            switch (branch.getStorageState()) {
               case CREATED:
                  branch.setId(getDatabaseService().getSequence().getNextBranchId());
                  insertData.add(toInsertValues(branch));
                  break;
               case MODIFIED:
                  updateData.add(toUpdateValues(branch));
                  break;
               case PURGED:
                  deleteData.add(toDeleteValues(branch));
                  break;
               default:
                  break;
            }
         }
         if (branch.isFieldDirty(BranchField.BRANCH_ARCHIVED_STATE_FIELD_KEY)) {
            DatabaseTxCallable task =
               new MoveBranchCallable(getDatabaseService(), getEventService(), branch.getArchiveState().isArchived(),
                  branch);
            try {
               getExecutorAdmin().schedule(task);
            } catch (Exception ex) {
               OseeExceptions.wrapAndThrow(ex);
            }
         }
      }
      getDatabaseService().runBatchUpdate(connection, INSERT_BRANCH, insertData);
      getDatabaseService().runBatchUpdate(connection, UPDATE_BRANCH, updateData);
      getDatabaseService().runBatchUpdate(connection, DELETE_BRANCH, deleteData);

      for (Branch branch : branches) {
         branch.clearDirty();
      }
      getEventService().postEvent(OrcsConstants.BRANCH_CHANGE_EVENT, new HashMap<String, Object>());
   }

   private Object[] toInsertValues(Branch branch) throws OseeCoreException {
      Branch parentBranch = branch.getParentBranch();
      TransactionRecord baseTxRecord = branch.getBaseTransaction();
      int parentBranchId = parentBranch != null ? parentBranch.getId() : NULL_PARENT_BRANCH_ID;
      int baselineTransaction = baseTxRecord != null ? baseTxRecord.getId() : NULL_PARENT_BRANCH_ID;

      return new Object[] {
         branch.getId(),
         branch.getGuid(),
         branch.getName(),
         parentBranchId,
         branch.getSourceTransaction().getId(),
         branch.getArchiveState().getValue(),
         branch.getAssociatedArtifactId(),
         branch.getBranchType().getValue(),
         branch.getBranchState().getValue(),
         baselineTransaction};
   }

   private Object[] toUpdateValues(Branch branch) throws OseeCoreException {
      Branch parentBranch = branch.getParentBranch();
      TransactionRecord baseTxRecord = branch.getBaseTransaction();
      int parentBranchId = parentBranch != null ? parentBranch.getId() : NULL_PARENT_BRANCH_ID;
      int baselineTransaction = baseTxRecord != null ? baseTxRecord.getId() : NULL_PARENT_BRANCH_ID;
      return new Object[] {
         branch.getName(),
         parentBranchId,
         branch.getSourceTransaction().getId(),
         branch.getArchiveState().getValue(),
         branch.getAssociatedArtifactId(),
         branch.getBranchType().getValue(),
         branch.getBranchState().getValue(),
         baselineTransaction,
         branch.getId()};
   }

   private Object[] toDeleteValues(Branch branch) {
      return new Object[] {branch.getId()};
   }

   private boolean isDataDirty(Branch type) throws OseeCoreException {
      return type.areFieldsDirty(//
         AbstractOseeType.NAME_FIELD_KEY, //
         AbstractOseeType.UNIQUE_ID_FIELD_KEY, //
         BranchField.BRANCH_ARCHIVED_STATE_FIELD_KEY, //
         BranchField.BRANCH_STATE_FIELD_KEY, //
         BranchField.BRANCH_TYPE_FIELD_KEY, //
         BranchField.BRANCH_ASSOCIATED_ARTIFACT_ID_FIELD_KEY, //
         BranchField.BRANCH_BASE_TRANSACTION);
   }

}