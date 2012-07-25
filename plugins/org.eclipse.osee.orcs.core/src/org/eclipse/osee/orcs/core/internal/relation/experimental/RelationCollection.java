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
package org.eclipse.osee.orcs.core.internal.relation.experimental;

import java.util.List;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.data.ResultSetList;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.orcs.core.internal.util.AbstractTypeCollection;
import org.eclipse.osee.orcs.core.internal.util.DataMatcher;
import org.eclipse.osee.orcs.core.internal.util.DeletedMatcher;
import org.eclipse.osee.orcs.core.internal.util.DirtyMatcher;
import org.eclipse.osee.orcs.core.internal.util.DirtyMatcher.DirtyFlag;

/**
 * @author Roberto E. Escobar
 */
public class RelationCollection extends AbstractTypeCollection<RelationType, Relation, IRelationTypeSide, Relation> {

   //@formatter:off
   private static final DataMatcher<Relation> DIRTY_MATCHER = new DirtyMatcher<Relation>(DirtyFlag.DIRTY);
   private static final DataMatcher<Relation> INCLUDE_DELETED = new DeletedMatcher<Relation>(DeletionFlag.INCLUDE_DELETED);
   private static final DataMatcher<Relation> EXCLUDE_DELETED = new DeletedMatcher<Relation>(DeletionFlag.EXCLUDE_DELETED);
   //@formatter:on

   //////////////////////////////////////////////////////////////

   public List<Relation> getList(IRelationTypeSide type, DeletionFlag includeDeleted) throws OseeCoreException {
      return getListByFilter(type, getDeletedFilter(includeDeleted));
   }

   //////////////////////////////////////////////////////////////

   @Override
   protected DataMatcher<Relation> getDeletedFilter(DeletionFlag includeDeleted) {
      return DeletionFlag.INCLUDE_DELETED == includeDeleted ? INCLUDE_DELETED : EXCLUDE_DELETED;
   }

   @Override
   protected DataMatcher<Relation> getDirtyMatcher() {
      return DIRTY_MATCHER;
   }

   @Override
   protected RelationType getType(Relation data) throws OseeCoreException {
      return data.getRelationType();
   }

   @Override
   protected ResultSet<Relation> createResultSet(List<Relation> values) {
      return new ResultSetList<Relation>(values);
   }

   @Override
   protected <T extends Relation> ResultSet<T> createResultSet(IRelationTypeSide attributeType, List<T> values) {
      return new ResultSetList<T>(values);
   }

   @Override
   protected Relation asMatcherData(Relation data) {
      return data;
   }
}
