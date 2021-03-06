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
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.config.store;

import java.util.Arrays;
import java.util.Date;
import junit.framework.Assert;
import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.config.AtsConfigCache;
import org.eclipse.osee.ats.core.config.IAtsConfig;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class VersionArtifactStoreTest {

   @BeforeClass
   @AfterClass
   public static void cleanup() throws OseeCoreException {
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtilCore.getAtsBranchToken(),
            VersionArtifactStoreTest.class.getSimpleName() + " - cleanup");

      IAtsConfig config = AtsClientService.get().getAtsConfig();
      for (String name : Arrays.asList("VersionArtifactStoreTest - version 1", "VersionArtifactStoreTest - version 2",
         "VersionArtifactStoreTest - version 3")) {
         for (Artifact art : ArtifactQuery.getArtifactListFromTypeAndName(AtsArtifactTypes.Version, name,
            AtsUtilCore.getAtsBranchToken())) {
            art.deleteAndPersist(transaction);

            IAtsConfigObject soleByGuid = config.getSoleByGuid(art.getGuid());
            config.invalidate(soleByGuid);
         }
      }
      transaction.execute();
   }

   @Test
   public void testLoadFromArtifact() throws OseeCoreException {
      IAtsConfig cache = new AtsConfigCache();

      Artifact verArt = ArtifactTypeManager.addArtifact(AtsArtifactTypes.Version, AtsUtilCore.getAtsBranchToken());
      verArt.setName("VersionArtifactStoreTest - version 1");
      verArt.persist(getClass().getSimpleName());

      IAtsVersion version = AtsClientService.get().getConfigObject(verArt);
      Assert.assertEquals("VersionArtifactStoreTest - version 1", version.getName());
      Assert.assertFalse(version.isAllowCommitBranch());
      Assert.assertFalse(version.isAllowCreateBranch());
      Assert.assertFalse(version.isLocked());
      Assert.assertFalse(version.isReleased());
      Assert.assertFalse(version.isNextVersion());
      Assert.assertEquals("", version.getBaslineBranchGuid());
      Assert.assertEquals("", version.getDescription());
      Assert.assertEquals("", version.getFullName());
      Assert.assertNull(version.getReleaseDate());

      verArt.setSoleAttributeValue(AtsAttributeTypes.AllowCommitBranch, true);
      verArt.setSoleAttributeValue(AtsAttributeTypes.AllowCreateBranch, true);
      verArt.setSoleAttributeValue(AtsAttributeTypes.NextVersion, true);
      verArt.setSoleAttributeValue(AtsAttributeTypes.Released, true);
      verArt.setSoleAttributeValue(AtsAttributeTypes.VersionLocked, true);
      verArt.setSoleAttributeValue(AtsAttributeTypes.BaselineBranchGuid, "guid1234");
      verArt.setSoleAttributeValue(AtsAttributeTypes.Description, "description");
      verArt.setSoleAttributeValue(AtsAttributeTypes.FullName, "this is full name");
      Date releaseDate = new Date();
      verArt.setSoleAttributeValue(AtsAttributeTypes.ReleaseDate, releaseDate);
      verArt.persist(getClass().getSimpleName());

      version = AtsClientService.get().getConfigObject(verArt);
      Assert.assertEquals("VersionArtifactStoreTest - version 1", version.getName());
      Assert.assertTrue(version.isAllowCommitBranch());
      Assert.assertTrue(version.isAllowCreateBranch());
      Assert.assertTrue(version.isLocked());
      Assert.assertTrue(version.isReleased());
      Assert.assertTrue(version.isNextVersion());
      Assert.assertEquals("guid1234", version.getBaslineBranchGuid());
      Assert.assertEquals("description", version.getDescription());
      Assert.assertEquals("this is full name", version.getFullName());
      Assert.assertEquals(releaseDate, version.getReleaseDate());
   }

   @Test
   public void testSaveToArtifact() throws OseeCoreException {
      IAtsConfig cache = new AtsConfigCache();

      Artifact verArt = ArtifactTypeManager.addArtifact(AtsArtifactTypes.Version, AtsUtilCore.getAtsBranchToken());
      verArt.setName("VersionArtifactStoreTest - version 2");

      Artifact teamDef =
         ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.TeamDefinition, "SAW SW",
            AtsUtilCore.getAtsBranchToken());
      verArt.addRelation(AtsRelationTypes.TeamDefinitionToVersion_TeamDefinition, teamDef);
      verArt.persist(getClass().getSimpleName());

      IAtsVersion version = AtsClientService.get().getConfigObject(verArt);

      version.setAllowCommitBranch(true);
      version.setAllowCreateBranch(true);
      version.setName("VersionArtifactStoreTest - version 3");
      version.setNextVersion(true);
      version.setLocked(true);
      Date releaseDate = new Date();
      version.setReleaseDate(releaseDate);
      version.setVersionLocked(true);
      version.setBaselineBranchGuid("guid3456");
      version.setDescription("description");
      version.setFullName("full name");

      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtilCore.getAtsBranchToken(),
            VersionArtifactStoreTest.class.getSimpleName() + " - testSaveToArtifact");
      AtsClientService.get().storeConfigObject(version, transaction);
      transaction.execute();

      Artifact saveArt = AtsClientService.get().getConfigArtifact(version);
      Assert.assertEquals("VersionArtifactStoreTest - version 3", version.getName());
      Assert.assertTrue(saveArt.getSoleAttributeValue(AtsAttributeTypes.AllowCommitBranch, true));
      Assert.assertTrue(saveArt.getSoleAttributeValue(AtsAttributeTypes.AllowCreateBranch, true));
      Assert.assertTrue(saveArt.getSoleAttributeValue(AtsAttributeTypes.VersionLocked, true));
      Assert.assertTrue(saveArt.getSoleAttributeValue(AtsAttributeTypes.Released, true));
      Assert.assertTrue(saveArt.getSoleAttributeValue(AtsAttributeTypes.NextVersion, true));
      Assert.assertEquals("guid3456", saveArt.getSoleAttributeValue(AtsAttributeTypes.BaselineBranchGuid, ""));
      Assert.assertEquals("description", saveArt.getSoleAttributeValue(AtsAttributeTypes.Description, ""));
      Assert.assertEquals("full name", saveArt.getSoleAttributeValue(AtsAttributeTypes.FullName, ""));
      Assert.assertEquals(releaseDate, saveArt.getSoleAttributeValue(AtsAttributeTypes.ReleaseDate, null));
   }
}
