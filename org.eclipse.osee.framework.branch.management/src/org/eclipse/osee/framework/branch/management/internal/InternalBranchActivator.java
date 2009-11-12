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
package org.eclipse.osee.framework.branch.management.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.framework.branch.management.IBranchArchivingService;
import org.eclipse.osee.framework.branch.management.IBranchCommitService;
import org.eclipse.osee.framework.branch.management.IBranchCreation;
import org.eclipse.osee.framework.branch.management.IBranchExchange;
import org.eclipse.osee.framework.branch.management.IChangeReportService;
import org.eclipse.osee.framework.branch.management.ITransactionService;
import org.eclipse.osee.framework.branch.management.change.ChangeReportService;
import org.eclipse.osee.framework.branch.management.commit.BranchCommitService;
import org.eclipse.osee.framework.branch.management.creation.BranchCreation;
import org.eclipse.osee.framework.branch.management.exchange.BranchExchange;
import org.eclipse.osee.framework.branch.management.remote.BranchArchivingService;
import org.eclipse.osee.framework.branch.management.transaction.TransactionService;
import org.eclipse.osee.framework.core.server.IApplicationServerManager;
import org.eclipse.osee.framework.resource.management.IResourceLocatorManager;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public class InternalBranchActivator implements BundleActivator {
   public static final String PLUGIN_ID = "org.eclipse.osee.framework.branch.management";

   private enum TrackerId {
      RESOURCE_LOCATOR,
      RESOURCE_MANAGER,
      BRANCH_EXCHANGE,
      TRANSACTION_SERVICE,
      MASTER_SERVICE;
   }

   private static InternalBranchActivator instance;

   private final Map<TrackerId, ServiceTracker> mappedTrackers;
   private final List<ServiceRegistration> services;

   public InternalBranchActivator() {
      this.mappedTrackers = new HashMap<TrackerId, ServiceTracker>();
      this.services = new ArrayList<ServiceRegistration>();
   }

   public void start(BundleContext context) throws Exception {
      InternalBranchActivator.instance = this;

      createService(context, IBranchCreation.class, new BranchCreation());
      createService(context, IBranchArchivingService.class, new BranchArchivingService());
      createService(context, IBranchCommitService.class, new BranchCommitService());
      createService(context, IChangeReportService.class, new ChangeReportService());
      createService(context, IBranchCreation.class, new BranchCreation());
      createService(context, IBranchExchange.class, new BranchExchange());
      createService(context, ITransactionService.class, new TransactionService());

      createServiceTracker(context, IResourceLocatorManager.class, TrackerId.RESOURCE_LOCATOR);
      createServiceTracker(context, IResourceManager.class, TrackerId.RESOURCE_MANAGER);
      createServiceTracker(context, IBranchExchange.class, TrackerId.BRANCH_EXCHANGE);
      createServiceTracker(context, IApplicationServerManager.class, TrackerId.MASTER_SERVICE);
      createServiceTracker(context, ITransactionService.class, TrackerId.TRANSACTION_SERVICE);
   }

   public void stop(BundleContext context) throws Exception {
      for (ServiceRegistration service : services) {
         service.unregister();
      }

      for (ServiceTracker tracker : mappedTrackers.values()) {
         tracker.close();
      }
      services.clear();
      mappedTrackers.clear();

      instance = null;
   }

   private void createService(BundleContext context, Class<?> serviceInterface, Object serviceImplementation) {
      services.add(context.registerService(serviceInterface.getName(), serviceImplementation, null));
   }

   private void createServiceTracker(BundleContext context, Class<?> clazz, TrackerId trackerId) {
      ServiceTracker tracker = new ServiceTracker(context, clazz.getName(), null);
      tracker.open();
      mappedTrackers.put(trackerId, tracker);
   }

   public static InternalBranchActivator getInstance() {
      return instance;
   }

   public IBranchExchange getBranchExchange() {
      return getTracker(TrackerId.BRANCH_EXCHANGE, IBranchExchange.class);
   }

   public IResourceManager getResourceManager() {
      return getTracker(TrackerId.RESOURCE_MANAGER, IResourceManager.class);
   }

   public IResourceLocatorManager getResourceLocatorManager() {
      return getTracker(TrackerId.RESOURCE_LOCATOR, IResourceLocatorManager.class);
   }

   public IApplicationServerManager getApplicationServerManger() {
      return getTracker(TrackerId.MASTER_SERVICE, IApplicationServerManager.class);
   }

   public ITransactionService getTransactionService() {
      return getTracker(TrackerId.TRANSACTION_SERVICE, ITransactionService.class);
   }

   private <T> T getTracker(TrackerId trackerId, Class<T> clazz) {
      ServiceTracker tracker = mappedTrackers.get(trackerId);
      Object service = tracker.getService();
      return clazz.cast(service);
   }
}
