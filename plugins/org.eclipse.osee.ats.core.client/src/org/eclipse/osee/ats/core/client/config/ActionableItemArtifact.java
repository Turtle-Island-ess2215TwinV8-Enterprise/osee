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

package org.eclipse.osee.ats.core.client.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.util.AtsUsersClient;
import org.eclipse.osee.ats.core.model.IAtsUser;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;

/**
 * @author Donald G. Dunne
 */
public class ActionableItemArtifact extends Artifact {

   public ActionableItemArtifact(ArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, IArtifactType artifactType) throws OseeCoreException {
      super(parentFactory, guid, humanReadableId, branch, artifactType);
   }

   public Collection<IAtsUser> getLeads() throws OseeCoreException {
      List<IAtsUser> leads = new LinkedList<IAtsUser>();
      for (User user : getRelatedArtifacts(AtsRelationTypes.ActionableItemLead_Lead, User.class)) {
         leads.add(AtsUsersClient.getUser(user.getUserId()));
      }
      return leads;
   }

   public boolean isActionable() throws OseeCoreException {
      return getSoleAttributeValue(AtsAttributeTypes.Actionable, false);
   }

   public Collection<TeamDefinitionArtifact> getImpactedTeamDefs() throws OseeCoreException {
      return TeamDefinitionManagerCore.getImpactedTeamDefs(Arrays.asList(this));
   }

}