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

import java.util.Date;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.osee.framework.core.data.BaseIdentity;
import org.eclipse.osee.framework.core.data.ITransaction;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Jeff C. Phillips
 */
public class TransactionRecord extends BaseIdentity<Integer> implements ITransaction, IAdaptable {
   private static final int NON_EXISTING_BRANCH = -1;
   private final TransactionDetailsType txType;

   private final int branchId;
   private String comment;
   private Date time;
   private int authorArtId;
   private int commitArtId;
   private final BranchCache branchCache;

   public TransactionRecord(int transactionNumber, int branchId, String comment, Date time, int authorArtId, int commitArtId, TransactionDetailsType txType, BranchCache branchCache) {
      super(transactionNumber);
      this.branchId = branchId;
      this.comment = Strings.intern(comment);
      this.time = time;
      this.authorArtId = authorArtId;
      this.commitArtId = commitArtId;
      this.txType = txType;
      this.branchCache = branchCache;
   }

   public TransactionRecord(int transactionNumber, BranchCache branchCache) {
      this(transactionNumber, NON_EXISTING_BRANCH, "INVALID", new Date(0), -1, -1, TransactionDetailsType.INVALID,
         branchCache);
   }

   public boolean exists() {
      return branchId != NON_EXISTING_BRANCH;
   }

   public int getBranchId() {
      return branchId;
   }

   public Branch getBranch() throws OseeCoreException {
      Conditions.checkNotNull(branchCache, "BranchCache was not set after construction");
      return branchCache.getById(getBranchId());
   }

   public int getId() {
      return getGuid();
   }

   public String getComment() {
      return comment;
   }

   public Date getTimeStamp() {
      return time;
   }

   public int getAuthor() {
      return authorArtId;
   }

   public int getCommit() {
      return commitArtId;
   }

   public TransactionDetailsType getTxType() {
      return txType;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public void setTimeStamp(Date time) {
      this.time = time;
   }

   public void setAuthor(int authorArtId) {
      this.authorArtId = authorArtId;
   }

   public void setCommit(int commitArtId) {
      this.commitArtId = commitArtId;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ITransaction) {
         ITransaction other = (ITransaction) obj;
         return super.equals(other);
      }
      return false;
   }

   @SuppressWarnings("rawtypes")
   @Override
   public Object getAdapter(Class adapter) {
      if (adapter != null && getClass().isAssignableFrom(adapter)) {
         return this;
      }
      return null;
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

   @Override
   public String toString() {
      try {
         return String.format("%s (%s:%s)", getBranch(), getGuid(), getBranchId());
      } catch (OseeCoreException ex) {
         return String.format("%s:%s", getGuid(), getBranchId());
      }
   }

   public boolean isDirty() {
      return false;
   }

   public void clearDirty() {
      //
   }

   public boolean isIdValid() {
      return getId() > 0;
   }
}