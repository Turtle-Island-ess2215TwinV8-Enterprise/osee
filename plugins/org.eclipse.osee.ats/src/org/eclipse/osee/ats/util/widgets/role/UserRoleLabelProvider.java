/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.util.widgets.role;

import java.util.logging.Level;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.Severity;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectManager;
import org.eclipse.osee.ats.core.client.review.role.UserRole;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.util.widgets.defect.DefectSeverityToImage;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public class UserRoleLabelProvider extends XViewerLabelProvider {
   private final UserRoleXViewer xViewer;

   public UserRoleLabelProvider(UserRoleXViewer xViewer) {
      super(xViewer);
      this.xViewer = xViewer;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn dCol, int columnIndex) {
      UserRole roleItem = (UserRole) element;
      try {
         if (dCol.equals(UserRoleXViewerFactory.User_Col)) {
            return ArtifactImageManager.getImage(AtsClientService.get().getUserAdmin().getOseeUser(roleItem.getUser()));
         } else if (dCol.equals(UserRoleXViewerFactory.Role_Col)) {
            return ImageManager.getImage(AtsImage.ROLE);
         } else if (dCol.equals(UserRoleXViewerFactory.Hours_Spent_Col)) {
            return ImageManager.getImage(FrameworkImage.CLOCK);
         } else if (dCol.equals(UserRoleXViewerFactory.Completed_Col)) {
            return ImageManager.getImage(roleItem.isCompleted() ? PluginUiImage.CHECKBOX_ENABLED : PluginUiImage.CHECKBOX_DISABLED);
         } else if (dCol.equals(UserRoleXViewerFactory.Num_Major_Col)) {
            return DefectSeverityToImage.getImage(Severity.Major);
         } else if (dCol.equals(UserRoleXViewerFactory.Num_Minor_Col)) {
            return DefectSeverityToImage.getImage(Severity.Minor);
         } else if (dCol.equals(UserRoleXViewerFactory.Num_Issues_Col)) {
            return DefectSeverityToImage.getImage(Severity.Issue);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn aCol, int columnIndex) throws OseeCoreException {

      UserRole defectItem = (UserRole) element;
      if (aCol.equals(UserRoleXViewerFactory.User_Col)) {
         return defectItem.getUser().getName();
      } else if (aCol.equals(UserRoleXViewerFactory.Hours_Spent_Col)) {
         return defectItem.getHoursSpent() == null ? "" : AtsUtilCore.doubleToI18nString(defectItem.getHoursSpent(),
            false);
      } else if (aCol.equals(UserRoleXViewerFactory.Role_Col)) {
         return defectItem.getRole().name();
      } else if (aCol.equals(UserRoleXViewerFactory.Completed_Col)) {
         return String.valueOf(defectItem.isCompleted());
      } else if (aCol.equals(UserRoleXViewerFactory.Num_Major_Col)) {
         ReviewDefectManager defectMgr = new ReviewDefectManager(xViewer.getXUserRoleViewer().getReviewArt());
         return defectMgr.getNumMajor(defectItem.getUser()) + "";
      } else if (aCol.equals(UserRoleXViewerFactory.Num_Minor_Col)) {
         ReviewDefectManager defectMgr = new ReviewDefectManager(xViewer.getXUserRoleViewer().getReviewArt());
         return defectMgr.getNumMinor(defectItem.getUser()) + "";
      } else if (aCol.equals(UserRoleXViewerFactory.Num_Issues_Col)) {
         ReviewDefectManager defectMgr = new ReviewDefectManager(xViewer.getXUserRoleViewer().getReviewArt());
         return defectMgr.getNumIssues(defectItem.getUser()) + "";
      }
      return "unhandled column";
   }

   @Override
   public void dispose() {
      // do nothing
   }

   @Override
   public boolean isLabelProperty(Object element, String property) {
      return false;
   }

   @Override
   public void addListener(ILabelProviderListener listener) {
      // do nothing
   }

   @Override
   public void removeListener(ILabelProviderListener listener) {
      // do nothing
   }

   public UserRoleXViewer getTreeViewer() {
      return xViewer;
   }

}
