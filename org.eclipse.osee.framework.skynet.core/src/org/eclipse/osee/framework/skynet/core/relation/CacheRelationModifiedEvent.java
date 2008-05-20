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
package org.eclipse.osee.framework.skynet.core.relation;

import org.eclipse.osee.framework.skynet.core.artifact.Branch;

/**
 * @author Jeff C. Phillips
 */
public class CacheRelationModifiedEvent extends RelationModifiedEvent {

   /**
    * @param branch TODO
    * @param relationType
    * @param relationSide
    * @param modType
    * @param sender
    */
   public CacheRelationModifiedEvent(RelationLink link, Branch branch, String relationType, String relationSide, ModType modType, Object sender) {
      super(link, branch, relationType, relationSide, modType, sender);
   }

   /**
    * @param relationType
    * @param relationSide
    * @param modType
    * @param sender
    * @param branch TODO
    */
   public CacheRelationModifiedEvent(RelationLink link, String relationType, String relationSide, String modType, Object sender, Branch branch) {
      super(link, branch, relationType, relationSide, modType, sender);

   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof CacheRelationModifiedEvent) {
         return (getLink().getRelationId() == (((CacheRelationModifiedEvent) obj).getLink().getRelationId()));
      }
      return super.equals(obj);
   }

   @Override
   public int hashCode() {
      return getLink().getRelationId();
   }
}
