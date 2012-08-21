/*
 * Created on Aug 17, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.workdef;

import java.util.List;

public interface IAtsStepPageDefinition {

   /**
    * Identification
    */
   public abstract String getName();

   public abstract String getDescription();

   /**
    * Returns fully qualified name of <work definition name>.<this state name>
    */
   public abstract String getFullName();

   /**
    * Layout
    */
   public abstract List<IAtsLayoutItem> getLayoutItems();

   @Override
   public abstract int hashCode();

   @Override
   public abstract boolean equals(Object obj);

   /**
    * Misc
    */
   @Override
   public abstract String toString();

}
