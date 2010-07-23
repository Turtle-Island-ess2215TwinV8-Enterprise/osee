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
package org.eclipse.osee.client.integration.tests.suite;

import static org.junit.Assert.assertTrue;
import org.eclipse.osee.ats.test.AtsTest_Config_Suite;
import org.eclipse.osee.ats.test.AtsTest_Demo_Suite;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.messaging.event.res.test.FrameworkRes_Demo_Suite;
import org.eclipse.osee.framework.skynet.core.test.FrameworkCore_Demo_Suite;
import org.eclipse.osee.framework.ui.skynet.artifact.ArtifactSaveNotificationHandler;
import org.eclipse.osee.framework.ui.skynet.test.FrameworkUi_Demo_Suite;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({//
CoreRuntimeFeatureTestsSuite.class, //
			FrameworkCore_Demo_Suite.class, //
			FrameworkRes_Demo_Suite.class, //
			AtsTest_Config_Suite.class,//
			AtsTest_Demo_Suite.class, //
			FrameworkUi_Demo_Suite.class, //
})
/**
 * This suite should contain all cases and suites that can be run against a Demo Db Init and Demo Populated osee
 * database.
 *
 * @author Donald G. Dunne
 */
public class MasterTestSuite_DemoDbTests {
	@BeforeClass
	public static void setUp() throws Exception {
		assertTrue("Demo Application Server must be running.",
					ClientSessionManager.getAuthenticationProtocols().contains("demo"));
		assertTrue("Client must authenticate using demo protocol",
					ClientSessionManager.getSession().getAuthenticationProtocol().equals("demo"));
		assertTrue("Should be run on demo database.", TestUtil.isDemoDb());
		ArtifactSaveNotificationHandler.setNoPopUp(true);
		TestUtil.setIsInTest(true);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		TestUtil.setIsInTest(false);
	}
}