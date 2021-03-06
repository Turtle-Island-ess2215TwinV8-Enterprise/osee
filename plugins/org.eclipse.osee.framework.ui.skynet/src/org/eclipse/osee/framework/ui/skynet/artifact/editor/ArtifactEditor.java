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

package org.eclipse.osee.framework.ui.skynet.artifact.editor;

import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.SystemGroup;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.AttributesComposite;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.RelationsComposite;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.pages.ArtifactEditorOutlinePage;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.pages.ArtifactFormPage;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.preferences.EditorsPreferencePage;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactEditor extends AbstractEventArtifactEditor {
   public static final String EDITOR_ID = "org.eclipse.osee.framework.ui.skynet.artifact.editor.ArtifactEditor";

   private IActionContributor actionBarContributor;
   private ArtifactFormPage formPage;
   private ArtifactEditorOutlinePage outlinePage;

   public IActionContributor getActionBarContributor() {
      if (actionBarContributor == null) {
         actionBarContributor = new ArtifactEditorActionBarContributor(this);
      }
      return actionBarContributor;
   }

   @Override
   public BaseArtifactEditorInput getEditorInput() {
      return (BaseArtifactEditorInput) super.getEditorInput();
   }

   @Override
   public void editorDirtyStateChanged() {
      super.editorDirtyStateChanged();
      getOutlinePage().refresh();
   }

   @Override
   public void onDirtied() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            firePropertyChange(PROP_DIRTY);
         }
      });
   }

   @Override
   public void showBusy(boolean busy) {
      ArtifactFormPage page = getFormPage();
      if (page != null) {
         page.showBusy(busy);
      }
   }

   @Override
   public void doSave(IProgressMonitor monitor) {
      try {
         getFormPage().doSave(monitor);
         Artifact artifact = getEditorInput().getArtifact();
         artifact.persist(getClass().getSimpleName());
         firePropertyChange(PROP_DIRTY);
      } catch (OseeCoreException ex) {
         onDirtied();
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public void dispose() {
      try {
         // If the artifact is dirty when the editor gets disposed, then it needs to be reverted
         Artifact artifact = getEditorInput().getArtifact();
         if (!artifact.isDeleted() && artifact.isDirty()) {
            try {
               artifact.reloadAttributesAndRelations();
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      } finally {
         super.dispose();
      }
   }

   @Override
   protected void checkEnabledTooltems() {
      //      if (!attributeComposite.isDisposed()) {
      //        				Displays.ensureInDisplayThread(new Runnable() {
      //		@Override
      //		public void run() {
      //               boolean isEditAllowed = artifact.isReadOnly() != true;
      //
      //               if (attributeComposite.getToolBar() == null || attributeComposite.getToolBar().isDisposed()) {
      //                  return;
      //               }
      //               attributeComposite.getToolBar().getItem(REVEAL_ARTIFACT_INDEX).setEnabled(true);
      //               attributeComposite.getToolBar().getItem(EDIT_ARTIFACT_INDEX).setEnabled(isEditAllowed);
      //               attributeComposite.getToolBar().update();
      //
      //               relationsComposite.getToolBar().getItem(REVEAL_ARTIFACT_INDEX).setEnabled(true);
      //               relationsComposite.getToolBar().getItem(EDIT_ARTIFACT_INDEX).setEnabled(isEditAllowed);
      //               relationsComposite.getToolBar().update();
      //            }
      //         });
      //      }
   }

   @Override
   public void closeEditor() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            AWorkbench.getActivePage().closeEditor(ArtifactEditor.this, false);
         }
      });
   }

   @Override
   public void refreshDirtyArtifact() {
      Jobs.startJob(new RefreshDirtyArtifactJob());
   }

   @Override
   public void refreshRelations() {
      Jobs.startJob(new RefreshRelations());
   }

   @Override
   protected void addPages() {
      OseeStatusContributionItemFactory.addTo(this, true);
      setPartName(getEditorInput().getName());

      formPage = new ArtifactFormPage(this, "ArtifactFormPage", null);
      try {
         addPage(formPage);
      } catch (PartInitException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      try {
         if (EditorsPreferencePage.isIncludeAttributeTabOnArtifactEditor()) {
            createAttributesTab();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   private ToolBar createToolBar(Composite parent) {
      ToolBar toolBar = ALayout.createCommonToolBar(parent);
      new ToolItem(toolBar, SWT.SEPARATOR);
      Text artifactInfoLabel = new Text(toolBar.getParent(), SWT.END);
      artifactInfoLabel.setEditable(false);
      artifactInfoLabel.setText("Type: \"" + getEditorInput().getArtifact().getArtifactTypeName() + "\"   HRID: " + getEditorInput().getArtifact().getHumanReadableId());
      artifactInfoLabel.setToolTipText("The human readable id and database id for this artifact");

      return toolBar;
   }

   private void createAttributesTab() {
      try {
         if (!SystemGroup.OseeAdmin.isCurrentUserMember()) {
            return;
         }

         // Create Attributes tab
         Composite composite = new Composite(getContainer(), SWT.NONE);
         GridLayout layout = new GridLayout(1, false);
         layout.marginHeight = 0;
         layout.marginWidth = 0;
         layout.verticalSpacing = 0;
         composite.setLayout(layout);
         ToolBar toolBar = createToolBar(composite);

         ToolItem item = new ToolItem(toolBar, SWT.PUSH);
         item.setImage(ImageManager.getImage(FrameworkImage.SAVE));
         item.setToolTipText("Save attributes changes only");
         item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
               try {
                  getEditorInput().getArtifact().persist("ArtifactEditor attribute tab persist");
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         });

         Label label = new Label(composite, SWT.NONE);
         label.setText("  NOTE: Changes made on this page MUST be saved through save icon on this page");
         label.setForeground(Displays.getSystemColor(SWT.COLOR_RED));

         new AttributesComposite(this, composite, SWT.NONE, getEditorInput().getArtifact());
         int attributesPageIndex = addPage(composite);
         setPageText(attributesPageIndex, "Attributes");
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   private ArtifactFormPage getFormPage() {
      return formPage;
   }

   @SuppressWarnings("rawtypes")
   @Override
   public Object getAdapter(Class adapter) {
      if (adapter == IContentOutlinePage.class) {
         ArtifactEditorOutlinePage page = getOutlinePage();
         page.setInput(this);
         return page;
      } else if (adapter == RelationsComposite.class) {
         return getFormPage().getRelationsComposite();
      }
      return super.getAdapter(adapter);
   }

   public ArtifactEditorOutlinePage getOutlinePage() {
      if (outlinePage == null) {
         outlinePage = new ArtifactEditorOutlinePage();
      }
      return outlinePage;
   }

   private final class RefreshRelations extends UIJob {
      public RefreshRelations() {
         super("Refresh Relations");
      }

      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
         ArtifactFormPage page = getFormPage();
         if (page != null) {
            page.showBusy(true);
            RelationsComposite relationsComposite = page.getRelationsComposite();
            if (relationsComposite != null && !relationsComposite.isDisposed()) {
               relationsComposite.refresh();
               onDirtied();
            }
            page.showBusy(false);
         }
         return Status.OK_STATUS;
      }
   }

   private final class RefreshDirtyArtifactJob extends UIJob {

      public RefreshDirtyArtifactJob() {
         super("Refresh Dirty Artifact");
      }

      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
         try {
            setPartName(getEditorInput().getName());
            ArtifactEditorOutlinePage outlinePage = getOutlinePage();
            outlinePage.refresh();

            ArtifactFormPage page = getFormPage();
            if (page != null && Widgets.isAccessible(page.getPartControl())) {
               page.refresh();
            }
            onDirtied();
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
         return Status.OK_STATUS;
      }
   }

   @Override
   public boolean isDisposed() {
      return formPage == null || formPage.getPartControl() == null || formPage.getPartControl().isDisposed();
   }

}