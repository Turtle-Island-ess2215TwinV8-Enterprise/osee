/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.column;

import org.eclipse.osee.ats.core.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.task.TaskArtifact;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.shared.AtsRelationTypes;
import org.eclipse.osee.ats.util.DemoTestUtil;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.support.test.util.DemoWorkType;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.Assert;

/**
 * @tests PriorityColumn
 * @author Donald G. Dunne
 */
public class PriorityColumnTest {

   @org.junit.Test
   public void testGetColumnText() throws Exception {
      SevereLoggingMonitor loggingMonitor = TestUtil.severeLoggingStart();

      TeamWorkFlowArtifact reqArt =
         (TeamWorkFlowArtifact) DemoTestUtil.getUncommittedActionWorkflow(DemoWorkType.Requirements);
      Assert.assertEquals("3", PriorityColumn.getInstance().getColumnText(reqArt, AssigneeColumn.getInstance(), 0));

      TeamWorkFlowArtifact codeArt =
         (TeamWorkFlowArtifact) DemoTestUtil.getUncommittedActionWorkflow(DemoWorkType.Code);
      Assert.assertEquals("3", PriorityColumn.getInstance().getColumnText(codeArt, AssigneeColumn.getInstance(), 0));

      PeerToPeerReviewArtifact peerArt =
         (PeerToPeerReviewArtifact) codeArt.getRelatedArtifact(AtsRelationTypes.TeamWorkflowToReview_Review);
      Assert.assertEquals("", PriorityColumn.getInstance().getColumnText(peerArt, AssigneeColumn.getInstance(), 0));

      TaskArtifact taskArt =
         (TaskArtifact) codeArt.getRelatedArtifacts(AtsRelationTypes.SmaToTask_Task).iterator().next();
      Assert.assertEquals("", PriorityColumn.getInstance().getColumnText(taskArt, AssigneeColumn.getInstance(), 0));

      Artifact actionArt = reqArt.getParentActionArtifact();
      Assert.assertEquals("3", PriorityColumn.getInstance().getColumnText(actionArt, AssigneeColumn.getInstance(), 0));

      TestUtil.severeLoggingEnd(loggingMonitor);
   }

}
