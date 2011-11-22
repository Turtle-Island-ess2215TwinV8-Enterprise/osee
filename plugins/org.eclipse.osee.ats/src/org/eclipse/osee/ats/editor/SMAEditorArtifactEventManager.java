/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.eclipse.osee.ats.core.action.ActionArtifact;
import org.eclipse.osee.ats.core.action.ActionManager;
import org.eclipse.osee.ats.core.review.AbstractReviewArtifact;
import org.eclipse.osee.ats.core.review.ReviewManager;
import org.eclipse.osee.ats.core.task.AbstractTaskableArtifact;
import org.eclipse.osee.ats.core.task.TaskArtifact;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IArtifactEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * Common location for event handling for SMAEditors in order to keep number of registrations and processing to a
 * minimum.
 * 
 * @author Donald G. Dunne
 */
public class SMAEditorArtifactEventManager implements IArtifactEventListener {

   List<ISMAEditorEventHandler> handlers = new ArrayList<ISMAEditorEventHandler>();
   static SMAEditorArtifactEventManager instance = new SMAEditorArtifactEventManager();

   private SMAEditorArtifactEventManager() {
      OseeEventManager.addListener(this);
   }

   public static void add(ISMAEditorEventHandler iWorldEventHandler) {
      instance.handlers.add(iWorldEventHandler);
   }

   public static void remove(ISMAEditorEventHandler iWorldEventHandler) {
      if (instance != null) {
         instance.handlers.remove(iWorldEventHandler);
      }
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      return AtsUtilCore.getAtsObjectEventFilters();
   }

   @Override
   public void handleArtifactEvent(final ArtifactEvent artifactEvent, Sender sender) {
      for (ISMAEditorEventHandler handler : new CopyOnWriteArrayList<ISMAEditorEventHandler>(handlers)) {
         if (handler.isDisposed()) {
            handlers.remove(handler);
         }
      }
      try {
         if (!artifactEvent.isForBranch(AtsUtil.getAtsBranch())) {
            return;
         }
      } catch (OseeCoreException ex) {
         return;
      }
      for (final ISMAEditorEventHandler handler : new CopyOnWriteArrayList<ISMAEditorEventHandler>(handlers)) {
         try {
            safelyProcessHandler(artifactEvent, handler);
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, "Error processing event handler - " + handler, ex);
         }
      }
   }

   private void safelyProcessHandler(final ArtifactEvent artifactEvent, final ISMAEditorEventHandler handler) {
      final AbstractWorkflowArtifact awa = handler.getSMAEditor().getAwa();
      Artifact actionArt = null;
      boolean refreshed = false;
      try {
         actionArt = awa.isOfType(AtsArtifactTypes.TeamWorkflow) ? awa.getParentActionArtifact() : null;
      } catch (OseeCoreException ex) {
         // do nothing
      }
      if (awa.isInTransition()) {
         return;
      }
      if (artifactEvent.isDeletedPurged(awa)) {
         handler.getSMAEditor().closeEditor();
      } else if (artifactEvent.isModifiedReloaded(awa) ||
      //
      artifactEvent.isRelAddedChangedDeleted(awa) ||
      //
      (actionArt != null && artifactEvent.isModifiedReloaded(actionArt)) ||
      //
      (actionArt != null && artifactEvent.isRelAddedChangedDeleted(actionArt))) {
         refreshed = true;
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               handler.getSMAEditor().refreshPages();
            }
         });
      } else if (isReloaded(artifactEvent, awa)) {
         SMAEditor.close(Collections.singleton(awa), false);
         if (!awa.isDeleted()) {
            SMAEditor.editArtifact(awa);
         }
      }
      if (!refreshed && awa.isTeamWorkflow() && ReviewManager.hasReviews((TeamWorkFlowArtifact) awa)) {
         try {
            // If related review has made a change, redraw
            for (AbstractReviewArtifact reviewArt : ReviewManager.getReviews((TeamWorkFlowArtifact) awa)) {
               if (artifactEvent.isHasEvent(reviewArt)) {
                  refreshed = true;
                  Displays.ensureInDisplayThread(new Runnable() {
                     @Override
                     public void run() {
                        handler.getSMAEditor().refreshPages();
                     }
                  });
                  // Only refresh editor for first review that has event
                  break;
               }
            }
         } catch (Exception ex) {
            // do nothing
         }
      }
      if (!refreshed && awa.isTeamWorkflow() && ((TeamWorkFlowArtifact) awa).hasTaskArtifacts()) {
         try {
            // If related review has made a change, redraw
            for (TaskArtifact taskArt : ((TeamWorkFlowArtifact) awa).getTaskArtifactsFromCurrentState()) {
               if (artifactEvent.isHasEvent(taskArt)) {
                  refreshed = true;
                  Displays.ensureInDisplayThread(new Runnable() {
                     @Override
                     public void run() {
                        handler.getSMAEditor().refreshPages();
                     }
                  });
                  // Only refresh editor for first task that has event
                  break;
               }
            }
         } catch (Exception ex) {
            // do nothing
         }
      }
      if (!refreshed) {
         try {
            // Since SMAEditor is refreshed when a sibling workflow is changed, need to refresh this
            // list of actionable items when a sibling changes
            for (TeamWorkFlowArtifact teamWf : ActionManager.getTeams(awa.getParentActionArtifact())) {
               ActionArtifact parentAction = teamWf.getParentActionArtifact();
               if (!awa.equals(teamWf) && (artifactEvent.isHasEvent(teamWf) || (parentAction != null && artifactEvent.isRelAddedChangedDeleted(parentAction)))) {
                  refreshed = true;
                  Displays.ensureInDisplayThread(new Runnable() {
                     @Override
                     public void run() {
                        handler.getSMAEditor().refreshPages();
                     }
                  });
                  // Only need to refresh once
                  return;
               }
            }
         } catch (Exception ex) {
            // do nothing
         }
      }

   }

   private boolean isReloaded(ArtifactEvent artifactEvent, AbstractWorkflowArtifact sma) {
      try {
         if (artifactEvent.isReloaded(sma)) {
            return true;
         }
         if (sma instanceof AbstractTaskableArtifact) {
            for (TaskArtifact taskArt : ((AbstractTaskableArtifact) sma).getTaskArtifacts()) {
               if (artifactEvent.isReloaded(taskArt)) {
                  return true;
               }
            }
         }
         if (sma.isTeamWorkflow()) {
            for (AbstractReviewArtifact reviewArt : ReviewManager.getReviews((TeamWorkFlowArtifact) sma)) {
               if (artifactEvent.isReloaded(reviewArt)) {
                  return true;
               }
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return false;
      }
      return false;
   }

}
