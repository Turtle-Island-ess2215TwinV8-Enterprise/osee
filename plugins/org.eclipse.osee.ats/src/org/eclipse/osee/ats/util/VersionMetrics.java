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
package org.eclipse.osee.ats.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.artifact.VersionArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.util.ChangeType;

/**
 * @author Donald G. Dunne
 */
public class VersionMetrics {

   private final VersionArtifact verArt;
   private final VersionTeamMetrics verTeamMet;

   public VersionMetrics(VersionArtifact verArt, VersionTeamMetrics verTeamMet) {
      this.verArt = verArt;
      this.verTeamMet = verTeamMet;
   }

   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer(verArt.getName());
      sb.append("\n");
      try {
         sb.append("Workflows: ");
         sb.append(verArt.getTargetedForTeamArtifacts().size());
         sb.append(" Problem: ");
         sb.append(getTeamWorkFlows(ChangeType.Problem).size());
         sb.append(" Improve: ");
         sb.append(getTeamWorkFlows(ChangeType.Improvement).size());
         sb.append(" Support: ");
         sb.append(getTeamWorkFlows(ChangeType.Support).size());
         sb.append(" Release Date: ");
         sb.append(verArt.getReleaseDate());
         VersionMetrics prevVerMet = getPreviousVerMetViaReleaseDate();
         if (prevVerMet == null) {
            sb.append(" Prev Release Version: <not found>");
         } else {
            sb.append(" Prev Release Version \"");
            sb.append(prevVerMet);
            sb.append("\"   Release Date: ");
            sb.append(verArt.getReleaseDate());
         }
         sb.append(" Start Date: ");
         sb.append(getReleaseStartDate());
         sb.append(" Num Days: ");
         sb.append(getNumberDaysInRelease());
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
      return sb.toString();
   }

   public Integer getNumberDaysInRelease() throws OseeCoreException {
      Date startDate = getReleaseStartDate();
      if (startDate == null) {
         return null;
      }
      if (verArt.getReleaseDate() == null) {
         return null;
      }
      return DateUtil.getDifference(startDate, verArt.getReleaseDate());
   }

   public Date getReleaseStartDate() throws OseeCoreException {
      VersionMetrics prevVerMet = getPreviousVerMetViaReleaseDate();
      if (prevVerMet == null) {
         return null;
      }
      return prevVerMet.getVerArt().getReleaseDate();
   }

   public Collection<TeamWorkFlowArtifact> getTeamWorkFlows(ChangeType... changeType) throws OseeCoreException {
      List<ChangeType> changeTypes = Arrays.asList(changeType);
      Set<TeamWorkFlowArtifact> teams = new HashSet<TeamWorkFlowArtifact>();
      for (TeamWorkFlowArtifact team : verArt.getTargetedForTeamArtifacts()) {
         if (changeTypes.contains(team.getChangeType())) {
            teams.add(team);
         }
      }
      return teams;
   }

   public VersionMetrics getPreviousVerMetViaReleaseDate() throws OseeCoreException {
      if (verArt.getReleaseDate() == null) {
         return null;
      }
      int index = verTeamMet.getReleasedOrderedVersions().indexOf(this);
      if (index > 0) {
         return verTeamMet.getReleasedOrderedVersions().get(index - 1);
      }
      return null;
   }

   public VersionMetrics getNextVerMetViaReleaseDate() throws OseeCoreException {
      if (verArt.getReleaseDate() == null) {
         return null;
      }
      int index = verTeamMet.getReleasedOrderedVersions().indexOf(this);
      if (index < verTeamMet.getReleasedOrderedVersions().size() - 1) {
         return verTeamMet.getReleasedOrderedVersions().get(index + 1);
      }
      return null;
   }

   /**
    * @return the verArt
    */
   public VersionArtifact getVerArt() {
      return verArt;
   }

   /**
    * @return the verTeamMet
    */
   public VersionTeamMetrics getVerTeamMet() {
      return verTeamMet;
   }

}
