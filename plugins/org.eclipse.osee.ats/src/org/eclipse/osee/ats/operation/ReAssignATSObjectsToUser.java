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
package org.eclipse.osee.ats.operation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.notify.OseeNotificationManager;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactTypeAndDescriptiveLabelProvider;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.ArtifactCheckTreeDialog;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Donald G. Dunne
 */
public class ReAssignATSObjectsToUser extends AbstractBlam {

   public final static String FROM_ASSIGNEE = "From Assignee";
   public final static String TO_ASSIGNEE = "To Assignee";

   @Override
   public String getName() {
      return "Admin - Re-Assign ATS Objects To User";
   }

   @Override
   public void runOperation(final VariableMap variableMap, IProgressMonitor monitor) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            try {
               final User fromUser = variableMap.getUser(FROM_ASSIGNEE);
               if (fromUser == null) {
                  AWorkbench.popup("ERROR", "Please select From Assignee");
                  return;
               }

               final User toUser = variableMap.getUser(TO_ASSIGNEE);
               if (toUser == null) {
                  AWorkbench.popup("ERROR", "Please select To Assignee");
                  return;
               }

               // Get all things user is directly assigned to
               Collection<Artifact> assignedToArts = AtsUtil.getAssigned(AtsClientService.get().getUserAdmin().getUserFromOseeUser(fromUser));
               Set<Artifact> atsArts = new HashSet<Artifact>();
               for (Artifact assignedArt : assignedToArts) {
                  if (assignedArt instanceof AbstractWorkflowArtifact) {
                     atsArts.add(assignedArt);
                  }
               }
               if (atsArts.isEmpty()) {
                  AWorkbench.popup("ERROR", "Not workflows, tasks or reviews assigned to " + fromUser);
                  return;
               }

               // Show in list dialog and allow select for ones to change
               ArtifactCheckTreeDialog dialog =
                  new ArtifactCheckTreeDialog(atsArts, new ArtifactTypeAndDescriptiveLabelProvider());
               dialog.setTitle("ReAssign ATS Object to User");
               dialog.setMessage("Select to re-assign to user \"" + toUser);
               if (dialog.open() != 0) {
                  return;
               }
               final Collection<Artifact> artsToReAssign = dialog.getSelection();

               // Make the changes and persist
               for (Artifact artifact : artsToReAssign) {
                  if (artifact instanceof AbstractWorkflowArtifact) {
                     ((AbstractWorkflowArtifact) artifact).getStateMgr().removeAssignee(AtsClientService.get().getUserAdmin().getUserFromOseeUser(fromUser));
                     ((AbstractWorkflowArtifact) artifact).getStateMgr().addAssignee(AtsClientService.get().getUserAdmin().getUserFromOseeUser(toUser));
                  }
               }
               Artifacts.persistInTransaction("Re-Assign ATS Objects to User", artsToReAssign);
               OseeNotificationManager.getInstance().sendNotifications();
            } catch (Exception ex) {
               log(ex);
            }
         };
      });
   }

   @Override
   public String getXWidgetsXml() {
      StringBuffer buffer = new StringBuffer("<xWidgets>");
      buffer.append("<XWidget xwidgetType=\"XMembersComboAll\" displayName=\"" + FROM_ASSIGNEE + "\" />");
      buffer.append("<XWidget xwidgetType=\"XMembersCombo\" displayName=\"" + TO_ASSIGNEE + "\" />");
      buffer.append("</xWidgets>");
      return buffer.toString();
   }

   @Override
   public String getDescriptionUsage() {
      return "Re-Assign ATS Workflows, Tasks and Reviews to another user.  Enter to and from User and select play.  You will be promted to select the ATS Objects to reassign.";
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("ATS.Admin");
   }
}