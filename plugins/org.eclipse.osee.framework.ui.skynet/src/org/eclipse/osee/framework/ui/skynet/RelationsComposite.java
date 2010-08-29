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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osee.framework.core.data.IRelationSorterId;
import org.eclipse.osee.framework.core.enums.IRelationEnumeration;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.RelationTypeSide;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactData;
import org.eclipse.osee.framework.skynet.core.event.FrameworkTransactionData;
import org.eclipse.osee.framework.skynet.core.event.IFrameworkTransactionEventListener;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.Sender;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeSideSorter;
import org.eclipse.osee.framework.ui.skynet.artifact.massEditor.MassArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.relation.explorer.RelationExplorerWindow;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.util.SkynetDragAndDrop;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

/**
 * <REM2> handled by IArtifactEditorEventHandler
 * 
 * @author Ryan D. Brooks
 */
public class RelationsComposite extends Composite implements IFrameworkTransactionEventListener {
   private TreeViewer treeViewer;
   private Tree tree;
   private NeedSelectedArtifactListener needSelectedArtifactListener;
   private final IDirtiableEditor editor;
   public static final String VIEW_ID = "osee.define.relation.RelationExplorer";
   public static final String[] columnNames = new String[] {" ", "Rationale"};
   public static final Integer[] columnLengths = new Integer[] {500, 50};
   // the index of column order
   private static int COLUMN_ORDER = 1;

   private MenuItem openMenuItem;
   private MenuItem wordPreviewItem;
   //   private MenuItem openWithMenuItem;
   private MenuItem editMenuItem;
   private MenuItem viewRelationTreeItem;
   private MenuItem orderRelationMenuItem;
   private MenuItem deleteRelationMenuItem;
   private MenuItem deleteArtifactMenuItem;
   private MenuItem massEditMenuItem;

   private final Artifact artifact;
   private final RelationLabelProvider relationLabelProvider;
   private final ToolBar toolBar;

   public RelationsComposite(IDirtiableEditor editor, Composite parent, int style, Artifact artifact) {
      this(editor, parent, style, artifact, null);
   }

   public RelationsComposite(IDirtiableEditor editor, Composite parent, int style, Artifact artifact, ToolBar toolBar) {
      super(parent, style);

      if (artifact == null) {
         throw new IllegalArgumentException("Can not edit a null artifact");
      }

      this.artifact = artifact;
      this.editor = editor;
      this.relationLabelProvider = new RelationLabelProvider(artifact);

      createPartControl();
      OseeEventManager.addListener(this);
      this.toolBar = toolBar;
   }

   public TreeViewer getTreeViewer() {
      return treeViewer;
   }

   public void createPartControl() {
      this.setLayout(new GridLayout());
      this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      createTreeArea(this);
      createColumns();
      packColumnData();

      needSelectedArtifactListener = new NeedSelectedArtifactListener();
      createPopupMenu();
      setHelpContexts();
   }

   private void createTreeArea(Composite parent) {
      treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.NO_SCROLL);
      tree = treeViewer.getTree();
      tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      tree.setHeaderVisible(true);

      CellEditor[] editors = new CellEditor[columnNames.length];
      editors[1] = new TextCellEditor(tree);
      treeViewer.setCellEditors(editors);
      treeViewer.setCellModifier(new RelationCellModifier(treeViewer));
      treeViewer.setColumnProperties(columnNames);
      treeViewer.setContentProvider(new RelationContentProvider());
      treeViewer.setLabelProvider(relationLabelProvider);
      treeViewer.setUseHashlookup(true);
      treeViewer.setInput(new ArtifactRoot(artifact));

      treeViewer.addDoubleClickListener(new DoubleClickListener());
      treeViewer.getTree().addMouseListener(new MouseListener() {

         @Override
         public void mouseUp(MouseEvent e) {
            packColumnData();
         }

         @Override
         public void mouseDown(MouseEvent e) {
            // do nothing
         }

         @Override
         public void mouseDoubleClick(MouseEvent e) {
            // do nothing
         }
      });
      tree.addKeyListener(new KeySelectedListener());

