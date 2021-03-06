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

package org.eclipse.osee.framework.skynet.core.artifact;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.core.client.OseeClientProperties;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.BranchDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleBranchesExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.MergeBranch;
import org.eclipse.osee.framework.core.model.TransactionDelta;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.BranchFilter;
import org.eclipse.osee.framework.core.model.event.DefaultBasicGuidArtifact;
import org.eclipse.osee.framework.core.operation.CompositeOperation;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.database.core.OseeInfo;
import org.eclipse.osee.framework.database.core.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.ExtensionDefinedObjects;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.operation.FinishUpdateBranchOperation;
import org.eclipse.osee.framework.skynet.core.artifact.operation.UpdateBranchOperation;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.update.ConflictResolverOperation;
import org.eclipse.osee.framework.skynet.core.commit.actions.CommitAction;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.framework.skynet.core.httpRequests.CommitBranchHttpRequestOperation;
import org.eclipse.osee.framework.skynet.core.httpRequests.CreateBranchHttpRequestOperation;
import org.eclipse.osee.framework.skynet.core.httpRequests.PurgeBranchHttpRequestOperation;
import org.eclipse.osee.framework.skynet.core.httpRequests.UpdateBranchArchivedStateHttpRequestOperation;
import org.eclipse.osee.framework.skynet.core.httpRequests.UpdateBranchStateHttpRequestOperation;
import org.eclipse.osee.framework.skynet.core.httpRequests.UpdateBranchTypeHttpRequestOperation;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;

/**
 * Provides access to all branches as well as support for creating branches of all types
 * 
 * @author Ryan D. Brooks
 */
public class BranchManager {
   private static final BranchManager instance = new BranchManager();

   private static final String LAST_DEFAULT_BRANCH = "LastDefaultBranch";
   public static final String COMMIT_COMMENT = "Commit Branch ";
   private static final String MERGE_RELOAD_KEY = "merge.branch.reload.cache";

   private Branch lastBranch;

   private BranchManager() {
      // this private empty constructor exists to prevent the default constructor from allowing public construction
   }

   /**
    * use static methods instead
    */
   @Deprecated
   public static BranchManager getInstance() {
      return instance;
   }

   public static BranchCache getCache() throws OseeCoreException {
      return ServiceUtil.getOseeCacheService().getBranchCache();
   }

   private static Branch commonBranch = null;

   /**
    * Since BranchManager is static and not a service yet, cache common branch cause it currently takes too long to get
    * service every single time this method is called.
    */
   public synchronized static Branch getCommonBranch() throws OseeCoreException {
      if (commonBranch == null) {
         Branch branch = getCache().getCommonBranch();
         Conditions.checkNotNull(branch, "Common Branch");
         commonBranch = branch;
      }
      return commonBranch;
   }

   public static List<Branch> getBranches(BranchArchivedState archivedState, BranchType... branchTypes) throws OseeCoreException {
      return getBranches(new BranchFilter(archivedState, branchTypes));
   }

   public static List<Branch> getBranches(BranchFilter branchFilter) throws OseeCoreException {
      return getCache().getBranches(branchFilter);
   }

   public static void refreshBranches() throws OseeCoreException {
      getCache().reloadCache();
   }

   public static Branch getBranch(DefaultBasicGuidArtifact guidArt) throws OseeCoreException {
      return BranchManager.getBranchByGuid(guidArt.getBranchGuid());
   }

   public static Branch getBranch(String branchName) throws OseeCoreException {
      Collection<Branch> branches = getBranchesByName(branchName);
      if (branches.isEmpty()) {
         throw new BranchDoesNotExist("No branch exists with the name: [%s]", branchName);
      }
      if (branches.size() > 1) {
         throw new MultipleBranchesExist("More than 1 branch exists with the name: [%s]", branchName);
      }
      return branches.iterator().next();
   }

   public static Collection<Branch> getBranchesByName(String branchName) throws OseeCoreException {
      return getCache().getByName(branchName);
   }

   public static int getBranchId(IOseeBranch branch) throws OseeCoreException {
      return getBranch(branch).getId();
   }

   public static Branch getBranch(IOseeBranch branch) throws OseeCoreException {
      if (branch instanceof Branch) {
         return (Branch) branch;
      } else {
         return getBranchByGuid(branch.getGuid());
      }
   }

   public static Branch getBranchByGuid(String guid) throws OseeCoreException {
      BranchCache cache = getCache();
      Branch branch = cache.getByGuid(guid);
      if (branch == null) {
         if (cache.reloadCache()) {
            branch = cache.getByGuid(guid);
         }
      }
      if (branch == null) {
         throw new BranchDoesNotExist("Branch with guid [%s] does not exist", guid);
      }
      return branch;
   }

