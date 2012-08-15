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
package org.eclipse.osee.orcs.core.internal.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.enums.RelationTypeMultiplicity;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link RelationTypeValidityImpl}
 * 
 * @author Roberto E. Escobar
 */
public class RelationTypeValidityImplTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   // @formatter:off
   @Mock RelationTypeCache cache;
   @Mock ArtifactType artifactType;
   @Mock RelationType relationType1;
   @Mock RelationType relationType2;
   @Mock RelationType relationType3;
   @Mock RelationType relationType4;
   // @formatter:on

   private final IRelationTypeSide relationTypeSide = CoreRelationTypes.Default_Hierarchical__Child;
   private RelationTypeValidity validity;

   @Before
   public void init() throws OseeCoreException {
      MockitoAnnotations.initMocks(this);

      validity = new RelationTypeValidityImpl(cache);

      when(cache.get(relationTypeSide)).thenReturn(relationType1);
   }

   @Test
   public void testMaximumRelationAllowedNullArtifactType() throws OseeCoreException {
      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("artifactType cannot be null");
      validity.getMaximumRelationAllowed(null, relationTypeSide);
   }

   @Test
   public void testMaximumRelationAllowedNullRelationTypeSide() throws OseeCoreException {
      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("relationTypeSide cannot be null");
      validity.getMaximumRelationAllowed(artifactType, null);
   }

   @Test
   public void testValidRelationTypesNullArtifactType() throws OseeCoreException {
      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("artifactType cannot be null");
      validity.getValidRelationTypes(null);
   }

   @Test
   public void testMaximumRelationAllowed1() throws OseeCoreException {
      when(relationType1.isArtifactTypeAllowed(RelationSide.SIDE_B, artifactType)).thenReturn(true);
      when(relationType1.getMultiplicity()).thenReturn(RelationTypeMultiplicity.MANY_TO_MANY);

      int actual = validity.getMaximumRelationAllowed(artifactType, relationTypeSide);
      assertEquals(Integer.MAX_VALUE, actual);
   }

   @Test
   public void testMaximumRelationAllowed2() throws OseeCoreException {
      when(relationType1.isArtifactTypeAllowed(RelationSide.SIDE_B, artifactType)).thenReturn(true);
      when(relationType1.getMultiplicity()).thenReturn(RelationTypeMultiplicity.MANY_TO_ONE);

      int actual = validity.getMaximumRelationAllowed(artifactType, relationTypeSide);
      assertEquals(1, actual);
   }

   @Test
   public void testMaximumRelationAllowed3() throws OseeCoreException {
      when(relationType1.isArtifactTypeAllowed(RelationSide.SIDE_A, artifactType)).thenReturn(true);

      int actual = validity.getMaximumRelationAllowed(artifactType, relationTypeSide);
      assertEquals(0, actual);
   }

   @Test
   public void testValidRelationTypes() throws OseeCoreException {
      Collection<RelationType> types = Arrays.asList(relationType1, relationType2, relationType3, relationType4);
      when(cache.getAll()).thenReturn(types);

      when(relationType1.isArtifactTypeAllowed(RelationSide.SIDE_B, artifactType)).thenReturn(true);
      when(relationType1.getMultiplicity()).thenReturn(RelationTypeMultiplicity.MANY_TO_MANY);

      when(relationType2.isArtifactTypeAllowed(RelationSide.SIDE_A, artifactType)).thenReturn(false);
      when(relationType2.getMultiplicity()).thenReturn(RelationTypeMultiplicity.MANY_TO_ONE);

      when(relationType3.isArtifactTypeAllowed(RelationSide.SIDE_A, artifactType)).thenReturn(true);
      when(relationType3.getMultiplicity()).thenReturn(RelationTypeMultiplicity.ONE_TO_ONE);

      when(relationType4.isArtifactTypeAllowed(RelationSide.SIDE_A, artifactType)).thenReturn(false);
      when(relationType4.getMultiplicity()).thenReturn(RelationTypeMultiplicity.ONE_TO_MANY);

      List<RelationType> actual = validity.getValidRelationTypes(artifactType);

      assertEquals(2, actual.size());
      assertTrue(actual.contains(relationType1));
      assertFalse(actual.contains(relationType2));
      assertTrue(actual.contains(relationType3));
      assertFalse(actual.contains(relationType4));
   }

}
