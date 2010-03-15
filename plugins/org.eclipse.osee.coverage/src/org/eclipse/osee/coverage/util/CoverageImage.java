/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.coverage.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.KeyedImage;

/**
 * @author Donald G. Dunne
 */
public enum CoverageImage implements KeyedImage {
   COVERAGE("coverage.gif"),
   COVERAGE_IMPORT("coverageImport.gif"),
   COVERAGE_PACKAGE("coveragePackage.gif"),
   LINK("link.gif"),
   FOLDER_GREEN("folderGreenPlus.gif"),
   FOLDER_RED("folderRedX.gif"),
   UNIT("unit.gif"),
   UNIT_EDIT("unitEdit.gif"),
   UNIT_GREEN("unitGreenPlus.gif"),
   UNIT_RED("unitRedPlus.gif"),
   TEST_UNIT("testUnit.gif"),
   TEST_UNIT_GREEN("testUnitGreenPlus.gif"),
   TEST_UNIT_RED("testUnitRedPlus.gif"),
   ITEM("item.gif"),
   ITEMS("items.gif"),
   ITEM_EDIT("itemEdit.gif"),
   ITEM_GREEN("itemGreenPlus.gif"),
   ITEM_RED("itemRedPlus.gif");

   private final String fileName;

   private CoverageImage(String fileName) {
      this.fileName = fileName;
   }

   @Override
   public ImageDescriptor createImageDescriptor() {
      return ImageManager.createImageDescriptor(Activator.PLUGIN_ID, "images", fileName);
   }

   @Override
   public String getImageKey() {
      return Activator.PLUGIN_ID + "." + fileName;
   }
}