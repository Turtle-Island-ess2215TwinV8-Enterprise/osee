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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.eclipse.osee.framework.jdk.core.util.StringFormat;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.eclipse.osee.framework.skynet.core.ArtifactVersionIncrementedEvent;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.SkynetAuthentication;
import org.eclipse.osee.framework.skynet.core.access.AccessControlManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactData;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactModifiedEvent;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTransfer;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.CacheArtifactModifiedEvent;
import org.eclipse.osee.framework.skynet.core.artifact.DefaultBranchChangedEvent;
import org.eclipse.osee.framework.skynet.core.artifact.TransactionArtifactModifiedEvent;
import org.eclipse.osee.framework.skynet.core.attribute.ArtifactSubtypeDescriptor;
import org.eclipse.osee.framework.skynet.core.attribute.ConfigurationPersistenceManager;
import org.eclipse.osee.framework.skynet.core.event.ArtifactLockStatusChanged;
import org.eclipse.osee.framework.skynet.core.event.LocalCommitBranchEvent;
import org.eclipse.osee.framework.skynet.core.event.RemoteCommitBranchEvent;
import org.eclipse.osee.framework.skynet.core.event.RemoteTransactionEvent;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;
import org.eclipse.osee.framework.skynet.core.event.TransactionEvent;
import org.eclipse.osee.framework.skynet.core.relation.CacheRelationModifiedEvent;
import org.eclipse.osee.framework.skynet.core.relation.RelationModifiedEvent;
import org.eclipse.osee.framework.skynet.core.relation.RelationSide;
import org.eclipse.osee.framework.skynet.core.relation.TransactionRelationModifiedEvent;
import org.eclipse.osee.framework.skynet.core.transaction.AbstractSkynetTxTemplate;
import org.eclipse.osee.framework.ui.plugin.event.AuthenticationEvent;
import org.eclipse.osee.framework.ui.plugin.event.IEventReceiver;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.Jobs;
import org.eclipse.osee.framework.ui.plugin.util.SelectionCountChangeListener;
import org.eclipse.osee.framework.ui.plugin.util.Wizards;
import org.eclipse.osee.framework.ui.skynet.Import.ArtifactImportWizard;
import org.eclipse.osee.framework.ui.skynet.access.PolicyDialog;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.ArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.artifact.massEditor.MassArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.ats.IActionable;
import org.eclipse.osee.framework.ui.skynet.ats.OseeAts;
import org.eclipse.osee.framework.ui.skynet.autoRun.KickoffOseeAutoRunTaskAction;
import org.eclipse.osee.framework.ui.skynet.history.RevisionHistoryView;
import org.eclipse.osee.framework.ui.skynet.menu.ArtifactPreviewMenu;
import org.eclipse.osee.framework.ui.skynet.menu.ArtifactTreeViewerGlobalMenuHelper;
import org.eclipse.osee.framework.ui.skynet.menu.GlobalMenu;
import org.eclipse.osee.framework.ui.skynet.menu.GlobalMenuPermissions;
import org.eclipse.osee.framework.ui.skynet.menu.IGlobalMenuHelper;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.skywalker.SkyWalkerView;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactClipboard;
import org.eclipse.osee.framework.ui.skynet.util.BranchSelectionDialog;
import org.eclipse.osee.framework.ui.skynet.util.DbConnectionExceptionComposite;
import org.eclipse.osee.framework.ui.skynet.util.HierarchicalReportDialog;
import org.eclipse.osee.framework.ui.skynet.util.HtmlReportJob;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.util.ShowAttributeAction;
import org.eclipse.osee.framework.ui.skynet.util.SkynetDragAndDrop;
import org.eclipse.osee.framework.ui.skynet.util.SkynetViews;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;
import org.eclipse.osee.framework.ui.swt.MenuItems;
import org.eclipse.osee.framework.ui.swt.TreeViewerUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Ryan D. Brooks
 */
public class ArtifactExplorer extends ViewPart implements IEventReceiver, IActionable, ISelectionProvider {
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(ArtifactExplorer.class);
   private static final Image ACCESS_DENIED_IMAGE = SkynetGuiPlugin.getInstance().getImage("lockkey.gif");
   public static final String VIEW_ID = "org.eclipse.osee.framework.ui.skynet.ArtifactExplorer";
   private static final String ROOT_GUID = "artifact.explorer.last.root_guid";
   private static final ArtifactClipboard artifactClipboard = new ArtifactClipboard(VIEW_ID);
   private static final LinkedList<Tree> trees = new LinkedList<Tree>();

   private TreeViewer treeViewer;
   private Action upAction;
   private Artifact root;
   private MenuItem editMenuItem;
   private MenuItem massEditMenuItem;
   private MenuItem skywalkerMenuItem;
   private MenuItem createMenuItem;
   private MenuItem reportMenuItem;
   private MenuItem openMenuItem;
   private MenuItem accessControlMenuItem;
   private MenuItem lockMenuItem;
   private MenuItem goIntoMenuItem;
   private MenuItem copyMenuItem;
   private MenuItem pasteMenuItem;
   private NeedArtifactMenuListener needArtifactListener;
   private NeedProjectMenuListener needProjectListener;
   private Action showArtIds;
   private Action showArtType;
   private Action newArtifactExplorer;
   private Action collapseAllAction;
   private ShowAttributeAction attributesAction;
   IGlobalMenuHelper globalMenuHelper;

   private Composite stackComposite;
   private Control branchUnreadableWarning;
   private StackLayout stackLayout;

   public ArtifactExplorer() {
   }

