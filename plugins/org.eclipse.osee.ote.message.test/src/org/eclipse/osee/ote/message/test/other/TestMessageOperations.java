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
package org.eclipse.osee.ote.message.test.other;

import org.eclipse.osee.framework.jdk.core.util.benchmark.Benchmark;
import org.eclipse.osee.ote.message.MessageSystemException;
import org.eclipse.osee.ote.message.data.MessageData;
import org.eclipse.osee.ote.message.enums.MemType;
import org.eclipse.osee.ote.message.listener.IOSEEMessageListener;
import org.eclipse.osee.ote.message.test.mock.TestMessage;
import org.eclipse.osee.ote.message.test.mock.UnitTestSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMessageOperations {
   private UnitTestSupport support;

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
   }

   @Before
   public void setUp() throws Exception {
      support = new UnitTestSupport();
   }

   @After
   public void tearDown() throws Exception {
      support.cleanup();
   }

   @Test
   public void testTransmissionRate() throws InterruptedException {
      Benchmark.setBenchmarkingEnabled(true);
      TestMessage msg = new TestMessage();
      final Benchmark bm = new Benchmark("transmission rate");
      msg.addListener(new IOSEEMessageListener() {
         public void onDataAvailable(MessageData data, MemType type) throws MessageSystemException {
            bm.samplePoint();
         }

         public void onInitListener() throws MessageSystemException {

         }

      });
      int time = 5000; // 5 seconds
      support.activateMsg(msg);
      int period = (int) Math.round(1000.0 / msg.getRate());
      int expectedXmits = (int) Math.round(msg.getRate()) * (time / 1000);
      Thread.sleep(time + 10);
      Assert.assertEquals(expectedXmits, bm.getTotalSamples());
      long avg = bm.getAverage() / 1000;
      Assert.assertTrue("period is out of range:expected " + period + ", actual " + avg, support.inRange(period, 1,
            (int) avg));
   }

   @Test
   public void testCheckForTransmission() throws InterruptedException {
      TestMessage msg = new TestMessage();
      support.activateMsg(msg);
      int time = 1000;
      int expectedXmits = (int) Math.round(msg.getRate()) * (time / 1000);
      support.checkForTransmission(msg, expectedXmits, time + 10);

      support.checkForTransmissionFail(msg, expectedXmits, time - 10);
   }

}
