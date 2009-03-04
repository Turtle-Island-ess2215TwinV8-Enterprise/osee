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
package org.eclipse.osee.framework.ui.skynet.group;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Donald G. Dunne
 */
public class GroupTreeViewer extends TreeViewer {

   private final GroupExplorer groupExplorer;

   /**
    * @param parent
    */
   public GroupTreeViewer(GroupExplorer groupExplorer, Composite parent) {
      super(parent);
      this.groupExplorer = groupExplorer;
   }

   /* (non-Javadoc)
    * @see org.eclipse.jface.viewers.StructuredViewer#refresh()
    */
   @Override
   public void refresh() {
      super.refresh();
      //      System.out.println("TreeViewer: refresh");
      groupExplorer.restoreExpandedAndSelection();
   }

   /* (non-Javadoc)
    * @see org.eclipse.jface.viewers.StructuredViewer#refresh(boolean)
    */
   @Override
   public void refresh(boolean updateLabels) {
      super.refresh(updateLabels);
      //      System.out.println("TreeViewer: refresh(updateLabels)");
      groupExplorer.restoreExpandedAndSelection();
   }

}
