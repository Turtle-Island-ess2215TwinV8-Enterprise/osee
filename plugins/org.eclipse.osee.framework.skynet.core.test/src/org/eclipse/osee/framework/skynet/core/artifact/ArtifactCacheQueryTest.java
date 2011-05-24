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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Test unit for {@link ArtifactCacheQuery}
 * 
 * @author Donald G. Dunne
 */
public class ArtifactCacheQueryTest {

   private static final String STATIC_ID_AAA = "aaa";
   private static final String STATIC_ID_BBB = "bbb";
   private static final String STATIC_ID_CCC = "ccc";
   private static final String STATIC_ID_DDD = "ddd";
   private static final String STATIC_ID_EEE = "eee";

   private static final List<String> ALL_STATIC_IDS = Arrays.asList(STATIC_ID_AAA, STATIC_ID_BBB, STATIC_ID_CCC,
      STATIC_ID_DDD, STATIC_ID_EEE);

   @BeforeClass
   @AfterClass
   public static void testCleanupForReRun() throws OseeCoreException {
      SkynetTransaction transaction =
         new SkynetTransaction(BranchManager.getCommonBranch(), "Static ID Manager test cleanup for re-run");
      for (String staticIdValue : ALL_STATIC_IDS) {
         for (Artifact artifact : ArtifactQuery.getArtifactListFromAttribute(CoreAttributeTypes.StaticId,
            staticIdValue, BranchManager.getCommonBranch())) {
            artifact.deleteAndPersist(transaction);
            System.out.println("Deleting " + artifact.getGuid());
         }
      }
      transaction.execute();

      for (String staticIdValue : ALL_STATIC_IDS) {
         Collection<Artifact> artifacts =
            ArtifactQuery.getArtifactListFromTypeAndAttribute(CoreArtifactTypes.GeneralData,
               CoreAttributeTypes.StaticId, staticIdValue, BranchManager.getCommonBranch());
         for (Artifact artifact : artifacts) {
            System.err.println("Search returned non-deleted " + artifact.getGuid());
         }
         assertTrue("Expected 0 artifacts; Returned " + artifacts.size(), artifacts.isEmpty());
      }
   }

   @Before
   public void setup() throws Exception {
      assertTrue("Should be run on demo datbase.", TestUtil.isDemoDb());
   }

   /**
    * Test method for (@link ArtifactCacheQuery.getOrCreateSingletonArtifactByText, ArtifactCache.getListByTextId}
    */
   @org.junit.Test
   public void testStaticIdsGettingCached() throws OseeCoreException {
      String staticId = "org." + GUID.create();
      Artifact artifact =
         ArtifactCacheQuery.getOrCreateSingletonArtifactByText(CoreArtifactTypes.GeneralData,
            CoreAttributeTypes.StaticId, staticId, BranchManager.getCommonBranch());
      assertNotNull(artifact);
      artifact.addAttribute(CoreAttributeTypes.StaticId, staticId);
      artifact.persist();

      Collection<Artifact> artifacts = ArtifactCache.getListByTextId(staticId, BranchManager.getCommonBranch());
      assertTrue("Should be 1; Returned " + artifacts.size(), artifacts.size() == 1);

      artifact.deleteAndPersist();
      artifacts = ArtifactCache.getListByTextId(staticId, BranchManager.getCommonBranch());
      assertTrue("Should be 0; Returned " + artifacts.size(), artifacts.isEmpty());
   }

   /**
    * Test method for {@link ArtifactCacheQuery.getSingletonArtifactByText,
    * ArtifactCacheQuery.getOrCreateSingletonArtifactByText}
    */
   @org.junit.Test
   public void testGetSingletonArtifact() throws OseeCoreException {
      Artifact artifact =
         ArtifactCacheQuery.getSingletonArtifactByText(CoreArtifactTypes.GeneralData, CoreAttributeTypes.StaticId,
            STATIC_ID_AAA, BranchManager.getCommonBranch(), true);
      assertNull(artifact);

      artifact =
         ArtifactCacheQuery.getOrCreateSingletonArtifactByText(CoreArtifactTypes.GeneralData,
            CoreAttributeTypes.StaticId, STATIC_ID_AAA, BranchManager.getCommonBranch());
      assertNotNull(artifact);

      deleteArtifacts(Arrays.asList(artifact), STATIC_ID_AAA);
   }

