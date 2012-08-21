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
package org.eclipse.osee.ats.core.validator;

import java.util.Arrays;
import junit.framework.Assert;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workdef.WidgetResult;
import org.eclipse.osee.ats.api.workdef.WidgetStatus;
import org.eclipse.osee.ats.mocks.MockStateDefinition;
import org.eclipse.osee.ats.mocks.MockWidgetDefinition;
import org.eclipse.osee.framework.core.enums.WidgetOption;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * Test unit for {@link AtsXComboBooleanValidator}
 * 
 * @author Donald G. Dunne
 */
public class AtsXComboBooleanValidatorTest {

   @org.junit.Test
   public void testValidateTransition() throws OseeCoreException {
      AtsXComboBooleanValidator validator = new AtsXComboBooleanValidator();

      MockWidgetDefinition widgetDef = new MockWidgetDefinition("test");
      widgetDef.setXWidgetName("xLabel");

      MockStateDefinition fromStateDef = new MockStateDefinition("from");
      fromStateDef.setStateType(StateType.Working);
      MockStateDefinition toStateDef = new MockStateDefinition("to");
      toStateDef.setStateType(StateType.Working);

      // Valid for anything not XIntegerDam
      WidgetResult result =
         validator.validateTransition(ValidatorTestUtil.emptyValueProvider, widgetDef, fromStateDef, toStateDef);
      ValidatorTestUtil.assertValidResult(result);

      widgetDef.setXWidgetName("XComboBooleanDam");

      result = validator.validateTransition(ValidatorTestUtil.emptyValueProvider, widgetDef, fromStateDef, toStateDef);
      ValidatorTestUtil.assertValidResult(result);

      widgetDef.getOptions().add(WidgetOption.REQUIRED_FOR_TRANSITION);

      // Not valid if widgetDef required and no values set
      result = validator.validateTransition(ValidatorTestUtil.emptyValueProvider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Invalid_Incompleted, result.getStatus());

      // Check for "true" value
      MockValueProvider valueProvider = new MockValueProvider(Arrays.asList("true"));
      result = validator.validateTransition(valueProvider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Valid, result.getStatus());

      // Check for "false" value
      valueProvider = new MockValueProvider(Arrays.asList("false"));
      result = validator.validateTransition(valueProvider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Valid, result.getStatus());

      // Check for "junk" value
      valueProvider = new MockValueProvider(Arrays.asList("junk"));
      result = validator.validateTransition(valueProvider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Invalid_Range, result.getStatus());

   }
}
