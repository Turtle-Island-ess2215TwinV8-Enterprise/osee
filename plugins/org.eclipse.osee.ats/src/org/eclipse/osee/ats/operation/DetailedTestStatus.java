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

package org.eclipse.osee.ats.operation;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osee.ats.core.config.IAtsProgram;
import org.eclipse.osee.ats.core.task.TaskArtifact;
import org.eclipse.osee.ats.core.team.TeamState;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.type.AtsAttributeTypes;
import org.eclipse.osee.ats.core.version.VersionArtifact;
import org.eclipse.osee.ats.core.workflow.PercentCompleteTotalUtil;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.widgets.XAtsProgramComboWidget;
import org.eclipse.osee.define.traceability.RequirementTraceabilityData;
import org.eclipse.osee.define.traceability.ScriptTraceabilityOperation;
import org.eclipse.osee.define.traceability.TraceabilityProviderOperation;
import org.eclipse.osee.define.traceability.report.RequirementStatus;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.jdk.core.type.CompositeKeyHashMap;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.io.CharBackedInputStream;
import org.eclipse.osee.framework.jdk.core.util.io.xml.ExcelXmlWriter;
import org.eclipse.osee.framework.jdk.core.util.io.xml.ISheetWriter;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.AIFile;
import org.eclipse.osee.framework.plugin.core.util.OseeData;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.widgets.XArtifactList;
import org.eclipse.osee.framework.ui.skynet.widgets.XBranchSelectWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.ote.define.artifacts.TestRunOperator;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Ryan D. Brooks
 */
public class DetailedTestStatus extends AbstractBlam {
   private static final Pattern taskNamePattern = Pattern.compile("(?:\"([^\"]+)\")? for \"([^\"]+)\"");
   private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
   private final Matcher taskNameMatcher = taskNamePattern.matcher("");
   private CharBackedInputStream charBak;
   private ISheetWriter excelWriter;
   //                            reqName  legacyId
   private final CompositeKeyHashMap<String, String, RequirementStatus> reqTaskMap =
      new CompositeKeyHashMap<String, String, RequirementStatus>();
   private final StringBuilder sumFormula = new StringBuilder(500);
   private int rowNumber;
   private HashCollection<Artifact, String> requirementToCodeUnitsMap;
   private final HashMap<String, String> testProcedureInfo = new HashMap<String, String>();
   private final HashCollection<String, IBasicUser> legacyIdToImplementers = new HashCollection<String, IBasicUser>();
   private final HashMap<String, Artifact> testRunArtifacts = new HashMap<String, Artifact>();
   private final HashMap<String, String> scriptCategories = new HashMap<String, String>();
   private final HashSet<IBasicUser> testPocs = new HashSet<IBasicUser>();
   private final HashSet<String> requirementPocs = new HashSet<String>();
   private final ArrayList<String[]> statusLines = new ArrayList<String[]>();
   private final ArrayList<RequirementStatus> statuses = new ArrayList<RequirementStatus>(100);
   private HashCollection<String, Artifact> requirementNameToTestProcedures;
   private Collection<VersionArtifact> versions;

   private XBranchSelectWidget requirementsBranchWidget;
   private XBranchSelectWidget scriptsBranchWidget;
   private XBranchSelectWidget testProcedureBranchWidget;
   private XArtifactList versionsListViewer;

   private IOseeBranch selectedBranch;
   private IAtsProgram selectedProgram;

   private enum Index {
      Category,
      TEST_POC,
      PARTITION,
      SUBSYSTEM,
      REQUIREMENT_NAME,
      QUALIFICATION_METHOD,
      REQUIREMENT_POC,
      SW_ENHANCEMENT,
      TEST_PROCEDURE,
      TEST_SCRIPT,
      RUN_DATE,
      TOTAL_TP,
      FAILED_TP,
      HOURS_REMAINING
   };

   private class BranchChangeListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection versionArtifactSelection =
            (IStructuredSelection) event.getSelectionProvider().getSelection();
         Iterator<?> iter = versionArtifactSelection.iterator();
         if (iter.hasNext()) {
            VersionArtifact versionArtifact = (VersionArtifact) iter.next();

            try {
               selectedBranch = versionArtifact.getBaselineBranch();

               requirementsBranchWidget.setSelection(selectedBranch);
               scriptsBranchWidget.setSelection(selectedBranch);
               testProcedureBranchWidget.setSelection(selectedBranch);
            } catch (OseeCoreException ex) {
               OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      }
   }
   private final BranchChangeListener branchSelectionListener = new BranchChangeListener();

