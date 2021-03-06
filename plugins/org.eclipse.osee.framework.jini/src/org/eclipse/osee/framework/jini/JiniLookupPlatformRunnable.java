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
package org.eclipse.osee.framework.jini;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osee.framework.jdk.core.util.CmdLineArgs;
import org.eclipse.osee.framework.jini.utility.StartJini;
import org.osgi.framework.Bundle;

public class JiniLookupPlatformRunnable implements IApplication {
   private StartJini jiniService;

   public JiniLookupPlatformRunnable() {
      super();
      this.jiniService = null;
   }

   private String getJiniHome() throws IOException {
      Bundle bundle = Platform.getBundle("net.jini");
      URL url = bundle.getEntry("/");
      url = FileLocator.resolve(url);
      return new File(url.getFile()).getAbsolutePath();
   }

   private InputStream getJiniManifest() throws IOException {
      Bundle bundle = Platform.getBundle("org.eclipse.osee.framework.jini");
      URL url = bundle.getEntry("META-INF/MANIFEST.MF");
      return url.openStream();
   }

   @Override
   public Object start(IApplicationContext context) throws Exception {
      String[] inputArgs = Platform.getApplicationArgs();
      CmdLineArgs args = new CmdLineArgs(inputArgs);

      String port = args.get("-port");
      if (port == null) {
         port = "8081";
      }
      jiniService = null;
      jiniService = new StartJini(port, true, false, getJiniHome(), getJiniManifest());
      return IApplication.EXIT_OK;
   }

   @Override
   public void stop() {
      if (jiniService != null) {
         jiniService.kill();
      }
   }

}
