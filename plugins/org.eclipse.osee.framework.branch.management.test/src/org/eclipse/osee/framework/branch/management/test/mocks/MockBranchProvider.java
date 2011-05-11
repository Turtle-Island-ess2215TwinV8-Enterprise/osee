/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.branch.management.test.mocks;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.osee.framework.branch.management.purge.IBranchesProvider;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.util.GUID;

/**
 * @author John Misinco
 */
public final class MockBranchProvider implements IBranchesProvider {

   private Branch root;
   private final Collection<Branch> branches = new ArrayList<Branch>();

   public MockBranchProvider() throws OseeCoreException {
      initializeData();
   }

   private void initializeData() throws OseeCoreException {
      //create a root branch
      root = new Branch(GUID.create(), "root", BranchType.SYSTEM_ROOT, BranchState.COMMITTED, false);

      //add a child to root (parent)
      Branch parent = new Branch(GUID.create(), "parent", BranchType.SYSTEM_ROOT, BranchState.CREATED, false);
      parent.setParentBranch(root);

      //add children branches to parent
      Branch child1 = new Branch(GUID.create(), "child1", BranchType.SYSTEM_ROOT, BranchState.MODIFIED, false);
      child1.setParentBranch(parent);
      child1.setArchived(true);
      child1.setBranchState(BranchState.DELETED);

      //this branch should get deleted
      Branch child2 = new Branch(GUID.create(), "child2", BranchType.WORKING, BranchState.DELETED, true);
      child2.setParentBranch(parent);

      Branch child3 = new Branch(GUID.create(), "child3", BranchType.SYSTEM_ROOT, BranchState.CREATED, false);
      //make one a merge branch
      child3.setBranchType(BranchType.MERGE);
      child3.setParentBranch(parent);

      Branch grandChild1 = new Branch(GUID.create(), "grandChild1", BranchType.MERGE, BranchState.UNKNOWN, false);
      grandChild1.setBranchState(BranchState.DELETED);
      grandChild1.setArchived(true);
      grandChild1.setParentBranch(child1);

      Branch grandChild2 =
         new Branch(GUID.create(), "grandChild2", BranchType.SYSTEM_ROOT, BranchState.MODIFIED, false);
      grandChild2.setParentBranch(child1);

      //this branch should get deleted
      Branch grandChild3 = new Branch(GUID.create(), "grandChild3", BranchType.WORKING, BranchState.DELETED, true);
      grandChild3.setParentBranch(child2);

      //add branches in a random order
      branches.add(child2);
      branches.add(grandChild2);
      branches.add(grandChild3);
      branches.add(child1);
      branches.add(root);
      branches.add(child3);
      branches.add(grandChild1);
      branches.add(parent);
   }

   public Branch getRootBranch() {
      return root;
   }

   @Override
   public Collection<Branch> getBranches() {
      return branches;
   }
}