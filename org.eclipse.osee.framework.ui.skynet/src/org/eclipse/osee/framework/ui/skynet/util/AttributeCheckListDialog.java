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

package org.eclipse.osee.framework.ui.skynet.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.attribute.ConfigurationPersistenceManager;
import org.eclipse.osee.framework.skynet.core.attribute.DynamicAttributeDescriptor;
import org.eclipse.osee.framework.skynet.core.attribute.DynamicAttributeManager;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.ArtifactTreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * @author Donald G. Dunne
 */
public class AttributeCheckListDialog extends SelectionDialog {
   private CheckboxTreeViewer treeViewer;
   private final ArrayList<DynamicAttributeDescriptor> selectedAttributes;
   private String preferenceKey;

   public AttributeCheckListDialog(Shell parent, String preferenceKey) {
      this(parent, null, preferenceKey);
   }

   public AttributeCheckListDialog(Shell parent, Collection<DynamicAttributeDescriptor> attrTypes, String preferenceKey) {
      super(parent);
      setTitle("Select Attributes");
      setMessage("Select Attributes");
      this.selectedAttributes = new ArrayList<DynamicAttributeDescriptor>();
      this.preferenceKey = preferenceKey;

      if (attrTypes != null && !attrTypes.isEmpty()) {
         selectedAttributes.addAll(attrTypes);
      }
   }

   public String getSelectedAttributeData(Artifact artifact) {
      if (artifact == null) {
         throw new IllegalArgumentException("Attribute data can not be acquired for null artifact.");
      }

      StringBuilder result = new StringBuilder();
      try {
         for (DynamicAttributeManager attributeManager : artifact.getAttributes()) {
            if (selectedAttributes.contains(attributeManager.getDescriptor())) {
               result.append(" - " + attributeManager.getAttributesStr());
            }
         }
      } catch (SQLException ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
      return result.toString();
   }

   @Override
   protected Control createDialogArea(Composite container) {

      Composite comp = new Composite(container, SWT.NONE);
      comp.setLayout(new GridLayout(2, false));
      comp.setLayoutData(new GridData(GridData.FILL_BOTH));

      treeViewer =
            new CheckboxTreeViewer(comp,
                  SWT.MULTI | SWT.CHECK | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
      treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      treeViewer.setContentProvider(new ArtifactTreeContentProvider());
      treeViewer.setSorter(new AttributeViewerSorter());
      ArrayList<Object> objs = new ArrayList<Object>();
      for (Object obj : selectedAttributes)
         objs.add(obj);
      treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            selectedAttributes.clear();
            for (Object obj : treeViewer.getCheckedElements())
               selectedAttributes.add((DynamicAttributeDescriptor) obj);
         };
      });
      treeViewer.setLabelProvider(new LabelProvider() {

         public String getText(Object obj) {
            return obj.toString();
         }
      });
      try {
         treeViewer.setInput(ConfigurationPersistenceManager.getInstance().getDynamicAttributeDescriptors(
               BranchPersistenceManager.getInstance().getDefaultBranch()));
         treeViewer.setCheckedElements(objs.toArray(new Object[objs.size()]));
      } catch (SQLException ex) {
         SkynetGuiPlugin.getLogger().log(Level.SEVERE, ex.toString(), ex);
      }
      return container;
   }

   public class AttributeViewerSorter extends ViewerSorter {
      public AttributeViewerSorter() {
         super();
      }

      @SuppressWarnings("unchecked")
      public int compare(Viewer viewer, Object o1, Object o2) {
         return getComparator().compare(((DynamicAttributeDescriptor) o1).getName(),
               ((DynamicAttributeDescriptor) o2).getName());
      }
   }

   public boolean noneSelected() {
      return selectedAttributes.isEmpty();
   }

   /**
    * @return the selectedAttributes
    */
   public Collection<DynamicAttributeDescriptor> getSelectedAttributes() {
      return selectedAttributes;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @Override
   protected void okPressed() {
      super.okPressed();

      IPreferenceStore prefStore = SkynetGuiPlugin.getInstance().getPreferenceStore();
      prefStore.setValue(preferenceKey, Collections.toString(getSelectedAttributes(), "", "|", ""));
   }
}
