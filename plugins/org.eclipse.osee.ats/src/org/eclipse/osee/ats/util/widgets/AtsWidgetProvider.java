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

package org.eclipse.osee.ats.util.widgets;

import org.eclipse.osee.ats.column.OperationalImpactWithWorkaroundXWidget;
import org.eclipse.osee.ats.column.OperationalImpactXWidget;
import org.eclipse.osee.ats.core.client.review.defect.AtsXDefectValidator;
import org.eclipse.osee.ats.core.client.review.role.AtsXUserRoleValidator;
import org.eclipse.osee.ats.core.client.validator.AtsOperationalImpactValidator;
import org.eclipse.osee.ats.core.client.validator.AtsOperationalImpactWithWorkaroundValidator;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.XVersionList;
import org.eclipse.osee.ats.util.widgets.commit.XCommitManager;
import org.eclipse.osee.ats.util.widgets.defect.XDefectViewer;
import org.eclipse.osee.ats.util.widgets.dialog.AtsObjectMultiChoiceSelect;
import org.eclipse.osee.ats.util.widgets.role.XUserRoleViewer;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.widgets.XHyperlabelGroupSelection;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IXWidgetProvider;
import org.eclipse.osee.framework.ui.skynet.widgets.util.XWidgetRendererItem;

/**
 * @author Donald G. Dunne
 */
public class AtsWidgetProvider implements IXWidgetProvider {

   @Override
   public XWidget createXWidget(String widgetName, String name, XWidgetRendererItem widgetLayoutData) {
      XWidget toReturn = null;
      if (widgetName.equals(XHyperlabelTeamDefinitionSelection.WIDGET_ID)) {
         XHyperlabelTeamDefinitionSelection widget = new XHyperlabelTeamDefinitionSelection(name);
         widget.setToolTip(widgetLayoutData.getToolTip());
         toReturn = widget;
      } else if (widgetName.equals(XHyperlabelActionableItemSelection.WIDGET_ID)) {
         XHyperlabelActionableItemSelection widget = new XHyperlabelActionableItemSelection(name);
         widget.setToolTip(widgetLayoutData.getToolTip());
         toReturn = widget;
      } else if (widgetName.equals(XHyperlabelGroupSelection.WIDGET_ID)) {
         XHyperlabelGroupSelection widget = new XHyperlabelGroupSelection(name);
         widget.setToolTip(widgetLayoutData.getToolTip());
         toReturn = widget;
      } else if (widgetName.equals(AtsObjectMultiChoiceSelect.WIDGET_ID)) {
         toReturn = new AtsObjectMultiChoiceSelect();
      } else if (widgetName.equals(XReviewStateSearchCombo.WIDGET_ID)) {
         toReturn = new XReviewStateSearchCombo();
      } else if (widgetName.equals(XStateCombo.WIDGET_ID)) {
         toReturn = new XStateCombo();
      } else if (widgetName.equals(XStateSearchCombo.WIDGET_ID)) {
         toReturn = new XStateSearchCombo();
      } else if (widgetName.equals(XCommitManager.WIDGET_NAME)) {
         toReturn = new XCommitManager();
      } else if (widgetName.equals(XWorkingBranch.WIDGET_NAME)) {
         toReturn = new XWorkingBranch();
      } else if (widgetName.equals(XWorkingBranchLabel.WIDGET_NAME)) {
         toReturn = new XWorkingBranchLabel();
      } else if (widgetName.equals(XWorkingBranchButtonCreate.WIDGET_NAME)) {
         toReturn = new XWorkingBranchButtonCreate();
      } else if (widgetName.equals(XWorkingBranchButtonArtifactExplorer.WIDGET_NAME)) {
         toReturn = new XWorkingBranchButtonArtifactExplorer();
      } else if (widgetName.equals(XWorkingBranchButtonChangeReport.WIDGET_NAME)) {
         toReturn = new XWorkingBranchButtonChangeReport();
      } else if (widgetName.equals(XWorkingBranchButtonDelete.WIDGET_NAME)) {
         toReturn = new XWorkingBranchButtonDelete();
      } else if (widgetName.equals(XWorkingBranchButtonFavorites.WIDGET_NAME)) {
         toReturn = new XWorkingBranchButtonFavorites();
      } else if (widgetName.equals(XWorkingBranchButtonLock.WIDGET_NAME)) {
         toReturn = new XWorkingBranchButtonLock();
      } else if (widgetName.equals(AtsOperationalImpactValidator.WIDGET_NAME)) {
         toReturn = new OperationalImpactXWidget();
      } else if (widgetName.equals(XTeamDefinitionCombo.WIDGET_ID)) {
         toReturn = new XTeamDefinitionCombo();
      } else if (widgetName.equals(XActionableItemCombo.WIDGET_ID)) {
         toReturn = new XActionableItemCombo();
      } else if (widgetName.equals(AtsOperationalImpactWithWorkaroundValidator.WIDGET_NAME)) {
         toReturn = new OperationalImpactWithWorkaroundXWidget();
      } else if (widgetName.equals(AtsXDefectValidator.WIDGET_NAME)) {
         return new XDefectViewer();
      } else if (widgetName.equals(AtsXUserRoleValidator.WIDGET_NAME)) {
         return new XUserRoleViewer();
      } else if (widgetName.equals("XAtsProgramComboWidget")) {
         try {
            return new XAtsProgramComboWidget();
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      } else if (widgetName.equals(XVersionList.WIDGET_ID)) {
         return new XVersionList();
      }
      return toReturn;
   }
}
