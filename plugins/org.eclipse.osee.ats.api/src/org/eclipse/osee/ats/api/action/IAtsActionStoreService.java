/*
 * Created on Aug 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.action;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public interface IAtsActionStoreService {

   public Collection<? extends IAtsTeamWorkflow> getTeamWorkflows(IAtsWorkItem workItem) throws OseeCoreException;

   public IAtsTeamWorkflow getFirstTeamWorkflow(IAtsWorkItem workItem) throws OseeCoreException;

}
