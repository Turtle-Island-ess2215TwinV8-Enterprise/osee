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
package org.eclipse.osee.framework.ui.skynet.search;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.INCLUDE_DELETED;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Roberto E. Escobar
 */
final class IdArtifactSearch extends AbstractLegacyArtifactSearchQuery {
   private final List<String> idsToSearch;
   private final IOseeBranch branchToSearch;
   private final DeletionFlag allowDeleted;

   IdArtifactSearch(String searchString, IOseeBranch branchToSearch, DeletionFlag allowDeleted) {
      super();
      this.idsToSearch = new ArrayList<String>();
      this.branchToSearch = branchToSearch;
      this.allowDeleted = allowDeleted;

      this.idsToSearch.addAll(Arrays.asList(searchString.split("[\\s,]+")));
   }

   @Override
   public Collection<Artifact> getArtifacts() throws Exception {
      return ArtifactQuery.getArtifactListFromIds(idsToSearch, branchToSearch, allowDeleted);
   }

   @Override
   public String getCriteriaLabel() {
      return String.format("%s%s", idsToSearch.toString(),
         allowDeleted == INCLUDE_DELETED ? " - Options:[Include Deleted]" : "");
   }
}
