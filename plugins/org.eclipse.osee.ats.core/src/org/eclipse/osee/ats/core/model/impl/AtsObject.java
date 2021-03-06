/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.model.impl;

import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.framework.core.data.NamedIdentity;
import org.eclipse.osee.framework.jdk.core.util.HumanReadableId;

/**
 * @author Donald G. Dunne
 */
public class AtsObject extends NamedIdentity<String> implements IAtsObject {

   private String humanReadableId;
   private String desc;

   public AtsObject(String name, String guid, String hrid) {
      super(guid, name);
      this.humanReadableId = hrid;
   }

   public AtsObject(String name) {
      this(name, org.eclipse.osee.framework.jdk.core.util.GUID.create(), HumanReadableId.generate());
   }

   @Override
   public String getDescription() {
      return desc;
   }

   @Override
   public String getHumanReadableId() {
      return humanReadableId;
   }

   public void setDescription(String desc) {
      this.desc = desc;
   }

   public void setHumanReadableId(String hrid) {
      this.humanReadableId = hrid;
   }

   @Override
   public String toString() {
      return getName();
   }

   public final String toStringWithId() {
      return String.format("[%s][%s]", getHumanReadableId(), getName());
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((getGuid() == null) ? 0 : getGuid().hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      AtsObject other = (AtsObject) obj;
      if (getGuid() == null) {
         if (other.getGuid() != null) {
            return false;
         } else {
            return false;
         }
      } else if (!getGuid().equals(other.getGuid())) {
         return false;
      }
      return true;
   }

}
