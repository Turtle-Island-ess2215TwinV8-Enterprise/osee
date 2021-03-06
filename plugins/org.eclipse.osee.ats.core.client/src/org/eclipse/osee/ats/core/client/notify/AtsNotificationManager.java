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
package org.eclipse.osee.ats.core.client.notify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.ExtensionDefinedObjects;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.email.EmailGroup;
import org.eclipse.osee.framework.skynet.core.utility.INotificationManager;
import org.eclipse.osee.framework.skynet.core.utility.OseeNotificationEvent;

/**
 * @author Donald G. Dunne
 */
public class AtsNotificationManager {

   private static ExtensionDefinedObjects<IAtsNotification> contributions;

   private static boolean inTest = false;

   private static ConfigurationProvider provider;

   public static interface ConfigurationProvider {
      INotificationManager getNotificationManager();

      boolean isProduction() throws OseeCoreException;
   }

   private AtsNotificationManager() {
      //
   }

   /**
    * Handle notifications for subscription by IAtsTeamDefinition and ActionableItem
    */
   public static void notifySubscribedByTeamOrActionableItem(TeamWorkFlowArtifact teamArt) {
      if (isInTest() || !AtsUtilCore.isEmailEnabled() || !isProduction()) {
         return;
      }
      boolean notificationAdded = false;
      try {

         Collection<IAtsUser> subscribedUsers = new HashSet<IAtsUser>();
         // Handle Team Definitions
         IAtsTeamDefinition teamDef = teamArt.getTeamDefinition();
         subscribedUsers.addAll(teamDef.getSubscribed());
         if (subscribedUsers.size() > 0) {
            notificationAdded = true;
            getNotificationManager().addNotificationEvent(
               new OseeNotificationEvent(
                  AtsClientService.get().getUserAdmin().getOseeUsers(subscribedUsers),
                  getIdString(teamArt),
                  "Workflow Creation",
                  "You have subscribed for email notification for Team \"" + teamArt.getTeamName() + "\"; New Team Workflow created with title \"" + teamArt.getName() + "\""));
         }

         // Handle Actionable Items
         for (IAtsActionableItem aia : teamArt.getActionableItemsDam().getActionableItems()) {
            subscribedUsers = aia.getSubscribed();
            if (subscribedUsers.size() > 0) {
               notificationAdded = true;
               getNotificationManager().addNotificationEvent(
                  new OseeNotificationEvent(
                     AtsClientService.get().getUserAdmin().getOseeUsers(subscribedUsers),
                     getIdString(teamArt),
                     "Workflow Creation",
                     "You have subscribed for email notification for Actionable Item \"" + teamArt.getTeamName() + "\"; New Team Workflow created with title \"" + teamArt.getName() + "\""));
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      } finally {
         if (notificationAdded) {
            try {
               getNotificationManager().sendNotifications();
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      }
   }

   public static synchronized List<IAtsNotification> getAtsNotificationItems() {
      if (contributions == null) {
         contributions =
            new ExtensionDefinedObjects<IAtsNotification>("org.eclipse.osee.ats.AtsNotification", "AtsNotification",
               "classname", true);
      }
      return contributions.getObjects();
   }

   protected static String getIdString(AbstractWorkflowArtifact sma) {
      try {
         String legacyPcrId = sma.getSoleAttributeValue(AtsAttributeTypes.LegacyPcrId, "");
         if (!legacyPcrId.equals("")) {
            return "HRID: " + sma.getHumanReadableId() + " / LegacyId: " + legacyPcrId;
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return "HRID: " + sma.getHumanReadableId();
   }

   public static void notify(AbstractWorkflowArtifact sma, AtsNotifyType... notifyTypes) throws OseeCoreException {
      notify(sma, null, notifyTypes);
   }

   public static void notify(AbstractWorkflowArtifact awa, Collection<? extends IAtsUser> notifyUsers, AtsNotifyType... notifyTypes) throws OseeCoreException {
      if (isInTest() || !AtsUtilCore.isEmailEnabled() || !isProduction() || awa.getName().startsWith("tt ")) {
         return;
      }
      AtsNotifyUsers.notify(getNotificationManager(), awa, notifyUsers, notifyTypes);
   }

   public static List<EmailGroup> getEmailableGroups(AbstractWorkflowArtifact workflow) throws OseeCoreException {
      ArrayList<EmailGroup> groupNames = new ArrayList<EmailGroup>();
      ArrayList<String> emails = new ArrayList<String>();
      User oseeUser = AtsClientService.get().getUserAdmin().getOseeUser(workflow.getCreatedBy());
      emails.add(oseeUser.getEmail());
      groupNames.add(new EmailGroup("Originator", emails));
      if (workflow.getStateMgr().getAssignees().size() > 0) {
         emails = new ArrayList<String>();
         for (IAtsUser u : workflow.getStateMgr().getAssignees()) {
            User oseeUser2 = AtsClientService.get().getUserAdmin().getOseeUser(u);
            emails.add(oseeUser2.getEmail());
         }
         groupNames.add(new EmailGroup("Assignees", emails));
      }
      return groupNames;
   }

   //////////////////////////////////// FOR TEST ////////////////////////////////////
   private static INotificationManager getNotificationManager() {
      return getConfigurationProvider().getNotificationManager();
   }

   public static void setConfigurationProvider(ConfigurationProvider provider) {
      AtsNotificationManager.provider = provider;
   }

   protected static ConfigurationProvider getConfigurationProvider() {
      return AtsNotificationManager.provider;
   }

   public static boolean isProduction() {
      boolean result = false;
      try {
         result = getConfigurationProvider().isProduction();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return result;
   }

   protected static boolean isInTest() {
      return AtsNotificationManager.inTest;
   }

   public static void setInTest(boolean inTest) {
      AtsNotificationManager.inTest = inTest;
   }
}
