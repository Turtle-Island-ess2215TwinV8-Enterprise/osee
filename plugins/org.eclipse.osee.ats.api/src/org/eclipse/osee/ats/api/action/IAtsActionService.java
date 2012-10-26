/*
 * Created on Aug 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.action;

import java.util.Collection;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public interface IAtsActionService {

   public Collection<IAtsTeamWorkflow> getTeamWorkflows(IAtsAction action) throws OseeCoreException;

   public IAtsTeamWorkflow getFirstTeamWorkflow(IAtsAction action) throws OseeCoreException;

}
