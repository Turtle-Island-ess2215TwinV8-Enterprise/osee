/*
 * Created on Jun 4, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.core.internal.Activator;
import org.eclipse.osee.ats.core.workflow.AtsWorkItemService;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public class Versions {

   private static Set<String> targetErrorLoggedForId = new HashSet<String>(10);

   public static Collection<String> getNames(Collection<? extends IAtsVersion> versions) {
      ArrayList<String> names = new ArrayList<String>();
      for (IAtsVersion version : versions) {
         names.add(version.getName());
      }
      return names;
   }

   public static String getTargetedVersionStr(Object object) throws OseeCoreException {
      if (object instanceof IAtsWorkItem) {
         IAtsTeamWorkflow teamWf = AtsWorkItemService.get().getParentTeamWorkflow((IAtsWorkItem) object);
         if (teamWf != null) {
            IAtsVersion version = AtsVersionService.get().getTargetedVersion(object);
            if (version != null) {
               if (!AtsWorkItemService.get().isCompletedOrCancelled(teamWf) && AtsVersionService.get().isReleased(
                  teamWf)) {
                  String errStr =
                     "Workflow " + teamWf.getHumanReadableId() + " targeted for released version, but not completed: " + version;
                  // only log error once
                  if (!targetErrorLoggedForId.contains(teamWf.getGuid())) {
                     OseeLog.log(Activator.class, Level.SEVERE, errStr, null);
                     targetErrorLoggedForId.add(teamWf.getGuid());
                  }
                  return "!Error " + errStr;
               }
               return version.getName();
            }
         }
      }
      return "";
   }
}
