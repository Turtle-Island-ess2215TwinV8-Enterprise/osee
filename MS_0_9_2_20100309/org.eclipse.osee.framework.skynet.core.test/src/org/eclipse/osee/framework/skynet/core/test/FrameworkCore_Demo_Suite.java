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
package org.eclipse.osee.framework.skynet.core.test;

import static org.junit.Assert.assertTrue;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.skynet.core.test.branch.BranchManagerTest;
import org.eclipse.osee.framework.skynet.core.test.branch.BranchTestSuite;
import org.eclipse.osee.framework.skynet.core.test.cases.ArtifactPurgeTest;
import org.eclipse.osee.framework.skynet.core.test.cases.ArtifactQueryTestDemo;
import org.eclipse.osee.framework.skynet.core.test.cases.ArtifactTypeInheritanceTest;
import org.eclipse.osee.framework.skynet.core.test.cases.Artifact_setAttributeValues;
import org.eclipse.osee.framework.skynet.core.test.cases.BranchPurgeTest;
import org.eclipse.osee.framework.skynet.core.test.cases.BranchStateTest;
import org.eclipse.osee.framework.skynet.core.test.cases.ChangeManagerTest;
import org.eclipse.osee.framework.skynet.core.test.cases.ConflictTest;
import org.eclipse.osee.framework.skynet.core.test.cases.CsvArtifactTest;
import org.eclipse.osee.framework.skynet.core.test.cases.DuplicateHridTest;
import org.eclipse.osee.framework.skynet.core.test.cases.NativeArtifactTest;
import org.eclipse.osee.framework.skynet.core.test.cases.RelationDeletionTest;
import org.eclipse.osee.framework.skynet.core.test.cases.RelationOrderingTest;
import org.eclipse.osee.framework.skynet.core.test.cases.SevereLogMonitorTest;
import org.eclipse.osee.framework.skynet.core.test.cases.StaticIdManagerTest;
import org.eclipse.osee.framework.skynet.core.test.cases.TransactionManagerTest;
import org.eclipse.osee.framework.skynet.core.test.cases.PurgeTransactionTest;
import org.eclipse.osee.framework.skynet.core.test.importing.ReqNumberingTest;
import org.eclipse.osee.framework.skynet.core.test.relation.RelationTestSuite;
import org.eclipse.osee.framework.skynet.core.test.types.OseeCacheTestSuite;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {ArtifactQueryTestDemo.class, TransactionManagerTest.class, BranchTestSuite.class,
      BranchManagerTest.class, RelationTestSuite.class, OseeCacheTestSuite.class, ArtifactTypeInheritanceTest.class,
      ArtifactPurgeTest.class, BranchPurgeTest.class, PurgeTransactionTest.class, Artifact_setAttributeValues.class,
      CsvArtifactTest.class, NativeArtifactTest.class, ConflictTest.class, ChangeManagerTest.class,
      SevereLogMonitorTest.class, RelationDeletionTest.class, StaticIdManagerTest.class, BranchStateTest.class,
      DuplicateHridTest.class, RelationOrderingTest.class, ReqNumberingTest.class})
/**
 * @author Donald G. Dunne
 */
public class FrameworkCore_Demo_Suite {
   @BeforeClass
   public static void setUp() throws Exception {
      assertTrue("Demo Application Server must be running.",
            ClientSessionManager.getAuthenticationProtocols().contains("demo"));
      assertTrue("Client must authenticate using demo protocol",
            ClientSessionManager.getSession().getAuthenticationProtocol().equals("demo"));
   }

}
