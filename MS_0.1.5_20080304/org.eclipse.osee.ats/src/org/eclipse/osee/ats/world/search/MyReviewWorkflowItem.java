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
package org.eclipse.osee.ats.world.search;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.artifact.ATSAttributes;
import org.eclipse.osee.ats.util.widgets.ReviewManager;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactTypeSearch;
import org.eclipse.osee.framework.skynet.core.artifact.search.AttributeValueSearch;
import org.eclipse.osee.framework.skynet.core.artifact.search.FromArtifactsSearch;
import org.eclipse.osee.framework.skynet.core.artifact.search.ISearchPrimitive;
import org.eclipse.osee.framework.skynet.core.artifact.search.Operator;

/**
 * @author Donald G. Dunne
 */
public class MyReviewWorkflowItem extends UserSearchItem {

   private final ReviewState reviewState;

   public enum ReviewState {
      InWork, All
   };

   public MyReviewWorkflowItem(String name, User user, ReviewState reviewState) {
      super(name, user);
      this.reviewState = reviewState;
   }

   @Override
   protected Collection<Artifact> searchIt(User user) throws SQLException, IllegalArgumentException {

      // SMA having user as portion of current state attribute (Team WorkFlow and Task)
      List<ISearchPrimitive> currentStateCriteria = new LinkedList<ISearchPrimitive>();
      currentStateCriteria.add(new AttributeValueSearch(ATSAttributes.CURRENT_STATE_ATTRIBUTE.getStoreName(),
            "<" + user.getUserId() + ">", Operator.CONTAINS));
      if (reviewState == ReviewState.All) {
         currentStateCriteria.add(new AttributeValueSearch(ATSAttributes.STATE_ATTRIBUTE.getStoreName(),
               "<" + user.getUserId() + ">", Operator.CONTAINS));
         currentStateCriteria.add(new AttributeValueSearch(ATSAttributes.ROLE_ATTRIBUTE.getStoreName(),
               "userId>" + user.getUserId() + "</userId", Operator.CONTAINS));
      }
      FromArtifactsSearch currentStateSearch = new FromArtifactsSearch(currentStateCriteria, false);

      // Find all Team Workflows artifact types
      List<ISearchPrimitive> reviewTypeCriteria = new LinkedList<ISearchPrimitive>();
      for (String reviewArtName : ReviewManager.getAllReviewArtifactTypeNames())
         reviewTypeCriteria.add(new ArtifactTypeSearch(reviewArtName, Operator.EQUAL));
      FromArtifactsSearch reviewArtSearch = new FromArtifactsSearch(reviewTypeCriteria, false);

      List<ISearchPrimitive> allCriteria = new LinkedList<ISearchPrimitive>();
      allCriteria.add(currentStateSearch);
      allCriteria.add(reviewArtSearch);

      if (isCancelled()) return EMPTY_SET;
      Collection<Artifact> arts =
            ArtifactPersistenceManager.getInstance().getArtifacts(allCriteria, true,
                  BranchPersistenceManager.getInstance().getAtsBranch());
      if (isCancelled()) return EMPTY_SET;
      return arts;
   }
}
