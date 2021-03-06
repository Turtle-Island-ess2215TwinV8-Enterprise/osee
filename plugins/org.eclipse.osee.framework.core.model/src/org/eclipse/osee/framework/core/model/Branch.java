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

package org.eclipse.osee.framework.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.StorageState;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.BranchFilter;
import org.eclipse.osee.framework.core.model.internal.fields.AssociatedArtifactField;
import org.eclipse.osee.framework.core.model.internal.fields.CollectionField;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Roberto E. Escobar
 */
public class Branch extends AbstractOseeType<String> implements WriteableBranch, IAdaptable {

   private static final int SHORT_NAME_LIMIT = 35;

   private final Collection<Branch> childBranches = new HashSet<Branch>();

   public Branch(String guid, String name, BranchType branchType, BranchState branchState, boolean isArchived) {
      super(guid, name);
      initializeFields();
      setFieldLogException(BranchField.BRANCH_TYPE_FIELD_KEY, branchType);
      setFieldLogException(BranchField.BRANCH_STATE_FIELD_KEY, branchState);
      setFieldLogException(BranchField.BRANCH_ARCHIVED_STATE_FIELD_KEY, BranchArchivedState.fromBoolean(isArchived));
   }

   protected void initializeFields() {
      addField(BranchField.PARENT_BRANCH, new OseeField<Branch>());
      addField(BranchField.BRANCH_BASE_TRANSACTION, new OseeField<TransactionRecord>());
      addField(BranchField.BRANCH_SOURCE_TRANSACTION, new OseeField<TransactionRecord>());
      addField(BranchField.BRANCH_TYPE_FIELD_KEY, new OseeField<BranchType>());
      addField(BranchField.BRANCH_STATE_FIELD_KEY, new OseeField<BranchState>());
      addField(BranchField.BRANCH_ARCHIVED_STATE_FIELD_KEY, new OseeField<BranchArchivedState>());

      addField(BranchField.BRANCH_ASSOCIATED_ARTIFACT_ID_FIELD_KEY, new AssociatedArtifactField(null));
      addField(BranchField.BRANCH_CHILDREN, new CollectionField<Branch>(childBranches));
   }

   @Override
   public Branch getParentBranch() throws OseeCoreException {
      return getFieldValue(BranchField.PARENT_BRANCH);
   }

   public void internalRemovePurgedBranchFromParent() throws OseeCoreException {
      if (hasParentBranch()) {
         Branch parentBranch = getParentBranch();
         Iterator<Branch> siblings = parentBranch.childBranches.iterator();

         while (siblings.hasNext()) {
            Branch sibling = siblings.next();
            if (sibling.isPurged() && sibling.equals(this)) {
               siblings.remove();
               break;
            }
         }
      }
   }

   @Override
   public boolean hasParentBranch() throws OseeCoreException {
      return getParentBranch() != null;
   }

   @Override
   public String getShortName() {
      return getShortName(this);
   }

   public static String getShortName(IOseeBranch branch) {
      return Strings.truncate(branch.getName(), SHORT_NAME_LIMIT);
   }

   @Override
   public BranchType getBranchType() {
      return getFieldValueLogException(null, BranchField.BRANCH_TYPE_FIELD_KEY);
   }

   @Override
   public BranchState getBranchState() {
      return getFieldValueLogException(null, BranchField.BRANCH_STATE_FIELD_KEY);
   }

   @Override
   public BranchArchivedState getArchiveState() {
      return getFieldValueLogException(null, BranchField.BRANCH_ARCHIVED_STATE_FIELD_KEY);
   }

   @Override
   public Integer getAssociatedArtifactId() throws OseeCoreException {
      return getFieldValue(BranchField.BRANCH_ASSOCIATED_ARTIFACT_ID_FIELD_KEY);
   }

   public void setAssociatedArtifactId(Integer artId) throws OseeCoreException {
      setField(BranchField.BRANCH_ASSOCIATED_ARTIFACT_ID_FIELD_KEY, artId);
   }

   @Override
   public TransactionRecord getBaseTransaction() throws OseeCoreException {
      return getFieldValue(BranchField.BRANCH_BASE_TRANSACTION);
   }

   @Override
   public TransactionRecord getSourceTransaction() throws OseeCoreException {
      return getFieldValue(BranchField.BRANCH_SOURCE_TRANSACTION);
   }

   public void setArchived(boolean isArchived) {
      BranchArchivedState newValue = BranchArchivedState.fromBoolean(isArchived);
      setFieldLogException(BranchField.BRANCH_ARCHIVED_STATE_FIELD_KEY, newValue);
   }

