/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.x.ats.core.internal.data;

import org.eclipse.osee.framework.core.data.FullyNamed;
import org.eclipse.osee.framework.core.data.HasDescription;
import org.eclipse.osee.framework.core.data.Identity;
import org.eclipse.osee.framework.core.data.Named;
import org.eclipse.osee.orcs.data.ReadableArtifact;

public abstract class AbstractAtsData implements Identity<String>, FullyNamed, HasDescription {

   private final ReadableArtifact proxiedObject;

   public AbstractAtsData(ReadableArtifact proxiedObject) {
      this.proxiedObject = proxiedObject;
   }

   @Override
   public String getName() {
      return proxiedObject.getName();
   }

   @Override
   public int compareTo(Named other) {
      if (other != null && other.getName() != null && getName() != null) {
         return getName().compareTo(other.getName());
      }
      return -1;
   }

   @Override
   public String getDescription() {
      return "";
   }

   @Override
   public String getUnqualifiedName() {
      return null;
   }

   @Override
   public String getGuid() {
      return proxiedObject.getGuid();
   }

   @Override
   public boolean matches(Identity<?>... identities) {
      return proxiedObject.matches(identities);
   }

   @Override
   public int hashCode() {
      return proxiedObject.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return proxiedObject.equals(obj);
   }

   public ReadableArtifact getProxiedObject() {
      return proxiedObject;
   }
}
