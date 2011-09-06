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
package org.eclipse.osee.framework.ui.skynet.history.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.xviewer.customize.XViewerCustomMenu;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.BranchDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.help.ui.OseeHelpContext;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.change.AttributeChange;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEventType;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.HelpUtil;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.OpenContributionItem;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.action.EditTransactionComment;
import org.eclipse.osee.framework.ui.skynet.action.ITransactionRecordSelectionProvider;
import org.eclipse.osee.framework.ui.skynet.action.WasIsCompareEditorAction;
import org.eclipse.osee.framework.ui.skynet.change.ChangeUiUtil;
import org.eclipse.osee.framework.ui.skynet.history.table.XHistoryWidget;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.listener.IRebuildMenuListener;
import org.eclipse.osee.framework.ui.skynet.menu.CompareArtifactAction;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author Ryan D. Brooks
 */
public class ResourceHistoryPage extends FormPage implements IBranchEventListener, ITransactionRecordSelectionProvider, IRebuildMenuListener {

   private XHistoryWidget xHistoryWidget;
   private Artifact artifact;
   private final ResourceHistoryEditor editor;
   private Label descriptionLabel;

   public ResourceHistoryPage(ResourceHistoryEditor editor) {
      super(editor, "resource.history", "Resource History");
      this.editor = editor;
   }

   @Override
   public void showBusy(boolean busy) {
      super.showBusy(busy);
      if (Widgets.isAccessible(getManagedForm().getForm())) {
         getManagedForm().getForm().getForm().setBusy(busy);
      }
   }

   @Override
   protected void createFormContent(IManagedForm managedForm) {
      super.createFormContent(managedForm);

      final ScrolledForm form = managedForm.getForm();
      final FormToolkit toolkit = managedForm.getToolkit();

      Composite parent = form.getBody();
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      layout.marginHeight = 10;
      layout.marginWidth = 6;
      layout.horizontalSpacing = 20;
      form.getBody().setLayout(layout);
      GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
      layoutData.widthHint = 300;
      form.getBody().setLayoutData(layoutData);

      updateTitle(form);
      updateImage(form);

      descriptionLabel = new Label(parent, SWT.NONE);
      descriptionLabel.setText("Loading...");

      managedForm.getMessageManager().setAutoUpdate(false);

      xHistoryWidget = new XHistoryWidget();
      xHistoryWidget.setDisplayLabel(false);
      xHistoryWidget.createWidgets(parent, 1);

      MenuManager menuManager = new MenuManager();
      menuManager.setRemoveAllWhenShown(true);
      menuManager.addMenuListener(new IMenuListener() {
         @Override
         public void menuAboutToShow(IMenuManager manager) {
            MenuManager menuManager = (MenuManager) manager;
            menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
         }
      });

      menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

      xHistoryWidget.getXViewer().getTree().setMenu(
         menuManager.createContextMenu(xHistoryWidget.getXViewer().getTree()));

      getSite().registerContextMenu(ResourceHistoryEditor.EDITOR_ID, menuManager, xHistoryWidget.getXViewer());
      getSite().setSelectionProvider(xHistoryWidget.getXViewer());

      HelpUtil.setHelp(parent, OseeHelpContext.HISTORY_VIEW);

      OseeStatusContributionItemFactory.addTo(editor, true);

      addToolBar(toolkit, form, true);
      form.reflow(true);

      HelpUtil.setHelp(form.getBody(), OseeHelpContext.CHANGE_REPORT_EDITOR);
   }

   private void setDescription(String desc) {
      descriptionLabel.setText(desc);
      descriptionLabel.getParent().layout();
   }

   private void updateTitle(ScrolledForm form) {
      form.setText(Strings.escapeAmpersands(getEditorInput().getName()));
   }

   private void updateImage(ScrolledForm form) {
      form.setImage(getEditor().getEditorInput().getImage());
   }

