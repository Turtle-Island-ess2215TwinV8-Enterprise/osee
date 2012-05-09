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
package org.eclipse.osee.ats.core.client.review.defect;

import java.util.Arrays;
import java.util.Date;
import junit.framework.Assert;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.Disposition;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.InjectionActivity;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.Severity;
import org.eclipse.osee.ats.core.validator.ValidatorTestUtil;
import org.eclipse.osee.ats.core.validator.WidgetResult;
import org.eclipse.osee.ats.core.validator.WidgetStatus;
import org.eclipse.osee.ats.core.workdef.StateDefinition;
import org.eclipse.osee.ats.core.workdef.WidgetDefinition;
import org.eclipse.osee.ats.core.workdef.WidgetOption;
import org.eclipse.osee.ats.core.workflow.WorkPageType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * Test unit for {@link AtsXDefectValidator}
 *
 * @author Donald G. Dunne
 */
public class AtsXDefectValidatorTest {

   @org.junit.Test
   public void testValidateTransition() throws OseeCoreException {
      AtsXDefectValidator validator = new AtsXDefectValidator();

      WidgetDefinition widgetDef = new WidgetDefinition("test");
      widgetDef.setXWidgetName("xList");

      StateDefinition fromStateDef = new StateDefinition("from");
      fromStateDef.setWorkPageType(WorkPageType.Working);
      StateDefinition toStateDef = new StateDefinition("to");
      toStateDef.setWorkPageType(WorkPageType.Working);

      // Valid for anything not XDefectViewer
      WidgetResult result =
         validator.validateTransition(ValidatorTestUtil.emptyValueProvider, widgetDef, fromStateDef, toStateDef);
      ValidatorTestUtil.assertValidResult(result);

      widgetDef.setXWidgetName("XDefectViewer");

      result = validator.validateTransition(ValidatorTestUtil.emptyValueProvider, widgetDef, fromStateDef, toStateDef);
      ValidatorTestUtil.assertValidResult(result);

      widgetDef.getOptions().add(WidgetOption.REQUIRED_FOR_TRANSITION);

      // Reviews do not require Defects to be entered, even if required for transition
      result = validator.validateTransition(ValidatorTestUtil.emptyValueProvider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Valid, result.getStatus());
   }

   @org.junit.Test
   public void testValidateTransition__Defect() throws OseeCoreException {
      AtsXDefectValidator validator = new AtsXDefectValidator();

      WidgetDefinition widgetDef = new WidgetDefinition("test");
      widgetDef.setXWidgetName("XDefectViewer");

      StateDefinition fromStateDef = new StateDefinition("from");
      fromStateDef.setWorkPageType(WorkPageType.Working);
      StateDefinition toStateDef = new StateDefinition("to");
      toStateDef.setWorkPageType(WorkPageType.Working);

      ReviewDefectItem item = getValidItem();
      MockDefectValueProvider itemValueProvider = new MockDefectValueProvider(Arrays.asList(item));

      // Valid defect
      WidgetResult result = validator.validateTransition(itemValueProvider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Valid, result.getStatus());

      // Invalid Severity
      item = getValidItem();
      item.setSeverity(Severity.None);
      itemValueProvider = new MockDefectValueProvider(Arrays.asList(item));
      result = validator.validateTransition(itemValueProvider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Invalid_Incompleted, result.getStatus());
      Assert.assertEquals(ReviewDefectError.AllItemsMustBeMarkedAndClosed.getError(), result.getDetails());

      // Invalid Disposition
      item = getValidItem();
      item.setDisposition(Disposition.None);
      itemValueProvider = new MockDefectValueProvider(Arrays.asList(item));
      result = validator.validateTransition(itemValueProvider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Invalid_Incompleted, result.getStatus());
      Assert.assertEquals(ReviewDefectError.AllItemsMustBeMarkedAndClosed.getError(), result.getDetails());

      // Invalid closed
      item = getValidItem();
      item.setClosed(false);
      itemValueProvider = new MockDefectValueProvider(Arrays.asList(item));
      result = validator.validateTransition(itemValueProvider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Invalid_Incompleted, result.getStatus());
      Assert.assertEquals(ReviewDefectError.AllItemsMustBeMarkedAndClosed.getError(), result.getDetails());

   }

   private ReviewDefectItem getValidItem() {
      Date date = new Date();
      String userId = "1234";
      ReviewDefectItem item =
         new ReviewDefectItem(userId, Severity.Issue, Disposition.Accept, InjectionActivity.Software_Design,
            "this is the description", "this is the resolution", "this is the location", date);
      item.setClosed(true);
      return item;
   }
}