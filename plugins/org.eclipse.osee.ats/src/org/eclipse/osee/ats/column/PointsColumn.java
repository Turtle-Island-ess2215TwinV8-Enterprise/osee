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

import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class PointsColumn extends XViewerAtsAttributeValueColumn {

   public static PointsColumn instance = new PointsColumn();

   public static PointsColumn getInstance() {
      return instance;
   }

   private PointsColumn() {
      super(AtsAttributeTypes.Points, 40, SWT.LEFT, false, SortDataType.Integer, true, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public PointsColumn copy() {
      PointsColumn newXCol = new PointsColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   public static String getPoints(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      return teamArt.getSoleAttributeValue(AtsAttributeTypes.Points, "");
   }

}