   public static boolean branchExists(IOseeBranch branchToken) throws OseeCoreException {
      return getCache().get(branchToken) != null;
   }

   public static boolean branchExists(String branchGuid) throws OseeCoreException {
      return getCache().getByGuid(branchGuid) != null;
   }

   /**
    * returns the merge branch for this source destination pair from the cache or null if not found
    */
   public static MergeBranch getMergeBranch(Branch sourceBranch, Branch destinationBranch) throws OseeCoreException {
      return getMergeBranch(sourceBranch, destinationBranch, true);
   }

   public static MergeBranch getMergeBranch(Branch sourceBranch, Branch destinationBranch, boolean isReLoadAllowed) throws OseeCoreException {
      BranchCache cache = getCache();
      // If someone else made a branch on another machine, we may not know about it
      // so refresh the cache.
      MergeBranch mergeBranch = cache.findMergeBranch(sourceBranch, destinationBranch);
      if (OseeInfo.isCacheEnabled(MERGE_RELOAD_KEY)) {
         if (mergeBranch == null && isReLoadAllowed) {
            if (cache.reloadCache()) {
               mergeBranch = cache.findMergeBranch(sourceBranch, destinationBranch);
            }
         }
      }
      return mergeBranch;
   }

   public static boolean doesMergeBranchExist(Branch sourceBranch, Branch destBranch) throws OseeCoreException {
      return getMergeBranch(sourceBranch, destBranch, false) != null;
   }

   public static Branch getBranch(Integer branchId) throws OseeCoreException {
      if (branchId == null) {
         throw new BranchDoesNotExist("Branch Id is null");
      }

      BranchCache cache = getCache();
      // If someone else made a branch on another machine, we may not know about it
      // so refresh the cache.
      Branch branch = cache.getById(branchId);
      if (branch == null) {
         if (cache.reloadCache()) {
            branch = cache.getById(branchId);
         }
      }
      if (branch == null) {
         throw new BranchDoesNotExist("Branch could not be acquired for branch id %d", branchId);
      }
      return branch;
   }

   /**
    * Update branch
    */
   public static Job updateBranch(final Branch branch, final ConflictResolverOperation resolver) {
      IOperation operation = new UpdateBranchOperation(branch, resolver);
      return Operations.executeAsJob(operation, true);
   }

   /**
    * Completes the update branch operation by committing latest parent based branch with branch with changes. Then
    * swaps branches so we are left with the most current branch containing latest changes.
    */
   public static Job completeUpdateBranch(final ConflictManagerExternal conflictManager, final boolean archiveSourceBranch, final boolean overwriteUnresolvedConflicts) {
      IOperation operation =
         new FinishUpdateBranchOperation(conflictManager, archiveSourceBranch, overwriteUnresolvedConflicts);
      return Operations.executeAsJob(operation, true);
   }

   public static void purgeBranch(final IOseeBranch branch) throws OseeCoreException {
      Operations.executeWorkAndCheckStatus(new PurgeBranchHttpRequestOperation(branch, false));
   }

   public static void updateBranchType(IProgressMonitor monitor, final int branchId, String branchGuid, final BranchType type) throws OseeCoreException {
      IOperation operation = new UpdateBranchTypeHttpRequestOperation(branchId, branchGuid, type);
      Operations.executeWorkAndCheckStatus(operation, monitor);
   }

   public static void updateBranchState(IProgressMonitor monitor, final int branchId, String branchGuid, final BranchState state) throws OseeCoreException {
      IOperation operation = new UpdateBranchStateHttpRequestOperation(branchId, branchGuid, state);
      Operations.executeWorkAndCheckStatus(operation, monitor);
   }

   public static void updateBranchArchivedState(IProgressMonitor monitor, final int branchId, String branchGuid, final BranchArchivedState state) throws OseeCoreException {
      IOperation operation = new UpdateBranchArchivedStateHttpRequestOperation(branchId, branchGuid, state);
      Operations.executeWorkAndCheckStatus(operation, monitor);
   }

   /**
    * Delete a branch from the system. (This operation will set the branch state to deleted. This operation is
    * undo-able)
    */
   public static Job deleteBranch(final IOseeBranch branch) {
      return Operations.executeAsJob(new DeleteBranchOperation(branch), true);
   }

   public static IStatus deleteBranchAndPend(final IOseeBranch branch) {
      return Operations.executeWork(new DeleteBranchOperation(branch));
   }

