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

import java.util.List;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.XFormToolkit;
import org.eclipse.osee.framework.ui.skynet.XWidgetParser;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.parts.AttributeFormPart;
import org.eclipse.osee.framework.ui.skynet.util.FormsUtil;
import org.eclipse.osee.framework.ui.skynet.widgets.util.DefaultXWidgetOptionResolver;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IDynamicWidgetLayoutListener;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IXWidgetOptionResolver;
import org.eclipse.osee.framework.ui.skynet.widgets.util.SwtXWidgetRenderer;
import org.eclipse.osee.framework.ui.skynet.widgets.util.XWidgetRendererItem;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.FontManager;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Donald G. Dunne
 */
public abstract class AtsXWidgetActionFormPage extends FormPage {
   protected SwtXWidgetRenderer dynamicXWidgetLayout;
   protected final XFormToolkit toolkit;
   private Composite parametersContainer;
   private Section parameterSection;
   protected Composite resultsContainer;
   protected Section resultsSection;
   protected ScrolledForm scrolledForm;
   private String title;

   public AtsXWidgetActionFormPage(FormEditor editor, String id, String name) {
      super(editor, id, name);
      this.toolkit = new XFormToolkit();
   }

   public abstract Result isResearchSearchValid() throws OseeCoreException;

   public abstract String getXWidgetsXml() throws OseeCoreException;

   @Override
   protected void createFormContent(IManagedForm managedForm) {
      scrolledForm = managedForm.getForm();
      FormsUtil.addHeadingGradient(toolkit, scrolledForm, true);

      Composite body = scrolledForm.getBody();
      body.setLayout(new GridLayout(1, true));
      body.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));

      try {
         if (Strings.isValid(getXWidgetsXml())) {
            managedForm.addPart(new SectionPart(createParametersSection(managedForm, body)));
         }
         managedForm.addPart(new SectionPart(createResultsSection(body)));
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

      AttributeFormPart.setLabelFonts(body, FontManager.getDefaultLabelFont());

      createToolBar();
      managedForm.refresh();
   }

   protected void createToolBar(IToolBarManager toolBarManager) {
      // provided for subclass implementation
   }

   private void createToolBar() {
      IToolBarManager toolBarManager = scrolledForm.getToolBarManager();
      createToolBar(toolBarManager);
      scrolledForm.updateToolBar();
   }

   public void reflow() {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            IManagedForm manager = getManagedForm();
            if (manager != null && Widgets.isAccessible(manager.getForm())) {
               getManagedForm().reflow(true);
            }
         }
      });
   }

   private Section createParametersSection(IManagedForm managedForm, Composite body) throws OseeCoreException {
      parameterSection = toolkit.createSection(body, ExpandableComposite.NO_TITLE);
      parameterSection.setText("Parameters");
      parameterSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      parametersContainer = toolkit.createClientContainer(parameterSection, 1);
      parameterSection.setExpanded(true);

      Composite mainComp = toolkit.createComposite(parametersContainer, SWT.NONE);
      mainComp.setLayout(ALayout.getZeroMarginLayout(3, false));
      mainComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      createButtonCompositeOnLeft(mainComp);
      createSearchParametersOnRight(managedForm, mainComp);
      createSaveButtonCompositeOnRight(mainComp);

      return parameterSection;
   }

   public void createSearchParametersOnRight(IManagedForm managedForm, Composite mainComp) throws OseeCoreException {
      Composite paramComp = new Composite(mainComp, SWT.NONE);
      paramComp.setLayout(ALayout.getZeroMarginLayout(1, false));
      paramComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      List<XWidgetRendererItem> layoutDatas = null;
      dynamicXWidgetLayout = new SwtXWidgetRenderer(getDynamicWidgetLayoutListener(), getXWidgetOptionResolver());
      try {
         layoutDatas = XWidgetParser.extractWorkAttributes(dynamicXWidgetLayout, getXWidgetsXml());
         if (layoutDatas != null && !layoutDatas.isEmpty()) {
            dynamicXWidgetLayout.addWorkLayoutDatas(layoutDatas);
            dynamicXWidgetLayout.createBody(managedForm, paramComp, null, null, true);
            parametersContainer.layout();
            parametersContainer.getParent().layout();
         }
         parameterSection.setExpanded(true);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public void createButtonCompositeOnLeft(Composite mainComp) {
      Composite buttonComp = toolkit.createComposite(mainComp, SWT.NONE);
      buttonComp.setLayout(ALayout.getZeroMarginLayout(1, false));
      buttonComp.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, true));

      Button runButton = toolkit.createButton(buttonComp, "Search", SWT.PUSH);
      GridData gridData = new GridData(SWT.FILL, SWT.BOTTOM, true, true);
      runButton.setLayoutData(gridData);
      runButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            handleSearchButtonPressed();
         }
      });

      buttonComp.layout();
   }

   public void createSaveButtonCompositeOnRight(Composite mainComp) {
      if (isSaveButtonAvailable()) {
         Composite buttonComp = toolkit.createComposite(mainComp, SWT.NONE);
         buttonComp.setLayout(ALayout.getZeroMarginLayout(1, false));
         buttonComp.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, true));
         GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);

         Button saveButton = toolkit.createButton(buttonComp, "Save Options", SWT.PUSH);
         saveButton.setToolTipText("Save search selections as default");
         gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
         saveButton.setLayoutData(gridData);
         saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
               if (MessageDialog.openConfirm(Displays.getActiveShell(), "Save Default Parameters",
                  "Save current parameters as default?")) {
                  handleSaveButtonPressed();
               }
            }
         });
      }
   }

   @SuppressWarnings("unused")
   public IDynamicWidgetLayoutListener getDynamicWidgetLayoutListener() throws OseeCoreException {
      return null;
   }

   public IXWidgetOptionResolver getXWidgetOptionResolver() {
      return new DefaultXWidgetOptionResolver();
   }

   public abstract void handleSearchButtonPressed();

   public abstract boolean isSaveButtonAvailable();

   public abstract void handleSaveButtonPressed();

   public void setTableTitle(final String title, final boolean warning) {
      this.title = title;
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (Widgets.isAccessible(scrolledForm)) {
               scrolledForm.setText(title);
            }
         };
      });
   }

   public abstract Section createResultsSection(Composite body) throws OseeCoreException;

   public ScrolledForm getScrolledForm() {
      return scrolledForm;
   }

   public String getCurrentTitleLabel() {
      if (title != null) {
         return title;
      } else {
         return WorldEditor.EDITOR_ID;
      }
   }

}