   private class ProgramSelectionListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelectionProvider().getSelection();

         Iterator<?> iter = selection.iterator();
         if (iter.hasNext()) {
            selectedProgram = (IAtsProgram) iter.next();
            selectedBranch = null;

            try {
               Collection<VersionArtifact> versionArtifacts =
                  selectedProgram.getTeamDefHoldingVersions().getVersionsArtifacts();
               versionsListViewer.setInputArtifacts(versionArtifacts);

               requirementsBranchWidget.setSelection(null);
               scriptsBranchWidget.setSelection(null);
               testProcedureBranchWidget.setSelection(null);

               versionsListViewer.addSelectionChangedListener(branchSelectionListener);
            } catch (OseeCoreException ex) {
               OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      }
   }

   private void init() throws IOException {
      charBak = new CharBackedInputStream();
      excelWriter = new ExcelXmlWriter(charBak.getWriter());
      reqTaskMap.clear();
      testRunArtifacts.clear();
      testProcedureInfo.clear();
      legacyIdToImplementers.clear();
      rowNumber = 7;
   }

   private String getScriptName(String scriptPath) {
      return scriptPath.substring(scriptPath.lastIndexOf(File.separatorChar) + 1,
         scriptPath.length() - ".java".length());
   }

   private void loadTestRunArtifacts(IOseeBranch scriptsBranch) throws OseeCoreException {
      Collection<Artifact> testRuns =
         ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.TestRun, scriptsBranch, DeletionFlag.EXCLUDE_DELETED);

