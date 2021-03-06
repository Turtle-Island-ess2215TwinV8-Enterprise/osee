/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.query.IAtsQuery;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionService;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.ats.core.client.IAtsClient;
import org.eclipse.osee.ats.core.client.IAtsUserAdmin;
import org.eclipse.osee.ats.core.client.IAtsVersionAdmin;
import org.eclipse.osee.ats.core.client.IAtsWorkDefinitionAdmin;
import org.eclipse.osee.ats.core.client.internal.config.ActionableItemFactory;
import org.eclipse.osee.ats.core.client.internal.config.AtsArtifactConfigCache;
import org.eclipse.osee.ats.core.client.internal.config.AtsConfigCacheProvider;
import org.eclipse.osee.ats.core.client.internal.config.TeamDefinitionFactory;
import org.eclipse.osee.ats.core.client.internal.config.VersionFactory;
import org.eclipse.osee.ats.core.client.internal.query.AtsQuery;
import org.eclipse.osee.ats.core.client.internal.store.ActionableItemArtifactReader;
import org.eclipse.osee.ats.core.client.internal.store.ActionableItemArtifactWriter;
import org.eclipse.osee.ats.core.client.internal.store.AtsArtifactStore;
import org.eclipse.osee.ats.core.client.internal.store.AtsVersionCache;
import org.eclipse.osee.ats.core.client.internal.store.AtsVersionServiceImpl;
import org.eclipse.osee.ats.core.client.internal.store.TeamDefinitionArtifactReader;
import org.eclipse.osee.ats.core.client.internal.store.TeamDefinitionArtifactWriter;
import org.eclipse.osee.ats.core.client.internal.store.VersionArtifactReader;
import org.eclipse.osee.ats.core.client.internal.store.VersionArtifactWriter;
import org.eclipse.osee.ats.core.client.internal.user.AtsUserAdminImpl;
import org.eclipse.osee.ats.core.client.internal.workdef.AtsWorkDefinitionAdminImpl;
import org.eclipse.osee.ats.core.client.internal.workdef.AtsWorkDefinitionCache;
import org.eclipse.osee.ats.core.client.internal.workdef.AtsWorkDefinitionCacheProvider;
import org.eclipse.osee.ats.core.client.team.ITeamWorkflowProviders;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.core.config.IActionableItemFactory;
import org.eclipse.osee.ats.core.config.IAtsConfig;
import org.eclipse.osee.ats.core.config.ITeamDefinitionFactory;
import org.eclipse.osee.ats.core.config.IVersionFactory;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Donald G. Dunne
 */
public class AtsClientImpl implements IAtsClient {

   private IAtsWorkDefinitionService workDefService;
   private IAtsWorkItemService workItemService;
   private IAtsWorkItemArtifactProvider workItemArtifactProvider;

   private final AtsConfigProxy configProxy = new AtsConfigProxy();

   private IAtsVersionAdmin versionService;
   private IAtsArtifactStore artifactStore;
   private CacheProvider<AtsArtifactConfigCache> configCacheProvider;
   private IAtsWorkDefinitionAdmin workDefAdmin;
   private IActionableItemFactory actionableItemFactory;
   private ITeamDefinitionFactory teamDefFactory;
   private IVersionFactory versionFactory;
   private CacheProvider<AtsWorkDefinitionCache> workDefCacheProvider;
   private IAtsUserAdmin atsUserAdmin;

   public void setAtsWorkDefinitionService(IAtsWorkDefinitionService workDefService) {
      this.workDefService = workDefService;
   }

   public void setAtsWorkItemService(IAtsWorkItemService workItemService) {
      this.workItemService = workItemService;
   }

   public void setAtsWorkItemArtifactProvider(IAtsWorkItemArtifactProvider workItemArtifactProvider) {
      this.workItemArtifactProvider = workItemArtifactProvider;
   }

