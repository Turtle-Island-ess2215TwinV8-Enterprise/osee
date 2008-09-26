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
package org.eclipse.osee.framework.branch.management.exchange.resource;

import java.net.URI;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.resource.management.IResourceLocator;
import org.eclipse.osee.framework.resource.management.IResourceLocatorProvider;
import org.eclipse.osee.framework.resource.management.ResourceLocator;
import org.eclipse.osee.framework.resource.management.exception.MalformedLocatorException;

/**
 * @author Roberto E. Escobar
 */
public class ExchangeLocatorProvider implements IResourceLocatorProvider {
   public static final String PROTOCOL = "exchange";

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.resource.management.IResourceLocatorProvider#generateResourceLocator(java.lang.String, java.lang.String)
    */
   @Override
   public IResourceLocator generateResourceLocator(String seed, String name) throws MalformedLocatorException {
      URI uri = null;
      try {
         uri = new URI(generatePath(name));
      } catch (Exception ex) {
         throw new MalformedLocatorException(ex);
      }
      return new ResourceLocator(uri);
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.resource.management.IResourceLocatorProvider#getResourceLocator(java.lang.String)
    */
   @Override
   public IResourceLocator getResourceLocator(String path) throws MalformedLocatorException {
      URI uri = null;
      if (isPathValid(path) != false) {
         try {
            uri = new URI(path);
         } catch (Exception ex) {
            throw new MalformedLocatorException(ex);
         }
      } else {
         throw new MalformedLocatorException(String.format("Invalid path hint: [%s]", path));
      }
      return new ResourceLocator(uri);
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.resource.management.IResourceLocatorProvider#isValid(java.lang.String)
    */
   @Override
   public boolean isValid(String protocol) {
      return Strings.isValid(protocol) != false && protocol.startsWith(PROTOCOL) != false;
   }

   private boolean isPathValid(String value) {
      return Strings.isValid(value) && value.startsWith(PROTOCOL + "://");
   }

   private String generatePath(String name) throws MalformedLocatorException {
      StringBuilder builder = new StringBuilder(PROTOCOL + "://");
      if (Strings.isValid(name)) {
         builder.append(name);
      } else {
         throw new MalformedLocatorException("Invalid arguments during locator generation.");
      }
      return builder.toString();
   }
}
