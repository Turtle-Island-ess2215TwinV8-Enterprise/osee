/*
 * Created on Sep 26, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.port;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;

public class PortController {

   private final String errorTip = "Only Workflows are supported for drag and drop here!";
   private final String errorText = "Note:";

   private final PortTableDisplay display;
   private final IBranchEventListener branchListener;
   private TeamWorkFlowArtifact workflow;
   private final Collection<TeamWorkFlowArtifact> sourceWorkflows = new LinkedHashSet<TeamWorkFlowArtifact>();

   public PortController(PortTableDisplay display) {
      this.display = display;
      this.branchListener = new BranchEventListener();
   }

   public TeamWorkFlowArtifact getDestinationWorkflow() {
      return workflow;
   }

   public void bind() {
      OseeEventManager.addListener(branchListener);
      updatePortDataDisplay();
   }

   public void unbind() {
      display.dispose();
      OseeEventManager.removeListener(branchListener);
   }

   public void updatePortArea() {
      TeamWorkFlowArtifact teamArt = getDestinationWorkflow();
      if (teamArt != null) {
         // check to see if the branch is there
         Branch b = null;
         try {
            b = teamArt.getWorkingBranch();
         } catch (OseeCoreException ex) {
            // do nothing
         } finally {
            if (b == null) {
               display.showWarningAreaMessage("Port Manager: Working Branch not created yet");
               display.showWarningArea();
            } else if (teamArt.isWorkingBranchCreationInProgress()) {
               display.showWarningAreaMessage("Port Manager: Branch Creation in Progress");
               display.showWarningArea();
            } else {
               // display.showPortStatusMessage("Port Manager:", "");
               display.showPortData();
            }
         }
      }
   }

   public void setDestinationWorkflow(Artifact artifact) throws OseeCoreException {
      Conditions.checkExpressionFailOnTrue(!artifact.isOfType(AtsArtifactTypes.TeamWorkflow),
         "Must be TeamWorkflowArtifact, set was a [%s]", artifact.getArtifactTypeName());
      this.workflow = TeamWorkFlowManager.cast(artifact);
      load();
   }

   public Collection<TeamWorkFlowArtifact> getSourceWorkflows() {
      return sourceWorkflows;
   }

   public boolean hasSourceWorkflows() {
      return !getSourceWorkflows().isEmpty();
   }

   public void onDragLeave() {
      display.hideErrorToolTip();
   }

   public boolean onDragEnter(Artifact[] selectedArtifacts) {
      boolean droppable = isSupported(selectedArtifacts);
      if (!droppable) {
         display.showErrorToolTip(errorText, errorTip);
      }
      return droppable;
   }

   public void onArtifactDrop(Artifact[] droppedItems) {
      Set<TeamWorkFlowArtifact> items = java.util.Collections.emptySet();
      if (droppedItems != null) {
         items = new LinkedHashSet<TeamWorkFlowArtifact>();
         for (Artifact artifact : droppedItems) {
            if (artifact instanceof TeamWorkFlowArtifact) {
               items.add((TeamWorkFlowArtifact) artifact);
            }
         }
      }
      addSourceWorkflow(items);
   }

   private boolean isSupported(Artifact[] selectedArtifacts) {
      if (selectedArtifacts != null) {
         for (Artifact art : selectedArtifacts) {
            if (!art.isOfType(AtsArtifactTypes.TeamWorkflow)) {
               return false;
            }
         }
      }
      // if valid types are not specified, all of the types are valid
      return true;
   }

   public Result isDirty() {
      try {
         Collection<TeamWorkFlowArtifact> storedTeamWfs = getStoredSourceWorkflows();
         Collection<TeamWorkFlowArtifact> widgetTeamWfs = getSourceWorkflows();
         if (!Collections.isEqual(storedTeamWfs, widgetTeamWfs)) {
            return Result.TrueResult;
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return new Result(false, "Port Widget is Dirty");
   }

   private Collection<TeamWorkFlowArtifact> getStoredSourceWorkflows() throws OseeCoreException {
      return getDestinationWorkflow().getRelatedArtifacts(AtsRelationTypes.Port_From, TeamWorkFlowArtifact.class);
   }

   public IStatus isValid() {
      Status returnStatus = new Status(IStatus.OK, getClass().getSimpleName(), "");
      return returnStatus;
   }

   public void doApplyAll() {
      PortApplyAllOperation operation = new PortApplyAllOperation(getDestinationWorkflow());
      Operations.executeAsJob(operation, true, Job.LONG, new JobChangeAdapter() {

         @Override
         public void done(IJobChangeEvent event) {
            IStatus status = event.getResult();
            if (status.isOK()) {
               display.setRevertAllEnabled(true);
            }
            // TODO: implement the apply all action
         }

      });
      display.setApplyAllEnabled(false);
   }

   public void update() {
      load();
   }

   public void load() {
      try {
         Collection<TeamWorkFlowArtifact> storedWorkflows = getStoredSourceWorkflows();
         Collection<TeamWorkFlowArtifact> sourceWorkflows = getSourceWorkflows();
         sourceWorkflows.addAll(storedWorkflows);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      } finally {
         updatePortDataDisplay();
      }
   }

   public void updatePortDataDisplay() {
      if (hasSourceWorkflows()) {
         display.setPortData(getSourceWorkflows());
      } else {
         display.showPortDataMessage("Drop Team Workflows to Port");
      }
      display.refresh();
   }

   public void removeSourceWorkflow(TeamWorkFlowArtifact artToRemove) {
      try {
         artToRemove.deleteRelation(AtsRelationTypes.Port_To, getDestinationWorkflow());
         artToRemove.persist("Source Work Flow removed from port");
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public void portSourceWorkflow(TeamWorkFlowArtifact artToPort) throws OseeCoreException {
      // TODO: implement the single apply action - 
      // work in progress - do we need to check for some conditions?
      // if the port fails, is this the correct action to take?
      Branch branch = workflow.getWorkingBranch();
      if (!PortUtil.portWorkflowToWorkingBranch(artToPort, branch)) {
         throw new OseeCoreException(String.format("Porting %s failed.", artToPort.getName()), artToPort);
      }
   }

   public void storeSourceWorkflows() throws OseeCoreException {
      TeamWorkFlowArtifact destinationWorkflow = getDestinationWorkflow();
      Collection<TeamWorkFlowArtifact> sourceWorkflows = getSourceWorkflows();
      destinationWorkflow.setRelations(AtsRelationTypes.Port_From, sourceWorkflows);
      destinationWorkflow.persist("Port Team Workflows Stored");
   }

   public void addSourceWorkflow(Collection<TeamWorkFlowArtifact> teamWorkflows) {
      if (!teamWorkflows.isEmpty()) {
         try {
            Collection<TeamWorkFlowArtifact> sourceWorkflows = getSourceWorkflows();
            sourceWorkflows.addAll(teamWorkflows);
            storeSourceWorkflows();
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         } finally {
            updatePortDataDisplay();
         }
      }
   }

   public PortStatus getSourceWorkflowStatus(TeamWorkFlowArtifact source) throws OseeCoreException {
      return PortUtil.getPortStatus(getDestinationWorkflow(), source);
   }

   public PortAction getSourceWorkflowAction(TeamWorkFlowArtifact source) throws OseeCoreException {
      return PortUtil.getPortAction(getDestinationWorkflow(), source);
   }

   private class BranchEventListener implements IBranchEventListener {

      @Override
      public List<? extends IEventFilter> getEventFilters() {
         // PORT_TODO Add a filter
         return null;
      }

      @Override
      public void handleBranchEvent(Sender sender, BranchEvent branchEvent) {
         display.redrawComposite();
      }
   }

   public void doRevertAll() {
      // PORT_TODO Add a filter
      display.setApplyAllEnabled(true);
      // TODO: add code as a job to undo the apply operation
      System.out.println("revert all button pressed");
   }

}
