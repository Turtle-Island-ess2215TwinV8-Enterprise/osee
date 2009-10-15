/*
 * Created on Jul 14, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.xviewer.column;

import java.sql.SQLException;
import org.eclipse.osee.ats.artifact.ReviewSMArtifact;
import org.eclipse.osee.ats.util.widgets.role.UserRole;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.exception.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerValueColumn;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class XViewerReviewRoleColumn extends XViewerValueColumn {

   private final User user;

   public XViewerReviewRoleColumn(XViewer viewer, int orderNum, User user) {
      super(viewer, "Role", 75, 75, SWT.LEFT, true, SortDataType.String, orderNum);
      this.user = user;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerValueColumn#getColumnText(java.lang.Object, org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn)
    */
   @Override
   public String getColumnText(Object element, XViewerColumn column) throws OseeCoreException, SQLException {
      if (element instanceof ReviewSMArtifact) {
         return getRolesStr((ReviewSMArtifact) element, user);
      }
      return "";
   }

   private static String getRolesStr(ReviewSMArtifact reviewArt, User user) throws OseeCoreException, SQLException {
      String str = "";
      for (UserRole role : reviewArt.getUserRoleManager().getUserRoles()) {
         if (role.getUser().equals(user)) str += role.getRole().name() + ", ";
      }
      return str.replaceFirst(", $", "");
   }

}
