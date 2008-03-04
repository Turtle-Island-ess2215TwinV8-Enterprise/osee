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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author David Diepenbrock
 */
public class Collections {

   public static Collection<String> fromString(String string, String seperator) {
      return Arrays.asList(string.split(seperator));
   }

   /**
    * An flexible alternative for converting a Collection to a String.
    * 
    * @param c The Collection to convert to a String
    * @param start The String to place at the beginning of the returned String
    * @param separator The String to place in between elements of the Collection c.
    * @param end The String to place at the end of the returned String
    * @return A String which starts with 'start', followed by the elements in the Collection c separated by 'separator',
    *         ending with 'end'.
    */
   @SuppressWarnings("unchecked")
   public static String toString(Collection c, String start, String separator, String end) {
      Iterator i = c.iterator();
      StringBuilder myString = new StringBuilder();

      if (start != null) myString.append(start);

      boolean first = true;
      while (i.hasNext()) {
         if (!first) myString.append(separator);
         myString.append(i.next().toString());
         first = false;
      }

      if (end != null) myString.append(end);

      return myString.toString();
   }

   public static String toString(String separator, Object... objects) {
      Collection<Object> objectsCol = new ArrayList<Object>(objects.length);
      for (Object obj : objects)
         objectsCol.add(obj);
      return toString(objectsCol, null, separator, null);
   }

   @SuppressWarnings("unchecked")
   public static String toString(String separator, Collection c) {
      return toString(c, null, separator, null);
   }

   /**
    * The resultant set is those elements in superSet which are not in the subSet
    * 
    * @param superSet
    * @param subList
    * @return Return complement list reference
    */
   public static <T> List<T> setComplement(Collection<T> superSet, Collection<T> subList) {
      ArrayList<T> complement = new ArrayList<T>(superSet.size());
      for (T obj : superSet) {
         if (!subList.contains(obj)) {
            complement.add(obj);
         }
      }
      return complement;
   }

   /**
    * @param listA
    * @param listB
    * @return The intersection of two sets A and B is the set of elements common to A and B
    */
   public static <T> ArrayList<T> setIntersection(Collection<T> listA, Collection<T> listB) {
      ArrayList<T> intersection = new ArrayList<T>(listA.size());

      for (T obj : listA) {
         if (listB.contains(obj)) {
            intersection.add(obj);
         }
      }
      return intersection;
   }

   @SuppressWarnings("unchecked")
   public static Set toSet(Collection collection) {
      Set set = null;
      if (collection instanceof Set) {
         set = (Set) collection;
      } else {
         set = new LinkedHashSet();
         set.addAll(collection);
      }
      return set;
   }

   public static List<Object> getAggregateTree(List<Object> items, int maxPerList) {
      if (items == null) throw new IllegalArgumentException("items can not be null");
      if (maxPerList < 2) throw new IllegalArgumentException("maxPerList can not be less than 2");

      if (items.size() > maxPerList) {
         return (recursiveAggregateTree(items, maxPerList));
      } else {
         return new ArrayList<Object>(items);
      }
   }

   private static ArrayList<Object> recursiveAggregateTree(List<Object> items, int maxPerList) {
      if (items.size() > maxPerList) {
         ArrayList<Object> aggregateList = new ArrayList<Object>(maxPerList);
         ArrayList<Object> childList = null;

         for (Object item : items) {
            if (childList == null || childList.size() == maxPerList) {
               childList = new ArrayList<Object>(maxPerList);
               aggregateList.add(childList);
            }
            childList.add(item);
         }
         childList.trimToSize();

         aggregateList = recursiveAggregateTree(aggregateList, maxPerList);

         aggregateList.trimToSize();

         return aggregateList;
      } else {
         // This is a safe blind cast since only subsequent calls of this method will end up here
         // and this method always uses ArrayList<Object>
         return (ArrayList<Object>) items;
      }
   }
}
