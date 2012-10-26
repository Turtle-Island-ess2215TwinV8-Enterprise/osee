/*
 * Created on Aug 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.action;

import java.util.Collection;
import org.eclipse.osee.ats.api.action.IAtsAction;
import org.eclipse.osee.ats.api.action.IAtsActionService;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public class AtsActionServiceImpl implements IAtsActionService {

   private static AtsActionServiceImpl instance = new AtsActionServiceImpl();

   public static AtsActionServiceImpl get() {
      return instance;
   }

   @Override
   public Collection<IAtsTeamWorkflow> getTeamWorkflows(IAtsAction action) throws OseeCoreException {
      return null;
   }

   @Override
   public IAtsTeamWorkflow getFirstTeamWorkflow(IAtsAction action) throws OseeCoreException {
      return null;
   }

}
