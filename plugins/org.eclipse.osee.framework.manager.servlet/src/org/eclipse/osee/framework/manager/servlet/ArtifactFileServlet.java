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
package org.eclipse.osee.framework.manager.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.server.UnsecuredOseeHttpServlet;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.manager.servlet.data.ArtifactUtil;
import org.eclipse.osee.framework.manager.servlet.data.DefaultOseeArtifact;
import org.eclipse.osee.framework.manager.servlet.data.HttpArtifactFileInfo;
import org.eclipse.osee.framework.resource.management.IResource;
import org.eclipse.osee.framework.resource.management.IResourceLocator;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.framework.resource.management.StandardOptions;
import org.eclipse.osee.logger.Log;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactFileServlet extends UnsecuredOseeHttpServlet {

   private static final long serialVersionUID = -6334080268467740905L;

   private final IResourceManager resourceManager;
   private final BranchCache branchCache;

   private static Map<Integer, String> branchIdToGuidMap;

   public ArtifactFileServlet(Log logger, IResourceManager resourceManager, BranchCache branchCache) {
      super(logger);
      this.resourceManager = resourceManager;
      this.branchCache = branchCache;
   }

   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      try {
         HttpArtifactFileInfo artifactFileInfo = null;

         String servletPath = request.getServletPath();
         //         System.out.println("servletPath: " + servletPath);
         if (!Strings.isValid(servletPath) || "/".equals(servletPath) || "/index".equals(servletPath)) {
            //            Enumeration<?> enumeration = request.getHeaderNames();
            //            while (enumeration.hasMoreElements()) {
            //               String headerField = (String) enumeration.nextElement();
            //               String value = request.getHeader(headerField);
            //               System.out.println(String.format("%s: %s", headerField, value));
            //            }

            Pair<String, String> defaultArtifact = DefaultOseeArtifact.get();
            if (defaultArtifact != null) {
               artifactFileInfo =
                  new HttpArtifactFileInfo(defaultArtifact.getFirst(), null, null, defaultArtifact.getSecond());
            }
         } else {
            artifactFileInfo = new HttpArtifactFileInfo(request);
         }

         String uri = null;
         if (artifactFileInfo != null) {
            Branch branch = null;
            if (artifactFileInfo.isBranchNameValid()) {
               branch = branchCache.getBySoleName(artifactFileInfo.getBranchName());
            } else if (artifactFileInfo.isBranchGuidValid()) {
               branch = branchCache.getByGuid(artifactFileInfo.getBranchGuid());
            } else {

               int branchId = artifactFileInfo.getId();
               if (branchId != HttpArtifactFileInfo.INVALID_BRANCH_ID) {
                  loadIdToGuidMapping();
                  branch = branchCache.getByGuid(branchIdToGuidMap.get(branchId));
               }

            }
            uri = ArtifactUtil.getUri(artifactFileInfo.getGuid(), branch);
         }
         handleArtifactUri(resourceManager, request.getQueryString(), uri, response);
      } catch (NumberFormatException ex) {
         handleError(response, HttpServletResponse.SC_BAD_REQUEST,
            String.format("Invalid Branch Id: [%s]", request.getQueryString()), ex);
      } catch (Exception ex) {
         handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            String.format("Unable to acquire resource: [%s]", request.getQueryString()), ex);
      } finally {
         response.flushBuffer();
      }
   }

   public static void handleArtifactUri(IResourceManager resourceManager, String request, String uri, HttpServletResponse response) throws OseeCoreException {
      boolean wasProcessed = false;
      if (Strings.isValid(uri)) {
         IResourceLocator locator = resourceManager.getResourceLocator(uri);
         PropertyStore options = new PropertyStore();
         options.put(StandardOptions.DecompressOnAquire.name(), true);
         IResource resource = resourceManager.acquire(locator, options);

         if (resource != null) {
            wasProcessed = true;

            InputStream inputStream = null;
            try {
               inputStream = resource.getContent();

               response.setStatus(HttpServletResponse.SC_OK);
               response.setContentLength(inputStream.available());
               response.setCharacterEncoding("ISO-8859-1");
               String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
               if (mimeType == null) {
                  mimeType = URLConnection.guessContentTypeFromName(resource.getLocation().toString());
                  if (mimeType == null) {
                     mimeType = "application/*";
                  }
               }
               response.setContentType(mimeType);
               if (!mimeType.equals("text/html")) {
                  response.setHeader("Content-Disposition", "attachment; filename=" + resource.getName());
               }
               Lib.inputStreamToOutputStream(inputStream, response.getOutputStream());
               response.flushBuffer();
            } catch (IOException ex) {
               OseeExceptions.wrapAndThrow(ex);
            } finally {
               Lib.close(inputStream);
            }
         }
      }
      if (!wasProcessed) {
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         response.setContentType("text/plain");
         try {
            response.getWriter().write(String.format("Unable to find resource: [%s]", request));
         } catch (IOException ex) {
            OseeExceptions.wrapAndThrow(ex);
         }
      }
   }

   private void handleError(HttpServletResponse response, int status, String message, Throwable ex) throws IOException {
      response.setStatus(status);
      response.setContentType("text/plain");
      getLogger().error(ex, message);
      response.getWriter().write(Lib.exceptionToString(ex));
   }

   private synchronized void loadIdToGuidMapping() {
      if (branchIdToGuidMap == null) {
         branchIdToGuidMap = new HashMap<Integer, String>();
         BufferedReader reader = null;

         try {
            reader =
               new BufferedReader(new InputStreamReader(FrameworkUtil.getBundle(ArtifactFileServlet.class).getResource(
                  "templates/branchIdToBranchGuid.txt").openStream()));
            String branchIdAndGuidLine = null;

            while ((branchIdAndGuidLine = reader.readLine()) != null) {

               String[] idAndGuid = branchIdAndGuidLine.split(",");

               int branchId;
               try {
                  branchId = Integer.parseInt(idAndGuid[0]);
               } catch (Exception ex) {
                  branchId = -1;
               }

               branchIdToGuidMap.put(branchId, idAndGuid[1].trim());
            }
         } catch (Exception e) {
            getLogger().error(e, "Error mapping branchIds to guids");
         } finally {
            Lib.close(reader);
         }
      }
   }
}
