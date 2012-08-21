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
package org.eclipse.osee.ats.workdef;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.workdef.IAtsCompositeLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsStepDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsStepsLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.editor.stateItem.AtsStateItemManager;
import org.eclipse.osee.ats.editor.stateItem.IAtsStateItem;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.widgets.commit.XCommitManager;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.WidgetOption;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.ui.skynet.widgets.IArtifactWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.IAttributeWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XText;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Instantiation of a StateXWidgetPage for a given StateDefinition to provide for automatic creation and management of
 * the XWidgets
 * 
 * @author Donald G. Dunne
 */
public class StateXWidgetPage implements IAtsWidgetLayoutListener, IStateToken {

   private static final Pair<IStatus, XWidget> OK_PAIR = new Pair<IStatus, XWidget>(Status.OK_STATUS, null);
   protected final IAtsStateDefinition stateDefinition;
   protected final IAtsWorkDefinition workDefinition;
   private AbstractWorkflowArtifact sma;
   private final IAtsWidgetLayoutListener atsWidgetLayoutListener;
   private final List<IAtsWidgetDefinitionRender> widgetItems = new ArrayList<IAtsWidgetDefinitionRender>();
   private XModifiedListener xModListener;
   private final Collection<ArrayList<String>> orRequired = new ArrayList<ArrayList<String>>();
   private final Collection<ArrayList<String>> xorRequired = new ArrayList<ArrayList<String>>();
   private final List<Label> labels = new ArrayList<Label>();

   public StateXWidgetPage(IAtsWorkDefinition workDefinition, IAtsStateDefinition stateDefinition, IAtsWidgetLayoutListener atsWidgetLayoutListener) {
      this.workDefinition = workDefinition;
      this.stateDefinition = stateDefinition;
      this.atsWidgetLayoutListener = atsWidgetLayoutListener;
   }

   @Override
   public void widgetCreated(XWidget xWidget, FormToolkit toolkit, Artifact art, StateXWidgetPage stateXWidgetPage, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      widgetCreated(xWidget, toolkit, art, stateDefinition, xModListener, isEditable);
   }

   @Override
   public void widgetCreating(XWidget xWidget, FormToolkit toolkit, Artifact art, StateXWidgetPage stateXWidgetPage, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      widgetCreating(xWidget, toolkit, art, stateDefinition, xModListener, isEditable);
   }

   public void dispose() {
      try {
         for (IAtsWidgetDefinitionRender layoutData : getlayoutDatas()) {
            layoutData.getXWidget().dispose();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof StateXWidgetPage) {
         return getName().equals(((StateXWidgetPage) obj).getName());
      }
      return false;
   }

   public void createBody(IManagedForm managedForm, Composite parent, AbstractWorkflowArtifact awa, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      final FormToolkit toolkit = managedForm != null ? managedForm.getToolkit() : null;

      final Composite topLevelComp = createComposite(parent, toolkit);
      GridLayout layout = new GridLayout(1, false);
      layout.marginWidth = 2;
      layout.marginHeight = 2;
      topLevelComp.setLayout(layout);
      topLevelComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      if (toolkit != null) {
         toolkit.adapt(topLevelComp);
      }

      processLayoutItems(awa, managedForm, toolkit, topLevelComp, stateDefinition.getLayoutItems(), isEditable);

      topLevelComp.layout();

      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            try {
               for (IAtsWidgetDefinitionRender widgetItem : widgetItems) {
                  widgetItem.getXWidget().validate();
               }
               refreshOrAndXOrRequiredFlags();
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });

   }

   private void processLayoutItems(AbstractWorkflowArtifact awa, IManagedForm managedForm, FormToolkit toolkit, Composite parent, List<IAtsLayoutItem> layoutItems, boolean isEditable) throws OseeCoreException {
      for (IAtsLayoutItem item : layoutItems) {
         if (item instanceof IAtsWidgetDefinition) {
            IAtsWidgetDefinition widgetDef = (IAtsWidgetDefinition) item;
            IAtsWidgetDefinitionRender widgetItem =
               processWidgetDefinition(awa, managedForm, widgetDef, toolkit, parent, isEditable);
            widgetItems.add(widgetItem);
         } else if (item instanceof IAtsCompositeLayoutItem) {
            IAtsCompositeLayoutItem compositeLayoutItem = (IAtsCompositeLayoutItem) item;
            processCompositeLayout(awa, managedForm, compositeLayoutItem, toolkit, parent, isEditable);
         } else if (item instanceof IAtsStepsLayoutItem) {
            IAtsStepsLayoutItem stepsLayout = (IAtsStepsLayoutItem) item;
            processStepsLayout(awa, managedForm, stepsLayout, toolkit, parent, isEditable);
         }
      }
      parent.layout();
   }

