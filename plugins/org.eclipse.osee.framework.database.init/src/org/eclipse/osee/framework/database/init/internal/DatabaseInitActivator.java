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
package org.eclipse.osee.framework.database.init.internal;

import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.core.translation.IDataTranslationService;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class DatabaseInitActivator implements BundleActivator {
   public static final String PLUGIN_ID = "org.eclipse.osee.framework.database.init";

   private static DatabaseInitActivator instance;

   private ServiceTracker<IDataTranslationService, IDataTranslationService> serviceTracker;
   private ServiceTracker<IOseeCachingService, IOseeCachingService> serviceTracker2;
   private ServiceTracker<IOseeDatabaseService, IOseeDatabaseService> serviceTracker3;
   private ServiceTracker<IdentityService, IdentityService> serviceTracker4;

   @Override
   public void start(BundleContext context) throws Exception {
      DatabaseInitActivator.instance = this;
      serviceTracker =
         new ServiceTracker<IDataTranslationService, IDataTranslationService>(context, IDataTranslationService.class,
            null);
      serviceTracker.open(true);

      serviceTracker2 =
         new ServiceTracker<IOseeCachingService, IOseeCachingService>(context, IOseeCachingService.class, null);
      serviceTracker2.open(true);

      serviceTracker3 =
         new ServiceTracker<IOseeDatabaseService, IOseeDatabaseService>(context, IOseeDatabaseService.class, null);
      serviceTracker3.open(true);

      serviceTracker4 = new ServiceTracker<IdentityService, IdentityService>(context, IdentityService.class, null);
      serviceTracker4.open(true);
   }

   @Override
   public void stop(BundleContext context) throws Exception {
      if (serviceTracker != null) {
         serviceTracker.close();
      }
      if (serviceTracker2 != null) {
         serviceTracker2.close();
      }
      if (serviceTracker3 != null) {
         serviceTracker3.close();
      }
      if (serviceTracker4 != null) {
         serviceTracker4.close();
      }
   }

   public static DatabaseInitActivator getInstance() {
      return instance;
   }

   public IOseeCachingService getCachingService() {
      return serviceTracker2.getService();
   }

   public IDataTranslationService getTranslationService() {
      return serviceTracker.getService();
   }

   public IOseeDatabaseService getDatabaseService() {
      return serviceTracker3.getService();
   }

   public IdentityService getIdentityService() {
      return serviceTracker4.getService();
   }
}
