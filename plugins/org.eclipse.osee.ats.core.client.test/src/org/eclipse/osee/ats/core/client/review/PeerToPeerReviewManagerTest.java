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
package org.eclipse.osee.ats.core.client.review;

import java.util.Date;
import junit.framework.Assert;
import org.eclipse.osee.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.util.AtsUsersClient;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test unit for {@link PeerToPeerReviewManager}
 *
 * @author Donald G. Dunne
 */
public class PeerToPeerReviewManagerTest extends PeerToPeerReviewManager {

   @BeforeClass
   @AfterClass
   public static void cleanup() throws Exception {
      AtsTestUtil.cleanup();
   }

   @org.junit.Test
   public void testCreateNewPeerToPeerReview__Base() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("PeerToPeerReviewManagerTest - Base");
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();

      // create and transition peer review
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      String reviewTitle = "Test Review - " + teamArt.getName();

      PeerToPeerReviewArtifact peerArt =
         PeerToPeerReviewManager.createNewPeerToPeerReview(teamArt, reviewTitle,
            AtsTestUtil.getAnalyzeStateDef().getPageName(), new Date(), AtsUsersClient.getUser(), transaction);
      transaction.execute();

      Assert.assertNotNull(peerArt);
      Assert.assertFalse(
         String.format("PeerToPeer Review artifact should not be dirty [%s]", Artifacts.getDirtyReport(peerArt)),
         peerArt.isDirty());
      Assert.assertEquals(PeerToPeerReviewState.Prepare.getPageName(), peerArt.getCurrentStateName());
      Assert.assertEquals("Joe Smith", peerArt.getStateMgr().getAssigneesStr());
      Assert.assertEquals("Joe Smith", peerArt.getCreatedBy().getName());
      Assert.assertEquals(AtsTestUtil.getAnalyzeStateDef().getPageName(),
         peerArt.getSoleAttributeValue(AtsAttributeTypes.RelatedToState));

   }

   @org.junit.Test
   public void testCreateNewPeerToPeerReview__Simple() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("PeerToPeerReviewManagerTest - Simple");
      TeamWorkFlowArtifact teamArt = AtsTestUtil.getTeamWf();

      // create and transition peer review
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      String reviewTitle = "Test Review - " + teamArt.getName();

      PeerToPeerReviewArtifact peerArt =
         PeerToPeerReviewManager.createNewPeerToPeerReview(teamArt, reviewTitle,
            AtsTestUtil.getAnalyzeStateDef().getPageName(), transaction);
      transaction.execute();

      Assert.assertNotNull(peerArt);
      Assert.assertFalse(
         String.format("PeerToPeer Review artifact should not be dirty [%s]", Artifacts.getDirtyReport(peerArt)),
         peerArt.isDirty());
      Assert.assertEquals(PeerToPeerReviewState.Prepare.getPageName(), peerArt.getCurrentStateName());
      Assert.assertEquals("Joe Smith", peerArt.getStateMgr().getAssigneesStr());
      Assert.assertEquals(AtsTestUtil.getAnalyzeStateDef().getPageName(),
         peerArt.getSoleAttributeValue(AtsAttributeTypes.RelatedToState));

   }
}