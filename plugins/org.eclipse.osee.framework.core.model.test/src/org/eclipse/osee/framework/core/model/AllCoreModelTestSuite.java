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
package org.eclipse.osee.framework.core.model;

import org.eclipse.osee.framework.core.model.access.AccessTestSuite;
import org.eclipse.osee.framework.core.model.cache.CacheTestSuite;
import org.eclipse.osee.framework.core.model.fields.FieldTestSuite;
import org.eclipse.osee.framework.core.model.type.TypeTestSuite;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({//
AccessTestSuite.class, //
   CacheTestSuite.class, //
   FieldTestSuite.class, //
   TypeTestSuite.class, //
})
/**
 * @author Roberto E. Escobar
 */
public class AllCoreModelTestSuite {
   @BeforeClass
   public static void setUp() throws Exception {
      OseeProperties.setIsInTest(true);
      System.out.println("\n\nBegin " + AllCoreModelTestSuite.class.getSimpleName());
   }

   @AfterClass
   public static void tearDown() throws Exception {
      System.out.println("End " + AllCoreModelTestSuite.class.getSimpleName());
   }
}