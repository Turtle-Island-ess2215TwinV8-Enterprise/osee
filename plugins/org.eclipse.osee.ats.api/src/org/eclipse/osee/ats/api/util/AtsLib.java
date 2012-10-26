/*
 * Created on Aug 1, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.Identifiable;

public class AtsLib {

   public final static double DEFAULT_HOURS_PER_WORK_DAY = 8;
   public final static String normalColor = "#FFFFFF";
   public final static String activeColor = "#EEEEEE";

   public static boolean isInTest() {
      return Boolean.valueOf(System.getProperty("osee.isInTest"));
   }

   public static String doubleToI18nString(double d) {
      return doubleToI18nString(d, false);
   }

   public static String doubleToI18nString(double d, boolean blankIfZero) {
      if (blankIfZero && d == 0) {
         return "";
      }
      // This enables java to use same string for all 0 cases instead of creating new one
      else if (d == 0) {
         return "0.00";
      } else {
         return String.format("%4.2f", d);
      }
   }

   public static List<String> toGuids(Collection<? extends Identifiable> atsObjects) {
      List<String> guids = new ArrayList<String>(atsObjects.size());
      for (Identifiable atsObject : atsObjects) {
         guids.add(atsObject.getGuid());
      }
      return guids;
   }

   /**
    * getName() all atsObjects, else toString()
    */
   public static String toString(String separator, Collection<? extends Object> objects) {
      StringBuilder sb = new StringBuilder();
      for (Object obj : objects) {
         if (obj instanceof Identifiable) {
            sb.append(((Identifiable) obj).getName());
         } else {
            sb.append(obj.toString());
         }
         sb.append(separator);
      }
      if (sb.length() > separator.length()) {
         return sb.substring(0, sb.length() - separator.length());
      }
      return "";
   }

   public static Collection<String> getNames(Collection<? extends Identifiable> atsObjects) {
      ArrayList<String> names = new ArrayList<String>();
      for (Identifiable namedAtsObject : atsObjects) {
         names.add(namedAtsObject.getName());
      }
      return names;
   }

}
