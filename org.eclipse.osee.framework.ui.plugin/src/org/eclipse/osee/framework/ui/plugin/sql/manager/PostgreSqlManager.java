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
package org.eclipse.osee.framework.ui.plugin.sql.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.framework.jdk.core.util.StringFormat;
import org.eclipse.osee.framework.ui.plugin.sql.dataType.SqlDataType;
import org.eclipse.osee.framework.ui.plugin.util.db.data.AppliesToClause;
import org.eclipse.osee.framework.ui.plugin.util.db.data.ColumnMetadata;
import org.eclipse.osee.framework.ui.plugin.util.db.data.ConstraintElement;
import org.eclipse.osee.framework.ui.plugin.util.db.data.ForeignKey;
import org.eclipse.osee.framework.ui.plugin.util.db.data.IndexElement;
import org.eclipse.osee.framework.ui.plugin.util.db.data.ReferenceClause;
import org.eclipse.osee.framework.ui.plugin.util.db.data.TableElement;
import org.eclipse.osee.framework.ui.plugin.util.db.data.ReferenceClause.OnDeleteEnum;
import org.eclipse.osee.framework.ui.plugin.util.db.data.ReferenceClause.OnUpdateEnum;
import org.eclipse.osee.framework.ui.plugin.util.db.data.TableElement.ColumnFields;

/**
 * @author Andrew M. Finkbeiner
 */
public class PostgreSqlManager extends SqlManagerImpl {
   /**
    * @param sqlDataType
    */
   public PostgreSqlManager(SqlDataType sqlDataType) {
      super(sqlDataType);
   }

   private String handleColumnCreationSection(Connection connection, Map<String, ColumnMetadata> columns) throws SQLException {
      List<String> lines = new ArrayList<String>();
      Set<String> keys = columns.keySet();
      for (String key : keys) {
         Map<ColumnFields, String> column = columns.get(key).getColumnFields();
         lines.add(columnDataToSQL(column));
      }
      String toExecute = StringFormat.listToValueSeparatedString(lines, ",\n");
      return toExecute;
   }

   public void createTable(Connection connection, TableElement tableDef) throws SQLException, Exception {
      String toExecute = "CREATE TABLE " + tableDef.getFullyQualifiedTableName() + " ( \n";
      toExecute += handleColumnCreationSection(connection, tableDef.getColumns());
      toExecute += handleConstraintCreationSection(tableDef.getConstraints(), tableDef.getFullyQualifiedTableName());
      toExecute +=
            handleConstraintCreationSection(tableDef.getForeignKeyConstraints(), tableDef.getFullyQualifiedTableName());
      toExecute += " \n)\n";
      logger.log(Level.INFO, "Creating Table: [ " + tableDef.getFullyQualifiedTableName() + "]");
      executeStatement(connection, toExecute);
   }

   @Override
   public void dropTable(Connection connection, TableElement tableDef) throws SQLException, Exception {
      String toExecute = "DROP TABLE " + formatQuotedString(tableDef.getFullyQualifiedTableName(), "\\.") + " CASCADE";
      logger.log(Level.INFO, "Dropping Table: [ " + tableDef.getFullyQualifiedTableName() + "]");
      executeStatement(connection, toExecute);
   }

   protected String formatQuotedString(String value, String splitAt) {
      String[] array = value.split(splitAt);
      for (int index = 0; index < array.length; index++) {
         array[index] = array[index];
      }
      // return value;
      return StringFormat.separateWith(array, splitAt.replaceAll("\\\\", ""));
   }

   public void dropIndex(Connection connection, TableElement tableDef) throws SQLException, Exception {
      List<IndexElement> tableIndeces = tableDef.getIndexData();
      String tableName = tableDef.getFullyQualifiedTableName();
      for (IndexElement iData : tableIndeces) {
         if (iData.ignoreMySql()) continue;
         logger.log(Level.INFO, String.format("Dropping Index: [%s] FROM [%s]\n", iData.getId(), tableName));
         if (iData.getId().equals("PRIMARY")) {
            executeStatement(connection, "ALTER TABLE " + tableDef.getFullyQualifiedTableName() + " DROP PRIMARY KEY");
         } else {
            executeStatement(connection,
                  "ALTER TABLE " + tableDef.getFullyQualifiedTableName() + " DROP INDEX " + iData.getId());
         }
      }
   }

   public String constraintDataToSQL(ConstraintElement constraint, String tableID) {
      StringBuilder toReturn = new StringBuilder();
      String id = formatQuotedString(constraint.getId(), "\\.");
      String type = constraint.getConstraintType().toString();
      String appliesTo = formatQuotedString(constraint.getCommaSeparatedColumnsList(), ",");

      if (id != null && !id.equals("") && appliesTo != null && !appliesTo.equals("")) {
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

                  if (constraint.isDeferrable()) {
                     toReturn.append(" DEFERRABLE");
                  }
               }

               else {
                  logger.log(Level.WARNING, "Skipping CONSTRAINT at Table: " + tableID + "\n\t " + fk.toString());
               }

            }
         }
      } else {
         logger.log(Level.WARNING, "Skipping CONSTRAINT at Table: " + tableID + "\n\t " + constraint.toString());
      }
      return toReturn.toString();
   }

   public void createIndex(Connection connection, TableElement tableDef) throws SQLException, Exception {
      List<IndexElement> tableIndeces = tableDef.getIndexData();
      String indexId = null;
      StringBuilder appliesTo = new StringBuilder();
      String tableName = formatQuotedString(tableDef.getFullyQualifiedTableName(), "\\.");
      for (IndexElement iData : tableIndeces) {
         if (iData.ignoreMySql()) continue;
         indexId = iData.getId();
         appliesTo.delete(0, appliesTo.length());

         List<AppliesToClause> appliesToList = iData.getAppliesToList();
         for (int index = 0; index < appliesToList.size(); index++) {
            AppliesToClause record = appliesToList.get(index);
            appliesTo.append(record.getColumnName());

            //            switch (record.getOrderType()) {
            //               case Ascending:
            //                  appliesTo.append(" ASC");
            //                  break;
            //               case Descending:
            //                  appliesTo.append(" DESC");
            //                  break;
            //               default:
            //                  break;
            //            }
            if (index + 1 < appliesToList.size()) {
               appliesTo.append(", ");
            }
         }
         String toExecute =
               String.format(CREATE_STRING + " " + iData.getIndexType() + " INDEX %s ON %s (%s)", indexId, tableName,
                     appliesTo);
         logger.log(Level.INFO, toExecute);
         executeStatement(connection, toExecute);
      }
   }

}
