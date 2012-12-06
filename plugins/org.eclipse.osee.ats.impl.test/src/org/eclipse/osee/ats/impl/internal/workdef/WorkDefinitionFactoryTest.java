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
package org.eclipse.osee.ats.impl.internal.workdef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.Assert;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinitionService;
import org.eclipse.osee.ats.api.util.WorkDefinitionMatch;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionService;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionStore;
import org.eclipse.osee.ats.api.workdef.WorkDefinitionDefault;
import org.eclipse.osee.ats.api.workflow.IAtsPeerToPeerReview;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.framework.core.util.XResultData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test case for {@link WorkDefinitionFactory}
 * 
 * @author Donald G. Dunne
 */
public class WorkDefinitionFactoryTest {

   // @formatter:off
   @Mock IAtsTeamDefinition topTeamDef;
   @Mock IAtsTeamDefinition projTeamDef;
   @Mock IAtsTeamDefinition featureTeamDef;
   @Mock IAtsWorkItemService workItemService;
   @Mock IAtsWorkDefinitionService workDefinitionService;
   @Mock IAtsWorkDefinitionStore workDefinitionStore;
   @Mock IAtsTeamDefinitionService teamDefinitionService;
   @Mock IAtsActionableItem actionableItem;
   @Mock IAtsPeerToPeerReview peerReview;
   @Mock XResultData resultData;
   @Mock IAtsWorkDefinition workDefinition;
   @Mock IAtsTeamWorkflow teamWf;

