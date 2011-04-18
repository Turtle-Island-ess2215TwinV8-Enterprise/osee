/*
 * Created on Apr 18, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.test.review;

import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.ats.artifact.ActionableItemArtifact;
import org.eclipse.osee.ats.artifact.ActionableItemManager;
import org.eclipse.osee.ats.navigate.VisitedItems;
import org.eclipse.osee.ats.review.ReviewWorldSearchItem;
import org.eclipse.osee.ats.test.util.DemoTestUtil;
import org.eclipse.osee.ats.test.util.NavigateTestUtil;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
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
      Set<ActionableItemArtifact> aias = ActionableItemManager.getActionableItems(Arrays.asList("SAW Code"));
      ReviewWorldSearchItem search = new ReviewWorldSearchItem("", aias, false, false, false, null, joe, null, null);
      Collection<Artifact> arts = search.performSearchGetResults();
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.PeerToPeerReview, 2);
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.DecisionReview, 0);
   }

   @org.junit.Test
   public void testState() throws Exception {
      User joe = DemoTestUtil.getDemoUser(DemoUsers.Joe_Smith);
      Set<ActionableItemArtifact> aias = ActionableItemManager.getActionableItems(Arrays.asList("SAW Code"));
      ReviewWorldSearchItem search =
         new ReviewWorldSearchItem("", aias, false, false, false, null, joe, null, "Prepare");
      Collection<Artifact> arts = search.performSearchGetResults();
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.PeerToPeerReview, 1);
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.DecisionReview, 0);
   }

   @org.junit.Test
   public void testIncludeCompleted() throws Exception {
      Set<ActionableItemArtifact> aias = ActionableItemManager.getActionableItems(Arrays.asList("SAW Code"));
      ReviewWorldSearchItem search = new ReviewWorldSearchItem("", aias, true, false, false, null, null, null, null);
      Collection<Artifact> arts = search.performSearchGetResults();
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.PeerToPeerReview, 3);
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.DecisionReview, 0);
   }

   @org.junit.Test
   public void testAssignee_Kay() throws Exception {
      User Kay_Jones = DemoTestUtil.getDemoUser(DemoUsers.Kay_Jones);
      Set<ActionableItemArtifact> aias = ActionableItemManager.getActionableItems(Arrays.asList("SAW Code"));
      ReviewWorldSearchItem search =
         new ReviewWorldSearchItem("", aias, false, false, false, null, Kay_Jones, null, null);
      Collection<Artifact> arts = search.performSearchGetResults();
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.PeerToPeerReview, 1);
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.DecisionReview, 0);
   }

   @org.junit.Test
   public void testAssignee_Joe() throws Exception {
      User Joe_Smith = DemoTestUtil.getDemoUser(DemoUsers.Joe_Smith);
      ReviewWorldSearchItem search =
         new ReviewWorldSearchItem("", (List<String>) null, false, false, false, null, Joe_Smith, null, null);
      Collection<Artifact> arts = search.performSearchGetResults();
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.PeerToPeerReview, 2);
      NavigateTestUtil.testExpectedVersusActual("AI Search", arts, AtsArtifactTypes.DecisionReview, 1);
   }

}