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
package org.eclipse.osee.orcs.api;

import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.orcs.ApplicationContext;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.OrcsBranch;
import org.eclipse.osee.orcs.OrcsIntegrationRule;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.ArtifactWriteable;
import org.eclipse.osee.orcs.db.mock.OseeDatabase;
import org.eclipse.osee.orcs.db.mock.OsgiService;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.orcs.transaction.OrcsTransaction;
import org.eclipse.osee.orcs.transaction.TransactionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author David W. Miller
 */
public class OrcsPortingTest {

   @Rule
   public OrcsIntegrationRule osgi = new OrcsIntegrationRule(this);

   @Rule
   public OseeDatabase db = new OseeDatabase("osee.demo.h2");

   @OsgiService
   private OrcsApi orcsApi;

   private OrcsBranch branchApi;
   private QueryFactory query;
   private TransactionFactory txFactory;

   private ArtifactReadable author;

   @Before
   public void setUp() throws Exception {
      ApplicationContext context = null; // TODO use real application context

      branchApi = orcsApi.getBranchOps(context);
      query = orcsApi.getQueryFactory(context);
      txFactory = orcsApi.getTransactionFactory(context);

      author = query.fromBranch(CoreBranches.COMMON).andIds(SystemUser.OseeSystem).getResults().getExactlyOne();
   }

   @Test
   public void testCreateBranch() throws Exception {

      String artifactGuid = GUID.create();

      IOseeBranch mainBranch = createBaselineBranchAndArtifacts(artifactGuid);
      TransactionRecord transactionToCopy = createWorkingBranchChanges(mainBranch, artifactGuid);

      IOseeBranch copyTxBranch = createCopyFromTransactionBranch(transactionToCopy);
      TransactionRecord finalTx = commitToDestinationBranch(copyTxBranch);

      // now check to make sure everything is as expected
      // we should have a SoftwareRequirement named "SecondRequirement" with an attribute named "test changed" (changed on child branch to this)
      // the attribute for the SecondRequirement should not be named "test changed again" (on the branch after the copy from)
      // we should have a folder named "childBranch folder", but no folder named "folder after transaction"
      ResultSet<ArtifactReadable> artifacts =
         query.fromBranch(finalTx.getBranch()).andIsOfType(CoreArtifactTypes.Artifact).getResults();
      for (ArtifactReadable art : artifacts) {
         if (art.isOfType(CoreArtifactTypes.SoftwareRequirement)) {
            Assert.assertEquals(2, art.getAttributes().size());

            Assert.assertEquals(artifactGuid, art.getGuid());
            Assert.assertEquals("SecondRequirement", art.getName());

            String actual = art.getSoleAttributeAsString(CoreAttributeTypes.Subsystem);
            Assert.assertEquals("test changed", actual);
            // if there is a requirement with an attribute other than "test changed" then the above should fail
         } else if (art.isOfType(CoreArtifactTypes.Folder)) {
            Assert.assertEquals("childBranch folder", art.getName());
            // if there is any other folder like "folder after transaction" then the above should fail
         } else {
            Assert.assertTrue(false);
         }
      }

   }

   private IOseeBranch createBaselineBranchAndArtifacts(String artifactGuid) throws Exception {
      // set up the main branch
      IOseeBranch branch = TokenFactory.createBranch(GUID.create(), "MainFromBranch");
      branchApi.createTopLevelBranch(branch, author).call();

      // baseline branch - set up artifacts on the main branch, and on the child branch
      // first, add some transaction on the main branch, then create the child branch
      OrcsTransaction tx = txFactory.createTransaction(branch, author, "add base requirement");
      ArtifactWriteable req1 = tx.createArtifact(CoreArtifactTypes.SoftwareRequirement, "BaseRequirement");
      req1.setSoleAttributeFromString(CoreAttributeTypes.Subsystem, "Test");

      tx.commit();

      OrcsTransaction tx2 = txFactory.createTransaction(branch, author, "add another requirement");
      ArtifactWriteable req2 =
         tx2.createArtifact(CoreArtifactTypes.SoftwareRequirement, "SecondRequirement", artifactGuid);
      req2.setSoleAttributeFromString(CoreAttributeTypes.Subsystem, "Test2");

      tx2.commit();
      return branch;
   }

   private TransactionRecord createWorkingBranchChanges(IOseeBranch parentBranch, String artifactToModifyGuid) throws Exception {
      // set up the child branch to copy to

      IOseeBranch childBranch = TokenFactory.createBranch(GUID.create(), "childBranch");
      branchApi.createWorkingBranch(childBranch, author, parentBranch, null).call();

      OrcsTransaction tx3 = txFactory.createTransaction(childBranch, author, "update second requirement");
      ArtifactReadable readableReq2 =
         query.fromBranch(childBranch).andGuidsOrHrids(artifactToModifyGuid).getResults().getExactlyOne();

      // modifying this artifact should cause it to get introduced
      ArtifactWriteable writeableReq2 = tx3.asWriteable(readableReq2);
      writeableReq2.setSoleAttributeFromString(CoreAttributeTypes.Subsystem, "test changed");

      // new artifacts should come across as new
      tx3.createArtifact(CoreArtifactTypes.Folder, "childBranch folder");

      // set this aside to use in the copy from transaction for the branch
      TransactionRecord transactionToCopy = tx3.commit();

      // make an additional transaction to make sure it doesn't get copied also
      OrcsTransaction tx4 = txFactory.createTransaction(childBranch, author, "after second requirement");
      ArtifactReadable readableReq2verA =
         query.fromBranch(childBranch).andGuidsOrHrids(artifactToModifyGuid).getResults().getExactlyOne();

      // modifying this artifact should cause it to get introduced
      ArtifactWriteable writeableReq2verA = tx4.asWriteable(readableReq2verA);
      writeableReq2verA.setSoleAttributeFromString(CoreAttributeTypes.Subsystem, "test changed again");

      // additional artifacts should not come across
      tx4.createArtifact(CoreArtifactTypes.Folder, "folder after transaction");
      tx4.commit();

      return transactionToCopy;
   }

   private IOseeBranch createCopyFromTransactionBranch(TransactionRecord transactionToCopy) throws Exception {
      // create the branch with the copied transaction
      IOseeBranch branch = TokenFactory.createBranch(GUID.create(), "CopiedTxBranch");
      // the new branch will contain two transactions -
      return branchApi.createCopyTxBranch(branch, author, transactionToCopy.getId(), null).call();
   }

   private TransactionRecord commitToDestinationBranch(IOseeBranch copyTxBranch) throws Exception {
      IOseeBranch destinationBranch = TokenFactory.createBranch(GUID.create(), "IndepToBranch");
      branchApi.createTopLevelBranch(destinationBranch, author).call();
      return branchApi.commitBranch(author, copyTxBranch, destinationBranch).call();
   }
}
