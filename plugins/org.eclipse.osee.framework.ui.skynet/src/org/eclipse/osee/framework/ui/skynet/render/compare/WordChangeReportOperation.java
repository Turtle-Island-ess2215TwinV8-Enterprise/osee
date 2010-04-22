package org.eclipse.osee.framework.ui.skynet.render.compare;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.OseeData;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.change.ArtifactDelta;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.preferences.MsWordPreferencePage;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RenderingUtil;
import org.eclipse.osee.framework.ui.skynet.render.VbaWordDiffGenerator;
import org.eclipse.osee.framework.ui.skynet.render.WordImageChecker;
import org.eclipse.osee.framework.ui.skynet.util.WordUiUtil;

public final class WordChangeReportOperation extends AbstractOperation {
   private final Collection<ArtifactDelta> artifactsToCompare;

   private final String reportDirName;
   private final boolean isSuppressWord;
   private final IAttributeType attributeType;
   private final ArtifactDeltaToFileConverter converter;
   private IFolder changeReportFolder;

   public WordChangeReportOperation(Collection<ArtifactDelta> artifactsToCompare, ArtifactDeltaToFileConverter converter, String reportDirName, boolean isSuppressWord) {
      super("Word Change Report", SkynetGuiPlugin.PLUGIN_ID);
      this.converter = converter;
      this.artifactsToCompare = artifactsToCompare;

      this.isSuppressWord = isSuppressWord;
      this.reportDirName = Strings.isValid(reportDirName) ? reportDirName : GUID.create();

      this.attributeType = CoreAttributeTypes.WORD_TEMPLATE_CONTENT;
   }

   private IFolder getChangeReportFolder() throws OseeCoreException {
      if (changeReportFolder == null) {
         RenderingUtil.ensureRenderFolderExists(PresentationType.DIFF);
         changeReportFolder = OseeData.getFolder(".diff/" + reportDirName);
      }
      return changeReportFolder;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      if (!artifactsToCompare.isEmpty()) {
         double workPercentage = 0.70 / artifactsToCompare.size();

         VbaWordDiffGenerator generator = new VbaWordDiffGenerator();
         generator.initialize(false, false);

         String baseFileStr = getChangeReportFolder().getLocation().toOSString();

         Set<Artifact> artifacts = new HashSet<Artifact>();
         int countSuccessful = 0;

         for (ArtifactDelta delta : artifactsToCompare) {
            checkForCancelledStatus(monitor);

            try {
               //Remove tracked changes and display image diffs
               Pair<String, Boolean> originalValue = null;
               Pair<String, Boolean> newAnnotationValue = null;

               //Check for tracked changes
               artifacts.clear();
               artifacts.addAll(RenderingUtil.checkForTrackedChangesOn(delta.getStartArtifact()));
               artifacts.addAll(RenderingUtil.checkForTrackedChangesOn(delta.getEndArtifact()));

               if (!artifacts.isEmpty()) {
                  if (RenderingUtil.arePopupsAllowed()) {
                     WordUiUtil.displayWarningMessageDialog("Diff Artifacts Warning",
                           "Detected tracked changes for some artifacts. Please refer to the results HTML report.");
                     WordUiUtil.displayTrackedChangesOnArtifacts(artifacts);
                  }
                  continue;
               }

               Artifact baseArtifact = delta.getStartArtifact();
               Artifact newerArtifact = delta.getEndArtifact();

               if (baseArtifact == null && newerArtifact == null) {
                  throw new OseeArgumentException("baseVersion and newerVersion can't both be null.");
               }

               Attribute<String> baseContent = getWordContent(baseArtifact, attributeType);
               Attribute<String> newerContent = getWordContent(newerArtifact, attributeType);

               if (!UserManager.getUser().getBooleanSetting(MsWordPreferencePage.IDENTFY_IMAGE_CHANGES)) {
                  originalValue = WordImageChecker.checkForImageDiffs(baseContent, newerContent);
               }
               Pair<IFile, IFile> fileDeltas = converter.convertToFile(PresentationType.DIFF, delta);

               WordImageChecker.restoreOriginalValue(baseContent, originalValue);
               WordImageChecker.restoreOriginalValue(newerContent, newAnnotationValue);

               monitor.setTaskName("Adding to Diff Script: " + (newerArtifact == null ? "Unnamed Artifact" : newerArtifact.getName()));

               String localFileName = baseFileStr + "/" + GUID.create() + ".xml";
               generator.addComparison(fileDeltas.getFirst(), fileDeltas.getSecond(), localFileName, false);

               countSuccessful++;
            } catch (OseeCoreException ex) {
               OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            } finally {
               monitor.worked(calculateWork(workPercentage));
            }
         }

         checkForCancelledStatus(monitor);
         if (countSuccessful > 0) {
            monitor.setTaskName("Running Diff Script");
            generator.finish(baseFileStr + "/compareDocs.vbs", !isSuppressWord);
         }
         monitor.worked(calculateWork(0.20));
         checkForCancelledStatus(monitor);
         // Let the user know that these artifacts had tracked changes on and we are not handling them
         // Also, list these artifacts in an artifact explorer
         if (!artifacts.isEmpty() && RenderingUtil.arePopupsAllowed()) {
            WordUiUtil.displayWarningMessageDialog("Diff Artifacts Warning",
                  "Detected tracked changes for some artifacts. Please refer to the results HTML report.");
            WordUiUtil.displayTrackedChangesOnArtifacts(artifacts);
         }
         monitor.worked(calculateWork(0.10));
      } else {
         monitor.worked(calculateWork(1.0));
      }
   }

   private Attribute<String> getWordContent(Artifact artifact, IAttributeType attributeType) throws OseeCoreException {
      Attribute<String> toReturn = null;
      if (artifact != null && !artifact.isDeleted()) {
         toReturn = artifact.getSoleAttribute(attributeType);
      }
      return toReturn;
   }
}