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
package org.eclipse.osee.ats.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.config.AtsVersionService;
import org.eclipse.osee.ats.help.ui.AtsHelpContext;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.world.search.WorldSearchItem.SearchType;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.HelpUtil;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.MultiPageEditorPart;

/**
 * @author Donald G. Dunne
 */
public class WorldEditor extends FormEditor implements IWorldEditor, IDirtiableEditor, IAtsMetricsProvider {
   public static final String EDITOR_ID = "org.eclipse.osee.ats.world.WorldEditor";
   private WorldXWidgetActionPage worldXWidgetActionPage;
   public static final int TITLE_MAX_LENGTH = 80;

   @Override
   public void doSave(IProgressMonitor monitor) {
      // do nothing
   }

   public static void open(final IWorldEditorProvider provider) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            IWorkbenchPage page = AWorkbench.getActivePage();
            try {
               page.openEditor(new WorldEditorInput(provider), EDITOR_ID);
            } catch (PartInitException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });
   }

   public void closeEditor() {
      final MultiPageEditorPart editor = this;
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            AWorkbench.getActivePage().closeEditor(editor, false);
         }
      });
   }

   public static Collection<WorldEditor> getEditors() {
      final List<WorldEditor> editors = new ArrayList<WorldEditor>();
      Displays.pendInDisplayThread(new Runnable() {
         @Override
         public void run() {
            for (IEditorReference editor : AWorkbench.getEditors(EDITOR_ID)) {
               editors.add((WorldEditor) editor.getEditor(false));
            }
         }
      });
      return editors;
   }

   public static void closeAll() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            for (IEditorReference editor : AWorkbench.getEditors(EDITOR_ID)) {
               AWorkbench.getActivePage().closeEditor(editor.getEditor(false), false);
            }
         }
      });
   }

   @Override
   public boolean isSaveOnCloseNeeded() {
      return isDirty();
   }

   public void refreshTitle() {
      firePropertyChange(IWorkbenchPart.PROP_TITLE);
   }

   @Override
   public void dispose() {
      if (worldXWidgetActionPage != null && worldXWidgetActionPage.getWorldComposite() != null) {
         worldXWidgetActionPage.getWorldComposite().disposeComposite();
      }
      super.dispose();
   }

   @Override
   public String getCurrentTitleLabel() {
      return worldXWidgetActionPage.getCurrentTitleLabel();
   }

   @Override
   public void setTableTitle(final String title, final boolean warning) {
      worldXWidgetActionPage.setTableTitle(title, warning);
   }

   @Override
   public boolean isDirty() {
      return false;
   }

   @Override
   protected void addPages() {

      try {
         OseeStatusContributionItemFactory.addTo(this, true);

         IWorldEditorProvider provider = getWorldEditorProvider();
         if (provider instanceof IWorldEditorConsumer) {
            ((IWorldEditorConsumer) provider).setWorldEditor(this);
         }

         createMainTab();
         createMetricsTab();
         setActivePage(0);

         setPartName(provider.getSelectedName(SearchType.Search));

         getSite().setSelectionProvider(getWorldComposite().getXViewer());

         // Until WorldEditor has different help, just use WorldView's help
         HelpUtil.setHelp(worldXWidgetActionPage.getWorldComposite().getControl(), AtsHelpContext.WORLD_VIEW);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   public WorldComposite getWorldComposite() {
      return worldXWidgetActionPage.getWorldComposite();
   }

   public WorldXWidgetActionPage getWorldXWidgetActionPage() {
      return worldXWidgetActionPage;
   }

   public void setEditorTitle(final String str) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            setPartName(str);
            firePropertyChange(IWorkbenchPart.PROP_TITLE);
         }
      });
   }

   @Override
   public IWorldEditorProvider getWorldEditorProvider() throws OseeArgumentException {
      IEditorInput editorInput = getEditorInput();
      if (!(editorInput instanceof WorldEditorInput)) {
         throw new OseeArgumentException("Editor Input not WorldEditorInput");
      }
      WorldEditorInput worldEditorInput = (WorldEditorInput) editorInput;
      return worldEditorInput.getIWorldEditorProvider();
   }

   @Override
   public void reSearch() throws OseeCoreException {
      worldXWidgetActionPage.reSearch();
   }

   private void createMainTab() throws PartInitException {
      worldXWidgetActionPage = new WorldXWidgetActionPage(this);
      addPage(worldXWidgetActionPage);
   }

   private void createMetricsTab() {
      Composite comp = AtsUtil.createCommonPageComposite(getContainer());
      new AtsMetricsComposite(this, comp, SWT.NONE);
      int metricsPageIndex = addPage(comp);
      setPageText(metricsPageIndex, "Metrics");
   }

   public List<Artifact> getLoadedArtifacts() {
      return worldXWidgetActionPage.getWorldComposite().getLoadedArtifacts();
   }

   @Override
   public Collection<? extends Artifact> getMetricsArtifacts() {
      return getLoadedArtifacts();
   }

   @Override
   public IAtsVersion getMetricsVersionArtifact() throws OseeCoreException {
      IAtsVersion verArt = getWorldEditorProvider().getTargetedVersionArtifact();
      if (verArt != null) {
         return verArt;
      }
      for (Artifact artifact : getLoadedArtifacts()) {
         if (artifact instanceof AbstractWorkflowArtifact && AtsVersionService.get().hasTargetedVersion(artifact)) {
            return AtsVersionService.get().getTargetedVersion(artifact);
         }
      }
      return null;
   }

   @Override
   public double getManHoursPerDayPreference() throws OseeCoreException {
      return worldXWidgetActionPage.getWorldComposite().getManHoursPerDayPreference();
   }

   @Override
   public void reflow() {
      getWorldXWidgetActionPage().reflow();
   }

   @Override
   public void createToolBarPulldown(Menu menu) {
      new MenuItem(menu, SWT.SEPARATOR);
      try {
         for (IAtsWorldEditorItem item : AtsWorldEditorItems.getItems()) {
            for (final Action action : item.getWorldEditorMenuActions(getWorldEditorProvider(), this)) {
               AtsUtil.actionToMenuItem(menu, action, SWT.PUSH);
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   @Override
   public void doSaveAs() {
      // do nothing
   }

   @Override
   public boolean isSaveAsAllowed() {
      return false;
   }

   @Override
   public void onDirtied() {
      // do nothing
   }

}
