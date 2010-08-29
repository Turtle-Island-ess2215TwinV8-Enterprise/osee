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

package org.eclipse.osee.framework.core.datastore.schema.sql;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.datastore.internal.Activator;
import org.eclipse.osee.framework.core.datastore.schema.data.ConstraintElement;
import org.eclipse.osee.framework.core.datastore.schema.data.ForeignKey;
import org.eclipse.osee.framework.core.datastore.schema.data.ReferenceClause;
import org.eclipse.osee.framework.core.datastore.schema.data.ReferenceClause.OnDeleteEnum;
import org.eclipse.osee.framework.core.datastore.schema.data.ReferenceClause.OnUpdateEnum;
import org.eclipse.osee.framework.core.datastore.schema.data.TableElement.ColumnFields;
import org.eclipse.osee.framework.database.core.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Roberto E. Escobar
 */
public class DerbySqlManager extends SqlManagerImpl {

   public DerbySqlManager(SqlDataType sqlDataType) {
      super(sqlDataType);
   }

   @Override
   public String constraintDataToSQL(ConstraintElement constraint, String tableID) {
      StringBuilder toReturn = new StringBuilder();
      String id = formatQuotedString(constraint.getId(), "\\.");
      String type = constraint.getConstraintType().toString();
      String appliesTo = formatQuotedString(constraint.getCommaSeparatedColumnsList(), ",");

      if (Strings.isValid(id) && Strings.isValid(appliesTo)) {
         toReturn.append("CONSTRAINT " + id + " " + type + " (" + appliesTo + ")");

         if (constraint instanceof ForeignKey) {
            ForeignKey fk = (ForeignKey) constraint;
            List<ReferenceClause> refs = fk.getReferences();

            for (ReferenceClause ref : refs) {
               String refTable = formatQuotedString(ref.getFullyQualifiedTableName(), "\\.");
               String refColumns = formatQuotedString(ref.getCommaSeparatedColumnsList(), ",");

               String onUpdate = "";
               if (!ref.getOnUpdateAction().equals(OnUpdateEnum.UNSPECIFIED)) {
                  onUpdate = "ON UPDATE " + ref.getOnUpdateAction().toString();
               }

               String onDelete = "";
               if (!ref.getOnDeleteAction().equals(OnDeleteEnum.UNSPECIFIED)) {
                  onDelete = "ON DELETE " + ref.getOnDeleteAction().toString();
               }

               if (refTable != null && refColumns != null && !refTable.equals("") && !refColumns.equals("")) {
                  toReturn.append(" REFERENCES " + refTable + " (" + refColumns + ")");
                  if (!onUpdate.equals("")) {
                     toReturn.append(" " + onUpdate);
                  }

                  if (!onDelete.equals("")) {
                     toReturn.append(" " + onDelete);
                  }

                  // Not Supported in Derby ?
                  //                  if (constraint.isDeferrable()) {
                  //                     toReturn.append(" DEFERRABLE");
                  //                  }
               }

               else {
                  OseeLog.log(Activator.class, Level.WARNING,
                     "Skipping CONSTRAINT at Table: " + tableID + "\n\t " + fk.toString());
               }

            }
         }
      } else {
         OseeLog.log(Activator.class, Level.WARNING,
            "Skipping CONSTRAINT at Table: " + tableID + "\n\t " + constraint.toString());
      }
      return toReturn.toString();
   }

   @Override
   public String columnDataToSQL(Map<ColumnFields, String> column) {
      StringBuilder toReturn = new StringBuilder();

      String columnLimits = column.get(ColumnFields.limits);
      String defaultValue = column.get(ColumnFields.defaultValue);

      SQL3DataType dataType = SQL3DataType.valueOf(column.get(ColumnFields.type));
      columnLimits = sqlDataType.getLimit(dataType, columnLimits);
      toReturn.append("\"");
      toReturn.append(column.get(ColumnFields.id));
      toReturn.append("\"");
      toReturn.append(" ");
      toReturn.append(sqlDataType.getType(dataType));

      if (Strings.isValid(columnLimits)) {
         toReturn.append(" (" + columnLimits + ")");
      }
      if (Strings.isValid(defaultValue)) {
         toReturn.append(" " + defaultValue);
      }
      return toReturn.toString();
   }
}
