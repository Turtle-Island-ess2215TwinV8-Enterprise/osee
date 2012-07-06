/*
 * Created on Jun 25, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.workdef.IAtsCompositeLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionService;
import org.eclipse.osee.ats.api.workdef.IAttributeResolver;
import org.eclipse.osee.ats.api.workdef.IUserResolver;
import org.eclipse.osee.ats.dsl.ModelUtil;
import org.eclipse.osee.ats.dsl.atsDsl.AtsDsl;
import org.eclipse.osee.ats.impl.internal.convert.ConvertAtsDslToWorkDefinition;
import org.eclipse.osee.ats.impl.internal.convert.ConvertWorkDefinitionToAtsDsl;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * Provides new and stored Work Definitions
 * 
 * @author Donald G. Dunne
 */
public class AtsWorkDefinitionServiceImpl implements IAtsWorkDefinitionService {

   @Override
   public IAtsWorkDefinition copyWorkDefinition(String newName, IAtsWorkDefinition workDef, XResultData resultData, IAttributeResolver attrResolver, IUserResolver userResolver) {
      ConvertWorkDefinitionToAtsDsl converter = new ConvertWorkDefinitionToAtsDsl(resultData);
      AtsDsl atsDsl = converter.convert(newName, workDef);

      // Convert back to WorkDefinition
      ConvertAtsDslToWorkDefinition converter2 =
         new ConvertAtsDslToWorkDefinition(newName, atsDsl, resultData, attrResolver, userResolver);
      IAtsWorkDefinition newWorkDef = converter2.convert();
      return newWorkDef;
   }

   @Override
   public String getStorageString(IAtsWorkDefinition workDef, XResultData resultData) throws Exception {
      ConvertWorkDefinitionToAtsDsl converter = new ConvertWorkDefinitionToAtsDsl(resultData);
      AtsDsl atsDsl = converter.convert(workDef.getName(), workDef);
      StringOutputStream writer = new StringOutputStream();
      ModelUtil.saveModel(atsDsl, "ats:/mock" + Lib.getDateTimeString() + ".ats", writer);
      return writer.toString();
   }

   private static class StringOutputStream extends OutputStream {
      private final StringBuilder string = new StringBuilder();

      @Override
      public void write(int b) {
         this.string.append((char) b);
      }

      @Override
      public String toString() {
         return this.string.toString();
      }
   };

   @Override
   public IAtsWorkDefinition getWorkDef(String workDefId, XResultData resultData) throws Exception {
      String workDefStr = AtsWorkDefinitionStore.getService().loadWorkDefinitionString(workDefId);
      AtsDsl atsDsl = ModelUtil.loadModel(workDefId + ".ats", workDefStr);
      ConvertAtsDslToWorkDefinition convert =
         new ConvertAtsDslToWorkDefinition(workDefId, atsDsl, resultData,
            AtsWorkDefinitionStore.getService().getAttributeResolver(),
            AtsWorkDefinitionStore.getService().getUserResolver());
      return convert.convert();
   }

   @Override
   public boolean isStateWeightingEnabled(IAtsWorkDefinition workDef) {
      for (IAtsStateDefinition stateDef : workDef.getStates()) {
         if (stateDef.getStateWeight() != 0) {
            return true;
         }
      }
      return false;
   }

   @Override
   public Collection<String> getStateNames(IAtsWorkDefinition workDef) {
      List<String> names = new ArrayList<String>();
      for (IAtsStateDefinition state : workDef.getStates()) {
         names.add(state.getName());
      }
      return names;
   }

   @Override
   public List<IAtsStateDefinition> getStatesOrderedByOrdinal(IAtsWorkDefinition workDef) {
      List<IAtsStateDefinition> orderedPages = new ArrayList<IAtsStateDefinition>();
      List<IAtsStateDefinition> unOrderedPages = new ArrayList<IAtsStateDefinition>();
      for (int x = 1; x < workDef.getStates().size() + 1; x++) {
         for (IAtsStateDefinition state : workDef.getStates()) {
            if (state.getOrdinal() == x) {
               orderedPages.add(state);
            } else if (state.getOrdinal() == 0 && !unOrderedPages.contains(state)) {
               unOrderedPages.add(state);
            }
         }
      }
      orderedPages.addAll(unOrderedPages);
      return orderedPages;
   }