   /**
    * Delete branches from the system. (sets branch state to deleted. operation is undo-able)
    * 
    * @throws OseeCoreException
    */
   public static Job deleteBranch(final List<? extends IOseeBranch> branches) {
      List<IOperation> ops = new ArrayList<IOperation>();
      for (IOseeBranch branch : branches) {
         ops.add(new DeleteBranchOperation(branch));
      }
      return Operations.executeAsJob(new CompositeOperation("Deleting multiple branches...", Activator.PLUGIN_ID, ops),
         true);
   }

   /**
    * Commit the net changes from the source branch into the destination branch. If there are conflicts between the two
    * branches, the source branch changes will override those on the destination branch.
    */
   public static void commitBranch(IProgressMonitor monitor, ConflictManagerExternal conflictManager, boolean archiveSourceBranch, boolean overwriteUnresolvedConflicts) throws OseeCoreException {
      if (monitor == null) {
         monitor = new NullProgressMonitor();
      }
      if (conflictManager.remainingConflictsExist() && !overwriteUnresolvedConflicts) {
         throw new OseeCoreException("Commit failed due to unresolved conflicts");
      }
      if (!conflictManager.getDestinationBranch().isEditable()) {
         throw new OseeCoreException("Commit failed - unable to commit into a non-editable branch");
      }

      boolean skipCommitChecksAndEvents = OseeClientProperties.isSkipCommitChecksAndEvents();
      if (!skipCommitChecksAndEvents) {
         runCommitExtPointActions(conflictManager);
      }

      IOperation operation =
         new CommitBranchHttpRequestOperation(UserManager.getUser(), conflictManager.getSourceBranch(),
            conflictManager.getDestinationBranch(), archiveSourceBranch, skipCommitChecksAndEvents);
      Operations.executeWorkAndCheckStatus(operation, monitor);
   }

   private static void runCommitExtPointActions(ConflictManagerExternal conflictManager) throws OseeCoreException {
      ExtensionDefinedObjects<CommitAction> extensions =
         new ExtensionDefinedObjects<CommitAction>("org.eclipse.osee.framework.skynet.core.CommitActions",
            "CommitActions", "className");
      for (CommitAction commitAction : extensions.getObjects()) {
         commitAction.runCommitAction(conflictManager.getSourceBranch(), conflictManager.getDestinationBranch());
      }
   }

   /**
    * Calls the getMergeBranch method and if it returns null it will create a new merge branch based on the artIds from
    * the source branch.
    */
   public static Branch getOrCreateMergeBranch(Branch sourceBranch, Branch destBranch, ArrayList<Integer> expectedArtIds) throws OseeCoreException {
      MergeBranch mergeBranch = getMergeBranch(sourceBranch, destBranch);
      if (mergeBranch == null) {
         mergeBranch = createMergeBranch(sourceBranch, destBranch, expectedArtIds);
      } else {
         UpdateMergeBranch dbTransaction = new UpdateMergeBranch(mergeBranch, expectedArtIds, destBranch, sourceBranch);
         dbTransaction.execute();
      }
      return mergeBranch;
   }

   private static MergeBranch createMergeBranch(final Branch sourceBranch, final Branch destBranch, final ArrayList<Integer> expectedArtIds) throws OseeCoreException {
      Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();
      int mergeAddressingQueryId = ArtifactLoader.getNewQueryId();
      List<Object[]> datas = new LinkedList<Object[]>();
      for (int artId : expectedArtIds) {
         datas.add(new Object[] {mergeAddressingQueryId, insertTime, artId, sourceBranch.getId(), SQL3DataType.INTEGER});
      }
      MergeBranch mergeBranch = null;
      try {
         ArtifactLoader.insertIntoArtifactJoin(datas);

         int parentTxId = sourceBranch.getBaseTransaction().getId();
         String creationComment =
            String.format("New Merge Branch from %s(%s) and %s", sourceBranch.getName(), parentTxId,
               destBranch.getName());
         String branchName = "Merge " + sourceBranch.getShortName() + " <=> " + destBranch.getShortName();
         mergeBranch =
            (MergeBranch) createBranch(BranchType.MERGE, sourceBranch.getBaseTransaction(), branchName, null,
               UserManager.getUser(), creationComment, mergeAddressingQueryId, destBranch.getId());
         mergeBranch.setSourceBranch(sourceBranch);
         mergeBranch.setDestinationBranch(destBranch);
      } finally {
         ArtifactLoader.clearQuery(mergeAddressingQueryId);
      }
      return mergeBranch;
   }

