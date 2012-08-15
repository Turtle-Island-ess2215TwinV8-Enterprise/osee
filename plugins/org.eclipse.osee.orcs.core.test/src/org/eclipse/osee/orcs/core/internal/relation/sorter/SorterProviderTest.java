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
import java.util.List;
import org.eclipse.osee.framework.core.data.IRelationSorterId;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test Case for {@link SorterProvider}
 * 
 * @author Roberto E. Escobar
 */
public class SorterProviderTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   private SorterProvider provider;

   @Before
   public void setUp() {
      provider = new SorterProvider();
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
      for (RelationOrderBaseTypes baseType : RelationOrderBaseTypes.values) {
         Sorter actual = provider.getSorter(baseType.getGuid());
         assertEquals(baseType, actual.getId());
         boolean matches = false;

         if (baseType == LEXICOGRAPHICAL_ASC) {
            matches = actual instanceof LexicographicalSorter;
         } else if (baseType == LEXICOGRAPHICAL_DESC) {
            matches = actual instanceof LexicographicalSorter;
         } else if (baseType == UNORDERED) {
            matches = actual instanceof UnorderedSorter;
         } else if (baseType == USER_DEFINED) {
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
      thrown.expectMessage("Error invalid id argument [ABC]");
      provider.getSorter("ABC");
   }

   @Test
   public void testNotFoundExceptions() throws OseeCoreException {
      String randomGuid = GUID.create();

      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage(String.format("sorter cannot be null - Unable to locate sorter with id[%s]", randomGuid));
      provider.getSorter(randomGuid);
   }

}
