/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.branch.management.exchange;

import java.util.Map;
import org.eclipse.osee.framework.branch.management.exchange.handler.BaseDbSaxHandler;
import org.eclipse.osee.framework.branch.management.exchange.handler.IExportItem;
import org.eclipse.osee.framework.database.IOseeDatabaseService;

/**
 * @author Ryan D. Brooks
 */
public class ForeignKeyReader extends BaseDbSaxHandler {
   private final String[] foreignKeys;
   private final PrimaryKeyCollector primaryKeyCollector;
   private final IExportItem foreignTable;

   public ForeignKeyReader(IOseeDatabaseService service, PrimaryKeyCollector primaryKeyCollector, IExportItem foreignTable, String... foreignKeys) {
      super(service, true, 0);
      this.primaryKeyCollector = primaryKeyCollector;
      this.foreignKeys = foreignKeys;
      this.foreignTable = foreignTable;
   }

   @Override
   protected void processData(Map<String, String> fieldMap) {
      for (String foreignKey : foreignKeys) {
         String value = fieldMap.get(foreignKey);
         if (value != null) {
            Long id = Long.valueOf(value);
            primaryKeyCollector.markAsReferenced(foreignTable + "." + foreignKey, id);
         }
      }
   }
}