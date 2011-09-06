/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.history.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * @author Donald G Dunne
 */
public class ResourceHistoryEditor extends FormEditor {
   public static final String EDITOR_ID = "org.eclipse.osee.framework.ui.skynet.history.editor.ResourceHistoryEditor";

   private ResourceHistoryPage resourceHistoryPage;

   public ResourceHistoryEditor() {
   }

   @Override
   public ResourceHistoryEditorInput getEditorInput() {
      return (ResourceHistoryEditorInput) super.getEditorInput();
   }

   @Override
   public void showBusy(boolean busy) {
      super.showBusy(busy);
      if (resourceHistoryPage != null) {
         resourceHistoryPage.showBusy(busy);
      }
   }

   @Override
   protected void addPages() {
      OseeStatusContributionItemFactory.addTo(this, true);
      try {
         resourceHistoryPage = new ResourceHistoryPage(this);
         addPage(resourceHistoryPage);
      } catch (PartInitException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public void doSave(IProgressMonitor monitor) {
      // method overridden only to satisfy its defining interface
   }

   @Override
   public void doSaveAs() {
      // method overridden only to satisfy its defining interface
   }

   @Override
   public boolean isSaveAsAllowed() {
      return false;
   }

   @Override
   public void dispose() {
      super.dispose();
   }

   public void refresh() {
      setPartName(getEditorInput().getName());
      setTitleImage(getEditorInput().getImage());
      if (resourceHistoryPage != null) {
         resourceHistoryPage.refresh();
      }
   }

   public static void open(final Artifact artifact) {
      if (artifact == null) {
         return;
      }
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            IWorkbenchPage page = AWorkbench.getActivePage();
            try {
               page.openEditor(new ResourceHistoryEditorInput(artifact), EDITOR_ID);
            } catch (PartInitException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });

   }

}
