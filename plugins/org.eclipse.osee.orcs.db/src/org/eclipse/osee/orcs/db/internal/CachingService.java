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
package org.eclipse.osee.orcs.db.internal;

import java.util.Collection;
import org.eclipse.osee.event.EventService;
import org.eclipse.osee.executor.admin.ExecutorAdmin;
import org.eclipse.osee.framework.core.enums.OseeCacheEnum;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.OseeCachingService;
import org.eclipse.osee.framework.core.model.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.model.cache.AttributeTypeCache;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.IOseeCache;
import org.eclipse.osee.framework.core.model.cache.OseeEnumTypeCache;
import org.eclipse.osee.framework.core.model.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.model.cache.TransactionCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.core.model.type.OseeEnumType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.core.services.IOseeCachingServiceFactory;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryService;
import org.eclipse.osee.framework.core.services.IOseeModelingService;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.db.internal.accessor.ArtifactTypeDataAccessor;
import org.eclipse.osee.orcs.db.internal.accessor.DatabaseBranchAccessor;
import org.eclipse.osee.orcs.db.internal.accessor.DatabaseTransactionRecordAccessor;
import org.eclipse.osee.orcs.db.internal.accessor.TypeLoaderImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * @author Roberto E. Escobar
 */
public class CachingService implements IOseeCachingService, IOseeCachingServiceFactory {

   private IOseeCachingService proxied;

   private IOseeDatabaseService dbService;
   private IResourceManager resourceManager;
   private IOseeModelFactoryService factoryService;
   //   private IOseeModelingService modelingService;
   private IdentityService identityService;
   private Log logger;
   private EventService eventService;
   private ExecutorAdmin executorAdmin;

   public void setDatabaseService(IOseeDatabaseService dbService) {
      this.dbService = dbService;
   }

   public void setResourceManager(IResourceManager resourceManager) {
      this.resourceManager = resourceManager;
   }

   public void setIdentityService(IdentityService identityService) {
      this.identityService = identityService;
   }

   public void setFactoryService(IOseeModelFactoryService factoryService) {
      this.factoryService = factoryService;
   }

   public void setEventService(EventService eventService) {
      this.eventService = eventService;
   }

   public void setExecutorAdmin(ExecutorAdmin executorAdmin) {
      this.executorAdmin = executorAdmin;
   }

   public void setLogger(Log logger) {
      this.logger = logger;
   }

   private Log getLogger() {
      return logger;
   }

   public void start() throws OseeCoreException {
      proxied = createCachingService(true);
      getLogger().info("ORCS Cache - Created");
   }

   public void stop() {
      proxied = null;
      getLogger().info("ORCS Cache - Destroyed");
   }

   @Override
   public BranchCache getBranchCache() {
      return proxied.getBranchCache();
   }

   @Override
   public TransactionCache getTransactionCache() {
      return proxied.getTransactionCache();
   }

   @Override
   public ArtifactTypeCache getArtifactTypeCache() {
      return proxied.getArtifactTypeCache();
   }

   @Override
   public AttributeTypeCache getAttributeTypeCache() {
      return proxied.getAttributeTypeCache();
   }

   @Override
   public RelationTypeCache getRelationTypeCache() {
      return proxied.getRelationTypeCache();
   }

   @Override
   public OseeEnumTypeCache getEnumTypeCache() {
      return proxied.getEnumTypeCache();
   }

   @Override
   public IdentityService getIdentityService() {
      return proxied.getIdentityService();
   }

   @Override
   public Collection<?> getCaches() {
      return proxied.getCaches();
   }

   @Override
   public IOseeCache<?, ?> getCache(OseeCacheEnum cacheId) throws OseeCoreException {
      return proxied.getCache(cacheId);
   }

   @Override
   public void reloadAll() throws OseeCoreException {
      proxied.reloadAll();
   }

   @Override
   public void clearAll() {
      proxied.clearAll();
   }

   private BundleContext getBundleContext() throws OseeCoreException {
      Bundle bundle = FrameworkUtil.getBundle(this.getClass());
      Conditions.checkNotNull(bundle, "bundle");
      return bundle.getBundleContext();
   }

   private <T> T getService(Class<T> clazz) throws OseeCoreException {
      BundleContext context = getBundleContext();
      Conditions.checkNotNull(context, "bundleContext");
      ServiceReference<T> reference = context.getServiceReference(clazz);
      Conditions.checkNotNull(reference, "serviceReference");
      T service = context.getService(reference);
      Conditions.checkNotNull(service, "service");
      return service;
   }

   @Override
   public IOseeCachingService createCachingService(boolean needsPriming) throws OseeCoreException {
      TransactionCache txCache = new TransactionCache();
      BranchCache branchCache =
         new BranchCache(new DatabaseBranchAccessor(logger, executorAdmin, eventService, dbService, txCache,
            factoryService.getBranchFactory()));
      txCache.setAccessor(new DatabaseTransactionRecordAccessor(dbService, branchCache,
         factoryService.getTransactionFactory()));

      IOseeModelingService model = getService(IOseeModelingService.class);
      TypeLoaderImpl loader =
         new TypeLoaderImpl(model, identityService, dbService, resourceManager, branchCache, needsPriming);

      OseeEnumTypeCache oseeEnumTypeCache =
         new OseeEnumTypeCache(new ArtifactTypeDataAccessor<OseeEnumType>(identityService, loader));
      AttributeTypeCache attributeCache =
         new AttributeTypeCache(new ArtifactTypeDataAccessor<AttributeType>(identityService, loader));
      ArtifactTypeCache artifactCache =
         new ArtifactTypeCache(new ArtifactTypeDataAccessor<ArtifactType>(identityService, loader));
      RelationTypeCache relationCache =
         new RelationTypeCache(new ArtifactTypeDataAccessor<RelationType>(identityService, loader));

      return new OseeCachingService(branchCache, txCache, artifactCache, attributeCache, relationCache,
         oseeEnumTypeCache, identityService);
   }
}