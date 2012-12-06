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

package org.eclipse.osee.ats.impl.internal.html;

import java.util.Arrays;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.task.IAtsTask;
import org.eclipse.osee.ats.api.util.AtsLib;
import org.eclipse.osee.ats.api.workdef.IAtsCompositeLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemStore;
import org.eclipse.osee.ats.api.workflow.notes.IAtsNoteItem;
import org.eclipse.osee.ats.impl.internal.AtsWorkDefinitionServiceImpl;
import org.eclipse.osee.ats.impl.internal.task.AtsTaskServiceImpl;
import org.eclipse.osee.ats.impl.internal.workitem.AtsWorkItemServiceImpl;
import org.eclipse.osee.ats.impl.internal.workitem.assignee.AtsAssigneeServiceImpl;
import org.eclipse.osee.ats.impl.internal.workitem.metrics.AtsMetricsService;
import org.eclipse.osee.ats.impl.internal.workitem.notes.AtsNoteServiceImpl;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class WorkItemToPrintHtml {

   private final IAtsWorkItem workItem;
   boolean includeTaskList = true;
   private final IAtsWorkItemStore workItemStore;

   public WorkItemToPrintHtml(IAtsWorkItem workItem, IAtsWorkItemStore workItemStore) {
      super();
      this.workItem = workItem;
      this.workItemStore = workItemStore;
   }

   public XResultData getResultData() throws OseeCoreException {
      XResultData resultData = new XResultData();
      resultData.addRaw(AHTML.beginMultiColumnTable(100));
      resultData.addRaw(AHTML.addRowMultiColumnTable(new String[] {AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR,
         "Title", workItem.getName())}));
      resultData.addRaw(AHTML.endMultiColumnTable());
      resultData.addRaw(AHTML.beginMultiColumnTable(100));
      resultData.addRaw(AHTML.addRowMultiColumnTable(new String[] {
         //
         AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, "Current State",
            AtsWorkItemServiceImpl.get().getCurrentStateName(workItem)),
         //
         AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, "Team",
            workItemStore.getParentTeamWorkflow(workItem).getName()),
         //
         AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, "Assignees",
            AtsAssigneeServiceImpl.get().getAssigneeStr(workItem)),
         //
         AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, "Originator",
            AtsWorkItemServiceImpl.get().getCreatedBy(workItem).getName()),
         //
         AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, "Created",
            DateUtil.getMMDDYYHHMM(AtsWorkItemServiceImpl.get().getCreatedDate(workItem)))

      }));
      resultData.addRaw(AHTML.endMultiColumnTable());
      resultData.addRaw(AHTML.beginMultiColumnTable(100));
      resultData.addRaw(AHTML.addRowMultiColumnTable(new String[] {
         //
         AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, "Change Type",
            AtsWorkItemServiceImpl.get().getChangeTypeStr(workItem)),
         AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, "Priority",
            AtsWorkItemServiceImpl.get().getPriorityStr(workItem)),
         AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, "Need By", workItemStore.getNeedByDateStr(workItem))}));

      String pcrId = workItemStore.getPcrId(workItem);
      resultData.addRaw(AHTML.addRowMultiColumnTable(new String[] {
         //
         AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, "Workflow", workItemStore.getTypeName(workItem)),
         AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, "HRID", workItem.getHumanReadableId()),
         (pcrId == null ? "" : AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, "Id", pcrId))}));
      resultData.addRaw(AHTML.endMultiColumnTable());
      for (IAtsNoteItem note : AtsNoteServiceImpl.instance.getNoteItems(workItem)) {
         if (note.getState().equals("")) {
            resultData.addRaw(note.toHTML() + AHTML.newline());
         }
      }
      getWorkFlowHtml(resultData);
      if (includeTaskList) {
         getTaskHtml(resultData);
      }
      resultData.addRaw(AHTML.newline());
      // TODO Add log here
      //      resultData.addRaw(workItem.getLog().getHtml());

      return resultData;
   }

   private void getTaskHtml(XResultData rd) throws OseeCoreException {
      if (!(AtsTaskServiceImpl.get().isTaskable(workItem))) {
         return;
      }
      rd.addRaw(AHTML.addSpace(1) + AHTML.getLabelStr(AHTML.LABEL_FONT, "Tasks"));
      rd.addRaw(AHTML.startBorderTable(100, AtsLib.normalColor, ""));
      rd.addRaw(AHTML.addHeaderRowMultiColumnTable(new String[] {
         "Title",
         "State",
         "POC",
         "%",
         "Hrs",
         "Resolution",
         "ID"}));
      for (IAtsTask task : AtsTaskServiceImpl.get().getTasks(workItem)) {
         rd.addRaw(AHTML.addRowMultiColumnTable(new String[] {
            task.getName(),
            AtsWorkItemServiceImpl.get().getCurrentStateName(task).replaceAll("(Task|State)", ""),
            AtsAssigneeServiceImpl.get().getAssigneeStr(task),
            AtsMetricsService.get().getPercentCompleteTotal(task) + "",
            AtsMetricsService.get().getHoursSpentTotal(task) + "",
            AtsWorkItemServiceImpl.get().getResolution(task),
            task.getHumanReadableId()}));
      }
      rd.addRaw(AHTML.endBorderTable());
   }

   private void getWorkFlowHtml(XResultData rd) throws OseeCoreException {

      AtsWorkDefinitionServiceImpl service = new AtsWorkDefinitionServiceImpl();

      // Only display current or past states
      IAtsWorkDefinition workDef = service.getWorkDefinition(workItem).getWorkDefinition();
      for (IAtsStateDefinition stateDef : service.getStatesOrderedByOrdinal(workDef)) {
         boolean inState = AtsWorkItemServiceImpl.get().isInState(workItem, stateDef);
         boolean stateVisited = AtsWorkItemServiceImpl.get().isStateVisited(workItem, stateDef);
         if (inState || stateVisited) {
            // Don't show completed or cancelled state if not currently those state
            if (stateDef.getStateType().isCompletedState() && !AtsWorkItemServiceImpl.get().isCompleted(workItem)) {
               continue;
            }
            if (stateDef.getStateType().isCancelledState() && !AtsWorkItemServiceImpl.get().isCancelled(workItem)) {
               continue;
            }
            StringBuffer notesSb = new StringBuffer();
            for (IAtsNoteItem note : AtsNoteServiceImpl.instance.getNoteItems(workItem)) {
               if (note.getState().equals(stateDef.getName())) {
                  notesSb.append(note.toHTML());
                  notesSb.append(AHTML.newline());
               }
            }
            if (inState || stateVisited) {
               String backgroundColor = inState ? AtsLib.activeColor : AtsLib.normalColor;
               rd.addRaw(AHTML.startBorderTable(100, backgroundColor, stateDef.getName()));
               rd.addRaw(AHTML.beginMultiColumnTable(100, 0));
               for (IAtsLayoutItem item : stateDef.getLayoutItems()) {
                  rd.addRaw(AHTML.addRowMultiColumnTable(addLayoutItemHtml(Arrays.asList(item))));
               }
               rd.addRaw(AHTML.addRowMultiColumnTable(getStateHoursSpentHtml(stateDef)));
               String notesStr = notesSb.toString();
               if (Strings.isValid(notesStr)) {
                  rd.addRaw(AHTML.addRowMultiColumnTable(notesStr));
               }
               String reviewData = getReviewData(workItem, stateDef);
               if (Strings.isValid(reviewData)) {
                  rd.addRaw(AHTML.addRowMultiColumnTable(reviewData));
               }
               rd.addRaw(AHTML.endMultiColumnTable());
               rd.addRaw(AHTML.endBorderTable());
            }
         }
      }
   }

   public String addLayoutItemHtml(List<IAtsLayoutItem> layoutItems) throws OseeCoreException {
      for (IAtsLayoutItem layoutItem : layoutItems) {
         if (layoutItem instanceof IAtsWidgetDefinition) {
            return addLayoutItemForWidget(layoutItem);
         } else if (layoutItem instanceof IAtsCompositeLayoutItem) {
            IAtsCompositeLayoutItem compositeItem = (IAtsCompositeLayoutItem) layoutItem;
            StringBuilder sBuild = new StringBuilder();
            sBuild.append(AHTML.beginMultiColumnTable(100, 0));
            sBuild.append("<tr>");
            for (IAtsLayoutItem lItem : compositeItem.getLayoutItems()) {
               if (lItem instanceof IAtsWidgetDefinition) {
                  sBuild.append("<td>" + addLayoutItemForWidget(lItem) + "</td>");
               } else if (lItem instanceof IAtsCompositeLayoutItem) {
                  sBuild.append("<td>" + addLayoutItemHtml(((IAtsCompositeLayoutItem) lItem).getLayoutItems()) + "</td>");
               }
            }
            sBuild.append("</tr>");
            sBuild.append(AHTML.endMultiColumnTable());
            return sBuild.toString();
         }
      }
      return "";
   }

   public String addLayoutItemForWidget(IAtsLayoutItem layoutItem) throws OseeCoreException {
      IAtsWidgetDefinition widget = (IAtsWidgetDefinition) layoutItem;
      String atrributeName = widget.getAtrributeName();
      String value = workItemStore.getAttributeStringValue(workItem, atrributeName);
      return AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, widget.getName(), value);
   }

   private String getReviewData(IAtsWorkItem workItem, IAtsStateDefinition stateDef) throws OseeCoreException {
      // TODO add review data here
      //      if (AtsWorkItemServiceImpl.instance.isOfType(workItem, IAtsTeamWorkflow.class)) {
      //         return ReviewInfoXWidget.toHTML((TeamWorkFlowArtifact) workItem, stateDef);
      //      }
      return "";
   }

   private String getStateHoursSpentHtml(IAtsStateDefinition statePage) throws OseeCoreException {
      return AHTML.getLabelValueStr(AHTML.LABEL_FONT_WITH_COLOR, "State Hours Spent",
         AtsLib.doubleToI18nString(AtsMetricsService.get().getHoursSpent(workItem, statePage.getName())) + "<br>");
   }

   public boolean isIncludeTaskList() {
      return includeTaskList;
   }

   public void setIncludeTaskList(boolean includeTaskList) {
      this.includeTaskList = includeTaskList;
   }

}