   public void start() {
      Map<Class<? extends IAtsConfigObject>, IAtsArtifactWriter<? extends IAtsConfigObject>> writers =
         new HashMap<Class<? extends IAtsConfigObject>, IAtsArtifactWriter<? extends IAtsConfigObject>>();

      Map<IArtifactType, IAtsArtifactReader<? extends IAtsConfigObject>> readers =
         new HashMap<IArtifactType, IAtsArtifactReader<? extends IAtsConfigObject>>();

      writers.put(IAtsActionableItem.class, new ActionableItemArtifactWriter());
      writers.put(IAtsTeamDefinition.class, new TeamDefinitionArtifactWriter());
      writers.put(IAtsVersion.class, new VersionArtifactWriter());

      artifactStore = new AtsArtifactStore(readers, writers);
      configCacheProvider = new AtsConfigCacheProvider(artifactStore);

      AtsVersionCache versionCache = new AtsVersionCache();
      versionService = new AtsVersionServiceImpl(configCacheProvider, artifactStore, versionCache);

      actionableItemFactory = new ActionableItemFactory();
      teamDefFactory = new TeamDefinitionFactory();
      versionFactory = new VersionFactory(versionService);

      atsUserAdmin = new AtsUserAdminImpl();

      readers.put(AtsArtifactTypes.ActionableItem, new ActionableItemArtifactReader(actionableItemFactory,
         teamDefFactory, versionFactory, atsUserAdmin));
      readers.put(AtsArtifactTypes.TeamDefinition, new TeamDefinitionArtifactReader(actionableItemFactory,
         teamDefFactory, versionFactory, versionService, atsUserAdmin));
      readers.put(AtsArtifactTypes.Version, new VersionArtifactReader(actionableItemFactory, teamDefFactory,
         versionFactory, versionService));

      ITeamWorkflowProviders atsTeamWorkflowProviders = TeamWorkFlowManager.getTeamWorkflowProviders();

      workDefCacheProvider = new AtsWorkDefinitionCacheProvider(workDefService);
      workDefAdmin =
         new AtsWorkDefinitionAdminImpl(workDefCacheProvider, workItemArtifactProvider, workItemService,
            workDefService, atsTeamWorkflowProviders);
   }

   public void stop() {
      if (workDefAdmin != null) {
         workDefAdmin.clearCaches();
      }
      workDefAdmin = null;

      if (configCacheProvider != null) {
         configCacheProvider.invalidate();
         configCacheProvider = null;
      }

      if (workDefCacheProvider != null) {
         workDefCacheProvider.invalidate();
         workDefCacheProvider = null;
      }
      versionService = null;
      artifactStore = null;
      actionableItemFactory = null;
      teamDefFactory = null;
      versionFactory = null;

      atsUserAdmin = null;
   }

   @Override
   public <T extends IAtsConfigObject> Artifact storeConfigObject(T configObject, SkynetTransaction transaction) throws OseeCoreException {
      AtsArtifactConfigCache atsConfigCache = getConfigCache();
      return artifactStore.store(atsConfigCache, configObject, transaction);
   }

   @Override
   public <T extends IAtsConfigObject> T getConfigObject(Artifact artifact) throws OseeCoreException {
      AtsArtifactConfigCache atsConfigCache = getConfigCache();
      return artifactStore.load(atsConfigCache, artifact);
   }

   @Override
   public Artifact getConfigArtifact(IAtsConfigObject atsConfigObject) throws OseeCoreException {
      return getConfigCache().getArtifact(atsConfigObject);
   }

   @Override
   public List<Artifact> getConfigArtifacts(Collection<? extends IAtsObject> atsObjects) throws OseeCoreException {
      return getConfigCache().getArtifacts(atsObjects);
   }

   @Override
   public <T extends IAtsConfigObject> Collection<T> getConfigObjects(Collection<? extends Artifact> artifacts, Class<T> clazz) throws OseeCoreException {
      IAtsConfig config = getAtsConfig();
      List<T> objects = new ArrayList<T>();
      for (Artifact art : artifacts) {
         objects.addAll(config.getByTag(art.getGuid(), clazz));
      }
      return objects;
   }

