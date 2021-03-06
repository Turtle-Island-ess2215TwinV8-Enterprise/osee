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
package org.eclipse.osee.coverage.util;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.osee.coverage.internal.ServiceProvider;
import org.eclipse.osee.coverage.model.WorkProductAction;
import org.eclipse.osee.framework.ui.skynet.cm.IOseeCmService.ImageType;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;

public class WorkProductActionLabelProvider implements ILabelProvider {

   @Override
   public Image getImage(Object arg0) {
      if (arg0 instanceof WorkProductAction) {
         return ImageManager.getImage(ServiceProvider.getOseeCmService().getImage(ImageType.Pcr));
      } else {
         return ImageManager.getImage(ServiceProvider.getOseeCmService().getImage(ImageType.Task));
      }
   }

   @Override
   public String getText(Object arg0) {
      if (arg0 instanceof WorkProductAction) {
         return String.format("%s - %d Tasks", arg0.toString(), ((WorkProductAction) arg0).getTasks().size());
      }
      return arg0.toString();
   }

   @Override
   public void addListener(ILabelProviderListener arg0) {
      // do nothing
   }

   @Override
   public void dispose() {
      // do nothing
   }

   @Override
   public boolean isLabelProperty(Object arg0, String arg1) {
      return false;
   }

   @Override
   public void removeListener(ILabelProviderListener arg0) {
      // do nothing
   }

}