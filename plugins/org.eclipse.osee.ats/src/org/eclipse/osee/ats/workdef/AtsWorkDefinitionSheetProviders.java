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
package org.eclipse.osee.ats.workdef;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.ats.core.client.config.AtsArtifactToken;
import org.eclipse.osee.ats.core.workdef.WorkDefinitionSheet;
import org.eclipse.osee.ats.dsl.atsDsl.AtsDsl;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.workdef.config.ImportAIsAndTeamDefinitionsToDb;
import org.eclipse.osee.ats.workdef.provider.AtsWorkDefinitionImporter;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.PluginUtil;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.osgi.framework.Bundle;

/**
 * @author Donald G. Dunne
 */
public final class AtsWorkDefinitionSheetProviders {

   private static Set<IAtsWorkDefinitionSheetProvider> teamWorkflowExtensionItems;
   public static String WORK_DEF_TEAM_DEFAULT = "WorkDef_Team_Default";

   private AtsWorkDefinitionSheetProviders() {
      // private constructor
   }

   public static void initializeDatabase(XResultData resultData) throws OseeCoreException {
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Import ATS Work Definitions, Teams and AIs");
      Artifact folder =
         OseeSystemArtifacts.getOrCreateArtifact(AtsArtifactToken.WorkDefinitionsFolder, AtsUtil.getAtsBranch());
      if (folder.isDirty()) {
         folder.persist(transaction);
      }
      List<WorkDefinitionSheet> sheets = getWorkDefinitionSheets();
      Set<String> stateNames = new HashSet<String>();
      importWorkDefinitionSheets(resultData, transaction, folder, sheets, stateNames);
      createStateNameArtifact(stateNames, folder, transaction);
      importTeamsAndAis(resultData, transaction, folder, sheets);
      transaction.execute();
   }

   private static Artifact createStateNameArtifact(Set<String> stateNames, Artifact folder, SkynetTransaction transaction) throws OseeCoreException {
      Artifact stateNameArt =
         ArtifactTypeManager.addArtifact(org.eclipse.osee.ats.api.data.AtsArtifactToken.WorkDef_State_Names,
            AtsUtil.getAtsBranchToken());
      stateNameArt.addAttribute(CoreAttributeTypes.GeneralStringData,
         org.eclipse.osee.framework.jdk.core.util.Collections.toString(",", stateNames));
      stateNameArt.persist(transaction);
      folder.addChild(stateNameArt);
      folder.persist(transaction);
      return stateNameArt;
   }

   public static void importWorkDefinitionSheets(XResultData resultData, SkynetTransaction transaction, Artifact folder, Collection<WorkDefinitionSheet> sheets, Set<String> stateNames) throws OseeCoreException {
      for (WorkDefinitionSheet sheet : sheets) {
         OseeLog.logf(Activator.class, Level.INFO, "Importing ATS Work Definitions [%s]", sheet.getName());
         Artifact artifact =
            AtsWorkDefinitionImporter.get().importWorkDefinitionSheetToDb(sheet, resultData, stateNames, transaction);
         if (artifact != null) {
            folder.addChild(artifact);
            artifact.persist(transaction);
         }
      }
   }

   public static void importTeamsAndAis(XResultData resultData, SkynetTransaction transaction, Artifact folder, Collection<WorkDefinitionSheet> sheets) throws OseeCoreException {
      for (WorkDefinitionSheet sheet : sheets) {
         OseeLog.logf(Activator.class, Level.INFO, "Importing ATS Teams and AIs [%s]", sheet.getName());
         importAIsAndTeamsToDb(sheet, transaction);
      }
   }

