/*
 * Created on Jun 25, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.workdef;

import java.util.List;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.util.WorkDefinitionMatch;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.Pair;

/**
 * @author Donald G. Dunne
 */
public interface IAtsWorkDefinitionStore {

   public abstract boolean isWorkDefinitionExists(String workDefId) throws OseeCoreException;

   public abstract String loadWorkDefinitionString(String workDefId) throws OseeCoreException;

   public abstract IAttributeResolver getAttributeResolver();

   public abstract IUserResolver getUserResolver();

   public abstract List<Pair<String, String>> getWorkDefinitionStrings() throws OseeCoreException;

   public abstract String getWorkDefinitionAttribute(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract String getRelatedTaskWorkDefinitionAttribute(IAtsWorkItem workItem) throws OseeCoreException;

   public abstract WorkDefinitionMatch getWorkDefinitionFromTaskViaProviders(IAtsTeamWorkflow teamWf) throws OseeCoreException;

   public abstract WorkDefinitionMatch getWorkDefinitionFromProviders(IAtsWorkItem workItem) throws OseeCoreException;
}
