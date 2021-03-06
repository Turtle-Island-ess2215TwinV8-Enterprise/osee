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
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.review;

import java.util.Arrays;
import junit.framework.Assert;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.ReviewBlockType;
import org.eclipse.osee.ats.api.workdef.StateEventType;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.workflow.transition.MockTransitionHelper;
import org.eclipse.osee.ats.core.client.review.PeerReviewDefinitionManager;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewState;
import org.eclipse.osee.ats.core.client.review.ReviewManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionManager;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionOption;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.mocks.MockPeerReviewDefinition;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test unit for {@link PeerReviewDefinitionManager}
 * 
 * @author Donald G. Dunne
 */
public class PeerReviewDefinitionManagerTest extends PeerReviewDefinitionManager {

   @BeforeClass
   @AfterClass
   public static void cleanup() throws Exception {
      AtsTestUtil.cleanup();
   }

   @org.junit.Test
   public void testCreatePeerReviewDuringTransition() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("PeerReviewDefinitionManagerTest");

      // configure WorkDefinition to create a new Review on transition to Implement
      IAtsStateDefinition implement = AtsTestUtil.getImplementStateDef();

      MockPeerReviewDefinition revDef = new MockPeerReviewDefinition("Create New on Implement");
      revDef.setBlockingType(ReviewBlockType.Transition);
      revDef.setDescription("the description");
      revDef.setRelatedToState(implement.getName());
      revDef.setStateEventType(StateEventType.TransitionTo);
      revDef.setReviewTitle("This is my review title");
      revDef.getAssignees().add(SystemUser.UnAssigned.getUserId());

      implement.getPeerReviews().add(revDef);

      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();
      Assert.assertEquals("No reviews should be present", 0, ReviewManager.getReviews(teamArt).size());

      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      MockTransitionHelper helper =
         new MockTransitionHelper(getClass().getSimpleName(), Arrays.asList(teamArt), implement.getName(),
            Arrays.asList(AtsClientService.get().getUserAdmin().getCurrentUser()), null, TransitionOption.None);
      TransitionManager transMgr = new TransitionManager(helper, transaction);
      TransitionResults results = transMgr.handleAll();
      transaction.execute();

      Assert.assertTrue(results.toString(), results.isEmpty());

      Assert.assertEquals("One review should be present", 1, ReviewManager.getReviews(teamArt).size());
      PeerToPeerReviewArtifact decArt = (PeerToPeerReviewArtifact) ReviewManager.getReviews(teamArt).iterator().next();

      Assert.assertEquals(PeerToPeerReviewState.Prepare.getName(), decArt.getCurrentStateName());
      Assert.assertEquals("UnAssigned", decArt.getStateMgr().getAssigneesStr());
      Assert.assertEquals(ReviewBlockType.Transition.name(),
         decArt.getSoleAttributeValue(AtsAttributeTypes.ReviewBlocks));
      Assert.assertEquals("This is my review title", decArt.getName());
      Assert.assertEquals("the description", decArt.getSoleAttributeValue(AtsAttributeTypes.Description));
      Assert.assertEquals(implement.getName(), decArt.getSoleAttributeValue(AtsAttributeTypes.RelatedToState));

      AtsTestUtil.validateArtifactCache();
   }

}
