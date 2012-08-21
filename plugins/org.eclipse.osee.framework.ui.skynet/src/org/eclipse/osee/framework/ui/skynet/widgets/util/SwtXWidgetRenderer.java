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
package org.eclipse.osee.framework.ui.skynet.widgets.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.xml.Jaxp;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.ui.skynet.XWidgetParser;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.widgets.IArtifactWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.IAttributeWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XOption;
import org.eclipse.osee.framework.ui.skynet.widgets.XText;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Jeff C. Phillips
 */
public class SwtXWidgetRenderer {
   public static final String XWIDGET = "XWidget";

   private final Set<XWidgetRendererItem> datas = new LinkedHashSet<XWidgetRendererItem>();
   private final Map<String, XWidgetRendererItem> nameToLayoutData = new HashMap<String, XWidgetRendererItem>();

   private final Collection<ArrayList<String>> orRequired = new ArrayList<ArrayList<String>>();
   private final Collection<ArrayList<String>> xorRequired = new ArrayList<ArrayList<String>>();

   private final IDynamicWidgetLayoutListener dynamicWidgetLayoutListener;
   private final IXWidgetOptionResolver optionResolver;
   private final Collection<XWidget> xWidgets = new ArrayList<XWidget>();
   private TabFolder currentTabFolder;
   private TabItem currentTabItem;
   private Composite tabFolderComp;

   public SwtXWidgetRenderer() {
      this(null, new DefaultXWidgetOptionResolver());
   }

   public SwtXWidgetRenderer(IDynamicWidgetLayoutListener dynamicWidgetLayoutListener, IXWidgetOptionResolver optionResolver) {
      this.dynamicWidgetLayoutListener = dynamicWidgetLayoutListener;
      this.optionResolver = optionResolver;
   }

   private Composite createComposite(Composite parent, FormToolkit toolkit) {
      return toolkit != null ? toolkit.createComposite(parent, SWT.WRAP) : new Composite(parent, SWT.NONE);
   }

   private Group buildGroupComposite(Composite given, String name, int numColumns, FormToolkit toolkit) {
      Group groupComp = new Group(given, SWT.None);
      if (Strings.isValid(name)) {
         groupComp.setText(name);
      }
      groupComp.setLayout(ALayout.getZeroMarginLayout(numColumns, false));
      groupComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      if (toolkit != null) {
         toolkit.adapt(groupComp);
      }
      return groupComp;
   }

   private Composite buildChildComposite(Composite given, int numColumns, FormToolkit toolkit) {
      Composite outComp = createComposite(given, toolkit);
      GridLayout zeroMarginLayout = ALayout.getZeroMarginLayout(numColumns, false);
      zeroMarginLayout.marginWidth = 4;
      zeroMarginLayout.horizontalSpacing = 8;
      outComp.setLayout(zeroMarginLayout);
      outComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      if (toolkit != null) {
         toolkit.adapt(outComp);
      }
      return outComp;
   }

   private XWidget setupXWidget(XWidgetRendererItem xWidgetLayoutData, boolean isEditable) throws OseeCoreException {
      XWidget xWidget = xWidgetLayoutData.getXWidget();
      xWidgets.add(xWidget);

      if (Strings.isValid(xWidgetLayoutData.getName())) {
         xWidget.setLabel(xWidgetLayoutData.getName().replaceFirst("^.*?\\.", ""));
      }

      if (Strings.isValid(xWidgetLayoutData.getToolTip())) {
         xWidget.setToolTip(xWidgetLayoutData.getToolTip());
      }

      xWidget.setRequiredEntry(xWidgetLayoutData.isRequired());
      xWidget.setEditable(xWidgetLayoutData.getXOptionHandler().contains(XOption.EDITABLE) && isEditable);

      return xWidget;
   }

