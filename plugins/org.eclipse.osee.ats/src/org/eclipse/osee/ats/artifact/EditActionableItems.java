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
package org.eclipse.osee.ats.artifact;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.osee.ats.core.client.action.ActionArtifact;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUsersClient;
import org.eclipse.osee.ats.core.config.TeamDefinitions;
import org.eclipse.osee.ats.core.model.IAtsActionableItem;
import org.eclipse.osee.ats.core.model.IAtsTeamDefinition;
import org.eclipse.osee.ats.core.model.IAtsUser;
import org.eclipse.osee.ats.core.users.AtsUsers;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.dialog.AICheckTreeDialog;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;

public class EditActionableItems {

   public static Result editActionableItems(ActionArtifact actionArt) throws OseeCoreException {
      final AICheckTreeDialog diag =
         new AICheckTreeDialog(
            "Add Impacted Actionable Items",
            "Select New Impacted Actionable Items\n\n" + "Note: Un-selecting existing items will NOT remove the impact.\n" + "Team Workflow with no impact should be transitioned to Cancelled.",
            Active.Active);

      diag.setInitialAias(actionArt.getActionableItems());
      if (diag.open() != 0) {
         return Result.FalseResult;
      }

      // ensure that at least one actionable item exists for each team after aias added/removed
      for (TeamWorkFlowArtifact team : ActionManager.getTeams(actionArt)) {
         Set<IAtsActionableItem> currentAias = team.getActionableItemsDam().getActionableItems();
         Collection<IAtsActionableItem> checkedAias = diag.getChecked();
         for (IAtsActionableItem aia : new CopyOnWriteArrayList<IAtsActionableItem>(currentAias)) {
            if (!checkedAias.contains(aia)) {
               currentAias.remove(aia);
            }
         }
         if (currentAias.isEmpty()) {
            return new Result("Can not remove all actionable items for a team.\n\nActionable Items will go to 0 for [" +
            //
            team.getTeamName() + "][" + team.getHumanReadableId() + "]\n\nCancel team workflow instead.");
         }
      }

      final StringBuffer sb = new StringBuffer();
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Edit Actionable Items");
      Date createdDate = new Date();
      IAtsUser createdBy = AtsUsersClient.getUser();

      // Add new aias
      for (IAtsActionableItem aia : diag.getChecked()) {
         Result result = addActionableItemToTeamsOrAddTeams(actionArt, aia, createdDate, createdBy, transaction);
         sb.append(result.getText());
      }
      // Remove unchecked aias
      for (TeamWorkFlowArtifact team : ActionManager.getTeams(actionArt)) {
         for (IAtsActionableItem aia : team.getActionableItemsDam().getActionableItems()) {
            if (!diag.getChecked().contains(aia)) {
               team.getActionableItemsDam().removeActionableItem(aia);
            }
         }
         team.persist(transaction);
      }

      transaction.execute();
      return new Result(true, sb.toString());
   }

   public static Result addActionableItemToTeamsOrAddTeams(Artifact actionArt, IAtsActionableItem aia, Date createdDate, IAtsUser createdBy, SkynetTransaction transaction) throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      for (IAtsTeamDefinition tda : TeamDefinitions.getImpactedTeamDefs(Arrays.asList(aia))) {
         boolean teamExists = false;
         // Look for team workflow that is associated with this tda
         for (TeamWorkFlowArtifact teamArt : ActionManager.getTeams(actionArt)) {
            // If found
            if (teamArt.getTeamDefinition().equals(tda)) {
               // And workflow doesn't already have this actionable item,
               // ADD it
               if (!teamArt.getActionableItemsDam().getActionableItems().contains(aia)) {
                  teamArt.getActionableItemsDam().addActionableItem(aia);
                  teamArt.saveSMA(transaction);
                  sb.append(aia.getName() + " => added to existing team workflow \"" + tda.getName() + "\"\n");
                  teamExists = true;
               } else {
                  sb.append(aia.getName() + " => already exists in team workflow \"" + tda.getName() + "\"\n");
                  teamExists = true;
               }
            }
         }
         if (!teamExists) {
            TeamWorkFlowArtifact teamArt =
               ActionManager.createTeamWorkflow(actionArt, tda, Arrays.asList(aia), AtsUsers.toList(tda.getLeads()),
                  transaction, createdDate, createdBy, null);
            teamArt.persist(transaction);
            sb.append(aia.getName() + " => added team workflow \"" + tda.getName() + "\"\n");
         }
      }
      return new Result(true, sb.toString());
   }

   public static Result editActionableItems(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      ActionArtifact parentAction = teamArt.getParentActionArtifact();
      if (parentAction == null) {
         return new Result("No Parent Action; Aborting");
      }
      return EditActionableItems.editActionableItems(parentAction);
   }

}