   public static void importAIsAndTeamsToDatabase() throws OseeCoreException {
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranch(), "Import ATS AIs and Team Definitions");
      for (WorkDefinitionSheet sheet : getWorkDefinitionSheets()) {
         OseeLog.logf(Activator.class, Level.INFO, "Importing ATS AIs and Teams sheet [%s]", sheet.getName());
         importAIsAndTeamsToDb(sheet, transaction);
      }
      transaction.execute();
   }

   public static void importAIsAndTeamsToDb(WorkDefinitionSheet sheet, SkynetTransaction transaction) throws OseeCoreException {
      String modelName = sheet.getFile().getName();
      AtsDsl atsDsl = AtsDslUtil.getFromSheet(modelName, sheet);
      ImportAIsAndTeamDefinitionsToDb importer = new ImportAIsAndTeamDefinitionsToDb(modelName, atsDsl, transaction);
      importer.execute();
   }

   public static List<WorkDefinitionSheet> getWorkDefinitionSheets() {
      List<WorkDefinitionSheet> sheets = new ArrayList<WorkDefinitionSheet>();
      sheets.add(new WorkDefinitionSheet(WORK_DEF_TEAM_DEFAULT, getSupportFile(Activator.PLUGIN_ID,
         "support/WorkDef_Team_Default.ats")));
      sheets.add(new WorkDefinitionSheet("WorkDef_Task_Default", getSupportFile(Activator.PLUGIN_ID,
         "support/WorkDef_Task_Default.ats")));
      sheets.add(new WorkDefinitionSheet("WorkDef_Review_Decision", getSupportFile(Activator.PLUGIN_ID,
         "support/WorkDef_Review_Decision.ats")));
      sheets.add(new WorkDefinitionSheet("WorkDef_Review_PeerToPeer", getSupportFile(Activator.PLUGIN_ID,
         "support/WorkDef_Review_PeerToPeer.ats")));
      sheets.add(new WorkDefinitionSheet("WorkDef_Team_Simple", getSupportFile(Activator.PLUGIN_ID,
         "support/WorkDef_Team_Simple.ats")));
      sheets.add(new WorkDefinitionSheet("WorkDef_Goal",
         getSupportFile(Activator.PLUGIN_ID, "support/WorkDef_Goal.ats")));
      for (IAtsWorkDefinitionSheetProvider provider : getProviders()) {
         sheets.addAll(provider.getWorkDefinitionSheets());
      }
      return sheets;
   }

   public static File getSupportFile(String pluginId, String filename) {
      try {
         PluginUtil util = new PluginUtil(pluginId);
         return util.getPluginFile(filename);
      } catch (IOException ex) {
         OseeLog.logf(Activator.class, Level.SEVERE, ex, "Unable to access work definition sheet [%s]", filename);
      }
      return null;
   }

   /*
    * due to lazy initialization, this function is non-reentrant therefore, the synchronized keyword is necessary
    */
   private synchronized static Set<IAtsWorkDefinitionSheetProvider> getProviders() {
      if (teamWorkflowExtensionItems != null) {
         return teamWorkflowExtensionItems;
      }
      teamWorkflowExtensionItems = new HashSet<IAtsWorkDefinitionSheetProvider>();

      IExtensionPoint point =
         Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.osee.ats.AtsWorkDefinitionSheetProvider");
      if (point == null) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP,
            "Can't access AtsWorkDefinitionSheetProvider extension point");
         return teamWorkflowExtensionItems;
      }
      IExtension[] extensions = point.getExtensions();
      for (IExtension extension : extensions) {
         IConfigurationElement[] elements = extension.getConfigurationElements();
         String classname = null;
         String bundleName = null;
         for (IConfigurationElement el : elements) {
            if (el.getName().equals("AtsWorkDefinitionSheetProvider")) {
               classname = el.getAttribute("classname");
               bundleName = el.getContributor().getName();
               if (classname != null && bundleName != null) {
                  Bundle bundle = Platform.getBundle(bundleName);
                  try {
                     Class<?> taskClass = bundle.loadClass(classname);
                     Object obj = taskClass.newInstance();
                     teamWorkflowExtensionItems.add((IAtsWorkDefinitionSheetProvider) obj);
                  } catch (Exception ex) {
                     OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP,
                        "Error loading AtsWorkDefinitionSheetProvider extension", ex);
                  }
               }
            }
         }
      }
      return teamWorkflowExtensionItems;
   }

}
