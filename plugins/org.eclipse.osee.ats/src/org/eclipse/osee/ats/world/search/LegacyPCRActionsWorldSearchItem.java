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
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.shared.AtsAttributeTypes;
import org.eclipse.osee.ats.shared.AtsRelationTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.AbstractArtifactSearchCriteria;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.search.AttributeCriteria;
import org.eclipse.osee.framework.skynet.core.artifact.search.RelationCriteria;

/**
 * @author Donald G. Dunne
 */
public class LegacyPCRActionsWorldSearchItem extends WorldUISearchItem {
   private final boolean returnActions;
   private final Collection<String> pcrIds;
   private final Collection<TeamDefinitionArtifact> teamDefs;

   public LegacyPCRActionsWorldSearchItem(Collection<String> pcrIds, Collection<TeamDefinitionArtifact> teamDefs, boolean returnActions) {
      super("");
      this.pcrIds = pcrIds;
      this.teamDefs = teamDefs;
      this.returnActions = returnActions;
   }

   public LegacyPCRActionsWorldSearchItem(Collection<TeamDefinitionArtifact> teamDefs, boolean returnActions) {
      this(null, teamDefs, returnActions);
   }

   public LegacyPCRActionsWorldSearchItem(LegacyPCRActionsWorldSearchItem legacyPCRActionsWorldSearchItem) {
      super(legacyPCRActionsWorldSearchItem);
      this.returnActions = legacyPCRActionsWorldSearchItem.returnActions;
      this.pcrIds = legacyPCRActionsWorldSearchItem.pcrIds;
      this.teamDefs = legacyPCRActionsWorldSearchItem.teamDefs;
   }

   @Override
   public Collection<Artifact> performSearch(SearchType searchType) throws OseeCoreException {
      List<AbstractArtifactSearchCriteria> criteria = new ArrayList<AbstractArtifactSearchCriteria>(4);

      if (teamDefs != null && teamDefs.size() > 0) {
         List<String> teamDefGuids = new ArrayList<String>(teamDefs.size());
         for (TeamDefinitionArtifact teamDef : teamDefs) {
            teamDefGuids.add(teamDef.getGuid());
         }
         criteria.add(new AttributeCriteria(AtsAttributeTypes.TeamDefinition, teamDefGuids));
      }

      if (pcrIds != null && pcrIds.size() > 0) {
         criteria.add(new AttributeCriteria(AtsAttributeTypes.LegacyPcrId, pcrIds));
      } else {
         criteria.add(new AttributeCriteria(AtsAttributeTypes.LegacyPcrId));
      }

      if (returnActions) {
         criteria.add(new RelationCriteria(AtsRelationTypes.ActionToWorkflow_Action));
      }

      return ArtifactQuery.getArtifactListFromCriteria(AtsUtil.getAtsBranch(), 200, criteria);
   }

   @Override
   public WorldUISearchItem copy() {
      return new LegacyPCRActionsWorldSearchItem(this);
   }

}