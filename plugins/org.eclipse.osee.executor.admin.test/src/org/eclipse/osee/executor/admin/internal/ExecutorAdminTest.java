/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.executor.admin.internal;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import org.eclipse.osee.event.EventService;
import org.eclipse.osee.logger.Log;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Roberto E. Escobar
 */
public class ExecutorAdminTest {

   //@formatter:off
   @Mock private Log logger;
   @Mock private EventService eventService;
   //@formatter:on

   private ExecutorAdminImpl admin;

   @Before
   public void setUp() {
      MockitoAnnotations.initMocks(this);

      admin = new ExecutorAdminImpl();
      admin.setLogger(logger);
      admin.setEventService(eventService);

      admin.start(new HashMap<String, Object>());
   }

   @After
   public void tearDown() {
      if (admin != null) {
         admin.stop(new HashMap<String, Object>());
      }
   }

   @Test
   public void testGetService() throws Exception {
      ExecutorService defaultService = admin.getDefaultExecutor();
      Assert.assertNotNull(defaultService);

      ExecutorService serviceByName = admin.getExecutor("default.executor");
      Assert.assertNotNull(serviceByName);
      Assert.assertEquals(serviceByName, defaultService);

      ExecutorService anotherExecutor = admin.getExecutor("hello");
      Assert.assertNotNull(anotherExecutor);
      Assert.assertTrue(!anotherExecutor.equals(defaultService));
   }

   @Test
   public void testTerminateExecutorService() throws Exception {
      ExecutorService anotherExecutor = admin.getExecutor("hello");
      Assert.assertNotNull(anotherExecutor);

      ExecutorService second = admin.getExecutor("hello");
      Assert.assertEquals(anotherExecutor, second);

      second.shutdown();
      ExecutorService third = admin.getExecutor("hello");
      Assert.assertFalse(third.equals(second));
   }
}
