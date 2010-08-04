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
package org.eclipse.osee.framework.core.dsl.integration.internal;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.dsl.integration.AccessModelInterpreter.AccessDetailCollector;
import org.eclipse.osee.framework.core.dsl.integration.ArtifactDataProvider.ArtifactData;
import org.eclipse.osee.framework.core.dsl.integration.OseeUtil;
import org.eclipse.osee.framework.core.dsl.integration.RestrictionHandler;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ArtifactTypeRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ObjectRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactType;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.access.AccessDetail;
import org.eclipse.osee.framework.core.model.type.ArtifactType;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactTypeRestrictionHandler implements RestrictionHandler<ArtifactTypeRestriction> {

   @Override
   public ArtifactTypeRestriction asCastedObject(ObjectRestriction objectRestriction) {
      ArtifactTypeRestriction toReturn = null;
      if (objectRestriction instanceof ArtifactTypeRestriction) {
         toReturn = (ArtifactTypeRestriction) objectRestriction;
      }
      return toReturn;
   }

   @Override
   public void process(ObjectRestriction objectRestriction, ArtifactData artifactData, AccessDetailCollector collector) throws OseeCoreException {
      ArtifactTypeRestriction restriction = asCastedObject(objectRestriction);
      if (restriction != null) {
         XArtifactType artifactTypeRef = restriction.getArtifactTypeRef();
         IArtifactType typeToMatch = OseeUtil.toToken(artifactTypeRef);

         ArtifactType artifactType = artifactData.getArtifactType();
         boolean isOfType = artifactType != null && artifactType.inheritsFrom(typeToMatch);
         if (isOfType) {
            PermissionEnum permission = OseeUtil.getPermission(restriction);
            collector.collect(new AccessDetail<IArtifactType>(artifactType, permission));
         }
      }
   }

}