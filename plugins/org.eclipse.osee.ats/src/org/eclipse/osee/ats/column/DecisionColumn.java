/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.artifact.ActionArtifact;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.swt.SWT;

public class DecisionColumn extends XViewerAtsAttributeValueColumn {

   public static DecisionColumn instance = new DecisionColumn();

   public static DecisionColumn getInstance() {
      return instance;
   }

   private DecisionColumn() {
      super(AtsAttributeTypes.Decision, 150, SWT.LEFT, false, SortDataType.String, false, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public DecisionColumn copy() {
      DecisionColumn newXCol = new DecisionColumn();
      copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      if (element instanceof ActionArtifact) {
         return "";
      }
      return super.getColumnText(element, column, columnIndex);
   }

}