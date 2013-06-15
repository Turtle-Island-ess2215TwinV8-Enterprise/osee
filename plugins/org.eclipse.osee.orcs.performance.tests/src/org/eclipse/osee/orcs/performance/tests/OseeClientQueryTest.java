/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.performance.tests;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.rest.client.OseeClient;
import org.eclipse.osee.orcs.rest.client.OseeClientConfig;
import org.eclipse.osee.orcs.rest.client.OseeClientStandaloneSetup;
import org.eclipse.osee.orcs.rest.model.search.SearchResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@PerfTest(threads = 4, invocations = 10)
public class OseeClientQueryTest {

   @Rule
   public ContiPerfRule rule = ContiPerfRule.createDefaultRule();

   private OseeClient createClient;

   @Before
   public void testSetup() {
      OseeClientConfig config = new OseeClientConfig("http://localhost:8095");
      createClient = OseeClientStandaloneSetup.createClient(config);
   }

   @Test
   public void searchForFoldersOnCommon() throws OseeCoreException {
      int expectedResults = 38;

      SearchResult results =
         createClient.createQueryBuilder(CoreBranches.COMMON).andIsOfType(CoreArtifactTypes.Folder).getSearchResult();
      Assert.assertEquals(expectedResults, results.getTotal());
   }

   @Test
   public void searchForArtifactWithActionInName() throws OseeCoreException {
      int expectedResults = 1589;

      SearchResult results =
         createClient.createQueryBuilder(CoreBranches.COMMON).and(CoreAttributeTypes.Name, "action").getSearchResult();
      Assert.assertEquals(expectedResults, results.getTotal());
   }
}
