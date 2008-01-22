/*
 * Created on Jan 22, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.util;

/**
 * @author Donald G. Dunne
 */
public abstract class AutoRunTask implements IAutoRunTask {

   private String autoRunUniqueId;

   public AutoRunTask() {
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.util.IAutoRunTask#getName()
    */
   public String getAutoRunUniqueId() {
      if (autoRunUniqueId != null) return autoRunUniqueId;
      return "Un-named";
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.util.IAutoRunTask#setName(java.lang.String)
    */
   public void setAutoRunUniqueId(String name) {
      this.autoRunUniqueId = name;
   }

}
