/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.event.EventUtil;
import org.eclipse.osee.framework.skynet.core.event.filter.ArtifactTypeEventFilter;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.utility.DbUtil;

/**
 * @author Donald G. Dunne
 */
public class AtsUtilCore {
   public final static double DEFAULT_HOURS_PER_WORK_DAY = 8;
   private static ArtifactTypeEventFilter atsObjectArtifactTypesFilter = new ArtifactTypeEventFilter(
      AtsArtifactTypes.TeamWorkflow, AtsArtifactTypes.Action, AtsArtifactTypes.Task, AtsArtifactTypes.Goal,
      AtsArtifactTypes.PeerToPeerReview, AtsArtifactTypes.DecisionReview);
   private static ArtifactTypeEventFilter reviewArtifactTypesFilter = new ArtifactTypeEventFilter(
      AtsArtifactTypes.PeerToPeerReview, AtsArtifactTypes.DecisionReview);
   private static ArtifactTypeEventFilter teamWorkflowArtifactTypesFilter = new ArtifactTypeEventFilter(
      AtsArtifactTypes.TeamWorkflow);
   private static List<IEventFilter> atsObjectEventFilter = new ArrayList<IEventFilter>(2);
   private static boolean emailEnabled = true;

   public static boolean isEmailEnabled() {
      return emailEnabled;
   }

   public static boolean isInTest() {
      return Boolean.valueOf(System.getProperty("osee.isInTest"));
   }

   public static void setEmailEnabled(boolean enabled) {
      if (!DbUtil.isDbInit() && !AtsUtilCore.isInTest()) {
         OseeLog.log(Activator.class, Level.INFO, "Email " + (enabled ? "Enabled" : "Disabled"));
      }
      emailEnabled = enabled;
   }

   /**
    * TODO Remove duplicate Active flags, need to convert all ats.Active to Active in DB
    *
    * @param artifacts to iterate through
    * @param active state to validate against; Both will return all artifacts matching type
    * @param clazz type of artifacts to consider; null for all
    * @return set of Artifacts of type clazz that match the given active state of the "Active" or "ats.Active" attribute
    * value. If no attribute exists, Active == true;
    */
   @SuppressWarnings("unchecked")
   public static <A extends Artifact> List<A> getActive(Collection<A> artifacts, Active active, Class<? extends Artifact> clazz) throws OseeCoreException {
      List<A> results = new ArrayList<A>();
      Collection<? extends Artifact> artsOfClass =
         clazz != null ? Collections.castMatching(clazz, artifacts) : artifacts;
      for (Artifact art : artsOfClass) {
         if (active == Active.Both) {
            results.add((A) art);
         } else {
            // assume active unless otherwise specified
            boolean attributeActive = ((A) art).getSoleAttributeValue(AtsAttributeTypes.Active, false);
            if (active == Active.Active && attributeActive) {
               results.add((A) art);
            } else if (active == Active.InActive && !attributeActive) {
               results.add((A) art);
            }
         }
      }
      return results;
   }

   public static boolean isAtsAdmin() {
      try {
         return AtsGroup.AtsAdmin.isCurrentUserMember();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return false;
      }
   }

   public static String doubleToI18nString(double d) {
      return doubleToI18nString(d, false);
   }

   public static String doubleToI18nString(double d, boolean blankIfZero) {
      if (blankIfZero && d == 0) {
         return "";
      }
      // This enables java to use same string for all 0 cases instead of creating new one
      else if (d == 0) {
         return "0.00";
      } else {
         return String.format("%4.2f", d);
      }
   }

   public static Branch getAtsBranch() throws OseeCoreException {
      return BranchManager.getCommonBranch();
   }

   public static IOseeBranch getAtsBranchToken() {
      return CoreBranches.COMMON;
   }

   public static Artifact getFromToken(IArtifactToken token) {
      Artifact toReturn = null;
      try {
         toReturn = ArtifactQuery.getArtifactFromToken(token, getAtsBranchToken());
      } catch (OseeCoreException ex) {
         // Do Nothing;
      }
      return toReturn;
   }

   public synchronized static List<IEventFilter> getAtsObjectEventFilters() {
      try {
         if (atsObjectEventFilter.isEmpty()) {
            atsObjectEventFilter.add(EventUtil.getCommonBranchFilter());
            atsObjectEventFilter.add(getAtsObjectArtifactTypeEventFilter());
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return atsObjectEventFilter;
   }

   public static ArtifactTypeEventFilter getAtsObjectArtifactTypeEventFilter() {
      return atsObjectArtifactTypesFilter;
   }

   public static ArtifactTypeEventFilter getTeamWorkflowArtifactTypeEventFilter() {
      return teamWorkflowArtifactTypesFilter;
   }

   public static ArtifactTypeEventFilter getReviewArtifactTypeEventFilter() {
      return reviewArtifactTypesFilter;
   }

}
