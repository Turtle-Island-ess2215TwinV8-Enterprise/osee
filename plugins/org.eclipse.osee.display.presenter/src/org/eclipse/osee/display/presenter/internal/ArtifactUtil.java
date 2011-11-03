/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.display.presenter.internal;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.presenter.ArtifactProvider;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.data.ReadableArtifact;

/**
 * @author John Misinco
 */
public class ArtifactUtil {

   public static List<ViewId> getAncestry(ReadableArtifact art, ArtifactProvider artifactProvider) throws OseeCoreException {
      ReadableArtifact cur = artifactProvider.getParent(art);
      List<ViewId> ancestry = new ArrayList<ViewId>();
      while (cur != null) {
         ancestry.add(new ViewId(cur.getGuid(), cur.getName()));
         cur = artifactProvider.getParent(cur);
      }
      return ancestry;
   }
}
