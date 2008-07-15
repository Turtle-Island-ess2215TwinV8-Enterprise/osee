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
package org.eclipse.osee.ats.world;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.ats.artifact.ATSAttributes;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn.SortDataType;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.column.XViewerAttributeColumn;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public enum AtsXColumn {

   Type_Col("Type", 80, SWT.LEFT, true, SortDataType.String, false),
   State_Col("State", 70, SWT.LEFT, true, SortDataType.String, false),
   Priority_Col("Priority", 20, SWT.CENTER, true, SortDataType.String, false),
   Change_Type_Col("Change Type", 22, SWT.LEFT, true, SortDataType.String, false),
   Assignees_Col("Assignees", 100, SWT.LEFT, true, SortDataType.String, false),
   Title_Col("Title", 200, SWT.LEFT, true, SortDataType.String, false),
   Actionable_Items_Col("Actionable Items", 80, SWT.LEFT, true, SortDataType.String, false, "Actionable Items that are impacted by this change."),
   User_Community_Col("User Community", 60, SWT.LEFT, true, SortDataType.String, false, "Program, Project or Group that caused the creation of this Action."),
   ID_Col("ID", 40, SWT.LEFT, true, SortDataType.String, false, "Human Readable ID"),
   Created_Date_Col("Created Date", 80, SWT.LEFT, true, SortDataType.Date, false),
   Version_Target_Col("Version Target", 40, SWT.LEFT, true, SortDataType.String, false),
   Team_Col("Team", 50, SWT.LEFT, true, SortDataType.String, false, "Team that has been assigned to work this Action."),
   Notes_Col("Notes", 80, SWT.LEFT, true, SortDataType.String, true),
   Deadline_Col("Deadline", 80, SWT.LEFT, true, SortDataType.Date, true, "Date the changes need to be completed by."),

   // Aren't shown by default
   Annual_Cost_Avoidance_Col("Annual Cost Avoidance", 50, SWT.LEFT, false, SortDataType.Float, false, "Hours that would be saved for the first year if this change were completed.\n\n" + "(Weekly Benefit Hours * 52 weeks) - Remaining Hours\n\n" + "If number is high, benefit is great given hours remaining."),
   Description_Col("Description", 150, SWT.LEFT, false, SortDataType.String, true),
   Legacy_PCR_Col(ATSAttributes.LEGACY_PCR_ID_ATTRIBUTE, 40, SWT.LEFT, false, SortDataType.String, false),
   Decision_Col("Decision", 150, SWT.LEFT, false, SortDataType.String, false),
   Resolution_Col("Resolution", 150, SWT.LEFT, false, SortDataType.String, false),
   Estimated_Release_Date_Col(ATSAttributes.ESTIMATED_RELEASE_DATE_ATTRIBUTE.getDisplayName(), 80, SWT.LEFT, false, SortDataType.Date, false, "Date the changes will be made available to the users."),
   Release_Date_Col(ATSAttributes.RELEASE_DATE_ATTRIBUTE.getDisplayName(), 80, SWT.LEFT, false, SortDataType.Date, false, "Date the changes were made available to the users."),
   Work_Package_Col("Work Package", 80, SWT.LEFT, false, SortDataType.String, true),
   Category_Col("Category", 80, SWT.LEFT, false, SortDataType.String, true, "Open field for user to be able to enter text to use for categorizing/sorting."),
   Category2_Col("Category2", 80, SWT.LEFT, false, SortDataType.String, true, "Open field for user to be able to enter text to use for categorizing/sorting."),
   Category3_Col("Category3", 80, SWT.LEFT, false, SortDataType.String, true, "Open field for user to be able to enter text to use for categorizing/sorting."),
   Related_To_State_Col("Related To State", ATSAttributes.RELATED_TO_STATE_ATTRIBUTE.getStoreName(), 80, SWT.LEFT, false, SortDataType.String, true, "State of the parent State Machine that this object is related to."),
   Estimated_Hours_Col("Estimated Hours", 40, SWT.CENTER, false, SortDataType.Float, true, "Hours estimated to implement the changes associated with this Action."),
   Weekly_Benefit_Hrs_Col("Weekly Benefit Hrs", 40, SWT.CENTER, false, SortDataType.Float, false, "Estimated number of hours that will be saved over a single year if this change is completed."),
   Remaining_Hours_Col("Remaining Hours", 40, SWT.CENTER, false, SortDataType.Float, false, "Hours that remain to complete the changes.\n\nEstimated Hours - (Estimated Hours * Percent Complete)."),

   Percent_Complete_State_Col("State Percent Complete", 40, SWT.CENTER, false, SortDataType.Percent, false, "Percent Complete for the changes to the current state.\n\nAmount entered from user."),
   Percent_Complete_State_Task_Col("State Task Percent Complete", 40, SWT.CENTER, false, SortDataType.Percent, false, "Percent Complete for the tasks related to the current state.\n\nCalculation: total percent of all tasks related to state / number of tasks related to state"),
   Percent_Complete_State_Review_Col("State Review Percent Complete", 40, SWT.CENTER, false, SortDataType.Percent, false, "Percent Complete for the reviews related to the current state.\n\nCalculation: total percent of all reviews related to state / number of reviews related to state"),
   Percent_Complete_Total_Col("Total Percent Complete", 40, SWT.CENTER, false, SortDataType.Percent, false, "Percent Complete for the reviews related to the current state."),

   Hours_Spent_State_Col("State Hours Spent", 40, SWT.CENTER, false, SortDataType.Float, false, "Hours spent in performing the changes to the current state."),
   Hours_Spent_State_Task_Col("State Task Hours Spent", 40, SWT.CENTER, false, SortDataType.Float, false, "Hours spent in performing the changes for the tasks related to the current state."),
   Hours_Spent_State_Review_Col("State Review Hours Spent", 40, SWT.CENTER, false, SortDataType.Float, false, "Hours spent in performing the changes for the reveiws related to the current state."),
   Hours_Spent_Total_Col("State Total Hours Spent", 40, SWT.CENTER, false, SortDataType.Percent, false, "Hours spent for all work related to the current state."),

   Total_Hours_Spent_Col("Total Hours Spent", 40, SWT.CENTER, false, SortDataType.Percent, false, "Hours spent for all work related to all states."),

   Originator_Col("Originator", 80, SWT.LEFT, false, SortDataType.String, false),
   Implementor_Col("Implementer", 80, SWT.LEFT, false, SortDataType.String, false, "User assigned to the Implementation of the changes."),
   Review_Author_Col("Review Author", 100, SWT.LEFT, false, SortDataType.String, false, "Review Author(s)"),
   Review_Moderator_Col("Review Moderator", 100, SWT.LEFT, false, SortDataType.String, false, "Review Moderator(s)"),
   Review_Reviewer_Col("Review Reviewer", 100, SWT.LEFT, false, SortDataType.String, false, "Review Reviewer(s)"),
   Review_Decider_Col("Review Decider", 100, SWT.LEFT, false, SortDataType.String, false, "Review Decider"),
   Completed_Date_Col("Completed Date", 80, SWT.CENTER, false, SortDataType.Date, false),
   Cancelled_Date_Col("Cancelled Date", 80, SWT.CENTER, false, SortDataType.Date, false),
   Man_Days_Needed_Col("Man Days Needed", 40, SWT.CENTER, false, SortDataType.Float, false),
   Percent_Rework_Col("Percent Rework", 40, SWT.CENTER, false, SortDataType.Integer, false),
   Branch_Status_Col("Branch Status", 40, SWT.CENTER, false, SortDataType.String, false),
   Number_of_Tasks_Col("Number of Tasks", 40, SWT.CENTER, false, SortDataType.String, false),
   Last_Modified_Col("Last Modified", 40, SWT.CENTER, false, SortDataType.Date, false, "Retrieves timestamp of last database update of this artifact."),
   Last_Statused_Col("Last Statused", 40, SWT.CENTER, false, SortDataType.Date, false, "Retrieves timestamp of status (percent completed or hours spent)."),
   Validation_Required_Col("Validation Required", 80, SWT.LEFT, false, SortDataType.String, false, "If set, Originator will be asked to perform a review to\nensure changes are as expected.");

   private final String name;
   private String storeName;
   private final int width;
   private final int align;
   private final boolean show;
   private final SortDataType sortDataType;
   private final String desc;
   private static Map<String, AtsXColumn> nameToAtsXColumn = new HashMap<String, AtsXColumn>();
   private final boolean multiColumnEditable;

   public static AtsXColumn getAtsXColumn(XViewerColumn xCol) {
      if (nameToAtsXColumn.size() == 0) {
         for (AtsXColumn atsCol : AtsXColumn.values())
            nameToAtsXColumn.put(atsCol.getName(), atsCol);
      }
      return nameToAtsXColumn.get(xCol.getSystemName());
   }

   public XViewerColumn getXViewerColumn(AtsXColumn atsXCol) {
      XViewerColumn xCol =
            new XViewerColumn(atsXCol.name, atsXCol.width, atsXCol.width, atsXCol.align, atsXCol.isShow(),
                  atsXCol.sortDataType, 0);
      if (atsXCol.getDesc() != null)
         xCol.setToolTip(atsXCol.getName() + ":\n" + atsXCol.getDesc());
      else
         xCol.setToolTip(atsXCol.getDesc());
      return xCol;
   }

   public XViewerColumn getXViewerAttributeColumn(boolean show) {
      XViewerColumn xCol = AtsXColumn.getXViewerAttributeColumn(this);
      xCol.setShow(show);
      return xCol;
   }

   public static XViewerColumn getXViewerAttributeColumn(AtsXColumn atsXCol) {
      XViewerAttributeColumn xCol =
            new XViewerAttributeColumn(atsXCol.name,
                  (atsXCol.getStoreName() != null ? atsXCol.getStoreName() : atsXCol.getName()), atsXCol.width,
                  atsXCol.width, atsXCol.align, atsXCol.isShow(), atsXCol.sortDataType, 0);
      if (atsXCol.getDesc() != null)
         xCol.setToolTip(atsXCol.getName() + ":\n" + atsXCol.getDesc());
      else
         xCol.setToolTip(atsXCol.getDesc());
      return xCol;
   }

   private AtsXColumn(String name, int width, int align, boolean show, SortDataType sortDataType, boolean multiColumnEditable) {
      this(name, width, align, show, sortDataType, multiColumnEditable, null);
   }

   private AtsXColumn(ATSAttributes atsAttribute, int width, int align, boolean show, SortDataType sortDataType, boolean multiColumnEditable) {
      this(atsAttribute.getDisplayName(), atsAttribute.getStoreName(), width, align, show, sortDataType,
            multiColumnEditable, atsAttribute.getDescription());
   }

   private AtsXColumn(String name, String storeName, int width, int align, boolean show, SortDataType sortDataType, boolean multiColumnEditable, String desc) {
      this(name, width, align, show, sortDataType, multiColumnEditable);
      this.storeName = storeName;
   }

   private AtsXColumn(String name, int width, int align, boolean show, SortDataType sortDataType, boolean multiColumnEditable, String desc) {
      this.name = name;
      this.width = width;
      this.align = align;
      this.show = show;
      this.sortDataType = sortDataType;
      this.multiColumnEditable = multiColumnEditable;
      this.desc = desc;
   }

   /**
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * @return the align
    */
   public int getAlign() {
      return align;
   }

   /**
    * @return the show
    */
   public boolean isShow() {
      return show;
   }

   /**
    * @return the sortDataType
    */
   public SortDataType getSortDataType() {
      return sortDataType;
   }

   /**
    * @return the width
    */
   public int getWidth() {
      return width;
   }

   /**
    * @return the desc
    */
   public String getDesc() {
      return desc;
   }

   public boolean isMultiColumnEditable() {
      return multiColumnEditable;
   }

   /**
    * @return the storeName
    */
   public String getStoreName() {
      return storeName;
   }

   /**
    * @param storeName the storeName to set
    */
   public void setStoreName(String storeName) {
      this.storeName = storeName;
   }

}
