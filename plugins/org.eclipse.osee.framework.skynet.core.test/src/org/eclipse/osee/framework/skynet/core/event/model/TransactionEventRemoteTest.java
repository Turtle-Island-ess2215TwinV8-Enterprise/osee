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
package org.eclipse.osee.framework.skynet.core.event.model;

import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.TransactionEventTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * event loopback will test that remote messages get processed and treated like local messages by turning off local and
 * enabling remote to be loop-ed back without another client. same tests as base-class should still pass
 * 
 * @author Donald G. Dunne
 */
public class TransactionEventRemoteTest extends TransactionEventTest {

   @BeforeClass
   public static void setUp() {
      OseeEventManager.getPreferences().setEnableRemoteEventLoopback(true);
   }

   @AfterClass
   public static void tearDown() {
      OseeEventManager.getPreferences().setEnableRemoteEventLoopback(false);
   }

   @Override
   protected boolean isRemoteTest() {
      return true;
   }

}