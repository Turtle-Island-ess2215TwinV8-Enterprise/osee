/*
 * Created on Nov 22, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.x.ats.data;

import java.util.Collection;

/**
 * @author Donald G. Dunne
 */
public abstract class WorkUnitImpl implements WorkUnit, HasWorkDefinition, HasAssignees {

   @Override
   public XXXWorkDefinition getWorkDefinition() {
      return null;
   }

   @Override
   public Collection<LogItem> getStatusHistory() {
      return null;
   }

}
