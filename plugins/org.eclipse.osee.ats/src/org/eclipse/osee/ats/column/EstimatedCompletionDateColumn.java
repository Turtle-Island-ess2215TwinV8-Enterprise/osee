/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.column;

import java.util.Date;
import java.util.logging.Level;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.DeadlineManager;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public class EstimatedCompletionDateColumn extends XViewerAtsAttributeValueColumn {

   public static EstimatedCompletionDateColumn instance = new EstimatedCompletionDateColumn();

   public static EstimatedCompletionDateColumn getInstance() {
      return instance;
   }

   private EstimatedCompletionDateColumn() {
      super(AtsAttributeTypes.EstimatedCompletionDate,
         WorldXViewerFactory.COLUMN_NAMESPACE + ".estimatedCompletionDate",
         AtsAttributeTypes.EstimatedCompletionDate.getUnqualifiedName(), 80, SWT.LEFT, false, SortDataType.Date, true,
         "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public EstimatedCompletionDateColumn copy() {
      EstimatedCompletionDateColumn newXCol = new EstimatedCompletionDateColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) {
      try {
         if (!AtsUtil.isAtsArtifact(element)) {
            return null;
         }
         if (isWorldViewEcdAlerting(element).isTrue()) {
            return ImageManager.getImage(FrameworkImage.WARNING);
         }
      } catch (Exception ex) {
         // do nothing
      }
      return null;
   }

   public static Result isWorldViewEcdAlerting(Object object) throws OseeCoreException {
      if (Artifacts.isOfType(object, AtsArtifactTypes.Action)) {
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(object)) {
            Result result = isWorldViewEcdAlerting(team);
            if (result.isTrue()) {
               return result;
            }
         }
      } else if (object instanceof AbstractWorkflowArtifact) {
         return DeadlineManager.isEcdDateAlerting((AbstractWorkflowArtifact) object);
      }
      return Result.FalseResult;
   }

   public static Date getDate(Object object) throws OseeCoreException {
      if (Artifacts.isOfType(object, AtsArtifactTypes.Action)) {
         return getDate((ActionManager.getFirstTeam(object)));
      } else if (Artifacts.isOfType(object, AtsArtifactTypes.TeamWorkflow)) {
         Date date =
            ((TeamWorkFlowArtifact) object).getSoleAttributeValue(AtsAttributeTypes.EstimatedCompletionDate, null);
         if (date == null) {
            date = EstimatedReleaseDateColumn.getDateFromWorkflow(object);
         }
         if (date == null) {
            date = EstimatedReleaseDateColumn.getDateFromTargetedVersion(object);
         }
         return date;
      } else if (object instanceof AbstractWorkflowArtifact) {
         AbstractWorkflowArtifact abstractWorkflowArtifact = (AbstractWorkflowArtifact) object;
         Date date = abstractWorkflowArtifact.getSoleAttributeValue(AtsAttributeTypes.EstimatedCompletionDate, null);
         if (date == null) {
            TeamWorkFlowArtifact teamArt = abstractWorkflowArtifact.getParentTeamWorkflow();
            if (teamArt != null) {
               return getDate(teamArt);
            }
         } else {
            return date;
         }
      }
      return null;
   }

   public static String getDateStr(AbstractWorkflowArtifact artifact) throws OseeCoreException {
      return DateUtil.getMMDDYY(getDate(artifact));
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         return DateUtil.getMMDDYY(getDate(element));
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return "";
   }

   @Override
   public Object getBackingData(Object element, XViewerColumn xCol, int columnIndex) throws Exception {
      return getDate(element);
   }

}
