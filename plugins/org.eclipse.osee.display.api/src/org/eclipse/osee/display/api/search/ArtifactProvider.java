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
package org.eclipse.osee.display.api.search;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.data.ReadableAttribute;
import org.eclipse.osee.orcs.search.Match;

/**
 * @author John Misinco
 */
public interface ArtifactProvider {

   ReadableArtifact getArtifactByArtifactToken(IOseeBranch branch, IArtifactToken token) throws OseeCoreException;

   ReadableArtifact getArtifactByGuid(IOseeBranch branch, String guid) throws OseeCoreException;

   List<Match<ReadableArtifact, ReadableAttribute<?>>> getSearchResults(IOseeBranch branch, boolean nameOnly, String searchPhrase) throws OseeCoreException;

   List<ReadableArtifact> getRelatedArtifacts(ReadableArtifact art, IRelationTypeSide relationTypeSide) throws OseeCoreException;

   ReadableArtifact getRelatedArtifact(ReadableArtifact art, IRelationTypeSide relationTypeSide) throws OseeCoreException;

   ReadableArtifact getParent(ReadableArtifact art) throws OseeCoreException;

   Collection<RelationType> getValidRelationTypes(ReadableArtifact art) throws OseeCoreException;
}
