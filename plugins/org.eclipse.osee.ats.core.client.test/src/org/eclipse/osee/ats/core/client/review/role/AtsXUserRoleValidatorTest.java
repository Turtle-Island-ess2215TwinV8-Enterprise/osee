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
package org.eclipse.osee.ats.core.client.review.role;

import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;
import org.eclipse.osee.ats.core.client.review.role.AtsXUserRoleValidator;
import org.eclipse.osee.ats.core.client.review.role.Role;
import org.eclipse.osee.ats.core.client.review.role.UserRole;
import org.eclipse.osee.ats.core.client.review.role.UserRoleError;
import org.eclipse.osee.ats.core.validator.ValidatorTestUtil;
import org.eclipse.osee.ats.core.validator.WidgetResult;
import org.eclipse.osee.ats.core.validator.WidgetStatus;
import org.eclipse.osee.ats.core.workdef.StateDefinition;
import org.eclipse.osee.ats.core.workdef.WidgetDefinition;
import org.eclipse.osee.ats.core.workdef.WidgetOption;
import org.eclipse.osee.ats.core.workflow.WorkPageType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public class AtsXUserRoleValidatorTest {

   @org.junit.Test
   public void testValidateTransition() throws OseeCoreException {
      AtsXUserRoleValidator validator = new AtsXUserRoleValidator();

      WidgetDefinition widgetDef = new WidgetDefinition("test");
      widgetDef.setXWidgetName("xList");

      StateDefinition fromStateDef = new StateDefinition("from");
      fromStateDef.setWorkPageType(WorkPageType.Working);
      StateDefinition toStateDef = new StateDefinition("to");
      toStateDef.setWorkPageType(WorkPageType.Working);

      // Valid for anything not XIntegerDam
      WidgetResult result =
         validator.validateTransition(ValidatorTestUtil.emptyValueProvider, widgetDef, fromStateDef, toStateDef);
      ValidatorTestUtil.assertValidResult(result);

      widgetDef.setXWidgetName("XUserRoleViewer");

      // Not valid to have no roles
      result = validator.validateTransition(ValidatorTestUtil.emptyValueProvider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Invalid_Incompleted, result.getStatus());
      Assert.assertEquals(UserRoleError.OneRoleEntryRequired.getError(), result.getDetails());

      widgetDef.getOptions().add(WidgetOption.REQUIRED_FOR_TRANSITION);

      // Not valid to have no roles
      result = validator.validateTransition(ValidatorTestUtil.emptyValueProvider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Invalid_Incompleted, result.getStatus());
      Assert.assertEquals(UserRoleError.OneRoleEntryRequired.getError(), result.getDetails());
   }

   @org.junit.Test
   public void testValidateTransition_Roles() throws OseeCoreException {
      AtsXUserRoleValidator validator = new AtsXUserRoleValidator();

      WidgetDefinition widgetDef = new WidgetDefinition("test");
      widgetDef.setXWidgetName("XUserRoleViewer");

      StateDefinition fromStateDef = new StateDefinition("from");
      fromStateDef.setWorkPageType(WorkPageType.Working);
      StateDefinition toStateDef = new StateDefinition("to");
      toStateDef.setWorkPageType(WorkPageType.Working);

      UserRole author = new UserRole(Role.Author, "2134", 0.0, false);
      UserRole reviewer = new UserRole(Role.Reviewer, "123");
      List<UserRole> roles = Arrays.asList(author, reviewer);

      MockUserRoleValueProvider provider = new MockUserRoleValueProvider(roles);

      // Valid Roles
      WidgetResult result = validator.validateTransition(provider, widgetDef, fromStateDef, toStateDef);
      ValidatorTestUtil.assertValidResult(result);

      // Not valid to have no author
      roles = Arrays.asList(reviewer);
      provider = new MockUserRoleValueProvider(roles);
      result = validator.validateTransition(provider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Invalid_Incompleted, result.getStatus());
      Assert.assertEquals(UserRoleError.MustHaveAtLeastOneAuthor.getError(), result.getDetails());

      // Not valid to have no reviewer
      roles = Arrays.asList(author);
      provider = new MockUserRoleValueProvider(roles);
      result = validator.validateTransition(provider, widgetDef, fromStateDef, toStateDef);
      Assert.assertEquals(WidgetStatus.Invalid_Incompleted, result.getStatus());
      Assert.assertEquals(UserRoleError.MustHaveAtLeastOneReviewer.getError(), result.getDetails());

   }

}