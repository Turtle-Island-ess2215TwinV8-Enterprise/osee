/*
 * Created on Aug 1, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.html;

import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.task.IAtsTask;
import org.eclipse.osee.ats.api.util.AtsLib;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.impl.internal.workitem.AtsWorkItemServiceImpl;
import org.eclipse.osee.ats.impl.internal.workitem.assignee.AtsAssigneeStoreService;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;

public class WorkItemToOverviewHtml {

   private final IAtsWorkItem workItem;
   private final static String normalColor = "#EEEEEE";
   private final static String activeColor = "#9CCCFF";
   public final static String labelFont = "<font color=\"darkcyan\" face=\"Arial\" size=\"-1\">";
   StringBuilder html = new StringBuilder();

   public WorkItemToOverviewHtml(IAtsWorkItem workItem) {
      this.workItem = workItem;
   }

   public String get() throws OseeCoreException {
      startBorderTable(100, false, "");
      addTable(getLabelValue("Title", workItem.getName()));
      html.append(AHTML.multiColumnTable(new String[] {
         AHTML.getLabelStr(labelFont, "State: ") + AtsWorkItemServiceImpl.get().getCurrentStateName(workItem),
         AHTML.getLabelStr(labelFont, "Type: ") + AtsWorkItemServiceImpl.get().getTypeName(workItem),
         AHTML.getLabelStr(labelFont, "Id: ") + workItem.getHumanReadableId()}));
      addTable(getLabelValue("Originator", AtsWorkItemServiceImpl.get().getCreatedBy(workItem).getName()),
         getLabelValue("Creation Date", DateUtil.getMMDDYYHHMM(AtsWorkItemServiceImpl.get().getCreatedDate(workItem))));
      if (AtsWorkItemServiceImpl.get().isOfType(workItem, IAtsTeamWorkflow.class)) {
         addTable(getLabelValue("Team", AtsWorkItemServiceImpl.get().getTeamName(workItem)),
            getLabelValue("Assignees", AtsLib.toString("; ", AtsAssigneeStoreService.get().getAssignees(workItem))));
      } else {
         addTable(getLabelValue("Assignees", AtsLib.toString("; ", AtsAssigneeStoreService.get().getAssignees(workItem))));
      }
      addTable(getLabelValue("Description", workItem.getDescription()));
      if (AtsWorkItemServiceImpl.get().isCancelled(workItem)) {
         addTable(getLabelValue("Cancelled From", AtsWorkItemServiceImpl.get().getCancelledFromState(workItem)));
         addTable(getLabelValue("Cancellation Reason", AtsWorkItemServiceImpl.get().getCancelledReason(workItem)));
      }
      if (AtsWorkItemServiceImpl.get().isOfType(workItem, IAtsTask.class)) {
         IAtsWorkItem parentWorkItem = AtsWorkItemServiceImpl.get().getParentWorkItem(workItem);
         if (parentWorkItem != null) {
            html.append(AHTML.multiColumnTable(new String[] {AHTML.getLabelStr(labelFont, "Parent Workflow: ") + parentWorkItem.getName()}));
            html.append(AHTML.multiColumnTable(new String[] {AHTML.getLabelStr(labelFont, "Parent State: ") + AtsWorkItemServiceImpl.get().getCurrentStateName(
               workItem)}));
         }
         html.append(AHTML.multiColumnTable(new String[] {AHTML.getLabelStr(labelFont, "Task Owner: ") + AtsLib.toString(
            "; ", AtsAssigneeStoreService.get().getAssignees(workItem))}));
      }
      endBorderTable();
      html.append(AHTML.newline());
      html.append("Start OSEE, select the ATS perspective and search by the Id shown.");
      return html.toString();
   }

   public void addTable(String... strs) {
      addTable(strs, 100);
   }

   public void addTable(String[] strs, int width) {
      if (strs.length == 1) {
         this.html.append(AHTML.simpleTable(strs[0]));
      } else {
         this.html.append(AHTML.multiColumnTable(width, strs));
      }
   }

   /**
    * Return label with value converted to show html reserved characters
    */
   private String getLabelValue(String label, String value) {
      String valueStr = AHTML.textToHtml(value);
      return getLabel(label) + valueStr;
   }

   private static String getLabel(String label) {
      return AHTML.getLabelStr(labelFont, label + ": ");
   }

   private void startBorderTable(int width, boolean active, String caption) {
      this.html.append(AHTML.startBorderTable(width, active ? activeColor : normalColor, caption));
   }

   private void endBorderTable() {
      this.html.append(AHTML.endBorderTable());
   }

}
