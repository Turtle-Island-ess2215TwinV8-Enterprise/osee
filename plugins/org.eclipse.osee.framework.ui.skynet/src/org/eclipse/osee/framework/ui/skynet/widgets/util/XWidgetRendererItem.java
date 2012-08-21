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

import org.eclipse.osee.framework.core.enums.WidgetOption;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.WidgetOptionHandler;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;

/**
 * @author Donald G. Dunne
 */
public class XWidgetRendererItem implements IXWidgetRendererItem, Cloneable {

   private static final FrameworkXWidgetProvider xWidgetFactory = FrameworkXWidgetProvider.getInstance();
   private static final String UNKNOWN = "Unknown";
   private static final int DEFAULT_HEIGHT = 9999;

   private String name = "Unknown";
   private String id = "";
   private String storeName = "";
   private String xWidgetName = UNKNOWN;

   private XWidget xWidget;
   private int beginComposite = 0; // If >0, indicates new child composite with columns == value
   private int beginGroupComposite = 0; // If >0, indicates new child composite with columns == value
   private boolean endComposite, endGroupComposite; // indicated end of child composite
   private int height = DEFAULT_HEIGHT;
   private String toolTip;
   private SwtXWidgetRenderer dynamicXWidgetLayout;
   private String defaultValue;
   private String keyedBranchName;
   private final WidgetOptionHandler xOptionHandler = new WidgetOptionHandler();
   private Artifact artifact;
   private Object object;
   private String beginTabFolder = null;
   private boolean endTabFolder;
   private String beginTabItem = null;
   private boolean endTabItem;
   private String tabItemDescription = null;

