package org.eclipse.osee.ats.util.widgets.port;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactData;
import org.eclipse.osee.framework.ui.skynet.artifact.ArtifactTransfer;
import org.eclipse.osee.framework.ui.skynet.util.SkynetDragAndDrop;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;

public class PortDragAndDrop extends SkynetDragAndDrop {

   private final XPortTableWidget xPortTableWidget;

   public PortDragAndDrop(XPortTableWidget xPortTableWidget) {
      super(null, xPortTableWidget.getXViewer().getTree(), "viewId");
      this.xPortTableWidget = xPortTableWidget;
   }

   @Override
   public void performDragOver(DropTargetEvent event) {

      ArtifactTransfer artTransfer = ArtifactTransfer.getInstance();
      if (artTransfer.isSupportedType(event.currentDataType)) {

         ArtifactData artData = artTransfer.nativeToJava(event.currentDataType);
         Artifact[] selectedArtifacts = artData.getArtifacts();
         if (testArtifactTypes(selectedArtifacts)) {
            event.detail = DND.DROP_COPY;
         } else {
            event.detail = DND.ERROR_INVALID_DATA;
         }

      }
   }

   @Override
   public Artifact[] getArtifacts() {
      return null;
   }

   @Override
   public void performArtifactDrop(Artifact[] dropArtifacts) {
      addToInput(dropArtifacts);
      xPortTableWidget.notifyXModifiedListeners();
      xPortTableWidget.getXViewer().refresh();
      try {
         List<TeamWorkFlowArtifact> widgetTeamWfs = xPortTableWidget.getWidgetTeamWorkflows();
         xPortTableWidget.getTeamArt().setRelations(AtsRelationTypes.Port_From, widgetTeamWfs);
         xPortTableWidget.getTeamArt().persist("Port Team Workflows Dropped");
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

   }

   private boolean testArtifactTypes(Artifact[] selectedArtifacts) {
      for (Artifact art : selectedArtifacts) {
         if (!art.isOfType(AtsArtifactTypes.TeamWorkflow)) {
            return false;
         }
      }
      // if valid types are not specified, all of the types are valid
      return true;
   }

   /**
    * Adds artifacts to the viewer's input.
    */
   public void addToInput(Artifact... artifacts) {
      List<Object> objects = new ArrayList<Object>();
      for (Artifact artifact : artifacts) {
         if (!objects.contains(artifact)) {
            objects.add(artifact);
         }
      }
      addToInput(objects);
   }

   public void addToInput(final Collection<Object> objects) {
      if (!objects.isEmpty()) {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               if (xPortTableWidget.getXViewer().getInput() == null) {
                  xPortTableWidget.getXViewer().setInput(objects);
               } else {
                  Object input = xPortTableWidget.getXViewer().getInput();
                  Collection colInput = null;
                  if (input instanceof Collection) {
                     colInput = (Collection<TeamWorkFlowArtifact>) xPortTableWidget.getXViewer().getInput();
                  } else {
                     colInput = new ArrayList<TeamWorkFlowArtifact>();
                  }
                  for (Object obj : objects) {
                     if (obj instanceof TeamWorkFlowArtifact && !colInput.contains(obj)) {
                        colInput.add(obj);
                     }
                  }
                  xPortTableWidget.getXViewer().setInput(colInput);
               }
               xPortTableWidget.notifyXModifiedListeners();
               xPortTableWidget.validate();
            }
         });
      }
   }

}
