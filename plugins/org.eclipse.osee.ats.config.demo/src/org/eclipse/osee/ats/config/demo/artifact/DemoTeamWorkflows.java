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
package org.eclipse.osee.ats.config.demo.artifact;

import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.osee.ats.actions.wizard.TeamWorkflowProviderAdapter;
import org.eclipse.osee.ats.config.demo.internal.Activator;
import org.eclipse.osee.ats.core.config.ActionableItemArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.support.test.util.DemoArtifactTypes;

/**
 * @author Donald G. Dunne
 */
public class DemoTeamWorkflows extends TeamWorkflowProviderAdapter {

   @Override
   public IArtifactType getTeamWorkflowArtifactType(TeamDefinitionArtifact teamDef, Collection<ActionableItemArtifact> actionableItems) {
      if (teamDef.getName().contains("Code")) {
         return DemoArtifactTypes.DemoCodeTeamWorkflow;
      } else if (teamDef.getName().contains("Test")) {
         return DemoArtifactTypes.DemoTestTeamWorkflow;
      } else if (teamDef.getName().contains("Requirements") || teamDef.getName().contains("SAW HW")) {
         return DemoArtifactTypes.DemoReqTeamWorkflow;
      }
      return AtsArtifactTypes.TeamWorkflow;
   }

   @Override
   public boolean isResponsibleForTeamWorkflowCreation(TeamDefinitionArtifact teamDef, Collection<ActionableItemArtifact> actionableItems) {
      return teamDef.getName().contains("SAW") || teamDef.getName().contains("CIS");
   }

   @Override
   public String getPcrId(TeamWorkFlowArtifact teamArt) {
      return "";
   }

   @Override
   public String getBranchName(TeamWorkFlowArtifact teamArt) {
      return null;
   }

   @Override
   public boolean isResponsibleFor(AbstractWorkflowArtifact awa) {
      try {
         TeamWorkFlowArtifact teamArt = awa.getParentTeamWorkflow();
         if (teamArt != null) {
            return (teamArt.isOfType(DemoArtifactTypes.DemoCodeTeamWorkflow) || teamArt.isOfType(DemoArtifactTypes.DemoReqTeamWorkflow) || teamArt.isOfType(DemoArtifactTypes.DemoTestTeamWorkflow));
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return false;
   }

}
