/*
 * Created on Aug 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.workflow.assignee;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public interface IAtsAssigneeStoreService {

   public abstract Collection<? extends IAtsUser> getAssignees(IAtsObject atsObject) throws OseeCoreException;

   public abstract List<IAtsUser> getAssigneesForState(IAtsWorkItem workItem, String stateName) throws OseeCoreException;

   public abstract void setAssigneesForState(IAtsWorkItem workItem, String stateName, List<? extends IAtsUser> users) throws OseeCoreException;

   public abstract void addAssigneeForState(IAtsWorkItem workItem, String stateName, IAtsUser user) throws OseeCoreException;

   public abstract void addAssigneesForState(IAtsWorkItem workItem, String stateName, List<? extends IAtsUser> users) throws OseeCoreException;

   public abstract List<? extends IAtsUser> getImplementers_fromWorkItem(IAtsWorkItem workItem) throws OseeCoreException;

}