   public void createBody(IManagedForm managedForm, Composite parent, Artifact artifact, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
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

      boolean inChildComposite = false;
      boolean inGroupComposite = false;
      Composite childComp = null;
      Group groupComp = null;
      // Create Attributes
      for (XWidgetRendererItem xWidgetLayoutData : getLayoutDatas()) {
         Composite currentComp = null;

         // first, check if this one is a group, if so, we set the group up and are done with this loop iteration
         int i = xWidgetLayoutData.getBeginGroupComposite();
         if (i > 0) {
            inGroupComposite = true;
            groupComp = buildGroupComposite(topLevelComp, xWidgetLayoutData.getName(), i, toolkit);
            continue;
         }
         if (inGroupComposite) {
            currentComp = groupComp;
            if (xWidgetLayoutData.isEndGroupComposite()) {
               inGroupComposite = false;
               // No XWidget associated, so go to next one
               continue;
            }

         } else {
            currentComp = topLevelComp;
         }

         if (xWidgetLayoutData.isBeginTabFolder()) {
            tabFolderComp = createCompositeForTabFolder(currentComp);

            if (Strings.isValid(xWidgetLayoutData.getBeginTabFolder())) {
               Label label = new Label(tabFolderComp, SWT.NONE);
               label.setText(xWidgetLayoutData.getBeginTabFolder());
            }
            currentTabFolder = new TabFolder(tabFolderComp, SWT.LEFT | SWT.NONE);
            currentTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

         }
         if (xWidgetLayoutData.isBeginTabItem()) {
            currentTabItem = new TabItem(currentTabFolder, 0);
            currentTabItem.setText(xWidgetLayoutData.getBeginTabItem());
            setInitialCompositeForTabItem(currentTabItem);

            if (Strings.isValid(xWidgetLayoutData.getTabItemDescription())) {
               Label label = new Label((Composite) currentTabItem.getControl(), SWT.NONE);
               label.setText("Description: " + xWidgetLayoutData.getTabItemDescription());
            }

         }

         if (currentComp == null) {
            System.out.println("debug: got null composite");
         }

         // defaults to grab horizontal, causes scrollbars on items that extend past the provided window space
         GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
         currentComp.setLayoutData(gd);

         if (xWidgetLayoutData.getXOptionHandler().contains(XOption.FILL_VERTICALLY)) {
            gd.grabExcessVerticalSpace = true;
         }

         int j = xWidgetLayoutData.getBeginComposite();
         if (j > 0) {
            inChildComposite = true;
            childComp = buildChildComposite(currentComp, j, toolkit);
         }

         if (inChildComposite) {
            currentComp = childComp;
            if (xWidgetLayoutData.isEndComposite()) {
               inChildComposite = false;
            }
         } else if (xWidgetLayoutData.getXOptionHandler().contains(XOption.HORIZONTAL_LABEL)) {
            currentComp = createComposite(topLevelComp, toolkit);
            currentComp.setLayout(ALayout.getZeroMarginLayout(2, false));
            currentComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            if (toolkit != null) {
               toolkit.adapt(currentComp);
            }
         }

         XWidget xWidget = setupXWidget(xWidgetLayoutData, isEditable);

         if (dynamicWidgetLayoutListener != null) {
            dynamicWidgetLayoutListener.widgetCreating(xWidget, toolkit, artifact, this, xModListener, isEditable);
         }

         setupArtifactInfo(artifact, xWidgetLayoutData, xWidget);

         if (xWidget instanceof XText) {
            XText xText = (XText) xWidget;
            if (xWidgetLayoutData.getXOptionHandler().contains(XOption.FILL_HORIZONTALLY)) {
               xText.setFillHorizontally(true);
            }
            if (xWidgetLayoutData.getXOptionHandler().contains(XOption.FILL_VERTICALLY)) {
               xText.setFillVertically(true);
            }
            if (xWidgetLayoutData.isHeightSet()) {
               xText.setHeight(xWidgetLayoutData.getHeight());
            }
            xText.setDynamicallyCreated(true);
         }

         if (currentTabItem != null) {
            xWidget.createWidgets(managedForm, (Composite) currentTabItem.getControl(), 2);
            ((Composite) currentTabItem.getControl()).layout();
         } else {
            xWidget.createWidgets(managedForm, currentComp, 2);
         }

         if (xModListener != null) {
            xWidget.addXModifiedListener(xModListener);
         }

         xWidget.addXModifiedListener(refreshRequiredModListener);
         if (dynamicWidgetLayoutListener != null) {
            dynamicWidgetLayoutListener.widgetCreated(xWidget, toolkit, artifact, this, xModListener, isEditable);
            dynamicWidgetLayoutListener.createXWidgetLayoutData(xWidgetLayoutData, xWidget, toolkit, artifact,
               xModListener, isEditable);
         }

         if (xWidgetLayoutData.isEndTabItem() && !xWidgetLayoutData.isBeginTabItem()) {
            currentTabItem = null;
         }
         if (xWidgetLayoutData.isEndTabFolder()) {
            currentTabFolder = null;
         }

      }
      topLevelComp.layout();

      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            try {
               for (XWidgetRendererItem xWidgetLayoutData : getLayoutDatas()) {
                  xWidgetLayoutData.getXWidget().validate();
               }
               refreshOrAndXOrRequiredFlags();
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });
   }

   private Composite createCompositeForTabFolder(Composite parent) {
      Composite comp = new Composite(parent, SWT.BORDER);
      GridLayout layout = new GridLayout(1, true);
      layout.marginWidth = 10;
      comp.setLayout(layout);
      comp.setLayoutData(new GridData(GridData.FILL_BOTH));
      return comp;
   }

   private void setInitialCompositeForTabItem(TabItem ti) {
      Composite comp = new Composite(ti.getParent(), SWT.NONE);
      comp.setLayout(new GridLayout(1, true));
      comp.setLayoutData(new GridData(GridData.FILL_BOTH));
      ti.setControl(comp);
   }

   private void setupArtifactInfo(Artifact artifact, XWidgetRendererItem xWidgetLayoutData, XWidget xWidget) {
      if (artifact == null) {
         return;
      }
      if (xWidget instanceof IAttributeWidget) {
         try {
            IAttributeType attributeType = AttributeTypeManager.getType(xWidgetLayoutData.getStoreName());
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
            XWidgetRendererItem layoutData = getLayoutData(aName);
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
            XWidgetRendererItem layoutData = getLayoutData(aName);
            Label label = layoutData.getXWidget().getLabelWidget();
            if (label != null && !label.isDisposed()) {
               label.setForeground(isComplete ? null : Displays.getSystemColor(SWT.COLOR_RED));
            }
         }
      }
   }

   public IStatus isPageComplete() {
      try {
         for (XWidgetRendererItem data : datas) {
            IStatus valid = data.getXWidget().isValid();
            if (!valid.isOK()) {
               // Check to see if widget is part of a completed OR or XOR group
               if (!isOrGroupFromAttrNameComplete(data.getStoreName()) && !isXOrGroupFromAttrNameComplete(data.getStoreName())) {
                  return valid;
               }
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return Status.OK_STATUS;
   }

   public Set<XWidgetRendererItem> getLayoutDatas() {
      return datas;
   }

   public void setLayoutDatas(List<XWidgetRendererItem> datas) {
      this.datas.clear();
      for (XWidgetRendererItem data : datas) {
         data.setDynamicXWidgetLayout(this);
         this.datas.add(data);
      }
   }

   public void addWorkLayoutDatas(List<XWidgetRendererItem> datas) {
      this.datas.addAll(datas);
   }

   public void addWorkLayoutData(XWidgetRendererItem data) {
      this.datas.add(data);
   }

   public XWidgetRendererItem getLayoutData(String attrName) {
      for (XWidgetRendererItem layoutData : datas) {
         if (layoutData.getStoreName().equals(attrName)) {
            return layoutData;
         }
      }
      return null;
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
         XWidgetRendererItem layoutData = getLayoutData(aName);
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
         XWidgetRendererItem layoutData = getLayoutData(aName);
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

   protected void processOrRequired(String instr) {
      ArrayList<String> names = new ArrayList<String>();
      for (String attr : instr.split(";")) {
         if (!attr.contains("[ \\s]*")) {
            names.add(attr);
         }
      }
      orRequired.add(names);
   }

   protected void processXOrRequired(String instr) {
      ArrayList<String> names = new ArrayList<String>();
      for (String attr : instr.split(";")) {
         if (!attr.contains("[ \\s]*")) {
            names.add(attr);
         }
      }
      xorRequired.add(names);
   }

   public void processlayoutDatas(String xWidgetXml) throws OseeCoreException {
      try {
         Document document = Jaxp.readXmlDocument(xWidgetXml);
         Element rootElement = document.getDocumentElement();

         List<XWidgetRendererItem> attrs = XWidgetParser.extractlayoutDatas(this, rootElement);
         for (XWidgetRendererItem attr : attrs) {
            nameToLayoutData.put(attr.getName(), attr);
            datas.add(attr);
         }
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
   }

   public void processLayoutDatas(Element element) {
      List<XWidgetRendererItem> layoutDatas = XWidgetParser.extractlayoutDatas(this, element);
      for (XWidgetRendererItem layoutData : layoutDatas) {
         nameToLayoutData.put(layoutData.getName(), layoutData);
         datas.add(layoutData);
      }
   }

   /**
    * @return the optionResolver
    */
   public IXWidgetOptionResolver getOptionResolver() {
      return optionResolver;
   }

   /**
    * @return the xWidgets
    */
   public Collection<XWidget> getXWidgets() {
      return xWidgets;
   }
}
