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
package org.eclipse.osee.framework.manager.servlet.internal;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.framework.core.data.OseeServerContext;
import org.eclipse.osee.framework.core.server.IApplicationServerLookup;
import org.eclipse.osee.framework.core.server.IApplicationServerManager;
import org.eclipse.osee.framework.core.server.IAuthenticationManager;
import org.eclipse.osee.framework.core.server.ISessionManager;
import org.eclipse.osee.framework.core.server.OseeHttpServlet;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryService;
import org.eclipse.osee.framework.core.translation.IDataTranslationService;
import org.eclipse.osee.framework.manager.servlet.AdminServlet;
import org.eclipse.osee.framework.manager.servlet.ArtifactFileServlet;
import org.eclipse.osee.framework.manager.servlet.AtsServlet;
import org.eclipse.osee.framework.manager.servlet.BranchExchangeServlet;
import org.eclipse.osee.framework.manager.servlet.BranchManagerServlet;
import org.eclipse.osee.framework.manager.servlet.ConfigurationServlet;
import org.eclipse.osee.framework.manager.servlet.DataServlet;
import org.eclipse.osee.framework.manager.servlet.OseeCacheServlet;
import org.eclipse.osee.framework.manager.servlet.OseeModelServlet;
import org.eclipse.osee.framework.manager.servlet.ResourceManagerServlet;
import org.eclipse.osee.framework.manager.servlet.SearchEngineServlet;
import org.eclipse.osee.framework.manager.servlet.SearchEngineTaggerServlet;
import org.eclipse.osee.framework.manager.servlet.ServerLookupServlet;
import org.eclipse.osee.framework.manager.servlet.SessionClientLoopbackServlet;
import org.eclipse.osee.framework.manager.servlet.SessionManagementServlet;
import org.eclipse.osee.framework.manager.servlet.SystemManagerServlet;
import org.eclipse.osee.framework.manager.servlet.UnsubscribeServlet;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsApi;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

/**
 * @author Roberto E. Escobar
 */
public class ServletRegistrationHandler {

   private HttpService httpService;
   private Log logger;
   private ISessionManager sessionManager;
   private IApplicationServerLookup serverLookup;
   private IApplicationServerManager appServerManager;
   private IDataTranslationService translationService;
   private IOseeCachingService caching;
   private IAuthenticationManager authenticationManager;
   private IOseeModelFactoryService factoryService;
   private IResourceManager resourceManager;
   private OrcsApi orcsApi;

   private final Set<String> contexts = new HashSet<String>();

   public void setSessionManager(ISessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }

   public void setServerLookup(IApplicationServerLookup serverLookup) {
      this.serverLookup = serverLookup;
   }

   public void setAppServerManager(IApplicationServerManager appServerManager) {
      this.appServerManager = appServerManager;
   }

   public void setTranslationService(IDataTranslationService translationService) {
      this.translationService = translationService;
   }

   public void setCaching(IOseeCachingService caching) {
      this.caching = caching;
   }

   public void setAuthenticationManager(IAuthenticationManager authenticationManager) {
      this.authenticationManager = authenticationManager;
   }

   public void setFactoryService(IOseeModelFactoryService factoryService) {
      this.factoryService = factoryService;
   }

   public void setResourceManager(IResourceManager resourceManager) {
      this.resourceManager = resourceManager;
   }

   public void setOrcsApi(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   public void setLogger(Log logger) {
      this.logger = logger;
   }

   public Log getLogger() {
      return logger;
   }

   public void setHttpService(HttpService httpService) {
      this.httpService = httpService;
   }

   public synchronized void start(BundleContext context) {
      ServletUtil.unregister(httpService, appServerManager, contexts);
      registerServices(context);
   }

   public synchronized void stop() {
      ServletUtil.unregister(httpService, appServerManager, contexts);
      contexts.clear();
   }

   private void registerServices(BundleContext context) {
      contexts.clear();
      register(new SystemManagerServlet(logger, sessionManager), OseeServerContext.MANAGER_CONTEXT);
      register(new ResourceManagerServlet(logger, sessionManager, resourceManager), OseeServerContext.RESOURCE_CONTEXT);
      register(new ArtifactFileServlet(logger, resourceManager, caching), OseeServerContext.PROCESS_CONTEXT);
      register(new ArtifactFileServlet(logger, resourceManager, caching), OseeServerContext.ARTIFACT_CONTEXT);
      register(new ArtifactFileServlet(logger, resourceManager, caching), "index");
      register(new BranchExchangeServlet(logger, sessionManager, resourceManager, orcsApi),
         OseeServerContext.BRANCH_EXCHANGE_CONTEXT);
      register(new BranchManagerServlet(logger, sessionManager, translationService, orcsApi),
         OseeServerContext.BRANCH_CONTEXT);
      register(new SearchEngineServlet(logger, sessionManager, translationService, orcsApi),
         OseeServerContext.SEARCH_CONTEXT);
      register(new SearchEngineTaggerServlet(logger, sessionManager, orcsApi), OseeServerContext.SEARCH_TAGGING_CONTEXT);
      register(new ServerLookupServlet(logger, serverLookup, appServerManager), OseeServerContext.LOOKUP_CONTEXT);
      register(new SessionManagementServlet(logger, sessionManager, authenticationManager),
         OseeServerContext.SESSION_CONTEXT);
      register(new SessionClientLoopbackServlet(logger, sessionManager), OseeServerContext.CLIENT_LOOPBACK_CONTEXT);
      register(new OseeCacheServlet(logger, sessionManager, translationService, caching, factoryService),
         OseeServerContext.CACHE_CONTEXT);
      register(new OseeModelServlet(logger, sessionManager, translationService, orcsApi),
         OseeServerContext.OSEE_MODEL_CONTEXT);
      register(new UnsubscribeServlet(logger, context, orcsApi), "osee/unsubscribe");

      register(new AtsServlet(logger, resourceManager, caching), "osee/ats");
      register(new ConfigurationServlet(logger, translationService, orcsApi), OseeServerContext.OSEE_CONFIGURE_CONTEXT);
      register(new DataServlet(logger, resourceManager, caching), "osee/data");
      register(new AdminServlet(logger, context), "osee/console");
   }

   private void register(OseeHttpServlet servlet, String contexts) {
      this.contexts.add(contexts);
      ServletUtil.register(httpService, appServerManager, servlet, contexts);
   }
}