   private void processStepsLayout(AbstractWorkflowArtifact awa, IManagedForm managedForm, IAtsStepsLayoutItem stepsLayout, FormToolkit toolkit, Composite parent, boolean isEditable) throws OseeCoreException {

      if (Strings.isValid(stepsLayout.getName())) {
         Label label = new Label(parent, SWT.NONE);
         label.setText(stepsLayout.getName() + ": ");
         labels.add(label);
      }

      Composite stepsComp = createStepsLayoutComposite(parent);
      TabFolder currentTabFolder = new TabFolder(stepsComp, SWT.LEFT | SWT.NONE);
      currentTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

      for (IAtsStepDefinition stepDef : stepsLayout.getStepDefinitions()) {
         processStepDefinition(currentTabFolder, awa, managedForm, stepDef, toolkit, parent, isEditable);
      }
   }

   private void processStepDefinition(TabFolder currentTabFolder, AbstractWorkflowArtifact awa, IManagedForm managedForm, IAtsStepDefinition stepDef, FormToolkit toolkit, Composite parent, boolean isEditable) throws OseeCoreException {
      TabItem currentTabItem = new TabItem(currentTabFolder, 0);
      currentTabItem.setText(stepDef.getName());
      Composite tabComp = createStepDefinitionComposite(currentTabItem);

      if (Strings.isValid(stepDef.getDescription())) {
         Label label = new Label((Composite) currentTabItem.getControl(), SWT.NONE);
         label.setText("Description: " + stepDef.getDescription());
         labels.add(label);
      }
      processLayoutItems(awa, managedForm, toolkit, tabComp, stepDef.getLayoutItems(), isEditable);
   }

   private void processCompositeLayout(AbstractWorkflowArtifact awa, IManagedForm managedForm, IAtsCompositeLayoutItem compositeLayoutItem, FormToolkit toolkit, Composite parent, boolean isEditable) throws OseeCoreException {
      Composite outComp = createCompositeLayoutComposite(parent, compositeLayoutItem.getNumColumns(), toolkit);
      processLayoutItems(awa, managedForm, toolkit, outComp, compositeLayoutItem.getLayoutItems(), isEditable);
   }

   private Composite createStepsLayoutComposite(Composite parent) {
      Composite comp = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout(1, true);
      layout.marginWidth = 20;
      comp.setLayout(layout);
      comp.setLayoutData(new GridData(GridData.FILL_BOTH));
      return comp;
   }

   private Composite createStepDefinitionComposite(TabItem ti) {
      Composite comp = new Composite(ti.getParent(), SWT.NONE);
      comp.setLayout(new GridLayout(1, true));
      comp.setLayoutData(new GridData(GridData.FILL_BOTH));
      ti.setControl(comp);
      return comp;
   }

   private Composite createCompositeLayoutComposite(Composite parent, int numColumns, FormToolkit toolkit) {
      Composite comp = createComposite(parent, toolkit);
      GridLayout layout = new GridLayout(numColumns, false);
      layout.horizontalSpacing = 8;
      comp.setLayout(layout);
      comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      if (toolkit != null) {
         toolkit.adapt(comp);
      }
      return comp;
   }