   @Override
   public List<IAtsStateDefinition> getStatesOrderedByDefaultToState(IAtsWorkDefinition workDef) {
      if (workDef.getStartState() == null) {
         throw new IllegalArgumentException("Can't locate Start State for workflow " + workDef.getName());
      }

      // Get ordered pages starting with start page
      List<IAtsStateDefinition> orderedPages = new ArrayList<IAtsStateDefinition>();
      getStatesOrderedByDefaultToState(workDef, workDef.getStartState(), orderedPages);

      // Move completed to the end if it exists
      IAtsStateDefinition completedPage = null;
      for (IAtsStateDefinition stateDefinition : orderedPages) {
         if (stateDefinition.getStateType().isCompletedState()) {
            completedPage = stateDefinition;
         }
      }
      if (completedPage != null) {
         orderedPages.remove(completedPage);
         orderedPages.add(completedPage);
      }
      return orderedPages;
   }

   @Override
   public void getStatesOrderedByDefaultToState(IAtsWorkDefinition workDef, IAtsStateDefinition stateDefinition, List<IAtsStateDefinition> pages) {
      if (pages.contains(stateDefinition)) {
         return;
      }
      // Add this page first
      pages.add(stateDefinition);
      // Add default page
      IAtsStateDefinition defaultToState = stateDefinition.getDefaultToState();
      if (defaultToState != null && !defaultToState.getName().equals(stateDefinition.getName())) {
         getStatesOrderedByDefaultToState(workDef, stateDefinition.getDefaultToState(), pages);
      }
      // Add remaining pages
      for (IAtsStateDefinition stateDef : stateDefinition.getToStates()) {
         if (!pages.contains(stateDef)) {
            getStatesOrderedByDefaultToState(workDef, stateDef, pages);
         }
      }
   }

   /**
    * Recursively decend StateItems and grab all widgetDefs.<br>
    * <br>
    * Note: Modifing this list will not affect the state widgets. Use addStateItem().
    */
   @Override
   public List<IAtsWidgetDefinition> getWidgetsFromLayoutItems(IAtsStateDefinition stateDef) {
      List<IAtsWidgetDefinition> widgets = new ArrayList<IAtsWidgetDefinition>();
      getWidgets(stateDef, widgets, stateDef.getLayoutItems());
      return widgets;
   }

   private static void getWidgets(IAtsStateDefinition stateDef, List<IAtsWidgetDefinition> widgets, List<IAtsLayoutItem> stateItems) {
      for (IAtsLayoutItem stateItem : stateItems) {
         if (stateItem instanceof IAtsCompositeLayoutItem) {
            getWidgets(stateDef, widgets, ((IAtsCompositeLayoutItem) stateItem).getaLayoutItems());
         } else if (stateItem instanceof IAtsWidgetDefinition) {
            widgets.add((IAtsWidgetDefinition) stateItem);
         }
      }
   }

   @Override
   public boolean hasWidgetNamed(IAtsStateDefinition stateDef, String name) {
      for (IAtsWidgetDefinition widgetDef : getWidgetsFromLayoutItems(stateDef)) {
         if (widgetDef.getName().equals(name)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public IAtsWorkDefinition getWorkDefinition(String workDefinitionDsl) throws Exception {
      AtsDsl atsDsl = ModelUtil.loadModel("model.ats", workDefinitionDsl);
      XResultData result = new XResultData(false);
      ConvertAtsDslToWorkDefinition convert =
         new ConvertAtsDslToWorkDefinition(Strings.unquote(atsDsl.getWorkDef().getName()), atsDsl, result,
            AtsWorkDefinitionStore.getService().getAttributeResolver(),
            AtsWorkDefinitionStore.getService().getUserResolver());
      if (!result.isEmpty()) {
         throw new IllegalStateException(result.toString());
      }
      return convert.convert();
   }
}