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
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.data.HasDeleteState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link DeletedMatcher}
 * 
 * @author Roberto E. Escobar
 */
public class DeletedMatcherTest {

   // @formatter:off
   @Mock private HasDeleteState deleted;
   @Mock private HasDeleteState notDeleted;
   // @formatter:on

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);

      when(deleted.isDeleted()).thenReturn(true);
      when(notDeleted.isDeleted()).thenReturn(false);
   }

   @Test
   public void testIncludeDeleted() throws OseeCoreException {
      Assert.assertTrue(filter(DeletionFlag.INCLUDE_DELETED).accept(deleted));
      Assert.assertTrue(filter(DeletionFlag.INCLUDE_DELETED).accept(notDeleted));
   }

   @Test
   public void testExcludeDeleted() throws OseeCoreException {
      Assert.assertFalse(filter(DeletionFlag.EXCLUDE_DELETED).accept(deleted));
      Assert.assertTrue(filter(DeletionFlag.EXCLUDE_DELETED).accept(notDeleted));
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private DataMatcher<HasDeleteState> filter(DeletionFlag flag) {
      return new DeletedMatcher(flag);
   }

}
