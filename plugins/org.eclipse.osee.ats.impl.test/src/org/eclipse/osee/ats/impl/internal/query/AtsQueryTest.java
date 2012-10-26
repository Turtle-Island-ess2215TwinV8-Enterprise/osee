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
package org.eclipse.osee.ats.impl.internal.query;

import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workflow.IAtsWorkData;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AtsQueryTest {

   // @formatter:off
   @Mock private IAtsWorkItemService mockWorkItemService;
   @Mock private IAtsWorkItem wf1;
   @Mock private IAtsWorkItem wf2;
   @Mock private IAtsWorkItem wf3;
   @Mock private IAtsWorkItem wf4;
   @Mock private IAtsWorkItem wf5;
   @Mock private IAtsWorkItem wf6;
   @Mock private IAtsWorkData workData1;
   @Mock private IAtsWorkData workData2;
   @Mock private IAtsWorkData workData3;
   // @formatter:on

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);
   }

   @Test
   public void testGetItems() {
      List<IAtsWorkItem> workItems1 = new ArrayList<IAtsWorkItem>();
      workItems1.add(wf1);
      workItems1.add(wf2);
      AtsQuery query = new AtsQuery(workItems1, mockWorkItemService, null);
      Assert.assertEquals(2, query.getItems().size());
      Assert.assertTrue(query.getItems().contains(wf1));
      Assert.assertTrue(query.getItems().contains(wf2));
   }

   @Test
   public void testIsOfType() throws OseeCoreException {
      List<IAtsWorkItem> workItems = new ArrayList<IAtsWorkItem>();
      workItems.add(wf1);
      workItems.add(wf2);
      when(mockWorkItemService.isOfType(wf1, AtsArtifactTypes.Task)).thenReturn(false);
      when(mockWorkItemService.isOfType(wf2, AtsArtifactTypes.Task)).thenReturn(true);
      AtsQuery query = new AtsQuery(workItems, mockWorkItemService, null);
      Assert.assertEquals(1, query.isOfType(AtsArtifactTypes.Task).getItems().size());
      Assert.assertEquals(wf2, query.isOfType(AtsArtifactTypes.Task).getItems().iterator().next());
   }

   @Test
   public void testUnion() throws OseeCoreException {
      List<IAtsWorkItem> workItems1 = new ArrayList<IAtsWorkItem>();
      workItems1.add(wf1);
      workItems1.add(wf2);
      AtsQuery query1 = new AtsQuery(workItems1, mockWorkItemService, null);

      List<IAtsWorkItem> workItems2 = new ArrayList<IAtsWorkItem>();
      workItems2.add(wf3);
      workItems2.add(wf4);
      AtsQuery query2 = new AtsQuery(workItems2, mockWorkItemService, null);

      List<IAtsWorkItem> workItems3 = new ArrayList<IAtsWorkItem>();
      workItems3.add(wf5);
      workItems3.add(wf6);
      AtsQuery query3 = new AtsQuery(workItems3, mockWorkItemService, null);

      query1.union(query2, query3);

      Assert.assertEquals(6, query1.getItems().size());
      Assert.assertTrue(query1.getItems().contains(wf1));
      Assert.assertTrue(query1.getItems().contains(wf2));
      Assert.assertTrue(query1.getItems().contains(wf3));
      Assert.assertTrue(query1.getItems().contains(wf4));
      Assert.assertTrue(query1.getItems().contains(wf5));
      Assert.assertTrue(query1.getItems().contains(wf6));
   }

   @Test
   public void testIsStateType() throws OseeCoreException {
      List<IAtsWorkItem> workItems1 = new ArrayList<IAtsWorkItem>();
      workItems1.add(wf1);
      when(mockWorkItemService.getWorkData(wf1)).thenReturn(workData1);
      when(workData1.isCompleted()).thenReturn(true);
      when(workData1.isCancelled()).thenReturn(false);
      when(workData1.isInWork()).thenReturn(false);
      workItems1.add(wf2);
      when(mockWorkItemService.getWorkData(wf2)).thenReturn(workData2);
      when(workData2.isCompleted()).thenReturn(false);
      when(workData2.isCancelled()).thenReturn(true);
      when(workData2.isInWork()).thenReturn(false);
      workItems1.add(wf3);
      when(mockWorkItemService.getWorkData(wf3)).thenReturn(workData3);
      when(workData3.isCompleted()).thenReturn(false);
      when(workData3.isCancelled()).thenReturn(false);
      when(workData3.isInWork()).thenReturn(true);

      List<IAtsWorkItem> workItems = new ArrayList<IAtsWorkItem>();
      workItems.addAll(workItems1);
      AtsQuery query = new AtsQuery(workItems, mockWorkItemService, null);
      Assert.assertEquals(1, query.isStateType(StateType.Completed).getItems().size());
      Assert.assertEquals(wf1, query.isStateType(StateType.Completed).getItems().iterator().next());

      workItems.clear();
      workItems.addAll(workItems1);
      query = new AtsQuery(workItems, mockWorkItemService, null);
      Assert.assertEquals(1, query.isStateType(StateType.Cancelled).getItems().size());
      Assert.assertEquals(wf2, query.isStateType(StateType.Cancelled).getItems().iterator().next());

      workItems.clear();
      workItems.addAll(workItems1);
      query = new AtsQuery(workItems, mockWorkItemService, null);
      Assert.assertEquals(1, query.isStateType(StateType.Working).getItems().size());
      Assert.assertEquals(wf3, query.isStateType(StateType.Working).getItems().iterator().next());
   }

   @Test
   public void testWithOrValue() throws OseeCoreException {
      List<Object> attrValues = new ArrayList<Object>();
      attrValues.add("this");
      attrValues.add("and");
      List<IAtsWorkItem> workItems1 = new ArrayList<IAtsWorkItem>();
      workItems1.add(wf1);
      when(mockWorkItemService.getAttributeValues(wf1, CoreAttributeTypes.StaticId)).thenReturn(attrValues);

      List<Object> attrValues2 = new ArrayList<Object>();
      attrValues2.add("this");
      attrValues2.add("or");
      workItems1.add(wf2);
      when(mockWorkItemService.getAttributeValues(wf2, CoreAttributeTypes.StaticId)).thenReturn(attrValues2);

      AtsQuery query = new AtsQuery(workItems1, mockWorkItemService, null);
      Assert.assertEquals(1, query.withOrValue(CoreAttributeTypes.StaticId, Arrays.asList("or")).getItems().size());

      query = new AtsQuery(workItems1, mockWorkItemService, null);
      Assert.assertEquals(2,
         query.withOrValue(CoreAttributeTypes.StaticId, Arrays.asList("or", "and")).getItems().size());

      query = new AtsQuery(workItems1, mockWorkItemService, null);
      Assert.assertEquals(2, query.withOrValue(CoreAttributeTypes.StaticId, Arrays.asList("this")).getItems().size());

      query = new AtsQuery(workItems1, mockWorkItemService, null);
      Assert.assertEquals(0, query.withOrValue(CoreAttributeTypes.StaticId, Arrays.asList("not")).getItems().size());

      query = new AtsQuery(workItems1, mockWorkItemService, null);
      Assert.assertEquals(0, query.withOrValue(CoreAttributeTypes.Name, Arrays.asList("this")).getItems().size());
   }

}
