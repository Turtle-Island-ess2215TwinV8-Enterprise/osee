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
package org.eclipse.osee.ats.core.client.internal.store;

import java.util.Date;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.core.client.internal.config.AtsArtifactConfigCache;
import org.eclipse.osee.ats.core.config.AtsVersionService;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Donald G. Dunne
 */
public class VersionArtifactWriter extends AbstractAtsArtifactWriter<IAtsVersion> {

   @Override
   public Artifact store(IAtsVersion ai, AtsArtifactConfigCache cache, SkynetTransaction transaction) throws OseeCoreException {
      Artifact artifact = getArtifactOrCreate(cache, AtsArtifactTypes.Version, ai, transaction);
      store(ai, artifact, cache, transaction);
      return artifact;
   }

   @Override
   public Artifact store(IAtsVersion version, Artifact artifact, AtsArtifactConfigCache cache, SkynetTransaction transaction) throws OseeCoreException {
      artifact.setName(version.getName());
      boolean allowCommitBranch = artifact.getSoleAttributeValue(AtsAttributeTypes.AllowCreateBranch, true);
      if (allowCommitBranch != version.isAllowCommitBranch()) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.AllowCommitBranch, version.isAllowCommitBranch());
      }
      boolean allowCreateBranch = artifact.getSoleAttributeValue(AtsAttributeTypes.AllowCreateBranch, true);
      if (allowCreateBranch != version.isAllowCreateBranch()) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.AllowCreateBranch, version.isAllowCreateBranch());
      }
      boolean next = artifact.getSoleAttributeValue(AtsAttributeTypes.NextVersion, false);
      if (next != version.isNextVersion()) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.NextVersion, version.isNextVersion());
      }
      boolean released = artifact.getSoleAttributeValue(AtsAttributeTypes.Released, false);
      if (released != version.isReleased()) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.Released, version.isReleased());
      }
      boolean versionLocked = artifact.getSoleAttributeValue(AtsAttributeTypes.VersionLocked, false);
      if (versionLocked != version.isLocked()) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.VersionLocked, version.isLocked());
      }
      if (Strings.isValid(version.getBaslineBranchGuid())) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.BaselineBranchGuid, version.getBaslineBranchGuid());
      }
      Date releaseDate = artifact.getSoleAttributeValue(AtsAttributeTypes.ReleaseDate, null);
      if (releaseDate != version.getReleaseDate()) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.ReleaseDate, version.getReleaseDate());
      }
      if (Strings.isValid(version.getDescription())) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.Description, version.getDescription());
      }
      if (Strings.isValid(version.getFullName())) {
         artifact.setSoleAttributeValue(AtsAttributeTypes.FullName, version.getFullName());
      }

      // set parent artifact to top team def
      IAtsTeamDefinition teamDefinition = AtsVersionService.get().getTeamDefinition(version);
      if (teamDefinition != null) {
         Artifact teamDefArt = cache.getArtifact(teamDefinition);
         if (teamDefArt != null) {
            if (!teamDefArt.getRelatedArtifacts(AtsRelationTypes.TeamDefinitionToVersion_Version).contains(artifact)) {
               teamDefArt.addRelation(AtsRelationTypes.TeamDefinitionToVersion_Version, artifact);
               teamDefArt.persist(transaction);
            }
         }
      }
      artifact.persist(transaction);
      cache.cache(version);
      return artifact;
   }

}
