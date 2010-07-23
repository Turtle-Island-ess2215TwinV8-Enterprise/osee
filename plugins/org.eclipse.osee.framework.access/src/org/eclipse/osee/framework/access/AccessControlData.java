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
package org.eclipse.osee.framework.access;

import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * A data object in an access control list. Contains a subject (user artifact) and object (what the subject is trying to
 * access) and a permission level
 *
 * @author Jeff C. Phillips
 */
public class AccessControlData implements Comparable<AccessControlData> {

   private boolean dirty = false;
   private boolean birth = false;
   private final Artifact subject;
   private final AccessObject object;
   private PermissionEnum permission;
   private PermissionEnum branchPermission = null;
   private PermissionEnum artifactTypePermission = null;
   private PermissionEnum artifactPermission = null;

   public AccessControlData(Artifact subject, AccessObject object, PermissionEnum permission, boolean birth) {
      this(subject, object, permission, birth, true);
   }

   public AccessControlData(Artifact subject, AccessObject object, PermissionEnum permission, boolean birth, boolean dirty) {
      super();
      this.subject = subject;
      this.permission = permission;
      this.dirty = dirty;
      this.birth = birth;
      this.object = object;
   }

   /**
    * @return Returns the user.
    */
   public Artifact getSubject() {
      return subject;
   }

   /**
    * @param permission The permissionLevel to set.
    */
   public void setPermission(PermissionEnum permission) {

      if (this.permission == permission) {
         return;
      }

      this.permission = permission;
      dirty = true;
   }

   public PermissionEnum getBranchPermission() {
      return branchPermission;
   }

   public PermissionEnum getArtifactPermission() {
      return artifactPermission;
   }

   public PermissionEnum getArtifactTypePermission() {
      return artifactTypePermission;
   }

   /**
    * @return PermissionEnum
    */
   public PermissionEnum getPermission() {
      return permission;
   }

   /**
    * @return Returns the dirty.
    */
   public boolean isDirty() {
      return dirty;
   }

   public void setNotDirty() {
      this.dirty = false;
   }

   /**
    * @return Returns the object.
    */
   public AccessObject getObject() {
      return object;
   }

   public boolean isBirth() {
      return birth;
   }

   public int compareTo(AccessControlData data) {
      return subject.getName().compareTo(data.subject.getName());
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof AccessControlData)) {
         return false;
      }
      return subject.getName().equals(((AccessControlData) obj).subject.getName());
   }

   @Override
   public int hashCode() {
      return subject.getName().hashCode();
   }

   /**
    * @param branchPermission the branchPermission to set
    */
   public void setBranchPermission(PermissionEnum branchPermission) {
      this.branchPermission = branchPermission;
   }

   /**
    * @param artifactTypePermission the artifactTypePermission to set
    */
   public void setArtifactTypePermission(PermissionEnum artifactTypePermission) {
      this.artifactTypePermission = artifactTypePermission;
   }

   /**
    * @param artifactPermission the artifactPermission to set
    */
   public void setArtifactPermission(PermissionEnum artifactPermission) {
      this.artifactPermission = artifactPermission;
   }
}