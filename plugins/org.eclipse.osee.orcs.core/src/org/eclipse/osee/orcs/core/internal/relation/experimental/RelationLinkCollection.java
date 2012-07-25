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
public class RelationLinkCollection extends AbstractTypeCollection<RelationType, RelationLink, IRelationTypeSide, RelationLink> {

   //@formatter:off
   private static final DataMatcher<RelationLink> DIRTY_MATCHER = new DirtyMatcher<RelationLink>(DirtyFlag.DIRTY);
   private static final DataMatcher<RelationLink> INCLUDE_DELETED = new DeletedMatcher<RelationLink>(DeletionFlag.INCLUDE_DELETED);
   private static final DataMatcher<RelationLink> EXCLUDE_DELETED = new DeletedMatcher<RelationLink>(DeletionFlag.EXCLUDE_DELETED);
   //@formatter:on

   @Override
   protected DataMatcher<RelationLink> getDeletedFilter(DeletionFlag includeDeleted) {
      return DeletionFlag.INCLUDE_DELETED == includeDeleted ? INCLUDE_DELETED : EXCLUDE_DELETED;
   }

   @Override
   protected DataMatcher<RelationLink> getDirtyMatcher() {
      return DIRTY_MATCHER;
   }

   @Override
   protected RelationType getType(RelationLink data) throws OseeCoreException {
      return data.getRelationType();
   }

   @Override
   protected ResultSet<RelationLink> createResultSet(List<RelationLink> values) {
      return new ResultSetList<RelationLink>(values);
   }

   @Override
   protected <T extends RelationLink> ResultSet<T> createResultSet(IRelationTypeSide attributeType, List<T> values) {
      return new ResultSetList<T>(values);
   }

   @Override
   protected RelationLink asMatcherData(RelationLink data) {
      return data;
   }
}
