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
package org.eclipse.osee.framework.ui.skynet.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTransfer;
import org.eclipse.osee.framework.ui.skynet.ArtifactLabelProvider;
import org.eclipse.osee.framework.ui.skynet.util.SkynetDragAndDrop;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Ryan D. Brooks
 */
public class XListDropViewer extends XListViewer {
   private MenuItem removeFromMenuItem;
   private TableViewer myTableViewer;
   private ArrayContentProvider myArrayContentProvider = null;
   private ArtifactLabelProvider myArtifactLabelProvider = null;

   //   private Map<Object, Object> mySelectedItemsMap = new HashMap<Object, Object>();

   /**
    * @param displayLabel
    */
   public XListDropViewer(String displayLabel) {
      super(displayLabel);
      this.myArrayContentProvider = new ArrayContentProvider();
      setContentProvider(this.myArrayContentProvider);
      this.myArtifactLabelProvider = new ArtifactLabelProvider();
      setLabelProvider(this.myArtifactLabelProvider);
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.XWidget#createWidgets(org.eclipse.ui.forms.widgets.FormToolkit, org.eclipse.swt.widgets.Composite, int)
    */
   @Override
   public void createWidgets(FormToolkit toolkit, Composite parent, int horizontalSpan) {
      Menu popupMenu = definePopup(parent);
      setMultiSelect(true);
      super.setListMenu(popupMenu);
      super.createWidgets(toolkit, parent, horizontalSpan);
      this.myTableViewer = super.getTableViewer();
      defineMenus(popupMenu);
   }

   private Menu definePopup(Composite parent) {
      Menu popupMenu = new Menu(parent);
      return popupMenu;
   }

   private void defineMenus(Menu popupMenu) {
      //      popupMenu.addMenuListener(new MenuEnablingListener());
      createRemoveFromMenuItem(popupMenu);
      myTableViewer.getTable().setMenu(popupMenu);
      return;
   }

   private void createRemoveFromMenuItem(Menu popupMenu) {
      removeFromMenuItem = new MenuItem(popupMenu, SWT.PUSH);
      removeFromMenuItem.setText("Remove From This Blam's Parameters ");
      removeFromMenuItem.addSelectionListener(new SelectionListener() {

         public void widgetSelected(SelectionEvent event) {
            IStructuredSelection structuredSelection = (IStructuredSelection) myTableViewer.getSelection();
            Iterator<?> iterator = structuredSelection.iterator();

            Object orginalInput = getInput();
            Collection<Object> modList = getCollectionInput();

            while (iterator.hasNext()) {
               modList.remove(iterator.next());
            }

            myArrayContentProvider.inputChanged(myTableViewer, orginalInput, modList);
            refresh();
            //old
            //            Object myGetInputObject = myTableViewer.getInput();
            //            List myFullList = null;
            //            if (myGetInputObject instanceof List) {
            //               myFullList = (List<Object>) myGetInputObject;
            //               List myAdjustedList = myFullList;
            //               for (int i = 0; i < myAdjustedList.size(); i++) {
            //                  if (mySelectedItemsMap.containsKey(myAdjustedList.get(i))) {
            //                     myTableViewer.remove(myAdjustedList.get(i));
            //                     myAdjustedList.remove(i);
            //                  }
            //               }
            //               myArrayContentProvider.inputChanged(myTableViewer, myFullList, myAdjustedList);
            //            }
         }

         public void widgetDefaultSelected(SelectionEvent ev) {
         }
      });
   }

   //   public class MenuEnablingListener implements MenuListener {
   //
   //      public void menuHidden(MenuEvent e) {
   //      }
   //
   //      @SuppressWarnings("unchecked")
   //      public void menuShown(MenuEvent e) {
   //         IStructuredSelection myIStructuredSelection = (IStructuredSelection) myTableViewer.getSelection();
   //         mySelectedItemsMap.clear();
   //         if (!myIStructuredSelection.isEmpty()) {
   //            List<Object> mySelectedItems = myIStructuredSelection.toList();
   //            for (Object mySelectedItem : mySelectedItems) {
   //               mySelectedItemsMap.put(mySelectedItem, mySelectedItem);
   //            }
   //         }
   //         removeFromMenuItem.setEnabled(!myIStructuredSelection.isEmpty());
   //      }
   //   }

   /**
    * Adds artifacts to the viewer's input.
    * 
    * @param artifacts
    */
   public void addToInput(Artifact... artifacts) {
      ArrayList<Object> objects = new ArrayList<Object>();

      for (Artifact artifact : artifacts) {
         objects.add((Object) artifact);
      }

      if (getInput() == null) {
         setInput(objects);
      } else {
         add(objects);
         updateListWidget();
      }
      notifyXModifiedListeners();
   }

   @Override
   public void createWidgets(Composite parent, int horizontalSpan) {
      super.createWidgets(parent, horizontalSpan);

      // the viewer must be initialized first so the control is not null.
      new XDragAndDrop();
   }

   @Override
   public Object getData() {
      return getInput();
   }

   private class XDragAndDrop extends SkynetDragAndDrop {
      public XDragAndDrop() {
         super(null, getControl(), "viewId");
      }

      @Override
      public void performDragOver(DropTargetEvent event) {
         if (ArtifactTransfer.getInstance().isSupportedType(event.currentDataType)) {
            event.detail = DND.DROP_COPY;
         }
      }

      @Override
      public Artifact[] getArtifacts() {
         return null;
      }

      @Override
      public void performArtifactDrop(Artifact[] dropArtifacts) {
         addToInput(dropArtifacts);
      }
   }
}
