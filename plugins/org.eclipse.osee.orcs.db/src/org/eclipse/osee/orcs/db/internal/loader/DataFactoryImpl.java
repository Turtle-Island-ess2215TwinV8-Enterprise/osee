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

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.HumanReadableId;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.DataFactory;
import org.eclipse.osee.orcs.core.ds.OrcsData;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.ds.VersionData;
import org.eclipse.osee.orcs.data.HasLocalId;
import org.eclipse.osee.orcs.db.internal.OrcsObjectFactory;

/**
 * @author Roberto E. Escobar
 */
public class DataFactoryImpl implements DataFactory {

   private final IdFactory idFactory;
   private final OrcsObjectFactory objectFactory;
   private final ArtifactTypeCache artifactCache;

   public DataFactoryImpl(IdFactory idFactory, OrcsObjectFactory objectFactory, ArtifactTypeCache artifactCache) {
      super();
      this.idFactory = idFactory;
      this.objectFactory = objectFactory;
      this.artifactCache = artifactCache;
   }

   @Override
   public ArtifactData create(IOseeBranch branch, IArtifactType artifactType, String guid) throws OseeCoreException {
      return create(branch, artifactType, guid, null);
   }

   @Override
   public ArtifactData create(IOseeBranch branch, IArtifactType token, String guid, String hrid) throws OseeCoreException {
      Conditions.checkNotNull(branch, "branch");

      ArtifactType artifactType = artifactCache.get(token);
      Conditions.checkNotNull(artifactType, "artifactType", "Unable to find artifactType matching [%s]", token);

      Conditions.checkExpressionFailOnTrue(artifactType.isAbstract(),
         "Cannot create an instance of abstract type [%s]", artifactType);

      String guidToSet = idFactory.getUniqueGuid(guid);
      String humanReadableId = idFactory.getUniqueHumanReadableId(hrid);

      Conditions.checkExpressionFailOnTrue(!GUID.isValid(guidToSet),
         "Invalid guid [%s] during artifact creation [type: %s]", guidToSet, artifactType);

      Conditions.checkExpressionFailOnTrue(!HumanReadableId.isValid(humanReadableId),
         "Invalid human readable id [%s] during artifact creation [type: %s, guid: %s]", humanReadableId, artifactType,
         guid);

      int branchId = idFactory.getBranchId(branch);

      VersionData version = objectFactory.createDefaultVersionData();
      version.setBranchId(branchId);

      ModificationType modType = RelationalConstants.DEFAULT_MODIFICATION_TYPE;
      int artifactId = idFactory.getNextArtifactId();
      ArtifactData artifactData =
         objectFactory.createArtifactData(version, artifactId, artifactType, modType, guidToSet, humanReadableId);
      return artifactData;
   }

   @Override
   public ArtifactData copy(IOseeBranch destination, ArtifactData source) throws OseeCoreException {
      ArtifactData copy = objectFactory.createCopy(source);
      updateDataForCopy(destination, copy);
      copy.setGuid(idFactory.getUniqueGuid(null));
      copy.setHumanReadableId(idFactory.getUniqueHumanReadableId(null));
      copy.setLocalId(idFactory.getNextArtifactId());
      return copy;
   }

   @Override
   public AttributeData introduce(IOseeBranch destination, AttributeData source) throws OseeCoreException {
      AttributeData newVersion = objectFactory.createCopy(source);
      updateDataForIntroduce(destination, newVersion);
      return newVersion;
   }

   @Override
   public AttributeData create(ArtifactData parent, IAttributeType attributeType) throws OseeCoreException {
      VersionData version = objectFactory.createDefaultVersionData();
      version.setBranchId(parent.getVersion().getBranchId());
      ModificationType modType = RelationalConstants.DEFAULT_MODIFICATION_TYPE;
      int attributeId = RelationalConstants.DEFAULT_ITEM_ID;
      return objectFactory.createAttributeData(version, attributeId, attributeType, modType, parent.getLocalId());
   }

   @Override
   public AttributeData copy(IOseeBranch destination, AttributeData orcsData) throws OseeCoreException {
      AttributeData copy = objectFactory.createCopy(orcsData);
      updateDataForCopy(destination, copy);
      copy.setLocalId(RelationalConstants.DEFAULT_ITEM_ID);
      return copy;
   }

   @Override
   public ArtifactData introduce(IOseeBranch destination, ArtifactData source) throws OseeCoreException {
      ArtifactData newVersion = objectFactory.createCopy(source);
      updateDataForIntroduce(destination, newVersion);
      return newVersion;
   }

   @Override
   public RelationData createRelationData(IRelationType relationType, ArtifactData parent, HasLocalId aArt, HasLocalId bArt, String rationale) throws OseeCoreException {
      VersionData version = objectFactory.createDefaultVersionData();
      version.setBranchId(parent.getVersion().getBranchId());
      ModificationType modType = RelationalConstants.DEFAULT_MODIFICATION_TYPE;
      int relationId = RelationalConstants.DEFAULT_ITEM_ID;
      return objectFactory.createRelationData(version, relationId, relationType, modType, parent.getLocalId(),
         aArt.getLocalId(), bArt.getLocalId(), rationale);
   }

   @Override
   public ArtifactData clone(ArtifactData source) {
      return objectFactory.createCopy(source);
   }

   @Override
   public AttributeData clone(AttributeData source) throws OseeCoreException {
      return objectFactory.createCopy(source);
   }

   @Override
   public RelationData clone(RelationData source) throws OseeCoreException {
      return objectFactory.createCopy(source);
   }

   private void updateDataForCopy(IOseeBranch destination, OrcsData data) throws OseeCoreException {
      VersionData version = data.getVersion();
      version.setBranchId(idFactory.getBranchId(destination));
      version.setTransactionId(RelationalConstants.TRANSACTION_SENTINEL);
      version.setStripeId(RelationalConstants.TRANSACTION_SENTINEL);
      version.setHistorical(false);
      version.setGammaId(RelationalConstants.GAMMA_SENTINEL);

      data.setModType(ModificationType.NEW);
   }

   private void updateDataForIntroduce(IOseeBranch destination, OrcsData data) throws OseeCoreException {
      VersionData version = data.getVersion();
      version.setBranchId(idFactory.getBranchId(destination));
      version.setTransactionId(RelationalConstants.TRANSACTION_SENTINEL);
      version.setStripeId(RelationalConstants.TRANSACTION_SENTINEL);
      version.setHistorical(false);
      // do not clear gammaId for introduce case so we reuse the same version
      data.setModType(ModificationType.INTRODUCED);
   }
}
