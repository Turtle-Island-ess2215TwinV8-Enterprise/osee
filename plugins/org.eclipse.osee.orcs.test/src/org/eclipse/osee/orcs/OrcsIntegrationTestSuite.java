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
package org.eclipse.osee.orcs;

import org.eclipse.osee.orcs.api.OrcsAttributeLoadingTest;
import org.eclipse.osee.orcs.api.OrcsAttributeSearchTest;
import org.eclipse.osee.orcs.api.OrcsBranchTest;
import org.eclipse.osee.orcs.api.OrcsPortingTest;
import org.eclipse.osee.orcs.api.OrcsQueryTest;
import org.eclipse.osee.orcs.api.OrcsRelationLoadingTest;
import org.eclipse.osee.orcs.api.OrcsTransactionTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Roberto E. Escobar
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   OrcsAttributeLoadingTest.class,
   OrcsAttributeSearchTest.class,
   OrcsBranchTest.class,
   OrcsPortingTest.class,
   OrcsQueryTest.class,
   OrcsRelationLoadingTest.class,
   OrcsTransactionTest.class})
public class OrcsIntegrationTestSuite {
   // Test Suite
}