   public static void explore(Collection<Artifact> artifacts) {
      IWorkbenchPage page = AWorkbench.getActivePage();
      ArtifactExplorer artifactExplorer;
      try {
         artifactExplorer =
               (ArtifactExplorer) page.showView(ArtifactExplorer.VIEW_ID, new GUID().toString(),
                     IWorkbenchPage.VIEW_ACTIVATE);
         artifactExplorer.setPartName("Artifacts");
         artifactExplorer.setContentDescription("These artifact must be edited singly");
         artifactExplorer.treeViewer.setInput(artifacts);
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

   private Control createDefaultWarning(Composite parent) {
      Composite composite = new Composite(parent, SWT.BORDER);
      composite.setLayout(new GridLayout(2, false));
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      composite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

      Label image = new Label(composite, SWT.NONE);
      image.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
      image.setImage(ACCESS_DENIED_IMAGE);
      image.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

      Label label = new Label(composite, SWT.NONE);
      Font font = new Font(PlatformUI.getWorkbench().getDisplay(), "Courier New", 10, SWT.BOLD);
      label.setFont(font);
      label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
      label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
      label.setText("Branch Read Access Denied.\nContact your administrator.");
      label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

      return composite;
   }

   private void checkBranchReadable() {
      Control control = branchUnreadableWarning;
      if (false != (new GlobalMenuPermissions(globalMenuHelper)).isDefaultBranchReadable()) {
         control = treeViewer.getTree();
      }
      stackLayout.topControl = control;
      stackComposite.layout();
      stackComposite.getParent().layout();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
    */

   @Override
   public void createPartControl(Composite parent) {
      if (!DbConnectionExceptionComposite.dbConnectionIsOk(parent)) return;

      GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
      gridData.heightHint = 1000;
      gridData.widthHint = 1000;

      parent.setLayout(new GridLayout(1, false));
      parent.setLayoutData(gridData);

      stackComposite = new Composite(parent, SWT.NONE);
      stackLayout = new StackLayout();
      stackComposite.setLayout(stackLayout);
      stackComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      branchUnreadableWarning = createDefaultWarning(stackComposite);

      treeViewer = new TreeViewer(stackComposite);
      Tree tree = treeViewer.getTree();
      treeViewer.setContentProvider(new ArtifactContentProvider(this));
      treeViewer.setLabelProvider(new ArtifactLabelProvider(this));
      treeViewer.addDoubleClickListener(new ArtifactDoubleClick());
      treeViewer.getControl().setLayoutData(gridData);
      tree.addKeyListener(new keySelectedListener());

      // We can not use the hash lookup because an artifact may not have a
      // good equals.
      // This can be added back once the content provider is converted over to
      // use job node.
      treeViewer.setUseHashlookup(false);

      treeViewer.addSelectionChangedListener(new SelectionCountChangeListener(getViewSite()));
      globalMenuHelper = new ArtifactTreeViewerGlobalMenuHelper(treeViewer);

      createCollapseAllAction();
      createUpAction();
      createShowArtTypeAction();
      createAttributesAction();
      createNewArtifactExplorerAction();

      if (OseeProperties.getInstance().isDeveloper()) createDebugStubActions();

      getSite().setSelectionProvider(treeViewer);
      addExploreSelection();

      setupPopupMenu();

      new ArtifactExplorerDragAndDrop(tree, VIEW_ID);
      parent.layout();

      if (OseeProperties.getInstance().isDeveloper()) {
         createShowArtIdsAction();
      }
      createSetDefaultBranchAction();
      addAutoRunAction();
      OseeAts.addBugToViewToolbar(this, this, SkynetActivator.getInstance(), VIEW_ID, "Artifact Explorer");

      SkynetDefaultBranchContributionItem.addTo(this, false);
      SkynetContributionItem.addTo(this, false);
      getViewSite().getActionBars().updateActionBars();

      updateEnablementsEtAl();
      trees.add(tree);
      setHelpContexts();
      checkBranchReadable();
   }

   /**
    * Reveal an artifact in the viewer and select it.
    * 
    * @throws SQLException
    * @throws PartInitException
    */
   public static void revealArtifact(String guid, Branch branch) throws SQLException, PartInitException {
      Artifact artifact = ArtifactPersistenceManager.getInstance().getArtifact(guid, branch);
      IWorkbenchPage page = AWorkbench.getActivePage();
      ArtifactExplorer artifactExplorer;
      artifactExplorer = (ArtifactExplorer) page.showView(ArtifactExplorer.VIEW_ID);
      artifactExplorer.treeViewer.setSelection(new StructuredSelection(artifact), true);
   }

   private void setupPopupMenu() {

      Menu popupMenu = new Menu(treeViewer.getTree().getParent());
      needArtifactListener = new NeedArtifactMenuListener();
      needProjectListener = new NeedProjectMenuListener();
      popupMenu.addMenuListener(needArtifactListener);
      popupMenu.addMenuListener(needProjectListener);

      createNewItemMenuItem(popupMenu);
      createGoIntoMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createOpenMenuItem(popupMenu);
      createEditMenuItem(popupMenu);

      ArtifactPreviewMenu.createPreviewMenuItem(popupMenu, treeViewer);

      createMassEditMenuItem(popupMenu);
      createSkywalkerMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      new GlobalMenu(popupMenu, globalMenuHelper);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createReportMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createHistoryMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createImportExportMenuItems(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createLockMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createCopyMenuItem(popupMenu);
      createPasteMenuItem(popupMenu);
      createExpandAllMenuItem(popupMenu);
      createSelectAllMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createAccessControlMenuItem(popupMenu);
      treeViewer.getTree().setMenu(popupMenu);
   }

   protected void createUpAction() {
      upAction = new Action("View Parent") {
         @Override
         public void run() {
            try {
               Artifact parent = root.getParent();

               if (parent == null) return;

               Object[] expanded = treeViewer.getExpandedElements();
               Object[] expandedPlus = new Object[expanded.length + 1];
               for (int i = 0; i < expanded.length; i++)
                  expandedPlus[i] = expanded[i];
               expandedPlus[expandedPlus.length - 1] = root;

               explore(parent);

               treeViewer.setExpandedElements(expandedPlus);
            } catch (Exception ex) {
               logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
         }
      };

      upAction.setImageDescriptor(SkynetGuiPlugin.getInstance().getImageDescriptor("up.gif"));
      upAction.setToolTipText("View Parent");
      updateEnablementsEtAl();

      IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
      toolbarManager.add(upAction);
   }

   protected void createDebugStubActions() {
      try {
         addTemplates();
      } catch (SQLException ex) {
         logger.log(Level.SEVERE, ex.toString(), ex);
      } catch (IOException ex) {
         logger.log(Level.SEVERE, ex.toString(), ex);
      } catch (ClassNotFoundException ex) {
         logger.log(Level.SEVERE, ex.toString(), ex);
      }
   }

   private void addTemplates() throws SQLException, IOException, ClassNotFoundException {
   }

   protected void createShowArtIdsAction() {

      showArtIds = new Action("Show Artifact Ids") {
         @Override
         public void run() {
            setChecked(!isChecked());
            updateShowArtIdText();
            treeViewer.refresh();
         }
      };

      showArtIds.setImageDescriptor(SkynetGuiPlugin.getInstance().getImageDescriptor("filter.gif"));
      updateShowArtIdText();

      IMenuManager toolbarManager = getViewSite().getActionBars().getMenuManager();
      toolbarManager.add(showArtIds);
   }

   private void createSetDefaultBranchAction() {
      Action setDefaultBranch = new Action("Set Default Branch", Action.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            BranchSelectionDialog branchSelection = new BranchSelectionDialog("Set Default Branch");
            int result = branchSelection.open();
            if (result == Window.OK) {
               BranchPersistenceManager.getInstance().setDefaultBranch(branchSelection.getSelection());
            }
         }
      };
      setDefaultBranch.setImageDescriptor(SkynetGuiPlugin.getInstance().getImageDescriptor("branch_change.gif"));
      IMenuManager toolbarManager = getViewSite().getActionBars().getMenuManager();
      toolbarManager.add(setDefaultBranch);
   }

   protected void createShowArtTypeAction() {

      showArtType = new Action("Show Artifact Type") {
         @Override
         public void run() {
            setChecked(!isChecked());
            updateShowArtTypeText();
            treeViewer.refresh();
         }
      };

      showArtType.setImageDescriptor(SkynetGuiPlugin.getInstance().getImageDescriptor("filter.gif"));
      updateShowArtTypeText();

      IMenuManager toolbarManager = getViewSite().getActionBars().getMenuManager();
      toolbarManager.add(showArtType);
   }

   private void addAutoRunAction() {
      IMenuManager toolbarManager = getViewSite().getActionBars().getMenuManager();
      toolbarManager.add(new KickoffOseeAutoRunTaskAction());
   }

   private void createNewArtifactExplorerAction() {

      newArtifactExplorer = new Action("New Artifact Explorer") {
         @Override
         public void run() {
            IWorkbenchPage page = AWorkbench.getActivePage();
            ArtifactExplorer artifactExplorer;
            try {
               artifactExplorer =
                     (ArtifactExplorer) page.showView(ArtifactExplorer.VIEW_ID, GUID.generateGuidStr(),
                           IWorkbenchPage.VIEW_ACTIVATE);
               artifactExplorer.explore(ArtifactPersistenceManager.getInstance().getDefaultHierarchyRootArtifact(
                     BranchPersistenceManager.getInstance().getDefaultBranch()));
               artifactExplorer.setExpandedArtifacts(treeViewer.getExpandedElements());
            } catch (Exception ex) {
               throw new RuntimeException(ex);
            }
         }
      };

      newArtifactExplorer.setImageDescriptor(SkynetGuiPlugin.getInstance().getImageDescriptor("artifact_explorer.gif"));

      IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
      toolbarManager.add(newArtifactExplorer);
   }

   private void createCollapseAllAction() {

      collapseAllAction = new Action("Collapse All") {
         @Override
         public void run() {
            if (treeViewer != null) {
               treeViewer.collapseAll();
            }
         }
      };

      collapseAllAction.setImageDescriptor(SkynetGuiPlugin.getInstance().getImageDescriptor("collapseAll.gif"));

      IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
      toolbarManager.add(collapseAllAction);
   }

   private void updateShowArtIdText() {
      showArtIds.setText((showArtIds.isChecked() ? "Hide" : "Show") + " Artifact Ids");
   }

   private void updateShowArtTypeText() {
      showArtType.setText((showArtType.isChecked() ? "Hide" : "Show") + " Artifact Type");
   }

   protected void createAttributesAction() {
      try {
         attributesAction = new ShowAttributeAction(treeViewer, SkynetGuiPlugin.ARTIFACT_EXPLORER_ATTRIBUTES_PREF);
         attributesAction.addToView(this,
               SkynetViews.loadAttrTypesFromPreferenceStore(SkynetGuiPlugin.ARTIFACT_EXPLORER_ATTRIBUTES_PREF));
      } catch (SQLException ex) {
         logger.log(Level.SEVERE, ex.toString(), ex);
      }
   }

   public String getSelectedAttributeData(Artifact artifact) {
      return attributesAction.getSelectedAttributeData(artifact);
   }

   private void createOpenMenuItem(Menu parentMenu) {
      openMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      openMenuItem.setText("&Open");

      needArtifactListener.add(openMenuItem);
      openMenuItem.addSelectionListener(new OpenListener());
   }

   public class OpenListener extends SelectionAdapter {
      @Override
      public void widgetSelected(SelectionEvent event) {
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
         Iterator<?> itemsIter = selection.iterator();

         while (itemsIter.hasNext()) {
            ArtifactEditor.editArtifact((Artifact) itemsIter.next());
         }
      }
   }

   private void createNewItemMenuItem(Menu parentMenu) {
      SelectionAdapter listener = new NewArtifactMenuListener();
      createMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
      Menu subMenu = new Menu(parentMenu.getShell(), SWT.DROP_DOWN);
      createMenuItem.setMenu(subMenu);
      needProjectListener.add(createMenuItem);
      createMenuItem.setText("&New Child");
      createMenuItem.setEnabled(true);

      try {
         Collection<ArtifactSubtypeDescriptor> descriptors =
               ConfigurationPersistenceManager.getInstance().getArtifactSubtypeDescriptors(
                     BranchPersistenceManager.getInstance().getDefaultBranch());
         for (ArtifactSubtypeDescriptor descriptor : descriptors) {
            if (!descriptor.getName().equals("Root Artifact")) {
               MenuItem item = new MenuItem(subMenu, SWT.PUSH);
               item.setText(descriptor.getName());
               item.setImage(descriptor.getImage());
               item.setData(descriptor);
               item.addSelectionListener(listener);
            }
         }
      } catch (SQLException ex) {
         logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
   }

   private class NewArtifactMenuListener extends SelectionAdapter {
      @Override
      public void widgetSelected(SelectionEvent ev) {
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
         Iterator<?> itemsIter = selection.iterator();
         ArtifactSubtypeDescriptor descriptor = (ArtifactSubtypeDescriptor) ((MenuItem) ev.getSource()).getData();

         EntryDialog ed =
               new EntryDialog("New \"" + descriptor.getName() + "\" Artifact",
                     "Enter name for \"" + descriptor.getName() + "\" Artifact");
         if (ed.open() != 0) return;
         try {
            // If nothing was selected, then the child belongs at the root
            if (!itemsIter.hasNext()) {
               root.addNewChild(descriptor, ed.getEntry());
            } else {
               while (itemsIter.hasNext()) {
                  ((Artifact) itemsIter.next()).addNewChild(descriptor, ed.getEntry());
               }
            }
            treeViewer.refresh();
         } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
         }
         treeViewer.refresh(false);
      }
   }

   private void createGoIntoMenuItem(Menu parentMenu) {
      goIntoMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      goIntoMenuItem.setText("&Go Into");
      needArtifactListener.add(goIntoMenuItem);

      ArtifactMenuListener listener = new ArtifactMenuListener();
      parentMenu.addMenuListener(listener);
      goIntoMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent ev) {

            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            Iterator<?> itemsIter = selection.iterator();
            if (itemsIter.hasNext()) {
               try {
                  Object[] expanded = treeViewer.getExpandedElements();
                  explore((Artifact) itemsIter.next());
                  treeViewer.setExpandedElements(expanded);
               } catch (Exception e) {
                  logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
               }
            }
         }
      });
   }

   private void createEditMenuItem(Menu parentMenu) {
      editMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      editMenuItem.setText("&Edit");
      needArtifactListener.add(editMenuItem);

      ArtifactMenuListener listener = new ArtifactMenuListener();
      parentMenu.addMenuListener(listener);
      editMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent ev) {
            LinkedList<Artifact> selectedItems = new LinkedList<Artifact>();
            TreeViewerUtility.getPreorderSelection(treeViewer, selectedItems);
            RendererManager.getInstance().editInJob(selectedItems);
         }
      });
   }

   private void createMassEditMenuItem(Menu parentMenu) {
      massEditMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      massEditMenuItem.setText("Mass Edit");
      needArtifactListener.add(massEditMenuItem);

      ArtifactMenuListener listener = new ArtifactMenuListener();
      parentMenu.addMenuListener(listener);
      massEditMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent ev) {
            LinkedList<Artifact> selectedItems = new LinkedList<Artifact>();
            TreeViewerUtility.getPreorderSelection(treeViewer, selectedItems);
            MassArtifactEditor.editArtifacts("", selectedItems);
         }
      });
   }

   private void createSkywalkerMenuItem(Menu parentMenu) {
      skywalkerMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      skywalkerMenuItem.setText("Sky Walker");
      needArtifactListener.add(skywalkerMenuItem);

      ArtifactMenuListener listener = new ArtifactMenuListener();
      parentMenu.addMenuListener(listener);
      skywalkerMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent ev) {
            LinkedList<Artifact> selectedItems = new LinkedList<Artifact>();
            TreeViewerUtility.getPreorderSelection(treeViewer, selectedItems);
            SkyWalkerView.exploreArtifact(selectedItems.getFirst());
         }
      });
   }

   private void createSelectAllMenuItem(Menu parentMenu) {
      MenuItem menuItem = new MenuItem(parentMenu, SWT.PUSH);
      menuItem.setText("&Select All\tCtrl+A");
      menuItem.addListener(SWT.Selection, new Listener() {
         public void handleEvent(org.eclipse.swt.widgets.Event event) {
            treeViewer.getTree().selectAll();
         }
      });
   }

   private void createHistoryMenuItem(Menu parentMenu) {
      MenuItem revisionMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      revisionMenuItem.setText("&Show Resource History ");
      revisionMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            Artifact selectedArtifact = (Artifact) selection.getFirstElement();

            IWorkbenchPage page = AWorkbench.getActivePage();
            try {
               RevisionHistoryView revisionHistoryView =
                     (RevisionHistoryView) page.showView(RevisionHistoryView.VIEW_ID, selectedArtifact.getGuid(),
                           IWorkbenchPage.VIEW_ACTIVATE);
               revisionHistoryView.explore(selectedArtifact);
            } catch (Exception ex) {
               logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
         }
      });
   }

   private void createImportExportMenuItems(Menu parentMenu) {
      MenuItems.createMenuItem(parentMenu, SWT.PUSH, new ImportResourcesAction(getViewSite().getWorkbenchWindow()));
      MenuItems.createMenuItem(parentMenu, SWT.PUSH, new ExportResourcesAction(getViewSite().getWorkbenchWindow()));
   }

   private void createAccessControlMenuItem(Menu parentMenu) {
      accessControlMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      accessControlMenuItem.setText("&Access Control ");
      // accessControlMenuItem.setEnabled(false);
      accessControlMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            Artifact selectedArtifact = (Artifact) selection.getFirstElement();
            try {
               if (selectedArtifact != null) {
                  PolicyDialog pd = new PolicyDialog(Display.getCurrent().getActiveShell(), selectedArtifact);
                  pd.open();
                  checkBranchReadable();
               }
            } catch (Exception ex) {
               logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
         }
      });
   }

   private void createReportMenuItem(Menu parentMenu) {
      reportMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      reportMenuItem.setText("&Hierarchical Report");
      reportMenuItem.addSelectionListener(new ReportListener());
   }

   private void createLockMenuItem(Menu parentMenu) {
      lockMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      lockMenuItem.addSelectionListener(new SelectionListener() {

         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            Iterator<?> iterator = selection.iterator();

            while (iterator.hasNext()) {
               Artifact object = (Artifact) iterator.next();
               if ((new GlobalMenuPermissions(object)).isLocked()) {
                  AccessControlManager.getInstance().unLockObject(object,
                        SkynetAuthentication.getInstance().getAuthenticatedUser());
               } else {
                  AccessControlManager.getInstance().lockObject(object,
                        SkynetAuthentication.getInstance().getAuthenticatedUser());
               }
            }
         }

         public void widgetDefaultSelected(SelectionEvent e) {
         }

      });
   }

   private void createCopyMenuItem(Menu parentMenu) {
      copyMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      copyMenuItem.setText("Copy \tCtrl+C");
      copyMenuItem.addSelectionListener(new SelectionListener() {

         public void widgetSelected(SelectionEvent e) {
            performCopy();
         }

         public void widgetDefaultSelected(SelectionEvent e) {
         }

      });
   }

   private void performCopy() {
      IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
      ArrayList<Artifact> artifactTransferData = new ArrayList<Artifact>();
      ArrayList<String> textTransferData = new ArrayList<String>();
      Artifact artifact;

      if (selection != null && !selection.isEmpty()) {
         for (Object object : selection.toArray()) {
            if (object instanceof Artifact) {
               artifact = (Artifact) object;

               artifactTransferData.add(artifact);
               textTransferData.add(artifact.getDescriptiveName());
            }
         }
         artifactClipboard.setArtifactsToClipboard(artifactTransferData, textTransferData);
      }
   }

   private void createPasteMenuItem(Menu parentMenu) {
      pasteMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      pasteMenuItem.setText("Paste \tCtrl+V");
      pasteMenuItem.addSelectionListener(new SelectionListener() {

         public void widgetSelected(SelectionEvent e) {
            performPaste();
         }

         public void widgetDefaultSelected(SelectionEvent e) {
         }

      });
   }

   /**
    * This method must be called from the display thread
    */
   private void performPaste() {
      IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();

      if (selection != null && !selection.isEmpty()) {
         Object object = selection.getFirstElement();

         if (object instanceof Artifact) {
            try {
               artifactClipboard.pasteArtifactsFromClipboard((Artifact) object);
            } catch (SQLException ex) {
               OSEELog.logException(getClass(), ex, true);
            }
         }
      }
   }

   private void createExpandAllMenuItem(Menu parentMenu) {
      MenuItem menuItem = new MenuItem(parentMenu, SWT.PUSH);
      menuItem.setText("Expand All\tCtrl+X");
      menuItem.addSelectionListener(new ExpandListener());
   }

   public class ExpandListener extends SelectionAdapter {
      @Override
      public void widgetSelected(SelectionEvent event) {
         expandAll((IStructuredSelection) treeViewer.getSelection());
      }
   }

   public class ReportListener extends SelectionAdapter {
      @Override
      public void widgetSelected(SelectionEvent event) {
         Iterator<?> iter = ((IStructuredSelection) treeViewer.getSelection()).iterator();
         ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
         while (iter.hasNext()) {
            artifacts.add((Artifact) iter.next());
         }
         if (artifacts.size() > 0) {
            HierarchicalReportDialog ld = new HierarchicalReportDialog(Display.getCurrent().getActiveShell());
            int result = ld.open();
            if (result == 0) {
               HtmlReportJob job;
               try {
                  job = new HtmlReportJob("Hierarchical Report", artifacts, RelationSide.DEFAULT_HIERARCHICAL__CHILD);
                  job.setIncludeAttributes(ld.isShowAttributes());
                  job.setRecurseChildren(ld.isRecurseChildren());
                  Jobs.startJob(job);
               } catch (Exception ex) {
                  OSEELog.logException(getClass(), ex, true);
               }
            }
         }
      }
   }

   private void expandAll(IStructuredSelection selection) {
      Iterator<?> iter = selection.iterator();
      while (iter.hasNext()) {
         treeViewer.expandToLevel(iter.next(), TreeViewer.ALL_LEVELS);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.IWorkbenchPart#setFocus()
    */
   @Override
   public void setFocus() {
      if (treeViewer != null) treeViewer.getControl().setFocus();
   }

   public void explore(Artifact artifact) throws CoreException, IllegalArgumentException {
      if (artifact == null) {
         throw new IllegalArgumentException("Can not explore a null artifact.");
      }

      root = artifact;

      SkynetEventManager.getInstance().unRegisterAll(this);
      SkynetEventManager.getInstance().register(ArtifactVersionIncrementedEvent.class, this);
      SkynetEventManager.getInstance().register(AuthenticationEvent.class, this);
      SkynetEventManager.getInstance().register(CacheArtifactModifiedEvent.class, this);
      SkynetEventManager.getInstance().register(CacheRelationModifiedEvent.class, this);
      SkynetEventManager.getInstance().register(TransactionRelationModifiedEvent.class, this);
      SkynetEventManager.getInstance().register(TransactionArtifactModifiedEvent.class, this);
      SkynetEventManager.getInstance().register(RemoteTransactionEvent.class, this);
      SkynetEventManager.getInstance().register(DefaultBranchChangedEvent.class, this);
      SkynetEventManager.getInstance().register(ArtifactLockStatusChanged.class, this);
      SkynetEventManager.getInstance().register(LocalCommitBranchEvent.class, this);
      SkynetEventManager.getInstance().register(RemoteCommitBranchEvent.class, this);

      if (treeViewer != null) {
         treeViewer.setInput(root);
         setupPopupMenu();
         updateEnablementsEtAl();

      }
   }

   public void setExpandedArtifacts(Object... artifacts) {
      if (treeViewer != null) {
         treeViewer.setExpandedElements(artifacts);
      }
   }

   private void updateEnablementsEtAl() {
      // The upAction may be null if this viewpart has not been layed out yet
      if (upAction != null) upAction.setEnabled(root != null && root.getParent() != null);

      if (root != null)
         setContentDescription(root.getDescriptiveName());
      else
         setContentDescription("");

      if (root != null && root.getPersistenceMemo() != null) {
         Branch branch = root.getPersistenceMemo().getTransactionId().getBranch();
         if (editMenuItem != null) {
            editMenuItem.setText("Edit (" + StringFormat.truncate(branch.getBranchName(), 25) + ")");
         }
      } else {
         if (editMenuItem != null) editMenuItem.setText("Edit");
      }
   }

   private class NeedArtifactMenuListener implements MenuListener {
      private final HashCollection<Class<? extends Artifact>, MenuItem> menuItemMap;

      public NeedArtifactMenuListener() {
         this.menuItemMap = new HashCollection<Class<? extends Artifact>, MenuItem>();
      }

      public void add(MenuItem item) {
         menuItemMap.put(Artifact.class, item);
      }

      public void add(MenuItem item, Class<? extends Artifact> artifactClass) {
         menuItemMap.put(artifactClass, item);
      }

      public void menuHidden(MenuEvent e) {
      }

      public void menuShown(MenuEvent e) {
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();

         Object obj = selection.getFirstElement();
         if (obj != null) {
            Class<? extends Artifact> selectedClass = obj.getClass().asSubclass(Artifact.class);

            for (Class<? extends Artifact> artifactClass : menuItemMap.keySet()) {
               boolean valid = artifactClass.isAssignableFrom(selectedClass);

               for (MenuItem item : menuItemMap.getValues(artifactClass)) {
                  if (!(item.getData() instanceof Exception)) {
                     // Only modify enabling if no error is associated
                     item.setEnabled(valid);
                  }
               }
            }
         }
      }
   }

   private class NeedProjectMenuListener implements MenuListener {
      Collection<MenuItem> items;

      public NeedProjectMenuListener() {
         this.items = new LinkedList<MenuItem>();
      }

      public void add(MenuItem item) {
         items.add(item);
      }

      public void menuHidden(MenuEvent e) {
      }

      public void menuShown(MenuEvent e) {
         boolean valid = treeViewer.getInput() != null;
         for (MenuItem item : items)
            if (!(item.getData() instanceof Exception)) // Only modify
            // enabling if no
            // error is
            // associated
            item.setEnabled(valid);
      }
   }

   /**
    * Add the selection from the define explorer
    */
   private void addExploreSelection() {
      if (root != null) {
         try {
            treeViewer.setInput(root);
         } catch (IllegalArgumentException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
         }
      }
   }

   /**
    * @author Jeff C. Phillips
    */
   public class ArtifactMenuListener implements MenuListener {

      public void menuHidden(MenuEvent e) {
      }

      public void menuShown(MenuEvent e) {
         // Use this menu listener until all menu items can be moved to
         // GlobaMenu
         GlobalMenuPermissions permiss = new GlobalMenuPermissions(globalMenuHelper);

         lockMenuItem.setText((permiss.isLocked() ? "Unlock: (" + permiss.getSubjectFromLockedObjectName() + ")" : "Lock"));

         lockMenuItem.setEnabled(permiss.isWritePermission() && (!permiss.isLocked() || permiss.isAccessToRemoveLock()));
         editMenuItem.setEnabled(permiss.isWritePermission());
         createMenuItem.setEnabled(permiss.isWritePermission());
         // previewMenuItem.setEnabled(readPermission);
         openMenuItem.setEnabled(permiss.isWritePermission());
         goIntoMenuItem.setEnabled(permiss.isReadPermission());
         copyMenuItem.setEnabled(permiss.isReadPermission());
         pasteMenuItem.setEnabled(permiss.isWritePermission());
      }
   }

   public void onEvent(final org.eclipse.osee.framework.ui.plugin.event.Event event) {
      final ArtifactExplorer artifactExplorer = this;

      try {
         if (treeViewer != null && !treeViewer.getTree().isDisposed()) {
            checkBranchReadable();
            if (event instanceof ArtifactModifiedEvent) {
               ArtifactModifiedEvent artifactModifiedEvent = (ArtifactModifiedEvent) event;
               Artifact artifact = artifactModifiedEvent.getArtifact();

               if (artifact != null) {

                  if (artifactModifiedEvent.getType() == ArtifactModifiedEvent.ModType.Purged) {
                     treeViewer.refresh();
                     return;
                  }

                  if (artifact.isDeleted() || artifactModifiedEvent.getType() == ArtifactModifiedEvent.ModType.Reverted) {
                     Artifact parent = artifact.getParent();

                     if (parent != null) {
                        parent.clearLinkManager();
                        parent.getLinkManager();
                     }
                     treeViewer.refresh(parent);
                  } else
                     treeViewer.refresh(artifact);
               }
            } else if (event instanceof RelationModifiedEvent) {
               RelationModifiedEvent relationModifiedEvent = (RelationModifiedEvent) event;

               Artifact aArt = relationModifiedEvent.getLink().getArtifactA();
               Artifact bArt = relationModifiedEvent.getLink().getArtifactB();

               if (aArt != null && !aArt.isDeleted() && !aArt.isReadOnly()) {
                  // make sure his linkmanager is loaded
                  aArt.getLinkManager();
                  treeViewer.refresh(aArt, false);
               }

               if (bArt != null && !bArt.isDeleted() && !bArt.isReadOnly()) {
                  // make sure his linkmanager is loaded
                  bArt.getLinkManager();
                  treeViewer.refresh(bArt, false);
               }
            } else if (event instanceof ArtifactVersionIncrementedEvent) {
               ArtifactVersionIncrementedEvent verEvent = (ArtifactVersionIncrementedEvent) event;
               Artifact parentArtifact = verEvent.getNewVersion().getParent();
               treeViewer.remove(verEvent.getOldVersion());

               if (parentArtifact != null) treeViewer.refresh(parentArtifact);

            } else if (event instanceof TransactionEvent) {
               ((TransactionEvent) event).fireSingleEvent(artifactExplorer);
            } else if (event instanceof DefaultBranchChangedEvent) {
               try {
                  // Check that we are not already displaying the default
                  // branch
                  Branch defaultBranch = BranchPersistenceManager.getInstance().getDefaultBranch();

                  if (root == null) {
                     explore(ArtifactPersistenceManager.getInstance().getDefaultHierarchyRootArtifact(defaultBranch));
                  } else if (root.getBranch() != defaultBranch) {
                     Artifact candidate =
                           ArtifactPersistenceManager.getInstance().getArtifact(root.getGuid(), defaultBranch);
                     if (candidate == null) {
                        explore(ArtifactPersistenceManager.getInstance().getDefaultHierarchyRootArtifact(defaultBranch));
                     } else {
                        explore(candidate);
                     }
                  }
               } catch (Exception ex) {
                  logger.log(Level.SEVERE, ex.toString(), ex);
               } finally {
                  updateEnablementsEtAl();
               }
            } else if (event instanceof ArtifactLockStatusChanged) {
               treeViewer.update(((ArtifactLockStatusChanged) event).getArtifact(), null);
            } else if (event instanceof AuthenticationEvent) {
               treeViewer.refresh();
            } else if ((event instanceof LocalCommitBranchEvent) || (event instanceof RemoteCommitBranchEvent)) {
               Object object = treeViewer.getInput();

               if (object instanceof Artifact) {
                  Artifact artifact = (Artifact) object;
                  try {
                     explore(ArtifactPersistenceManager.getInstance().getArtifact(artifact.getGuid(),
                           BranchPersistenceManager.getInstance().getDefaultBranch()));
                  } catch (IllegalArgumentException ex) {
                     logger.log(Level.SEVERE, ex.toString(), ex);
                  } catch (CoreException ex) {
                     logger.log(Level.SEVERE, ex.toString(), ex);
                  } catch (SQLException ex) {
                     logger.log(Level.SEVERE, ex.toString(), ex);
                  }
               }
            }
         }
      } catch (SQLException ex) {
         SkynetGuiPlugin.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
   }

   public boolean runOnEventInDisplayThread() {
      return true;
   }

   private class keySelectedListener implements KeyListener {
      public void keyPressed(KeyEvent e) {
         GlobalMenuPermissions permiss = new GlobalMenuPermissions(globalMenuHelper);

         if (e.keyCode == SWT.DEL && permiss.isWritePermission() && e.stateMask == 0) {
            (new GlobalMenu(new ArtifactTreeViewerGlobalMenuHelper(treeViewer))).getDeleteArtifactAction().run();
         }
         if (e.keyCode == 'a' && e.stateMask == SWT.CONTROL && permiss.isReadPermission()) {
            treeViewer.getTree().selectAll();
         }
         if (e.keyCode == 'x' && e.stateMask == SWT.CONTROL && permiss.isReadPermission()) {
            expandAll((IStructuredSelection) treeViewer.getSelection());
         }
         if (e.keyCode == 'c' && e.stateMask == SWT.CONTROL && permiss.isWritePermission()) {
            performCopy();
         }
         if (e.keyCode == 'v' && e.stateMask == SWT.CONTROL && permiss.isWritePermission()) {
            performPaste();
         }
      }

      public void keyReleased(KeyEvent e) {
      }
   }

   @Override
   public void init(IViewSite site, IMemento memento) throws PartInitException {
      super.init(site, memento);

      if (!DbConnectionExceptionComposite.dbConnectionIsOk(null)) return;
      try {
         if (memento != null) {

            Artifact previousArtifact =
                  ArtifactPersistenceManager.getInstance().getArtifact(memento.getString(ROOT_GUID),
                        BranchPersistenceManager.getInstance().getDefaultBranch());
            if (previousArtifact != null) {
               explore(previousArtifact);
               return;
            }
         }
      } catch (Exception ex) {
         logger.log(Level.SEVERE, "Falling back to the root artifact: " + ex.getLocalizedMessage(), ex);
      }

      try {
         explore(ArtifactPersistenceManager.getInstance().getDefaultHierarchyRootArtifact(
               BranchPersistenceManager.getInstance().getDefaultBranch()));
      } catch (Exception ex) {
         logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
   }

   @Override
   public void saveState(IMemento memento) {
      super.saveState(memento);
      if (root != null) {
         memento.putString(ROOT_GUID, root.getGuid());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.part.WorkbenchPart#dispose()
    */
   @Override
   public void dispose() {
      super.dispose();
      SkynetEventManager.getInstance().unRegisterAll(this);
      trees.remove(treeViewer.getTree());
   }

   public String getActionDescription() {
      return "";
   }

   public boolean showArtIds() {
      return showArtIds != null && showArtIds.isChecked();
   }

   public boolean showArtType() {
      return showArtType != null && showArtType.isChecked();
   }

   private class ArtifactExplorerDragAndDrop extends SkynetDragAndDrop {

      public ArtifactExplorerDragAndDrop(Tree tree, String viewId) {
         super(tree, tree, viewId);
      }

      @Override
      public Artifact[] getArtifacts() {
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
         Object[] objects = selection.toArray();
         Artifact[] artifacts = new Artifact[objects.length];

         for (int index = 0; index < objects.length; index++)
            artifacts[index] = (Artifact) objects[index];

         return artifacts;
      }

      @Override
      public void performDragOver(DropTargetEvent event) {
         event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;

         if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
            event.detail = DND.DROP_COPY;
         } else if (isValidForArtifactDrop(event)) {
            event.detail = DND.DROP_MOVE;
         } else {
            event.detail = DND.DROP_NONE;
         }
      }

      private boolean isValidForArtifactDrop(DropTargetEvent event) {
         if (ArtifactTransfer.getInstance().isSupportedType(event.currentDataType)) {
            ArtifactData artData = ArtifactTransfer.getInstance().nativeToJava(event.currentDataType);

            if (artData != null) {

               Artifact parentArtifact = getSelectedArtifact(event);
               if (parentArtifact != null && artData.getSource().equals(VIEW_ID)) {
                  Artifact[] artifactsToBeRelated = artData.getArtifacts();

                  for (Artifact artifact : artifactsToBeRelated) {
                     if (parentArtifact == artifact) {
                        return false;
                     }
                     for (Artifact descendant : artifact.getLoadedDescendants()) {
                        if (parentArtifact == descendant) {
                           return false;
                        }
                     }

                  }

                  return true;
               }
            } else {
               // only occurs during the drag on some platforms
               return true;
            }
         }
         return false;
      }

      private Artifact getSelectedArtifact(DropTargetEvent event) {
         TreeItem selected = treeViewer.getTree().getItem(treeViewer.getTree().toControl(event.x, event.y));

         if (selected != null && selected.getData() instanceof Artifact) {
            return (Artifact) selected.getData();
         }
         return null;
      }

      @Override
      public void performDrop(final DropTargetEvent event) {
         final Artifact parentArtifact = getSelectedArtifact(event);

         if (parentArtifact != null) {

            if (ArtifactTransfer.getInstance().isSupportedType(event.currentDataType) && isValidForArtifactDrop(event) && MessageDialog.openQuestion(
                  getViewSite().getShell(),
                  "Confirm Move",
                  "Are you sure you want to make each of the selected artifacts a child of " + parentArtifact.getDescriptiveName() + "?")) {
               ArtifactData artData = ArtifactTransfer.getInstance().nativeToJava(event.currentDataType);
               final Artifact[] artifactsToBeRelated = artData.getArtifacts();

               AbstractSkynetTxTemplate replaceRelationTx = new AbstractSkynetTxTemplate(parentArtifact.getBranch()) {

                  @Override
                  protected void handleTxWork() throws Exception {
                     // Replace all of the parent relations
                     for (Artifact artifact : artifactsToBeRelated) {
                        artifact.relateReplace(RelationSide.DEFAULT_HIERARCHICAL__PARENT, parentArtifact, true);
                        artifact.persistAttributes();
                     }
                  }

               };

               try {
                  replaceRelationTx.execute();
               } catch (Exception ex) {
                  OSEELog.logException(getClass(), ex, true);
               }
            }

            else if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
               Object object = FileTransfer.getInstance().nativeToJava(event.currentDataType);
               if (object instanceof String[]) {
                  String filename = ((String[]) object)[0];

                  ArtifactImportWizard wizard = new ArtifactImportWizard();
                  wizard.setImportResourceAndArtifactDestination(new File(filename), parentArtifact);

                  Wizards.initAndOpen(wizard, ArtifactExplorer.this);
               }
            }
         }
      }
   }

   public void addSelectionChangedListener(ISelectionChangedListener listener) {
      treeViewer.addSelectionChangedListener(listener);
   }

   public ISelection getSelection() {
      return treeViewer.getSelection();
   }

   public void removeSelectionChangedListener(ISelectionChangedListener listener) {
      treeViewer.removeSelectionChangedListener(listener);
   }

   public void setSelection(ISelection selection) {
      treeViewer.setSelection(selection);
   }

   private void setHelpContexts() {
      SkynetGuiPlugin.getInstance().setHelp(treeViewer.getControl(), "artifact_explorer_tree_viewer");
   }
}