      for (Artifact testRun : testRuns) {
         String shortName = testRun.getName();
         shortName = shortName.substring(shortName.lastIndexOf('.') + 1);
         Artifact previousTestRun = testRunArtifacts.put(shortName, testRun);

         if (previousTestRun != null) {
            Date date = new TestRunOperator(testRun).getEndDate();
            Date previousDate = new TestRunOperator(previousTestRun).getEndDate();
            if (previousDate.after(date)) {
               testRunArtifacts.put(shortName, previousTestRun);
            }
         }
      }
   }

   @Override
   public void runOperation(VariableMap variableMap, final IProgressMonitor monitor) throws Exception {
      if (!blamReadyToExecute()) {
         monitor.setCanceled(true);
         return;
      }

      IOseeBranch requirementsBranch = variableMap.getBranch("Requirements Branch");
      IOseeBranch scriptsBranch = variableMap.getBranch("Scripts Branch");
      IOseeBranch procedureBranch = variableMap.getBranch("Test Procedure Branch");

      File scriptDir = new File(variableMap.getString("Script Root Directory"));
      versions = variableMap.getCollection(VersionArtifact.class, "Versions");
      init();

      loadTestRunArtifacts(scriptsBranch);

      // Load Requirements Data
      TraceabilityProviderOperation provider = new ScriptTraceabilityOperation(scriptDir, requirementsBranch, false);
      RequirementTraceabilityData traceabilityData =
         new RequirementTraceabilityData(requirementsBranch, procedureBranch, provider);

      IStatus status = traceabilityData.initialize(monitor);
      switch (status.getSeverity()) {
         case IStatus.OK:
            requirementToCodeUnitsMap = traceabilityData.getRequirementsToCodeUnits();
            requirementNameToTestProcedures = traceabilityData.getRequirementNameToTestProcedures();

            loadReqTaskMap();

            writeStatusSheet(traceabilityData.getAllSwRequirements());

            writeTestScriptSheet(traceabilityData.getCodeUnits());

            excelWriter.endWorkbook();
            IFile iFile = OseeData.getIFile("Block_III_Test_Status_" + Lib.getDateTimeString() + ".xml");
            AIFile.writeToFile(iFile, charBak);
            Program.launch(iFile.getLocation().toOSString());
            break;
         case IStatus.CANCEL:
            monitor.setCanceled(true);
            break;
         default:
            throw new OseeCoreException(status.getMessage(), status.getException());
      }
   }

   private boolean blamReadyToExecute() {
      final List<String> items = new ArrayList<String>();

      if (selectedProgram == null) {
         items.add("program");
      }
      if (selectedBranch == null) {
         items.add("branch");
      }

      boolean ready = items.isEmpty();
      if (!ready) {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               MessageDialog.openInformation(Displays.getActiveShell(), "Problem",
                  String.format("Select a %s ...", Strings.buildStatment(items)));
            }
         });
      }
      return ready;
   }

   private void writeTestScriptSheet(Set<String> scripts) throws IOException, OseeCoreException {
      excelWriter.startSheet("Scripts", 8);
      excelWriter.writeRow("Category", CoreArtifactTypes.TestCase.getName(), "Run Date", "Total Test Points",
         "Failed Test Points", "Duration", "Aborted", "Last Author");

      for (String scriptPath : scripts) {
         String scriptName = getScriptName(scriptPath);
         Artifact testRunArtifact = testRunArtifacts.get(scriptName);
         String totalTestPoints = null;
         String failedTestPoints = null;
         String category = scriptCategories.get(scriptPath);
         String runDate = null;
         String duration = null;
         String aborated = null;
         String lastAuthor = null;
         if (testRunArtifact != null) {
            TestRunOperator runOperator = new TestRunOperator(testRunArtifact);
            try {
               runDate = dateFormatter.format(runOperator.getEndDate());
            } catch (Exception ex) {
               OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
            }
            totalTestPoints = String.valueOf(runOperator.getTotalTestPoints());
            failedTestPoints = String.valueOf(runOperator.getTestPointsFailed());
            duration = runOperator.getRunDuration();
            aborated = runOperator.wasAborted() ? "aborted" : null;
            lastAuthor = runOperator.getLastAuthor();
         }

         excelWriter.writeRow(category, scriptName, runDate, totalTestPoints, failedTestPoints, duration, aborated,
            lastAuthor);
      }

      excelWriter.endSheet();
   }

   private void writeStatusSheet(Collection<Artifact> requirements) throws IOException, OseeCoreException {
      excelWriter.startSheet("SW Req Status", 256);
      excelWriter.writeRow(null, null, null, null, "Hours per UI per RPCR", "=4");
      excelWriter.writeRow(null, null, null, null, "Hours to integrate all scripts for a UI", "=11");
      excelWriter.writeRow(null, null, null, null, "Hours to develop a new script", "=20");
      excelWriter.writeRow();
      excelWriter.writeRow();
      excelWriter.writeRow("Category", "Test POCs", CoreAttributeTypes.Partition.getName(),
         CoreAttributeTypes.Subsystem.getName(), "Requirement Name", CoreAttributeTypes.QualificationMethod.getName(),
         "Requirement POCs", "SW Enhancement", CoreArtifactTypes.TestProcedure.getName(),
         CoreArtifactTypes.TestCase.getName(), "Run Date", "Total Test Points", "Failed Test Points",
         "Hours Remaining", "RPCR", "Hours", "Resolution by Partition");

      for (Artifact requirement : requirements) {
         writeRequirementStatusLines(requirement);
      }

      excelWriter.endSheet();
   }

   private void setScriptCategories(Artifact requirement, Collection<String> scripts) {
      try {
         String reqCategory = requirement.getSoleAttributeValue(CoreAttributeTypes.Category, "");

         for (String scriptPath : scripts) {
            String scriptCategory = scriptCategories.get(scriptPath);
            if (scriptCategory == null || scriptCategory.compareTo(reqCategory) > 0) {
               scriptCategories.put(scriptPath, reqCategory);
            }
         }
      } catch (Exception ex) {
         // really don't care because we handle unknown priorities later
      }
   }

   private void processRpcrStatuses(Artifact requirement, String[] statusLine) {
      int columnIndex = Index.HOURS_REMAINING.ordinal() + 1;

      sumFormula.append(",");
      for (String requirementName : getAliases(requirement)) {
         Collection<RequirementStatus> tempStatuses = reqTaskMap.getValues(requirementName);

         if (tempStatuses != null) {
            statuses.clear();
            statuses.addAll(tempStatuses);
            java.util.Collections.sort(statuses);
            for (int i = statuses.size() - 1; i >= 0; i--) {
               RequirementStatus status = statuses.get(i);
               statusLine[columnIndex++] = status.getLegacyId();
               statusLine[columnIndex++] = "=R1C6*(100-" + status.getRolledupPercentComplete() + ")/100";

               sumFormula.append("RC");
               sumFormula.append(columnIndex);
               sumFormula.append(",");

               statusLine[columnIndex++] = status.getPartitionStatuses();

               Collection<IBasicUser> implementers = legacyIdToImplementers.getValues(status.getLegacyId());
               if (implementers != null) {
                  for (IBasicUser implementer : implementers) {
                     requirementPocs.add(implementer.getName());
                  }
               }
               testPocs.addAll(status.getTestPocs());
            }
         }
      }

      sumFormula.setCharAt(sumFormula.length() - 1, ')');
   }

   private void processTestScriptsAndProcedures(Artifact requirement, String[] statusLine) throws OseeCoreException {
      Collection<String> scripts = requirementToCodeUnitsMap.getValues(requirement);
      if (scripts == null) {
         if (requirement.isOfType(CoreArtifactTypes.IndirectSoftwareRequirement)) {
            statusLine[Index.TEST_SCRIPT.ordinal()] = requirement.getArtifactTypeName();
            sumFormula.insert(0, "=sum(0");
            statusLine[Index.HOURS_REMAINING.ordinal()] = sumFormula.toString();
         } else {
            statusLine[Index.TEST_SCRIPT.ordinal()] = "No script found";
            statusLine[Index.HOURS_REMAINING.ordinal()] = "=R3C6";
         }
         rowNumber++;
         statusLines.add(statusLine);
      } else {
         setScriptCategories(requirement, scripts);
         int testPointTotalForScripts = 0;
         int testPointFailsForScripts = 0;

         for (String script : scripts) {
            String scriptName = getScriptName(script);
            statusLine[Index.TEST_SCRIPT.ordinal()] = scriptName;
            Artifact testRunArtifact = testRunArtifacts.get(scriptName);
            if (testRunArtifact != null) {
               TestRunOperator runOperator = new TestRunOperator(testRunArtifact);

               try {
                  statusLine[Index.RUN_DATE.ordinal()] = dateFormatter.format(runOperator.getEndDate());
               } catch (Exception ex) {
                  OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
               }
               if (runOperator.wasAborted()) {
                  statusLine[Index.TOTAL_TP.ordinal()] = "Aborted";
                  statusLine[Index.FAILED_TP.ordinal()] = "Aborted";
               } else {
                  int individualTestPointsFailed = runOperator.getTestPointsFailed();
                  int individualTestPointTotal = runOperator.getTotalTestPoints();

                  statusLine[Index.TOTAL_TP.ordinal()] = String.valueOf(individualTestPointTotal);
                  statusLine[Index.FAILED_TP.ordinal()] = String.valueOf(individualTestPointsFailed);

                  testPointFailsForScripts += individualTestPointsFailed;
                  testPointTotalForScripts += individualTestPointTotal;
               }
            }
            rowNumber++;
            statusLines.add(statusLine);
            String[] oldStatusLine = statusLine;
            statusLine = new String[100];
            initStatusLine(oldStatusLine, statusLine);
         }

         String failRatio = "1";
         if (testPointTotalForScripts != 0) {
            failRatio = testPointFailsForScripts + "/" + testPointTotalForScripts;
         }
         sumFormula.insert(0, "=sum(R2C6*" + failRatio);
         statusLines.get(0)[Index.HOURS_REMAINING.ordinal()] = sumFormula.toString();
      }

      addTestProcedureNames(requirement.getName());
   }

   private void writeRequirementStatusLines(Artifact requirement) throws OseeCoreException, IOException {
      statusLines.clear();
      testPocs.clear();
      requirementPocs.clear();
      sumFormula.delete(0, 99999);
      String[] statusLine = new String[100];

      processRpcrStatuses(requirement, statusLine);

      statusLine[Index.Category.ordinal()] = requirement.getSoleAttributeValue(CoreAttributeTypes.Category, "");
      if (requirement.isOfType(CoreArtifactTypes.IndirectSoftwareRequirement)) {
         statusLine[Index.Category.ordinal()] = "I";
      }

      statusLine[Index.TEST_POC.ordinal()] = Artifacts.toString("; ", testPocs);
      statusLine[Index.PARTITION.ordinal()] = requirement.getAttributesToString(CoreAttributeTypes.Partition);
      statusLine[Index.SUBSYSTEM.ordinal()] = requirement.getSoleAttributeValue(CoreAttributeTypes.Subsystem, "");
      statusLine[Index.REQUIREMENT_NAME.ordinal()] = requirement.getName();
      statusLine[Index.QUALIFICATION_METHOD.ordinal()] =
         requirement.getAttributesToStringSorted(CoreAttributeTypes.QualificationMethod);
      statusLine[Index.REQUIREMENT_POC.ordinal()] = Collections.toString(",", requirementPocs);

      Collection<RequirementStatus> reqStats = reqTaskMap.getValues(requirement.getName());
      statusLine[Index.SW_ENHANCEMENT.ordinal()] =
         reqStats.isEmpty() ? "" : reqStats.iterator().next().getSwEnhancement();

      processTestScriptsAndProcedures(requirement, statusLine);

      for (String[] line : statusLines) {
         excelWriter.writeRow(line);
      }
   }

   private void addTestProcedureNames(String requirementName) {
      Collection<Artifact> testProcedures = requirementNameToTestProcedures.getValues(requirementName);
      if (testProcedures != null) {
         int index = 0;
         String[] firstStatusLine = statusLines.get(index);
         String lastTestProcedure = null;
         for (Artifact testProcedure : testProcedures) {
            if (index < statusLines.size()) {
               statusLines.get(index++)[Index.TEST_PROCEDURE.ordinal()] = testProcedure.getName();
               lastTestProcedure = testProcedure.getName();
            } else {
               String[] statusLine = new String[Index.RUN_DATE.ordinal()];
               initStatusLine(firstStatusLine, statusLine);
               statusLine[Index.TEST_PROCEDURE.ordinal()] = testProcedure.getName();
               statusLines.add(statusLine);
            }
         }
         while (index < statusLines.size()) {
            statusLines.get(index++)[Index.TEST_PROCEDURE.ordinal()] = lastTestProcedure;
         }
      }
   }

   private void initStatusLine(String[] oldStatusLine, String[] newStatusLine) {
      System.arraycopy(oldStatusLine, 0, newStatusLine, 0, Index.RUN_DATE.ordinal());
   }

   private void loadReqTaskMap() throws Exception {
      for (VersionArtifact version : versions) {
         for (TeamWorkFlowArtifact workflow : version.getTargetedForTeamArtifacts()) {
            loadTasksFromWorkflow(workflow);
         }
      }
   }

   @Override
   public void widgetCreating(XWidget xWidget, FormToolkit toolkit, Artifact art, DynamicXWidgetLayout dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) {
      String widgetLabel = xWidget.getLabel();

      if (widgetLabel.equals("Versions")) {
         versionsListViewer = (XArtifactList) xWidget;
      } else if (widgetLabel.equals("Requirements Branch")) {
         requirementsBranchWidget = (XBranchSelectWidget) xWidget;
      } else if (widgetLabel.equals("Scripts Branch")) {
         scriptsBranchWidget = (XBranchSelectWidget) xWidget;
      } else if (widgetLabel.equals("Test Procedure Branch")) {
         testProcedureBranchWidget = (XBranchSelectWidget) xWidget;
      }
   }

   @Override
   public void widgetCreated(XWidget xWidget, FormToolkit toolkit, Artifact art, DynamicXWidgetLayout dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) {
      String widgetName = xWidget.getLabel();
      if (widgetName.equals("Program")) {
         XAtsProgramComboWidget programWidget = (XAtsProgramComboWidget) xWidget;
         programWidget.getComboViewer().addSelectionChangedListener(new ProgramSelectionListener());
      }
   }

   private void loadTasksFromWorkflow(TeamWorkFlowArtifact workflow) throws OseeCoreException {
      Collection<TaskArtifact> tasks = workflow.getTaskArtifacts(TeamState.Implement);
      String legacyId = workflow.getSoleAttributeValue(AtsAttributeTypes.LegacyPcrId, "");

      List<IBasicUser> implementers = workflow.getImplementers();
      legacyIdToImplementers.put(legacyId, implementers);

      for (TaskArtifact task : tasks) {

         taskNameMatcher.reset(task.getName());
         if (taskNameMatcher.find()) {
            String requirementName = taskNameMatcher.group(2);
            RequirementStatus requirementStatus = reqTaskMap.get(requirementName, legacyId);
            if (requirementStatus == null) {
               requirementStatus =
                  new RequirementStatus(requirementName, legacyId, workflow.getSoleAttributeValueAsString(
                     AtsAttributeTypes.SwEnhancement, ""));
               reqTaskMap.put(requirementName, legacyId, requirementStatus);
            }

            int percentComplete = PercentCompleteTotalUtil.getPercentCompleteTotal(task);
            requirementStatus.addPartitionStatus(percentComplete, taskNameMatcher.group(1),
               task.getSoleAttributeValue(AtsAttributeTypes.Resolution, ""));
            requirementStatus.setTestPocs(task.getImplementers());
         } else {
            System.out.println("odd task:  " + task.getName());
         }
      }
   }

   /*
    * returns a collection of all the names the given artifact has ever had
    */
   private Collection<String> getAliases(Artifact artifact) {
      // TODO: this method should return history of names
      ArrayList<String> aliases = new ArrayList<String>(1);
      aliases.add(artifact.getName());
      return aliases;
   }

   @Override
   public String getXWidgetsXml() throws OseeCoreException {
      return getXWidgetsXmlFromUiFile(getClass().getSimpleName(), AtsPlugin.PLUGIN_ID);
   }

   @Override
   public String getDescriptionUsage() {
      return "Generates an excel spreadsheet with detailed test status for scripts and procedures";
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("OTE");
   }
}