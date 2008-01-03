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
package org.eclipse.osee.framework.messaging.event.skynet.event;

import java.util.Collection;

/**
 * @author Robert A. Fisher
 */
public class RemoteArtifactModifiedEvent extends SkynetArtifactEventBase {
   private static final long serialVersionUID = -4325821466558180270L;
   private Collection<SkynetAttributeChange> attributeValues;

   /**
    * @param branchId
    * @param transactionId
    * @param artId
    * @param artTypeId
    * @param author TODO
    */
   public RemoteArtifactModifiedEvent(int branchId, int transactionId, int artId, int artTypeId, String factoryName, Collection<SkynetAttributeChange> attributeValues, int author) {
      super(branchId, transactionId, artId, artTypeId, factoryName, author);

      this.attributeValues = attributeValues;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof RemoteArtifactModifiedEvent) {
         return (getArtId() == (((RemoteArtifactModifiedEvent) obj).getArtId()));
      }
      return super.equals(obj);
   }

   @Override
   public int hashCode() {
      return getArtId();
   }

   /**
    * @return Returns the serialVersionUID.
    */
   public static long getSerialVersionUID() {
      return serialVersionUID;
   }

   /**
    * @return Returns the attributeValues.
    */
   public Collection<SkynetAttributeChange> getAttributeChanges() {
      return attributeValues;
   }
}
