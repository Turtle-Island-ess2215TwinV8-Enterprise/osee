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
package org.eclipse.osee.framework.skynet.core.artifact.factory;

import java.sql.SQLException;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.WordArtifact;

/**
 * @author Ryan D. Brooks
 */
public class WordArtifactFactory extends ArtifactFactory<WordArtifact> {
   private static WordArtifactFactory factory = null;

   private WordArtifactFactory(int factoryId) {
      super(factoryId);
   }

   public static WordArtifactFactory getInstance(int factoryId) {
      if (factory == null) {
         factory = new WordArtifactFactory(factoryId);
      }
      return factory;
   }

   public static WordArtifactFactory getInstance() {
      return factory;
   }

   public @Override
   WordArtifact getNewArtifact(String guid, String humandReadableId, String factoryKey, Branch branch) throws SQLException {
      return new WordArtifact(this, guid, humandReadableId, branch);
   }
}