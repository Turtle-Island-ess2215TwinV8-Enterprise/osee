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
package org.eclipse.osee.orcs.rest.internal;

import org.eclipse.osee.orcs.rest.internal.search.dsl.DslTranslatorImplTest;
import org.eclipse.osee.orcs.rest.internal.search.dsl.SearchDslTest;
import org.eclipse.osee.orcs.rest.internal.search.predicate.AttributeTypePredicateHandlerTest;
import org.eclipse.osee.orcs.rest.internal.search.predicate.ExistsTypePredicateHandlerTest;
import org.eclipse.osee.orcs.rest.internal.search.predicate.GuidOrHridsPredicateHandlerTest;
import org.eclipse.osee.orcs.rest.internal.search.predicate.IdsPredicateHandlerTest;
import org.eclipse.osee.orcs.rest.internal.search.predicate.IsOfTypePredicateHandlerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author John R. Misinco
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   DslTranslatorImplTest.class,
   SearchDslTest.class,
   AttributeTypePredicateHandlerTest.class,
   ExistsTypePredicateHandlerTest.class,
   IdsPredicateHandlerTest.class,
   GuidOrHridsPredicateHandlerTest.class,
   IsOfTypePredicateHandlerTest.class,})
public class InternalTestSuite {
   // Test Suite
}
