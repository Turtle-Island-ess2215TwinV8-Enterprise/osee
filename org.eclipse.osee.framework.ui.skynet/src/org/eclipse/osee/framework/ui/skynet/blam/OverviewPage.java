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
package org.eclipse.osee.framework.ui.skynet.blam;

import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.ui.plugin.util.Jobs;
import org.eclipse.osee.framework.ui.skynet.IHelpContextIds;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.XFormToolkit;
import org.eclipse.osee.framework.ui.skynet.ats.IActionable;
import org.eclipse.osee.framework.ui.skynet.ats.OseeAts;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayout;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Ryan D. Brooks
 */
public class OverviewPage extends FormPage implements IActionable, XModifiedListener {
   private static final SkynetGuiPlugin plugin = SkynetGuiPlugin.getInstance();
   private final BlamWorkflow workflow;
   private final DynamicXWidgetLayout dynamicXWidgetLayout;
   private final XFormToolkit toolkit;
   private Composite parametersContainer;
   private final WorkflowEditor editor;
   private Text outputText;
   private Section parameterSection;

   public OverviewPage(WorkflowEditor editor) {
      super(editor, "overview", "Blam Workflow");

      this.editor = editor;
      this.toolkit = editor.getToolkit();
      this.workflow = (BlamWorkflow) editor.getEditorInput().getArtifact();
      this.dynamicXWidgetLayout = new DynamicXWidgetLayout();
   }

   public void update(DynamicXWidgetLayout dynamicXWidgetLayout) {
      //dispose old widgets before adding the new ones.
      for (Control control : parametersContainer.getChildren()) {
         control.dispose();
      }

      dynamicXWidgetLayout.createBody(toolkit, parametersContainer, null, this, true);
      parametersContainer.layout();
      parametersContainer.getParent().layout();
   }

   /**
    * Loads stored DynamicXWidgetLayoutData and updates the overview page
    * 
    * @param workflow
    */
   private void loadStoredLayoutData(BlamWorkflow workflow) {
      List<DynamicXWidgetLayoutData> layoutDatas = null;
      try {
         layoutDatas = workflow.getLayoutDatas();
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }

      if (layoutDatas != null && !layoutDatas.isEmpty()) {
         dynamicXWidgetLayout.addWorkLayoutDatas(layoutDatas);
         update(dynamicXWidgetLayout);
      }
      parameterSection.setExpanded(true);
   }

   protected void createFormContent(IManagedForm managedForm) {
      ScrolledForm form = managedForm.getForm();
      form.setText(getEditorInput().getName());
      createToolBarActions(form);
      form.updateToolBar();
      fillBody(managedForm);

      loadStoredLayoutData(workflow);

      managedForm.refresh();
   }

   private void createToolBarActions(ScrolledForm form) {
      Action runAction = new Action("Run Workflow in Job", Action.AS_PUSH_BUTTON) {
         public void run() {
            BlamJob blamJob =
                  new BlamJob(editor.getBlamVariableMap(), editor.getWorkflow(),
                        BranchPersistenceManager.getInstance().getDefaultBranch(),
                        editor.getWorkflow().getDescriptiveName());
            blamJob.addListener(editor);
            Jobs.startJob(blamJob);
         }
      };
      runAction.setToolTipText("Starts the workflow in a platform job");
      runAction.setImageDescriptor(plugin.getImageDescriptor("run_exc.gif"));
      form.getToolBarManager().add(runAction);

      OseeAts.addButtonToEditorToolBar(editor, this, SkynetGuiPlugin.getInstance(), form.getToolBarManager(),
            WorkflowEditor.EDITOR_ID, "Blam Workflow Editor");

   }

   public String getActionDescription() {
      return "";
   }

   private void fillBody(IManagedForm managedForm) {
      Composite body = managedForm.getForm().getBody();
      GridLayout gridLayout = new GridLayout(1, true);
      body.setLayout(gridLayout);

      PlatformUI.getWorkbench().getHelpSystem().setHelp(body, IHelpContextIds.MAIN_WORKFLOW_PAGE);

      createUsageSection(body);
      createParametersSection(body);
      createOutputSection(body);
   }

   private void createUsageSection(Composite body) {
      Section section = toolkit.createSection(body, Section.TWISTIE | Section.TITLE_BAR);
      section.setText("Description and Usage");
      section.setExpanded(true);
      section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      toolkit.addHelpLinkToSection(section, "/org.eclipse.pde.doc.user/guide/pde_running.htm");

      FormText formText = toolkit.createFormText(section, true);
      formText.setWhitespaceNormalized(true);
      formText.setFont("header", JFaceResources.getHeaderFont());
      formText.setFont("code", JFaceResources.getTextFont());
      formText.setText("Select parameters below and click the play button at the top right.", false, false);
      section.setClient(formText);
   }

   private void createParametersSection(Composite body) {
      parameterSection = toolkit.createSection(body, Section.TWISTIE | Section.TITLE_BAR);
      parameterSection.setText("Parameters");
      parameterSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      parametersContainer = toolkit.createClientContainer(parameterSection, 2);

      /*
       * for (int i = 0; i < 4; i++) { Label parameterNameLabel = toolkit.createLabel(container,
       * "Parameter Name" + i + ":"); parameterNameLabel.setLayoutData(new GridData(SWT.FILL,
       * SWT.TOP, false, false)); FormText formText = toolkit.createFormText(container, true);
       * formText.setText("parameter documentation should go here which could even wrap several
       * lines and long as it does then ", false, false); formText.setLayoutData(new
       * GridData(SWT.FILL, SWT.FILL, true, true, 1, 2)); Text parameterValueWidget =
       * toolkit.createText(container, "default value"); parameterValueWidget.setLayoutData(new
       * GridData(SWT.FILL, SWT.TOP, false, false)); }
       */
   }

   private void createOutputSection(Composite body) {
      Section section = toolkit.createSection(body, Section.TWISTIE | Section.TITLE_BAR);
      section.setText("Output");
      section.setExpanded(true);
      section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      Composite outputContainer = toolkit.createClientContainer(section, 2);

      outputText = toolkit.createText(outputContainer, "Workflow has not yet run", SWT.MULTI);
      outputText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
   }

   /**
    * @param line
    */
   public void appendOuputText(String additionalOutput) {
      outputText.append(additionalOutput);
   }

   /**
    * @param line
    */
   public void setOuputText(String output) {
      outputText.setText(output);
   }

   public void widgetModified(XWidget widget) {
      editor.getBlamVariableMap().setValue(widget.getLabel(), widget.getData());
   }
}