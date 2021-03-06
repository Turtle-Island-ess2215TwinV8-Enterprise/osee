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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.widgets.dialog.TeamDefinitionCheckTreeDialog;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;

/**
 * @author Donald G. Dunne
 */
public class SubscribeByTeamDefinition extends XNavigateItemAction {

   public SubscribeByTeamDefinition(XNavigateItem parent) {
      super(parent, "Subscribe by Team Definition", FrameworkImage.EMAIL);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) {
      final TeamDefinitionCheckTreeDialog diag =
         new TeamDefinitionCheckTreeDialog(getName(),
            "Select Team Definition\n\nEmail will be sent for every Action created against these Teams.", Active.Active);
      try {
         List<IAtsTeamDefinition> objs = new ArrayList<IAtsTeamDefinition>();
         objs.addAll(AtsClientService.get().getConfigObjects(
            AtsClientService.get().getUserAdmin().getCurrentOseeUser().getRelatedArtifacts(
               AtsRelationTypes.SubscribedUser_Artifact), IAtsTeamDefinition.class));
         diag.setInitialTeamDefs(objs);
         if (diag.open() != 0) {
            return;
         }
         Collection<IAtsTeamDefinition> selected = diag.getChecked();
         Collection<Artifact> arts = AtsClientService.get().getConfigArtifacts(selected);

         AtsClientService.get().getUserAdmin().getCurrentOseeUser().setRelationsOfTypeUseCurrentOrder(
            AtsRelationTypes.SubscribedUser_Artifact, arts, IAtsTeamDefinition.class);
         AtsClientService.get().getUserAdmin().getCurrentOseeUser().persist(getClass().getSimpleName());
         AWorkbench.popup(getName(), "Subscriptions updated.");
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }
}
