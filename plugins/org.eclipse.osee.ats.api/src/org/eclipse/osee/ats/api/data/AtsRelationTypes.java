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
package org.eclipse.osee.ats.api.data;

import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.RelationSide;

/**
 * @author Donald G. Dunne
 */
public final class AtsRelationTypes {

   //@formatter:off
   public static final IRelationTypeSide ActionToWorkflow_Action = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x200000000000016DL, "ActionToWorkflow");
   public static final IRelationTypeSide ActionToWorkflow_WorkFlow = ActionToWorkflow_Action.getOpposite();
   
   public static final IRelationTypeSide Port_From = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x200000000000017AL, "Port");
   public static final IRelationTypeSide Port_To = Port_From.getOpposite();
   
   public static final IRelationTypeSide Derive_From = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x200000000000017BL, "Derive");
   public static final IRelationTypeSide Derive_To = Derive_From.getOpposite();
   
   public static final IRelationTypeSide FavoriteUser_Artifact = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x2000000000000173L, "FavoriteUser");
   public static final IRelationTypeSide FavoriteUser_User = FavoriteUser_Artifact.getOpposite();
   
   public static final IRelationTypeSide Goal_Goal = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x2000000000000175L, "Goal");
   public static final IRelationTypeSide Goal_Member = Goal_Goal.getOpposite();
  
   public static final IRelationTypeSide ParallelVersion_Parent = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x2000000000000174L, "ParallelVersion");   
   public static final IRelationTypeSide ParallelVersion_Child = ParallelVersion_Parent.getOpposite();
   
   public static final IRelationTypeSide PrivilegedMember_Team = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x200000000000016BL, "PrivilegedMember");
   public static final IRelationTypeSide PrivilegedMember_Member = PrivilegedMember_Team.getOpposite();
   
   public static final IRelationTypeSide TeamWfToTask_TeamWf = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x200000000000016EL, "TeamWfToTask");
   public static final IRelationTypeSide TeamWfToTask_Task = TeamWfToTask_TeamWf.getOpposite();
   
   public static final IRelationTypeSide SubscribedUser_Artifact = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x2000000000000172L, "SubscribedUser");
   public static final IRelationTypeSide SubscribedUser_User = SubscribedUser_Artifact.getOpposite();

   public static final IRelationTypeSide TeamActionableItem_Team = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x200000000000016CL, "TeamActionableItem");
   public static final IRelationTypeSide TeamActionableItem_ActionableItem = TeamActionableItem_Team.getOpposite();
   
   public static final IRelationTypeSide TeamDefinitionToVersion_TeamDefinition = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x2000000000000170L, "TeamDefinitionToVersion");
   public static final IRelationTypeSide TeamDefinitionToVersion_Version = TeamDefinitionToVersion_TeamDefinition.getOpposite();
   
   public static final IRelationTypeSide TeamLead_Team = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x2000000000000169L, "TeamLead");
   public static final IRelationTypeSide TeamLead_Lead = TeamLead_Team.getOpposite();
   
   public static final IRelationTypeSide TeamMember_Team = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x200000000000016AL, "TeamMember");
   public static final IRelationTypeSide TeamMember_Member = TeamMember_Team.getOpposite();

   public static final IRelationTypeSide TeamWorkflowTargetedForVersion_Workflow = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x200000000000016FL, "TeamWorkflowTargetedForVersion");
   public static final IRelationTypeSide TeamWorkflowTargetedForVersion_Version = TeamWorkflowTargetedForVersion_Workflow.getOpposite();

   public static final IRelationTypeSide TeamWorkflowToReview_Team = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x2000000000000171L, "TeamWorkflowToReview");
   public static final IRelationTypeSide TeamWorkflowToReview_Review = TeamWorkflowToReview_Team.getOpposite();
 
   public static final IRelationTypeSide ActionableItemLead_AI = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x2000000000000179L, "ActionableItemLead");
   public static final IRelationTypeSide ActionableItemLead_Lead = ActionableItemLead_AI.getOpposite();
   
   public static final IRelationTypeSide ActionableItem_Artifact = TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, 0x2000000000000178L, "ActionableItem Owner");
   public static final IRelationTypeSide ActionableItem_User = ActionableItem_Artifact.getOpposite();
   //@formatter:on

   private AtsRelationTypes() {
      // Constants
   }
}