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

package org.eclipse.osee.framework.core.enums;

/**
 * Enumeration used to determine the level of artifact data that will be pre-loaded from the datastore
 * 
 * @author Ryan D. Brooks
 */
public enum LoadLevel {
   SHALLOW,
   FULL,
   RELATION,
   ATTRIBUTE,
   ALL_CURRENT;

   public boolean isShallow() {
      return this == SHALLOW;
   }

   public boolean isRelationsOnly() {
      return this == RELATION;
   }

   public boolean isHead() {
      return this == ALL_CURRENT;
   }

   public boolean isAttributesOnly() {
      return this == ATTRIBUTE;
   }
}
