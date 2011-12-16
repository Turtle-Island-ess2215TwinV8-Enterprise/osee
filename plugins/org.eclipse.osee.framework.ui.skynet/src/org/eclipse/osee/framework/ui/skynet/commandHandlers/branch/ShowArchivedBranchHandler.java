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
package org.eclipse.osee.framework.ui.skynet.commandHandlers.branch;

import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osee.framework.skynet.core.utility.DbUtil;
import org.eclipse.osee.framework.ui.skynet.widgets.xBranch.BranchOptionsEnum;
import org.eclipse.osee.framework.ui.skynet.widgets.xBranch.BranchView;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

/**
 * @author Jeff C. Phillips
 */
public class ShowArchivedBranchHandler extends AbstractHandler implements IElementUpdater {
   public static final String COMMAND_ID =
      "org.eclipse.osee.framework.ui.skynet.branch.BranchView.showArchivedBranches";

   private final ICommandService service;
   private boolean itemChk;

   public ShowArchivedBranchHandler() {
      this.service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
   }

   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
      ((BranchView) HandlerUtil.getActivePartChecked(event)).changePresentation(
         BranchOptionsEnum.SHOW_ARCHIVED_BRANCHES, !itemChk);
      return null;
   }

   @SuppressWarnings("rawtypes")
   @Override
   public void updateElement(UIElement element, Map parameters) {
      itemChk =
         Platform.getPreferencesService().getRootNode().node(InstanceScope.SCOPE).node(BranchView.VIEW_ID).getBoolean(
            BranchOptionsEnum.SHOW_ARCHIVED_BRANCHES.origKeyName, false);
      element.setChecked(itemChk);
   }

   @Override
   public boolean isEnabled() {
      boolean isValid = !DbUtil.isDbInit();
      service.refreshElements(COMMAND_ID, null);
      return isValid;
   }
}
