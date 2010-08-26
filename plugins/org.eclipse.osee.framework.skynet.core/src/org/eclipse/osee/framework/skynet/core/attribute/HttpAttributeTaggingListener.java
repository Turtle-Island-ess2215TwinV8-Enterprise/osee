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
package org.eclipse.osee.framework.skynet.core.attribute;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.client.server.HttpUrlBuilderClient;
import org.eclipse.osee.framework.core.data.OseeServerContext;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.HttpProcessor;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.messaging.event.skynet.event.SkynetAttributeChange;
import org.eclipse.osee.framework.skynet.core.event.ArtifactTransactionModifiedEvent;
import org.eclipse.osee.framework.skynet.core.event.BranchEventType;
import org.eclipse.osee.framework.skynet.core.event.FrameworkTransactionData;
import org.eclipse.osee.framework.skynet.core.event.IArtifactsChangeTypeEventListener;
import org.eclipse.osee.framework.skynet.core.event.IArtifactsPurgedEventListener;
import org.eclipse.osee.framework.skynet.core.event.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.IFrameworkTransactionEventListener;
import org.eclipse.osee.framework.skynet.core.event.ITransactionsDeletedEventListener;
import org.eclipse.osee.framework.skynet.core.event.Sender;
import org.eclipse.osee.framework.skynet.core.event.systems.ArtifactModifiedEvent;
import org.eclipse.osee.framework.skynet.core.event2.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event2.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event2.ITransactionEventListener;
import org.eclipse.osee.framework.skynet.core.event2.TransactionEvent;
import org.eclipse.osee.framework.skynet.core.event2.artifact.IArtifactEventListener;
import org.eclipse.osee.framework.skynet.core.event2.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.utility.DbUtil;
import org.eclipse.osee.framework.skynet.core.utility.LoadedArtifacts;

/**
 * <REM2>
 * 
 * @author Roberto E. Escobar
 */
public class HttpAttributeTaggingListener implements IArtifactEventListener, IFrameworkTransactionEventListener, IBranchEventListener, IArtifactsPurgedEventListener, IArtifactsChangeTypeEventListener, ITransactionEventListener, ITransactionsDeletedEventListener {
   private static final String XML_START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AttributeTag>";
   private static final String XML_FINISH = "</AttributeTag>";
   private static final String PREFIX = "<entry gammaId=\"";
   private static final String POSTFIX = "\"/>\n";
   private final ExecutorService executor;

   public HttpAttributeTaggingListener() {
      this.executor = Executors.newSingleThreadExecutor();
   }

   @Override
   public void handleBranchEventREM1(Sender sender, BranchEventType branchModType, int branchId) {
      // do nothing
   }

   @Override
   public void handleArtifactsPurgedEvent(Sender sender, LoadedArtifacts loadedArtifacts) {
      //         if (sender.isRemote()) {
      //            return;
      //         }
      //         try {
      //            loadedArtifacts.
      //            //TODO: implements
      //            //            Map<String, String> parameters = new HashMap<String, String>();
      //            //            parameters.put("sessionId", ClientSessionManager.getSessionId());
      //            //            parameters.put("queryId", Integer.toString(transactionJoinId));
      //            //            String url =
      //            //                  HttpUrlBuilder.getInstance().getOsgiServletServiceUrl(OseeServerContext.SEARCH_TAGGING_CONTEXT,
      //            //                        parameters);
      //            //            String response = HttpProcessor.delete(new URL(url));
      //
      //         } catch (Exception ex) {
      //            OseeLog.log(Activator.class, Level.WARNING, "Error Deleting Tags during purge.", ex);
      //         }
   }

   @Override
   public void handleArtifactsChangeTypeEvent(Sender sender, int toArtifactTypeId, LoadedArtifacts loadedArtifacts) {
      // Need to fix tags based on this event
   }

   @Override
   public void handleTransactionsDeletedEvent(Sender sender, int[] transactionIds) {
      // Need to fix tags based on this event
   }

