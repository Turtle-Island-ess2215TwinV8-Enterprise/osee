/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.util;

import junit.framework.Assert;
import org.eclipse.osee.ats.api.workdef.ReviewBlockType;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil.AtsTestUtilState;
import org.eclipse.osee.ats.core.client.review.DecisionReviewArtifact;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.client.util.CopyActionDetails;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class CopyActionDetailsTest {

   @BeforeClass
   @AfterClass
   public static void cleanup() throws OseeCoreException {
      AtsTestUtil.cleanup();
   }

   @Test
   public void testGetDetailsStringForTeamWf() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName());
      String str = new CopyActionDetails(AtsTestUtil.getTeamWf()).getDetailsString();
      Assert.assertEquals(
         "\"Team Workflow\" - " + AtsTestUtil.getTeamWf().getHumanReadableId() + " - \"AtsTestUtil - Team WF [CopyActionDetailsTest]\"",
         str);
   }

   @Test
   public void testGetDetailsStringForTask() throws OseeCoreException {
      String str = new CopyActionDetails(AtsTestUtil.getOrCreateTaskOffTeamWf1()).getDetailsString();
      Assert.assertEquals(
         "\"Task\" - " + AtsTestUtil.getOrCreateTaskOffTeamWf1().getHumanReadableId() + " - \"AtsTestUtil - Task [CopyActionDetailsTest]\"",
         str);
   }

   @Test
   public void testGetDetailsStringForDecisionReview() throws OseeCoreException {
      DecisionReviewArtifact review =
         AtsTestUtil.getOrCreateDecisionReview(ReviewBlockType.Commit, AtsTestUtilState.Analyze);
      String str = new CopyActionDetails(review).getDetailsString();
      Assert.assertEquals(
         "\"Decision Review\" - " + review.getHumanReadableId() + " - \"AtsTestUtil Test Decision Review\"", str);
      review.persist(getClass().getSimpleName());
   }

   @Test
   public void testGetDetailsStringForPeerReview() throws OseeCoreException {
      PeerToPeerReviewArtifact review =
         AtsTestUtil.getOrCreatePeerReview(ReviewBlockType.None, AtsTestUtilState.Analyze, null);
      String str = new CopyActionDetails(review).getDetailsString();
      Assert.assertEquals(
         "\"PeerToPeer Review\" - " + review.getHumanReadableId() + " - \"AtsTestUtil Test Peer Review\"", str);
      review.persist(getClass().getSimpleName());
   }
}
