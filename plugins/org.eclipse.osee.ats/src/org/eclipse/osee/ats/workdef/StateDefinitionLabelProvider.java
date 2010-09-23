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
package org.eclipse.osee.ats.workdef;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public class StateDefinitionLabelProvider implements ILabelProvider {

   @Override
   public Image getImage(Object arg0) {
      return null;
   }

   @Override
   public String getText(Object arg0) {
      return ((StateDefinition) arg0).getPageName() + getCompletedAppend((StateDefinition) arg0);
   }

   private String getCompletedAppend(StateDefinition page) {
      if (page.isCompletedPage() && !page.getName().startsWith("Complete")) {
         if (!page.getPageName().equals("Completed")) {
            return " (Completed)";
         }
      }
      return "";
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