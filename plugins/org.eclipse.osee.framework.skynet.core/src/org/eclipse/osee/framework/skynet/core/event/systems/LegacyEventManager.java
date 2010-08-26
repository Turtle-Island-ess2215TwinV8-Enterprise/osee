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
package org.eclipse.osee.framework.skynet.core.event.systems;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.messaging.event.skynet.ISkynetEvent;
import org.eclipse.osee.framework.messaging.event.skynet.ISkynetEventListener;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkAccessControlArtifactsEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkArtifactAddedEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkArtifactChangeTypeEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkArtifactDeletedEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkArtifactModifiedEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkArtifactPurgeEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkBroadcastEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkCommitBranchEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkDeletedBranchEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkMergeBranchConflictResolvedEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkNewBranchEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkPurgeBranchEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkRelationLinkCreatedEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkRelationLinkDeletedEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkRelationLinkRationalModifiedEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkRenameBranchEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkTransactionDeletedEvent;
import org.eclipse.osee.framework.messaging.event.skynet.event.SkynetArtifactEventBase;
import org.eclipse.osee.framework.messaging.event.skynet.event.SkynetRelationLinkEventBase;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactModType;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.event.ArtifactTransactionModifiedEvent;
import org.eclipse.osee.framework.skynet.core.event.BranchEventType;
import org.eclipse.osee.framework.skynet.core.event.BroadcastEventType;
import org.eclipse.osee.framework.skynet.core.event.EventSystemPreferences;
import org.eclipse.osee.framework.skynet.core.event.EventUtil;
import org.eclipse.osee.framework.skynet.core.event.FrameworkTransactionData;
import org.eclipse.osee.framework.skynet.core.event.IAccessControlEventListener;
import org.eclipse.osee.framework.skynet.core.event.IArtifactModifiedEventListener;
import org.eclipse.osee.framework.skynet.core.event.IArtifactReloadEventListener;
import org.eclipse.osee.framework.skynet.core.event.IArtifactsChangeTypeEventListener;
import org.eclipse.osee.framework.skynet.core.event.IArtifactsPurgedEventListener;
import org.eclipse.osee.framework.skynet.core.event.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.IBroadcastEventListener;
import org.eclipse.osee.framework.skynet.core.event.IEventListener;
import org.eclipse.osee.framework.skynet.core.event.IFrameworkTransactionEventListener;
import org.eclipse.osee.framework.skynet.core.event.IMergeBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.IRelationModifiedEventListener;
import org.eclipse.osee.framework.skynet.core.event.IRemoteEventManagerEventListener;
import org.eclipse.osee.framework.skynet.core.event.ITransactionsDeletedEventListener;
import org.eclipse.osee.framework.skynet.core.event.LoadedRelation;
import org.eclipse.osee.framework.skynet.core.event.MergeBranchEventType;
import org.eclipse.osee.framework.skynet.core.event.RemoteEventServiceEventType;
import org.eclipse.osee.framework.skynet.core.event.Sender;
import org.eclipse.osee.framework.skynet.core.event2.AccessControlEvent;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.relation.RelationEventType;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeManager;
import org.eclipse.osee.framework.skynet.core.utility.LoadedArtifacts;
import org.eclipse.osee.framework.ui.plugin.event.UnloadedRelation;

/**
 * Internal implementation of OSEE Event Manager that should only be accessed from OseeEventManager classes.
 * 
 * @author Donald G. Dunne
 */
public class LegacyEventManager {

   private final Collection<IEventListener> priorityListeners;
   private final Collection<IEventListener> listeners;
   private final ExecutorService executorService;
   private final EventSystemPreferences preferences;
   private final JiniSkynetEventServiceLookup serviceLookup;
   private final ISkynetEventListener remoteEventReceiver;

   public LegacyEventManager(Collection<IEventListener> listeners, Collection<IEventListener> priorityListeners, ExecutorService executorService, EventSystemPreferences preferences, JiniSkynetEventServiceLookup serviceLookup, ISkynetEventListener remoteEventReceiver) {
      this.listeners = listeners;
      this.priorityListeners = priorityListeners;
      this.executorService = executorService;
      this.preferences = preferences;
      this.serviceLookup = serviceLookup;
      this.remoteEventReceiver = remoteEventReceiver;
   }

