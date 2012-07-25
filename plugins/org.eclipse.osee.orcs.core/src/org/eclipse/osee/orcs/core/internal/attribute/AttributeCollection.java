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
package org.eclipse.osee.orcs.core.internal.attribute;

import java.util.List;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.orcs.core.internal.util.AbstractTypeCollection;
import org.eclipse.osee.orcs.core.internal.util.DataMatcher;
import org.eclipse.osee.orcs.core.internal.util.DeletedMatcher;
import org.eclipse.osee.orcs.core.internal.util.DirtyMatcher;
import org.eclipse.osee.orcs.core.internal.util.DirtyMatcher.DirtyFlag;

/**
 * @author Roberto E. Escobar
 */
public class AttributeCollection extends AbstractTypeCollection<AttributeType, Attribute<?>, IAttributeType, Attribute<?>> {

   //@formatter:off
   private static final DataMatcher<Attribute<?>> DIRTY_MATCHER = new DirtyMatcher<Attribute<?>>(DirtyFlag.DIRTY);
   private static final DataMatcher<Attribute<?>> INCLUDE_DELETED = new DeletedMatcher<Attribute<?>>(DeletionFlag.INCLUDE_DELETED);
   private static final DataMatcher<Attribute<?>> EXCLUDE_DELETED = new DeletedMatcher<Attribute<?>>(DeletionFlag.EXCLUDE_DELETED);
   //@formatter:on

   private final AttributeExceptionFactory exceptionFactory;

   public AttributeCollection(AttributeExceptionFactory exceptionFactory) {
      super();
      this.exceptionFactory = exceptionFactory;
   }

   //////////////////////////////////////////////////////////////
   public <T> List<Attribute<T>> getList(IAttributeType attributeType, DeletionFlag includeDeleted) throws OseeCoreException {
      return getListByFilter(attributeType, getDeletedFilter(includeDeleted));
   }

   public <T> ResultSet<Attribute<T>> getResultSet(IAttributeType attributeType, DeletionFlag includeDeleted) throws OseeCoreException {
      List<Attribute<T>> result = getList(attributeType, includeDeleted);
      return new AttributeResultSet<T>(exceptionFactory, attributeType, result);
   }

   public <T> ResultSet<Attribute<T>> getAttributeSetFromString(IAttributeType attributeType, DeletionFlag includeDeleted, String value) throws OseeCoreException {
      DataMatcher<Attribute<?>> filter = getDeletedFilter(includeDeleted);
      filter = filter.and(new AttributeFromStringFilter(value));
      return getSetByFilter(attributeType, filter);
   }

   public <T> ResultSet<Attribute<T>> getAttributeSetFromValue(IAttributeType attributeType, DeletionFlag includeDeleted, T value) throws OseeCoreException {
      DataMatcher<Attribute<?>> filter = getDeletedFilter(includeDeleted);
      filter = filter.and(new AttributeValueFilter<T>(value));
      return getSetByFilter(attributeType, filter);
   }

   //////////////////////////////////////////////////////////////

   @Override
   protected DataMatcher<Attribute<?>> getDeletedFilter(DeletionFlag includeDeleted) {
      return DeletionFlag.INCLUDE_DELETED == includeDeleted ? INCLUDE_DELETED : EXCLUDE_DELETED;
   }

   @Override
   protected DataMatcher<Attribute<?>> getDirtyMatcher() {
      return DIRTY_MATCHER;
   }

   @Override
   protected AttributeType getType(Attribute<?> data) {
      return data.getAttributeType();
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   @Override
   protected ResultSet<Attribute<?>> createResultSet(List<Attribute<?>> values) {
      return new AttributeResultSet(exceptionFactory, values);
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   @Override
   protected <T extends Attribute<?>> ResultSet<T> createResultSet(IAttributeType attributeType, List<T> values) {
      return new AttributeResultSet(exceptionFactory, attributeType, values);
   }

   @Override
   protected Attribute<?> asMatcherData(Attribute<?> data) {
      return data;
   }

}