   public void setBranchState(BranchState branchState) {
      setFieldLogException(BranchField.BRANCH_STATE_FIELD_KEY, branchState);
   }

   public void setBranchType(BranchType branchType) {
      setFieldLogException(BranchField.BRANCH_TYPE_FIELD_KEY, branchType);
   }

   public void setParentBranch(Branch parentBranch) throws OseeCoreException {
      Branch oldParent = getParentBranch();
      if (oldParent != null) {
         oldParent.childBranches.remove(this);
      }
      setField(BranchField.PARENT_BRANCH, parentBranch);
      if (parentBranch != null) {
         parentBranch.childBranches.add(this);
      }
   }

   public void setBaseTransaction(TransactionRecord baseTx) throws OseeCoreException {
      setField(BranchField.BRANCH_BASE_TRANSACTION, baseTx);
   }

   public void setSourceTransaction(TransactionRecord srcTx) throws OseeCoreException {
      setField(BranchField.BRANCH_SOURCE_TRANSACTION, srcTx);
   }

   @Override
   public boolean isEditable() {
      BranchState state = getBranchState();
      return (state.isCreated() || state.isModified() || state.isRebaselineInProgress()) && getArchiveState().isUnArchived();
   }

   public boolean isCreated() {
      return getStorageState() == StorageState.CREATED;
   }

   public boolean isModified() {
      return getStorageState() == StorageState.MODIFIED;
   }

   public boolean isPurged() {
      return getStorageState() == StorageState.PURGED;
   }

   public boolean isDeleted() {
      return getBranchState() == BranchState.DELETED;
   }

   public boolean isDeleteInProgress() {
      return getBranchState() == BranchState.DELETE_IN_PROGRESS;
   }

   public boolean isPurgeInProgress() {
      return getBranchState() == BranchState.PURGE_IN_PROGRESS;
   }

   @Override
   public String toString() {
      return getName();
   }

   public Collection<Branch> getChildren() throws OseeCoreException {
      return getFieldValue(BranchField.BRANCH_CHILDREN);
   }

   public Branch getAccessControlBranch() {
      return this;
   }

   @Override
   public Collection<Branch> getChildBranches() throws OseeCoreException {
      return getChildBranches(false);
   }

   /**
    * @param recurse if true all descendants are processed, otherwise, only direct descendants are.
    * @return all unarchived child branches that are not of type merge
    * @throws OseeCoreException
    */
   @Override
   public Collection<Branch> getChildBranches(boolean recurse) throws OseeCoreException {
      Set<Branch> children = new HashSet<Branch>();
      BranchFilter filter = new BranchFilter(BranchArchivedState.UNARCHIVED);
      filter.setNegatedBranchTypes(BranchType.MERGE);

      getChildBranches(children, recurse, filter);
      return children;
   }

   /**
    * @return all child branches. It is equivalent to calling getChildBranches with new BranchFilter() (.i.e no child
    * branches are excluded)
    * @throws OseeCoreException
    */
   @Override
   public Collection<Branch> getAllChildBranches(boolean recurse) throws OseeCoreException {
      Set<Branch> children = new HashSet<Branch>();
      getChildBranches(children, recurse, new BranchFilter());
      return children;
   }

   @Override
   public void getChildBranches(Collection<? extends ReadableBranch> children, boolean recurse, BranchFilter filter) throws OseeCoreException {
      uncheckedGetChildBranches(children, recurse, filter);
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private void uncheckedGetChildBranches(Collection children, boolean recurse, BranchFilter filter) throws OseeCoreException {
      for (Branch branch : getChildren()) {
         if (filter.matches(branch)) {
            children.add(branch);
            if (recurse) {
               branch.getChildBranches(children, recurse, filter);
            }
         }
      }
   }

   @Override
   public Collection<Branch> getAncestors() throws OseeCoreException {
      List<Branch> ancestors = new ArrayList<Branch>();
      Branch branchCursor = this;
      ancestors.add(branchCursor);
      while (branchCursor.hasParentBranch()) {
         branchCursor = branchCursor.getParentBranch();
         ancestors.add(branchCursor);
      }
      return ancestors;
   }

   @SuppressWarnings("rawtypes")
   @Override
   public Object getAdapter(Class adapter) {
      if (adapter == null) {
         throw new IllegalArgumentException("adapter can not be null");
      }

      if (adapter.isInstance(this)) {
         return this;
      }
      return null;
   }

   @Override
   public boolean isAncestorOf(IOseeBranch branch) throws OseeCoreException {
      return getChildBranches(true).contains(branch);
   }

   /*
    * Provide easy way to display/report [guid][name]
    */
   public final String toStringWithId() {
      return String.format("[%s][%s]", getGuid(), getName());
   }

}