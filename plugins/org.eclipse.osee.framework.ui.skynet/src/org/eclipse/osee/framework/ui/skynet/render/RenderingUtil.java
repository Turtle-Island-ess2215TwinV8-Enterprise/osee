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
package org.eclipse.osee.framework.ui.skynet.render;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.OseeData;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.change.ArtifactDelta;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.swt.Displays;

public final class RenderingUtil {
   private static final Random generator = new Random();

   private static IFolder workingFolder;
   private static IFolder diffFolder;
   private static IFolder previewFolder;
   private static IFolder mergeEditFolder;
   private static boolean arePopupsAllowed = true;

   public static void setPopupsAllowed(boolean popupsAllowed) {
      arePopupsAllowed = popupsAllowed;
   }

   private static final String FILENAME_WARNING_MESSAGE =
      "\n\nis approaching a large size which may cause the opening application to error. " + "\nSuggest moving your workspace to avoid potential errors. ";
   private static final int FILENAME_LIMIT = 215;

   private static boolean showAgain = true;

   public static boolean ensureFilenameLimit(IFile file) {
      boolean withinLimit = true;
      if (Lib.isWindows()) {
         String absPath = file.getLocation().toFile().getAbsolutePath();
         if (absPath.length() > FILENAME_LIMIT) {
            final String warningMessage = "Your filename: \n\n" + absPath + FILENAME_WARNING_MESSAGE;
            // need to warn user that their filename size is large and may cause the program (Word, Excel, PPT) to error
            if (showAgain && arePopupsAllowed()) {
               //display warning once per session

               Displays.pendInDisplayThread(new Runnable() {
                  @Override
                  public void run() {
                     MessageDialog.openWarning(Displays.getActiveShell(), "Filename Size Warning", warningMessage);
                  }
               });

               showAgain = false;
            }
            //log the warning every time
            OseeLog.log(SkynetGuiPlugin.class, Level.WARNING, warningMessage);
            withinLimit = false;
         }
      }
      return withinLimit;
   }

   public static boolean arePopupsAllowed() {
      return arePopupsAllowed;
   }

   public static Pair<Artifact, Artifact> asRenderInput(ArtifactDelta artifactDelta) {
      Artifact artFile1;
      Artifact artFile2;

      if (artifactDelta.getEndArtifact().getModType().isDeleted()) {
         artFile1 = artifactDelta.getStartArtifact();
         artFile2 = null;
      } else if (artifactDelta.getStartArtifact() == null) {
         artFile1 = null;
         artFile2 = artifactDelta.getEndArtifact();
      } else {
         artFile1 = artifactDelta.getStartArtifact();
         artFile2 = artifactDelta.getEndArtifact();
      }
      return new Pair<Artifact, Artifact>(artFile1, artFile2);
   }

   public static IFile getRenderFile(FileSystemRenderer renderer, List<Artifact> artifacts, IOseeBranch branch, PresentationType presentationType) throws OseeCoreException {
      Artifact aritfact = artifacts.isEmpty() ? null : artifacts.get(0);
      String fileName = getFilenameFromArtifact(renderer, aritfact, presentationType);
      return getRenderFile(fileName, branch, presentationType);
   }

   public static IFile getRenderFile(String fileName, IOseeBranch branch, PresentationType presentationType) throws OseeCoreException {
      IFolder baseFolder = getRenderFolder(branch, presentationType);
      return baseFolder.getFile(fileName);
   }

   public static String getRenderPath(String fileName, IOseeBranch branch, PresentationType presentationType) throws OseeCoreException {
      return getRenderFile(fileName, branch, presentationType).getLocation().toOSString();
   }

   public static String getFilenameFromArtifact(FileSystemRenderer renderer, Artifact artifact, PresentationType presentationType) throws OseeCoreException {
      String fileName = renderer.getStringOption(IRenderer.FILE_NAME_OPTION);
      if (Strings.isValid(fileName)) {
         //         return fileName;
      }

      StringBuilder name = new StringBuilder(100);

      if (artifact != null) {
         name.append(artifact.getSafeName());
         name.append("(");
         name.append(artifact.getGuid());
         name.append(")");

         if (artifact.isHistorical() || presentationType == PresentationType.DIFF) {
            name.append("(");
            name.append(artifact.getTransactionNumber());
            name.append(")");
         }

         name.append(" ");
         name.append(new Date().toString().replaceAll(":", ";"));
         name.append("-");
         name.append(generator.nextInt(99) + 1);
         name.append(".");
         name.append(renderer.getAssociatedExtension(artifact));
      } else {
         name.append(GUID.create());
         name.append(".xml");
      }
      return name.toString();
   }

   public static IFolder getRenderFolder(IOseeBranch branch, PresentationType presentationType) throws OseeCoreException {
      try {
         IFolder baseFolder = ensureRenderFolderExists(presentationType);
         IFolder renderFolder = baseFolder.getFolder(toFileName(branch));
         if (!renderFolder.exists()) {
            renderFolder.create(true, true, null);
         }
         return renderFolder;
      } catch (CoreException ex) {
         OseeExceptions.wrapAndThrow(ex);
         return null; // unreachable since wrapAndThrow() always throws an exception
      }
   }

   public static String toFileName(IOseeBranch branch) throws OseeCoreException {
      return encode(Branch.getShortName(branch));
   }

   private static String encode(String name) throws OseeCoreException {
      String toReturn = null;
      try {
         toReturn = URLEncoder.encode(name, "UTF-8");
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return toReturn;
   }

   public static IFolder ensureRenderFolderExists(PresentationType presentationType) throws OseeCoreException {
      IFolder toReturn = null;
      switch (presentationType) {
         case DIFF:
            diffFolder = getOrCreateFolder(diffFolder, ".diff");
            toReturn = diffFolder;
            break;
         case SPECIALIZED_EDIT:
            workingFolder = getOrCreateFolder(workingFolder, ".working");
            toReturn = workingFolder;
            break;
         case PREVIEW:
            previewFolder = getOrCreateFolder(previewFolder, ".preview");
            toReturn = previewFolder;
            break;
         case MERGE_EDIT:
            mergeEditFolder = getOrCreateFolder(mergeEditFolder, ".mergeEdit");
            toReturn = mergeEditFolder;
            break;
         default:
            throw new OseeArgumentException("Unexpected presentation type: %s", presentationType);
      }
      return toReturn;
   }

   private static IFolder getOrCreateFolder(IFolder folder, String name) throws OseeCoreException {
      IFolder toCheck = folder;
      if (toCheck == null || !toCheck.exists()) {
         toCheck = OseeData.getFolder(name);
      }
      return toCheck;
   }
}