   /**
    * Creates a new Branch based on the transaction number selected and the parent branch.
    */
   public static Branch createWorkingBranch(TransactionRecord parentTransactionId, String childBranchName, String childBranchGuid) throws OseeCoreException {
      return createWorkingBranch(parentTransactionId, childBranchName, childBranchGuid,
         UserManager.getUser(SystemUser.OseeSystem));
   }

   public static Branch createWorkingBranch(TransactionRecord parentTransactionId, String childBranchName, String childBranchGuid, Artifact associatedArtifact) throws OseeCoreException {
      String creationComment =
         String.format("New Branch from %s (%s)", parentTransactionId.getBranch().getName(),
            parentTransactionId.getId());

      final String truncatedName = Strings.truncate(childBranchName, 195, true);
      return createBranch(BranchType.WORKING, parentTransactionId, truncatedName, childBranchGuid, associatedArtifact,
         creationComment, -1, -1);
   }

   /**
    * Creates a new Branch based on the most recent transaction on the parent branch.
    */
   public static Branch createWorkingBranchFromTx(TransactionRecord parentTransactionId, String childBranchName) throws OseeCoreException {
      String creationComment =
         String.format("New Branch created by copying prior tx and %s (%s)", parentTransactionId.getBranch().getName(),
            parentTransactionId.getId());

      final String truncatedName = Strings.truncate(childBranchName, 195, true);

      CreateBranchHttpRequestOperation operation =
         new CreateBranchHttpRequestOperation(BranchType.WORKING, parentTransactionId, truncatedName, null, null,
            creationComment, -1, -1);
      operation.setTxCopyBranchType(true);
      Operations.executeWorkAndCheckStatus(operation);
      return operation.getNewBranch();
   }

   public static Branch createWorkingBranch(IOseeBranch parentBranch, String childBranchName) throws OseeCoreException {
      return createWorkingBranch(parentBranch, childBranchName, UserManager.getUser(SystemUser.OseeSystem));
   }

   public static Branch createWorkingBranch(IOseeBranch parentBranch, String childBranchName, Artifact associatedArtifact) throws OseeCoreException {
      Conditions.checkNotNull(parentBranch, "Parent Branch");
      Conditions.checkNotNull(childBranchName, "Child Branch Name");
      Conditions.checkNotNull(associatedArtifact, "Associated Artifact");
      TransactionRecord parentTransactionId = TransactionManager.getHeadTransaction(parentBranch);
      return createWorkingBranch(parentTransactionId, childBranchName, null, associatedArtifact);
   }

   public static Branch createWorkingBranch(IOseeBranch parentBranch, IOseeBranch childBranch) throws OseeCoreException {
      return createWorkingBranch(parentBranch, childBranch, UserManager.getUser(SystemUser.OseeSystem));
   }

   public static Branch createWorkingBranch(IOseeBranch parentBranch, IOseeBranch childBranch, Artifact associatedArtifact) throws OseeCoreException {
      TransactionRecord parentTransactionId = TransactionManager.getHeadTransaction(parentBranch);
      return createWorkingBranch(parentTransactionId, childBranch.getName(), childBranch.getGuid(), associatedArtifact);
   }

   /**
    * Creates a new Branch based on the most recent transaction on the parent branch.
    */
   public static Branch createBaselineBranch(IOseeBranch parentBranch, IOseeBranch childBranch) throws OseeCoreException {
      return createBaselineBranch(parentBranch, childBranch, UserManager.getUser(SystemUser.OseeSystem));
   }

   public static Branch createBaselineBranch(IOseeBranch parentBranch, IOseeBranch childBranch, Artifact associatedArtifact) throws OseeCoreException {
      TransactionRecord parentTransactionId = TransactionManager.getHeadTransaction(parentBranch);
      String creationComment = String.format("Branch Creation for %s", childBranch.getName());
      return createBranch(BranchType.BASELINE, parentTransactionId, childBranch.getName(), childBranch.getGuid(),
         associatedArtifact, creationComment, -1, -1);
   }

   private static Branch createBranch(BranchType branchType, TransactionRecord parentTransaction, String branchName, String branchGuid, Artifact associatedArtifact, String creationComment, int mergeAddressingQueryId, int destinationBranchId) throws OseeCoreException {
      CreateBranchHttpRequestOperation operation =
         new CreateBranchHttpRequestOperation(branchType, parentTransaction, branchName, branchGuid,
            associatedArtifact, creationComment, mergeAddressingQueryId, destinationBranchId);
      Operations.executeWorkAndCheckStatus(operation);
      return operation.getNewBranch();
   }

