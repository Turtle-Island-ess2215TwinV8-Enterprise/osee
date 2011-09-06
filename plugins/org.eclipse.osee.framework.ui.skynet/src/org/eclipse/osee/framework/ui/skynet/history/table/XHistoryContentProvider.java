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
package org.eclipse.osee.framework.ui.skynet.history.table;

import java.util.Collection;
import java.util.Collections;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Jeff C. Phillips
 */
public class XHistoryContentProvider implements ITreeContentProvider {

   private final HistoryXViewer historyXViewer;
   private static Object[] EMPTY_ARRAY = new Object[0];

   public XHistoryContentProvider(HistoryXViewer historyXViewer) {
      this.historyXViewer = historyXViewer;
   }

   @Override
   public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof Collection) {
         return ((Collection<?>) parentElement).toArray();
      }
      return EMPTY_ARRAY;
   }

   @Override
   public Object getParent(Object element) {
      return null;
   }

   @Override
   public boolean hasChildren(Object element) {
      if (element instanceof Collection) {
         return true;
      }
      return false;
   }

   @Override
   public Object[] getElements(Object inputElement) {
      if (inputElement instanceof Collection) {
         return ((Collection<?>) inputElement).toArray();
      }
      return EMPTY_ARRAY;
   }

   @Override
   public void dispose() {
      // do nothing
   }

   @Override
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      // do nothing
   }

   public HistoryXViewer getChangeXViewer() {
      return historyXViewer;
   }

   public void clear(boolean forcePend) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            historyXViewer.setInput(Collections.emptyList());
            historyXViewer.refresh();
         };
      }, forcePend);
   }

}
