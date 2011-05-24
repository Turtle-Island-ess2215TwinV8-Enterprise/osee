/*
 * Created on May 11, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.util;

import java.util.Collection;
import org.eclipse.osee.ats.core.type.AtsArtifactTypes;
import org.eclipse.osee.ats.core.type.AtsAttributeTypes;
import org.eclipse.osee.ats.core.type.AtsRelationTypes;
import org.eclipse.osee.ats.core.workdef.RuleDefinitionOption;
import org.eclipse.osee.ats.core.workdef.StateDefinition;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.workflow.StateManager;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Donald G. Dunne
 */
public class WorkflowManagerCore {

   public static StateManager getStateManager(Artifact artifact) {
      return cast(artifact).getStateMgr();
   }

   public static AbstractWorkflowArtifact cast(Artifact artifact) {
      if (artifact instanceof AbstractWorkflowArtifact) {
         return (AbstractWorkflowArtifact) artifact;
      }
      return null;
   }

   public static boolean isEditable(AbstractWorkflowArtifact sma, StateDefinition stateDef, boolean priviledgedEditEnabled) throws OseeCoreException {
      // must be writeable
      return !sma.isReadOnly() &&
      // and access control writeable
      sma.isAccessControlWrite() &&
      // and current state
      (stateDef == null || sma.isInState(stateDef)) &&
      // and one of these
      //
      // page is define to allow anyone to edit
      (sma.getStateDefinition().hasRule(RuleDefinitionOption.AllowEditToAll) ||
      // team definition has allowed anyone to edit
      sma.teamDefHasRule(RuleDefinitionOption.AllowEditToAll) ||
      // priviledged edit mode is on
      priviledgedEditEnabled ||
      // current user is assigned
      sma.isAssigneeMe() ||
      // current user is ats admin
      AtsUtilCore.isAtsAdmin());
   }

   public static AbstractWorkflowArtifact getParentAWA(Artifact artifact) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.Task)) {
         Collection<Artifact> awas = artifact.getRelatedArtifacts(AtsRelationTypes.SmaToTask_Sma);
         if (awas.isEmpty()) {
            throw new OseeStateException("Task has no parent [%s]", artifact.getHumanReadableId());
         }
         return (AbstractWorkflowArtifact) awas.iterator().next();
      } else if (artifact.isOfType(AtsArtifactTypes.ReviewArtifact)) {
         Collection<Artifact> awas = artifact.getRelatedArtifacts(AtsRelationTypes.TeamWorkflowToReview_Team);
         if (!awas.isEmpty()) {
            return (AbstractWorkflowArtifact) awas.iterator().next();
         }
      }
      return null;
   }

   public static Artifact getTeamDefinition(Artifact artifact) throws OseeCoreException {
      Artifact team = getParentTeamWorkflow(artifact);
      if (team != null) {
         String teamDefGuid = team.getSoleAttributeValue(AtsAttributeTypes.TeamDefinition);
         return ArtifactQuery.getArtifactFromId(teamDefGuid, BranchManager.getCommonBranch());
      }
      return null;
   }

   public static Artifact getParentActionArtifact(Artifact artifact) throws OseeCoreException {
      Artifact team = getParentTeamWorkflow(artifact);
      if (team != null) {
         return artifact.getRelatedArtifact(AtsRelationTypes.ActionToWorkflow_Action);
      }
      return null;
   }

   public static Artifact getParentTeamWorkflow(Artifact artifact) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.TeamWorkflow)) {
         return artifact;
      } else if (artifact.isOfType(AtsArtifactTypes.Task)) {
         return getParentAWA(artifact);
      } else if (artifact.isOfType(AtsArtifactTypes.ReviewArtifact)) {
         return getParentAWA(artifact);
      }
      return null;
   }

}