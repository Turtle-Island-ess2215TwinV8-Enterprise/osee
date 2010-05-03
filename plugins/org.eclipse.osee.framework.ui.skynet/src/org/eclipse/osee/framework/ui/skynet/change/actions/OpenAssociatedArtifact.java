package org.eclipse.osee.framework.ui.skynet.change.actions;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.change.ChangeUiData;
import org.eclipse.osee.framework.ui.skynet.change.operations.LoadAssociatedArtifactOperation;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.swt.ImageManager;

public class OpenAssociatedArtifact extends Action {
   private final ChangeUiData changeData;

   public OpenAssociatedArtifact(ChangeUiData changeData) {
      super("Open Associated Artifact", Action.AS_PUSH_BUTTON);
      this.changeData = changeData;
      setToolTipText("Open Associated Artifact");
      setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.EDIT));
   }

   @Override
   public void run() {
      IOperation operation = new LoadAssociatedArtifactOperation(changeData);
      Operations.executeAsJob(operation, false, Job.SHORT, new JobChangeAdapter() {

         @Override
         public void done(IJobChangeEvent event) {
            if (changeData.getAssociatedArtifact() == null) {
               AWorkbench.popup("ERROR", "Cannot access associated artifact.");
            } else {
               RendererManager.openInJob(changeData.getAssociatedArtifact(), PresentationType.GENERALIZED_EDIT);
            }
         }
      });
   }

   public void updateEnablement() {
      Artifact associatedArtifact = changeData.getAssociatedArtifact();
      boolean isEnabled;
      ImageDescriptor descriptor;
      if (associatedArtifact != null && !(associatedArtifact instanceof User)) {
         descriptor = ArtifactImageManager.getImageDescriptor(associatedArtifact);
         isEnabled = true;
      } else {
         descriptor = ImageManager.getImageDescriptor(FrameworkImage.EDIT);
         isEnabled = false;
      }
      setImageDescriptor(descriptor);
      setEnabled(isEnabled);
   }
}