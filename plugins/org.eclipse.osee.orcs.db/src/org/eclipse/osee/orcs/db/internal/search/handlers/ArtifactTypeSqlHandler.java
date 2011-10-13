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
package org.eclipse.osee.orcs.db.internal.search.handlers;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.database.core.AbstractJoinQuery;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaArtifactType;
import org.eclipse.osee.orcs.db.internal.search.SqlConstants.CriteriaPriority;
import org.eclipse.osee.orcs.db.internal.search.SqlConstants.TableEnum;
import org.eclipse.osee.orcs.db.internal.search.SqlHandler;
import org.eclipse.osee.orcs.db.internal.search.SqlWriter;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactTypeSqlHandler extends SqlHandler {

   private CriteriaArtifactType criteria;

   private String jIdAlias;
   private AbstractJoinQuery joinQuery;

   @Override
   public void setData(Criteria criteria) {
      this.criteria = (CriteriaArtifactType) criteria;
   }

   @Override
   public void addTables(SqlWriter writer) throws OseeCoreException {
      if (criteria.getTypes().size() > 1) {
         jIdAlias = writer.writeTable(TableEnum.ID_JOIN_TABLE);
      }
   }

   @Override
   public void addPredicates(SqlWriter writer) throws OseeCoreException {
      Collection<? extends IArtifactType> types = criteria.getTypes();
      if (types.size() > 1) {
         Set<Integer> typeIds = new HashSet<Integer>();
         for (IArtifactType type : types) {
            typeIds.add(toLocalId(type));
         }
         joinQuery = writer.writeIdJoin(typeIds);
         writer.write(jIdAlias);
         writer.write(".query_id=?");
         writer.addParameter(joinQuery.getQueryId());

         List<String> aliases = writer.getAliases(TableEnum.ARTIFACT_TABLE);
         if (!aliases.isEmpty()) {
            writer.write(" AND ");
            int aSize = aliases.size();
            for (int index = 0; index < aSize; index++) {
               String artAlias = aliases.get(index);
               writer.write(artAlias);
               writer.write(".art_type_id=");
               writer.write(jIdAlias);
               writer.write(".id");
               if (index + 1 < aSize) {
                  writer.write(" AND ");
               }
            }
         }
      } else {
         IArtifactType type = types.iterator().next();
         int localId = toLocalId(type);

         List<String> aliases = writer.getAliases(TableEnum.ARTIFACT_TABLE);
         int aSize = aliases.size();
         for (int index = 0; index < aSize; index++) {
            String artAlias = aliases.get(index);
            writer.write(artAlias);
            writer.write(".art_type_id = ?");
            writer.addParameter(localId);
            if (index + 1 < aSize) {
               writer.write(" AND ");
            }
         }
      }
   }

   @Override
   public int getPriority() {
      return CriteriaPriority.ARTIFACT_TYPE.ordinal();
   }
}
