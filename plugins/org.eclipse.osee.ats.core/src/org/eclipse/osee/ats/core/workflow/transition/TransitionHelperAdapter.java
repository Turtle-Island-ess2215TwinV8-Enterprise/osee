/*
 * Created on Jun 6, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.workflow.transition;

import org.eclipse.osee.ats.core.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.UserManager;

/**
 * @author Donald G. Dunne
 */
public abstract class TransitionHelperAdapter implements ITransitionHelper {

   @Override
   public boolean isPriviledgedEditEnabled() {
      return false;
   }

   @Override
   public boolean isOverrideTransitionValidityCheck() {
      return false;
   }

   @Override
   public boolean isOverrideAssigneeCheck() {
      return false;
   }

   @Override
   public boolean isWorkingBranchInWork(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      return AtsBranchManagerCore.isWorkingBranchInWork(teamArt);
   }

   @Override
   public boolean isBranchInCommit(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      return AtsBranchManagerCore.isBranchInCommit(teamArt);
   }

   @Override
   public boolean isSystemUser() throws OseeCoreException {
      return UserManager.getUser(SystemUser.Guest) == UserManager.getUser() || UserManager.getUser(SystemUser.OseeSystem) == UserManager.getUser();
   }

   @Override
   public boolean isSystemUserAssingee(AbstractWorkflowArtifact awa) throws OseeCoreException {
      return awa.getStateMgr().getAssignees().contains(UserManager.getUser(SystemUser.Guest)) || awa.getStateMgr().getAssignees().contains(
         UserManager.getUser(SystemUser.OseeSystem));
   }

}