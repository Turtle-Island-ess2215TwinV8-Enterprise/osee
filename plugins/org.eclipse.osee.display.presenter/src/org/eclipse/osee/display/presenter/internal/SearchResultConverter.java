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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.display.api.data.SearchResultMatch;
import org.eclipse.osee.display.api.data.StyledText;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.presenter.ArtifactProvider;
import org.eclipse.osee.display.presenter.Utility;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.MatchLocation;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.data.ReadableAttribute;
import org.eclipse.osee.orcs.search.Match;

/**
 * @author John Misinco
 */
public class SearchResultConverter {

   private final ArtifactProvider artifactProvider;

   public SearchResultConverter(ArtifactProvider artifactProvider) {
      this.artifactProvider = artifactProvider;
   }

   public ViewId convertToViewId(ReadableArtifact art) {
      ViewId toReturn = new ViewId(art.getGuid(), art.getName());
      toReturn.setAttribute("branch", art.getBranch().getGuid());
      return toReturn;
   }

   public List<ViewId> getCrumbs(ReadableArtifact art, boolean isVerbose) throws OseeCoreException {
      List<ViewId> toReturn;
      if (isVerbose) {
         toReturn = ArtifactUtil.getAncestry(art, artifactProvider);
      } else {
         toReturn = Collections.emptyList();
      }
      return toReturn;
   }

   public List<SearchResultMatch> getSearchResultMatches(Match<ReadableArtifact, ReadableAttribute<?>> match, boolean isVerbose) throws OseeCoreException {
      List<SearchResultMatch> toReturn;
      if (isVerbose) {
         toReturn = new LinkedList<SearchResultMatch>();
         for (ReadableAttribute<?> element : match.getElements()) {
            List<MatchLocation> matches = match.getLocation(element);
            String data = String.valueOf(element.getDisplayableString());
            List<StyledText> text = Utility.getMatchedText(data, matches);
            SearchResultMatch srm = new SearchResultMatch(element.getAttributeType().getName(), matches.size(), text);
            toReturn.add(srm);
         }
      } else {
         toReturn = Collections.emptyList();
      }
      return toReturn;
   }

   public String getTypeName(ReadableArtifact art) {
      return art.getArtifactType().getName();
   }

}
