/*
 * Created on Jan 24, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.test.editor.stateItem;

import static org.junit.Assert.assertFalse;
import java.util.Collection;
import junit.framework.Assert;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.DecisionReviewArtifact;
import org.eclipse.osee.ats.artifact.DecisionReviewState;
import org.eclipse.osee.ats.editor.stateItem.AtsDecisionReviewDecisionStateItem;
import org.eclipse.osee.ats.test.util.DemoTestUtil;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.ReviewManager;
import org.eclipse.osee.ats.workdef.StateDefinition;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.widgets.XComboDam;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Case for {@link AtsDecisionReviewDecisionStateItem}
 * 
 * @author Donald G. Dunne
 */
public class AtsDecisionReviewDecisionStateItemTest {

   public static DecisionReviewArtifact decRevArt;

   @Before
   public void setUp() throws Exception {
      // This test should only be run on test db
      assertFalse("Test should not be run in production db", AtsUtil.isProductionDb());

      if (decRevArt == null) {
         // setup fake review artifact with decision options set
         decRevArt =
            (DecisionReviewArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.DecisionReview,
               AtsUtil.getAtsBranch());
         decRevArt.setName(getClass().getSimpleName());
         decRevArt.setSoleAttributeValue(AtsAttributeTypes.DecisionReviewOptions,
            ReviewManager.getDecisionReviewOptionsString(ReviewManager.getDefaultDecisionReviewOptions()));
         decRevArt.persist();
      }
   }

   @BeforeClass
   @AfterClass
   public static void testCleanup() throws Exception {
      DemoTestUtil.cleanupSimpleTest(AtsDecisionReviewDecisionStateItemTest.class.getSimpleName());
   }

   @Test
   public void testXWidgetCreating() throws OseeCoreException {
      Assert.assertNotNull(decRevArt);

      // setup fake combo that will hold values
      XComboDam decisionComboDam = new XComboDam("Decision");
      // set combo values an verify they got set
      decisionComboDam.setDataStrings(new String[] {"One", "Two", "Three"});
      StateDefinition stateDef = new StateDefinition("Decision");
      Assert.assertEquals("Two", decisionComboDam.getDisplayArray()[2]);

      // make call to state item that should set options based on artifact's attribute value
      AtsDecisionReviewDecisionStateItem stateItem = new AtsDecisionReviewDecisionStateItem();
      Result result = stateItem.xWidgetCreating(decisionComboDam, null, stateDef, decRevArt, null, true);

      // verify no errors and options are as specified in artifact's attribute
      Assert.assertTrue(result.getText(), result.isTrue());
      Assert.assertEquals("Yes", decisionComboDam.getDisplayArray()[1]);
      Assert.assertEquals("No", decisionComboDam.getDisplayArray()[2]);
   }

   @Test
   public void testGetOverrideTransitionToAssignees() throws OseeCoreException {
      Assert.assertNotNull(decRevArt);

      // setup fake combo that will hold values
      XComboDam decisionComboDam = new XComboDam("Decision");
      // set combo values an verify they got set
      decisionComboDam.setDataStrings(new String[] {"Yes", "No"});

      // Set Yes
      decisionComboDam.set(1);

      AtsDecisionReviewDecisionStateItem stateItem = new AtsDecisionReviewDecisionStateItem();
      Collection<User> users = stateItem.getOverrideTransitionToAssignees(decRevArt, decisionComboDam);
      Assert.assertEquals(1, users.size());
      Assert.assertEquals(UserManager.getUser(), users.iterator().next());

      // Set No
      decisionComboDam.set(2);

      users = stateItem.getOverrideTransitionToAssignees(decRevArt, decisionComboDam);
      Assert.assertTrue(users.isEmpty());
   }

   @Test
   public void testGetOverrideTransitionToStateName() throws OseeCoreException {
      Assert.assertNotNull(decRevArt);

      // setup fake combo that will hold values
      XComboDam decisionComboDam = new XComboDam("Decision");
      // set combo values an verify they got set
      decisionComboDam.setDataStrings(new String[] {"Yes", "No"});

      // Set Yes
      decisionComboDam.set(1);

      AtsDecisionReviewDecisionStateItem stateItem = new AtsDecisionReviewDecisionStateItem();
      String toStateName = stateItem.getOverrideTransitionToStateName(decRevArt, decisionComboDam);
      Assert.assertEquals(DecisionReviewState.Followup.getPageName(), toStateName);

      // Set No
      decisionComboDam.set(2);

      toStateName = stateItem.getOverrideTransitionToStateName(decRevArt, decisionComboDam);
      Assert.assertEquals(DecisionReviewState.Completed.getPageName(), toStateName);
   }

}