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
package org.eclipse.osee.framework.ui.data.model.editor.model;

import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Roberto E. Escobar
 */
public class DataType extends NodeModel {
   private static final String DEFAULT_NAMESPACE = "default";
   protected static final String EMPTY_STRING = "";
   private String name;
   private String uniqueId;

   public DataType() {
      this(EMPTY_STRING, EMPTY_STRING);
   }

   public DataType(String name) {
      this(EMPTY_STRING, name);
   }

   public DataType(String typeId, String name) {
      super();
      this.uniqueId = typeId;
      this.name = name;
   }

   /**
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(String name) {
      if (this.name != name) {
         this.name = Strings.isValid(name) ? name.trim() : name;
         fireModelEvent();
      }
   }

   /**
    * @return the typeId
    */
   public String getUniqueId() {
      return uniqueId;
   }

   /**
    * @param typeId the typeId to set
    */
   public void setUniqueId(String typeId) {
      this.uniqueId = typeId;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof DataType) {
         DataType other = (DataType) obj;
         return objectEquals(getName(), other.getName());
      }
      return false;
   }

   private boolean objectEquals(Object object1, Object object2) {
      if (object1 == object2) {
         return true;
      } else if (object1 != null && object2 != null) {
         return object1.equals(object2);
      }
      return false;
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

   @Override
   public String toString() {
      return getName();
   }
}
