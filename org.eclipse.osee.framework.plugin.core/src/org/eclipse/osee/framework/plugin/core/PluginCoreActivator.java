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
package org.eclipse.osee.framework.plugin.core;

import java.util.logging.Level;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osgi.framework.internal.core.AbstractBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class PluginCoreActivator extends OseeActivator {
   private static PluginCoreActivator pluginInstance; // The shared instance.
   public static final String PLUGIN_ID = "osee.plugin.core";

   public PluginCoreActivator() {
      super();
      pluginInstance = this;
   }

   /**
    * Returns the shared instance.
    */
   public static PluginCoreActivator getInstance() {
      return pluginInstance;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.plugin.core.OseeActivator#start(org.osgi.framework.BundleContext)
    */
   @Override
   public void start(BundleContext context) throws Exception {
      super.start(context);

      for (Bundle bundle : context.getBundles()) {
         checkForEarlyStartup(bundle);
      }

      context.addBundleListener(new BundleListener() {

         @Override
         public void bundleChanged(BundleEvent event) {
            if (event.getType() == BundleEvent.INSTALLED) {
               checkForEarlyStartup(event.getBundle());
            }
         }
      });
   }

   /**
    * @param bundle
    */
   private void checkForEarlyStartup(Bundle bundle) {
      if (bundle.getHeaders().get("OseeEarlyStart") != null) {
         try {
            if (!((AbstractBundle) bundle).testStateChanging(Thread.currentThread())) {
               bundle.start();
            }
         } catch (BundleException ex) {
            OseeLog.log(OseeActivator.class, Level.SEVERE, ex);
         }
      }
   }
}