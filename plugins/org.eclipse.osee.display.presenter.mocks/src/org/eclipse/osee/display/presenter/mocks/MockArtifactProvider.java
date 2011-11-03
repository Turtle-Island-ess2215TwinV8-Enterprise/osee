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
package org.eclipse.osee.display.presenter.mocks;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.display.presenter.ArtifactProvider;
import org.eclipse.osee.display.presenter.AsyncSearchHandler;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.data.ReadableAttribute;
import org.eclipse.osee.orcs.search.Match;

/**
 * @author John Misinco
 */
public class MockArtifactProvider implements ArtifactProvider {

   private final Map<String, ReadableArtifact> artifacts = new HashMap<String, ReadableArtifact>();
   private List<Match<ReadableArtifact, ReadableAttribute<?>>> resultList;

   public void addArtifact(ReadableArtifact artifact) {
      artifacts.put(artifact.getGuid(), artifact);
   }

   public void setResultList(List<Match<ReadableArtifact, ReadableAttribute<?>>> resultList) {
      this.resultList = resultList;
   }

   @Override
   public ReadableArtifact getArtifactByArtifactToken(IOseeBranch branch, IArtifactToken token) {
      return artifacts.get(token.getGuid());
   }

   @Override
   public ReadableArtifact getArtifactByGuid(IOseeBranch branch, String guid) {
      return artifacts.get(guid);
   }

   @Override
   public List<ReadableArtifact> getRelatedArtifacts(ReadableArtifact art, IRelationTypeSide relationTypeSide) {
      if (art instanceof MockArtifact) {
         MockArtifact mArt = (MockArtifact) art;
         return (List<ReadableArtifact>) mArt.getRelatedArtifacts(relationTypeSide);
      } else {
         return Collections.emptyList();
      }
   }

   @Override
   public ReadableArtifact getRelatedArtifact(ReadableArtifact art, IRelationTypeSide relationTypeSide) {
      if (art instanceof MockArtifact) {
         MockArtifact mArt = (MockArtifact) art;
         return mArt.getRelatedArtifacts(relationTypeSide).iterator().next();
      } else {
         return null;
      }
   }

   @Override
   public ReadableArtifact getParent(ReadableArtifact art) {
      if (art instanceof MockArtifact) {
         MockArtifact mArt = (MockArtifact) art;
         return mArt.getParent();
      } else {
         return null;
      }
   }

   @Override
   public Collection<RelationType> getValidRelationTypes(ReadableArtifact art) {
      if (art instanceof MockArtifact) {
         return ((MockArtifact) art).getValidRelationTypes();
      } else {
         return Collections.emptyList();
      }
   }

   @Override
   public void getSearchResults(IOseeBranch branch, boolean nameOnly, String searchPhrase, AsyncSearchHandler callback) {
      callback.onSearchComplete(resultList);
   }

   @Override
   public void cancelSearch() {
      //do nothing
   }
}