   public void start() {
      serviceLookup.start();
   }

   public void stop() {
      serviceLookup.stop();
   }

   public boolean isConnected() {
      return preferences.isOldEvents() && serviceLookup.isValid();
   }

   // Kick LOCAL "remote event manager" event
   public void kickRemoteEventManagerEvent(final Sender sender, final RemoteEventServiceEventType remoteEventServiceEventType) {
      if (preferences.isDisableEvents()) {
         return;
      }
      EventUtil.eventLog("IEM1: kickRemoteEventManagerEvent: type: " + remoteEventServiceEventType + " - " + sender);
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            // Kick LOCAL
            try {
               if (sender.isLocal() && remoteEventServiceEventType.isLocalEventType()) {
                  safelyInvokeListeners(IRemoteEventManagerEventListener.class, "handleRemoteEventManagerEvent",
                     sender, remoteEventServiceEventType);
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      };
      execute(runnable);
   }

   /*
    * Kick LOCAL and REMOTE broadcast event
    */
   public void kickBroadcastEvent(final Sender sender, final BroadcastEventType broadcastEventType, final String[] userIds, final String message) {
      if (preferences.isDisableEvents()) {
         return;
      }

      if (!broadcastEventType.isPingOrPong()) {
         EventUtil.eventLog("IEM1: kickBroadcastEvent: type: " + broadcastEventType.name() + " message: " + message + " - " + sender);
      }
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            try {
               // Kick from REMOTE
               if (sender.isRemote() || sender.isLocal() && broadcastEventType.isLocalEventType()) {
                  safelyInvokeListeners(IBroadcastEventListener.class, "handleBroadcastEvent", sender,
                     broadcastEventType, userIds, message);
               }

               // Kick REMOTE (If source was Local and this was not a default branch changed event
               if (sender.isLocal() && broadcastEventType.isRemoteEventType()) {
                  sendRemoteEvent(new NetworkBroadcastEvent(broadcastEventType.name(), message,
                     sender.getNetworkSender()));
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      };
      execute(runnable);
   }

   /*
    * Kick LOCAL and REMOTE branch events
    */
   public void kickBranchEvent(final Sender sender, final BranchEventType branchEventType, final int branchId) {
      if (preferences.isDisableEvents()) {
         return;
      }
      EventUtil.eventLog("IEM1: kickBranchEvent: type: " + branchEventType + " id: " + branchId + " - " + sender);
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            // Log if this is a loopback and what is happening
            if (preferences.isEnableRemoteEventLoopback()) {
               OseeLog.log(
                  LegacyEventManager.class,
                  Level.WARNING,
                  "IEM1: BranchEvent Loopback enabled" + (sender.isLocal() ? " - Ignoring Local Kick" : " - Kicking Local from Loopback"));
            }

            // Kick LOCAL
            if (!preferences.isEnableRemoteEventLoopback() || preferences.isEnableRemoteEventLoopback() && branchEventType.isRemoteEventType() && sender.isRemote()) {
               if (sender.isRemote() || sender.isLocal() && branchEventType.isLocalEventType()) {
                  safelyInvokeListeners(IBranchEventListener.class, "handleBranchEventREM1", sender, branchEventType,
                     branchId);
               }
            }
            // Kick REMOTE (If source was Local and this was not a default branch changed event

            if (sender.isLocal() && branchEventType.isRemoteEventType()) {
               if (branchEventType == BranchEventType.Added) {
                  sendRemoteEvent(new NetworkNewBranchEvent(branchId, sender.getNetworkSender()));
               } else if (branchEventType == BranchEventType.Deleted) {
                  sendRemoteEvent(new NetworkDeletedBranchEvent(branchId, sender.getNetworkSender()));
               } else if (branchEventType == BranchEventType.Purged) {
                  sendRemoteEvent(new NetworkPurgeBranchEvent(branchId, sender.getNetworkSender()));
               } else if (branchEventType == BranchEventType.Committed) {
                  sendRemoteEvent(new NetworkCommitBranchEvent(branchId, sender.getNetworkSender()));
               } else if (branchEventType == BranchEventType.Renamed) {
                  Branch branch = null;
                  try {
                     branch = BranchManager.getBranch(branchId);
                     sendRemoteEvent(new NetworkRenameBranchEvent(branchId, sender.getNetworkSender(),
                        branch.getName(), branch.getShortName()));
                  } catch (OseeCoreException ex) {
                     // do nothing
                  }
               }
            }
         }
      };
      execute(runnable);
   }

