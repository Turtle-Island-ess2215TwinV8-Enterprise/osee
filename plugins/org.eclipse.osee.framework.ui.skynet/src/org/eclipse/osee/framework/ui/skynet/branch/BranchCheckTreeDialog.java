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

package org.eclipse.osee.framework.ui.skynet.branch;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.util.filteredTree.MinMaxOSEECheckedFilteredTreeDialog;
import org.eclipse.osee.framework.ui.skynet.widgets.xBranch.BranchViewImageHandler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class BranchCheckTreeDialog extends MinMaxOSEECheckedFilteredTreeDialog {

   private Collection<Branch> initialBranches;

   public BranchCheckTreeDialog(String title, String message, int minSelectionRequired, int maxSelectionRequired) {
      super(title, message, new ArrayTreeContentProvider(), new BranchLabelProvider(), new BranchNameSorter(),
         minSelectionRequired, maxSelectionRequired);
   }

   public Collection<Branch> getChecked() {
      if (super.getTreeViewer() == null) {
         return Collections.emptyList();
      }
      Set<Branch> checked = new HashSet<Branch>();
      for (Object obj : super.getTreeViewer().getChecked()) {
         checked.add((Branch) obj);
      }
      return checked;
   }

   @Override
   protected Control createDialogArea(Composite container) {
      Control comp = super.createDialogArea(container);
      try {
         List<Branch> branches =
            BranchManager.getBranches(BranchArchivedState.UNARCHIVED, BranchType.WORKING, BranchType.BASELINE);
         Collections.sort(branches);
         getTreeViewer().getViewer().setInput(branches);
         if (getInitialBranches() != null) {
            getTreeViewer().setInitalChecked(getInitialBranches());
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return comp;
   }

   /**
    * @return the initialBranches
    */
   public Collection<Branch> getInitialBranches() {
      return initialBranches;
   }

   /**
    * @param initialBranches the initialBranches to set
    */
   public void setInitialBranches(Collection<Branch> initialBranches) {
      this.initialBranches = initialBranches;
   }

   public static class BranchLabelProvider extends LabelProvider {
      @Override
      public Image getImage(Object element) {
         if (element instanceof Branch) {
            return BranchViewImageHandler.getImage(element, 0);
         }
         return null;
      }
   }

}
