/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.IAltLeftClickProvider;
import org.eclipse.nebula.widgets.xviewer.IMultiColumnEditProvider;
import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.artifact.ActionManager;
import org.eclipse.osee.ats.artifact.WorkflowManager;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.type.AtsArtifactTypes;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkArtifactImageProvider;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.UserCheckTreeDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class AssigneeColumn extends XViewerAtsColumn implements IXViewerValueColumn, IAltLeftClickProvider, IMultiColumnEditProvider {

   public static AssigneeColumn instance = new AssigneeColumn();

   public static AssigneeColumn getInstance() {
      return instance;
   }

   private AssigneeColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".assignees", "Assignees", 100, SWT.LEFT, true, SortDataType.String,
         true, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public AssigneeColumn copy() {
      AssigneeColumn newXCol = new AssigneeColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      try {
         if (treeItem.getData() instanceof Artifact) {
            Artifact useArt = (Artifact) treeItem.getData();
            if (useArt.isOfType(AtsArtifactTypes.Action)) {
               if (ActionManager.getTeams(useArt).size() == 1) {
                  useArt = ActionManager.getFirstTeam(useArt);
               } else {
                  return false;
               }
            }
            if (!(useArt instanceof AbstractWorkflowArtifact)) {
               return false;
            }
            boolean modified =
               promptChangeAssignees(Arrays.asList((AbstractWorkflowArtifact) useArt), isPersistViewer());
            XViewer xViewer = ((XViewerColumn) treeColumn.getData()).getTreeViewer();
            if (modified && isPersistViewer(xViewer)) {
               useArt.persist("persist assignees via alt-left-click");
            }
            if (modified) {
               xViewer.update(useArt, null);
               return true;
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }

      return false;
   }

   public static boolean promptChangeAssignees(AbstractWorkflowArtifact sma, boolean persist) throws OseeCoreException {
      return promptChangeAssignees(Arrays.asList(sma), persist);
   }

   public static boolean promptChangeAssignees(final Collection<? extends AbstractWorkflowArtifact> awas, boolean persist) throws OseeCoreException {
      for (AbstractWorkflowArtifact awa : awas) {
         if (awa.isCompleted()) {
            AWorkbench.popup("ERROR",
               "Can't assign completed " + awa.getArtifactTypeName() + " (" + awa.getHumanReadableId() + ")");
            return false;
         } else if (awa.isCancelled()) {
            AWorkbench.popup("ERROR",
               "Can't assign cancelled " + awa.getArtifactTypeName() + " (" + awa.getHumanReadableId() + ")");
            return false;
         }
      }
      UserCheckTreeDialog uld = new UserCheckTreeDialog();
      uld.setMessage("Select to assign.\nDeSelect to un-assign.");
      if (awas.iterator().next().getParentTeamWorkflow() != null) {
         uld.setTeamMembers(awas.iterator().next().getParentTeamWorkflow().getTeamDefinition().getMembersAndLeads());
      }

      if (awas.size() == 1) {
         uld.setInitialSelections(awas.iterator().next().getStateMgr().getAssignees());
      }
      if (uld.open() != 0) {
         return false;
      }
      Collection<IBasicUser> users = uld.getUsersSelected();
      if (users.isEmpty()) {
         AWorkbench.popup("ERROR", "Must have at least one assignee");
         return false;
      }
      // As a convenience, remove the UnAssigned user if another user is selected
      if (users.size() > 1) {
         users.remove(UserManager.getUser(SystemUser.UnAssigned));
      }
      for (AbstractWorkflowArtifact awa : awas) {
         awa.getStateMgr().setAssignees(users);
      }
      if (persist) {
         Artifacts.persistInTransaction("Assignee - Prompt Change", awas);
      }
      return true;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof Artifact) {
            return getAssigneeStr((Artifact) element);
         }
      } catch (OseeCoreException ex) {
         return XViewerCells.getCellExceptionString(ex);
      }
      return "";
   }

   @Override
   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      try {
         Set<AbstractWorkflowArtifact> awas = new HashSet<AbstractWorkflowArtifact>();
         for (TreeItem item : treeItems) {
            Artifact art = (Artifact) item.getData();
            if (art instanceof AbstractWorkflowArtifact) {
               awas.add((AbstractWorkflowArtifact) art);
            }
            if (art.isOfType(AtsArtifactTypes.Action) && ActionManager.getTeams(art).size() == 1) {
               awas.add(ActionManager.getFirstTeam(art));
            }
         }
         if (awas.size() == 0) {
            AWorkbench.popup("Invalid selection for setting assignees.");
            return;
         }
         promptChangeAssignees(awas, true);
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) {
      try {
         if (element instanceof Artifact) {
            return AssigneeColumn.getAssigneeImage((Artifact) element);
         }
      } catch (Exception ex) {
         // do nothing
      }
      return null;
   }

   public static Image getAssigneeImage(Artifact artifact) throws OseeCoreException {
      if (artifact.isDeleted()) {
         return null;
      }
      if (artifact instanceof AbstractWorkflowArtifact) {
         return FrameworkArtifactImageProvider.getUserImage(((AbstractWorkflowArtifact) artifact).getStateMgr().getAssignees());
      }
      if (artifact.isOfType(AtsArtifactTypes.Action)) {
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(artifact)) {
            Image image = AssigneeColumn.getAssigneeImage(team);
            if (image != null) {
               return image;
            }
         }
      }
      return null;

   }

   public static String getAssigneeStr(Artifact artifact) throws OseeCoreException {
      if (artifact.isOfType(AtsArtifactTypes.Action)) {
         Set<IBasicUser> pocs = new HashSet<IBasicUser>();
         Set<IBasicUser> implementers = new HashSet<IBasicUser>();
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(artifact)) {
            if (team.isCompletedOrCancelled()) {
               implementers.addAll(team.getImplementers());
            } else {
               pocs.addAll(team.getStateMgr().getAssignees());
            }
         }
         return Artifacts.toString("; ", pocs) + (implementers.isEmpty() ? "" : "(" + Artifacts.toString("; ",
            implementers) + ")");
      } else if (artifact.isOfType(AtsArtifactTypes.AbstractWorkflowArtifact)) {
         AbstractWorkflowArtifact awa = WorkflowManager.cast(artifact);
         if (awa.isCompletedOrCancelled()) {
            if (awa.implementersStr == null && !awa.getImplementers().isEmpty()) {
               awa.implementersStr = "(" + Artifacts.toString("; ", awa.getImplementers()) + ")";
            }
            return awa.implementersStr;
         }
         return Artifacts.toString("; ", awa.getStateMgr().getAssignees());

      }
      return "";
   }

}
