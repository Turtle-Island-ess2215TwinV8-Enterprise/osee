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

package org.eclipse.osee.ats.util.widgets.dialog;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.ats.artifact.ActionableItemArtifact;
import org.eclipse.osee.ats.artifact.ActionableItemManager;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactDescriptiveLabelProvider;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactNameSorter;
import org.eclipse.osee.framework.ui.skynet.widgets.XCheckBox;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;

/**
 * @author Donald G. Dunne
 */
public class ActionableItemTreeWithChildrenDialog extends CheckedTreeSelectionDialog {

   XCheckBox recurseChildrenCheck = new XCheckBox("Include all children Actionable Item Actions");
   boolean recurseChildren = false;
   protected Composite dialogComp;

   public ActionableItemTreeWithChildrenDialog(Active active) throws OseeCoreException {
      this(active, ActionableItemManager.getTopLevelActionableItems(active));
   }

   public ActionableItemTreeWithChildrenDialog(Active active, Collection<ActionableItemArtifact> ActionableItemArtifacts) {
      super(Displays.getActiveShell(), new ArtifactDescriptiveLabelProvider(), new ActionableItemTreeContentProvider(
         active));
      setTitle("Select Actionable Item");
      setMessage("Select Actionable Item");
      setComparator(new ArtifactNameSorter());
      setInput(ActionableItemArtifacts);
   }

   /**
    * @return selected AIs and children if recurseChildren was checked
    */
   public Collection<ActionableItemArtifact> getResultAndRecursedAIs() throws OseeCoreException {
      Set<ActionableItemArtifact> aias = new HashSet<ActionableItemArtifact>(10);
      for (Object obj : getResult()) {
         aias.add((ActionableItemArtifact) obj);
         if (recurseChildren) {
            aias.addAll(Artifacts.getChildrenOfTypeSet((Artifact) obj, ActionableItemArtifact.class, true));
         }
      }
      return aias;
   }

   @Override
   protected Control createDialogArea(Composite container) {

      Control control = super.createDialogArea(container);
      dialogComp = new Composite(control.getParent(), SWT.NONE);
      dialogComp.setLayout(new GridLayout(2, false));
      dialogComp.setLayoutData(new GridData(GridData.FILL_BOTH));

      recurseChildrenCheck.createWidgets(dialogComp, 2);
      recurseChildrenCheck.set(recurseChildren);
      recurseChildrenCheck.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            recurseChildren = recurseChildrenCheck.isSelected();
         };
      });

      return container;
   }

   public boolean isRecurseChildren() {
      return recurseChildren;
   }

   public void setRecurseChildren(boolean recurseChildren) {
      this.recurseChildren = recurseChildren;
   }

}