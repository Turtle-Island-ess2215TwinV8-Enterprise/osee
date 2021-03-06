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
package org.eclipse.osee.framework.core.model.change;

import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.util.Conditions;

/**
 * @author Roberto E. Escobar
 */
public final class ChangeItemUtil {

   private ChangeItemUtil() {
      // Utility Class
   }

   public static ChangeVersion getStartingVersion(ChangeItem item) throws OseeCoreException {
      if (item == null) {
         throw new OseeArgumentException("ChangeItem cannot be null");
      }
      ChangeVersion toReturn = item.getBaselineVersion();
      if (!toReturn.isValid()) {
         toReturn = item.getFirstNonCurrentChange();
         if (!toReturn.isValid()) {
            toReturn = item.getCurrentVersion();
            if (!toReturn.isValid()) {
               throw new OseeStateException("Cannot find a valid starting point for change item: %s", item);
            }
         }
      }
      return toReturn;
   }

   public static void copy(ChangeVersion source, ChangeVersion dest) throws OseeCoreException {
      Conditions.checkNotNull(source, "source");
      Conditions.checkNotNull(dest, "Dest");

      dest.setGammaId(source.getGammaId());
      dest.setModType(source.getModType());
      dest.setValue(source.getValue());
   }

   public static boolean isModType(ChangeVersion changeVersion, ModificationType matchModType) {
      return changeVersion != null && changeVersion.getModType() == matchModType;
   }

   public static boolean isNew(ChangeVersion changeVersion) {
      return isModType(changeVersion, ModificationType.NEW);
   }

   public static boolean isIntroduced(ChangeVersion changeVersion) {
      return isModType(changeVersion, ModificationType.INTRODUCED);
   }

   public static boolean isDeleted(ChangeVersion changeVersion) {
      return changeVersion != null && changeVersion.getModType() != null && changeVersion.getModType().isDeleted();
   }

   public static boolean wasNewOnSource(ChangeItem changeItem) {
      return isNew(changeItem.getFirstNonCurrentChange()) || isNew(changeItem.getCurrentVersion());
   }

   public static boolean wasIntroducedOnSource(ChangeItem changeItem) {
      return isIntroduced(changeItem.getFirstNonCurrentChange()) || isIntroduced(changeItem.getCurrentVersion());
   }

   public static boolean hasBeenReplacedWithVersion(ChangeItem changeItem) {
      boolean results = areGammasEqual(changeItem.getCurrentVersion(), changeItem.getBaselineVersion()) && //
      isModType(changeItem.getCurrentVersion(), ModificationType.MODIFIED);
      return results;
   }

   public static boolean isAlreadyOnDestination(ChangeItem changeItem) {
      return areGammasEqual(changeItem.getCurrentVersion(), changeItem.getDestinationVersion()) && //
      areModTypesEqual(changeItem.getCurrentVersion(), changeItem.getDestinationVersion());
   }

   public static boolean areModTypesEqual(ChangeVersion object1, ChangeVersion object2) {
      boolean result = false;
      if (object1 == null && object2 == null) {
         result = true;
      } else if (object1 != null && object2 != null) {
         if (object1.getModType() == object2.getModType()) {
            result = true;
         } else if (object1.getModType() != null) {
            result = object1.getModType().equals(object2.getModType());
         }
      }
      return result;
   }

   public static boolean areGammasEqual(ChangeVersion object1, ChangeVersion object2) {
      boolean result = false;
      if (object1 == null && object2 == null) {
         result = true;
      } else if (object1 != null && object2 != null) {
         if (object1.getGammaId() == object2.getGammaId()) {
            result = true;
         } else if (object1.getGammaId() != null) {
            result = object1.getGammaId().equals(object2.getGammaId());
         }
      }
      return result;
   }

   public static boolean isIgnoreCase(ChangeItem changeItem) {
      return //
      wasCreatedAndDeleted(changeItem) || //
      isAlreadyOnDestination(changeItem) || //
      isDeletedAndDoesNotExistInDestination(changeItem) || //
      hasBeenDeletedInDestination(changeItem) || //
      hasBeenReplacedWithVersion(changeItem);
   }

   public static boolean wasCreatedAndDeleted(ChangeItem changeItem) {
      return !changeItem.getBaselineVersion().isValid() && isDeleted(changeItem.getCurrentVersion());
   }

   public static boolean isDeletedAndDoesNotExistInDestination(ChangeItem changeItem) {
      return !changeItem.getDestinationVersion().isValid() && isDeleted(changeItem.getCurrentVersion());
   }

   public static boolean hasBeenDeletedInDestination(ChangeItem changeItem) {
      return changeItem.getDestinationVersion().isValid() && isDeleted(changeItem.getDestinationVersion());
   }
}
