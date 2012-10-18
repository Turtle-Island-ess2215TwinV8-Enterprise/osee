/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.OperationLogger;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;

/**
 * @author Ryan D. Brooks
 * @authro David W. Miller
 */
public final class PortListOperation extends AbstractOperation {
   private final List<String> portList;

   public PortListOperation(OperationLogger logger, String portList) {
      this(logger, new ArrayList<String>());
      for (String item : portList.split("[\\s,]+")) {
         this.portList.add(item);
      }
   }

   public PortListOperation(OperationLogger logger, List<String> portItems) {
      super("Port Pair(s)", Activator.PLUGIN_ID, logger);
      this.portList = portItems;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws OseeCoreException {

      if (portList.isEmpty()) {
         throw new OseeArgumentException("Must specify at least one Workflow to port.");
      }
      logf("Porting %d  workflows.", portList.size());

      double pairPercentage = 100 / portList.size();
      for (String portItem : portList) {
         portPair(portItem);
         monitor.worked(calculateWork(pairPercentage));
      }

   }

   /**
    * <ol>
    * <li>create trax shadowed action for destination</li>
    * <li>create working branch from destination workflow</li>
    * <li>check that source workflow is completed</li>
    * <li>relate source workflow to destination workflow</li>
    * <li>create branch from transaction from the source workflow</li>
    * <li>commit into from port branch to destination working branch</li>
    * <li>report conflicts and commit completions if commit completes, delete port branch.</li>
    */
   private void portPair(String portItem) throws OseeCoreException {
      TeamWorkFlowArtifact sourceWorkflow = getWorkflowFromId(portItem);
      doPortWork(sourceWorkflow);
   }

   private TeamWorkFlowArtifact getWorkflowFromId(String HRID) throws OseeCoreException {

      return (TeamWorkFlowArtifact) ArtifactQuery.getArtifactFromId(HRID, CoreBranches.COMMON);

   }

   private TeamWorkFlowArtifact getDestinationFromSourceWorkflow(TeamWorkFlowArtifact source) throws OseeCoreException {
      // TODO make this work for the case where there are multiple versions already ported to
      TeamWorkFlowArtifact destination = (TeamWorkFlowArtifact) source.getRelatedArtifact(AtsRelationTypes.Port_To);
      return destination;
   }

   private void doPortWork(TeamWorkFlowArtifact sourceWorkflow) throws OseeCoreException {
      TeamWorkFlowArtifact destinationWorkflow = getDestinationFromSourceWorkflow(sourceWorkflow);
      if (destinationWorkflow.getWorkingBranchForceCacheUpdate() == null) {
         AtsBranchManagerCore.createWorkingBranch_Create(destinationWorkflow, true);
      }

      Branch destinationBranch = destinationWorkflow.getWorkingBranchForceCacheUpdate();
      Branch portBranch = getPortBranchFromWorkflow(sourceWorkflow, destinationWorkflow);
      if (portBranch == null) {
         logf("Source workflow [%s] not ready for port to Workflow [%s].", sourceWorkflow, destinationWorkflow);
         return;
      }

      try {
         if (portBranch.getBranchState().isCommitted()) {
            logf("Skipping completed workflow [%s].", destinationWorkflow);
         } else {
            ConflictManagerExternal conflictManager = new ConflictManagerExternal(destinationBranch, portBranch);
            BranchManager.commitBranch(null, conflictManager, false, false);
            logf("Commit complete for workflow [%s].", destinationWorkflow);
         }
      } catch (OseeCoreException ex) {
         logf("Resolve conflicts for workflow [%s].", destinationWorkflow);
      }
   }

   private Branch getPortBranchFromWorkflow(TeamWorkFlowArtifact sourceWorkflow, TeamWorkFlowArtifact destinationWorkflow) throws OseeCoreException {

      Collection<Branch> branches =
         BranchManager.getBranchesByName(String.format("Porting [%s] branch", sourceWorkflow.getHumanReadableId()));

      if (branches.isEmpty()) {
         TransactionRecord transRecord = AtsBranchManagerCore.getEarliestTransactionId(sourceWorkflow);
         if (transRecord == null) {
            return null;
         } else {
            return BranchManager.createWorkingBranchFromTx(transRecord,
               String.format("Porting [%s] branch", sourceWorkflow.getHumanReadableId()), null);
         }
      } else {
         return branches.iterator().next();
      }
   }
}