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
package org.eclipse.osee.ats.workflow.editor.model;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;

/**
 * A rectangular shape.
 * 
 * @author Donald G. Dunne
 */
public class RectangleShape extends Shape {
   /** A 16x16 pictogram of a rectangular shape. */

   public RectangleShape() {
      setSize(new Dimension(100, 50));
   }

   @SuppressWarnings("unused")
   @Override
   public Result validForSave() throws OseeCoreException {
      return Result.TrueResult;
   }

   @Override
   public Image getIcon() {
      return ImageManager.getImage(FrameworkImage.RECTANGLE_16);
   }

   @Override
   protected String getName() {
      return null;
   }

   @Override
   protected String getToolTip() {
      return null;
   }

   @SuppressWarnings("unused")
   @Override
   public Result doSave(SkynetTransaction transaction) throws OseeCoreException {
      return Result.TrueResult;
   }

}