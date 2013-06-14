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
package org.eclipse.osee.framework.manager.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.osee.framework.core.enums.CacheOperation;
import org.eclipse.osee.framework.core.enums.CoreTranslatorId;
import org.eclipse.osee.framework.core.enums.OseeCacheEnum;
import org.eclipse.osee.framework.core.enums.StorageState;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.message.BranchCacheStoreRequest;
import org.eclipse.osee.framework.core.message.BranchCacheUpdateResponse;
import org.eclipse.osee.framework.core.message.BranchCacheUpdateUtil;
import org.eclipse.osee.framework.core.message.CacheUpdateRequest;
import org.eclipse.osee.framework.core.message.TransactionCacheUpdateResponse;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.TransactionCache;
import org.eclipse.osee.framework.core.server.ISessionManager;
import org.eclipse.osee.framework.core.server.UnsecuredOseeHttpServlet;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryService;
import org.eclipse.osee.framework.core.translation.IDataTranslationService;
import org.eclipse.osee.framework.core.translation.ITranslatorId;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsApi;

/**
 * @author Roberto E. Escobar
 */
public class OseeCacheServlet extends UnsecuredOseeHttpServlet {

   private static final long serialVersionUID = 6693534844874109524L;
   private final IDataTranslationService translationService;
   private final IOseeModelFactoryService factoryService;
   private final ISessionManager sessionManager;
   private final OrcsApi orcsApi;

   public OseeCacheServlet(Log logger, ISessionManager sessionManager, IDataTranslationService translationService, OrcsApi orcsApi, IOseeModelFactoryService factoryService) {
      super(logger);
      this.sessionManager = sessionManager;
      this.translationService = translationService;
      this.factoryService = factoryService;
      this.orcsApi = orcsApi;
   }

