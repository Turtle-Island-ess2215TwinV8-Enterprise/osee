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
package org.eclipse.osee.orcs.rest;

import org.eclipse.osee.orcs.rest.internal.InternalTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author John Misinco
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({InternalTestSuite.class,})
public class OrcsRestTestSuite {
   // Test Suite
}