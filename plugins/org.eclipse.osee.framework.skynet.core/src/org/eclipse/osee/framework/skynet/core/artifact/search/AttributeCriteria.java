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
package org.eclipse.osee.framework.skynet.core.artifact.search;

import java.util.Collection;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.core.model.AttributeType;
import org.eclipse.osee.framework.database.core.JoinUtility;
import org.eclipse.osee.framework.database.core.JoinUtility.CharJoinQuery;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;

/**
 * @author Ryan D. Brooks
 */
public class AttributeCriteria extends AbstractArtifactSearchCriteria {

   private final IAttributeType attributeType;
   private String value;
   private Collection<String> values;
   private String txsAlias;
   private String attrAlias;
   private String joinAlias;
   private final boolean historical;
   private final Operator operator;
   private CharJoinQuery joinQuery;

   /**
    * Constructor for search criteria that finds an attribute of the given type with its current value equal to the
    * given value.
    * 
    * @param attributeType
    * @param value to search; supports % wildcard
    * @throws OseeCoreException
    */
   public AttributeCriteria(String attributeTypeName, String value) throws OseeCoreException {
      this(attributeTypeName, value, false);
   }

   public AttributeCriteria(IAttributeType attributeType, String value) throws OseeDataStoreException, OseeTypeDoesNotExist {
      this(attributeType, value, null, false, Operator.EQUAL);
   }

   /**
    * Constructor for search criteria that finds an attribute of the given type and any value (i.e. checks for
    * existence)
    * 
    * @param attributeTypeName
    * @param value
    * @throws OseeCoreException
    */
   public AttributeCriteria(IAttributeType attributeType) throws OseeCoreException {
      this(attributeType, null, null, false, Operator.EQUAL);
   }

   /**
    * Constructor for search criteria that finds an attribute of the given type with its current value exactly equal to
    * any one of the given literal values. If the list only contains one value, then the search is conducted exactly as
    * if the single value constructor was called. This search does not support the wildcard for multiple values. Throws
    * OseeArgumentException values is empty or null
    * 
    * @param attributeTypeName
    * @param values
    * @throws OseeCoreException
    */
   public AttributeCriteria(IAttributeType attributeType, Collection<String> values) throws OseeCoreException {
      this(attributeType, null, validate(values), false, Operator.EQUAL);
   }

   public AttributeCriteria(String attributeTypeName, Collection<String> values) throws OseeCoreException {
      this(toAttributeType(attributeTypeName), null, validate(values), false, Operator.EQUAL);
   }

   /**
    * Constructor for search criteria that finds an attribute of the given type with its current value exactly equal (or
    * not equal) to any one of the given literal values. If the list only contains one value, then the search is
    * conducted exactly as if the single value constructor was called. This search does not support the wildcard for
    * multiple values.
    * 
    * @param attributeTypeName
    * @param values
    * @throws OseeCoreException
    */
   public AttributeCriteria(String attributeTypeName, Collection<String> values, Operator operator) throws OseeCoreException {
      this(toAttributeType(attributeTypeName), null, validate(values), false, operator);
   }

   /**
    * Constructor for search criteria that finds an attribute of the given type with its current value equal to the
    * given value.
    * 
    * @param attributeTypeName
    * @param value to search; supports % wildcard
    * @param historical if true will search on any branch and any attribute revision
    * @throws OseeCoreException
    */
   public AttributeCriteria(String attributeTypeName, String value, boolean historical) throws OseeCoreException {
      this(toAttributeType(attributeTypeName), value, null, historical, Operator.EQUAL);
   }

   private static AttributeType toAttributeType(String attributeTypeName) throws OseeCoreException {
      return attributeTypeName == null ? null : AttributeTypeManager.getType(attributeTypeName);
   }

   private static Collection<String> validate(Collection<String> values) throws OseeArgumentException {
      if (values == null || values.isEmpty()) {
         throw new OseeArgumentException("values provided to AttributeCriteria must not be null or empty");
      }
      return values;
   }

   public AttributeCriteria(IAttributeType attributeType, String value, Collection<String> values, boolean historical, Operator operator) throws OseeDataStoreException, OseeTypeDoesNotExist {
      this.attributeType = attributeType;

      if (values == null) {
         this.value = value;
      } else {
         if (values.size() == 1) {
            this.value = values.iterator().next();
         } else {
            this.values = values;
            joinQuery = JoinUtility.createCharJoinQuery(ClientSessionManager.getSessionId());
            for (String str : values) {
               joinQuery.add(str);
            }
         }
      }
      this.operator = operator;
      this.historical = historical;
   }

   @Override
   public void addToTableSql(ArtifactQueryBuilder builder) {
      attrAlias = builder.appendAliasedTable("osee_attribute");
      txsAlias = builder.appendAliasedTable("osee_txs");
      if (joinQuery != null && operator == Operator.EQUAL) {
         joinAlias = builder.appendAliasedTable(joinQuery.getJoinTableName());
      }
   }

   @Override
   public void addToWhereSql(ArtifactQueryBuilder builder) throws OseeCoreException {
      if (attributeType != null) {
         builder.append(attrAlias);
         builder.append(".attr_type_id=? AND ");
         builder.addParameter(AttributeTypeManager.getTypeId(attributeType));
      }
      if (value != null) {
         builder.append(attrAlias);
         builder.append(".value");
         if (value.contains("%")) {
            if (operator == Operator.NOT_EQUAL) {
               builder.append(" NOT");
            }
            builder.append(" LIKE ");
         } else {
            builder.append(operator.toString());
         }
         builder.append("? AND ");
         builder.addParameter(value);
      }

      if (joinQuery != null) {
         if (operator == Operator.EQUAL) {
            builder.append(attrAlias);
            builder.append(".value = ");
            builder.append(joinAlias);
            builder.append(".id AND ");
            builder.append(joinAlias);
            builder.append(".query_id = ? AND ");
         } else {
            builder.append("NOT EXISTS (SELECT 1 from ");
            builder.append(joinQuery.getJoinTableName());
            builder.append(" WHERE id = ");
            builder.append(attrAlias);
            builder.append(".value AND query_id = ?) AND ");
         }

         builder.addParameter(joinQuery.getQueryId());
         joinQuery.store();
      }

      builder.append(attrAlias);
      builder.append(".gamma_id=");
      builder.append(txsAlias);
      builder.append(".gamma_id AND ");

      builder.addTxSql(txsAlias, historical);
   }

   @Override
   public void addJoinArtId(ArtifactQueryBuilder builder, boolean left) {
      builder.append(attrAlias);
      builder.append(".art_id");
   }

   @Override
   public String toString() {
      StringBuilder strB = new StringBuilder();
      if (attributeType != null) {
         strB.append(attributeType.getName());
      } else {
         strB.append("*");
      }
      if (operator == Operator.NOT_EQUAL) {
         strB.append(" NOT ");
      }
      strB.append("=");
      if (value != null) {
         strB.append(value);
      }

      if (joinQuery != null) {
         strB.append(attrAlias);
         strB.append("(" + Collections.toString(",", values) + ")");
      }
      return strB.toString();
   }

   @Override
   public void cleanUp() throws OseeDataStoreException {
      if (joinQuery != null) {
         joinQuery.delete();
      }
   }

}