   private void addToolBar(FormToolkit toolkit, ScrolledForm form, boolean add) {
      IToolBarManager manager = form.getToolBarManager();
      if (add) {
         manager.add(xHistoryWidget.getXViewer().getCustomizeAction());
         manager.update(true);
      } else {
         manager.removeAll();
      }
      form.reflow(true);
   }

   private void onLoad(ResourceHistoryEditorInput input, boolean loadHistory) {
      String name = "History: ";
      String description = "";
      if (xHistoryWidget != null) {
         if (this.artifact == null && loadHistory) {
            Branch branch = null;
            try {
               branch = BranchManager.getBranchByGuid(input.getBranchGuid());
            } catch (BranchDoesNotExist ex) {
               name += "Branch not Found";
               description = "Stored branch not found - nothing to load.";
            } catch (OseeCoreException ex) {
               name += "Error";
               description = "Exception reloading (see log for details)";
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
            try {
               this.artifact = ArtifactQuery.getArtifactFromId(input.getArtGuid(), branch);
            } catch (ArtifactDoesNotExist ex) {
               name += "Artifact not Found";
               description = "Artifact not Found - nothing to load.";
            } catch (OseeCoreException ex) {
               name += "Error";
               description = "Exception reloading (see log for details)";
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
            name = this.artifact.getName();
         } else {
            name += input.getName();
            description = "Press Reload";
         }
         setPartName(name);
         setDescription(description);
         if (loadHistory && this.artifact != null) {
            xHistoryWidget.setInputData(artifact, loadHistory);
         } else {
            xHistoryWidget.getXViewer().clear();
         }
      }
   }

   @Override
   public ResourceHistoryEditor getEditor() {
      return (ResourceHistoryEditor) super.getEditor();
   }

   @Override
   public ResourceHistoryEditorInput getEditorInput() {
      return (ResourceHistoryEditorInput) super.getEditorInput();
   }

   public void onLoad(final boolean loadHistory) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            onLoad(editor.getEditorInput(), loadHistory);
         }
      });
   }

   public void refresh() {
      final ScrolledForm sForm = getManagedForm().getForm();
      for (IFormPart part : getManagedForm().getParts()) {
         part.refresh();
      }
      onLoad(true);

      updateTitle(sForm);
      updateImage(sForm);

      sForm.getBody().layout(true);
      sForm.reflow(true);
      getManagedForm().refresh();
   }

   private void handleBranchEvent(BranchEventType branchModType) {
      if (branchModType == BranchEventType.Deleting || branchModType == BranchEventType.Deleted || branchModType == BranchEventType.Purging || branchModType == BranchEventType.Purged) {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               closeEditor();
            }
         });
         return;
      } else if (branchModType == BranchEventType.Committed) {
         onLoad(true);
      }
   }

   private void closeEditor() {
      final ResourceHistoryPage editor = this;
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            AWorkbench.getActivePage().closeEditor(editor, false);
         }
      });
   }

   @Override
   public void handleBranchEvent(Sender sender, BranchEvent branchEvent) {
      handleBranchEvent(branchEvent.getEventType());
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      if (artifact != null) {
         return OseeEventManager.getEventFiltersForBranch(artifact.getBranch());
      }
      return null;
   }

   @Override
   public ArrayList<TransactionRecord> getSelectedTransactionRecords() {
      return xHistoryWidget.getSelectedTransactionRecords();
   }

   @Override
   public void refreshUI(ArrayList<TransactionRecord> records) {
      xHistoryWidget.refresh();
   }

   @Override
   public void rebuildMenu() {
      setupMenus();
   }

   private void setupMenus() {
      Menu popupMenu = new Menu(xHistoryWidget.getXViewer().getTree().getParent());

      OpenContributionItem contributionItem = new OpenContributionItem(getClass().getSimpleName() + ".open");
      contributionItem.fill(popupMenu, -1);
      new MenuItem(popupMenu, SWT.SEPARATOR);

      createChangeReportMenuItem(popupMenu);

      new MenuItem(popupMenu, SWT.SEPARATOR);
      createReplaceAttributeWithVersionMenuItem(popupMenu);

      IAction action = new CompareArtifactAction("Compare two Artifacts", xHistoryWidget.getXViewer());
      (new ActionContributionItem(action)).fill(popupMenu, 3);

      (new ActionContributionItem(new EditTransactionComment(this))).fill(popupMenu, 3);
      (new ActionContributionItem(new WasIsCompareEditorAction())).fill(popupMenu, 3);

      // Setup generic xviewer menu items
      XViewerCustomMenu xMenu = new XViewerCustomMenu(xHistoryWidget.getXViewer());
      new MenuItem(popupMenu, SWT.SEPARATOR);
      xMenu.createTableCustomizationMenuItem(popupMenu);
      xMenu.createViewTableReportMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      xMenu.addCopyViewMenuBlock(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      xMenu.addFilterMenuBlock(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      xHistoryWidget.getXViewer().getTree().setMenu(popupMenu);
   }

   private void createReplaceAttributeWithVersionMenuItem(Menu popupMenu) {
      final MenuItem replaceWithMenu = new MenuItem(popupMenu, SWT.CASCADE);
      replaceWithMenu.setText("&Replace Attribute with Version");
      try {
         replaceWithMenu.setEnabled(AccessControlManager.isOseeAdmin());
      } catch (Exception ex) {
         replaceWithMenu.setEnabled(false);
      }
      popupMenu.addMenuListener(new MenuAdapter() {

         @Override
         public void menuShown(MenuEvent e) {
            List<?> selections = ((IStructuredSelection) xHistoryWidget.getXViewer().getSelection()).toList();
            replaceWithMenu.setEnabled(selections.size() == 1 && selections.iterator().next() instanceof AttributeChange);
         }

      });

      replaceWithMenu.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) xHistoryWidget.getXViewer().getSelection();
            Object selectedObject = selection.getFirstElement();

            if (selectedObject instanceof AttributeChange) {
               try {
                  AttributeChange attributeChange = (AttributeChange) selectedObject;
                  Artifact artifact =
                     ArtifactQuery.getArtifactFromId(attributeChange.getArtId(), attributeChange.getBranch());

                  for (Attribute<?> attribute : artifact.getAttributes(attributeChange.getAttributeType())) {
                     if (attribute.getId() == attributeChange.getAttrId()) {
                        attribute.replaceWithVersion((int) attributeChange.getGamma());
                        break;
                     }
                  }

                  artifact.persist("Replace attribute with version");
                  artifact.reloadAttributesAndRelations();

               } catch (OseeCoreException ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         }

      });
   }

   private void createChangeReportMenuItem(Menu popupMenu) {
      final MenuItem changeReportMenuItem = new MenuItem(popupMenu, SWT.CASCADE);
      changeReportMenuItem.setText("&Change Report");
      changeReportMenuItem.setImage(ImageManager.getImage(FrameworkImage.BRANCH_CHANGE));
      popupMenu.addMenuListener(new MenuAdapter() {

         @Override
         public void menuShown(MenuEvent e) {
            List<?> selections = ((IStructuredSelection) xHistoryWidget.getXViewer().getSelection()).toList();
            changeReportMenuItem.setEnabled(selections.size() == 1 && ((Change) selections.iterator().next()).getTxDelta().getStartTx().getTxType() != TransactionDetailsType.Baselined);
         }

      });

      changeReportMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) xHistoryWidget.getXViewer().getSelection();
            Object selectedObject = selection.getFirstElement();

            if (selectedObject instanceof Change) {
               try {
                  ChangeUiUtil.open(((Change) selectedObject).getTxDelta().getStartTx());
               } catch (OseeCoreException ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         }

      });
   }
}