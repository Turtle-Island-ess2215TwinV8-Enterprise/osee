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

import static org.eclipse.osee.framework.skynet.core.artifact.search.DeprecatedOperator.EQUAL;
import java.util.List;
import org.eclipse.osee.framework.core.data.IOseeBranch;

/**
 * @author Robert A. Fisher
 */
public class ArtifactHridSearch implements ISearchPrimitive {
   private final String humanReadableId;
   private final DeprecatedOperator operator;

   /**
    * @param humanReadableId The human readable id to search for
    */
   public ArtifactHridSearch(String humanReadableId, DeprecatedOperator operator) {
      super();
      this.operator = operator;
      this.humanReadableId = humanReadableId;
   }

   /**
    * @param humanReadableId The human readable id to search for
    */
   public ArtifactHridSearch(String humanReadableId) {
      this(humanReadableId, EQUAL);
   }

   @Override
   public String getArtIdColName() {
      return "art_id";
   }

   @Override
   public String getCriteriaSql(List<Object> dataList, IOseeBranch branch) {
      String sql = "osee_artifact.human_readable_id" + operator + "?";
      dataList.add(humanReadableId);

      return sql;
   }

   @Override
   public String getTableSql(List<Object> dataList, IOseeBranch branch) {
      return "osee_artifact";
   }

   @Override
   public String toString() {
      return "Artifact Human Readable Id: " + humanReadableId;
   }

   @Override
   public String getStorageString() {
      return humanReadableId;
   }

   public static ArtifactHridSearch getPrimitive(String storageString) {
      return new ArtifactHridSearch(storageString, EQUAL);
   }
}
