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

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.AIFile;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.FileWatcher;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;

/**
 * @author Ryan D. Brooks
 * @author Jeff C. Phillips
 */
public abstract class FileRenderer extends FileSystemRenderer {
   private static final ResourceAttributes readonlyfileAttributes = new ResourceAttributes();

   protected static final FileWatcher watcher = new ArtifactEditFileWatcher(3, TimeUnit.SECONDS);
   private static boolean firstTime = true;
   private static boolean workbenchSavePopUpDisabled = false;

   static {
      readonlyfileAttributes.setReadOnly(true);
      watcher.start();
   }

   @Override
   public IFile renderToFileSystem(IFolder baseFolder, Artifact artifact, Branch branch, PresentationType presentationType) throws OseeCoreException {
      String fileName = RenderingUtil.getFilenameFromArtifact(this, artifact, presentationType);
      InputStream inputStream = getRenderInputStream(artifact, presentationType);
      return renderToFile(baseFolder, fileName, branch, inputStream, presentationType);
   }

   @Override
   public IFile renderToFileSystem(IFolder baseFolder, List<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException {
      Branch initialBranch = null;
      for (Artifact artifact : artifacts) {
         if (initialBranch == null) {
            initialBranch = artifact.getBranch();
         } else {
            if (artifact.getBranch() != initialBranch) {
               throw new IllegalArgumentException("All of the artifacts must be on the same branch to be mass edited");
            }
         }
      }

      Artifact artifact = null;
      if (artifacts.size() == 1) {
         artifact = artifacts.iterator().next();
      }
      String fileName = RenderingUtil.getFilenameFromArtifact(this, artifact, presentationType);
      InputStream inputStream = getRenderInputStream(artifacts, presentationType);
      return renderToFile(baseFolder, fileName, initialBranch, inputStream, presentationType);
   }

   public IFile renderToFile(IFolder baseFolder, String fileName, Branch branch, InputStream renderInputStream, PresentationType presentationType) throws OseeCoreException {
      try {
         IFile workingFile = baseFolder.getFile(fileName);
         AIFile.writeToFile(workingFile, renderInputStream);

         if (presentationType == PresentationType.SPECIALIZED_EDIT || presentationType == PresentationType.MERGE_EDIT) {
            monitorFile(workingFile.getLocation().toFile());
         } else if (presentationType == PresentationType.PREVIEW) {
            workingFile.setResourceAttributes(readonlyfileAttributes);
         }

         return workingFile;
      } catch (CoreException ex) {
         throw new OseeCoreException(ex);
      }
   }

   public void addFileToWatcher(IFolder baseFolder, String fileName) {
      IFile workingFile = baseFolder.getFile(fileName);
      monitorFile(workingFile.getLocation().toFile());
   }

   public abstract InputStream getRenderInputStream(List<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException;

   public abstract InputStream getRenderInputStream(Artifact artifact, PresentationType presentationType) throws OseeCoreException;

   private static void monitorFile(File file) {
      watcher.addFile(file);
      if (firstTime && !workbenchSavePopUpDisabled) {
         firstTime = false;
         PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {

            @Override
            public void postShutdown(IWorkbench workbench) {
            }

            @Override
            public boolean preShutdown(IWorkbench workbench, boolean forced) {
               boolean wasConfirmed =
                     MessageDialog.openConfirm(
                           PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                           "OSEE Edit",
                           "OSEE artifacts were opened for edit. Please save all external work before continuing. Click OK to continue shutdown process or Cancel to abort.");
               return forced || wasConfirmed;
            }
         });
      }
   }

   /**
    * @return the workbenchSavePopUpDisabled
    */
   public static boolean isWorkbenchSavePopUpDisabled() {
      return workbenchSavePopUpDisabled;
   }

   /**
    * @param workbenchSavePopUpDisabled the workbenchSavePopUpDisabled to set
    */
   public static void setWorkbenchSavePopUpDisabled(boolean workbenchSavePopUpDisabled) {
      FileRenderer.workbenchSavePopUpDisabled = workbenchSavePopUpDisabled;
   }

   private static final class ArtifactEditFileWatcher extends FileWatcher {

      public ArtifactEditFileWatcher(long time, TimeUnit unit) {
         super(time, unit);
      }

      @Override
      public synchronized void run() {
         try {
            for (Map.Entry<File, Long> entry : filesToWatch.entrySet()) {
               final File file = entry.getKey();
               final Long storedLastModified = entry.getValue();

               Long latestLastModified = file.lastModified();
               boolean requiresUpdate = false;
               if (!storedLastModified.equals(latestLastModified)) {
                  entry.setValue(latestLastModified);
                  if (file.exists()) {
                     requiresUpdate = true;
                  }
               }

               if (requiresUpdate) {
                  UpdateArtifactJob updateJob = new UpdateArtifactJob();
                  updateJob.setWorkingFile(file);
                  updateJob.addJobChangeListener(new JobChangeAdapter() {

                     @Override
                     public void done(IJobChangeEvent event) {
                        if (event.getResult().isOK()) {
                           OseeLog.log(SkynetGuiPlugin.class, Level.INFO,
                                 "Updated artifact linked to: " + file.getAbsolutePath());
                        }
                     }
                  });
                  Jobs.startJob(updateJob);
               }
            }
         } catch (Exception ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
      }
   }
}
