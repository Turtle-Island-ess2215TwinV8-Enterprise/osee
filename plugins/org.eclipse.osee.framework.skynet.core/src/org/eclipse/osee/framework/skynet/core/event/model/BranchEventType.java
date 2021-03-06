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
package org.eclipse.osee.framework.skynet.core.event.model;

/**
 * @author Donald G. Dunne
 */
public enum BranchEventType {

   // Local and Remote events
   Purging(EventType.LocalAndRemote, "ATPHeMoAFyL543vrAyQA", false),
   Purged(EventType.LocalAndRemote, "AAn_QG7jRGZAqPE0UewA", true),
   Deleting(EventType.LocalAndRemote, "ATPHeNujxAkPZEkWUtQA", false),
   Deleted(EventType.LocalAndRemote, "AAn_QHBDvwtT5jjKaHgA", true),
   Added(EventType.LocalAndRemote, "AAn_QHDohywDoSTxwcQA", true),
   Renamed(EventType.LocalAndRemote, "AAn_QHGLIUsH2BdX2gwA", true),
   Committing(EventType.LocalAndRemote, "ATPHeN1du2GAbS3SQsAA", false),
   CommitFailed(EventType.LocalAndRemote, "ATPHeN3RaBnDmpoYXkQA", false),
   Committed(EventType.LocalAndRemote, "AAn_QHIu0mGZytQ11QwA", true),
   TypeUpdated(EventType.LocalAndRemote, "AAn_QHLW4DKKbUkEZggA", true),
   StateUpdated(EventType.LocalAndRemote, "AAn_QHQdKhxNLtWPchAA", true),
   ArchiveStateUpdated(EventType.LocalAndRemote, "AAn_QHS7Zhr6OLhKl3gA", true),
   MergeConflictResolved(EventType.LocalAndRemote, "AAn_QHiJ53W5W_k8W7AA", false),
   FavoritesUpdated(EventType.LocalOnly, "AFRkIheIUn3Jpz4kNBgA", false);

   private final EventType eventType;
   private final String guid;
   private final boolean justifiesCacheRefresh;

   public boolean isRemoteEventType() {
      return eventType == EventType.LocalAndRemote || eventType == EventType.RemoteOnly;
   }

   public boolean isLocalEventType() {
      return eventType == EventType.LocalAndRemote || eventType == EventType.LocalOnly;
   }

   private BranchEventType(EventType eventType, String guid, boolean justifiesCacheRefresh) {
      this.eventType = eventType;
      this.guid = guid;
      this.justifiesCacheRefresh = justifiesCacheRefresh;
   }

   public String getGuid() {
      return guid;
   }

   public static BranchEventType getByGuid(String guid) {
      for (BranchEventType type : values()) {
         if (type.guid.equals(guid)) {
            return type;
         }
      }
      return null;
   }

   public boolean justifiesCacheRefresh() {
      return justifiesCacheRefresh;
   }

}
