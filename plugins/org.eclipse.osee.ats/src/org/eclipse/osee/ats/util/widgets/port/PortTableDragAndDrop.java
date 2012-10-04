/*
 * Created on Oct 1, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.port;

import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactData;
import org.eclipse.osee.framework.ui.skynet.artifact.ArtifactTransfer;
import org.eclipse.osee.framework.ui.skynet.util.SkynetDragAndDrop;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Control;

public class PortTableDragAndDrop extends SkynetDragAndDrop {
   private static final Artifact[] EMPTY_ARRAY = new Artifact[0];
   private final PortController portController;

   public PortTableDragAndDrop(Control dragAndDropControl, String viewId, PortController pc) {
      super(dragAndDropControl, viewId);
      portController = pc;
   }

   @Override
   public Artifact[] getArtifacts() throws Exception {
      return null;
   }

   @Override
   public void performDragLeave(DropTargetEvent event) {
      portController.onDragLeave();
   }

   @Override
   public void performDragEnter(DropTargetEvent event) {
      Artifact[] selectedArtifacts = EMPTY_ARRAY;
      ArtifactTransfer artTransfer = ArtifactTransfer.getInstance();
      ArtifactData artData = artTransfer.nativeToJava(event.currentDataType);
      if (artData != null) {
         selectedArtifacts = artData.getArtifacts();
      }
      if (portController.onDragEnter(selectedArtifacts)) {
         event.detail = DND.DROP_COPY;
      } else {
         event.detail = DND.ERROR_INVALID_DATA;
      }
   }

   @Override
   public void performArtifactDrop(Artifact[] dropArtifacts) {
      portController.onArtifactDrop(dropArtifacts);
   }

   @Override
   public void performDragOver(DropTargetEvent event) {
      // not using the drag over for right now
   }

}
