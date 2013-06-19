/*
 * Created on Jun 17, 2013
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.orcs.db.internal.loader.data;

import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.AttributePersistData;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.core.ds.VersionData;

public class AttributePersistDataImpl implements AttributePersistData {

   private final AttributeData attrData;
   private final DataProxy dataProxy;

   public AttributePersistDataImpl(AttributeData attrData, DataProxy dataProxy) {
      this.attrData = attrData;
      this.dataProxy = dataProxy;
   }

   @Override
   public int getArtifactId() {
      return attrData.getArtifactId();
   }

   @Override
   public void setArtifactId(int artifactId) {
      attrData.setArtifactId(artifactId);
   }

   @Override
   public String getValue() {
      return attrData.getValue();
   }

   @Override
   public String getUri() {
      return attrData.getUri();
   }

   @Override
   public void setLocalId(int localId) {
      attrData.setLocalId(localId);
   }

   @Override
   public long getTypeUuid() {
      return attrData.getTypeUuid();
   }

   @Override
   public void setTypeUuid(long typeUuid) {
      attrData.setTypeUuid(typeUuid);
   }

   @Override
   public long getLoadedTypeUuid() {
      return attrData.getLoadedTypeUuid();
   }

   @Override
   public void setLoadedTypeUuid(long loadedTypeUuid) {
      attrData.setLoadedTypeUuid(loadedTypeUuid);
   }

   @Override
   public ModificationType getModType() {
      return attrData.getModType();
   }

   @Override
   public void setModType(ModificationType modType) {
      attrData.setModType(modType);
   }

   @Override
   public void setLoadedModType(ModificationType modType) {
      attrData.setLoadedModType(modType);
   }

   @Override
   public ModificationType getLoadedModType() {
      return attrData.getLoadedModType();
   }

   @Override
   public int getLocalId() {
      return attrData.getLocalId();
   }

   @Override
   public VersionData getVersion() {
      return attrData.getVersion();
   }

   @Override
   public DataProxy getDataProxy() {
      return dataProxy;
   }

}
