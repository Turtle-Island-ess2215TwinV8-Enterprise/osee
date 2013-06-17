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

import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.VersionData;
import org.eclipse.osee.orcs.db.internal.loader.RelationalConstants;

/**
 * @author Roberto E. Escobar
 */
public class AttributeDataImpl extends OrcsObjectImpl implements AttributeData {

   private int artifactId = RelationalConstants.ART_ID_SENTINEL;
   private String value;
   private String uri;

   public AttributeDataImpl(VersionData version) {
      super(version);
   }

   public void setValue(String value) {
      this.value = value;
   }

   public void setUri(String uri) {
      this.uri = uri;
   }

   @Override
   public String getValue() {
      return value;
   }

   @Override
   public String getUri() {
      return uri;
   }

   @Override
   public int getArtifactId() {
      return artifactId;
   }

   @Override
   public void setArtifactId(int artifactId) {
      this.artifactId = artifactId;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + artifactId;
      result = prime * result + (Strings.isValid(value) ? value.hashCode() : uri.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (!super.equals(obj)) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      AttributeDataImpl other = (AttributeDataImpl) obj;
      if (artifactId != other.artifactId) {
         return false;
      }
      if (Strings.isValid(value)) {
         if (!value.equals(other.getValue())) {
            return false;
         }
      }
      if (Strings.isValid(uri)) {
         if (!uri.equals(other.getUri())) {
            return false;
         }
      }
      return true;
   }

   @Override
   public String toString() {
      return "AttributeData [artifactId=" + artifactId + //
      " " + super.toString() + ", " + //
      (Strings.isValid(value) ? "value=" + value : "uri=" + uri) + "]";
   }
}