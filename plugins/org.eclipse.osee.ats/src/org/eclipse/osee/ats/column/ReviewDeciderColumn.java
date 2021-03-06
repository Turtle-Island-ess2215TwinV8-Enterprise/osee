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

import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.core.client.review.DecisionReviewArtifact;
import org.eclipse.osee.ats.core.client.review.DecisionReviewState;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class ReviewDeciderColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static ReviewDeciderColumn instance = new ReviewDeciderColumn();

   public static ReviewDeciderColumn getInstance() {
      return instance;
   }

   private ReviewDeciderColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".reviewDecider", "Review Decider", 100, SWT.LEFT, false,
         SortDataType.String, false, "Review Decider");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public ReviewDeciderColumn copy() {
      ReviewDeciderColumn newXCol = new ReviewDeciderColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof DecisionReviewArtifact) {
            return AtsObjects.toString("; ",
               ((DecisionReviewArtifact) element).getStateMgr().getAssignees(DecisionReviewState.Decision));
         }
      } catch (OseeCoreException ex) {
         LogUtil.getCellExceptionString(ex);
      }
      return "";
   }
}
