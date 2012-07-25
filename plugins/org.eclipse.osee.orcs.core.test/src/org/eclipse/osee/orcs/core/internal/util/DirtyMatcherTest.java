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
package org.eclipse.osee.orcs.core.internal.util;

import static org.mockito.Mockito.when;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.core.internal.util.DirtyMatcher.DirtyFlag;
import org.eclipse.osee.orcs.data.Modifiable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link DirtyMatcher}
 * 
 * @author Roberto E. Escobar
 */
public class DirtyMatcherTest {

   // @formatter:off
   @Mock private Modifiable dirty;
   @Mock private Modifiable notDirty;
   // @formatter:on

   @Before
   public void init() throws OseeCoreException {
      MockitoAnnotations.initMocks(this);

      when(dirty.isDirty()).thenReturn(true);
      when(notDirty.isDirty()).thenReturn(false);
   }

   @Test
   public void testAcceptDirties() throws OseeCoreException {
      Assert.assertFalse(filter(DirtyFlag.DIRTY).accept(notDirty));
      Assert.assertTrue(filter(DirtyFlag.DIRTY).accept(dirty));
   }

   @Test
   public void testAcceptNoneDirties() throws OseeCoreException {
      Assert.assertTrue(filter(DirtyFlag.NON_DIRTY).accept(notDirty));
      Assert.assertFalse(filter(DirtyFlag.NON_DIRTY).accept(dirty));
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   private DataMatcher<Modifiable> filter(DirtyFlag flag) {
      return new DirtyMatcher(flag);
   }

}
