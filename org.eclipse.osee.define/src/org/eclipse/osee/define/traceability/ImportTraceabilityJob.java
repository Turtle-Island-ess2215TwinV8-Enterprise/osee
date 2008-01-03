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
package org.eclipse.osee.define.traceability;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.define.DefinePlugin;
import org.eclipse.osee.framework.jdk.core.type.CountingMap;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.io.CharBackedInputStream;
import org.eclipse.osee.framework.jdk.core.util.io.xml.excel.ExcelXmlWriter;
import org.eclipse.osee.framework.jdk.core.util.io.xml.excel.ISheetWriter;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.attribute.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.WordAttribute;
import org.eclipse.osee.framework.skynet.core.util.WordUtil;
import org.eclipse.osee.framework.ui.plugin.util.AIFile;
import org.eclipse.osee.framework.ui.plugin.util.OseeData;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.swt.program.Program;

/**
 * @author Ryan D. Brooks
 */
public class ImportTraceabilityJob extends Job {
   private static final Pattern ofpReqTraceP = Pattern.compile("\\^SRS\\s*([^;\n\r]+);");
   private final Matcher ofpReqTraceMatcher;
   private static final Pattern scriptReqTraceP =
         Pattern.compile("addTraceability\\s*\\(\\\"(?:SubDD|SRS|CSID)?\\s*([^\\\"]+)\\\"");
   private final Matcher scriptReqTraceMatcher;
   private static final Pattern structuredReqNameP = Pattern.compile("\\[?(\\{[^\\}]+\\})(.*)");
   private static final Pattern filePattern = Pattern.compile(".*\\.(java|ada|ads|adb|c|h)");
   private static final Pattern embeddedVolumePattern = Pattern.compile("\\{\\d+ (.*)\\}");
   private static final Pattern invalidTraceMarkPattern = Pattern.compile("(\\[[A-Za-z]|USES_).*");
   private static final Pattern curlyBracesPattern = Pattern.compile("[{}]");

   private static final ArtifactPersistenceManager artifactManager = ArtifactPersistenceManager.getInstance();
   private final File file;
   private final Branch branch;
   private final ArrayList<String> noTraceabilityFiles;
   private final HashMap<String, Artifact> softwareReqs;
   private final HashMap<String, Artifact> indirectReqs;
   private final CountingMap<Artifact> reqsTraceCounts;
   private final HashCollection<Artifact, String> requirementToCodeUnitsMap;
   private final CharBackedInputStream charBak;
   private final ISheetWriter excelWriter;
   private int pathPrefixLength;

   public ImportTraceabilityJob(File file, Branch branch) throws IllegalArgumentException, CoreException, SQLException, IOException {
      super("Importing Traceability");
      this.file = file;
      this.branch = branch;
      noTraceabilityFiles = new ArrayList<String>(200);
      softwareReqs = new HashMap<String, Artifact>(3500);
      indirectReqs = new HashMap<String, Artifact>(700);
      reqsTraceCounts = new CountingMap<Artifact>();
      requirementToCodeUnitsMap = new HashCollection<Artifact, String>(false, LinkedList.class);
      charBak = new CharBackedInputStream();
      excelWriter = new ExcelXmlWriter(charBak.getWriter());
      ofpReqTraceMatcher = ofpReqTraceP.matcher("");
      scriptReqTraceMatcher = scriptReqTraceP.matcher("");
   }

   /*
    * (non-Javadoc)
    * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
    */
   public IStatus run(IProgressMonitor monitor) {
      try {
         monitor.beginTask("Importing From " + file.getName(), 100);
         monitor.worked(1);

         monitor.subTask("Aquiring Software Requirements"); // bulk load for performance reasons
         for (Artifact artifact : artifactManager.getArtifactsFromSubtypeName("Software Requirement", branch)) {
            softwareReqs.put(getCanonicalReqName(artifact.getDescriptiveName()), artifact);
         }
         monitor.worked(30);

         monitor.subTask("Aquiring Indirect Software Requirements");
         for (Artifact artifact : artifactManager.getArtifactsFromSubtypeName("Indirect Software Requirement", branch)) {
            indirectReqs.put(getCanonicalReqName(artifact.getDescriptiveName()), artifact);
         }
         monitor.worked(7);

         excelWriter.startSheet("srs <--> code units", 6);
         excelWriter.writeRow("Req in DB", "Code Unit", "Requirement Name", "Requirement Trace Mark in Code");

         if (file.isFile()) {
            for (String path : Lib.readListFromFile(file, true)) {
               monitor.subTask(path);
               handleDirectory(new File(path));
            }
         } else if (file.isDirectory()) {
            handleDirectory(file);
         } else {
            throw new IllegalStateException("unexpected file system type");
         }

         excelWriter.endSheet();

         writeNoTraceFilesSheet();
         writeTraceCountsSheet();

         excelWriter.endWorkbook();
         IFile iFile = OseeData.getIFile("CodeUnit_To_SRS_Trace.xml");
         AIFile.writeToFile(iFile, charBak);
         Program.launch(iFile.getLocation().toOSString());

         monitor.done();
         return Status.OK_STATUS;
      } catch (Exception ex) {
         return new Status(Status.ERROR, DefinePlugin.PLUGIN_ID, -1, ex.getLocalizedMessage(), ex);
      }
   }

