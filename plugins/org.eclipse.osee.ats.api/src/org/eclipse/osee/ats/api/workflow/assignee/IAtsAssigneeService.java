/*
 * Created on Aug 1, 2012
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

public interface IAtsAssigneeService {

   public abstract Collection<? extends IAtsUser> getAssignees(IAtsObject atsObject) throws OseeCoreException;

   public abstract String getAssigneeStr(IAtsObject atsObject) throws OseeCoreException;

   public abstract List<? extends IAtsUser> getImplementers(IAtsObject atsObject) throws OseeCoreException;

   public abstract List<? extends IAtsUser> getImplementers_fromWorkItem(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract String getImplementersStr(IAtsObject atsObject) throws OseeCoreException;

   public abstract List<IAtsUser> getAssigneesForState(IAtsWorkItem workItem, String fromStateName) throws OseeCoreException;

   public abstract void setAssigneesForState(IAtsWorkItem workItem, String stateName, List<? extends IAtsUser> asList) throws OseeCoreException;

   public abstract void addAssigneeForState(IAtsWorkItem workItem, String stateName, IAtsUser user) throws OseeCoreException;

   public abstract void addAssigneesForState(IAtsWorkItem workItem, String stateName, List<? extends IAtsUser> asList) throws OseeCoreException;

}
