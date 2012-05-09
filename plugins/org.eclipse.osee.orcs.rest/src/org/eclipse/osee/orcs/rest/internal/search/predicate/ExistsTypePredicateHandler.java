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
package org.eclipse.osee.orcs.rest.internal.search.predicate;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.orcs.rest.internal.search.Predicate;
import org.eclipse.osee.orcs.rest.internal.search.PredicateHandler;
import org.eclipse.osee.orcs.rest.internal.search.dsl.SearchMethod;
import org.eclipse.osee.orcs.search.QueryBuilder;

/**
 * @author John Misinco
 * @author Roberto E. Escobar
 */
public class ExistsTypePredicateHandler implements PredicateHandler {

   @Override
   public QueryBuilder handle(QueryBuilder builder, Predicate predicate) throws OseeCoreException {
      if (predicate.getType() != SearchMethod.EXISTS_TYPE) {
         throw new OseeArgumentException("This predicate handler only supports [%s]", SearchMethod.EXISTS_TYPE);
      }
      List<String> typeParameters = predicate.getTypeParameters();
      List<String> values = predicate.getValues();

      Conditions.checkNotNull(typeParameters, "typeParameters");
      Conditions.checkNotNull(values, "values");

      if (typeParameters.size() == 1) {
         String existsType = typeParameters.get(0);
         if ("attrType".equals(existsType)) {
            Collection<IAttributeType> attributeTypes = PredicateHandlerUtil.getIAttributeTypes(values);
            builder = andAttrTypesExists(builder, attributeTypes);
         } else if ("relType".equals(existsType)) {
            Collection<IRelationTypeSide> relations = PredicateHandlerUtil.getIRelationTypeSides(values);
            builder = andRelTypeSideExists(builder, relations);
         }
      }

      return builder;
   }

   protected QueryBuilder andRelTypeSideExists(QueryBuilder builder, Collection<IRelationTypeSide> relations) throws OseeCoreException {
      for (IRelationTypeSide rts : relations) {
         builder = builder.andExists(rts);
      }
      return builder;
   }

   protected QueryBuilder andAttrTypesExists(QueryBuilder builder, Collection<IAttributeType> attributeTypes) throws OseeCoreException {
      return builder.andExists(attributeTypes);
   }
}