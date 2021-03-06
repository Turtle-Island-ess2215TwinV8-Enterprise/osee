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
package org.eclipse.osee.framework.skynet.core.revision;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionDelta;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeSql;
import org.eclipse.osee.framework.database.core.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.type.CompositeKeyHashMap;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoader;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.change.ArtifactDelta;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;

/**
 * Public API class for access to change data from branches and transactionIds
 * 
 * @author Jeff C. Phillips
 * @author Donald G. Dunne
 */
public final class ChangeManager {

   private final static RevisionChangeLoader revsionChangeLoader = new RevisionChangeLoader();

   private ChangeManager() {
      // this private empty constructor exists to prevent the default constructor from allowing public construction
   }

   public static Collection<ArtifactDelta> getCompareArtifacts(Collection<Change> changes) {
      Collection<ArtifactDelta> toReturn = new ArrayList<ArtifactDelta>(changes.size());
      for (Change change : changes) {
         toReturn.add(change.getDelta());
      }
      return toReturn;
   }

   /**
    * Acquires changes for a particular artifact
    */
   public static Collection<Change> getChangesPerArtifact(Artifact artifact, IProgressMonitor monitor) throws OseeCoreException {
      return revsionChangeLoader.getChangesPerArtifact(artifact, monitor);
   }

   /**
    * Acquires artifact, relation and attribute changes from a source branch since its creation.
    */
   public static IOperation comparedToPreviousTx(TransactionRecord transactionId, Collection<Change> changes) throws OseeCoreException {
      TransactionRecord startTx = TransactionManager.getPriorTransaction(transactionId);
      TransactionRecord endTx = transactionId;

      TransactionDelta txDelta = new TransactionDelta(startTx, endTx);
      return new ChangeDataLoader(changes, txDelta);
   }

   /**
    * Acquires artifact, relation and attribute changes from a source branch since its creation.
    */
   public static IOperation comparedToParent(IOseeBranch sourceBranch, Collection<Change> changes) throws OseeCoreException {
      Branch branch = BranchManager.getBranch(sourceBranch);

      TransactionRecord startTx = TransactionManager.getHeadTransaction(branch);
      TransactionRecord endTx = TransactionManager.getHeadTransaction(branch.getParentBranch());

      TransactionDelta txDelta = new TransactionDelta(startTx, endTx);
      return new ChangeDataLoader(changes, txDelta);
   }

   public static IOperation compareTwoBranchesHead(IOseeBranch sourceBranch, IOseeBranch destinationBranch, Collection<Change> changes) throws OseeCoreException {
      TransactionRecord startTx = TransactionManager.getHeadTransaction(sourceBranch);
      TransactionRecord endTx = TransactionManager.getHeadTransaction(destinationBranch);
      return new ChangeDataLoader(changes, new TransactionDelta(startTx, endTx));
   }

   /**
    * For the given list of artifacts determine which transactions (on that artifact's branch) affected that artifact.
    * The branch's baseline transaction is excluded.
    * 
    * @return a map of artifact to collection of TransactionIds which affected the given artifact
    */
   public static HashCollection<Artifact, TransactionRecord> getModifingTransactions(Collection<Artifact> artifacts) throws OseeCoreException {
      List<Object[]> insertParameters = new ArrayList<Object[]>(artifacts.size() * 5);

      int queryId = ArtifactLoader.getNewQueryId();
      Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();

      CompositeKeyHashMap<Integer, Branch, Artifact> artifactMap = new CompositeKeyHashMap<Integer, Branch, Artifact>();
      for (Artifact artifact : artifacts) {
         Branch branch = artifact.getFullBranch();
         artifactMap.put(artifact.getArtId(), branch, artifact);
         int transactionNumber = TransactionManager.getHeadTransaction(branch).getId();
         insertParameters.add(new Object[] {queryId, insertTime, artifact.getArtId(), branch.getId(), transactionNumber});

         // for each combination of artifact and its branch hierarchy
         while (branch.hasParentBranch()) {
            transactionNumber = branch.getSourceTransaction().getId();
            branch = branch.getParentBranch();
            insertParameters.add(new Object[] {
               queryId,
               insertTime,
               artifact.getArtId(),
               branch.getId(),
               transactionNumber});
         }
      }

      HashCollection<Artifact, TransactionRecord> transactionMap = new HashCollection<Artifact, TransactionRecord>();
      try {
         ArtifactLoader.insertIntoArtifactJoin(insertParameters);
         IOseeStatement chStmt = ConnectionHandler.getStatement();
         try {
            chStmt.runPreparedQuery(insertParameters.size() * 2,
               ClientSessionManager.getSql(OseeSql.CHANGE_TX_MODIFYING), queryId);
            while (chStmt.next()) {
               Branch branch = BranchManager.getBranch(chStmt.getInt("branch_id"));
               Artifact artifact = artifactMap.get(chStmt.getInt("art_id"), branch);
               transactionMap.put(artifact, TransactionManager.getTransactionId(chStmt.getInt("transaction_id")));
            }
         } finally {
            chStmt.close();
         }
      } finally {
         ArtifactLoader.clearQuery(queryId);
      }

      return transactionMap;
   }

   /**
    * For the given list of artifacts determine which branches (in the branch hierarchy for that artifact) affected that
    * artifact.
    * 
    * @return a map of artifact to collection of branches which affected the given artifact
    */
   public static HashCollection<Artifact, Branch> getModifingBranches(Collection<Artifact> artifacts) throws OseeCoreException {
      List<Object[]> insertParameters = new ArrayList<Object[]>(artifacts.size() * 5);
      int queryId = ArtifactLoader.getNewQueryId();
      Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();

      CompositeKeyHashMap<Integer, IOseeBranch, Artifact> artifactMap =
         new CompositeKeyHashMap<Integer, IOseeBranch, Artifact>();
      for (Artifact artifact : artifacts) {
         artifactMap.put(artifact.getArtId(), artifact.getBranch(), artifact);
         // for each combination of artifact and all working branches in its
         // hierarchy
         for (Branch workingBranch : BranchManager.getBranches(BranchArchivedState.UNARCHIVED, BranchType.WORKING)) {
            if (artifact.getBranch().equals(workingBranch.getParentBranch())) {
               insertParameters.add(new Object[] {
                  queryId,
                  insertTime,
                  artifact.getArtId(),
                  workingBranch.getId(),
                  SQL3DataType.INTEGER});
            }
         }
      }

      HashCollection<Artifact, Branch> branchMap = new HashCollection<Artifact, Branch>();
      try {
         ArtifactLoader.insertIntoArtifactJoin(insertParameters);
         IOseeStatement chStmt = ConnectionHandler.getStatement();
         try {
            chStmt.runPreparedQuery(insertParameters.size() * 2,
               ClientSessionManager.getSql(OseeSql.CHANGE_BRANCH_MODIFYING), queryId);
            while (chStmt.next()) {
               if (chStmt.getInt("tx_count") > 0) {
                  Branch branch = BranchManager.getBranch(chStmt.getInt("branch_id"));
                  Artifact artifact = artifactMap.get(chStmt.getInt("art_id"), branch.getParentBranch());
                  branchMap.put(artifact, branch);
               }
            }
         } finally {
            chStmt.close();
         }
      } finally {
         ArtifactLoader.clearQuery(queryId);
      }
      return branchMap;
   }
}