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
package org.eclipse.osee.orcs.db.internal.loader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.HumanReadableId;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.DataFactory;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.ds.VersionData;
import org.eclipse.osee.orcs.data.HasLocalId;
import org.eclipse.osee.orcs.db.internal.OrcsObjectFactory;
import org.eclipse.osee.orcs.db.internal.loader.data.OrcsObjectFactoryImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link DataFactoryImplTest} and {@link OrcsObjectFactoryImpl}
 * 
 * @author Roberto E. Escobar
 */
public class DataFactoryImplTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   //@formatter:off
   @Mock private IdFactory idFactory;
   @Mock private ProxyDataFactory proxyFactory;
   @Mock private IdentityService identityService;
   @Mock private ArtifactTypeCache artifactCache;
   
   @Mock private ArtifactData artData;
   @Mock private AttributeData attrData;
   @Mock private RelationData relData;
   @Mock private VersionData verData;
   @Mock private DataProxy dataProxy;
   @Mock private DataProxy otherDataProxy;
   
   @Mock private ArtifactType artifactType;
   @Mock private IArtifactType artifactTypeToken;
   //@formatter:on

   private DataFactory dataFactory;
   private Object[] expectedProxyData;
   private String guid;
   private String hrid;

   @Before
   public void setUp() throws OseeCoreException {
      MockitoAnnotations.initMocks(this);

      guid = GUID.create();
      hrid = HumanReadableId.generate();

      OrcsObjectFactory objectFactory = new OrcsObjectFactoryImpl(proxyFactory, identityService);
      dataFactory = new DataFactoryImpl(idFactory, objectFactory, artifactCache);

      // VERSION
      when(verData.getBranchId()).thenReturn(11);
      when(verData.getGammaId()).thenReturn(222L);
      when(verData.getTransactionId()).thenReturn(333);
      when(verData.getStripeId()).thenReturn(444);
      when(verData.isHistorical()).thenReturn(true);

      // ARTIFACT
      when(artData.getVersion()).thenReturn(verData);
      when(artData.getLocalId()).thenReturn(555);
      when(artData.getModType()).thenReturn(ModificationType.MODIFIED);
      when(artData.getTypeUuid()).thenReturn(666L);
      when(artData.getLoadedModType()).thenReturn(ModificationType.NEW);
      when(artData.getLoadedTypeUuid()).thenReturn(777L);
      when(artData.getGuid()).thenReturn("abcdefg");
      when(artData.getHumanReadableId()).thenReturn("abc34");

      // ATTRIBUTE
      when(attrData.getVersion()).thenReturn(verData);
      when(attrData.getLocalId()).thenReturn(555);
      when(attrData.getModType()).thenReturn(ModificationType.MODIFIED);
      when(attrData.getTypeUuid()).thenReturn(666L);
      when(attrData.getLoadedModType()).thenReturn(ModificationType.NEW);
      when(attrData.getLoadedTypeUuid()).thenReturn(777L);
      when(attrData.getArtifactId()).thenReturn(88);
      when(attrData.getDataProxy()).thenReturn(dataProxy);

      expectedProxyData = new Object[] {45, "hello", "hello"};
      when(dataProxy.getData()).thenReturn(expectedProxyData);
      when(proxyFactory.createProxy(666L, expectedProxyData)).thenReturn(otherDataProxy);
      when(otherDataProxy.getData()).thenReturn(new Object[] {45, "hello", "hello"});

      // RELATION
      when(relData.getVersion()).thenReturn(verData);
      when(relData.getLocalId()).thenReturn(555);
      when(relData.getModType()).thenReturn(ModificationType.MODIFIED);
      when(relData.getTypeUuid()).thenReturn(666L);
      when(relData.getLoadedModType()).thenReturn(ModificationType.NEW);
      when(relData.getLoadedTypeUuid()).thenReturn(777L);
      when(relData.getArtIdA()).thenReturn(88);
      when(relData.getArtIdB()).thenReturn(99);
      when(relData.getParentId()).thenReturn(1111);
      when(relData.getRationale()).thenReturn("this is the rationale");

      when(idFactory.getBranchId(CoreBranches.COMMON)).thenReturn(657);
   }

   @Test
   public void testCreateArtifactDataNullType() throws OseeCoreException {
      when(artifactTypeToken.toString()).thenReturn("artifactTypeToken");
      when(artifactCache.get(artifactTypeToken)).thenReturn(null);

      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("artifactType cannot be null - Unable to find artifactType matching [artifactTypeToken]");
      dataFactory.create(CoreBranches.COMMON, artifactTypeToken, guid, hrid);
   }

   @Test
   public void testCreateArtifactDataUsingAbstratArtifactType() throws OseeCoreException {
      when(artifactType.toString()).thenReturn("artifactType");
      when(artifactCache.get(artifactTypeToken)).thenReturn(artifactType);
      when(artifactType.isAbstract()).thenReturn(true);

      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("Cannot create an instance of abstract type [artifactType]");
      dataFactory.create(CoreBranches.COMMON, artifactTypeToken, guid, hrid);
   }

   @Test
   public void testCreateArtifactDataInvalidGuid() throws OseeCoreException {
      when(artifactCache.get(artifactTypeToken)).thenReturn(artifactType);
      when(artifactType.isAbstract()).thenReturn(false);
      when(artifactType.toString()).thenReturn("artifactType");

      when(idFactory.getUniqueGuid(guid)).thenReturn("123");

      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("Invalid guid [123] during artifact creation [type: artifactType]");

      dataFactory.create(CoreBranches.COMMON, artifactTypeToken, guid, hrid);
   }

   @Test
   public void testCreateArtifactDataInvalidHrid() throws OseeCoreException {
      when(artifactCache.get(artifactTypeToken)).thenReturn(artifactType);
      when(artifactType.isAbstract()).thenReturn(false);
      when(artifactType.toString()).thenReturn("artifactType");

      when(idFactory.getUniqueGuid(guid)).thenReturn(guid);
      when(idFactory.getUniqueHumanReadableId(hrid)).thenReturn("123");

      thrown.expect(OseeArgumentException.class);
      thrown.expectMessage("Invalid human readable id [123] during artifact creation [type: artifactType, guid: " + guid + "]");

      dataFactory.create(CoreBranches.COMMON, artifactTypeToken, guid, hrid);
   }

   @Test
   public void testCreateArtifactData() throws OseeCoreException {
      when(artifactCache.get(artifactTypeToken)).thenReturn(artifactType);
      when(artifactType.getGuid()).thenReturn(4536L);
      when(artifactType.isAbstract()).thenReturn(false);
      when(idFactory.getUniqueGuid(guid)).thenReturn(guid);
      when(idFactory.getUniqueHumanReadableId(hrid)).thenReturn(hrid);
      when(idFactory.getNextArtifactId()).thenReturn(987);

      ArtifactData actual = dataFactory.create(CoreBranches.COMMON, artifactTypeToken, guid, hrid);
      verify(idFactory).getBranchId(CoreBranches.COMMON);
      verify(idFactory).getUniqueGuid(guid);
      verify(idFactory).getUniqueHumanReadableId(hrid);
      verify(idFactory).getNextArtifactId();

      VersionData actualVer = actual.getVersion();

      assertEquals(657, actualVer.getBranchId());
      assertEquals(RelationalConstants.GAMMA_SENTINEL, actualVer.getGammaId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getTransactionId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(987, actual.getLocalId());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getModType());
      assertEquals(4536L, actual.getTypeUuid());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getLoadedModType());
      assertEquals(4536L, actual.getLoadedTypeUuid());
      assertEquals(guid, actual.getGuid());
      assertEquals(hrid, actual.getHumanReadableId());
   }

   @Test
   public void testCreateArtifactDataGenerateHrid() throws OseeCoreException {
      String newHrid = HumanReadableId.generate();

      when(artifactCache.get(artifactTypeToken)).thenReturn(artifactType);
      when(artifactType.getGuid()).thenReturn(4536L);
      when(artifactType.isAbstract()).thenReturn(false);
      when(idFactory.getUniqueGuid(guid)).thenReturn(guid);
      when(idFactory.getUniqueHumanReadableId(null)).thenReturn(newHrid);
      when(idFactory.getNextArtifactId()).thenReturn(987);

      ArtifactData actual = dataFactory.create(CoreBranches.COMMON, artifactTypeToken, guid);
      verify(idFactory).getBranchId(CoreBranches.COMMON);
      verify(idFactory).getUniqueGuid(guid);
      verify(idFactory).getUniqueHumanReadableId(null);
      verify(idFactory).getNextArtifactId();

      VersionData actualVer = actual.getVersion();
      assertEquals(657, actualVer.getBranchId());
      assertEquals(RelationalConstants.GAMMA_SENTINEL, actualVer.getGammaId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getTransactionId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(987, actual.getLocalId());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getModType());
      assertEquals(4536L, actual.getTypeUuid());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getLoadedModType());
      assertEquals(4536L, actual.getLoadedTypeUuid());
      assertEquals(guid, actual.getGuid());
      assertEquals(newHrid, actual.getHumanReadableId());
   }

   @Test
   public void testCreateAttributeData() throws OseeCoreException {
      IAttributeType attributeType = mock(IAttributeType.class);

      when(attributeType.getGuid()).thenReturn(2389L);
      when(proxyFactory.createProxy(2389L, "", "")).thenReturn(otherDataProxy);
      when(otherDataProxy.getData()).thenReturn(new Object[] {2389L, "", ""});

      AttributeData actual = dataFactory.create(artData, attributeType);

      VersionData actualVer = actual.getVersion();
      assertEquals(11, actualVer.getBranchId());
      assertEquals(RelationalConstants.GAMMA_SENTINEL, actualVer.getGammaId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getTransactionId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(RelationalConstants.DEFAULT_ITEM_ID, actual.getLocalId());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getModType());
      assertEquals(2389L, actual.getTypeUuid());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getLoadedModType());
      assertEquals(2389L, actual.getLoadedTypeUuid());

      assertEquals(555, actual.getArtifactId());
      assertNotSame(dataProxy, actual.getDataProxy());

      Object[] objData = actual.getDataProxy().getData();
      assertEquals(2389L, objData[0]);
      assertEquals("", objData[1]);
      assertEquals("", objData[2]);
   }

   @Test
   public void testCreateRelationData() throws OseeCoreException {
      IRelationType relationType = mock(IRelationType.class);
      HasLocalId localId1 = mock(HasLocalId.class);
      HasLocalId localId2 = mock(HasLocalId.class);

      when(relationType.getGuid()).thenReturn(2389L);
      when(localId1.getLocalId()).thenReturn(4562);
      when(localId2.getLocalId()).thenReturn(9513);

      RelationData actual = dataFactory.createRelationData(relationType, artData, localId1, localId2, "My rationale");

      VersionData actualVer = actual.getVersion();
      assertEquals(11, actualVer.getBranchId());
      assertEquals(RelationalConstants.GAMMA_SENTINEL, actualVer.getGammaId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getTransactionId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(RelationalConstants.DEFAULT_ITEM_ID, actual.getLocalId());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getModType());
      assertEquals(2389L, actual.getTypeUuid());
      assertEquals(RelationalConstants.DEFAULT_MODIFICATION_TYPE, actual.getLoadedModType());
      assertEquals(2389L, actual.getLoadedTypeUuid());

      assertEquals(4562, actual.getArtIdA());
      assertEquals(9513, actual.getArtIdB());
      assertEquals(555, actual.getParentId());
      assertEquals("My rationale", actual.getRationale());
   }

   @Test
   public void testIntroduceArtifactData() throws OseeCoreException {
      ArtifactData actual = dataFactory.introduce(CoreBranches.COMMON, artData);
      verify(idFactory).getBranchId(CoreBranches.COMMON);

      VersionData actualVer = actual.getVersion();
      assertNotSame(verData, actualVer);
      assertEquals(657, actualVer.getBranchId());
      assertEquals(222L, actualVer.getGammaId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getTransactionId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(555, actual.getLocalId());
      assertEquals(ModificationType.INTRODUCED, actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getLoadedModType());
      assertEquals(777L, actual.getLoadedTypeUuid());
      assertEquals("abcdefg", actual.getGuid());
      assertEquals("abc34", actual.getHumanReadableId());
   }

   @Test
   public void testIntroduceAttributeData() throws OseeCoreException {
      AttributeData actual = dataFactory.introduce(CoreBranches.COMMON, attrData);
      verify(idFactory).getBranchId(CoreBranches.COMMON);

      VersionData actualVer = actual.getVersion();
      assertNotSame(verData, actualVer);
      assertEquals(657, actualVer.getBranchId());
      assertEquals(222L, actualVer.getGammaId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getTransactionId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(555, actual.getLocalId());
      assertEquals(ModificationType.INTRODUCED, actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getLoadedModType());
      assertEquals(777L, actual.getLoadedTypeUuid());

      assertEquals(88, actual.getArtifactId());
      assertNotSame(dataProxy, actual.getDataProxy());

      Object[] objData = actual.getDataProxy().getData();
      assertNotSame(expectedProxyData, objData);
      assertEquals(expectedProxyData[0], objData[0]);
      assertEquals(expectedProxyData[1], objData[1]);
      assertEquals(expectedProxyData[2], objData[2]);
   }

   @Test
   public void testCopyArtifactData() throws OseeCoreException {
      String newGuid = GUID.create();
      String newHrid = HumanReadableId.generate();
      when(idFactory.getNextArtifactId()).thenReturn(987);
      when(idFactory.getUniqueGuid(null)).thenReturn(newGuid);
      when(idFactory.getUniqueHumanReadableId(null)).thenReturn(newHrid);

      ArtifactData actual = dataFactory.copy(CoreBranches.COMMON, artData);
      verify(idFactory).getBranchId(CoreBranches.COMMON);
      verify(idFactory).getUniqueGuid(null);
      verify(idFactory).getUniqueHumanReadableId(null);

      VersionData actualVer = actual.getVersion();
      assertNotSame(verData, actualVer);
      assertEquals(657, actualVer.getBranchId());
      assertEquals(RelationalConstants.GAMMA_SENTINEL, actualVer.getGammaId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getTransactionId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(987, actual.getLocalId());
      assertEquals(ModificationType.NEW, actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getLoadedModType());
      assertEquals(777L, actual.getLoadedTypeUuid());
      assertEquals(newGuid, actual.getGuid());
      assertEquals(newHrid, actual.getHumanReadableId());
   }

   @Test
   public void testCopyAttributeData() throws OseeCoreException {
      AttributeData actual = dataFactory.copy(CoreBranches.COMMON, attrData);
      verify(idFactory).getBranchId(CoreBranches.COMMON);

      VersionData actualVer = actual.getVersion();
      assertNotSame(verData, actualVer);
      assertEquals(657, actualVer.getBranchId());
      assertEquals(RelationalConstants.GAMMA_SENTINEL, actualVer.getGammaId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getTransactionId());
      assertEquals(RelationalConstants.TRANSACTION_SENTINEL, actualVer.getStripeId());
      assertEquals(false, actualVer.isHistorical());
      assertEquals(false, actualVer.isInStorage());

      assertEquals(RelationalConstants.DEFAULT_ITEM_ID, actual.getLocalId());
      assertEquals(ModificationType.NEW, actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getLoadedModType());
      assertEquals(777L, actual.getLoadedTypeUuid());

      assertEquals(88, actual.getArtifactId());
      assertNotSame(dataProxy, actual.getDataProxy());

      Object[] objData = actual.getDataProxy().getData();
      assertNotSame(expectedProxyData, objData);
      assertEquals(expectedProxyData[0], objData[0]);
      assertEquals(expectedProxyData[1], objData[1]);
      assertEquals(expectedProxyData[2], objData[2]);
   }

   @Test
   public void testCloneArtifactData() {
      ArtifactData actual = dataFactory.clone(artData);
      VersionData actualVer = actual.getVersion();

      assertNotSame(artData, actual);
      assertNotSame(verData, actualVer);

      assertEquals(11, actualVer.getBranchId());
      assertEquals(222L, actualVer.getGammaId());
      assertEquals(333, actualVer.getTransactionId());
      assertEquals(444, actualVer.getStripeId());
      assertEquals(true, actualVer.isHistorical());
      assertEquals(true, actualVer.isInStorage());

      assertEquals(555, actual.getLocalId());
      assertEquals(ModificationType.MODIFIED, actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getLoadedModType());
      assertEquals(777L, actual.getLoadedTypeUuid());
      assertEquals("abcdefg", actual.getGuid());
      assertEquals("abc34", actual.getHumanReadableId());
   }

   @Test
   public void testCloneAttributeData() throws OseeCoreException {
      AttributeData actual = dataFactory.clone(attrData);
      verify(proxyFactory).createProxy(666L, expectedProxyData);

      VersionData actualVer = actual.getVersion();

      assertNotSame(attrData, actual);
      assertNotSame(verData, actualVer);

      assertEquals(11, actualVer.getBranchId());
      assertEquals(222L, actualVer.getGammaId());
      assertEquals(333, actualVer.getTransactionId());
      assertEquals(444, actualVer.getStripeId());
      assertEquals(true, actualVer.isHistorical());
      assertEquals(true, actualVer.isInStorage());

      assertEquals(555, actual.getLocalId());
      assertEquals(ModificationType.MODIFIED, actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getLoadedModType());
      assertEquals(777L, actual.getLoadedTypeUuid());

      assertEquals(88, actual.getArtifactId());
      assertNotSame(dataProxy, actual.getDataProxy());

      Object[] objData = actual.getDataProxy().getData();
      assertNotSame(expectedProxyData, objData);
      assertEquals(expectedProxyData[0], objData[0]);
      assertEquals(expectedProxyData[1], objData[1]);
      assertEquals(expectedProxyData[2], objData[2]);
   }

   @Test
   public void testCloneRelationData() throws OseeCoreException {
      RelationData actual = dataFactory.clone(relData);
      VersionData actualVer = actual.getVersion();

      assertNotSame(relData, actual);
      assertNotSame(verData, actualVer);

      assertEquals(11, actualVer.getBranchId());
      assertEquals(222L, actualVer.getGammaId());
      assertEquals(333, actualVer.getTransactionId());
      assertEquals(444, actualVer.getStripeId());
      assertEquals(true, actualVer.isHistorical());
      assertEquals(true, actualVer.isInStorage());

      assertEquals(555, actual.getLocalId());
      assertEquals(ModificationType.MODIFIED, actual.getModType());
      assertEquals(666L, actual.getTypeUuid());
      assertEquals(ModificationType.NEW, actual.getLoadedModType());
      assertEquals(777L, actual.getLoadedTypeUuid());

      assertEquals(88, actual.getArtIdA());
      assertEquals(99, actual.getArtIdB());
      assertEquals(1111, actual.getParentId());
      assertEquals("this is the rationale", actual.getRationale());
   }

}