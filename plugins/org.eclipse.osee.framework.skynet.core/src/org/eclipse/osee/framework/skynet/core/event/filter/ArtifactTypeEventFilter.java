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
package org.eclipse.osee.framework.skynet.core.event.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.model.event.IBasicGuidArtifact;
import org.eclipse.osee.framework.core.model.event.IBasicGuidRelation;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.internal.Activator;

/**
 * @author Donald G. Dunne
 */
public class ArtifactTypeEventFilter implements IEventFilter {

   private final Collection<IArtifactType> artifactTypes;

   /**
    * Provide artifact types of events to be passed through. All others will be ignored.
    */
   public ArtifactTypeEventFilter(IArtifactType... artifactTypes) {
      this.artifactTypes = Collections.getAggregate(artifactTypes);
   }

   /**
    * Return true if any artifact matches any of the desired artifact types
    */
   @Override
   public boolean isMatchArtifacts(List<? extends IBasicGuidArtifact> guidArts) {
      try {
         for (IBasicGuidArtifact guidArt : guidArts) {
            ArtifactType artType = ArtifactTypeManager.getTypeByGuid(guidArt.getArtTypeGuid());
            for (IArtifactType artifactType : artifactTypes) {
               if (artType.inheritsFrom(artifactType)) {
                  return true;
               }
               for (IArtifactType matchArtType : artifactTypes) {
                  if (matchArtType.getGuid().equals(artType.getGuid())) {
                     return true;
                  }
               }
            }
         }

      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return false;
   }

   /**
    * Return true if any artifact on either side of relation matches any of the desired artifact types
    */
   @Override
   public boolean isMatchRelationArtifacts(List<? extends IBasicGuidRelation> relations) {
      for (IBasicGuidRelation relation : relations) {
         if (isMatchArtifacts(Arrays.asList(relation.getArtA(), relation.getArtB()))) {
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean isMatch(String branchGuid) {
      return true;
   }

}
