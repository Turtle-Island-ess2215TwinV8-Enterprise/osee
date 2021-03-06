/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.core.model.IActionGroup;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * Implementers for a WorkItem are<br/>
 * <br/>
 * For In Work Item: blank<br/>
 * <br/>
 * For Completed or Cancelled: <br/>
 * 1) Assignees of CompletedFrom or CancelledFrom states <br/>
 * 2) CompletedBy or CancelledBy user of WorkItem <br/>
 * 3) Users identified by object's getImplementers() call, if any <br/>
 * <br/>
 * For ActionGroup, it's the set of users for each case above for each Action
 * 
 * @author Donald G. Dunne
 */
public class ImplementersColumn implements ImplementersStringProvider {

   public static ImplementersColumn instance = new ImplementersColumn();

   @Override
   public String getImplementersStr(Object object) throws OseeCoreException {
      List<IAtsUser> implementers = getImplementers(object);
      return implementers.isEmpty() ? "" : AtsObjects.toString("; ", implementers);
   }

   public List<IAtsUser> getImplementers(Object object) throws OseeCoreException {
      List<IAtsUser> implementers = new LinkedList<IAtsUser>();
      if (object instanceof IActionGroup) {
         implementers.addAll(getActionGroupImplementers((IActionGroup) object));
      } else if (object instanceof IAtsWorkItem) {
         implementers.addAll(getWorkItemImplementers((IAtsWorkItem) object));
      }
      implementers.remove(AtsCoreUsers.UNASSIGNED_USER);
      Collections.sort(implementers);
      return implementers;
   }

   public List<IAtsUser> getWorkItemImplementers(IAtsWorkItem workItem) throws OseeCoreException {
      List<IAtsUser> implementers = new ArrayList<IAtsUser>();
      getImplementers_fromWorkItem(workItem, implementers);
      getImplementers_fromCompletedCancelledBy(workItem, implementers);
      getImplementers_fromCompletedCancelledFrom(workItem, implementers);
      return implementers;
   }

   public void getImplementers_fromCompletedCancelledFrom(IAtsWorkItem workItem, List<IAtsUser> implementers) throws OseeCoreException {
      String fromStateName = null;
      if (workItem.getWorkData().isCompleted()) {
         fromStateName = workItem.getWorkData().getCompletedFromState();
      } else if (workItem.getWorkData().isCancelled()) {
         fromStateName = workItem.getWorkData().getCancelledFromState();
      }
      if (Strings.isValid(fromStateName)) {
         for (IAtsUser user : workItem.getStateData().getAssigneesForState(fromStateName)) {
            if (!implementers.contains(user)) {
               implementers.add(user);
            }
         }
      }
   }

   public void getImplementers_fromCompletedCancelledBy(IAtsWorkItem workItem, List<IAtsUser> implementers) throws OseeCoreException {
      if (workItem.getWorkData().isCompletedOrCancelled()) {
         if (workItem.getWorkData().isCompleted()) {
            IAtsUser completedBy = workItem.getWorkData().getCompletedBy();
            if (completedBy != null && !implementers.contains(completedBy)) {
               implementers.add(completedBy);
            }
         }
         if (workItem.getWorkData().isCancelled()) {
            IAtsUser cancelledBy = workItem.getWorkData().getCancelledBy();
            if (cancelledBy != null && !implementers.contains(cancelledBy)) {
               implementers.add(cancelledBy);
            }
         }
      }
   }

   public void getImplementers_fromWorkItem(IAtsWorkItem workItem, List<IAtsUser> implementers) throws OseeCoreException {
      for (IAtsUser user : workItem.getImplementers()) {
         if (!implementers.contains(user)) {
            implementers.add(user);
         }
      }
   }

   public List<IAtsUser> getActionGroupImplementers(IActionGroup actionGroup) throws OseeCoreException {
      List<IAtsUser> implementers = new LinkedList<IAtsUser>();
      for (IAtsWorkItem action : actionGroup.getActions()) {
         if (action.getWorkData().isCompletedOrCancelled()) {
            for (IAtsUser user : getWorkItemImplementers(action)) {
               if (!implementers.contains(user)) {
                  implementers.add(user);
               }
            }
         }
      }
      return implementers;
   }
}