   /**
    * Test unif or {@link ArtifactCacheQuery.getSingletonArtifactByText, Artifact.setSingletonAttributeValue}
    */
   @org.junit.Test
   public void testSetSingletonAttributeValue() throws OseeCoreException {
      // create artifact with two of same static id values
      Artifact artifact =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.GeneralData, BranchManager.getCommonBranch());
      artifact.persist();
      artifact.addAttribute(CoreAttributeTypes.StaticId, STATIC_ID_BBB);
      artifact.addAttribute(CoreAttributeTypes.StaticId, STATIC_ID_BBB);
      ArtifactCache.cacheByTextId(STATIC_ID_BBB, artifact);
      ArtifactCache.cacheByTextId(STATIC_ID_BBB, artifact);

      // call to search for artifact with STATIC_ID_BBB
      Artifact artifactWithDoubleBbb =
         ArtifactCacheQuery.getSingletonArtifactByText(CoreArtifactTypes.GeneralData, CoreAttributeTypes.StaticId,
            STATIC_ID_BBB, BranchManager.getCommonBranch(), false);
      assertNotNull(artifactWithDoubleBbb);

      // should be two static id attributes
      int count = artifactWithDoubleBbb.getAttributes(CoreAttributeTypes.StaticId).size();
      assertTrue("Expected 2 attributes; Returned " + count, count == 2);

      count = artifactWithDoubleBbb.getAttributeCount(CoreAttributeTypes.StaticId);
      assertTrue("Expected 2 attributes; Returned " + count, count == 2);

      // call to set singleton which should resolve duplicates
      artifactWithDoubleBbb.setSingletonAttributeValue(CoreAttributeTypes.StaticId, STATIC_ID_BBB);

      // should now be only one static id attributes
      count = artifactWithDoubleBbb.getAttributeCount(CoreAttributeTypes.StaticId);
      assertTrue("Expected 1 attributes; Returned " + count, count == 1);

