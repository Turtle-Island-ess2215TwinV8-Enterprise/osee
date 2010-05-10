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
package org.eclipse.osee.framework.ui.skynet.change.view;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.osee.framework.core.operation.CompositeOperation;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.change.ChangeReportEditorInput;
import org.eclipse.osee.framework.ui.skynet.change.ChangeUiData;
import org.eclipse.osee.framework.ui.skynet.change.IChangeReportPreferences;
import org.eclipse.osee.framework.ui.skynet.change.operations.LoadAssociatedArtifactOperation;
import org.eclipse.osee.framework.ui.skynet.change.operations.LoadChangesOperation;
import org.eclipse.osee.framework.ui.skynet.change.operations.UpdateChangeUiData;
import org.eclipse.osee.framework.ui.skynet.change.presenter.ChangeReportInfoPresenter;
import org.eclipse.osee.framework.ui.skynet.widgets.xchange.ChangeXViewer;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Ryan D. Brooks
 */
public class ChangeReportPage extends FormPage implements IChangeReportPreferences.Listener {
   private static String HELP_CONTEXT_ID = "ChangeView";

   private ChangeReportTable changeReportTable;
   private ChangeReportInfoPresenter infoPresenter;

   public ChangeReportPage(ChangeReportEditor editor) {
      super(editor, "change.report", "Change Report");
   }

   @Override
   public void showBusy(boolean busy) {
      super.showBusy(busy);
      if (Widgets.isAccessible(getManagedForm().getForm())) {
         getManagedForm().getForm().getForm().setBusy(busy);
      }
   }

   @Override
   protected void createFormContent(IManagedForm managedForm) {
      super.createFormContent(managedForm);

      final ScrolledForm form = managedForm.getForm();
      final FormToolkit toolkit = managedForm.getToolkit();

      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      layout.marginHeight = 10;
      layout.marginWidth = 6;
      layout.horizontalSpacing = 20;
      form.getBody().setLayout(layout);
      form.getBody().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      updateTitle(form);
      updateImage(form);

      managedForm.getMessageManager().setAutoUpdate(false);

      ChangeUiData uiData = getEditorInput().getChangeData();
      this.changeReportTable = new ChangeReportTable(uiData);
      this.infoPresenter = new ChangeReportInfoPresenter(new ChangeReportInfo(), uiData);

      int sectionStyle = Section.TITLE_BAR | Section.EXPANDED | Section.TWISTIE;

      managedForm.addPart(new EditorSection(infoPresenter, "Info", form.getBody(), managedForm.getToolkit(),
            sectionStyle, false));
      managedForm.addPart(new EditorSection(changeReportTable, "Changes", form.getBody(), managedForm.getToolkit(),
            sectionStyle, true));

      addToolBar(toolkit, form, true);
      form.reflow(true);

      PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(),
            "org.eclipse.osee.framework.help.ui." + HELP_CONTEXT_ID);
      bindMenu();

      getEditor().getPreferences().addListener(this);
      recomputeChangeReport(uiData.isLoadOnOpenEnabled());
   }

   private void bindMenu() {
      final ChangeXViewer xviewer = changeReportTable.getXViewer();

      MenuManager manager = xviewer.getMenuManager();
      manager.setRemoveAllWhenShown(true);
      manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      manager.addMenuListener(new ChangeReportMenuListener());

      Control control = xviewer.getTree();
      Menu menu = manager.createContextMenu(control);
      control.setMenu(menu);

      getSite().registerContextMenu("org.eclipse.osee.framework.ui.skynet.widgets.xchange.ChangeView", manager, xviewer);
      getSite().setSelectionProvider(xviewer);
   }

   private static final class ChangeReportMenuListener implements IMenuListener {
      public void menuAboutToShow(IMenuManager manager) {
         MenuManager menuManager = (MenuManager) manager;
         menuManager.insertBefore(XViewer.MENU_GROUP_PRE, new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      }
   }

   private void updateTitle(ScrolledForm form) {
      form.setText(getEditorInput().getName());
   }

   private void updateImage(ScrolledForm form) {
      form.setImage(getEditor().getEditorInput().getImage());
   }

   private void addToolBar(FormToolkit toolkit, ScrolledForm form, boolean add) {
      IToolBarManager manager = form.getToolBarManager();
      if (add) {
         getEditor().getActionBarContributor().contributeToToolBar(manager);
         manager.add(changeReportTable.getXViewer().getCustomizeAction());
         manager.update(true);
      } else {
         manager.removeAll();
      }
      form.reflow(true);
   }

   @Override
   public ChangeReportEditor getEditor() {
      return (ChangeReportEditor) super.getEditor();
   }

   @Override
   public ChangeReportEditorInput getEditorInput() {
      return (ChangeReportEditorInput) super.getEditorInput();
   }

   public void onLoad() {
      Display.getDefault().asyncExec(new Runnable() {
         public void run() {
            if (changeReportTable != null && infoPresenter != null) {
               changeReportTable.onLoading();
               infoPresenter.onLoading();
            }
         }
      });
   }

   public void refresh() {
      final ScrolledForm sForm = getManagedForm().getForm();
      for (IFormPart part : getManagedForm().getParts()) {
         part.refresh();
      }

      updateTitle(sForm);
      updateImage(sForm);

      sForm.getBody().layout(true);
      sForm.reflow(true);
      getManagedForm().refresh();
   }

   @Override
   public void onDocumentOrderChange(boolean value) {
      if (changeReportTable != null) {
         changeReportTable.getXViewer().setShowDocumentOrderFilter(value);
         changeReportTable.getXViewer().refresh();
      }
   }

   public void recomputeChangeReport(boolean isReloadAllowed) {
      List<IOperation> ops = new ArrayList<IOperation>();
      ChangeUiData changeData = getEditorInput().getChangeData();
      ops.add(new UpdateChangeUiData(changeData));
      if (isReloadAllowed) {
         changeData.reset();
         onLoad();
         ops.add(new LoadChangesOperation(changeData));
      }
      ops.add(new LoadAssociatedArtifactOperation(changeData));
      IOperation operation = new CompositeOperation("Load Change Report Data", SkynetGuiPlugin.PLUGIN_ID, ops);
      Operations.executeAsJob(operation, true, Job.LONG, new ReloadJobChangeAdapter());
   }

   private final class ReloadJobChangeAdapter extends JobChangeAdapter {
      private long startTime = 0;

      @Override
      public void scheduled(IJobChangeEvent event) {
         super.scheduled(event);
         getEditor().getActionBarContributor().getReloadAction().setEnabled(false);
         showBusy(true);
      }

      @Override
      public void aboutToRun(IJobChangeEvent event) {
         super.aboutToRun(event);
         startTime = System.currentTimeMillis();
      }

      @Override
      public void done(IJobChangeEvent event) {
         super.done(event);
         String message = String.format("Change Report Load completed in [%s]", Lib.getElapseString(startTime));
         OseeLog.log(SkynetGuiPlugin.class, Level.INFO, message);

         Job job = new UIJob("Refresh Change Report") {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
               getEditor().refresh();
               getEditor().getActionBarContributor().getReloadAction().setEnabled(true);
               getEditor().getActionBarContributor().getOpenAssociatedArtifactAction().updateEnablement();
               showBusy(false);
               return Status.OK_STATUS;
            }
         };
         Operations.scheduleJob(job, false, Job.SHORT, null);
      }
   }

}