   /**
    * Creates a new root branch, imports skynet types and initializes.
    * 
    * @param initializeArtifacts adds common artifacts needed by most normal root branches
    */
   public static Branch createTopLevelBranch(IOseeBranch branch) throws OseeCoreException {
      return createBaselineBranch(CoreBranches.SYSTEM_ROOT, branch, null);
   }

   public static Branch createTopLevelBranch(final String branchName) throws OseeCoreException {
      IOseeBranch branchToken = TokenFactory.createBranch(GUID.create(), branchName);
      return createTopLevelBranch(branchToken);
   }

   public static List<Branch> getBaselineBranches() throws OseeCoreException {
      return getBranches(BranchArchivedState.UNARCHIVED, BranchType.BASELINE);
   }

   private void initializeLastBranchValue() {
      try {
         String branchGuid = UserManager.getSetting(LAST_DEFAULT_BRANCH);
         lastBranch = getBranchByGuid(branchGuid);
      } catch (Exception ex) {
         try {
            lastBranch = getDefaultInitialBranch();
            UserManager.setSetting(LAST_DEFAULT_BRANCH, lastBranch.getGuid());
         } catch (OseeCoreException ex1) {
            OseeLog.log(Activator.class, Level.SEVERE, ex1);
         }
      }
   }

   private Branch getDefaultInitialBranch() throws OseeCoreException {
      ExtensionDefinedObjects<IDefaultInitialBranchesProvider> extensions =
         new ExtensionDefinedObjects<IDefaultInitialBranchesProvider>(
            "org.eclipse.osee.framework.skynet.core.DefaultInitialBranchProvider", "DefaultInitialBranchProvider",
            "class", true);
      for (IDefaultInitialBranchesProvider provider : extensions.getObjects()) {
         try {
            // Guard against problematic extensions
            for (Branch branch : provider.getDefaultInitialBranches()) {
               if (branch != null) {
                  return branch;
               }
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.WARNING,
               "Exception occurred while trying to determine initial default branch", ex);
         }
      }
      return getCommonBranch();
   }

   public static IOseeBranch getLastBranch() {
      if (instance.lastBranch == null) {
         instance.initializeLastBranchValue();
      }
      return instance.lastBranch;
   }

   public static void setLastBranch(Branch branch) {
      if (branch != null) {
         instance.lastBranch = branch;
      }
   }

   public static Branch getSystemRootBranch() throws OseeCoreException {
      return getCache().getSystemRootBranch();
   }

   public static void persist(Branch... branches) throws OseeCoreException {
      getCache().storeItems(Arrays.asList(branches));
   }

   public static void persist(Collection<Branch> branches) throws OseeCoreException {
      getCache().storeItems(branches);
   }

   public static void decache(Branch branch) throws OseeCoreException {
      getCache().decache(branch);
   }

   public static boolean hasChanges(Branch branch) throws OseeCoreException {
      return branch.getBaseTransaction() != TransactionManager.getHeadTransaction(branch);
   }

   public static boolean isChangeManaged(Branch branch) throws OseeCoreException {
      // TODO use Associated Artifacts
      int systemUserArtId = UserManager.getUser(SystemUser.OseeSystem).getArtId();

      int assocArtId = branch.getAssociatedArtifactId();
      return assocArtId > 0 && assocArtId != systemUserArtId;
   }

   public static Artifact getAssociatedArtifact(Branch branch) throws OseeCoreException {
      if (branch.getAssociatedArtifactId() == null || branch.getAssociatedArtifactId() == -1) {
         return UserManager.getUser(SystemUser.OseeSystem);
      }
      return ArtifactQuery.getArtifactFromId(branch.getAssociatedArtifactId(), BranchManager.getCommonBranch());
   }

   public static Artifact getAssociatedArtifact(TransactionDelta txDelta) throws OseeCoreException {
      Artifact associatedArtifact = null;
      if (txDelta.areOnTheSameBranch()) {
         TransactionRecord txRecord = txDelta.getEndTx();
         int commitArtId = txRecord.getCommit();
         if (commitArtId != 0) {
            associatedArtifact = ArtifactQuery.getArtifactFromId(commitArtId, BranchManager.getCommonBranch());
         }
      } else {
         Branch sourceBranch = txDelta.getStartTx().getBranch();
         associatedArtifact = BranchManager.getAssociatedArtifact(sourceBranch);
      }
      return associatedArtifact;
   }

   public static void invalidateBranches() throws OseeCoreException {
      getCache().invalidate();
   }
}