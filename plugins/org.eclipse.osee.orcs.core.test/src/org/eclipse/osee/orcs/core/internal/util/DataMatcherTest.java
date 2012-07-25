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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link DataMatcher}
 * 
 * @author Roberto E. Escobar
 */
public class DataMatcherTest {

   // @formatter:off
   @Mock private Object data;
   // @formatter:on

   private DataMatcher<Object> filter1;
   private DataMatcher<Object> filter2;

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);
      filter1 = Mockito.spy(new FilterMock());
      filter2 = Mockito.spy(new FilterMock());
   }

   @Test
   public void testAnd() throws OseeCoreException {
      DataMatcher<Object> andFilter = filter1.and(filter2);

      when(filter1.accept(data)).thenReturn(true);
      when(filter2.accept(data)).thenReturn(true);
      Assert.assertTrue(andFilter.accept(data));

      when(filter1.accept(data)).thenReturn(false);
      when(filter2.accept(data)).thenReturn(true);
      Assert.assertFalse(andFilter.accept(data));

      when(filter1.accept(data)).thenReturn(true);
      when(filter2.accept(data)).thenReturn(false);
      Assert.assertFalse(andFilter.accept(data));

      when(filter1.accept(data)).thenReturn(false);
      when(filter2.accept(data)).thenReturn(false);
      Assert.assertFalse(andFilter.accept(data));
   }

   @Test
   public void testOr() throws OseeCoreException {
      DataMatcher<Object> orFilter = filter1.or(filter2);

      when(filter1.accept(data)).thenReturn(true);
      when(filter2.accept(data)).thenReturn(true);
      Assert.assertTrue(orFilter.accept(data));

      when(filter1.accept(data)).thenReturn(false);
      when(filter2.accept(data)).thenReturn(true);
      Assert.assertTrue(orFilter.accept(data));

      when(filter1.accept(data)).thenReturn(true);
      when(filter2.accept(data)).thenReturn(false);
      Assert.assertTrue(orFilter.accept(data));

      when(filter1.accept(data)).thenReturn(false);
      when(filter2.accept(data)).thenReturn(false);
      Assert.assertFalse(orFilter.accept(data));
   }

   public class FilterMock extends DataMatcher<Object> {
      @Override
      public boolean accept(Object attribute) {
         return false;
      }
   };

}