   // @formatter:on

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
      Mockito.when(topTeamDef.getParentTeamDef()).thenReturn(null);
      Mockito.when(projTeamDef.getParentTeamDef()).thenReturn(topTeamDef);
      Mockito.when(featureTeamDef.getParentTeamDef()).thenReturn(projTeamDef);
   }

   @Test
   public void testGetWorkDefinitionForPeerToPeerReviewIAtsPeerToPeerReview() throws Exception {
      Mockito.when(workItemService.getParentTeamWorkflow(peerReview)).thenReturn(teamWf);
      WorkDefinitionFactory factory =
         new WorkDefinitionFactory(workItemService, workDefinitionService, workDefinitionStore, teamDefinitionService);
      Mockito.when(teamDefinitionService.getTeamDefinition(teamWf)).thenReturn(topTeamDef);
      Mockito.when(workItemService.getAttributeValues(topTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         Collections.emptyList());
      Mockito.when(
         workDefinitionService.getWorkDef(Matchers.eq(WorkDefinitionDefault.PeerToPeerWorkflowDefinitionId),
            (XResultData) Matchers.anyObject())).thenReturn(workDefinition);

      WorkDefinitionMatch match = factory.getWorkDefinitionForPeerToPeerReview(peerReview);
      Assert.assertEquals(workDefinition, match.getWorkDefinition());
   }

   @Test
   public void testGetWorkDefinitionForPeerToPeerReviewNotYetCreated() throws Exception {
      WorkDefinitionFactory factory =
         new WorkDefinitionFactory(workItemService, workDefinitionService, workDefinitionStore, teamDefinitionService);
      Mockito.when(teamDefinitionService.getTeamDefinition(teamWf)).thenReturn(topTeamDef);
      Mockito.when(workItemService.getAttributeValues(topTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         Collections.emptyList());
      Mockito.when(
         workDefinitionService.getWorkDef(Matchers.eq(WorkDefinitionDefault.PeerToPeerWorkflowDefinitionId),
            (XResultData) Matchers.anyObject())).thenReturn(workDefinition);

      WorkDefinitionMatch match = factory.getWorkDefinitionForPeerToPeerReviewNotYetCreated(teamWf);
      Assert.assertEquals(workDefinition, match.getWorkDefinition());
   }

   @Test
   public void testGetWorkDefinitionForPeerToPeerReviewIAtsTeamWorkflowIAtsPeerToPeerReview__fromReview() throws Exception {
      WorkDefinitionFactory factory =
         new WorkDefinitionFactory(workItemService, workDefinitionService, workDefinitionStore, teamDefinitionService);
      List<Object> attrValues = new ArrayList<Object>();
      attrValues.add("WorkDefinition_fromReview");
      Mockito.when(workItemService.getAttributeValues(peerReview, AtsAttributeTypes.WorkflowDefinition)).thenReturn(
         attrValues);
      Mockito.when(workDefinitionStore.getWorkDefinitionAttribute(peerReview)).thenReturn("WorkDefinition_fromReview");
      Mockito.when(
         workDefinitionService.getWorkDef(Matchers.eq("WorkDefinition_fromReview"), (XResultData) Matchers.anyObject())).thenReturn(
         workDefinition);

      WorkDefinitionMatch match = factory.getWorkDefinitionForPeerToPeerReview(peerReview);
      Assert.assertEquals(workDefinition, match.getWorkDefinition());
   }

   @Test
   public void testGetWorkDefinitionForPeerToPeerReviewNotYetCreatedAndStandalone() throws Exception {
      WorkDefinitionFactory factory =
         new WorkDefinitionFactory(workItemService, workDefinitionService, workDefinitionStore, teamDefinitionService);
      Mockito.when(teamDefinitionService.getTeamDefinition(teamWf)).thenReturn(featureTeamDef);
      List<Object> attrValues = new ArrayList<Object>();
      attrValues.add("WorkDefinition_test");
      Mockito.when(workItemService.getAttributeValues(topTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         attrValues);
      Mockito.when(
         workDefinitionService.getWorkDef(Matchers.eq("WorkDefinition_test"), (XResultData) Matchers.anyObject())).thenReturn(
         workDefinition);
      Mockito.when(actionableItem.getTeamDefinitionInherited()).thenReturn(topTeamDef);

      WorkDefinitionMatch match =
         factory.getWorkDefinitionForPeerToPeerReviewNotYetCreatedAndStandalone(actionableItem);
      Assert.assertEquals(workDefinition, match.getWorkDefinition());
   }

   @Test
   public void testGetPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse() throws Exception {
      WorkDefinitionFactory factory =
         new WorkDefinitionFactory(workItemService, workDefinitionService, workDefinitionStore, teamDefinitionService);
      Mockito.when(workItemService.getAttributeValues(topTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         Collections.emptyList());
      Mockito.when(workItemService.getAttributeValues(projTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         Collections.emptyList());
      Mockito.when(workItemService.getAttributeValues(featureTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         Collections.emptyList());

      WorkDefinitionMatch peerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse =
         factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(topTeamDef);
      Assert.assertFalse(peerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse.isMatched());
      Assert.assertFalse(factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(projTeamDef).isMatched());
      Assert.assertFalse(factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(featureTeamDef).isMatched());

      List<Object> attrValues = new ArrayList<Object>();
      attrValues.add("WorkDefinition_test");
      Mockito.when(workItemService.getAttributeValues(topTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         attrValues);
      Mockito.when(
         workDefinitionService.getWorkDef(Matchers.eq("WorkDefinition_test"), (XResultData) Matchers.anyObject())).thenReturn(
         workDefinition);

      peerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse =
         factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(topTeamDef);
      Assert.assertTrue(peerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse.isMatched());
      Assert.assertTrue(factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(projTeamDef).isMatched());
      Assert.assertTrue(factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(featureTeamDef).isMatched());
      Assert.assertEquals(workDefinition,
         factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(featureTeamDef).getWorkDefinition());
   }

   @Test
   public void testGetWorkDefinitionIAtsWorkItem() throws Exception {
      WorkDefinitionFactory factory =
         new WorkDefinitionFactory(workItemService, workDefinitionService, workDefinitionStore, teamDefinitionService);
      Mockito.when(workItemService.getParentTeamWorkflow(peerReview)).thenReturn(teamWf);
      Mockito.when(teamDefinitionService.getTeamDefinition(teamWf)).thenReturn(topTeamDef);
      Mockito.when(workItemService.getAttributeValues(topTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         Collections.emptyList());
      Mockito.when(
         workDefinitionService.getWorkDef(Matchers.eq(WorkDefinitionDefault.PeerToPeerWorkflowDefinitionId),
            (XResultData) Matchers.anyObject())).thenReturn(workDefinition);
      Mockito.when(workItemService.isOfType(peerReview, IAtsPeerToPeerReview.class)).thenReturn(true);

      WorkDefinitionMatch match = factory.getWorkDefinition(peerReview);
      Assert.assertEquals(workDefinition, match.getWorkDefinition());
   }

}