   @Override
   public void handleTransactionEvent(Sender sender, TransactionEvent transEvent) {
      // Need to fix tags based on this event
   }

   @Override
   public void handleFrameworkTransactionEvent(Sender sender, FrameworkTransactionData txData) throws OseeCoreException {
      if (sender.isRemote()) {
         return;
      }
      TagService taggingInfo = new TagService();
      for (ArtifactTransactionModifiedEvent event : txData.getXModifiedEvents()) {
         if (event instanceof ArtifactModifiedEvent) {
            for (SkynetAttributeChange change : ((ArtifactModifiedEvent) event).getAttributeChanges()) {
               if (AttributeTypeManager.getType(change.getTypeId()).isTaggable()) {
                  taggingInfo.add(change.getGammaId());
               }
            }
         }
      }
      if (taggingInfo.size() > 0) {
         Future<?> future = executor.submit(taggingInfo);
         if (DbUtil.isDbInit()) {
            try {
               future.get();
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, "Error while waiting for tagger to complete.", ex);
            }
         }
      }
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      return null;
   }

   @Override
   public void handleArtifactEvent(ArtifactEvent artifactEvent, Sender sender) {
      try {
         if (sender.isRemote()) {
            return;
         }
         TagService taggingInfo = new TagService();
         for (ArtifactTransactionModifiedEvent event : artifactEvent.getSkynetTransactionDetails()) {
            if (event instanceof ArtifactModifiedEvent) {
               for (SkynetAttributeChange change : ((ArtifactModifiedEvent) event).getAttributeChanges()) {
                  if (AttributeTypeManager.getType(change.getTypeId()).isTaggable()) {
                     taggingInfo.add(change.getGammaId());
                  }
               }
            }
         }
         if (taggingInfo.size() > 0) {
            Future<?> future = executor.submit(taggingInfo);
            if (DbUtil.isDbInit()) {
               try {
                  future.get();
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, "Error while waiting for tagger to complete.", ex);
               }
            }
         }
      } catch (OseeCoreException ex1) {
         OseeLog.log(Activator.class, Level.SEVERE, ex1);
         return;
      }
   }

   @Override
   public void handleBranchEvent(Sender sender, BranchEvent branchEvent) {
      // Need to fix tags based on this event
   }

   private final static class TagService implements Runnable {
      private final Set<Integer> changedGammas;

      public TagService() {
         this.changedGammas = new HashSet<Integer>();
      }

      public void add(int attributeGammaId) {
         changedGammas.add(attributeGammaId);
      }

      public int size() {
         return changedGammas.size();
      }

      @Override
      public void run() {
         long start = System.currentTimeMillis();
         StringBuffer response = new StringBuffer();
         ByteArrayInputStream inputStream = null;
         try {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("sessionId", ClientSessionManager.getSessionId());
            if (DbUtil.isDbInit()) {
               parameters.put("wait", "true");
            }
            StringBuilder payload = new StringBuilder(XML_START);
            for (int data : changedGammas) {
               payload.append(PREFIX);
               payload.append(data);
               payload.append(POSTFIX);
            }
            payload.append(XML_FINISH);

            inputStream = new ByteArrayInputStream(payload.toString().getBytes("UTF-8"));
            String url =
               HttpUrlBuilderClient.getInstance().getOsgiServletServiceUrl(OseeServerContext.SEARCH_TAGGING_CONTEXT,
                  parameters);
            response.append(HttpProcessor.put(new URL(url), inputStream, "application/xml", "UTF-8"));
            OseeLog.log(Activator.class, Level.FINEST,
               String.format("Transmitted to Tagger in [%d ms]", System.currentTimeMillis() - start));
         } catch (Exception ex) {
            if (response.length() > 0) {
               response.append("\n");
            }
            response.append(ex.getLocalizedMessage());
            OseeLog.log(Activator.class, Level.SEVERE, response.toString(), ex);
         } finally {
            changedGammas.clear();
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (IOException ex) {
                  OseeLog.log(Activator.class, Level.SEVERE, ex);
               }
            }
         }
      }
   }
}