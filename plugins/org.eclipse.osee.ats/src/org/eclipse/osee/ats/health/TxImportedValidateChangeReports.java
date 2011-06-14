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
package org.eclipse.osee.ats.health;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.IOseeSequence;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeInfo;
import org.eclipse.osee.framework.jdk.core.text.change.ChangeSet;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;

/**
 * @author Roberto E. Escobar
 */
public class TxImportedValidateChangeReports extends AbstractBlam {

   private static final String[] ARTIFACT_ID_ALIASES = new String[] {"artId", "bArtId", "aOrdr", "bOrdr"};
   public static final String BRANCH_ID_ALIASES = "brGuid";
   private static final String GAMMA_ID_ALIASES = "gamma";
   private static final String[] TRANSACTION_ID_ALIASES = new String[] {"tTranId", "fTranId"};
   private static final String ARTIFACT_TYPE_ID = "artTId";
   private static final String ATTRIBUTE_TYPE_ID = "attrTId";
   private static final String RELATION_TYPE_ID = "relTId";
   private static final String ATTRIBUTE_ID = "attrId";
   private static final String RELATION_ID = "relId";
   private static final String EMPTY_STRING = "";

   private static final String VCR_ROOT_ELEMENT_TAG = ValidateChangeReports.VCR_ROOT_ELEMENT_TAG;
   private static final String VCR_DB_GUID = ValidateChangeReports.VCR_DB_GUID;

   private static final Matcher NUMERICAL_MATCH = Pattern.compile("\\d+").matcher(EMPTY_STRING);
   private static final Matcher SOURCE_DB_GUID_MATCHER = Pattern.compile(
      "\\s*<" + VCR_ROOT_ELEMENT_TAG + "\\s*" + VCR_DB_GUID + "=\"(.*?)\"\\s*>").matcher(EMPTY_STRING);
   private static final Matcher XML_TAGGED_IDS_MATCHER = Pattern.compile("<(.*?)>(\\d+)</(.*?)>").matcher(EMPTY_STRING);

   private Map<String, ImportedId> translatorMap;
   private String currentDbGuid;

   @Override
   public String getName() {
      return "Tx Imported Validate Change Reports";
   }

   private void setup(String databaseTargetId) throws OseeCoreException {
      List<ImportedId> importtedIds = getImportedIds();
      for (ImportedId importedId : importtedIds) {
         report(importedId.getSequence());
         importedId.load(databaseTargetId);
      }

      this.translatorMap = new HashMap<String, ImportedId>();
      for (ImportedId translator : importtedIds) {
         for (String alias : translator.getAliases()) {
            translatorMap.put(alias, translator);
         }
      }

      this.currentDbGuid = OseeInfo.getDatabaseGuid();
   }

   private void cleanUp() {
      if (translatorMap != null && !translatorMap.isEmpty()) {
         for (String key : translatorMap.keySet()) {
            translatorMap.get(key).clear();
         }
         translatorMap.clear();
      }
      this.currentDbGuid = null;
   }

   private List<ImportedId> getImportedIds() {
      List<ImportedId> translators = new ArrayList<ImportedId>();
      translators.add(new ImportedId(IOseeSequence.GAMMA_ID_SEQ, GAMMA_ID_ALIASES));
      translators.add(new ImportedId(IOseeSequence.TRANSACTION_ID_SEQ, TRANSACTION_ID_ALIASES));
      translators.add(new ImportedId(IOseeSequence.BRANCH_ID_SEQ, BRANCH_ID_ALIASES));
      translators.add(new ImportedId(IOseeSequence.ART_TYPE_ID_SEQ, ARTIFACT_TYPE_ID));
      translators.add(new ImportedId(IOseeSequence.ATTR_TYPE_ID_SEQ, ATTRIBUTE_TYPE_ID));
      translators.add(new ImportedId(IOseeSequence.REL_LINK_TYPE_ID_SEQ, RELATION_TYPE_ID));
      translators.add(new ImportedId(IOseeSequence.ART_ID_SEQ, ARTIFACT_ID_ALIASES));
      translators.add(new ImportedId(IOseeSequence.ATTR_ID_SEQ, ATTRIBUTE_ID));
      translators.add(new ImportedId(IOseeSequence.REL_LINK_ID_SEQ, RELATION_ID));
      return translators;
   }

   private long translate(String tag, long original) {
      long toReturn = original;
      if (Strings.isValid(tag)) {
         ImportedId importedId = translatorMap.get(tag);
         if (importedId != null) {
            toReturn = importedId.getFromCache(original);
         }
      }
      return toReturn;
   }

   private String getDataDbGuid(String data) {
      String toReturn = null;
      SOURCE_DB_GUID_MATCHER.reset(data);
      if (SOURCE_DB_GUID_MATCHER.find()) {
         toReturn = SOURCE_DB_GUID_MATCHER.group(1);
      }
      return toReturn != null ? toReturn : EMPTY_STRING;
   }

