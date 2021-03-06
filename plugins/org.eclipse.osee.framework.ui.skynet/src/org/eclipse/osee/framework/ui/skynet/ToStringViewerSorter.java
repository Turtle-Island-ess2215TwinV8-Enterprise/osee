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
package org.eclipse.osee.framework.ui.skynet;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @author Donald G. Dunne
 */
public class ToStringViewerSorter extends ViewerSorter {

   public ToStringViewerSorter() {
      super();
   }

   @SuppressWarnings("unchecked")
   @Override
   public int compare(Viewer viewer, Object e1, Object e2) {
      return getComparator().compare(e1.toString(), e2.toString());
   }
}
