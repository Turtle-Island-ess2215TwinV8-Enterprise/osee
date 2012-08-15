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
package org.eclipse.osee.orcs.core.internal.relation.sorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.framework.core.data.IRelationSorterId;
import org.eclipse.osee.framework.core.data.Named;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.orcs.utility.SortOrder;

/**
 * @author Andrew M. Finkbeiner
 * @author Ryan Schmitt
 */
public class SorterProvider {

   private final Map<String, Sorter> orderMap = new HashMap<String, Sorter>();
   private final List<IRelationSorterId> ids = new ArrayList<IRelationSorterId>();

   public SorterProvider() {
      registerOrderType(new LexicographicalSorter(SortOrder.ASCENDING));
      registerOrderType(new LexicographicalSorter(SortOrder.DESCENDING));
      registerOrderType(new UnorderedSorter());
      registerOrderType(new UserDefinedSorter());

      Collection<Sorter> sorters = orderMap.values();
      for (Sorter sorter : sorters) {
         ids.add(sorter.getId());
      }
      Collections.sort(ids, new CaseInsensitiveNameComparator());
   }

   private void registerOrderType(Sorter order) {
      orderMap.put(order.getId().getGuid(), order);
   }

   public boolean exists(String orderGuid) throws OseeCoreException {
      Conditions.checkExpressionFailOnTrue(!GUID.isValid(orderGuid), "Error invalid id argument [%s]", orderGuid);
      return orderMap.containsKey(orderGuid);
   }

   public Sorter getSorter(String orderGuid) throws OseeCoreException {
      Conditions.checkExpressionFailOnTrue(!GUID.isValid(orderGuid), "Error invalid id argument [%s]", orderGuid);
      Sorter order = orderMap.get(orderGuid);
      Conditions.checkNotNull(order, "sorter", "Unable to locate sorter with id[%s]", orderGuid);
      return order;
   }

   public List<IRelationSorterId> getSorterIds() {
      return ids;
   }

   private static final class CaseInsensitiveNameComparator implements Comparator<Named> {
      @Override
      public int compare(Named o1, Named o2) {
         return o1.getName().compareToIgnoreCase(o2.getName());
      }
   }
}
