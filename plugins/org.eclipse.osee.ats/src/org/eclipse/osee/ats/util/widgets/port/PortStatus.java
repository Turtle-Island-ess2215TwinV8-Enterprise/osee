/*
 * Created on Aug 31, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.port;

public enum PortStatus {

   ERROR_NO_COMMIT_TRANSACTION_FOUND("No Commit Transaction Found"),
   ERROR_PORT_BRANCH_EXISTS("Port Branch Exists; Can not port"),
   ERROR_ALREADY_PORTED_TO_TARGET_VERSION("Already Ported to Target Version"),
   ERROR_TARGET_VERSION_NOT_SET("The Target Version must be set"),
   //PORT_FROM_BRANCH_CREATED("Port From Branch Created"),
   NONE(""),
   PORTED("Ported");

   private final String displayName;

   private PortStatus(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return displayName;
   }

   public boolean isError() {
      return this.name().startsWith("ERROR_");
   }

}