   @Override
   public void runOperation(VariableMap variableMap, IProgressMonitor monitor) throws Exception {
      try {
         Branch branch = AtsUtil.getAtsBranch();
         String databaseTargetId = variableMap.getString("Import Db Id");
         boolean shouldIncludeItemsWithoutDbId = variableMap.getBoolean("Include items without database id");
         if (!Strings.isValid(databaseTargetId)) {
            throw new OseeArgumentException("Invalid database target id");
         }

         databaseTargetId = databaseTargetId.trim();
         setup(databaseTargetId);

         List<Artifact> artifacts =
            ArtifactQuery.getArtifactListFromTypeAndName(CoreArtifactTypes.GeneralData, "VCR_%", branch);
         for (Artifact artifact : artifacts) {
            String data = artifact.getSoleAttributeValue(CoreAttributeTypes.GeneralStringData);
            String name = artifact.getName();
            try {
               String dataDbGuid = getDataDbGuid(data);
               if (Strings.isValid(dataDbGuid) || shouldIncludeItemsWithoutDbId && databaseTargetId.equals(dataDbGuid) || shouldIncludeItemsWithoutDbId && !currentDbGuid.equals(dataDbGuid)) {
                  String modified = translateImportedData(data);
                  modified = updateSourceGuid(currentDbGuid, modified);
                  artifact.setSoleAttributeValue(CoreAttributeTypes.GeneralStringData, modified);
               }
            } catch (Exception ex) {
               throw new OseeCoreException(String.format("Error processing [%s]", name), ex);
            }
         }
         Artifacts.persistInTransaction("Import Validate Change Reports", artifacts);
      } finally {
         cleanUp();
      }
   }

   @Override
   public String getXWidgetsXml() {
      StringBuilder builder = new StringBuilder();
      builder.append("<xWidgets>");
      builder.append("<XWidget xwidgetType=\"XText\" displayName=\"Import Db Id\" defaultValue=\"AAABHL_5XvkAFHT8QsrrPQ\"/>");
      builder.append("<XWidget xwidgetType=\"XCheckBox\" displayName=\"Include items without database id\" labelAfter=\"true\" horizontalLabel=\"true\"/>");
      builder.append("</xWidgets>");
      return builder.toString();
   }

   private static boolean isNumerical(String value) {
      boolean result = false;
      if (Strings.isValid(value)) {
         NUMERICAL_MATCH.reset(value);
         result = NUMERICAL_MATCH.matches();
      }
      return result;
   }

   private String translateImportedData(String data) {
      ChangeSet changeSet = new ChangeSet(data);
      XML_TAGGED_IDS_MATCHER.reset(data);
      while (XML_TAGGED_IDS_MATCHER.find()) {
         String tag = XML_TAGGED_IDS_MATCHER.group(3);
         tag = tag.toLowerCase().trim();
         String value = XML_TAGGED_IDS_MATCHER.group(2);
         if (isNumerical(value)) {
            long original = Long.parseLong(value);
            long newValue = translate(tag, original);
            if (original != newValue) {
               changeSet.replace(XML_TAGGED_IDS_MATCHER.start(2), XML_TAGGED_IDS_MATCHER.end(2),
                  Long.toString(newValue));
            }
         }
      }
      return changeSet.applyChangesToSelf().toString();
   }

   private String updateSourceGuid(String currentDbGuid, String data) throws OseeStateException {
      String toReturn = null;
      ChangeSet changeSet = new ChangeSet(data);
      SOURCE_DB_GUID_MATCHER.reset(data);
      if (SOURCE_DB_GUID_MATCHER.find()) {
         String id = SOURCE_DB_GUID_MATCHER.group(1);
         if (!currentDbGuid.equals(id)) {
            changeSet.replace(SOURCE_DB_GUID_MATCHER.start(1), SOURCE_DB_GUID_MATCHER.end(1), currentDbGuid);
            toReturn = changeSet.applyChangesToSelf().toString();
         } else {
            toReturn = data;
         }
      } else {
         if (!data.contains(VCR_ROOT_ELEMENT_TAG)) {
            toReturn =
               String.format("<%s dbGuid=\"%s\">%s</%s>", VCR_ROOT_ELEMENT_TAG, currentDbGuid, data,
                  VCR_ROOT_ELEMENT_TAG);
         } else {
            throw new OseeStateException("Error updating dbId");
         }
      }
      return toReturn;
   }
   private static final class ImportedId {
      private static final String SELECT_IDS_BY_DB_SOURCE_AND_SEQ_NAME =
         "SELECT original_id, mapped_id FROM osee_import_source ois, osee_import_map oim, osee_import_index_map oiim WHERE ois.import_id = oim.import_id AND oim.sequence_id = oiim.sequence_id AND oiim.sequence_id = oiim.sequence_id AND ois.db_source_guid = ?  AND oim.sequence_name = ?";

      private final String sequenceName;
      private final Map<Long, Long> originalToMapped;
      private final Set<String> aliases;

      ImportedId(String sequenceName, String... aliases) {
         this.sequenceName = sequenceName;
         this.originalToMapped = new HashMap<Long, Long>();
         this.aliases = new HashSet<String>();
         if (aliases != null && aliases.length > 0) {
            for (String alias : aliases) {
               this.aliases.add(alias.toLowerCase());
            }
         }
      }

      public void clear() {
         this.originalToMapped.clear();
         this.aliases.clear();
      }

      public Set<String> getAliases() {
         return this.aliases;
      }

      public String getSequence() {
         return this.sequenceName;
      }

      public Long getFromCache(Long original) {
         Long newVersion = null;
         if (original <= 0L) {
            newVersion = original;
         } else {
            newVersion = this.originalToMapped.get(original);
         }
         return newVersion;
      }

      public void load(String sourceDatabaseId) throws OseeCoreException {
         IOseeStatement chStmt = ConnectionHandler.getStatement();
         try {
            originalToMapped.clear();
            chStmt.runPreparedQuery(10000, SELECT_IDS_BY_DB_SOURCE_AND_SEQ_NAME, sourceDatabaseId, getSequence());
            while (chStmt.next()) {
               originalToMapped.put(chStmt.getLong("original_id"), chStmt.getLong("mapped_id"));
            }
         } finally {
            chStmt.close();
         }
      }
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("ATS.Admin");
   }
}