   // Kick LOCAL and REMOTE branch events
   public void kickMergeBranchEvent(final Sender sender, final MergeBranchEventType branchEventType, final int branchId) {
      EventUtil.eventLog("IEM1: kickMergeBranchEvent: type: " + branchEventType + " id: " + branchId + " - " + sender);
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            try {
               // Log if this is a loopback and what is happening
               if (preferences.isEnableRemoteEventLoopback()) {
                  OseeLog.log(
                     LegacyEventManager.class,
                     Level.WARNING,
                     "IEM1: MergeBranchEvent Loopback enabled" + (sender.isLocal() ? " - Ignoring Local Kick" : " - Kicking Local from Loopback"));
               }

               // Kick LOCAL
               if (!preferences.isEnableRemoteEventLoopback() || preferences.isEnableRemoteEventLoopback() && branchEventType.isRemoteEventType() && sender.isRemote()) {
                  if (sender.isRemote() || sender.isLocal() && branchEventType.isLocalEventType()) {
                     safelyInvokeListeners(IMergeBranchEventListener.class, "handleMergeBranchEvent", sender,
                        branchEventType, branchId);
                  }
               }
               // Kick REMOTE (If source was Local and this was not a default branch changed event

               if (sender.isLocal() && branchEventType.isRemoteEventType()) {
                  if (branchEventType == MergeBranchEventType.ConflictResolved) {
                     sendRemoteEvent(new NetworkMergeBranchConflictResolvedEvent(branchId, sender.getNetworkSender()));
                  }
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      };
      execute(runnable);
   }

   private void execute(Runnable runnable) {
      executorService.submit(runnable);
   }

   /*
    * Kick LOCAL and REMOTE access control events
    */
   public void kickAccessControlArtifactsEvent(final Sender sender, final AccessControlEvent accessControlEvent, final LoadedArtifacts loadedArtifacts) {
      if (sender == null) {
         throw new IllegalArgumentException("sender can not be null");
      }
      if (accessControlEvent.getEventType() == null) {
         throw new IllegalArgumentException("accessControlEventType can not be null");
      }
      if (loadedArtifacts == null) {
         throw new IllegalArgumentException("loadedArtifacts can not be null");
      }
      if (preferences.isDisableEvents()) {
         return;
      }
      EventUtil.eventLog("IEM1: kickAccessControlEvent - type: " + accessControlEvent.getEventType() + sender + " loadedArtifacts: " + loadedArtifacts);
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            // Kick LOCAL
            if (accessControlEvent.getEventType().isLocalEventType()) {
               safelyInvokeListeners(IAccessControlEventListener.class, "handleAccessControlArtifactsEvent", sender,
                  accessControlEvent);
            }
            // Kick REMOTE (If source was Local and this was not a default branch changed event
            try {
               if (sender.isLocal() && accessControlEvent.getEventType().isRemoteEventType()) {
                  Integer branchId = null;
                  if (loadedArtifacts.getLoadedArtifacts().isEmpty()) {
                     branchId = loadedArtifacts.getLoadedArtifacts().iterator().next().getBranch().getId();
                  }
                  Collection<Integer> artifactIds = loadedArtifacts.getAllArtifactIds();
                  Collection<Integer> artifactTypeIds = loadedArtifacts.getAllArtifactTypeIds();

                  sendRemoteEvent(new NetworkAccessControlArtifactsEvent(accessControlEvent.getEventType().name(),
                     branchId == null ? -1 : branchId, artifactIds, artifactTypeIds, sender.getNetworkSender()));
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      };
      execute(runnable);
   }

   // Kick LOCAL artifact modified event; This event does NOT go external
   public void kickArtifactModifiedEvent(final Sender sender, final ArtifactModType artifactModType, final Artifact artifact) {
      //      OseeEventManager.eventLog("IEM1: kickArtifactModifiedEvent - " + artifactModType + " - " + artifact.getGuid() + " - " + sender + " - " + artifact.getDirtySkynetAttributeChanges());
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            // Kick LOCAL
            safelyInvokeListeners(IArtifactModifiedEventListener.class, "handleArtifactModifiedEvent", sender,
               artifactModType, artifact);
         }
      };
      execute(runnable);
   }