   private String getCanonicalReqName(String reqReference) {
      String canonicalReqReference = reqReference.toUpperCase();
      if (canonicalReqReference.endsWith(".")) {
         canonicalReqReference = canonicalReqReference.substring(0, canonicalReqReference.length() - 1);
      }

      Matcher embeddedVolumeMatcher = embeddedVolumePattern.matcher(canonicalReqReference);
      if (embeddedVolumeMatcher.find()) {
         canonicalReqReference = embeddedVolumeMatcher.group(1);
      }

      int lastCurlyBraceIndex = canonicalReqReference.lastIndexOf('}');
      if (lastCurlyBraceIndex > -1) {
         canonicalReqReference = canonicalReqReference.substring(0, lastCurlyBraceIndex);
      }

      canonicalReqReference = curlyBracesPattern.matcher(canonicalReqReference).replaceAll("");

      return canonicalReqReference;
   }

   private void handleDirectory(File directory) throws IOException, SQLException {
      if (directory == null || directory.getParentFile() == null) {
         OSEELog.logWarning(DefinePlugin.class, "The path " + directory + " is invalid.", true);
         return;
      }

      pathPrefixLength = directory.getParentFile().getAbsolutePath().length();

      for (File sourceFile : (List<File>) Lib.recursivelyListFiles(directory, filePattern)) {
         CharBuffer buf = Lib.fileToCharBuffer(sourceFile);
         Matcher reqTraceMatcher = getReqTraceMatcher(sourceFile);
         reqTraceMatcher.reset(buf);

         int matchCount = 0;
         String relativePath = sourceFile.getPath().substring(pathPrefixLength);
         while (reqTraceMatcher.find()) {
            handelReqTrace(relativePath, reqTraceMatcher.group(1));
            matchCount++;
         }
         if (matchCount == 0) {
            noTraceabilityFiles.add(relativePath);
         }
      }
   }

   private Matcher getReqTraceMatcher(File sourceFile) {
      if (sourceFile.getName().endsWith("java")) {
         return scriptReqTraceMatcher;
      }
      return ofpReqTraceMatcher;
   }

   private void writeNoTraceFilesSheet() throws IOException {
      excelWriter.startSheet("no match files", 1);
      for (String path : noTraceabilityFiles) {
         excelWriter.writeRow(path);
      }
      excelWriter.endSheet();
   }

   private void writeTraceCountsSheet() throws IOException, SQLException {
      excelWriter.startSheet("trace counts", 3);
      excelWriter.writeRow("SRS Requirement from Database", "Trace Count", "Partitions");
      excelWriter.writeRow("% requirement coverage", null, "=1-COUNTIF(C2,&quot;0&quot;)/COUNTA(C2)");
      StringBuffer partitionStrB = new StringBuffer(100);
      for (Artifact artifact : softwareReqs.values()) {
         Collection<Attribute> partitions = artifact.getAttributeManager("Partition").getAttributes();

         for (Attribute attribute : partitions) {
            partitionStrB.append(attribute.getStringData());
            partitionStrB.append(',');
         }

         excelWriter.writeRow(artifact.getDescriptiveName(), String.valueOf(reqsTraceCounts.get(artifact)),
               partitionStrB.substring(0, partitionStrB.length() - 1));
         partitionStrB.delete(0, 99999);
      }

      excelWriter.endSheet();
   }

   private void handelReqTrace(String path, String traceMark) throws SQLException, IOException {
      String foundStr;
      Artifact reqArtifact = null;

      Matcher invalidTraceMarkMatcher = invalidTraceMarkPattern.matcher(traceMark);
      if (invalidTraceMarkMatcher.matches()) {
         foundStr = "invalid trace mark";
      } else {
         reqArtifact = getRequirementArtifact(traceMark);
         if (reqArtifact == null) {
            Matcher structuredReqNameMatcher = structuredReqNameP.matcher(traceMark);
            if (structuredReqNameMatcher.matches()) {
               reqArtifact = getRequirementArtifact(structuredReqNameMatcher.group(1));

               if (reqArtifact == null) {
                  foundStr = "no match in DB";
               } else {
                  // for local data and procedures search requirement text for traceMark
                  // example local data [{SUBSCRIBER}.ID] and example procedure {CURSOR_ACKNOWLEDGE}.NORMAL
                  String textContent =
                        WordUtil.textOnly(reqArtifact.getSoleAttributeValue(WordAttribute.CONTENT_NAME)).toUpperCase();
                  if (textContent.contains(getCanonicalReqName(structuredReqNameMatcher.group(2)))) {
                     foundStr = "req body match";
                  } else {
                     foundStr = "paritial match";
                  }
               }
            } else {
               foundStr = "no match in DB";
            }
         } else {
            foundStr = fullMatch(reqArtifact);
         }
      }

      String name = null;
      if (reqArtifact != null) {
         name = reqArtifact.getDescriptiveName();
         requirementToCodeUnitsMap.put(reqArtifact, path);
      }
      excelWriter.writeRow(foundStr, path, name, traceMark);
   }

   private Artifact getRequirementArtifact(String traceMark) {
      String canonicalTraceMark = getCanonicalReqName(traceMark);
      Artifact reqArtifact = softwareReqs.get(canonicalTraceMark);
      if (reqArtifact == null) {
         reqArtifact = indirectReqs.get(canonicalTraceMark);
      }
      return reqArtifact;
   }

   private String fullMatch(Artifact reqArtifact) {
      reqsTraceCounts.put(reqArtifact);
      return "full match";
   }

   public HashCollection<Artifact, String> getRequirementToCodeUnitsMap() {
      return requirementToCodeUnitsMap;
   }
}