   private IAtsWidgetDefinitionRender processWidgetDefinition(AbstractWorkflowArtifact awa, IManagedForm managedForm, IAtsWidgetDefinition widgetDef, FormToolkit toolkit, Composite parent, boolean isEditable) throws OseeCoreException {
      IAtsWidgetDefinitionRender renderer = new SwtAtsWidgetDefinitionRenderer(widgetDef);

      GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
      parent.setLayoutData(gd);

      if (renderer.getOptions().contains(WidgetOption.FILL_VERTICALLY)) {
         gd.grabExcessVerticalSpace = true;
      }

      XWidget xWidget = setupXWidget(awa, toolkit, isEditable, renderer);

      setupArtifactInfo(awa, widgetDef.getAtrributeName(), xWidget);

      if (xWidget instanceof XText) {
         XText xText = (XText) xWidget;
         if (renderer.getOptions().contains(WidgetOption.FILL_HORIZONTALLY)) {
            xText.setFillHorizontally(true);
         }
         if (renderer.getOptions().contains(WidgetOption.FILL_VERTICALLY)) {
            xText.setFillVertically(true);
         }
         if (renderer.isHeightSet()) {
            xText.setHeight(renderer.getHeight());
         }
         xText.setDynamicallyCreated(true);
      }

      Composite currentComp = parent;
      if (renderer.getOptions().contains(WidgetOption.HORIZONTAL_LABEL)) {
         currentComp = createComposite(currentComp, toolkit);
         currentComp.setLayout(ALayout.getZeroMarginLayout(2, false));
         currentComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         if (toolkit != null) {
            toolkit.adapt(currentComp);
         }
      }
      xWidget.createWidgets(managedForm, currentComp, 2);

      if (xModListener != null) {
         xWidget.addXModifiedListener(xModListener);
      }

      xWidget.addXModifiedListener(refreshRequiredModListener);
      if (atsWidgetLayoutListener != null) {
         atsWidgetLayoutListener.widgetCreated(xWidget, toolkit, awa, this, xModListener, isEditable);
         atsWidgetLayoutListener.createXWidgetLayoutData(renderer, xWidget, toolkit, awa, xModListener, isEditable);
      }

      return renderer;
   }

