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
package org.eclipse.osee.ats.core.client.internal.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.config.AtsConfigCache;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Donald G. Dunne
 */
public class AtsArtifactConfigCache extends AtsConfigCache {

   public List<Artifact> getArtifacts(Collection<? extends IAtsObject> atsObjects) throws OseeCoreException {
      List<String> guids = new ArrayList<String>();
      for (IAtsObject atsObject : atsObjects) {
         guids.add(atsObject.getGuid());
      }
      return ArtifactQuery.getArtifactListFromIds(guids, AtsUtilCore.getAtsBranchToken());
   }

   public <A extends IAtsConfigObject> Collection<A> getConfigObjects(Collection<? extends Artifact> artifacts, Class<A> clazz) {
      List<A> objects = new ArrayList<A>();
      for (Artifact art : artifacts) {
         objects.addAll(getByTag(art.getGuid(), clazz));
      }
      return objects;
   }

   public Artifact getSoleArtifact(IAtsObject artifact) throws OseeCoreException {
      return ArtifactQuery.getArtifactFromId(artifact.getGuid(), AtsUtilCore.getAtsBranchToken());
   }

   public Artifact getArtifact(IAtsConfigObject atsConfigObject) throws OseeCoreException {
      Conditions.checkNotNull(atsConfigObject, "atsConfigObject");
      Artifact artifact = null;
      try {
         artifact = ArtifactQuery.getArtifactFromId(atsConfigObject.getGuid(), AtsUtilCore.getAtsBranchToken());
      } catch (ArtifactDoesNotExist ex) {
         // do nothing
      }
      return artifact;
   }
}
