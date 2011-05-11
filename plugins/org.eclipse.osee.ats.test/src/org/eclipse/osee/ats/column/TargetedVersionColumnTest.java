/*
 * Created on Nov 10, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.column.AssigneeColumn;
import org.eclipse.osee.ats.column.TargetedVersionColumn;
import org.eclipse.osee.ats.util.DemoTestUtil;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.support.test.util.DemoSawBuilds;
import org.eclipse.osee.support.test.util.DemoWorkType;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.Assert;

/**
 * @tests TargetedVersionColumn
 * @author Donald G. Dunne
 */
public class TargetedVersionColumnTest {

   @org.junit.Test
   public void testGetColumnText() throws Exception {
      SevereLoggingMonitor loggingMonitor = TestUtil.severeLoggingStart();

      TeamWorkFlowArtifact reqArt =
         (TeamWorkFlowArtifact) DemoTestUtil.getUncommittedActionWorkflow(DemoWorkType.Requirements);
      Assert.assertEquals(DemoSawBuilds.SAW_Bld_2.getName(),
         TargetedVersionColumn.getInstance().getColumnText(reqArt, AssigneeColumn.getInstance(), 0));

      TeamWorkFlowArtifact codeArt =
         (TeamWorkFlowArtifact) DemoTestUtil.getUncommittedActionWorkflow(DemoWorkType.Code);
      Assert.assertEquals(DemoSawBuilds.SAW_Bld_2.getName(),
         TargetedVersionColumn.getInstance().getColumnText(codeArt, AssigneeColumn.getInstance(), 0));

      Artifact actionArt = reqArt.getParentActionArtifact();
      Assert.assertEquals(DemoSawBuilds.SAW_Bld_2.getName(),
         TargetedVersionColumn.getInstance().getColumnText(actionArt, AssigneeColumn.getInstance(), 0));

      TeamWorkFlowArtifact toolsArt = DemoTestUtil.getToolsTeamWorkflow();
      Assert.assertEquals("",
         TargetedVersionColumn.getInstance().getColumnText(toolsArt, AssigneeColumn.getInstance(), 0));

      actionArt = toolsArt.getParentActionArtifact();
      Assert.assertEquals("",
         TargetedVersionColumn.getInstance().getColumnText(actionArt, AssigneeColumn.getInstance(), 0));

      TestUtil.severeLoggingEnd(loggingMonitor);
   }

}