      expandItemsThatHaveChildren();
      new RelationSkynetDragAndDrop(tree, VIEW_ID);
   }

   private void expandItemsThatHaveChildren() {
      //expand items that have children
      Object[] types = ((ITreeContentProvider) treeViewer.getContentProvider()).getChildren(treeViewer.getInput());
      for (Object obj : types) {
         if (obj instanceof RelationType) {
            RelationType type = (RelationType) obj;
            if (RelationManager.getRelatedArtifactsCount(artifact, type, null) > 0) {
               treeViewer.expandToLevel(obj, 1);
            }
         }
      }
   }

   private void createColumns() {
      for (int index = 0; index < columnNames.length; index++) {
         TreeColumn column = new TreeColumn(tree, SWT.LEFT, index);
         column.setText(columnNames[index]);
         column.setWidth(columnLengths[index]);
      }
   }

   private void packColumnData() {
      TreeColumn[] columns = treeViewer.getTree().getColumns();
      for (TreeColumn column : columns) {
         column.pack();
      }
   }

   public void createPopupMenu() {
      Menu popupMenu = new Menu(treeViewer.getTree().getParent());
      popupMenu.addMenuListener(needSelectedArtifactListener);

      createOpenMenuItem(popupMenu);
      createWordPreviewMenuItem(popupMenu);
      //      createOpenWithMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createEditMenuItem(popupMenu);
      createMassEditMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createViewRelationTreeMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createOrderRelationTreeMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createDeleteRelationMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);

      createExpandAllMenuItem(popupMenu);
      createSelectAllMenuItem(popupMenu);

      new MenuItem(popupMenu, SWT.SEPARATOR);
      createDeleteArtifactMenuItem(popupMenu);

      tree.setMenu(popupMenu);
   }

   public class DoubleClickListener implements IDoubleClickListener {
      @Override
      public void doubleClick(DoubleClickEvent event) {
         openViewer((IStructuredSelection) event.getSelection());
      }
   }

   private void createOrderRelationTreeMenuItem(final Menu parentMenu) {

      orderRelationMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
      orderRelationMenuItem.setText("&Order Relations");
      needSelectedArtifactListener.add(orderRelationMenuItem);

      Menu subMenu = new Menu(parentMenu);
      orderRelationMenuItem.setMenu(subMenu);

      List<IRelationSorterId> orderTypes = RelationManager.getRelationOrderTypes();
      for (IRelationSorterId id : orderTypes) {
         MenuItem idMenu = new MenuItem(subMenu, SWT.CASCADE | SWT.CHECK);
         idMenu.setText(id.getName());
         idMenu.addSelectionListener(new SelectionId(id));
      }

      parentMenu.addListener(SWT.Show, new Listener() {

         @Override
         public void handleEvent(Event event) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            Object[] objects = selection.toArray();
            if (objects.length == 1 && objects[0] instanceof RelationTypeSideSorter) {
               orderRelationMenuItem.setEnabled(true);
               try {
                  checkCurrentOrderStrategy(orderRelationMenuItem.getMenu(), (RelationTypeSideSorter) objects[0]);
               } catch (OseeCoreException ex) {
                  OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
               }
            } else {
               orderRelationMenuItem.setEnabled(false);
            }
         }

      });
      orderRelationMenuItem.setEnabled(true);
   }

   private void checkCurrentOrderStrategy(Menu menu, RelationTypeSideSorter rts) throws OseeCoreException {
      String relationOrderName = rts.getSorterName();

      for (MenuItem item : menu.getItems()) {
         String itemName = item.getText();
         if (itemName.equals(relationOrderName)) {
            item.setSelection(true);
         } else {
            item.setSelection(false);
         }
      }
   }

   private class SelectionId implements SelectionListener {

      private final IRelationSorterId id;

      SelectionId(IRelationSorterId id) {
         this.id = id;
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
         // do nothing
      }

      @Override
      public void widgetSelected(SelectionEvent e) {
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
         Object[] objects = selection.toArray();
         if (objects.length == 1 && objects[0] instanceof RelationTypeSideSorter) {
            RelationTypeSideSorter typeSide = (RelationTypeSideSorter) objects[0];
            try {
               typeSide.getArtifact().setRelationOrder(typeSide, id);
            } catch (OseeCoreException ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
            }
         }
      }
   }

   private void createDeleteRelationMenuItem(final Menu parentMenu) {
      deleteRelationMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
      deleteRelationMenuItem.setText("&Delete Relation");
      needSelectedArtifactListener.add(deleteRelationMenuItem);
      deleteRelationMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            performDeleteRelation(selection);
         }
      });

      deleteRelationMenuItem.setEnabled(true);
   }

   private void createDeleteArtifactMenuItem(final Menu parentMenu) {
      deleteArtifactMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
      deleteArtifactMenuItem.setText("&Delete Artifact");
      needSelectedArtifactListener.add(deleteArtifactMenuItem);
      deleteArtifactMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();

            performDeleteArtifact(selection);
         }
      });

      deleteArtifactMenuItem.setEnabled(true);
   }

   private void createMassEditMenuItem(final Menu parentMenu) {
      massEditMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
      massEditMenuItem.setText("&Mass Edit");
      needSelectedArtifactListener.add(massEditMenuItem);
      massEditMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();

            performMassEdit(selection);
         }
      });

      massEditMenuItem.setEnabled(true);
   }

   private void createViewRelationTreeMenuItem(Menu menu) {
      viewRelationTreeItem = new MenuItem(menu, SWT.PUSH);
      viewRelationTreeItem.setText("&View Relation Table Report");
      viewRelationTreeItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            TreeViewerReport report =
               new TreeViewerReport("Relation View Report for " + artifact.getName(), treeViewer);
            ArrayList<Integer> ignoreCols = new ArrayList<Integer>();
            ignoreCols.add(COLUMN_ORDER);
            report.setIgnoreColumns(ignoreCols);
            report.open();
         }
      });
   }

   private void createOpenMenuItem(Menu parentMenu) {
      openMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      openMenuItem.setText("Open");
      needSelectedArtifactListener.addArtifactEnabled(openMenuItem);
      openMenuItem.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent event) {
            openViewer((IStructuredSelection) treeViewer.getSelection());
         }
      });
   }

   private void openViewer(IStructuredSelection selection) {
      for (Object object : selection.toArray()) {
         if (object instanceof WrapperForRelationLink) {
            WrapperForRelationLink link = (WrapperForRelationLink) object;
            try {
               RendererManager.open(link.getOther(), PresentationType.DEFAULT_OPEN);
            } catch (OseeCoreException ex) {
               OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      }
   }

   private void createWordPreviewMenuItem(Menu parentMenu) {
      wordPreviewItem = new MenuItem(parentMenu, SWT.PUSH);
      wordPreviewItem.setText("Open Preview");
      needSelectedArtifactListener.addArtifactEnabled(wordPreviewItem);
      wordPreviewItem.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent event) {
            openWordViewer((IStructuredSelection) treeViewer.getSelection());
         }
      });
   }

   private void openWordViewer(IStructuredSelection selection) {
      List<Artifact> artifacts = new ArrayList<Artifact>();

      for (Object object : selection.toArray()) {
         if (object instanceof WrapperForRelationLink) {
            WrapperForRelationLink link = (WrapperForRelationLink) object;
            artifacts.add(link.getOther());
         }
      }
      RendererManager.openInJob(artifacts, PresentationType.PREVIEW);
   }

   private void performMassEdit(IStructuredSelection selection) {
      Set<Artifact> selectedArtifacts = getSelectedArtifacts(selection);
      MassArtifactEditor.editArtifacts("Mass Edit", selectedArtifacts);
   }

   private Set<Artifact> getSelectedArtifacts(IStructuredSelection selection) {
      Set<Artifact> selectedArtifacts = new HashSet<Artifact>();
      Iterator<?> iter = selection.iterator();
      while (iter.hasNext()) {
         Object object = iter.next();
         if (object instanceof WrapperForRelationLink) {
            WrapperForRelationLink wrapped = (WrapperForRelationLink) object;
            selectedArtifacts.add(wrapped.getOther());
         }
      }
      return selectedArtifacts;
   }

   private void createEditMenuItem(Menu parentMenu) {
      editMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      editMenuItem.setText("&Edit");

      needSelectedArtifactListener.add(editMenuItem);
      editMenuItem.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent event) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            Object object = selection.getFirstElement();

            if (object instanceof WrapperForRelationLink) {
               RendererManager.openInJob(((WrapperForRelationLink) object).getOther(),
                  PresentationType.SPECIALIZED_EDIT);
            }
         }
      });
   }

   private void createExpandAllMenuItem(Menu parentMenu) {
      MenuItem menuItem = new MenuItem(parentMenu, SWT.PUSH);
      menuItem.setText("Expand All\tCtrl+X");
      menuItem.addSelectionListener(new ExpandListener());
   }

   public class ExpandListener extends SelectionAdapter {
      @Override
      public void widgetSelected(SelectionEvent event) {
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
         Iterator<?> iter = selection.iterator();
         while (iter.hasNext()) {
            treeViewer.expandToLevel(iter.next(), AbstractTreeViewer.ALL_LEVELS);
         }
         packColumnData();
      }
   }

   public class NeedSelectedArtifactListener implements MenuListener {
      Collection<MenuItem> accessControlitems;
      Collection<MenuItem> artEnabledOnlyitems;

      public NeedSelectedArtifactListener() {
         this.accessControlitems = new LinkedList<MenuItem>();
         this.artEnabledOnlyitems = new LinkedList<MenuItem>();
      }

      public void addArtifactEnabled(MenuItem item) {
         artEnabledOnlyitems.add(item);

      }

      public void add(MenuItem item) {
         accessControlitems.add(item);
      }

      @Override
      public void menuHidden(MenuEvent e) {
         // do nothing
      }

      @Override
      public void menuShown(MenuEvent e) {
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
         boolean valid = selection.getFirstElement() instanceof WrapperForRelationLink;

         for (MenuItem item : accessControlitems) {
            item.setEnabled(valid && !artifact.isReadOnly());
         }

         for (MenuItem item : artEnabledOnlyitems) {
            item.setEnabled(valid);
         }
      }
   }

   private void createSelectAllMenuItem(Menu parentMenu) {
      MenuItem menuItem = new MenuItem(parentMenu, SWT.PUSH);
      menuItem.setText("&Select All\tCtrl+A");
      menuItem.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(org.eclipse.swt.widgets.Event event) {
            treeViewer.getTree().selectAll();
         }
      });
   }

   @Override
   public void dispose() {
      OseeEventManager.removeListener(this);
      super.dispose();
   }

   private void expandAll(IStructuredSelection selection) {
      Iterator<?> iter = selection.iterator();
      while (iter.hasNext()) {
         treeViewer.expandToLevel(iter.next(), AbstractTreeViewer.ALL_LEVELS);
      }
      this.packColumnData();
   }

   /**
    * Performs the deletion functionality
    * 
    */
   private void performDeleteArtifact(IStructuredSelection selection) {
      try {
         Set<Artifact> artifactsToBeDeleted = getSelectedArtifacts(selection);

         //Ask if they are sure they want all artifacts to be deleted
         if (!artifactsToBeDeleted.isEmpty()) {
            if (MessageDialog.openConfirm(
               Displays.getActiveShell(),
               "Delete Artifact (s)",
               "Delete Artifact (s)?\n\n\"" + Collections.toString(",", artifactsToBeDeleted) + "\"\n\nNOTE: This will delete the artifact from the system.  Use \"Delete Relation\" to remove this artifact from the relation.")) {

               for (Artifact artifact : artifactsToBeDeleted) {
                  artifact.deleteAndPersist();
               }
            }
         }

      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
      refresh();
   }

   /**
    * Performs the deletion functionality
    * 
    * @throws ArtifactDoesNotExist
    */
   private void performDeleteRelation(IStructuredSelection selection) {
      if (artifact.isReadOnly()) {
         MessageDialog.openError(
            Displays.getActiveShell(),
            "Delete Relation Error",
            "Access control has restricted this action. The current user does not have sufficient permission to delete objects on this artifact.");
         return;
      }

      Object[] objects = selection.toArray();
      for (Object object : objects) {
         if (object instanceof WrapperForRelationLink) {
            WrapperForRelationLink wrapper = (WrapperForRelationLink) object;
            try {
               wrapper.getArtifactA().deleteRelation(
                  new RelationTypeSide(wrapper.getRelationType(), RelationSide.SIDE_B), wrapper.getArtifactB());
               Object parent = ((ITreeContentProvider) treeViewer.getContentProvider()).getParent(object);
               if (parent != null) {
                  treeViewer.refresh(parent);
               } else {
                  treeViewer.refresh();
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
            }
         } else if (object instanceof RelationTypeSideSorter) {
            RelationTypeSideSorter group = (RelationTypeSideSorter) object;
            try {
               RelationManager.deleteRelations(artifact, group.getRelationType(), group.getSide());
               treeViewer.refresh(group);
            } catch (OseeCoreException ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
            }
         }
      }
      editor.onDirtied();
   }

   public void refresh() {
      if (!treeViewer.getTree().isDisposed()) {
         treeViewer.refresh();
         packColumnData();
      }
   }

   private final class KeySelectedListener implements KeyListener {
      @Override
      public void keyPressed(KeyEvent e) {
         // do nothing
      }

      @Override
      public void keyReleased(KeyEvent e) {
         if (e.keyCode == SWT.DEL) {
            performDeleteRelation((IStructuredSelection) treeViewer.getSelection());
         }
         if (e.keyCode == 'a' && e.stateMask == SWT.CONTROL) {
            treeViewer.getTree().selectAll();
         }
         if (e.keyCode == 'x' && e.stateMask == SWT.CONTROL) {
            expandAll((IStructuredSelection) treeViewer.getSelection());
         }
      }
   }

   public Artifact getArtifact() {
      return artifact;
   }

   public void refreshArtifact(Artifact newArtifact) {
      relationLabelProvider.setArtifact(newArtifact);
      treeViewer.setInput(newArtifact);
      expandItemsThatHaveChildren();
      refresh();
   }

   private class RelationSkynetDragAndDrop extends SkynetDragAndDrop {
      boolean isFeedbackAfter = false;

      public RelationSkynetDragAndDrop(Tree tree, String viewId) {
         super(tree, viewId);
      }

      @Override
      public Artifact[] getArtifacts() {
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
         Object[] objects = selection.toArray();
         Artifact[] artifacts = null;

         if (objects.length > 0 && objects[0] instanceof WrapperForRelationLink) {
            artifacts = new Artifact[objects.length];

            for (int index = 0; index < objects.length; index++) {
               WrapperForRelationLink link = (WrapperForRelationLink) objects[index];
               artifacts[index] = link.getOther();
            }
         }
         return artifacts;
      }

      @Override
      public void performDragOver(DropTargetEvent event) {
         Tree tree = treeViewer.getTree();
         TreeItem selected = tree.getItem(treeViewer.getTree().toControl(event.x, event.y));

         event.feedback = DND.FEEDBACK_EXPAND;
         event.detail = DND.DROP_NONE;

         if (selected != null && selected.getData() instanceof RelationTypeSideSorter) {
            if (artifact.isReadOnly()) {
               event.detail = DND.DROP_NONE;

               MessageDialog.openError(
                  Displays.getActiveShell(),
                  "Create Relation Error",
                  "Access control has restricted this action. The current user does not have sufficient permission to create relations on this artifact.");
               return;
            } else {
               event.detail = DND.DROP_COPY;
               tree.setInsertMark(null, false);
            }
         } else if (selected != null && selected.getData() instanceof WrapperForRelationLink) {
            WrapperForRelationLink targetLink = (WrapperForRelationLink) selected.getData();
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            Object obj = selection.getFirstElement();
            if (obj instanceof WrapperForRelationLink) {
               WrapperForRelationLink dropTarget = (WrapperForRelationLink) obj;

               if (artifact.isReadOnly()) {
                  event.detail = DND.DROP_NONE;
                  MessageDialog.openError(
                     Displays.getActiveShell(),
                     "Create Relation Error",
                     "Access control has restricted this action. The current user does not have sufficient permission to create relations on this artifact.");
                  return;
               }
               // the links must be in the same group
               if (relationLinkIsInSameGroup(targetLink, dropTarget)) {
                  if (isFeedbackAfter) {
                     event.feedback = DND.FEEDBACK_INSERT_AFTER;
                  } else {
                     event.feedback = DND.FEEDBACK_INSERT_BEFORE;
                  }
                  event.detail = DND.DROP_MOVE;
               }
            }
         } else {
            tree.setInsertMark(null, false);
         }
      }

      private boolean relationLinkIsInSameGroup(WrapperForRelationLink targetLink, WrapperForRelationLink dropTarget) {
         return targetLink.getRelationType().equals(dropTarget.getRelationType()) && //same type
         (targetLink.getArtifactA().equals(dropTarget.getArtifactA()) || //either the A or B side is equal, meaning they are on the same side
         targetLink.getArtifactB().equals(dropTarget.getArtifactB()));
      }

      @Override
      public void operationChanged(DropTargetEvent event) {
         if (!isCtrlPressed(event)) {
            isFeedbackAfter = false;
         }
      }

      private boolean isCtrlPressed(DropTargetEvent event) {
         boolean ctrPressed = event.detail == 1;

         if (ctrPressed) {
            isFeedbackAfter = true;
         }
         return ctrPressed;
      }

      @Override
      public void performDrop(DropTargetEvent event) {
         TreeItem selected = treeViewer.getTree().getItem(treeViewer.getTree().toControl(event.x, event.y));
         Object object = selected.getData();
         try {
            if (object instanceof WrapperForRelationLink) {//used for ordering
               WrapperForRelationLink targetLink = (WrapperForRelationLink) object;
               Artifact[] artifactsToMove = ((ArtifactData) event.data).getArtifacts();
               for (Artifact artifactToMove : artifactsToMove) {
                  IRelationEnumeration typeSide =
                     new RelationTypeSide(targetLink.getRelationType(), targetLink.getRelationSide());
                  artifact.setRelationOrder(typeSide, targetLink.getOther(), isFeedbackAfter, artifactToMove);
               }
               treeViewer.refresh();
               editor.onDirtied();
            } else if (object instanceof RelationTypeSideSorter) {
               RelationTypeSideSorter group = (RelationTypeSideSorter) object;

               RelationExplorerWindow window = new RelationExplorerWindow(treeViewer, group);

               ArtifactDragDropSupport.performDragDrop(event, window,
                  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
               window.createArtifactInformationBox();
               treeViewer.refresh();
               editor.onDirtied();
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
         }

         isFeedbackAfter = false;
      }
   }

   private void setHelpContexts() {
      SkynetGuiPlugin.getInstance().setHelp(treeViewer.getControl(), "relation_page_tree_viewer",
         "org.eclipse.osee.framework.help.ui");
   }

   public ToolBar getToolBar() {
      return toolBar;
   }

   @Override
   public void handleFrameworkTransactionEvent(Sender sender, FrameworkTransactionData transData) {
      if (transData.isRelAddedChangedDeleted(this.artifact)) {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               if (!treeViewer.getControl().isDisposed()) {
                  treeViewer.refresh();
               }
            }
         });
      }
   }
}