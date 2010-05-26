/*
 * Created on Apr 29, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.test.cases;

import static org.junit.Assert.assertFalse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import junit.framework.Assert;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.test.util.FrameworkTestUtil;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Donald G. Dunne
 */
public class ArtifactLoaderTest {

   private static SevereLoggingMonitor monitorLog;
   private static int numThreadsCompleted = 0;
   private static int NUM_ARTIFACTS = 100;
   private static String ATTRIBUTE_VALUE = "now is the time";

   @BeforeClass
   public static void testInitialize() throws Exception {
      assertFalse(TestUtil.isProductionDb());
      testCleanup();
      monitorLog = TestUtil.severeLoggingStart();
   }

   @AfterClass
   public static void tearDown() throws Exception {
      TestUtil.severeLoggingEnd(monitorLog);
      testCleanup();
   }

   private static void testCleanup() throws Exception {
      SkynetTransaction transaction = new SkynetTransaction(BranchManager.getCommonBranch(), "ArtifactLoaderTest");
      for (Artifact artifact : ArtifactQuery.getArtifactListFromName("ArtifactLoaderTest",
            BranchManager.getCommonBranch(), false)) {
         artifact.deleteAndPersist(transaction);
      }
      transaction.execute();
   }

   /**
    * This test was created because artifact loading is loading multiple attribute instances for the same attribute in
    * the database. This will create 100 artifacts with an attribute that should only be a singleton, decache the
    * artifacts so they are reloaded and then reload them in 8 different threads. Due to the intermittent nature, it may
    * need to be run multiple times.<br>
    * <br>
    * Another interesting side-effect, is this test also gives a repeatable case for duplicate relation loading. These
    * are caught by the SevereLoggingMonitor.
    */
   @org.junit.Test
   public void testThreadSafeLoading() throws Exception {
      // Create some software artifacts
      SkynetTransaction transaction = new SkynetTransaction(BranchManager.getCommonBranch(), "ArtifactLoaderTest");
      Collection<Artifact> artifacts =
            FrameworkTestUtil.createSimpleArtifacts(CoreArtifactTypes.GlobalPreferences, NUM_ARTIFACTS,
                  "ArtifactLoaderTest", BranchManager.getCommonBranch());
      for (Artifact artifact : artifacts) {
         artifact.setName("ArtifactLoaderTest");
         artifact.addAttribute(CoreAttributeTypes.DEFAULT_MAIL_SERVER, ATTRIBUTE_VALUE);
         artifact.persist(transaction);
      }
      transaction.execute();

      // now, de-cache them
      for (Artifact artifact : artifacts) {
         ArtifactCache.deCache(artifact);
      }

      // create some threads to load them all at same time
      List<Thread> threads = new ArrayList<Thread>();
      for (int x = 1; x < 8; x++) {
         threads.add(new LoadArtifacts("Thread " + x));
      }

      // run the threads
      numThreadsCompleted = 0;
      for (Thread thread : threads) {
         thread.start();
      }

      // stop after 10 seconds
      long endTime = new Date().getTime() + (10 * 1000);
      while (true) {
         Thread.sleep(1000);
         System.out.println("Checking for thread completion..." + numThreadsCompleted + "/7");
         if (numThreadsCompleted == 7) {
            break;
         }
         if (new Date().getTime() > endTime) {
            Assert.fail("Hit timeout value before threads were completed");
         }
      }

      // Load and check artifacts
      artifacts = ArtifactQuery.getArtifactListFromName("ArtifactLoaderTest", BranchManager.getCommonBranch(), false);
      Assert.assertEquals(NUM_ARTIFACTS, artifacts.size());

      for (Artifact artifact : artifacts) {
         Assert.assertEquals(ATTRIBUTE_VALUE, artifact.getSoleAttributeValue(CoreAttributeTypes.DEFAULT_MAIL_SERVER));
         Assert.assertEquals(1, artifact.getAttributesToStringList(CoreAttributeTypes.DEFAULT_MAIL_SERVER).size());
      }

      System.out.println("Completed");
   }
   public class LoadArtifacts extends Thread {

      public LoadArtifacts(String name) {
         super(name);
      }

      @Override
      public void run() {
         try {
            System.out.println("Running " + getName());
            List<Artifact> artifacts =
                  ArtifactQuery.getArtifactListFromName("ArtifactLoaderTest", BranchManager.getCommonBranch(), false);
            if (artifacts.size() != NUM_ARTIFACTS) {
               throw new OseeStateException("Should have loaded " + NUM_ARTIFACTS + "; only got " + artifacts.size());
            }
            numThreadsCompleted++;
            System.out.println("Completed " + getName() + "; NumThreads " + numThreadsCompleted);
         } catch (OseeCoreException ex) {
            OseeLog.log(ArtifactLoaderTest.class, Level.SEVERE, ex.toString(), ex);
         }
      }
   };

}