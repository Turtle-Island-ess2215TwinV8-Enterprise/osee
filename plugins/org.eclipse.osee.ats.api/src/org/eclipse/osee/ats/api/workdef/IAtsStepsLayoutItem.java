/*
 * Created on Jun 20, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.workdef;

import java.util.List;

/**
 * @author Donald G. Dunne
 */
public interface IAtsStepsLayoutItem extends IAtsLayoutItem {

   public abstract void setName(String string);

   public abstract List<IAtsStepDefinition> getStepDefinitions();

   @Override
   public abstract String toString();

}