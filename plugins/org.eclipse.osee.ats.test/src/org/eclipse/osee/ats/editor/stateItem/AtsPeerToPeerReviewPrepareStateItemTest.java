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
package org.eclipse.osee.ats.editor.stateItem;

import static org.junit.Assert.assertFalse;
import org.eclipse.osee.ats.core.AtsTestUtil;
import org.eclipse.osee.ats.core.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.review.PeerToPeerReviewState;
import org.eclipse.osee.ats.core.workdef.StateDefinition;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.ats.shared.AtsAttributeTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.DemoTestUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.ui.skynet.widgets.XComboDam;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.support.test.util.DemoActionableItems;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Case for {@link AtsPeerToPeerReviewPrepareStateItem}
 * 
 * @author Donald G. Dunne
 */
public class AtsPeerToPeerReviewPrepareStateItemTest {

   public static PeerToPeerReviewArtifact peerRevArt;

   @Before
   public void setUp() throws Exception {
      // This test should only be run on test db
      assertFalse("Test should not be run in production db", AtsUtil.isProductionDb());

      if (peerRevArt == null) {
         // setup fake review artifact with decision options set
         peerRevArt =
            (PeerToPeerReviewArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.PeerToPeerReview,
               AtsUtil.getAtsBranch());
         peerRevArt.setName(getClass().getSimpleName());
         // Setup actionable item so don't get error that there is no parent team workflow
         peerRevArt.getActionableItemsDam().addActionableItem(
            DemoTestUtil.getActionableItem(DemoActionableItems.CIS_Code));
         peerRevArt.persist(getClass().getSimpleName());
      }
   }

   @BeforeClass
   @AfterClass
   public static void testCleanup() throws Exception {
      AtsTestUtil.cleanupSimpleTest(AtsPeerToPeerReviewPrepareStateItemTest.class.getSimpleName());
   }

   @Test
   public void testTransitioning() throws OseeCoreException {
      Assert.assertNotNull(peerRevArt);

      // setup fake combo that will hold values
      XComboDam decisionComboDam = new XComboDam(AtsAttributeTypes.ReviewBlocks.getUnqualifiedName());
      decisionComboDam.setDataStrings(new String[] {"None", "Transition", "Commit"});
      Composite comp = new Composite(Displays.getActiveShell(), SWT.None);
      decisionComboDam.createWidgets(comp, SWT.NONE);
      decisionComboDam.setEnabled(true);
      decisionComboDam.setRequiredEntry(true);

      // verify enabled and required (Default)
      Assert.assertNull(peerRevArt.getParentAWA()); // condition that causes combo to disable
      Assert.assertTrue(decisionComboDam.getComboBox().isEnabled());
      Assert.assertTrue(decisionComboDam.isRequiredEntry());

      StateDefinition reviewStateDef =
         peerRevArt.getWorkDefinition().getStateByName(PeerToPeerReviewState.Prepare.getPageName());

      // make call to state item that should 
      AtsPeerToPeerReviewPrepareStateItem stateItem = new AtsPeerToPeerReviewPrepareStateItem();
      stateItem.xWidgetCreated(decisionComboDam, null, reviewStateDef, peerRevArt, true);

      // verify the decision combo has been disabled
      Assert.assertFalse(decisionComboDam.getComboBox().isEnabled());
      Assert.assertFalse(decisionComboDam.isRequiredEntry());

   }
}
