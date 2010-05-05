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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.ArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.Handlers;
import org.eclipse.osee.framework.ui.skynet.render.IRenderer;
import org.eclipse.osee.framework.ui.skynet.render.NativeRenderer;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.render.WordRenderer;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * Dynamically provides the open with CommandContributionItem for menu items based off of calling applicable renderers
 * getCommandId(presenationType).
 * 
 * @author Jeff C. Phillips
 */
public class OpenWithContributionItem extends CompoundContributionItem {
   private ICommandService commandService;

   public OpenWithContributionItem() {
      this.commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
   }

   public OpenWithContributionItem(String id) {
      super(id);
   }

   @Override
   protected IContributionItem[] getContributionItems() {
      ArrayList<IContributionItem> contributionItems = new ArrayList<IContributionItem>(40);
      List<Artifact> artifacts = getSelectedArtifacts();
      if (artifacts != null && !artifacts.isEmpty()) {
         try {
            contributionItems.addAll(getCommonContributionItems(artifacts, PresentationType.PREVIEW));
            //add separator between preview and edit commands
            if (!contributionItems.isEmpty()) {
               contributionItems.add(new Separator());
            }
            contributionItems.addAll(getCommonContributionItems(artifacts, PresentationType.SPECIALIZED_EDIT));
         } catch (OseeCoreException ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
      }

      return contributionItems.toArray(new IContributionItem[0]);
   }

   private ArrayList<IContributionItem> getCommonContributionItems(List<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException {
      ArrayList<IContributionItem> contributionItems = new ArrayList<IContributionItem>(25);
      List<IRenderer> commonRenders = RendererManager.getCommonRenderers(artifacts, presentationType);
      Artifact firstArtifact = artifacts.iterator().next();
      for (IRenderer render : commonRenders) {
         ImageDescriptor imageDescriptor = null;
         if (render instanceof WordRenderer) {
            imageDescriptor = WordRenderer.getImageDescriptor();
         } else if (render instanceof NativeRenderer) {
            String fileExtension = firstArtifact.getSoleAttributeValue(CoreAttributeTypes.NATIVE_EXTENSION.getName());
            if (Strings.isValid(fileExtension)) {
               imageDescriptor = ImageManager.getProgramImageDescriptor(fileExtension);
            }
         }
         List<IContributionItem> commandList = loadCommands(render, presentationType, imageDescriptor);
         contributionItems.addAll(commandList);
      }
      return contributionItems;
   }

   private ArrayList<IContributionItem> loadCommands(IRenderer renderer, PresentationType presentationType, ImageDescriptor imageDescriptor) {
      ArrayList<IContributionItem> contributionItems = new ArrayList<IContributionItem>(25);
      CommandContributionItem contributionItem = null;

      for (String commandId : renderer.getCommandId(presentationType)) {
         contributionItem =
               new CommandContributionItem(new CommandContributionItemParameter(
                     PlatformUI.getWorkbench().getActiveWorkbenchWindow(), commandId, commandId,
                     Collections.emptyMap(), imageDescriptor, null, null, null, null, null, SWT.NONE, null, false));

         Command command = commandService.getCommand(contributionItem.getId());
         if (command != null && command.isEnabled()) {
            contributionItems.add(contributionItem);
         }
      }

      return contributionItems;
   }

   @Override
   public void fill(final ToolBar parent, int index) {
      final ToolItem toolItem = new ToolItem(parent, SWT.DROP_DOWN);
      toolItem.setImage(ImageManager.getImage(FrameworkImage.OPEN));
      toolItem.setToolTipText("Open the Artifact");

      OpenWithToolItemListener listener = new OpenWithToolItemListener(parent.getShell());
      toolItem.addListener(SWT.Selection, listener);
      toolItem.setEnabled(listener.isPreviewMenuEnabled());
   }

   @Override
   public void fill(Menu parent, int index) {
      /**
       * hard coded to show as the 2nd menu option; necessary for Xviewer menu options to work with Change View
       * command/handlers and this dynamic menu option
       */
      final MenuItem item = new MenuItem(parent, SWT.CASCADE, 1);
      item.setText("Open With");

      Menu subMenu = new Menu(parent);
      item.setMenu(subMenu);
      createDropDownMenu(parent.getShell(), subMenu);
      item.setEnabled(isMenuEnabled(subMenu));
   }

   private List<Artifact> getSelectedArtifacts() {
      IWorkbenchPage page = AWorkbench.getActivePage();
      if (page != null) {
         IWorkbenchPart part = page.getActivePart();
         if (part != null) {
            IWorkbenchPartSite site = part.getSite();
            if (site != null) {
               ISelectionProvider selectionProvider = site.getSelectionProvider();
               if (selectionProvider != null && selectionProvider.getSelection() instanceof IStructuredSelection) {
                  IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
                  return Handlers.getArtifactsFromStructuredSelection(structuredSelection);
               }
            }
         }
      }
      return Collections.emptyList();
   }

   private void createDropDownMenu(Shell shell, Menu previewMenu) {
      List<Artifact> artifacts = getSelectedArtifacts();
      boolean previewable = false;
      try {
         if (artifacts != null && !artifacts.isEmpty()) {
            Artifact artifact = artifacts.iterator().next();

            if (RendererManager.getApplicableRenderers(PresentationType.PREVIEW, artifact, null).size() > 1) {
               previewable = true;
            }

            if (previewable) {
               if (OpenWithMenuListener.loadMenuItems(previewMenu, PresentationType.PREVIEW, artifacts)) {
                  new MenuItem(previewMenu, SWT.SEPARATOR);
               }
            }
            OpenWithMenuListener.loadMenuItems(previewMenu, PresentationType.SPECIALIZED_EDIT, artifacts);
         }
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
      }
   }

   public boolean isMenuEnabled(Menu menu) {
      boolean itemzEnabled = false;
      for (MenuItem menuItems : menu.getItems()) {
         if (menuItems.isEnabled()) {
            itemzEnabled = true;
         }
      }
      return itemzEnabled;
   }

   private final class OpenWithToolItemListener implements Listener {
      private final Menu previewMenu;
      private final boolean isPreviewEnabled;

      public OpenWithToolItemListener(Shell shell) {
         previewMenu = new Menu(shell, SWT.POP_UP);
         createDropDownMenu(shell, previewMenu);
         isPreviewEnabled = isPreviewMenuEnabled();
      }

      public boolean isPreviewMenuEnabled() {
         return isMenuEnabled(previewMenu);
      }

      @Override
      public void handleEvent(Event event) {
         Widget widget = event.widget;
         if (widget instanceof ToolItem) {
            ToolItem toolItem = (ToolItem) widget;
            ToolBar toolBar = toolItem.getParent();

            if (event.detail == SWT.ARROW) {
               Rectangle rect = toolItem.getBounds();
               Point pt = new Point(rect.x, rect.y + rect.height);
               pt = toolBar.toDisplay(pt);
               previewMenu.setLocation(pt.x, pt.y);
               previewMenu.setVisible(true);
            }

            if (event.detail == 0) {
               try {
                  List<Artifact> artifacts = getSelectedArtifacts();
                  if (artifacts != null && !artifacts.isEmpty()) {
                     Artifact artifact = artifacts.iterator().next();
                     if (isPreviewEnabled && artifact != null) {
                        //                        if (true) {
                        RendererManager.previewInJob(artifact);
                        //                        } else {
                        //                           RendererManager.openInJob(artifact, PresentationType.SPECIALIZED_EDIT);
                        //                        }
                     }
                  }
               } catch (OseeCoreException ex) {
                  OseeLog.log(ArtifactEditor.class, Level.SEVERE, ex.getMessage());
               }
            }
         }
      }
   }
}
