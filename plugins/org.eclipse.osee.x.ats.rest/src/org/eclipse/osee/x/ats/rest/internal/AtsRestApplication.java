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
package org.eclipse.osee.x.ats.rest.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import lba.ats.rest.ReportProvider;

/**
 * Get application.wadl at this context to get rest documentation
 * 
 * @author Roberto E. Escobar
 */
public class AtsRestApplication extends Application {

   private final Collection<ReportProvider> reportProviders = new HashSet<ReportProvider>();

   @Override
   public Set<Class<?>> getClasses() {
      Set<Class<?>> classes = new HashSet<Class<?>>();
      for (ReportProvider provider : reportProviders) {
         classes.add(provider.getClass());
      }
      return classes;
   }

   public void addReportProvider(ReportProvider reportProvider) {
      reportProviders.add(reportProvider);
   }

}
