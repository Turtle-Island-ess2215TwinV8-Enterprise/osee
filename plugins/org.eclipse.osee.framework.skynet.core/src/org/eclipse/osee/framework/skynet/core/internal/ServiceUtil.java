/*
 * Created on Apr 25, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.internal;

import java.util.logging.Level;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.TransactionRecordFactory;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryService;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.core.translation.IDataTranslationService;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.lifecycle.ILifecycleService;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.AccessPolicy;
import org.eclipse.osee.framework.skynet.core.event.OseeEventService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public final class ServiceUtil {

   private ServiceUtil() {
      // Utility class
   }

   private static BundleContext getBundleContext() throws OseeCoreException {
      Bundle bundle = FrameworkUtil.getBundle(ServiceUtil.class);
      Conditions.checkNotNull(bundle, "bundle");
      return bundle.getBundleContext();
   }

   private static <T> T getService(Class<T> clazz) throws OseeCoreException {
      BundleContext context = getBundleContext();
      Conditions.checkNotNull(context, "bundleContext");
      ServiceReference<T> reference = context.getServiceReference(clazz);
      Conditions.checkNotNull(reference, "serviceReference");
      T service = context.getService(reference);
      Conditions.checkNotNull(service, "service");
      return service;
   }

   public static OseeEventService getEventService() throws OseeCoreException {
      return getService(OseeEventService.class);
   }

   public static IdentityService getIdentityService() throws OseeCoreException {
      return getService(IdentityService.class);
   }

   public static IOseeCachingService getOseeCacheService() throws OseeCoreException {
      return getService(IOseeCachingService.class);
   }

   public static IDataTranslationService getTranslationService() throws OseeCoreException {
      return getService(IDataTranslationService.class);
   }

   public static IOseeDatabaseService getOseeDatabaseService() throws OseeCoreException {
      return getService(IOseeDatabaseService.class);
   }

   public static ILifecycleService getLifecycleService() throws OseeCoreException {
      return getService(ILifecycleService.class);
   }

   public static TransactionRecordFactory getTransactionFactory() throws OseeCoreException {
      IOseeModelFactoryService service = getService(IOseeModelFactoryService.class);
      return service != null ? service.getTransactionFactory() : null;
   }

   public static IOseeModelFactoryService getOseeModelFactoryService() throws OseeCoreException {
      return getService(IOseeModelFactoryService.class);
   }

   public static AccessPolicy getAccessPolicy() throws OseeCoreException {
      try {
         Bundle bundle = Platform.getBundle("org.eclipse.osee.framework.access");
         if (bundle.getState() != Bundle.ACTIVE) {
            bundle.start();
         }
      } catch (BundleException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return getService(AccessPolicy.class);
   }
}