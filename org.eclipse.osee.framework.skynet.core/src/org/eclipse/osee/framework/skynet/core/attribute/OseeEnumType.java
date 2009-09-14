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
package org.eclipse.osee.framework.skynet.core.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.BaseOseeType;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.types.OseeEnumTypeCache;

/**
 * @author Roberto E. Escobar
 */
public class OseeEnumType extends BaseOseeType implements Comparable<OseeEnumType> {

   private final OseeEnumTypeCache cache;
   private boolean areEntriesDirty;

   public OseeEnumType(OseeEnumTypeCache cache, String guid, String enumTypeName) {
      super(guid, enumTypeName);
      this.cache = cache;
   }

   @Override
   public String toString() {
      List<String> data = new ArrayList<String>();
      try {
         for (OseeEnumEntry entry : values()) {
            data.add(entry.toString());
         }
      } catch (OseeCoreException ex) {
         data.add("Error");
      }
      return String.format("[%s] - %s", getName(), data);
   }

   public OseeEnumEntry[] values() throws OseeCoreException {
      List<OseeEnumEntry> entries = cache.getEnumEntries(this);
      Collections.sort(entries);
      return entries.toArray(new OseeEnumEntry[entries.size()]);
   }

   public Set<String> valuesAsOrderedStringSet() throws OseeCoreException {
      Set<String> values = new LinkedHashSet<String>();
      for (OseeEnumEntry oseeEnumEntry : values()) {
         values.add(oseeEnumEntry.getName());
      }
      return values;
   }

   public OseeEnumEntry valueOf(int ordinal) throws OseeCoreException {
      OseeEnumEntry toReturn = null;
      for (OseeEnumEntry oseeEnumEntry : values()) {
         if (oseeEnumEntry.ordinal() == ordinal) {
            toReturn = oseeEnumEntry;
         }
      }
      if (toReturn == null) {
         throw new OseeArgumentException(String.format("No enum const [%s] - ordinal [%s]", getName(), ordinal));
      }
      return toReturn;
   }

   public OseeEnumEntry valueOf(String entryName) throws OseeCoreException {
      OseeEnumEntry toReturn = null;
      for (OseeEnumEntry oseeEnumEntry : values()) {
         if (oseeEnumEntry.getName().equals(entryName)) {
            toReturn = oseeEnumEntry;
         }
      }
      if (toReturn == null) {
         throw new OseeArgumentException(String.format("No enum const [%s].[%s]", getName(), entryName));
      }
      return toReturn;
   }

   public void setEntries(Collection<OseeEnumEntry> entries) throws OseeCoreException {
      List<OseeEnumEntry> oldEntries = cache.getEnumEntries(this);
      cache.cacheEnumEntries(this, entries);
      List<OseeEnumEntry> newEntries = cache.getEnumEntries(this);
      areEntriesDirty |= isDifferent(oldEntries, newEntries);
   }

   void internalUpdateDirtyEntries(boolean areEntriesDirty) {
      this.areEntriesDirty |= areEntriesDirty;
   }

   public boolean areEntriesDirty() {
      return areEntriesDirty;
   }

   public boolean isDataDirty() {
      return super.isDirty();
   }

   @Override
   public boolean isDirty() {
      return isDataDirty() || areEntriesDirty();
   }

   @Override
   public int compareTo(OseeEnumType other) {
      int result = -1;
      if (other != null && other.getName() != null && getName() != null) {
         result = getName().compareTo(other.getName());
      }
      return result;
   }

   @Override
   public void clearDirty() {
      super.clearDirty();
      areEntriesDirty = false;
      try {
         for (OseeEnumEntry entry : values()) {
            entry.clearDirty();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

}