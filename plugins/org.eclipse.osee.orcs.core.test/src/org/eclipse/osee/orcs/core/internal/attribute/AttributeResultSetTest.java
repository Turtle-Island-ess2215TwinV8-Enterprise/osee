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
package org.eclipse.osee.orcs.core.internal.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.ItemDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleItemsExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link AttributeResultSet }
 * 
 * @author Roberto E. Escobar
 */
public class AttributeResultSetTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   // @formatter:off
   @Mock private AttributeExceptionFactory factory;
   @Mock private Attribute<String> attribute1;
   @Mock private Attribute<String> attribute2;
   // @formatter:on

   private AttributeResultSet<String> rSetWithType;
   private AttributeResultSet<String> rSetNoType;

   private final IAttributeType type = CoreAttributeTypes.Annotation;
   private List<Attribute<String>> list;

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);

      list = new ArrayList<Attribute<String>>();
      rSetNoType = new AttributeResultSet<String>(null, list);
      rSetWithType = new AttributeResultSet<String>(factory, type, list);
   }

   @Test
   public void testGetList() {
      assertEquals(list, rSetNoType.getList());
   }

   @Test
   public void testIterator() {
      List<Attribute<String>> spy = spy(list);
      rSetNoType = new AttributeResultSet<String>(factory, spy);
      rSetNoType.iterator();

      verify(spy).iterator();
   }

   @Test
   public void testGetIterable() {
      assertEquals(list, rSetNoType.getIterable(-1));
   }

   @Test
   public void testExactlyOne() throws OseeCoreException {
      thrown.expect(ItemDoesNotExist.class);
      rSetNoType.getExactlyOne();

      rSetWithType.getExactlyOne();
      verify(factory).createDoesNotExistException(type);

      list.add(attribute1);
      assertEquals(attribute1, rSetNoType.getExactlyOne());

      list.add(attribute2);

      thrown.expect(MultipleItemsExist.class);
      rSetNoType.getExactlyOne();

      rSetWithType.getExactlyOne();
      verify(factory).createManyExistException(type, 2);
   }

   @Test
   public void testGetOneOrNull() {
      assertNull(rSetNoType.getOneOrNull());

      list.add(attribute1);
      assertEquals(attribute1, rSetNoType.getOneOrNull());

      list.add(attribute2);
      assertEquals(attribute1, rSetNoType.getOneOrNull());
   }

   @Test
   public void testGetAtMostOneOrNull() throws OseeCoreException {
      assertNull(rSetNoType.getAtMostOneOrNull());

      list.add(attribute1);
      assertEquals(attribute1, rSetNoType.getAtMostOneOrNull());

      list.add(attribute2);

      thrown.expect(MultipleItemsExist.class);
      rSetNoType.getAtMostOneOrNull();

      rSetWithType.getAtMostOneOrNull();
      verify(factory).createManyExistException(type, 2);
   }
}