   public XWidget setupXWidget(AbstractWorkflowArtifact awa, FormToolkit toolkit, boolean isEditable, IAtsWidgetDefinitionRender renderer) throws OseeCoreException {
      XWidget xWidget = renderer.getXWidget();

      if (Strings.isValid(renderer.getName())) {
         xWidget.setLabel(renderer.getName().replaceFirst("^.*?\\.", ""));
      }

      if (Strings.isValid(renderer.getToolTip())) {
         xWidget.setToolTip(renderer.getToolTip());
      }

      // TODO fix required for completed / required for transition
      xWidget.setRequiredEntry(renderer.getOptions().contains(WidgetOption.REQUIRED_FOR_COMPLETION));
      xWidget.setEditable(renderer.getOptions().contains(WidgetOption.EDITABLE) && isEditable);

      if (atsWidgetLayoutListener != null) {
         atsWidgetLayoutListener.widgetCreating(xWidget, toolkit, awa, this, xModListener, isEditable);
      }
      return xWidget;
   }
   private final XModifiedListener refreshRequiredModListener = new XModifiedListener() {
      @Override
      public void widgetModified(XWidget widget) {
         try {
            refreshOrAndXOrRequiredFlags();
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }
   };

   /**
    * Required flags are set per XWidget and the labels change from Red to Black when the widget has been edited
    * successfully. When a page is made up of two or more widgets that need to work together, these required flags need
    * to be set/unset whenever a widget from the group gets modified.
    */
   private void refreshOrAndXOrRequiredFlags() throws OseeCoreException {
      // Handle orRequired
      for (Collection<String> orReq : orRequired) {
         // If group is complete, change all to black, else all red
         boolean isComplete = isOrGroupFromAttrNameComplete(orReq.iterator().next());
         for (String aName : orReq) {
            IAtsWidgetDefinitionRender layoutData = getLayoutData(aName);
            Label label = layoutData.getXWidget().getLabelWidget();
            if (label != null && !label.isDisposed()) {
               label.setForeground(isComplete ? null : Displays.getSystemColor(SWT.COLOR_RED));
            }
         }
      }
      // Handle xorRequired
      for (Collection<String> xorReq : xorRequired) {
         // If group is complete, change all to black, else all red
         boolean isComplete = isXOrGroupFromAttrNameComplete(xorReq.iterator().next());
         for (String aName : xorReq) {
            IAtsWidgetDefinitionRender layoutData = getLayoutData(aName);
            Label label = layoutData.getXWidget().getLabelWidget();
            if (label != null && !label.isDisposed()) {
               label.setForeground(isComplete ? null : Displays.getSystemColor(SWT.COLOR_RED));
            }
         }
      }
   }

   public boolean isOrRequired(String attrName) {
      return !getOrRequiredGroup(attrName).isEmpty();
   }

   public boolean isXOrRequired(String attrName) {
      return !getXOrRequiredGroup(attrName).isEmpty();
   }

   private Collection<String> getOrRequiredGroup(String attrName) {
      return getRequiredGroup(orRequired, attrName);
   }

   private Collection<String> getXOrRequiredGroup(String attrName) {
      return getRequiredGroup(xorRequired, attrName);
   }

   private Collection<String> getRequiredGroup(Collection<ArrayList<String>> requiredList, String attrName) {
      for (Collection<String> list : requiredList) {
         for (String aName : list) {
            if (aName.equals(attrName)) {
               return list;
            }
         }
      }
      return Collections.emptyList();
   }

   /**
    * @return true if ANY item in group is entered
    */
   public boolean isOrGroupFromAttrNameComplete(String name) throws OseeCoreException {
      for (String aName : getOrRequiredGroup(name)) {
         IAtsWidgetDefinitionRender layoutData = getLayoutData(aName);
         if (layoutData.getXWidget() != null && layoutData.getXWidget().isValid().isOK()) {
            return true;
         }
      }
      return false;
   }

   /**
    * @return true if only ONE item in group is entered
    */
   public boolean isXOrGroupFromAttrNameComplete(String attrName) throws OseeCoreException {
      boolean oneFound = false;
      for (String aName : getXOrRequiredGroup(attrName)) {
         IAtsWidgetDefinitionRender layoutData = getLayoutData(aName);
         if (layoutData.getXWidget() != null && layoutData.getXWidget().isValid().isOK()) {
            // If already found one, return false
            if (oneFound) {
               return false;
            } else {
               oneFound = true;
            }
         }
      }
      return oneFound;
   }

   private void setupArtifactInfo(Artifact artifact, String attributeName, XWidget xWidget) {
      if (artifact == null) {
         return;
      }
      if (xWidget instanceof IAttributeWidget) {
         try {
            IAttributeType attributeType = AttributeTypeManager.getType(attributeName);
            ((IAttributeWidget) xWidget).setAttributeType(artifact, attributeType);
         } catch (Exception ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      } else if (xWidget instanceof IArtifactWidget) {
         try {
            ((IArtifactWidget) xWidget).setArtifact(artifact);
         } catch (Exception ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }
   }

   private Composite createComposite(Composite parent, FormToolkit toolkit) {
      return toolkit != null ? toolkit.createComposite(parent, SWT.WRAP) : new Composite(parent, SWT.NONE);
   }

   public String getHtml(String backgroundColor) throws OseeCoreException {
      return getHtml(backgroundColor, "", "");
   }

   public String getHtml(String backgroundColor, String preHtml, String postHtml) throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      sb.append(AHTML.startBorderTable(100, backgroundColor, getName()));
      if (preHtml != null) {
         sb.append(preHtml);
      }
      for (IAtsWidgetDefinitionRender renderer : widgetItems) {
         XWidget xWidget = renderer.getXWidget();
         if (xWidget instanceof IArtifactWidget) {
            ((IArtifactWidget) xWidget).setArtifact(renderer.getArtifact());
         }
         sb.append(renderer.getXWidget().toHTML(AHTML.LABEL_FONT));
         sb.append(AHTML.newline());
      }
      if (postHtml != null) {
         sb.append(postHtml);
      }
      sb.append(AHTML.endBorderTable());
      return sb.toString();
   }

   @Override
   public String toString() {
      StringBuffer sb =
         new StringBuffer(
            stateDefinition.getName() + (stateDefinition.getName() != null ? " (" + stateDefinition.getName() + ") " : "") + "\n");
      try {
         for (IAtsStateDefinition page : stateDefinition.getToStates()) {
            sb.append("-> " + page.getName() + (stateDefinition.getOverrideAttributeValidationStates().contains(page) ? " (return)" : "") + "\n");
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return sb.toString();
   }

   private List<IAtsWidgetDefinitionRender> getlayoutDatas() {
      return widgetItems;
   }

   private void addLayoutData(IAtsWidgetDefinitionRender data) {
      widgetItems.add(data);
   }

   public IAtsWidgetDefinitionRender getLayoutData(String layoutName) {
      for (IAtsWidgetDefinitionRender item : widgetItems) {
         if (item.getName().equals(layoutName)) {
            return item;
         }
      }
      return null;
   }

   @Override
   public String getName() {
      return stateDefinition.getName();
   }

   @Override
   public StateType getStateType() {
      return stateDefinition.getStateType();
   }

   public String getFullName() {
      return stateDefinition.getFullName();
   }

   public List<IAtsStateDefinition> getToPages() {
      return stateDefinition.getToStates();
   }

   public IAtsStateDefinition getDefaultToPage() {
      if (stateDefinition.getDefaultToState() != null) {
         return stateDefinition.getDefaultToState();
      }
      return null;
   }

   public IAtsStateDefinition getStateDefinition() {
      return stateDefinition;
   }

   public IAtsWorkDefinition getWorkDefinition() {
      return workDefinition;
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

   @Override
   public String getDescription() {
      return null;
   }

   public AbstractWorkflowArtifact getSma() {
      return sma;
   }

   public void setsma(AbstractWorkflowArtifact sma) {
      this.sma = sma;
   }

   public boolean isCurrentState(AbstractWorkflowArtifact sma) {
      return sma.isInState(this);
   }

   public boolean isCurrentNonCompleteCancelledState(AbstractWorkflowArtifact sma) {
      return isCurrentState(sma) && !getStateType().isCompletedOrCancelledState();
   }

   public void widgetCreated(XWidget xWidget, FormToolkit toolkit, Artifact art, IAtsStateDefinition stateDef, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      // Check extension points for page creation
      if (sma != null) {
         for (IAtsStateItem item : AtsStateItemManager.getStateItems()) {
            item.xWidgetCreated(xWidget, toolkit, stateDef, art, isEditable);
         }
      }
   }

   @Override
   public void createXWidgetLayoutData(IAtsWidgetDefinitionRender layoutData, XWidget xWidget, FormToolkit toolkit, Artifact art, XModifiedListener xModListener, boolean isEditable) {

      // If no tool tip, add global tool tip
      if (!Strings.isValid(xWidget.getToolTip())) {
         String description = "";
         if (layoutData.getXWidgetName().equals(XCommitManager.WIDGET_NAME)) {
            description = XCommitManager.DESCRIPTION;
         }
         IAttributeType type = AtsAttributeTypes.getTypeByName(layoutData.getStoreName());
         if (type != null && Strings.isValid(type.getDescription())) {
            description = type.getDescription();
         }
         if (Strings.isValid(description)) {
            xWidget.setToolTip(description);
            layoutData.setToolTip(description);
         }
      }
      // Store workAttr in control for use by help
      if (xWidget.getControl() != null) {
         xWidget.getControl().setData(layoutData);
      }

   }

   public void widgetCreating(XWidget xWidget, FormToolkit toolkit, Artifact art, IAtsStateDefinition stateDefinition, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      // Check extension points for page creation
      if (sma != null) {
         for (IAtsStateItem item : AtsStateItemManager.getStateItems()) {
            Result result = item.xWidgetCreating(xWidget, toolkit, stateDefinition, art, isEditable);
            if (result.isFalse()) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Error in page creation => " + result.getText());
            }
         }
      }
   }

   public List<IAtsWidgetDefinitionRender> getWidgetItems() {
      return widgetItems;
   }

   public Collection<XWidget> getXWidgets() throws OseeCoreException {
      List<XWidget> widgets = new ArrayList<XWidget>();
      for (IAtsWidgetDefinitionRender render : widgetItems) {
         widgets.add(render.getXWidget());
      }
      return widgets;
   }

   public XModifiedListener getXModifiedListener() {
      return xModListener;
   }

   public void setXModifiedListener(XModifiedListener xModListener) {
      this.xModListener = xModListener;
   }

   public List<Label> getLabels() {
      return labels;
   }

}
