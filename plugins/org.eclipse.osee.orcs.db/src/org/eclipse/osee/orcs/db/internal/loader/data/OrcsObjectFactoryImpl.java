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
package org.eclipse.osee.orcs.db.internal.loader.data;

import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.AttributePersistData;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.ds.VersionData;
import org.eclipse.osee.orcs.db.internal.OrcsObjectFactory;
import org.eclipse.osee.orcs.db.internal.loader.RelationalConstants;

/**
 * @author Roberto E. Escobar
 */
public class OrcsObjectFactoryImpl implements OrcsObjectFactory {

   private final IdentityService identityService;

   public OrcsObjectFactoryImpl(IdentityService identityService) {
      super();
      this.identityService = identityService;
   }

   private long toUuid(int localId) throws OseeCoreException {
      return identityService.getUniversalId(localId);
   }

   @Override
   public VersionData createVersion(int branchId, int txId, long gamma, boolean historical) {
      return createVersion(branchId, txId, gamma, historical, RelationalConstants.TRANSACTION_SENTINEL);
   }

   @Override
   public VersionData createDefaultVersionData() {
      // @formatter:off
      return createVersion(
         RelationalConstants.BRANCH_SENTINEL, 
         RelationalConstants.TRANSACTION_SENTINEL, 
         RelationalConstants.GAMMA_SENTINEL, 
         RelationalConstants.IS_HISTORICAL_DEFAULT, 
         RelationalConstants.TRANSACTION_SENTINEL);
      // @formatter:on
   }

   @Override
   public VersionData createCopy(VersionData other) {
      // @formatter:off
      return createVersion(
         other.getBranchId(),
         other.getTransactionId(), 
         other.getGammaId(), 
         other.isHistorical(), 
         other.getStripeId()); 
      // @formatter:on
   }

   private VersionData createVersion(int branchId, int txId, long gamma, boolean historical, int stripeId) {
      VersionData version = new VersionDataImpl();
      version.setBranchId(branchId);
      version.setTransactionId(txId);
      version.setGammaId(gamma);
      version.setHistorical(historical);
      version.setStripeId(stripeId);
      return version;
   }

   @Override
   public ArtifactData createArtifactData(VersionData version, int localId, int localTypeID, ModificationType modType, String guid, String humanReadableId) throws OseeCoreException {
      long typeUuid = toUuid(localTypeID);
      return createArtifactFromRow(version, localId, typeUuid, modType, typeUuid, modType, guid, humanReadableId);
   }

   @Override
   public ArtifactData createArtifactData(VersionData version, int localId, long typeUuid, ModificationType modType, String guid, String humanReadableId) {
      return createArtifactFromRow(version, localId, typeUuid, modType, typeUuid, modType, guid, humanReadableId);
   }

   @Override
   public ArtifactData createCopy(ArtifactData source) {
      VersionData newVersion = createCopy(source.getVersion());
      return createArtifactFromRow(newVersion, source.getLocalId(), source.getTypeUuid(), source.getModType(),
         source.getLoadedTypeUuid(), source.getLoadedModType(), source.getGuid(), source.getHumanReadableId());
   }

   @Override
   public AttributeData createAttributeData(VersionData version, int localId, int localTypeID, ModificationType modType, int artifactId, String value, String uri) throws OseeCoreException {
      long typeId = toUuid(localTypeID);
      return createAttributeFromRow(version, localId, typeId, modType, typeId, modType, artifactId, value, uri);
   }

   @Override
   public AttributeData createCopy(AttributeData source) {
      VersionData newVersion = createCopy(source.getVersion());
      long typeId = source.getTypeUuid();
      return createAttributeFromRow(newVersion, source.getLocalId(), typeId, source.getModType(),
         source.getLoadedTypeUuid(), source.getLoadedModType(), source.getArtifactId(), source.getValue(),
         source.getUri());
   }

   @Override
   public AttributeData createAttributeData(VersionData version, int localId, IAttributeType type, ModificationType modType, int artId) throws OseeCoreException {
      long typeId = type.getGuid();
      return createAttributeFromRow(version, localId, typeId, modType, typeId, modType, artId, Strings.EMPTY_STRING,
         Strings.EMPTY_STRING);
   }

   @Override
   public RelationData createRelationData(VersionData version, int localId, int localTypeID, ModificationType modType, int parentId, int aArtId, int bArtId, String rationale) throws OseeCoreException {
      long typeId = toUuid(localTypeID);
      return createRelationData(version, localId, typeId, modType, typeId, modType, parentId, aArtId, bArtId, rationale);
   }

   @Override
   public RelationData createRelationData(VersionData version, int localId, IRelationType type, ModificationType modType, int parentId, int aArtId, int bArtId, String rationale) {
      long typeId = type.getGuid();
      return createRelationData(version, localId, typeId, modType, typeId, modType, parentId, aArtId, bArtId, rationale);
   }

   private ArtifactData createArtifactFromRow(VersionData version, int localId, long localTypeID, ModificationType modType, long loadedLocalTypeID, ModificationType loadedModType, String guid, String humanReadableId) {
      ArtifactData data = new ArtifactDataImpl(version);
      data.setLocalId(localId);
      data.setTypeUuid(localTypeID);
      data.setLoadedTypeUuid(loadedLocalTypeID);
      data.setModType(modType);
      data.setLoadedModType(loadedModType);
      data.setGuid(guid);
      data.setHumanReadableId(humanReadableId);
      return data;
   }

   private AttributeData createAttributeFromRow(VersionData version, int localId, long localTypeID, ModificationType modType, long loadedLocalTypeID, ModificationType loadedModType, int artifactId, String value, String uri) {
      AttributeDataImpl data = new AttributeDataImpl(version);
      data.setLocalId(localId);
      data.setTypeUuid(localTypeID);
      data.setLoadedTypeUuid(loadedLocalTypeID);
      data.setModType(modType);
      data.setLoadedModType(loadedModType);
      data.setArtifactId(artifactId);
      data.setValue(value);
      data.setUri(uri);
      return data;
   }

   private RelationData createRelationData(VersionData version, int localId, long localTypeID, ModificationType modType, long loadedLocalTypeID, ModificationType loadedModType, int parentId, int aArtId, int bArtId, String rationale) {
      RelationData data = new RelationDataImpl(version);
      data.setLocalId(localId);
      data.setTypeUuid(localTypeID);
      data.setLoadedTypeUuid(loadedLocalTypeID);
      data.setModType(modType);
      data.setLoadedModType(loadedModType);
      data.setParentId(parentId);
      data.setArtIdA(aArtId);
      data.setArtIdB(bArtId);
      data.setRationale(rationale);
      return data;
   }

   @Override
   public RelationData createCopy(RelationData source) {
      VersionData newVersion = createCopy(source.getVersion());
      return createRelationData(newVersion, source.getLocalId(), source.getTypeUuid(), source.getModType(),
         source.getLoadedTypeUuid(), source.getLoadedModType(), source.getParentId(), source.getArtIdA(),
         source.getArtIdB(), source.getRationale());
   }

   @Override
   public AttributePersistData createPersistCopy(AttributeData source, DataProxy dataProxy) {
      AttributeData copy = createCopy(source);
      return new AttributePersistDataImpl(copy, dataProxy);
   }

}