   public XWidgetRendererItem(SwtXWidgetRenderer dynamicXWidgetLayout, WidgetOption... options) {
      this.dynamicXWidgetLayout = dynamicXWidgetLayout;
      xOptionHandler.add(WidgetOption.EDITABLE);
      xOptionHandler.add(WidgetOption.ALIGN_LEFT);
      for (WidgetOption option : options) {
         xOptionHandler.add(option);
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   public boolean isHeightSet() {
      return height != DEFAULT_HEIGHT;
   }

   @Override
   public String toString() {
      return getName();
   }

   @Override
   public String getName() {
      return name.replaceFirst("^.*?\\.", "");
   }

   @Override
   public String getStoreName() {
      return storeName;
   }

   public void setStoreName(String storeName) {
      this.storeName = storeName;
   }

   public boolean isRequired() {
      if (xOptionHandler.contains(WidgetOption.REQUIRED_FOR_COMPLETION)) {
         return true;
      }
      if (dynamicXWidgetLayout != null) {
         return dynamicXWidgetLayout.isOrRequired(getStoreName()) || //
         dynamicXWidgetLayout.isXOrRequired(getStoreName());
      }
      return false;
   }

   public boolean isRequiredForCompletion() {
      if (xOptionHandler.contains(WidgetOption.REQUIRED_FOR_COMPLETION)) {
         return true;
      }
      return false;
   }

   @Override
   public String getXWidgetName() {
      return xWidgetName;
   }

   public void setXWidgetName(String widget) {
      xWidgetName = widget;
   }

   public void setName(String name) {
      this.name = name;
   }

   // TODO This method will need to be removed
   public XWidget getXWidget() throws OseeCoreException {
      if (xWidget == null) {
         xWidget = xWidgetFactory.createXWidget(this);
      }
      return xWidget;
   }

   public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
   }

   public int getHeight() {
      return height;
   }

   public void setHeight(int height) {
      this.height = height;
   }

   public int getBeginComposite() {
      if (xOptionHandler.contains(WidgetOption.BEGIN_COMPOSITE_10)) {
         return 10;
      }
      if (xOptionHandler.contains(WidgetOption.BEGIN_COMPOSITE_8)) {
         return 8;
      }
      if (xOptionHandler.contains(WidgetOption.BEGIN_COMPOSITE_6)) {
         return 6;
      }
      if (xOptionHandler.contains(WidgetOption.BEGIN_COMPOSITE_4)) {
         return 4;
      }
      return beginComposite;
   }

   public int getBeginGroupComposite() {
      if (xOptionHandler.contains(WidgetOption.BEGIN_GROUP_COMPOSITE_10)) {
         return 10;
      }
      if (xOptionHandler.contains(WidgetOption.BEGIN_GROUP_COMPOSITE_8)) {
         return 8;
      }
      if (xOptionHandler.contains(WidgetOption.BEGIN_GROUP_COMPOSITE_6)) {
         return 6;
      }
      if (xOptionHandler.contains(WidgetOption.BEGIN_GROUP_COMPOSITE_4)) {
         return 4;
      }
      return beginGroupComposite;
   }

   public void setBeginComposite(int beginComposite) {
      this.beginComposite = beginComposite;
   }

   public void setBeginGroupComposite(int beginGroupComposite) {
      this.beginGroupComposite = beginGroupComposite;
   }

   public boolean isEndComposite() {
      return endComposite;
   }

   public boolean isEndGroupComposite() {
      return endGroupComposite;
   }

   public void setEndComposite(boolean endComposite) {
      this.endComposite = endComposite;
   }

   public void setEndGroupComposite(boolean endGroupComposite) {
      this.endGroupComposite = endGroupComposite;
   }

   @Override
   public String getToolTip() {
      return toolTip;
   }

   public void setToolTip(String toolTip) {
      this.toolTip = toolTip;
   }

   public SwtXWidgetRenderer getDynamicXWidgetLayout() {
      return dynamicXWidgetLayout;
   }

   @Override
   public String getDefaultValue() {
      return defaultValue;
   }

   public void setKeyedBranchName(String keyedBranchName) {
      this.keyedBranchName = keyedBranchName;
   }

   public String getKeyedBranchName() {
      return keyedBranchName;
   }

   public void setDynamicXWidgetLayout(SwtXWidgetRenderer dynamicXWidgetLayout) {
      this.dynamicXWidgetLayout = dynamicXWidgetLayout;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public WidgetOptionHandler getXOptionHandler() {
      return xOptionHandler;
   }

   @Override
   public Artifact getArtifact() {
      return artifact;
   }

   public void setArtifact(Artifact artifact) {
      this.artifact = artifact;
   }

   /**
    * Generic object passed along to the created XWidget
    */
   public void setObject(Object object) {
      this.object = object;
   }

   @Override
   public Object getObject() {
      return object;
   }

   public String getBeginTabFolder() {
      return beginTabFolder;
   }

   public void setBeginTabFolder(String beginTabFolder) {
      this.beginTabFolder = beginTabFolder;
   }

   public boolean isEndTabFolder() {
      return endTabFolder;
   }

   public void setEndTabFolder(boolean endTabFolder) {
      this.endTabFolder = endTabFolder;
   }

   public String getBeginTabItem() {
      return beginTabItem;
   }

   public void setBeginTabItem(String beginTabItem) {
      this.beginTabItem = beginTabItem;
   }

   public boolean isEndTabItem() {
      return endTabItem;
   }

   public void setEndTabItem(boolean endTabItem) {
      this.endTabItem = endTabItem;
   }

   public boolean isBeginTabFolder() {
      return Strings.isValid(beginTabFolder);
   }

   public boolean isBeginTabItem() {
      return Strings.isValid(beginTabItem);
   }

   public String getTabItemDescription() {
      return tabItemDescription;
   }

   public void setTabItemDescription(String tabItemDescription) {
      this.tabItemDescription = tabItemDescription;
   }

   @Override
   public WidgetOptionHandler getWidgetOptionHandler() {
      return xOptionHandler;
   }

   @Override
   public IXWidgetOptionResolver getOptionResolver() {
      return dynamicXWidgetLayout.getOptionResolver();
   }

}