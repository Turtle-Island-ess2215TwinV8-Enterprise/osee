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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.Handlers;
import org.eclipse.osee.framework.ui.skynet.render.IRenderer;
import org.eclipse.osee.framework.ui.skynet.render.IRenderer.CommandGroup;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * Dynamically provides the open/open with CommandContributionItem for menu items based off of calling applicable
 * {@link IRenderer#getCommandIds(CommandGroup)}
 * 
 * @author Jeff C. Phillips
 */
public class OpenContributionItem extends ContributionItem {

   private static final String DEFAULT_OPEN_CMD_ID = "org.eclipse.osee.framework.ui.skynet.open.command";

   private final Collection<IContributionItem> openWithItems = new ArrayList<IContributionItem>();
   private IContributionItem defaultOpenItem;

   private boolean dirty = true;

   public OpenContributionItem() {
      this(null);
   }

   public OpenContributionItem(String id) {
      super(id);
   }

   @Override
   public boolean isDynamic() {
      return true;
   }

   @Override
   public boolean isDirty() {
      return dirty;
   }

   @Override
   public void dispose() {
      clearOpenWithItems();
      clearDefaultOpenItem();
      super.dispose();
   }

   private void clearDefaultOpenItem() {
      if (defaultOpenItem != null) {
         defaultOpenItem.dispose();
      }
      defaultOpenItem = null;
   }

   private void clearOpenWithItems() {
      for (IContributionItem item : openWithItems) {
         item.dispose();
      }
      openWithItems.clear();
   }

   @Override
   public void fill(final ToolBar parent, int index) {
      final ToolItem toolItem = new ToolItem(parent, SWT.DROP_DOWN);
      toolItem.setImage(ImageManager.getImage(FrameworkImage.OPEN));
      toolItem.setToolTipText("Open the Artifact");

      final Menu previewMenu = new Menu(parent.getShell(), SWT.POP_UP);
      fillOpenWithSubMenu(previewMenu);

      final OpenWithToolItemListener listener = new OpenWithToolItemListener(previewMenu);
      toolItem.addListener(SWT.Selection, listener);

      dirty = false;
   }

   @Override
   public void fill(final Menu parent, int index) {
      if (index == -1) {
         index = parent.getItemCount();
      }
      IContributionItem openItem = createDefaultOpenItem();
      openItem.fill(parent, index);

      final MenuItem item = new MenuItem(parent, SWT.CASCADE, index + 1);
      item.setText("Open With");

      Menu subMenu = new Menu(item);
      fillOpenWithSubMenu(subMenu);
      item.setMenu(subMenu);
      item.setEnabled(isMenuEnabled(subMenu));

      final OpenWithOnShowListener listener = new OpenWithOnShowListener(item);
      parent.addMenuListener(listener);

      item.addDisposeListener(new DisposeListener() {

         @Override
         public void widgetDisposed(DisposeEvent e) {
            parent.removeMenuListener(listener);
            dirty = true;
         }
      });
      dirty = false;
   }

   @Override
   public void setParent(IContributionManager parent) {
      if (getParent() instanceof IMenuManager) {
         IMenuManager menuMgr = (IMenuManager) getParent();
         menuMgr.removeMenuListener(menuListener);
      }
      if (parent instanceof IMenuManager) {
         IMenuManager menuMgr = (IMenuManager) parent;
         menuMgr.addMenuListener(menuListener);
      }
      super.setParent(parent);
   }

   private final IMenuListener menuListener = new IMenuListener() {
      @Override
      public void menuAboutToShow(IMenuManager manager) {
         manager.markDirty();
         dirty = true;
      }
   };

   private static boolean isMenuEnabled(Menu menu) {
      for (MenuItem menuItems : menu.getItems()) {
         if (menuItems.isEnabled()) {
            return true;
         }
      }
      return false;
   }

   private IContributionItem createDefaultOpenItem() {
      clearDefaultOpenItem();
      defaultOpenItem = createContributionItem(DEFAULT_OPEN_CMD_ID, null);
      return defaultOpenItem;
   }

   private Collection<IContributionItem> createOpenWithItems() {
      clearOpenWithItems();
      List<Artifact> artifacts = getSelectedArtifacts();
      if (!artifacts.isEmpty()) {
         Artifact testArtifact = artifacts.iterator().next();
         try {
            CommandGroup[] groups = IRenderer.CommandGroup.values();
            CommandGroup lastGroup = groups[groups.length - 1];
            for (CommandGroup commandGroup : groups) {

               List<IRenderer> commonRenders =
                  RendererManager.getCommonRenderers(artifacts, commandGroup.getPresentationType());
               openWithItems.addAll(getCommonContributionItems(commandGroup, testArtifact, commonRenders));

               if (lastGroup != commandGroup && !openWithItems.isEmpty()) {
                  //add separator between presentation type commands
                  openWithItems.add(new Separator());
               }
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
      }
      return openWithItems;
   }

   private Collection<IContributionItem> getCommonContributionItems(CommandGroup commandGroup, Artifact testArtifact, Collection<IRenderer> commonRenders) throws OseeCoreException {
      ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
      Map<String, IContributionItem> contributedItems = new LinkedHashMap<String, IContributionItem>(25);
      for (IRenderer renderer : commonRenders) {
         for (String commandId : renderer.getCommandIds(commandGroup)) {
            Command command = commandService.getCommand(commandId);
            if (command != null && command.isEnabled()) {
               ImageDescriptor imageDescriptor = renderer.getCommandImageDescriptor(command, testArtifact);
               IContributionItem item = createContributionItem(commandId, imageDescriptor);
               contributedItems.put(commandId, item);
            }
         }
      }
      return contributedItems.values();
   }

   private IContributionItem createContributionItem(String commandId, ImageDescriptor imageDescriptor) {
      IContributionItem contributionItem =
         new CommandContributionItem(new CommandContributionItemParameter(
            PlatformUI.getWorkbench().getActiveWorkbenchWindow(), commandId, commandId, Collections.emptyMap(),
            imageDescriptor, null, null, null, null, null, SWT.NONE, null, false));
      return contributionItem;
   }

   private ISelectionProvider getSelectionProvider() {
      ISelectionProvider toReturn = null;
      IWorkbenchPage page = AWorkbench.getActivePage();
      if (page != null) {
         IWorkbenchPart part = page.getActivePart();
         if (part != null) {
            IWorkbenchPartSite site = part.getSite();
            if (site != null) {
               toReturn = site.getSelectionProvider();
            }
         }
      }
      return toReturn;
   }

   private List<Artifact> getSelectedArtifacts() {
      List<Artifact> toReturn = Collections.emptyList();
      ISelectionProvider selectionProvider = getSelectionProvider();
      if (selectionProvider != null) {
         ISelection selection = selectionProvider.getSelection();
         if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
            toReturn = Handlers.getArtifactsFromStructuredSelection(structuredSelection);
         }
      }
      return toReturn;
   }

   private void fillOpenWithSubMenu(Menu menu) {
      for (IContributionItem item : createOpenWithItems()) {
         item.fill(menu, -1);
      }
   }

   private final class OpenWithOnShowListener implements MenuListener {
      private final MenuItem parentItem;

      public OpenWithOnShowListener(MenuItem parentItem) {
         this.parentItem = parentItem;
      }

      @Override
      public void menuShown(MenuEvent e) {
         Menu oldMenu = parentItem.getMenu();
         if (oldMenu != null) {
            oldMenu.dispose();
         }

         Menu subMenu = new Menu(parentItem);
         fillOpenWithSubMenu(subMenu);
         parentItem.setMenu(subMenu);
         parentItem.setEnabled(isMenuEnabled(subMenu));
      }

      @Override
      public void menuHidden(MenuEvent e) {
         // Do Nothing
      }
   }

   private static final class OpenWithToolItemListener implements Listener {
      private final Menu subMenu;

      public OpenWithToolItemListener(Menu subMenu) {
         this.subMenu = subMenu;
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
               subMenu.setLocation(pt.x, pt.y);
               subMenu.setVisible(true);
            }

            if (event.detail == 0) {
               try {
                  IHandlerService handlerService =
                     (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);

                  ICommandService commandService =
                     (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

                  Command command = commandService.getCommand(DEFAULT_OPEN_CMD_ID);
                  if (command.isEnabled()) {
                     handlerService.executeCommand(command.getId(), null);
                  }
               } catch (Exception ex) {
                  OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         }
      }
   }
}