   @Override
   public void invalidateConfigCache() {
      configCacheProvider.invalidate();
   }

   @Override
   public void reloadConfigCache() throws OseeCoreException {
      configCacheProvider.invalidate();
      configCacheProvider.get();
   }

   @Override
   public void reloadWorkDefinitionCache() throws OseeCoreException {
      workDefCacheProvider.invalidate();
      workDefCacheProvider.get();
   }

   @Override
   public void reloadAllCaches() throws OseeCoreException {
      reloadConfigCache();
      reloadWorkDefinitionCache();
   }

   @Override
   public void invalidateAllCaches() {
      invalidateConfigCache();
      invalidateWorkDefinitionCache();
   }

   @Override
   public void invalidateWorkDefinitionCache() {
      workDefCacheProvider.invalidate();
   }

   @Override
   public IAtsTeamDefinition createTeamDefinition(String guid, String title) throws OseeCoreException {
      IAtsTeamDefinition item = teamDefFactory.createTeamDefinition(guid, title);
      AtsArtifactConfigCache cache = getConfigCache();
      cache.cache(item);
      return item;
   }

   @Override
   public IAtsActionableItem createActionableItem(String guid, String name) throws OseeCoreException {
      IAtsActionableItem item = actionableItemFactory.createActionableItem(guid, name);
      AtsArtifactConfigCache cache = getConfigCache();
      cache.cache(item);
      return item;
   }

   @Override
   public IAtsVersion createVersion(String title, String create, String generate) throws OseeCoreException {
      IAtsVersion item = versionFactory.createVersion(title, create, generate);
      AtsArtifactConfigCache cache = getConfigCache();
      cache.cache(item);
      return item;
   }

   @Override
   public IAtsVersion createVersion(String name) throws OseeCoreException {
      IAtsVersion item = versionFactory.createVersion(name);
      AtsArtifactConfigCache cache = getConfigCache();
      cache.cache(item);
      return item;
   }

   @Override
   public IAtsWorkDefinitionAdmin getWorkDefinitionAdmin() {
      return workDefAdmin;
   }

   @Override
   public IAtsConfig getAtsConfig() {
      return configProxy;
   }

   @Override
   public IAtsVersionAdmin getAtsVersionService() {
      return versionService;
   }

   @Override
   public IAtsUserAdmin getUserAdmin() {
      return atsUserAdmin;
   }

   @Override
   public IAtsQuery createQuery(Collection<? extends IAtsWorkItem> workItems) {
      return new AtsQuery(workItems, workItemService, workItemArtifactProvider);
   }

   private AtsArtifactConfigCache getConfigCache() throws OseeCoreException {
      return configCacheProvider.get();
   }

   private final class AtsConfigProxy implements IAtsConfig {

      @Override
      public <A extends IAtsConfigObject> List<A> getByTag(String tag, Class<A> clazz) throws OseeCoreException {
         return getConfigCache().getByTag(tag, clazz);
      }

      @Override
      public <A extends IAtsConfigObject> A getSoleByTag(String tag, Class<A> clazz) throws OseeCoreException {
         return getConfigCache().getSoleByTag(tag, clazz);
      }

      @Override
      public <A extends IAtsConfigObject> List<A> get(Class<A> clazz) throws OseeCoreException {
         return getConfigCache().get(clazz);
      }

      @Override
      public <A extends IAtsConfigObject> A getSoleByGuid(String guid, Class<A> clazz) throws OseeCoreException {
         return getConfigCache().getSoleByGuid(guid, clazz);
      }

      @Override
      public IAtsConfigObject getSoleByGuid(String guid) throws OseeCoreException {
         return getConfigCache().getSoleByGuid(guid);
      }

      @Override
      public void getReport(XResultData rd) throws OseeCoreException {
         getConfigCache().getReport(rd);
      }

      @Override
      public void invalidate(IAtsConfigObject configObject) throws OseeCoreException {
         getConfigCache().invalidate(configObject);
      }

   }

}
