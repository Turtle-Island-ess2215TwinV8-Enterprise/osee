/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.workflow;

import junit.framework.Assert;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.workflow.ChangeType;
import org.eclipse.osee.ats.api.workflow.IAtsWorkData;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AtsWorkItemStoreServiceImpl;
import org.eclipse.osee.ats.mocks.MockWorkItem;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.junit.Test;

/**
 * Test case for {@link AtsWorkItemStoreServiceImpl}
 * 
 * @author Donald G. Dunne
 */
public class AtsWorkItemStoreServiceImplTest {

   @Test
   public void testGetWorkData() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName() + ".testGetWorkData");
      TeamWorkFlowArtifact teamWf = AtsTestUtil.getTeamWf();

      AtsWorkItemStoreServiceImpl store = new AtsWorkItemStoreServiceImpl();
      IAtsWorkData workData = store.getWorkData(teamWf);
      Assert.assertNotNull(workData);

      MockWorkItem item = new MockWorkItem(teamWf.getGuid());
      workData = store.getWorkData(item);
      Assert.assertNotNull(workData);
   }

   @Test
   public void testGetChangeTypeAndString() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName() + ".testGetChangeType");
      TeamWorkFlowArtifact teamWf = AtsTestUtil.getTeamWf();
      teamWf.setSoleAttributeValue(AtsAttributeTypes.ChangeType, ChangeType.Problem.name());
      teamWf.persist(getClass().getName());

      AtsWorkItemStoreServiceImpl store = new AtsWorkItemStoreServiceImpl();
      Assert.assertEquals(ChangeType.Problem, store.getChangeType(teamWf));
      Assert.assertEquals("Problem", store.getChangeTypeStr(teamWf));

      teamWf.deleteAttributes(AtsAttributeTypes.ChangeType);
      teamWf.persist(getClass().getName());

      Assert.assertEquals(ChangeType.None, store.getChangeType(teamWf));
      Assert.assertEquals("None", store.getChangeTypeStr(teamWf));
   }

   @Test
   public void testGetArtifactType() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName() + ".testGetArtifactType");
      TeamWorkFlowArtifact teamWf = AtsTestUtil.getTeamWf();

      AtsWorkItemStoreServiceImpl store = new AtsWorkItemStoreServiceImpl();
      Assert.assertEquals(AtsArtifactTypes.TeamWorkflow, store.getArtifactType(teamWf));
   }

   @Test(expected = OseeArgumentException.class)
   public void testGetArtifactType_null() throws OseeCoreException {
      AtsWorkItemStoreServiceImpl store = new AtsWorkItemStoreServiceImpl();
      Assert.assertNull(null, store.getArtifactType(new MockWorkItem("Asdf")));
   }

}
