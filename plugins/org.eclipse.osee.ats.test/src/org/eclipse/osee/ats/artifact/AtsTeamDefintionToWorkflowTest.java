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
package org.eclipse.osee.ats.artifact;

import static org.junit.Assert.assertFalse;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Donald G. Dunne
 */
public class AtsTeamDefintionToWorkflowTest {

   @org.junit.Test
   public void testTeamDefinitionToWorkflow() throws Exception {
      boolean error = false;
      StringBuffer sb = new StringBuffer();
      for (Artifact artifact : ArtifactQuery.getArtifactListFromType(AtsArtifactTypes.TeamDefinition,
         AtsUtil.getAtsBranch())) {
         TeamDefinitionArtifact teamDef = (TeamDefinitionArtifact) artifact;
         if (teamDef.isActionable() && teamDef.getWorkFlowDefinition() == null) {
            sb.append("Team Definition \"" + teamDef + "\" has no Work Flow associated and is Actionable.");
            error = true;
         }
      }
      assertFalse(sb.toString(), error);
   }

}