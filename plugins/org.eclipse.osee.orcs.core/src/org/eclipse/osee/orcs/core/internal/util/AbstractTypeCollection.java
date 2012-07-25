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
import java.util.List;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractTypeCollection<TYPE, MATCH_DATA, KEY, DATA> extends SearchableCollection<MATCH_DATA, KEY, DATA> {

   protected abstract DataMatcher<MATCH_DATA> getDeletedFilter(DeletionFlag includeDeleted);

   protected abstract DataMatcher<MATCH_DATA> getDirtyMatcher();

   protected abstract TYPE getType(DATA data) throws OseeCoreException;

   public Collection<TYPE> getExistingTypes(DeletionFlag includeDeleted) throws OseeCoreException {
      List<TYPE> toReturn = new ArrayList<TYPE>();
      for (DATA data : getList(includeDeleted)) {
         if (isValid(data)) {
            toReturn.add(getType(data));
         }
      }
      return toReturn;
   }

   public List<DATA> getDirties() throws OseeCoreException {
      return getListByFilter(getDirtyMatcher());
   }

   public boolean hasDirty() {
      return hasItemMatchingFilter(getDirtyMatcher());
   }

   public List<DATA> getList(DeletionFlag includeDeleted) throws OseeCoreException {
      return getListByFilter(getDeletedFilter(includeDeleted));
   }

   public ResultSet<DATA> getResultSet(DeletionFlag includeDeleted) throws OseeCoreException {
      return getResultSetByFilter(getDeletedFilter(includeDeleted));
   }
}