      deleteArtifacts(Arrays.asList(artifact), STATIC_ID_BBB);
   }

   /**
    * Test method for {@link ArtifactCacheQuery#getSingletonArtifactCacheByTextOrException} .
    */
   @org.junit.Test
   public void testGetSingletonArtifactOrException() throws OseeCoreException {
      try {
         ArtifactCacheQuery.getSingletonArtifactByTextOrException(CoreArtifactTypes.GeneralData,
            CoreAttributeTypes.StaticId, STATIC_ID_DDD, BranchManager.getCommonBranch());
         fail("ArtifactDoesNotExist should have been thrown.");
      } catch (Exception ex) {
         assertTrue("Was not ArtifactDoesNotExist was: " + ex.getClass().getSimpleName(),
            ex instanceof ArtifactDoesNotExist);
      }

      Collection<Artifact> artifacts = new ArrayList<Artifact>();
      SkynetTransaction transaction =
         new SkynetTransaction(BranchManager.getCommonBranch(), "testGetSingletonArtifactOrException");
      for (int index = 0; index < 2; index++) {
         Artifact artifact =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.GeneralData, BranchManager.getCommonBranch());
         artifact.setSingletonAttributeValue(CoreAttributeTypes.StaticId, STATIC_ID_DDD);
         ArtifactCache.cacheByTextId(STATIC_ID_DDD, artifact);
         artifact.persist(transaction);
         assertNotNull(artifact);
         artifacts.add(artifact);
      }
      transaction.execute();

      try {
         ArtifactCacheQuery.getSingletonArtifactByTextOrException(CoreArtifactTypes.GeneralData,
            CoreAttributeTypes.StaticId, STATIC_ID_DDD, BranchManager.getCommonBranch());
         fail("MultipleArtifactsExist should have been thrown");
      } catch (Exception ex) {
         assertTrue("Was not MultipleArtifactsExist was: " + ex.getClass().getSimpleName(),
            ex instanceof MultipleArtifactsExist);
      }

      deleteArtifacts(artifacts, STATIC_ID_DDD);
   }

   /**
    * Test unit for {@link ArtifactCache.getListByTextId} that deleted artifacts not returned
    */
   private void deleteArtifacts(Collection<Artifact> toDelete, String staticId) throws OseeCoreException {
      if (!toDelete.isEmpty()) {
         if (toDelete.size() == 1) {
            toDelete.iterator().next().deleteAndPersist();
         } else {
            SkynetTransaction transaction =
               new SkynetTransaction(BranchManager.getCommonBranch(), "Delete collection of artifacts");
            for (Artifact artifact : toDelete) {
               artifact.delete();
            }
            for (Artifact artifact : toDelete) {
               artifact.persist(transaction);
            }
            transaction.execute();
         }

         Collection<Artifact> artifacts = ArtifactCache.getListByTextId(staticId, BranchManager.getCommonBranch());
         assertTrue("Should be 0; Returned " + artifacts.size(), artifacts.isEmpty());
      }
   }

   /**
    * Test method for {@link ArtifactCacheQuery.getOrCreateSingletonArtifactByText,
    * ArtifactCacheQuery.getSingletonArtifactByText} .
    */
   @org.junit.Test
   public void testGetSingletonArtifactStringStringBranch() throws OseeCoreException {
      List<Artifact> itemsCreated = new ArrayList<Artifact>();

      // create single artifact with eee staticId
      SkynetTransaction transaction =
         new SkynetTransaction(BranchManager.getCommonBranch(), "create single artifact with eee staticId");
      Artifact artifact =
         ArtifactCacheQuery.getOrCreateSingletonArtifactByText(CoreArtifactTypes.GeneralData,
            CoreAttributeTypes.StaticId, STATIC_ID_EEE, BranchManager.getCommonBranch());
      artifact.persist(transaction);
      assertNotNull(artifact);
      transaction.execute();

      itemsCreated.add(artifact);

      // test that singleton comes back
      artifact =
         ArtifactCacheQuery.getSingletonArtifactByText(CoreArtifactTypes.GeneralData, CoreAttributeTypes.StaticId,
            STATIC_ID_EEE, BranchManager.getCommonBranch(), false);
      assertNotNull(artifact);

      // create another artifact with eee staticId
      transaction = new SkynetTransaction(BranchManager.getCommonBranch(), "create another artifact with eee staticId");
      artifact = ArtifactTypeManager.addArtifact(CoreArtifactTypes.GeneralData, BranchManager.getCommonBranch());
      artifact.setSingletonAttributeValue(CoreAttributeTypes.StaticId, STATIC_ID_EEE);
      artifact.persist(transaction);
      assertNotNull(artifact);
      transaction.execute();
      itemsCreated.add(artifact);

      // test that there are now two artifacts with eee
      Collection<Artifact> artifacts =
         ArtifactQuery.getArtifactListFromTypeAndAttribute(CoreArtifactTypes.GeneralData, CoreAttributeTypes.StaticId,
            STATIC_ID_EEE, BranchManager.getCommonBranch());
      assertTrue("Expected 2 artifacts; Returned " + artifacts.size(), artifacts.size() == 2);

      // test that call to get singleton does NOT exception
      try {
         artifact =
            ArtifactCacheQuery.getSingletonArtifactByText(CoreArtifactTypes.GeneralData, CoreAttributeTypes.StaticId,
               STATIC_ID_EEE, BranchManager.getCommonBranch(), false);
         assertNotNull(artifact);
      } catch (Exception ex) {
         fail("Exception should not have occurred " + ex.getLocalizedMessage());
      }
      deleteArtifacts(itemsCreated, STATIC_ID_EEE);
   }
}