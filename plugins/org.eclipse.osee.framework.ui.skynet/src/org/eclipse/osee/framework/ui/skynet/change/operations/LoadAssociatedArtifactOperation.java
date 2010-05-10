package org.eclipse.osee.framework.ui.skynet.change.operations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.TransactionDelta;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.change.ChangeUiData;

public class LoadAssociatedArtifactOperation extends AbstractOperation {
   private final ChangeUiData changeData;

   public LoadAssociatedArtifactOperation(ChangeUiData changeData) {
      super("Load Associated Artifact", SkynetGuiPlugin.PLUGIN_ID);
      this.changeData = changeData;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      Artifact associatedArtifact = null;
      TransactionDelta txDelta = changeData.getTxDelta();
      if (changeData.getCompareType().areSpecificTxs()) {
         TransactionRecord txRecord = txDelta.getEndTx();
         int commitId = txRecord.getCommit();
         if (commitId != 0) {
            associatedArtifact = ArtifactQuery.getArtifactFromId(commitId, BranchManager.getCommonBranch());
         }
      } else {
         Branch sourceBranch = txDelta.getStartTx().getBranch();
         associatedArtifact = (Artifact) sourceBranch.getAssociatedArtifact().getFullArtifact();
      }
      monitor.worked(calculateWork(0.80));
      changeData.setAssociatedArtifact(associatedArtifact);
      monitor.worked(calculateWork(0.20));
   }
}