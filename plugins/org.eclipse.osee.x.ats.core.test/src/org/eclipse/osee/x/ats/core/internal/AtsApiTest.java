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
package org.eclipse.osee.x.ats.core.internal;

import org.eclipse.osee.logger.Log;
import org.eclipse.osee.x.ats.core.mocks.MockLog;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Case for {@link AtsApiImpl}
 * 
 * @author Roberto E. Escobar
 */
public class AtsApiTest {

   @Test
   public void testDummy() {
      Log log = new MockLog();
      log.info("Testing...");
      Assert.assertEquals(1, 1);
   }
}
