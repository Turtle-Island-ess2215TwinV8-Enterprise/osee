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
package org.eclipse.osee.ats.review;

import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.ats.core.config.ActionableItemArtifact;
import org.eclipse.osee.ats.core.workflow.ActionableItemManagerCore;
import org.eclipse.osee.ats.navigate.VisitedItems;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.ats.util.DemoTestUtil;
import org.eclipse.osee.ats.util.NavigateTestUtil;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.support.test.util.DemoUsers;

/**
 * Test Case for @link {@link ReviewWorldSearchItem}
 * 
 * @author Donald G. Dunne
 */
public class ReviewWorldSearchItemDemoTest {

   @org.junit.Test
   public void testDemoDatabase() throws Exception {
      VisitedItems.clearVisited();
      DemoTestUtil.setUpTest();
      assertTrue(DemoTestUtil.getDemoUser(DemoUsers.Kay_Jones) != null);
   }

   @org.junit.Test
   public void testAiSearch() throws Exception {
      User joe = DemoTestUtil.getDemoUser(DemoUsers.Joe_Smith);
      Set<ActionableItemArtifact> aias = ActionableItemManagerCore.getActionableItems(Arrays.asList("SAW Code"));
      ReviewWorldSearchItem search =
         new ReviewWorldSearchItem("", aias, false, false, false, null, joe, null, null, null);
      Collection<Artifact> arts = search.performSearchGetResults();
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.PeerToPeerReview, 2);
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.DecisionReview, 0);
   }

   @org.junit.Test
   public void testState() throws Exception {
      User joe = DemoTestUtil.getDemoUser(DemoUsers.Joe_Smith);
      Set<ActionableItemArtifact> aias = ActionableItemManagerCore.getActionableItems(Arrays.asList("SAW Code"));
      ReviewWorldSearchItem search =
         new ReviewWorldSearchItem("", aias, false, false, false, null, joe, null, null, "Prepare");
      Collection<Artifact> arts = search.performSearchGetResults();
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.PeerToPeerReview, 1);
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.DecisionReview, 0);
   }

   @org.junit.Test
   public void testIncludeCompleted() throws Exception {
      Set<ActionableItemArtifact> aias = ActionableItemManagerCore.getActionableItems(Arrays.asList("SAW Code"));
      ReviewWorldSearchItem search =
         new ReviewWorldSearchItem("", aias, true, false, false, null, null, null, null, null);
      Collection<Artifact> arts = search.performSearchGetResults();
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.PeerToPeerReview, 3);
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.DecisionReview, 0);
   }

   @org.junit.Test
   public void testAssignee_Kay() throws Exception {
      User Kay_Jones = DemoTestUtil.getDemoUser(DemoUsers.Kay_Jones);
      Set<ActionableItemArtifact> aias = ActionableItemManagerCore.getActionableItems(Arrays.asList("SAW Code"));
      ReviewWorldSearchItem search =
         new ReviewWorldSearchItem("", aias, false, false, false, null, Kay_Jones, null, null, null);
      Collection<Artifact> arts = search.performSearchGetResults();
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.PeerToPeerReview, 1);
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.DecisionReview, 0);
   }

   @org.junit.Test
   public void testAssignee_Joe() throws Exception {
      User Joe_Smith = DemoTestUtil.getDemoUser(DemoUsers.Joe_Smith);
      ReviewWorldSearchItem search =
         new ReviewWorldSearchItem("", (List<String>) null, false, false, false, null, Joe_Smith, null, null, null);
      Collection<Artifact> arts = search.performSearchGetResults();
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.PeerToPeerReview, 2);
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.DecisionReview, 1);
   }

}
