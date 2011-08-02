/*
 * Created on Jul 15, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.walker;

import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.core.artifact.GoalArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;

public class GoalMemberWrapper implements IActionWalkerItem {

   private final GoalArtifact goal;

   public GoalMemberWrapper(GoalArtifact goal) {
      this.goal = goal;
   }

   @Override
   public String toString() {
      try {
         return String.format(goal.getMembers().size() + " Members");
      } catch (OseeCoreException ex) {
         return "Exception: " + ex.getLocalizedMessage();
      }
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((goal == null) ? 0 : goal.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      GoalMemberWrapper other = (GoalMemberWrapper) obj;
      if (goal == null) {
         if (other.goal != null) {
            return false;
         }
      } else if (!goal.equals(other.goal)) {
         return false;
      }
      return true;
   }

   @Override
   public Image getImage() {
      return ImageManager.getImage(AtsImage.GOAL);
   }

   @Override
   public String getName() {
      return toString();
   }

   @Override
   public void handleDoubleClick() {
      try {
         AtsUtil.openInAtsWorldEditor(String.format("Goal [%s] Members", goal.getName()),
            Collections.castAll(Artifact.class, goal.getMembers()));
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public GoalArtifact getGoal() {
      return goal;
   }

}