   public IDataTranslationService getTranslationService() {
      return translationService;
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
      OseeCacheEnum cacheId = OseeCacheEnum.valueOf(req.getParameter("cacheId"));
      try {
         IDataTranslationService service = getTranslationService();
         Pair<Object, ITranslatorId> pair = createResponse(true, new CacheUpdateRequest(cacheId));
         resp.setStatus(HttpServletResponse.SC_ACCEPTED);
         resp.setContentType("text/xml");
         resp.setCharacterEncoding("UTF-8");
         InputStream inputStream = service.convertToStream(pair.getFirst(), pair.getSecond());
         OutputStream outputStream = resp.getOutputStream();
         Lib.inputStreamToOutputStream(inputStream, outputStream);
      } catch (Exception ex) {
         getLogger().error(ex, "Error acquiring cache [%s]", cacheId);
      }
   }

   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      try {
         String sessionId = req.getParameter("sessionId");
         String clientVersion = ModCompatible.getClientVersion(sessionManager, sessionId);
         boolean isCompatible = ModCompatible.is_0_9_2_Compatible(clientVersion);

         CacheOperation operation = CacheOperation.fromString(req.getParameter("function"));
         switch (operation) {
            case UPDATE:
               sendUpdates(isCompatible, req, resp);
               break;
            case STORE:
               storeUpdates(isCompatible, req, resp);
               break;
            default:
               throw new UnsupportedOperationException();
         }
      } catch (Exception ex) {
         handleError(resp, req.toString(), ex);
      }
   }

   private void handleError(HttpServletResponse resp, String request, Throwable th) throws IOException {
      getLogger().error(th, "Osee Cache request error: [%s]", request);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.setContentType("text/plain");
      resp.getWriter().write(Lib.exceptionToString(th));
      resp.getWriter().flush();
      resp.getWriter().close();
   }

   private void storeUpdates(boolean isCompatible, HttpServletRequest req, HttpServletResponse resp) throws OseeCoreException {
      IDataTranslationService service = getTranslationService();
      TransactionCache txCache = orcsApi.getTxsCache();

      BranchCacheStoreRequest updateRequest = null;
      InputStream inputStream = null;
      try {
         inputStream = req.getInputStream();
         updateRequest = service.convert(inputStream, CoreTranslatorId.BRANCH_CACHE_STORE_REQUEST);
      } catch (IOException ex) {
         OseeExceptions.wrapAndThrow(ex);
      } finally {
         Lib.close(inputStream);
      }
      Collection<Branch> updated =
         new BranchCacheUpdateUtil(factoryService.getBranchFactory(), txCache).updateCache(updateRequest,
            orcsApi.getBranchCache());

      BranchCache cache = orcsApi.getBranchCache();
      if (updateRequest.isServerUpdateMessage()) {
         for (Branch branch : updated) {
            if (branch.isCreated()) {
               branch.setStorageState(StorageState.MODIFIED);
            }
            branch.clearDirty();
            cache.decache(branch);
            if (!branch.isPurged()) {
               cache.cache(branch);
            }
         }
      } else {
         cache.storeItems(updated);
      }
      try {
         resp.setStatus(HttpServletResponse.SC_ACCEPTED);
         resp.setContentType("text/plain");
         resp.setCharacterEncoding("UTF-8");
         resp.getWriter().write("Branch Store Successful");
      } catch (IOException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
   }

   private void sendUpdates(boolean isCompatible, HttpServletRequest req, HttpServletResponse resp) throws OseeCoreException {
      IDataTranslationService service = getTranslationService();
      CacheUpdateRequest updateRequest = null;
      InputStream inputStream = null;
      try {
         inputStream = req.getInputStream();
         updateRequest = service.convert(inputStream, CoreTranslatorId.OSEE_CACHE_UPDATE_REQUEST);
      } catch (IOException ex) {
         OseeExceptions.wrapAndThrow(ex);
      } finally {
         Lib.close(inputStream);
      }

      OutputStream outputStream = null;
      try {
         Pair<Object, ITranslatorId> pair = createResponse(isCompatible, updateRequest);

         resp.setStatus(HttpServletResponse.SC_ACCEPTED);
         resp.setContentType("text/xml");
         resp.setCharacterEncoding("UTF-8");

         ModCompatible.makeSendCompatible(isCompatible, pair.getFirst());

         inputStream = service.convertToStream(pair.getFirst(), pair.getSecond());
         outputStream = resp.getOutputStream();
         Lib.inputStreamToOutputStream(inputStream, outputStream);
      } catch (IOException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
   }

   private Pair<Object, ITranslatorId> createResponse(boolean isCompatible, CacheUpdateRequest updateRequest) throws OseeCoreException {
      Object response = null;
      ITranslatorId transalatorId = null;
      switch (updateRequest.getCacheId()) {
         case BRANCH_CACHE:
            BranchCache branchCache = orcsApi.getBranchCache();
            response = BranchCacheUpdateResponse.fromCache(branchCache, branchCache.getAll());
            transalatorId = CoreTranslatorId.BRANCH_CACHE_UPDATE_RESPONSE;
            break;
         case TRANSACTION_CACHE:
            TransactionCache txCache = orcsApi.getTxsCache();

            Collection<TransactionRecord> record;
            if (updateRequest.getItemsIds().isEmpty()) {
               record = txCache.getAll();
            } else {
               record = new ArrayList<TransactionRecord>();
               for (Integer item : updateRequest.getItemsIds()) {
                  record.add(txCache.getOrLoad(item));
               }
            }
            response =
               TransactionCacheUpdateResponse.fromCache(factoryService.getTransactionFactory(), record,
                  orcsApi.getBranchCache());
            transalatorId = CoreTranslatorId.TX_CACHE_UPDATE_RESPONSE;
            break;
         default:
            throw new OseeArgumentException("Invalid cacheId [%s]", updateRequest.getCacheId());
      }
      return new Pair<Object, ITranslatorId>(response, transalatorId);
   }

}
