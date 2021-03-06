/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.column;

import java.util.Set;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.ai.IAtsActionableItemProvider;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * Provides for rollup of parent workflow's top team definition
 * 
 * @author Donald G. Dunne
 */
public final class ParentTopTeamColumn {

   private ParentTopTeamColumn() {
      // do nothing
   }

   public static String getColumnText(Object object) throws OseeCoreException {
      String result = "";
      IAtsTeamDefinition teamDef = null;
      if (object instanceof IAtsWorkItem) {
         IAtsWorkItem workItem = (IAtsWorkItem) object;
         IAtsTeamWorkflow teamArt = workItem.getParentTeamWorkflow();
         if (teamArt != null) {
            teamDef = teamArt.getTeamDefinition();
         }
      }
      result = getTopTeamDefName(teamDef);
      if (!Strings.isValid(result) && (object instanceof IAtsActionableItemProvider)) {
         IAtsActionableItemProvider provider = (IAtsActionableItemProvider) object;
         Set<IAtsActionableItem> actionableItems = provider.getActionableItems();
         if (!actionableItems.isEmpty()) {
            teamDef = actionableItems.iterator().next().getTeamDefinition();
            if (teamDef == null) {
               teamDef = actionableItems.iterator().next().getTeamDefinitionInherited();
            }
            result = getTopTeamDefName(teamDef);
         }
      }
      return result;
   }

   protected static String getTopTeamDefName(IAtsTeamDefinition teamDef) throws OseeCoreException {
      String result = "";
      if (teamDef != null) {
         IAtsTeamDefinition teamDefinitionHoldingVersions = teamDef.getTeamDefinitionHoldingVersions();
         if (teamDefinitionHoldingVersions != null) {
            teamDef = teamDefinitionHoldingVersions;
         }
         result = teamDef.getName();
      }
      return result;
   }
}
