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

package org.eclipse.osee.framework.jdk.core.util;

/**
 * @author Jeff C. Phillips
 * @author Don Dunne
 * @author Karol M. Wilk
 */
public class Strings {
   private final static String EMPTY_STRING = "";

   private Strings() {
      // Utility class
   }

   public static boolean isValid(String value) {
      return value != null && value.length() > 0;
   }

   public static String emptyString() {
      return EMPTY_STRING;
   }

   /**
    * This method adjusts '&'-containing strings to break the keyboard shortcut ("Accelerator") feature some widgets
    * offer, where &Test will make Alt+T a shortcut. This method breaks the accelerator by escaping ampersands.
    * 
    * @return a string with doubled ampersands.
    */
   public static String escapeAmpersands(String stringWithAmp) {
      if (isValid(stringWithAmp)) {
         return stringWithAmp.replace("&", "&&");
      } else {
         return null;
      }
   }

   public static String intern(String str) {
      if (str == null) {
         return null;
      }
      return str.intern();
   }

   /**
    * Will truncate string if necessary and add "..." to end if addDots and truncated
    */
   public static String truncate(String value, int length, boolean addDots) {
      if (value == null) {
         return emptyString();
      }
      String toReturn = value;
      if (Strings.isValid(value) && value.length() > length) {
         int len = addDots && length - 3 > 0 ? length - 3 : length;
         toReturn = value.substring(0, Math.min(length, len)) + (addDots ? "..." : emptyString());
      }
      return toReturn;
   }

   public static String truncate(String value, int length) {
      return truncate(value, length, false);
   }

   public static String unquote(String nameReference) {
      String toReturn = nameReference;
      if (Strings.isValid(nameReference) && nameReference.contains("\"")) {
         toReturn = nameReference.replaceAll("\\\"", emptyString());
      }
      return toReturn;
   }

   public static String quote(String nameReference) {
      String toReturn = nameReference;
      if (Strings.isValid(nameReference)) {
         toReturn = String.format("\"%s\"", nameReference);
      }
      return toReturn;
   }

}
