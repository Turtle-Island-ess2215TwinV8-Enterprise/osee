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

import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.core.workflow.AtsWorkItemService;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;

/**
 * Show the overview html that will be emailed on notification or email of an action.
 * 
 * @author Donald G. Dunne
 */
public class WorkItemToOverviewItem extends XNavigateItemAction {

   public WorkItemToOverviewItem(XNavigateItem parent) {
      super(parent, "Work Item to Overview", AtsImage.REPORT);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) throws Exception {

      EntryDialog dialog = new EntryDialog(getName(), "Enter HRID of workflow");
      if (dialog.open() == 0) {
         String hrid = dialog.getEntry();
         Artifact artifactFromId = ArtifactQuery.getArtifactFromId(hrid, AtsUtil.getAtsBranchToken());
         String html = AtsWorkItemService.get().getOverviewHtml((IAtsWorkItem) artifactFromId);

         XResultData rd = new XResultData(false);
         rd.addRaw(html);
         XResultDataUI.report(rd, getName());
      }
   }
}
