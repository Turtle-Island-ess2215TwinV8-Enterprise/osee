/*
 * Created on Aug 31, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.port;

public enum PortAction {

   APPLY_NEXT("Apply"),
   RESOLVE_ERROR("Resolve Error"),
   NONE("");

   private final String displayName;

   private PortAction(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return displayName;
   }

}
