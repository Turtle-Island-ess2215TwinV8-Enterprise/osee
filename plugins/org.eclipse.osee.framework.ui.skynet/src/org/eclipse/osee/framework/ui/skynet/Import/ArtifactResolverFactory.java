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
package org.eclipse.osee.framework.ui.skynet.Import;

import java.util.Collection;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.skynet.core.importing.resolvers.AttributeBasedArtifactResolver;
import org.eclipse.osee.framework.skynet.core.importing.resolvers.GuidBasedArtifactResolver;
import org.eclipse.osee.framework.skynet.core.importing.resolvers.IArtifactImportResolver;
import org.eclipse.osee.framework.skynet.core.importing.resolvers.IRoughArtifactTranslator;
import org.eclipse.osee.framework.skynet.core.importing.resolvers.NewArtifactImportResolver;
import org.eclipse.osee.framework.skynet.core.importing.resolvers.RoughArtifactTranslatorImpl;

/**
 * @author Roberto E. Escobar
 */
public final class ArtifactResolverFactory {

   public static enum ArtifactCreationStrategy {
      CREATE_NEW_ALWAYS,
      CREATE_ON_NEW_ART_GUID,
      CREATE_ON_DIFFERENT_ATTRIBUTES
   }

   private ArtifactResolverFactory() {
      // Static Factory
   }

   public static IArtifactImportResolver createAlwaysNewArtifacts(IArtifactType primaryArtifactType) {
      return createAlwaysNewArtifacts(primaryArtifactType, CoreArtifactTypes.Heading);
   }

   public static IArtifactImportResolver createAlwaysNewArtifacts(IArtifactType primaryArtifactType, IArtifactType secondaryArtifactType) {
      IRoughArtifactTranslator translator = new RoughArtifactTranslatorImpl();
      return new NewArtifactImportResolver(translator, primaryArtifactType, secondaryArtifactType);
   }

   public static IArtifactImportResolver createResolver(ArtifactCreationStrategy strategy, IArtifactType primaryArtifactType, Collection<IAttributeType> nonChangingAttributes, boolean createNewIfNotExist, boolean deleteUnmatchedArtifacts) {
      IArtifactImportResolver toReturn;
      switch (strategy) {
         case CREATE_ON_DIFFERENT_ATTRIBUTES:
            toReturn =
               new AttributeBasedArtifactResolver(new RoughArtifactTranslatorImpl(), primaryArtifactType,
                  CoreArtifactTypes.Heading, nonChangingAttributes, createNewIfNotExist, deleteUnmatchedArtifacts);
            break;
         case CREATE_ON_NEW_ART_GUID:
            toReturn =
               new GuidBasedArtifactResolver(new RoughArtifactTranslatorImpl(), primaryArtifactType,
                  CoreArtifactTypes.Heading, createNewIfNotExist, deleteUnmatchedArtifacts);
            break;
         case CREATE_NEW_ALWAYS:
         default:
            toReturn = createAlwaysNewArtifacts(primaryArtifactType);
            break;
      }
      return toReturn;
   }
}
