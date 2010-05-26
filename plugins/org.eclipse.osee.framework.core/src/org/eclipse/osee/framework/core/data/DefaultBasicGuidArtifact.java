/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.data;

/**
 * @author Donald G. Dunne
 */
public class DefaultBasicGuidArtifact implements Identity, IBasicGuidArtifact {

   private static final long serialVersionUID = -4997763989583925345L;
   private String branchGuid;
   private String artTypeGuid;
   private String artGuid;

   public DefaultBasicGuidArtifact() {
   }

   public DefaultBasicGuidArtifact(String branchGuid, String artTypeGuid, String guid) {
      this.branchGuid = branchGuid;
      this.artTypeGuid = artTypeGuid;
      this.artGuid = guid;
   }

   public String getBranchGuid() {
      return branchGuid;
   }

   public String getArtTypeGuid() {
      return artTypeGuid;
   }

   public String toString() {
      return String.format("[%s]", artGuid);
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = prime * ((artTypeGuid == null) ? 0 : artTypeGuid.hashCode());
      result = prime * result + ((branchGuid == null) ? 0 : branchGuid.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      DefaultBasicGuidArtifact other = (DefaultBasicGuidArtifact) obj;
      if (artTypeGuid == null) {
         if (other.artTypeGuid != null) return false;
      } else if (!artTypeGuid.equals(other.artTypeGuid)) return false;
      if (branchGuid == null) {
         if (other.branchGuid != null) return false;
      } else if (!branchGuid.equals(other.branchGuid)) return false;
      if (artGuid == null) {
         if (other.artGuid != null) return false;
      } else if (!artGuid.equals(other.artGuid)) return false;
      return true;
   }

   public String getGuid() {
      return artGuid;
   }

   public void setBranchGuid(String branchGuid) {
      this.branchGuid = branchGuid;
   }

   public void setArtTypeGuid(String artTypeGuid) {
      this.artTypeGuid = artTypeGuid;
   }

   public void setGuid(String guid) {
      this.artGuid = guid;
   }

}