   // Kick LOCAL relation modified event; This event does NOT go external
   public void kickRelationModifiedEvent(final Sender sender, final RelationEventType relationEventType, final RelationLink link, final Branch branch, final String relationType) {
      //      OseeEventManager.eventLog("IEM1: kickRelationModifiedEvent - " + relationEventType + " - " + link + " - " + sender);
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            // Kick LOCAL
            safelyInvokeListeners(IRelationModifiedEventListener.class, "handleRelationModifiedEvent", sender,
               relationEventType, link, branch, relationType);
         }
      };
      execute(runnable);
   }

   // Kick LOCAL and REMOTE purged event depending on sender
   public void kickArtifactsPurgedEvent(final Sender sender, final LoadedArtifacts loadedArtifacts) {
      if (preferences.isDisableEvents()) {
         return;
      }
      EventUtil.eventLog("IEM1: kickArtifactsPurgedEvent " + sender + " - " + loadedArtifacts);
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            // Kick LOCAL
            safelyInvokeListeners(IArtifactsPurgedEventListener.class, "handleArtifactsPurgedEvent", sender,
               loadedArtifacts);
            // Kick REMOTE (If source was Local and this was not a default branch changed event
            try {
               if (sender.isLocal()) {
                  sendRemoteEvent(new NetworkArtifactPurgeEvent(
                     loadedArtifacts.getLoadedArtifacts().iterator().next().getBranch().getId(),
                     loadedArtifacts.getAllArtifactIds(), loadedArtifacts.getAllArtifactTypeIds(),
                     sender.getNetworkSender()));
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      };
      execute(runnable);
   }

   // Kick LOCAL and REMOTE artifact change type depending on sender
   public void kickArtifactsChangeTypeEvent(final Sender sender, final int toArtifactTypeId, final LoadedArtifacts loadedArtifacts) {
      if (preferences.isDisableEvents()) {
         return;
      }
      EventUtil.eventLog("IEM1: kickArtifactsChangeTypeEvent " + sender + " - " + loadedArtifacts);
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            // Kick LOCAL
            safelyInvokeListeners(IArtifactsChangeTypeEventListener.class, "handleArtifactsChangeTypeEvent", sender,
               toArtifactTypeId, loadedArtifacts);
            // Kick REMOTE (If source was Local and this was not a default branch changed event
            try {
               if (sender.isLocal()) {
                  sendRemoteEvent(new NetworkArtifactChangeTypeEvent(
                     loadedArtifacts.getLoadedArtifacts().iterator().next().getBranch().getId(),
                     loadedArtifacts.getAllArtifactIds(), loadedArtifacts.getAllArtifactTypeIds(), toArtifactTypeId,
                     sender.getNetworkSender()));
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      };
      execute(runnable);
   }

   // Kick LOCAL and remote transaction deleted event
   public void kickTransactionsPurgedEvent(final Sender sender, final int[] transactionIds) {
      //TODO This needs to be converted into the individual artifacts and relations that were deleted/modified
      if (preferences.isDisableEvents()) {
         return;
      }
      EventUtil.eventLog("IEM1: kickTransactionsDeletedEvent " + sender + " - " + transactionIds.length);
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            // Kick LOCAL
            safelyInvokeListeners(ITransactionsDeletedEventListener.class, "handleTransactionsDeletedEvent", sender,
               transactionIds);
            // Kick REMOTE (If source was Local and this was not a default branch changed event
            try {
               if (sender.isLocal()) {
                  sendRemoteEvent(new NetworkTransactionDeletedEvent(sender.getNetworkSender(), transactionIds));
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      };
      execute(runnable);
   }

   // Kick LOCAL and REMOTE ArtifactEvent
   public void kickPersistEvent(final Sender sender, Collection<ArtifactTransactionModifiedEvent> xModifiedEvents) {
      if (preferences.isDisableEvents()) {
         return;
      }
      EventUtil.eventLog("IEM1: kickPersistEvent #ModEvents: " + xModifiedEvents.size() + " - " + sender);
      final Collection<ArtifactTransactionModifiedEvent> xModifiedEventsCopy =
         new ArrayList<ArtifactTransactionModifiedEvent>();
      xModifiedEventsCopy.addAll(xModifiedEvents);
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            // Roll-up change information
            FrameworkTransactionData transData = createTransactionDataRollup(xModifiedEventsCopy);
            try {
               // Log if this is a loopback and what is happening
               if (preferences.isEnableRemoteEventLoopback()) {
                  OseeLog.log(
                     LegacyEventManager.class,
                     Level.WARNING,
                     "IEM1: ArtifactEvent Loopback enabled" + (sender.isLocal() ? " - Ignoring Local Kick" : " - Kicking Local from Loopback"));
               }

               // Kick LOCAL
               if (!preferences.isEnableRemoteEventLoopback() || preferences.isEnableRemoteEventLoopback() && sender.isRemote()) {
                  safelyInvokeListeners(IFrameworkTransactionEventListener.class, "handleFrameworkTransactionEvent",
                     sender, transData);
               }

               // Kick REMOTE (If source was Local and this was not a default branch changed event
               if (sender.isLocal()) {
                  List<ISkynetEvent> events = generateNetworkSkynetEvents(sender, xModifiedEventsCopy);
                  sendRemoteEvent(events);
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      };
      execute(runnable);
   }

   // Kick LOCAL ArtifactReloadEvent
   public void kickArtifactReloadEvent(final Sender sender, final Collection<? extends Artifact> artifacts) {
      if (preferences.isDisableEvents()) {
         return;
      }
      EventUtil.eventLog("IEM1: kickArtifactReloadEvent #Reloads: " + artifacts.size() + " - " + sender);
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            try {
               // Log if this is a loopback and what is happening
               if (preferences.isEnableRemoteEventLoopback()) {
                  OseeLog.log(
                     LegacyEventManager.class,
                     Level.WARNING,
                     "IEM1: kickArtifactReloadEvent Loopback enabled" + (sender.isLocal() ? " - Ignoring Local Kick" : " - Kicking Local from Loopback"));
               }

               // Kick LOCAL
               if (!preferences.isEnableRemoteEventLoopback()) {
                  safelyInvokeListeners(IArtifactReloadEventListener.class, "handleReloadEvent", sender, artifacts);
               }
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      };
      execute(runnable);
   }

   private List<ISkynetEvent> generateNetworkSkynetEvents(Sender sender, Collection<ArtifactTransactionModifiedEvent> xModifiedEvents) {
      List<ISkynetEvent> events = new ArrayList<ISkynetEvent>();
      for (ArtifactTransactionModifiedEvent xModifiedEvent : xModifiedEvents) {
         events.add(generateNetworkSkynetEvent(xModifiedEvent, sender));
      }
      return events;
   }

   private ISkynetEvent generateNetworkSkynetEvent(ArtifactTransactionModifiedEvent xModifiedEvent, Sender sender) {
      ISkynetEvent ret = null;
      if (xModifiedEvent instanceof ArtifactModifiedEvent) {
         ret = generateNetworkSkynetArtifactEvent((ArtifactModifiedEvent) xModifiedEvent, sender);
      } else if (xModifiedEvent instanceof RelationModifiedEvent) {
         ret = generateNetworkSkynetRelationEvent((RelationModifiedEvent) xModifiedEvent, sender);
      }
      return ret;
   }

   private ISkynetEvent generateNetworkSkynetArtifactEvent(ArtifactModifiedEvent artEvent, Sender sender) {
      SkynetArtifactEventBase eventBase = getArtifactEventBase(artEvent, sender);
      ISkynetEvent ret;
      if (artEvent.artifactModType == ArtifactModType.Changed) {
         ret = new NetworkArtifactModifiedEvent(eventBase, artEvent.dirtySkynetAttributeChanges);
      } else if (artEvent.artifactModType == ArtifactModType.Added) {
         ret = new NetworkArtifactAddedEvent(eventBase);
      } else if (artEvent.artifactModType == ArtifactModType.Deleted) {
         ret = new NetworkArtifactDeletedEvent(eventBase);
      } else {
         OseeLog.log(LegacyEventManager.class, Level.SEVERE, "Unhandled xArtifactModifiedEvent event: " + artEvent);
         ret = null;
      }
      return ret;
   }

   private SkynetArtifactEventBase getArtifactEventBase(ArtifactModifiedEvent artEvent, Sender sender) {
      Artifact artifact = artEvent.artifact;
      SkynetArtifactEventBase eventBase =
         new SkynetArtifactEventBase(artifact.getBranch().getId(), artEvent.transactionNumber, artifact.getArtId(),
            artifact.getArtTypeId(), artifact.getFactory().getClass().getCanonicalName(),
            artEvent.sender.getNetworkSender());

      return eventBase;
   }

   private static ISkynetEvent generateNetworkSkynetRelationEvent(RelationModifiedEvent relEvent, Sender sender) {
      RelationLink link = relEvent.link;
      SkynetRelationLinkEventBase eventBase = getRelationLinkEventBase(link, sender);
      SkynetRelationLinkEventBase networkEvent;

      String rationale = link.getRationale();
      String descriptorName = link.getRelationType().getName();

      if (relEvent.relationEventType == RelationEventType.ModifiedRationale) {
         networkEvent = new NetworkRelationLinkRationalModifiedEvent(eventBase, rationale);
      } else if (relEvent.relationEventType == RelationEventType.Deleted) {
         networkEvent = new NetworkRelationLinkDeletedEvent(eventBase);
      } else if (relEvent.relationEventType == RelationEventType.Added) {
         networkEvent = new NetworkRelationLinkCreatedEvent(eventBase, rationale, descriptorName);
      } else {
         OseeLog.log(LegacyEventManager.class, Level.SEVERE, "Unhandled xRelationModifiedEvent event: " + relEvent);
         networkEvent = null;
      }
      return networkEvent;
   }

   private static SkynetRelationLinkEventBase getRelationLinkEventBase(RelationLink link, Sender sender) {
      Artifact left = link.getArtifactIfLoaded(RelationSide.SIDE_A);
      Artifact right = link.getArtifactIfLoaded(RelationSide.SIDE_B);
      SkynetRelationLinkEventBase ret = null;
      ret =
         new SkynetRelationLinkEventBase(link.getGammaId(), link.getBranch().getId(), link.getId(),
            link.getAArtifactId(), (left != null ? left.getArtTypeId() : -1), link.getBArtifactId(),
            (right != null ? right.getArtTypeId() : -1), link.getRelationType().getId(), sender.getNetworkSender());

      return ret;
   }

   private static FrameworkTransactionData createTransactionDataRollup(Collection<ArtifactTransactionModifiedEvent> xModifiedEvents) {
      // Roll-up change information
      FrameworkTransactionData transData = new FrameworkTransactionData(xModifiedEvents);

      for (ArtifactTransactionModifiedEvent xModifiedEvent : xModifiedEvents) {
         if (xModifiedEvent instanceof ArtifactModifiedEvent) {
            ArtifactModifiedEvent xArtifactModifiedEvent = (ArtifactModifiedEvent) xModifiedEvent;
            if (xArtifactModifiedEvent.artifactModType == ArtifactModType.Added) {
               if (xArtifactModifiedEvent.artifact != null) {
                  transData.cacheAddedArtifacts.add(xArtifactModifiedEvent.artifact);
                  if (transData.branchId == -1) {
                     transData.branchId = xArtifactModifiedEvent.artifact.getBranch().getId();
                  }
               } else {
                  transData.unloadedAddedArtifacts.add(xArtifactModifiedEvent.unloadedArtifact);
                  if (transData.branchId == -1) {
                     transData.branchId = xArtifactModifiedEvent.unloadedArtifact.getBranchId();
                  }
               }
            }
            if (xArtifactModifiedEvent.artifactModType == ArtifactModType.Deleted) {
               if (xArtifactModifiedEvent.artifact != null) {
                  transData.cacheDeletedArtifacts.add(xArtifactModifiedEvent.artifact);
                  if (transData.branchId == -1) {
                     transData.branchId = xArtifactModifiedEvent.artifact.getBranch().getId();
                  }
               } else {
                  transData.unloadedDeletedArtifacts.add(xArtifactModifiedEvent.unloadedArtifact);
                  if (transData.branchId == -1) {
                     transData.branchId = xArtifactModifiedEvent.unloadedArtifact.getBranchId();
                  }
               }
            }
            if (xArtifactModifiedEvent.artifactModType == ArtifactModType.Changed) {
               if (xArtifactModifiedEvent.artifact != null) {
                  transData.cacheChangedArtifacts.add(xArtifactModifiedEvent.artifact);
                  if (transData.branchId == -1) {
                     transData.branchId = xArtifactModifiedEvent.artifact.getBranch().getId();
                  }
               } else {
                  transData.unloadedChangedArtifacts.add(xArtifactModifiedEvent.unloadedArtifact);
                  if (transData.branchId == -1) {
                     transData.branchId = xArtifactModifiedEvent.unloadedArtifact.getBranchId();
                  }
               }
            }
         }
         if (xModifiedEvent instanceof RelationModifiedEvent) {
            RelationModifiedEvent xRelationModifiedEvent = (RelationModifiedEvent) xModifiedEvent;
            UnloadedRelation unloadedRelation = xRelationModifiedEvent.unloadedRelation;
            LoadedRelation loadedRelation = null;
            // If link is loaded, get information from link
            if (xRelationModifiedEvent.link != null) {
               RelationLink link = xRelationModifiedEvent.link;
               // Get artifact A/B if loaded in artifact cache
               Artifact artA = ArtifactCache.getActive(link.getAArtifactId(), link.getABranch());
               Artifact artB = ArtifactCache.getActive(link.getBArtifactId(), link.getBBranch());
               try {
                  loadedRelation =
                     new LoadedRelation(artA, artB, xRelationModifiedEvent.link.getRelationType(),
                        xRelationModifiedEvent.branch, unloadedRelation);
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, ex);
               }
            }
            // Else, get information from unloadedRelation (if != null)
            else if (unloadedRelation != null) {
               Artifact artA = ArtifactCache.getActive(unloadedRelation.getArtifactAId(), unloadedRelation.getId());
               Artifact artB = ArtifactCache.getActive(unloadedRelation.getArtifactBId(), unloadedRelation.getId());
               if (artA != null || artB != null) {
                  try {
                     loadedRelation =
                        new LoadedRelation(artA, artB, RelationTypeManager.getType(unloadedRelation.getTypeId()),
                           artA != null ? artA.getBranch() : artB.getBranch(), unloadedRelation);
                  } catch (OseeCoreException ex) {
                     OseeLog.log(Activator.class, Level.SEVERE, ex);
                  }
               }
            }
            if (xRelationModifiedEvent.relationEventType == RelationEventType.Added) {
               if (loadedRelation != null) {
                  transData.cacheAddedRelations.add(loadedRelation);
                  if (loadedRelation.getArtifactA() != null) {
                     transData.cacheRelationAddedArtifacts.add(loadedRelation.getArtifactA());
                     if (transData.branchId == -1) {
                        transData.branchId = loadedRelation.getArtifactA().getBranch().getId();
                     }
                  }
                  if (loadedRelation.getArtifactB() != null) {
                     transData.cacheRelationAddedArtifacts.add(loadedRelation.getArtifactB());
                     if (transData.branchId == -1) {
                        transData.branchId = loadedRelation.getArtifactB().getBranch().getId();
                     }
                  }
               }
               if (unloadedRelation != null) {
                  transData.unloadedAddedRelations.add(unloadedRelation);
               }
            }
            if (xRelationModifiedEvent.relationEventType == RelationEventType.Deleted) {
               if (loadedRelation != null) {
                  transData.cacheDeletedRelations.add(loadedRelation);
                  if (loadedRelation.getArtifactA() != null) {
                     transData.cacheRelationDeletedArtifacts.add(loadedRelation.getArtifactA());
                     if (transData.branchId == -1) {
                        transData.branchId = loadedRelation.getArtifactA().getBranch().getId();
                        loadedRelation.getBranch();
                     }
                  }
                  if (loadedRelation.getArtifactB() != null) {
                     transData.cacheRelationDeletedArtifacts.add(loadedRelation.getArtifactB());
                     if (transData.branchId == -1) {
                        transData.branchId = loadedRelation.getArtifactB().getBranch().getId();
                     }
                  }
               }
               if (unloadedRelation != null) {
                  transData.unloadedDeletedRelations.add(unloadedRelation);
                  if (transData.branchId == -1) {
                     transData.branchId = unloadedRelation.getId();
                  }
               }
            }
            if (xRelationModifiedEvent.relationEventType == RelationEventType.ModifiedRationale) {
               if (loadedRelation != null) {
                  transData.cacheChangedRelations.add(loadedRelation);
                  if (loadedRelation.getArtifactA() != null) {
                     transData.cacheRelationChangedArtifacts.add(loadedRelation.getArtifactA());
                     if (transData.branchId == -1) {
                        transData.branchId = loadedRelation.getArtifactA().getBranch().getId();
                     }
                  }
                  if (loadedRelation.getArtifactB() != null) {
                     transData.cacheRelationChangedArtifacts.add(loadedRelation.getArtifactB());
                     if (transData.branchId == -1) {
                        transData.branchId = loadedRelation.getArtifactB().getBranch().getId();
                     }
                  }
               }
               if (unloadedRelation != null) {
                  transData.unloadedChangedRelations.add(unloadedRelation);
                  if (transData.branchId == -1) {
                     transData.branchId = unloadedRelation.getId();
                  }
               }
            }
         }
      }

      // Clean out known duplicates
      transData.cacheChangedArtifacts.removeAll(transData.cacheDeletedArtifacts);
      transData.cacheAddedArtifacts.removeAll(transData.cacheDeletedArtifacts);

      return transData;
   }

   public void safelyInvokeListeners(Class<? extends IEventListener> c, String methodName, Object... args) {
      for (IEventListener listener : priorityListeners) {
         try {
            if (c.isInstance(listener)) {
               for (Method m : c.getMethods()) {
                  if (m.getName().equals(methodName)) {
                     m.invoke(listener, args);
                  }
               }
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      for (IEventListener listener : listeners) {
         try {
            if (c.isInstance(listener)) {
               for (Method m : c.getMethods()) {
                  if (m.getName().equals(methodName)) {
                     m.invoke(listener, args);
                  }
               }
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }

   public void sendRemoteEvent(Collection<ISkynetEvent> events) {
      sendRemoteEvent(events.toArray(new ISkynetEvent[events.size()]));
   }

   private void sendRemoteEvent(final ISkynetEvent... events) {
      if (preferences.isOldEvents() && isConnected()) {
         Job job = new Job("Send Event") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
               serviceLookup.kick(events, serviceLookup.getClientEventListenerRemoteReference());
               return Status.OK_STATUS;
            }
         };

         job.schedule();
      }
      /*
       * This will enable a testing loopback that will take the kicked remote events and loop them back as if they came
       * from an external client. It will allow for the testing of the OEM -> REM -> OEM processing. In addition, this
       * onEvent is put in a non-display thread which will test that all handling by applications is properly handled by
       * doing all processing and then kicking off display-thread when need to update ui. SessionId needs to be modified
       * so this client doesn't think the events came from itself.
       */
      if (preferences.isEnableRemoteEventLoopback()) {
         EventUtil.eventLog("REM: Loopback enabled - Returning events as Remote event.");
         Thread thread = new Thread() {
            @Override
            public void run() {
               try {
                  String newSessionId = GUID.create();
                  for (ISkynetEvent event : events) {
                     event.getNetworkSender().sessionId = newSessionId;
                  }
                  remoteEventReceiver.onEvent(events);
               } catch (RemoteException ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, ex);

               }
            }
         };
         thread.start();
      }
   }

}