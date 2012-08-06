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
package org.eclipse.osee.orcs.db.internal.loader;

import java.util.List;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.DataPostProcessor;
import org.eclipse.osee.orcs.core.ds.LoadOptions;
import org.eclipse.osee.orcs.db.internal.SqlProvider;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.SqlContext;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;

/**
 * @author Roberto E. Escobar
 */
public class LoadSqlWriter extends AbstractSqlWriter<LoadOptions> {

   public LoadSqlWriter(Log logger, IOseeDatabaseService dbService, SqlProvider sqlProvider, SqlContext<LoadOptions, DataPostProcessor<?>> context) {
      super(logger, dbService, sqlProvider, context);
   }

   @Override
   public void writeSelect(List<SqlHandler<?, LoadOptions>> handlers) throws OseeCoreException {
      String txAlias = getAliasManager().getFirstAlias(TableEnum.TXS_TABLE);
      String artJoinAlias = getAliasManager().getFirstAlias(TableEnum.ARTIFACT_JOIN_TABLE);

      write("SELECT%s ", getSqlHint());
      write("%s.gamma_id, %s.mod_type, %s.branch_id, %s.transaction_id", txAlias, txAlias, txAlias, txAlias);
      if (getOptions().isHistorical()) {
         write(", %s.transaction_id as stripe_transaction_id", txAlias);
      }
      write(",\n %s.art_id", artJoinAlias);
      int size = handlers.size();
      for (int index = 0; index < size; index++) {
         write(", ");
         SqlHandler<?, LoadOptions> handler = handlers.get(index);
         handler.addSelect(this);
      }
   }

   @Override
   public void writeGroupAndOrder() throws OseeCoreException {
      String artAlias = getAliasManager().getFirstAlias(TableEnum.ARTIFACT_JOIN_TABLE);
      String txAlias = getAliasManager().getFirstAlias(TableEnum.TXS_TABLE);

      write("\n ORDER BY %s.branch_id, %s.art_id", txAlias, artAlias);
      if (getAliasManager().hasAlias(TableEnum.ATTRIBUTE_TABLE)) {
         write(", %s.attr_id", getAliasManager().getFirstAlias(TableEnum.ATTRIBUTE_TABLE));
      }
      if (getAliasManager().hasAlias(TableEnum.RELATION_TABLE)) {
         write(", %s.rel_link_id", getAliasManager().getFirstAlias(TableEnum.RELATION_TABLE));
      }
      write(", %s.transaction_id desc", txAlias);
   }

   @Override
   public void writeTxBranchFilter(String txsAlias) throws OseeCoreException {
      String artJoinAlias = getAliasManager().getFirstAlias(TableEnum.ARTIFACT_JOIN_TABLE);
      writeTxFilter(txsAlias, artJoinAlias);
      write(" AND ");
      write(txsAlias);
      write(".branch_id = ");
      write(artJoinAlias);
      write(".branch_id");
   }

   private void writeTxFilter(String txsAlias, String artJoinAlias) throws OseeCoreException {
      if (getOptions().isHistorical()) {
         write(txsAlias);
         write(".transaction_id <= ");
         write(artJoinAlias);
         write(".transaction_id");
         if (!getOptions().areDeletedIncluded()) {
            write(" AND ");
            write(txsAlias);
            write(".tx_current");
            write(" IN (");
            write(String.valueOf(TxChange.CURRENT.getValue()));
            write(", ");
            write(String.valueOf(TxChange.NOT_CURRENT.getValue()));
            write(")");
         }
      } else {
         write(txsAlias);
         write(".tx_current");
         if (getOptions().areDeletedIncluded()) {
            write(" IN (");
            write(String.valueOf(TxChange.CURRENT.getValue()));
            write(", ");
            write(String.valueOf(TxChange.DELETED.getValue()));
            write(", ");
            write(String.valueOf(TxChange.ARTIFACT_DELETED.getValue()));
            write(")");
         } else {
            write(" = ");
            write(String.valueOf(TxChange.CURRENT.getValue()));
         }
      }
   }

   @Override
   public LoadOptions getOptions() {
      return getContext().getOptions();
   }
}