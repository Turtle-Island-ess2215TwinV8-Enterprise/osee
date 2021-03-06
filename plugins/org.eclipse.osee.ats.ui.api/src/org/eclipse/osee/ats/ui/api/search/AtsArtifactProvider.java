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
package org.eclipse.osee.ats.ui.api.search;

import java.util.Collection;
import org.eclipse.osee.display.api.search.ArtifactProvider;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author John R. Misinco
 */
public interface AtsArtifactProvider extends ArtifactProvider {

   Collection<ArtifactReadable> getPrograms() throws OseeCoreException;

   Collection<ArtifactReadable> getBuilds(String programGuid) throws OseeCoreException;

   String getBaselineBranchGuid(String buildArtGuid) throws OseeCoreException;
}
