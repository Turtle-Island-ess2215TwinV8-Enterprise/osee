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
package org.eclipse.osee.framework.skynet.core.artifact;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.util.FrameworkTestUtil;
import org.eclipse.osee.framework.skynet.core.utility.DbUtil;
import org.eclipse.osee.support.test.util.DemoSawBuilds;
import org.eclipse.osee.support.test.util.TestUtil;

/**
 * This test is intended to be run against a demo database. It tests the purge logic by counting the rows of the version
 * and txs tables, creating artifacts, changing them and then purging them. If it works properly, all rows should be
 * equal.
 * 
 * @author Donald G. Dunne
 */
public class ArtifactPurgeTest extends AbstractPurgeTest {
   private static final List<String> tables = Arrays.asList("osee_attribute", "osee_artifact", "osee_relation_link",
      "osee_tx_details", "osee_txs");

   @Override
   public void runPurgeOperation() throws OseeCoreException {
      // Count rows in tables prior to purge
      getPostTableCount();

      // Create some software artifacts
      Branch branch = BranchManager.getBranch(DemoSawBuilds.SAW_Bld_2.getName());
      SkynetTransaction transaction = new SkynetTransaction(branch, "Test purge artifacts");
      Collection<Artifact> softArts =
         FrameworkTestUtil.createSimpleArtifacts(CoreArtifactTypes.SoftwareRequirement, 10, getClass().getSimpleName(),
            branch);
      for (Artifact softArt : softArts) {
         softArt.persist(transaction);
      }
      transaction.execute();

      // make more changes to artifacts
      for (Artifact softArt : softArts) {
         softArt.addAttribute(CoreAttributeTypes.StaticId, getClass().getSimpleName());
         softArt.persist();
      }

      // Count rows and check that increased
      DbUtil.getTableRowCounts(postCreateArtifactsCount, getTables());
      TestUtil.checkThatIncreased(preCreateArtifactsCount, postCreateArtifactsCount);

      new PurgeArtifacts(softArts).execute();

      // Count rows and check that same as when began
      getPostTableCount();
      // TODO Looks like attributes created after initial artifact creation are not getting purged.  Needs Fix.
      TestUtil.checkThatEqual(preCreateArtifactsCount, postPurgeCount);

   }

   @Override
   public List<String> getTables() {
      return tables;
   }

}