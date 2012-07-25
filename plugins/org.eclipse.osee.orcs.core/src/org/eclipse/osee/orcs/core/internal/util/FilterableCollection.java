/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * @author Roberto E. Escobar
 */
public abstract class FilterableCollection<KEY, TYPE, DATA> {

   private final Multimap<KEY, DATA> map = Multimaps.synchronizedMultimap(LinkedHashMultimap.<KEY, DATA> create());

   protected abstract ResultSet<DATA> createResultSet(List<DATA> values);

   protected abstract <T> ResultSet<DATA> createResultSet(KEY type, List<DATA> values);

   protected abstract DataMatcher<DATA> getDeletedFilter(DeletionFlag includeDeleted);

   protected abstract DataMatcher<DATA> getDirtyMatcher();

   protected abstract TYPE getType(DATA data);

   //////////////////////////////////////////////////////////////
   public void add(KEY type, DATA data) {
      map.put(type, data);
   }

   public void remove(KEY type, DATA data) {
      map.remove(type, data);
   }

   public Collection<DATA> getAll() {
      return map.values();
   }

   public Collection<TYPE> getExistingTypes(DeletionFlag includeDeleted) throws OseeCoreException {
      List<TYPE> toReturn = new ArrayList<TYPE>();
      for (DATA data : getList(includeDeleted)) {
         toReturn.add(getType(data));
      }
      return toReturn;
   }

   public List<DATA> getDirties() throws OseeCoreException {
      return getListByFilter(map.values(), getDirtyMatcher());
   }

   public boolean hasDirty() {
      return hasItemMatchingFilter(map.values(), getDirtyMatcher());
   }

   //////////////////////////////////////////////////////////////

   public <T> ResultSet<DATA> getResultSetBy(KEY type, DeletionFlag includeDeleted) throws OseeCoreException {
      return getResultSetByFilter(type, getDeletedFilter(includeDeleted));
   }

   public <T> List<DATA> getList(KEY type, DeletionFlag includeDeleted) throws OseeCoreException {
      return getListByFilter(type, getDeletedFilter(includeDeleted));
   }

   private <T> List<DATA> getListByFilter(KEY type, DataMatcher<DATA> matcher) throws OseeCoreException {
      return getListByFilter(map.get(type), matcher);
   }

   private <T> ResultSet<DATA> getResultSetByFilter(KEY type, DataMatcher<DATA> matcher) throws OseeCoreException {
      return getResultSetByFilter(map.get(type), matcher);
   }

   //////////////////////////////////////////////////////////////

   public ResultSet<DATA> getResultSetBy(DeletionFlag includeDeleted) throws OseeCoreException {
      return getResultSetByFilter(getDeletedFilter(includeDeleted));
   }

   public List<DATA> getList(DeletionFlag includeDeleted) throws OseeCoreException {
      return getListByFilter(getDeletedFilter(includeDeleted));
   }

   private List<DATA> getListByFilter(DataMatcher<DATA> matcher) throws OseeCoreException {
      return getListByFilter(map.values(), matcher);
   }

   private ResultSet<DATA> getResultSetByFilter(DataMatcher<DATA> matcher) throws OseeCoreException {
      return getResultSetByFilter(map.values(), matcher);
   }

   //////////////////////////////////////////////////////////////

   protected ResultSet<DATA> getResultSetByFilter(Collection<DATA> source, DataMatcher<DATA> matcher) throws OseeCoreException {
      List<DATA> values = getListByFilter(source, matcher);
      return createResultSet(values);
   }

   //////////////////////////////////////////////////////////////

   protected List<DATA> getListByFilter(Collection<DATA> source, DataMatcher<DATA> matcher) throws OseeCoreException {
      List<DATA> toReturn;
      if (source != null && !source.isEmpty()) {
         toReturn = new LinkedList<DATA>();
         for (DATA data : source) {
            if (matcher.accept(data)) {
               toReturn.add(data);
            }
         }
      } else {
         toReturn = Collections.emptyList();
      }
      return toReturn;
   }

   private boolean hasItemMatchingFilter(Collection<DATA> source, DataMatcher<DATA> matcher) {
      boolean result = false;
      if (source != null && !source.isEmpty()) {
         for (DATA data : source) {
            try {
               if (matcher.accept(data)) {
                  result = true;
                  break;
               }
            } catch (OseeCoreException ex) {
               // do nothing
            }
         }
      }
      return result;
   }
}
