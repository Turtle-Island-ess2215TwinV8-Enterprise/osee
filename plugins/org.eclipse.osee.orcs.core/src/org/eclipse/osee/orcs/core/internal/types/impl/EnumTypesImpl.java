/*
 * Created on Jun 17, 2013
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.orcs.core.internal.types.impl;

import java.util.Collection;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.core.internal.types.OrcsTypesIndexProvider;
import org.eclipse.osee.orcs.data.EnumType;
import org.eclipse.osee.orcs.data.EnumTypes;

public class EnumTypesImpl implements EnumTypes {

   private final OrcsTypesIndexProvider indexProvider;

   public EnumTypesImpl(OrcsTypesIndexProvider indexProvider) {
      this.indexProvider = indexProvider;
   }

   @Override
   public Collection<? extends EnumType> getAll() throws OseeCoreException {
      return getIndex().getAllTokens();
   }

   @Override
   public EnumType getByUuid(Long typeId) throws OseeCoreException {
      return getIndex().getTokenByUuid(typeId);
   }

   @Override
   public boolean exists(EnumType item) throws OseeCoreException {
      return getIndex().existsByUuid(item.getGuid());
   }

   @Override
   public boolean isEmpty() throws OseeCoreException {
      return getAll().isEmpty();
   }

   @Override
   public int size() throws OseeCoreException {
      return getAll().size();
   }

   private EnumTypeIndex getIndex() throws OseeCoreException {
      return indexProvider.getEnumTypeIndex();
   }

}
