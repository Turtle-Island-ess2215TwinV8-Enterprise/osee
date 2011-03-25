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
package org.eclipse.osee.ats.version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.artifact.VersionArtifact;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;

/**
 * @author Donald G. Dunne
 */
public class VersionTeamMetrics {

   private final TeamDefinitionArtifact verTeamDef;
   private final List<VersionMetrics> releasedOrderedVersions = new ArrayList<VersionMetrics>();
   private final Set<VersionMetrics> verMets = new HashSet<VersionMetrics>();
   Map<Date, VersionMetrics> relDateToVerMet = new HashMap<Date, VersionMetrics>();

   public VersionTeamMetrics(TeamDefinitionArtifact verTeamDef) throws OseeCoreException {
      this.verTeamDef = verTeamDef;
      loadMetrics();
   }

   private void loadMetrics() throws OseeCoreException {
      bulkLoadArtifacts();
      orderReleasedVersions();
   }

   private void bulkLoadArtifacts() throws OseeCoreException {
      RelationManager.getRelatedArtifacts(Arrays.asList(this.verTeamDef), 6,
         CoreRelationTypes.Default_Hierarchical__Child, AtsRelationTypes.TeamDefinitionToVersion_Version,
         AtsRelationTypes.TeamWorkflowTargetedForVersion_Workflow, AtsRelationTypes.SmaToTask_Task,
         AtsRelationTypes.ActionToWorkflow_Action);
   }

   private Map<TeamWorkFlowArtifact, Date> teamWorkflowToOrigDate = null;

   public Collection<TeamWorkFlowArtifact> getWorkflowsOriginatedBetween(Date startDate, Date endDate) throws OseeCoreException {
      if (teamWorkflowToOrigDate == null) {
         teamWorkflowToOrigDate = new HashMap<TeamWorkFlowArtifact, Date>();
         for (VersionArtifact verArt : verTeamDef.getVersionsArtifacts()) {
            for (TeamWorkFlowArtifact team : verArt.getTargetedForTeamArtifacts()) {
               Date origDate = team.getCreatedDate();
               teamWorkflowToOrigDate.put(team, origDate);
            }
         }
      }
      Set<TeamWorkFlowArtifact> teams = new HashSet<TeamWorkFlowArtifact>();
      for (Entry<TeamWorkFlowArtifact, Date> entry : teamWorkflowToOrigDate.entrySet()) {
         if (entry.getValue() != null && entry.getValue().after(startDate) && entry.getValue().before(endDate)) {
            teams.add(entry.getKey());
         }
      }
      return teams;
   }

   private void orderReleasedVersions() throws OseeCoreException {
      for (VersionArtifact verArt : verTeamDef.getVersionsArtifacts()) {
         VersionMetrics verMet = new VersionMetrics(verArt, this);
         Date relDate = verArt.getSoleAttributeValue(AtsAttributeTypes.ReleaseDate, null);
         if (relDate != null) {
            relDateToVerMet.put(relDate, verMet);
         }
         verMets.add(verMet);
      }
      Date[] releases = relDateToVerMet.keySet().toArray(new Date[relDateToVerMet.keySet().size()]);
      Arrays.sort(releases);
      for (Date date : releases) {
         releasedOrderedVersions.add(relDateToVerMet.get(date));
      }
   }

   /**
    * @return the verTeamDef
    */
   public TeamDefinitionArtifact getVerTeamDef() {
      return verTeamDef;
   }

   /**
    * @return the releasedOrderedVersions
    */
   public List<VersionMetrics> getReleasedOrderedVersions() {
      return releasedOrderedVersions;
   }

   /**
    * @return the verMets
    */
   public Set<VersionMetrics> getVerMets() {
      return verMets;
   }

   /**
    * @return the relDateToVerMet
    */
   public Map<Date, VersionMetrics> getRelDateToVerMet() {
      return relDateToVerMet;
   }
}