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
package org.eclipse.osee.orcs.core.internal.relation.sorter;

import static org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes.LEXICOGRAPHICAL_ASC;
import static org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC;
import static org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes.UNORDERED;
import static org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes.USER_DEFINED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.util.List;
import org.eclipse.osee.framework.core.data.IRelationSorterId;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link SorterProvider}
 * 
 * @author Roberto E. Escobar
 */
public class SorterProviderTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   // @formatter:off
   @Mock private RelationTypeCache relationTypeCache;
   @Mock private RelationType type1;
   @Mock private RelationType type2;
   // @formatter:on

   private SorterProvider provider;

   @Before
   public void setUp() {
      MockitoAnnotations.initMocks(this);

      provider = new SorterProvider(relationTypeCache);
   }

   @Test
   public void testGetDefaultSorterId() throws OseeCoreException {
      when(relationTypeCache.getByGuid(CoreRelationTypes.Default_Hierarchical__Child.getGuid())).thenReturn(type1);
      when(type1.getDefaultOrderTypeGuid()).thenReturn(RelationOrderBaseTypes.USER_DEFINED.getGuid());

      IRelationSorterId actual1 = provider.getDefaultSorterId(CoreRelationTypes.Default_Hierarchical__Child);
      assertEquals(RelationOrderBaseTypes.USER_DEFINED, actual1);

      when(relationTypeCache.getByGuid(CoreRelationTypes.Users_User.getGuid())).thenReturn(type2);
      when(type2.getDefaultOrderTypeGuid()).thenReturn(RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC.getGuid());

      IRelationSorterId actual2 = provider.getDefaultSorterId(CoreRelationTypes.Users_User);
      assertEquals(RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC, actual2);
   }

   @Test
   public void testGetDefaultSorterIdNull() throws OseeCoreException {
      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("type cannot be null");

      provider.getDefaultSorterId(null);
   }

   @Test
   public void testGetDefaultSorterIdTypeNotfound() throws OseeCoreException {
      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage(String.format("relationType cannot be null - RelationType was not found for [%s]",
         CoreRelationTypes.Allocation__Component));

      provider.getDefaultSorterId(CoreRelationTypes.Allocation__Component);
   }

   @Test
   public void testGetAllRelationOrderIds() {
      List<IRelationSorterId> actual = provider.getSorterIds();

      int index = 0;
      assertEquals(RelationOrderBaseTypes.LEXICOGRAPHICAL_ASC, actual.get(index++));
      assertEquals(RelationOrderBaseTypes.LEXICOGRAPHICAL_DESC, actual.get(index++));
      assertEquals(RelationOrderBaseTypes.UNORDERED, actual.get(index++));
      assertEquals(RelationOrderBaseTypes.USER_DEFINED, actual.get(index++));
   }

   @Test
   public void testGetRelationOrder() throws OseeCoreException {
      for (IRelationSorterId sorterId : RelationOrderBaseTypes.values()) {
         Sorter actual = provider.getSorter(sorterId);
         assertEquals(sorterId, actual.getId());
         boolean matches = false;

         if (sorterId == LEXICOGRAPHICAL_ASC) {
            matches = actual instanceof LexicographicalSorter;
         } else if (sorterId == LEXICOGRAPHICAL_DESC) {
            matches = actual instanceof LexicographicalSorter;
         } else if (sorterId == UNORDERED) {
            matches = actual instanceof UnorderedSorter;
         } else if (sorterId == USER_DEFINED) {
            matches = actual instanceof UserDefinedSorter;
         } else {
            assertNull("This line should not be reached");
         }
         assertTrue(matches);
      }
   }

   @Test
   public void testArgumentExceptions() throws OseeCoreException {
      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("sorterId cannot be null");
      provider.getSorter(null);
   }

   @Test
   public void testNotFoundExceptions() throws OseeCoreException {
      String randomGuid = GUID.create();
      String idName = "TestSorterId";
      IRelationSorterId sorterId = TokenFactory.createSorterId(randomGuid, "TestSorterId");
      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage(String.format("sorter cannot be null - Unable to locate sorter with sorterId [%s:%s]",
         idName, randomGuid));
      provider.getSorter(sorterId);
   }
}
