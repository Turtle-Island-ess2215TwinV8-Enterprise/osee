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
package org.eclipse.osee.rest.admin.internal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;
import org.eclipse.osee.rest.admin.RestAdminConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * @author Roberto E. Escobar
 */
public class RestServletManager {

   private final Map<String, ServletContainer> registeredServlets = new ConcurrentHashMap<String, ServletContainer>();
   private final List<ServiceReference> pending = new CopyOnWriteArrayList<ServiceReference>();

   private HttpService httpService;
   private EventAdmin eventAdmin;
   private Thread thread;

   public void setHttpService(HttpService httpService) {
      this.httpService = httpService;
   }

   public void setEventAdmin(EventAdmin eventAdmin) {
      this.eventAdmin = eventAdmin;
   }

   public void start() throws Exception {
      thread = new Thread("Register Pending Rest Services") {
         @Override
         public void run() {
            for (ServiceReference reference : pending) {
               register(reference);
            }
            pending.clear();
         }
      };
      thread.start();
   }

   public void stop() {
      if (thread != null && thread.isAlive()) {
         thread.interrupt();
      }
   }

   private boolean isReady() {
      return httpService != null && eventAdmin != null;
   }

   public void addApplication(ServiceReference reference) {
      if (isReady()) {
         register(reference);
      } else {
         pending.add(reference);
      }
   }

   public void removeApplication(ServiceReference reference) {
      if (isReady()) {
         unregister(reference);
      } else {
         pending.remove(reference);
      }
   }

   private void unregister(ServiceReference reference) {
      String componentName = RestServiceUtils.getComponentName(reference);
      String contextName = RestServiceUtils.getContextName(reference);

      System.out.printf("De-registering servlet for '%s' with alias '%s'\n", componentName, contextName);
      HttpServlet servlet = registeredServlets.remove(componentName);
      if (servlet != null) {
         httpService.unregister(contextName);
         servlet.destroy();
      }
      notifyDeRegistration(componentName, contextName);
   }

   private void register(ServiceReference reference) {
      String componentName = RestServiceUtils.getComponentName(reference);
      String contextName = RestServiceUtils.getContextName(reference);

      try {
         ServletContainer servlet = createContainer(reference);
         HttpContext httpContext = createHttpContext(reference);
         httpService.registerServlet(contextName, servlet, null, httpContext);
         registeredServlets.put(componentName, servlet);
         notifyRegistration(componentName, contextName);
         System.out.printf("Registered servlet for '%s' with alias '%s'\n", componentName, contextName);
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

   private HttpContext createHttpContext(ServiceReference reference) {
      Bundle bundle = reference.getBundle();
      return new BundleHttpContext(bundle);
   }

   private ServletContainer createContainer(ServiceReference reference) throws Exception {
      Bundle bundle = reference.getBundle();
      Application application = (Application) bundle.getBundleContext().getService(reference);
      RestServiceUtils.checkValid(application);
      return new ServletContainer(application);
   }

   private void notifyRegistration(String componentName, String contextName) {
      Map<String, String> data = RestServiceUtils.toMap(componentName, contextName);
      eventAdmin.postEvent(new Event(RestAdminConstants.REST_REGISTRATION_EVENT, data));
   }

   private void notifyDeRegistration(String componentName, String contextName) {
      Map<String, String> data = RestServiceUtils.toMap(componentName, contextName);
      eventAdmin.postEvent(new Event(RestAdminConstants.REST_DEREGISTRATION_EVENT, data));
   }

}