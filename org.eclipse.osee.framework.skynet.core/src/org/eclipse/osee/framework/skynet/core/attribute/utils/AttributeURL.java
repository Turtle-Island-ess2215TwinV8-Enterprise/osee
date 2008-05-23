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

package org.eclipse.osee.framework.skynet.core.attribute.utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.attribute.Attribute;
import org.eclipse.osee.framework.skynet.core.linking.HttpUrlBuilder;

/**
 * @author Roberto E. Escobar
 */
public class AttributeURL {
   private static final String SERVLET_CONTEXT = "resource";

   private AttributeURL() {
   }

   public static URL getStorageURL(Attribute<?> attribute, String extension) throws Exception {
      return getStorageURL(attribute.getGammaId(), attribute.getArtifact().getHumanReadableId(), extension);
   }

   public static URL getStorageURL(int gammaId, String artifactHrid, String extension) throws Exception {
      Map<String, String> parameterMap = new HashMap<String, String>();
      parameterMap.put("protocol", "attr");
      parameterMap.put("seed", Integer.toString(gammaId));
      parameterMap.put("name", artifactHrid);
      if (Strings.isValid(extension) != false) {
         parameterMap.put("extension", extension);
      }
      String urlString = HttpUrlBuilder.getInstance().getOsgiServletServiceUrl(SERVLET_CONTEXT, parameterMap);
      return new URL(urlString);
   }

   private static URL generatePathURL(String uri) throws Exception {
      Map<String, String> parameterMap = new HashMap<String, String>();
      parameterMap.put("uri", uri);
      String urlString = HttpUrlBuilder.getInstance().getOsgiServletServiceUrl(SERVLET_CONTEXT, parameterMap);
      return new URL(urlString);
   }

   public static URL getAcquireURL(String uri) throws Exception {
      return generatePathURL(uri);
   }

   public static URL getDeleteURL(String uri) throws Exception {
      return generatePathURL(uri);
   }
}
