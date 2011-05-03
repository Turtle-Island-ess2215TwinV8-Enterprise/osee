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
package org.eclipse.osee.ats.navigate;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.config.AtsDatabaseConfig;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.results.XResultData;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinition.WriteType;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Donald G. Dunne
 */
public class UpdateAtsWorkItemDefinitions extends XNavigateItemAction {

   public UpdateAtsWorkItemDefinitions(XNavigateItem parent) {
      super(parent, "Update Ats WorkItemDefinitions", PluginUiImage.ADMIN);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) throws OseeCoreException {
      if (!MessageDialog.openConfirm(Displays.getActiveShell(), getName(), getName())) {
         return;
      }
      if (!MessageDialog.openConfirm(Displays.getActiveShell(), getName(),
         "This could break lots of things, are you SURE?")) {
         return;
      }

      XResultData xResultData = new XResultData();
      AtsDatabaseConfig.configWorkItemDefinitions(WriteType.Update, xResultData);
      if (xResultData.isEmpty()) {
         xResultData.log("Nothing updated");
      }
      xResultData.report(getName());

      AWorkbench.popup("Completed", getName());
   }

}