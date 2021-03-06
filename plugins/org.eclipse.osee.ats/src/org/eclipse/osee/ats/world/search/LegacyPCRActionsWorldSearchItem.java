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
package org.eclipse.osee.ats.world.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.query.IAtsQuery;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class LegacyPCRActionsWorldSearchItem extends WorldUISearchItem {
   private final boolean returnActions;
   private final Collection<String> pcrIds;
   private final Collection<IAtsTeamDefinition> teamDefs;

   public LegacyPCRActionsWorldSearchItem(Collection<String> pcrIds, Collection<IAtsTeamDefinition> teamDefs, boolean returnActions) {
      super("");
      this.pcrIds = pcrIds;
      this.teamDefs = teamDefs;
      this.returnActions = returnActions;
   }

   public LegacyPCRActionsWorldSearchItem(Collection<IAtsTeamDefinition> teamDefs, boolean returnActions) {
      this(null, teamDefs, returnActions);
   }

   public LegacyPCRActionsWorldSearchItem(LegacyPCRActionsWorldSearchItem legacyPCRActionsWorldSearchItem) {
      super(legacyPCRActionsWorldSearchItem);
      this.returnActions = legacyPCRActionsWorldSearchItem.returnActions;
      this.pcrIds = legacyPCRActionsWorldSearchItem.pcrIds;
      this.teamDefs = legacyPCRActionsWorldSearchItem.teamDefs;
   }

   private boolean isPcrIdsSet() {
      return pcrIds != null && !pcrIds.isEmpty();
   }

   private boolean isTeamDefsSet() {
      return teamDefs != null && !teamDefs.isEmpty();
   }

   @Override
   public Collection<Artifact> performSearch(SearchType searchType) throws OseeCoreException {

      List<Artifact> pcrIdArts = new ArrayList<Artifact>();
      List<Artifact> teamDefArts = new ArrayList<Artifact>();
      List<String> teamDefGuids = new ArrayList<String>();

      if (isPcrIdsSet()) {
         LegacyPcrIdQuickSearch srch = new LegacyPcrIdQuickSearch(pcrIds);
         pcrIdArts.addAll(srch.performSearch());
      }
      if (isTeamDefsSet()) {
         TeamDefinitionQuickSearch srch = new TeamDefinitionQuickSearch(teamDefs);
         teamDefArts.addAll(srch.performSearch());
         teamDefGuids = AtsObjects.toGuids(teamDefs);
      }

      // If both set, return intersection; else return just what was set
      List<Artifact> arts = new ArrayList<Artifact>();
      if (isPcrIdsSet() && isTeamDefsSet()) {
         arts = Collections.setIntersection(pcrIdArts, teamDefArts);
      } else if (isPcrIdsSet()) {
         arts = pcrIdArts;
      } else if (isTeamDefsSet()) {
         arts = teamDefArts;
      }

      IAtsQuery query =
         AtsClientService.get().createQuery(AtsClientService.get().getWorkDefinitionAdmin().getWorkItems(arts)).withOrValue(
            AtsAttributeTypes.LegacyPcrId, pcrIds).withOrValue(AtsAttributeTypes.TeamDefinition, teamDefGuids).isOfType(
            AtsArtifactTypes.TeamWorkflow);

      Collection<? extends IAtsWorkItem> workItems = query.getItems();
      List<Artifact> artifacts = AtsClientService.get().getWorkDefinitionAdmin().get(workItems, Artifact.class);
      if (returnActions) {
         List<Artifact> actions = new ArrayList<Artifact>();
         for (Artifact artifact : artifacts) {
            if (artifact instanceof AbstractWorkflowArtifact) {
               AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) artifact;
               actions.add(awa.getParentActionArtifact());
            }
         }
         return actions;
      } else {
         return artifacts;
      }

   }

   @Override
   public WorldUISearchItem copy() {
      return new LegacyPCRActionsWorldSearchItem(this);
   }

}