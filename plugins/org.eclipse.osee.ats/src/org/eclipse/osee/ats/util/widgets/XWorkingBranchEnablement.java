/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.util.widgets;

import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.widgets.XWorkingBranch.BranchStatus;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;

public class XWorkingBranchEnablement {
   boolean populated = false;
   boolean workingBranchCreationInProgress = false;
   boolean workingBranchCommitInProgress = false;
   boolean workingBranchInWork = false;
   boolean committedBranchExists = false;
   boolean disableAll = false;
   Branch workingBranch = null;
   private final TeamWorkFlowArtifact teamArt;

   public XWorkingBranchEnablement(TeamWorkFlowArtifact teamArt) {
      this.teamArt = teamArt;
   }

   public boolean isCreateBranchButtonEnabled() {
      return !disableAll && ensurePopulatedLogError() && !workingBranchCommitInProgress && !workingBranchCreationInProgress && !workingBranchInWork && !committedBranchExists;
   }

   public boolean isShowArtifactExplorerButtonEnabled() {
      return !disableAll && ensurePopulatedLogError() && workingBranch != null && getStatus().changesPermitted;
   }

   public boolean isShowChangeReportButtonEnabled() {
      return !disableAll && ensurePopulatedLogError() && (workingBranchInWork || committedBranchExists);
   }

   public boolean isDeleteBranchButtonEnabled() {
      return !disableAll && ensurePopulatedLogError() && workingBranchInWork && !committedBranchExists;
   }

   public boolean isFavoriteBranchButtonEnabled() {
      return !disableAll && ensurePopulatedLogError() && workingBranchInWork;
   }

   public synchronized void refresh() {
      populated = false;
      disableAll = false;
   }

   public BranchStatus getStatus() {
      ensurePopulatedLogError();
      if (teamArt != null) {
         if (workingBranchCreationInProgress) {
            return BranchStatus.Changes_NotPermitted__CreationInProgress;
         } else if (workingBranchCommitInProgress) {
            return BranchStatus.Changes_NotPermitted__CommitInProgress;
         } else if (committedBranchExists) {
            return BranchStatus.Changes_NotPermitted__BranchCommitted;
         } else if (workingBranchInWork) {
            return BranchStatus.Changes_InProgress;
         }
      }
      return BranchStatus.Not_Started;
   }

   public Branch getWorkingBranch() {
      return workingBranch;
   }

   public void disableAll() {
      disableAll = true;
   }

   @Override
   public String toString() {
      return String.format(
         "disableAll [%s] CreateInProgress [%s] CommitInProgress [%s] InWorkBranch [%s] CommittedBranch [%s] Branch [%s]",
         disableAll, workingBranchCreationInProgress, workingBranchCommitInProgress, workingBranchInWork,
         committedBranchExists, workingBranch);
   }

   private synchronized void ensurePopulated() throws OseeCoreException {
      if (!populated) {
         workingBranch = AtsBranchManagerCore.getWorkingBranch(teamArt, true);
         workingBranchCreationInProgress =
            teamArt.isWorkingBranchCreationInProgress() || (workingBranch != null && workingBranch.getBranchState() == BranchState.CREATION_IN_PROGRESS);
         workingBranchCommitInProgress =
            teamArt.isWorkingBranchCommitInProgress() || workingBranch != null && workingBranch.getBranchState() == BranchState.COMMIT_IN_PROGRESS;
         workingBranchInWork = AtsBranchManagerCore.isWorkingBranchInWork(teamArt);
         committedBranchExists = AtsBranchManagerCore.isCommittedBranchExists(teamArt);
         disableAll = workingBranchCommitInProgress;
         populated = true;
      }
   }

   private boolean ensurePopulatedLogError() {
      boolean attemptedToPopulate = false;
      try {
         ensurePopulated();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      } finally {
         attemptedToPopulate = true;
      }
      return attemptedToPopulate;
   }
}
