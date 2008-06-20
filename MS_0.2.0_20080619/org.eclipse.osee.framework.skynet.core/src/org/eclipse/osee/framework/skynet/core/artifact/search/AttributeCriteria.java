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

import java.sql.SQLException;
import java.util.Collection;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeType;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.utility.JoinUtility;
import org.eclipse.osee.framework.skynet.core.utility.JoinUtility.AttributeJoinQuery;

/**
 * @author Ryan D. Brooks
 */
public class AttributeCriteria extends AbstractArtifactSearchCriteria {

   private AttributeType attributeType;
   private String value;
   private Collection<String> values;
   private String txsAlias;
   private String txdAlias;
   private String attrAlias;
   private final boolean historical;
   private final Operator operator;
   private AttributeJoinQuery joinQuery;

   /**
    * Constructor for search criteria that finds an attribute of the given type with its current value equal to the
    * given value.
    * 
    * @param attributeType
    * @param value to search; supports % wildcard
    * @throws SQLException
    */
   public AttributeCriteria(String attributeTypeName, String value) throws SQLException {
      this(attributeTypeName, value, false);
   }

   /**
    * Constructor for search criteria that finds an attribute of the given type and any value (i.e. checks for
    * existence)
    * 
    * @param attributeTypeName
    * @param value
    * @throws SQLException
    */
   public AttributeCriteria(String attributeTypeName) throws SQLException {
      this(attributeTypeName, null, false);
   }

   /**
    * Constructor for search criteria that finds an attribute of the given type with its current value exactly equal to
    * any one of the given literal values. If the list only contains one value, then the search is conducted exactly as
    * if the single value constructor was called. This search does not support the wildcard for multiple values.
    * 
    * @param attributeTypeName
    * @param values
    * @throws SQLException
    */
   public AttributeCriteria(String attributeTypeName, Collection<String> values) throws SQLException {
      this(attributeTypeName, null, values, false, Operator.EQUAL);
   }

   /**
    * Constructor for search criteria that finds an attribute of the given type with its current value exactly equal (or
    * not equal) to any one of the given literal values. If the list only contains one value, then the search is
    * conducted exactly as if the single value constructor was called. This search does not support the wildcard for
    * multiple values.
    * 
    * @param attributeTypeName
    * @param values
    * @throws SQLException
    */
   public AttributeCriteria(String attributeTypeName, Collection<String> values, Operator operator) throws SQLException {
      this(attributeTypeName, null, values, false, operator);
   }

   /**
    * Constructor for search criteria that finds an attribute of the given type with its current value equal to the
    * given value.
    * 
    * @param attributeTypeName
    * @param value to search; supports % wildcard
    * @param historical if true will search on any branch and any attribute revision
    * @throws SQLException
    */
   public AttributeCriteria(String attributeTypeName, String value, boolean historical) throws SQLException {
      this(attributeTypeName, value, null, historical, Operator.EQUAL);
   }

   private AttributeCriteria(String attributeTypeName, String value, Collection<String> values, boolean historical, Operator operator) throws SQLException {
      if (attributeTypeName != null) {
         this.attributeType = AttributeTypeManager.getType(attributeTypeName);
      }

      if (values == null) {
         this.value = value;
      } else {
         if (values.size() == 1) {
            this.value = values.iterator().next();
         } else {
            this.values = values;
            joinQuery = JoinUtility.createAttributeJoinQuery();
            for (String str : values) {
               joinQuery.add(str);
            }
            joinQuery.store();
         }
      }
      this.operator = operator;
      this.historical = historical;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.artifact.search.AbstractArtifactSearchCriteria#addToTableSql(org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQueryBuilder)
    */
   @Override
   public void addToTableSql(ArtifactQueryBuilder builder) {
      attrAlias = builder.appendAliasedTable("osee_define_attribute");
      txsAlias = builder.appendAliasedTable("osee_define_txs");
      txdAlias = builder.appendAliasedTable("osee_define_tx_details");
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.artifact.search.AbstractArtifactSearchCriteria#addToWhereSql(org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQueryBuilder)
    */
   @Override
   public void addToWhereSql(ArtifactQueryBuilder builder) {
      if (attributeType != null) {
         builder.append(attrAlias);
         builder.append(".attr_type_id=? AND ");
         builder.addParameter(SQL3DataType.INTEGER, attributeType.getAttrTypeId());
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
            if (operator == Operator.NOT_EQUAL) {
               builder.append("<>");
            } else {
               builder.append("=");
            }
         }
         builder.append("? AND ");
         builder.addParameter(SQL3DataType.VARCHAR, value);
      }

      if (values != null && values.size() > 0) {

         builder.append(attrAlias);
         builder.append(".value ");
         if (operator == Operator.NOT_EQUAL) {
            builder.append("NOT ");
         }
         builder.append("IN ( SELECT value FROM osee_join_attribute WHERE attr_query_id = ? ) AND ");
         builder.addParameter(SQL3DataType.INTEGER, joinQuery.getQueryId());
      }

      builder.append(attrAlias);
      builder.append(".gamma_id=");
      builder.append(txsAlias);
      builder.append(".gamma_id AND ");

      if (historical) {
         builder.addBranchTxSql(txsAlias, txdAlias);
      } else {
         builder.addCurrentTxSql(txsAlias, txdAlias);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.artifact.search.AbstractArtifactSearchCriteria#addJoinArtId(org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQueryBuilder)
    */
   @Override
   public void addJoinArtId(ArtifactQueryBuilder builder, boolean left) {
      builder.append(attrAlias);
      builder.append(".art_id");
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
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

      if (values != null && values.size() > 0) {
         strB.append(attrAlias);
         strB.append("(" + Collections.toString(",", values) + ")");
      }
      return strB.toString();
   }

   public void cleanUp() throws SQLException {
      if (joinQuery != null) {
         joinQuery.delete();
      }
   }

}