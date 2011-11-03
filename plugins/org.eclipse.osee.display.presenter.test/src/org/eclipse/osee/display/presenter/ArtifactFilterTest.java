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
package org.eclipse.osee.display.presenter;

import org.eclipse.osee.display.presenter.mocks.MockArtifact;
import org.eclipse.osee.display.presenter.mocks.MockArtifactProvider;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author John Misinco
 */
public class ArtifactFilterTest {

   @Test
   public void testAccept() throws Exception {
      MockArtifactProvider provider = new MockArtifactProvider();
      ArtifactFilter filter = new ArtifactFilter(provider);

      MockArtifact allowedByType =
         new MockArtifact("", "name", CoreArtifactTypes.SoftwareRequirement, CoreBranches.SYSTEM_ROOT);
      provider.addArtifact(allowedByType);

      Assert.assertTrue(filter.accept(allowedByType));

      MockArtifact allowedByBranch =
         new MockArtifact("", "name", CoreArtifactTypes.GlobalPreferences, CoreBranches.COMMON);
      provider.addArtifact(allowedByBranch);
      Assert.assertTrue(filter.accept(allowedByBranch));

      MockArtifact child =
         new MockArtifact("childGuid", "childName", CoreArtifactTypes.Component, CoreBranches.SYSTEM_ROOT);
      MockArtifact parent =
         new MockArtifact("parentGuid", "parentName", CoreArtifactTypes.Component, CoreBranches.SYSTEM_ROOT);
      MockArtifact allowedGrandParent =
         new MockArtifact("grandParentGuid", "Subsystem Requirements", CoreArtifactTypes.Component,
            CoreBranches.SYSTEM_ROOT);

      child.setParent(parent);
      parent.setParent(allowedGrandParent);

      provider.addArtifact(child);

      Assert.assertTrue(filter.accept(child));

      MockArtifact notAllowedGrandParent =
         new MockArtifact("grandParentGuid", "Unknown Name", CoreArtifactTypes.Component, CoreBranches.SYSTEM_ROOT);
      parent.setParent(notAllowedGrandParent);
      Assert.assertFalse(filter.accept(child));
   }
}
