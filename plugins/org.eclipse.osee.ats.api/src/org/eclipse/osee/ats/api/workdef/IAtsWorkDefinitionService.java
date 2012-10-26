/*
 * Created on Jun 21, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.workdef;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.task.IAtsTask;
import org.eclipse.osee.ats.api.util.WorkDefinitionMatch;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.core.exception.MultipleAttributesExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;

/**
 * @author Donald G. Dunne
 */
public interface IAtsWorkDefinitionService {

   WorkDefinitionMatch getWorkDefinitionMatch(String id);

   IAtsWorkDefinition getWorkDef(String id, XResultData resultData) throws Exception;

   IAtsWorkDefinition copyWorkDefinition(String newName, IAtsWorkDefinition workDef, XResultData resultData, IAttributeResolver resolver, IUserResolver iUserResolver);

   boolean isStateWeightingEnabled(IAtsWorkDefinition workDef);

   Collection<String> getStateNames(IAtsWorkDefinition workDef);

   List<IAtsStateDefinition> getStatesOrderedByOrdinal(IAtsWorkDefinition workDef);

   List<IAtsStateDefinition> getStatesOrderedByDefaultToState(IAtsWorkDefinition workDef);

   void getStatesOrderedByDefaultToState(IAtsWorkDefinition workDef, IAtsStateDefinition stateDefinition, List<IAtsStateDefinition> pages);

   /**
    * Recursively decend StateItems and grab all widgetDefs.<br>
    * <br>
    * Note: Modifing this list will not affect the state widgets. Use addStateItem().
    */
   List<IAtsWidgetDefinition> getWidgetsFromLayoutItems(IAtsStateDefinition stateDef);

   boolean hasWidgetNamed(IAtsStateDefinition stateDef, String name);

   IAtsWorkDefinition getWorkDefinition(String workDefinitionDsl) throws Exception;

   String getStorageString(IAtsWorkDefinition workDef, XResultData resultData) throws Exception;

   WorkDefinitionMatch getWorkDefinition(IAtsWorkItem workItem) throws OseeCoreException;

   WorkDefinitionMatch getWorkDefinitionForTask(IAtsTask taskToMove) throws OseeCoreException;

   WorkDefinitionMatch getWorkDefinitionForTaskNotYetCreated(IAtsTeamWorkflow teamWf) throws OseeCoreException;

   IAtsWorkDefinition copyWorkDefinition(String name, IAtsWorkDefinition workDef, XResultData resultData) throws OseeCoreException;

   boolean isTaskOverridingItsWorkDefinition(IAtsTask task) throws MultipleAttributesExist, OseeCoreException;

   void addWorkDefinition(IAtsWorkDefinition workDef);

   void removeWorkDefinition(IAtsWorkDefinition workDef);

   Collection<IAtsWorkDefinition> getLoadedWorkDefinitions();

   void clearCaches();

}
