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

import java.util.List;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.Identity;
import org.eclipse.osee.framework.core.data.NamedIdentity;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;

/**
 * @author Robert A. Fisher
 */
public class InRelationSearch implements ISearchPrimitive {
   private static final String relationTables = "osee_relation_link rel_1, osee_txs txs1";
   private final static String TOKEN = ";";
   private final Identity<Long> relationType;
   private final boolean sideA;

   public InRelationSearch(Identity<Long> relationType, boolean sideA) {
      this.relationType = relationType;
      this.sideA = sideA;
   }

   @Override
   public String getArtIdColName() {
      return sideA ? "a_art_id" : "b_art_id";
   }

   @Override
   public String getCriteriaSql(List<Object> dataList, IOseeBranch branch) throws OseeCoreException {
      IdentityService remoteIdManager = ServiceUtil.getIdentityService();
      dataList.add(remoteIdManager.getLocalId(relationType));
      dataList.add(BranchManager.getBranchId(branch));
      dataList.add(ModificationType.DELETED.getValue());
      return "rel_1.rel_link_type_id = ? AND rel_1.gamma_id = txs1.gamma_id AND txs1.transaction_id = (SELECT max(txs1.transaction_id) FROM osee_relation_link rel2, osee_txs txs1 WHERE rel2.rel_link_id = rel_1.rel_link_id AND rel2.gamma_id = txs1.gamma_id AND txs1.branch_id = ? AND txs1.mod_type <>?)";
   }

   @Override
   public String getTableSql(List<Object> dataList, IOseeBranch branch) {
      return relationTables;
   }

   @Override
   public String toString() {
      return "In Relation: " + relationType + " from";
   }

   @Override
   public String getStorageString() {
      return sideA + TOKEN + relationType.getGuid().toString();
   }

   public static InRelationSearch getPrimitive(String storageString) {
      String[] values = storageString.split(TOKEN);
      if (values.length < 2) {
         throw new IllegalStateException("Value for " + InRelationSearch.class.getSimpleName() + " not parsable");
      }

      NamedIdentity<Long> identity = new NamedIdentity<Long>(Long.valueOf(values[1]), "");
      return new InRelationSearch(identity, Boolean.parseBoolean(values[0]));
   }
}