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
package org.eclipse.osee.framework.ui.skynet.httpRequests;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.linking.HttpRequest;
import org.eclipse.osee.framework.skynet.core.linking.HttpResponse;
import org.eclipse.osee.framework.skynet.core.linking.HttpServer;
import org.eclipse.osee.framework.skynet.core.linking.IHttpServerRequest;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionIdManager;
import org.eclipse.osee.framework.ui.skynet.artifact.snapshot.ArtifactSnapshotManager;
import org.eclipse.osee.framework.ui.skynet.render.FileSystemRenderer;
import org.eclipse.osee.framework.ui.skynet.render.IRenderer;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.Renderer;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactRequest implements IHttpServerRequest {
   private static final String GUID_KEY = "guid";
   private static final String BRANCH_NAME_KEY = "branch";
   private static final String BRANCH_ID_KEY = "branchId";
   private static final String TRANSACTION_NUMBER_KEY = "transaction";
   private static final String FORCE_KEY = "force";
   private static final String FORMAT_KEY = "format";
   private static final ArtifactRequest instance = new ArtifactRequest();
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(ArtifactRequest.class);

   public enum FormatEnums {
      HTML, NATIVE
   }

   private ArtifactRequest() {
   }

   public static ArtifactRequest getInstance() {
      return instance;
   }

   public String getUrl(Artifact artifact, boolean includeRevision) {
      Map<String, String> keyValues = new HashMap<String, String>();
      String guid = artifact.getGuid();
      int branch = artifact.getBranch().getBranchId();
      try {
         if (Strings.isValid(guid)) {
            keyValues.put(GUID_KEY, URLEncoder.encode(guid, "UTF-8"));
         }
         keyValues.put(BRANCH_ID_KEY, URLEncoder.encode(Integer.toString(branch), "UTF-8"));
         if (includeRevision) {
            int txNumber = artifact.getPersistenceMemo().getTransactionNumber();
            keyValues.put(TRANSACTION_NUMBER_KEY, URLEncoder.encode(Integer.toString(txNumber), "UTF-8"));
         }
      } catch (UnsupportedEncodingException ex) {
         logger.log(Level.SEVERE, ex.toString(), ex);
      }
      return HttpServer.getUrl(this, keyValues);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.skynet.core.linking.IHttpServerRequest#getRequestType()
    */
   public String getRequestType() {
      return "GET.ARTIFACT";
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.skynet.core.linking.IHttpServerRequest#processRequest(org.eclipse.osee.framework.skynet.core.linking.HttpRequest,
    *      org.eclipse.osee.framework.skynet.core.linking.HttpResponse)
    */
   public void processRequest(HttpRequest httpRequest, HttpResponse httpResponse) {
      boolean updateCache = Boolean.parseBoolean(httpRequest.getParameter(FORCE_KEY));
      try {
         final Artifact artifact = getRequestedArtifact(httpRequest);
         final FormatEnums requesttedFormat = getFormatType(httpRequest);
         switch (requesttedFormat) {
            case NATIVE:
               sendAsNative(artifact, httpResponse);
               break;
            case HTML:
            default:
               sendAsHTML(artifact, updateCache, httpResponse);
               break;
         }
      } catch (Exception ex) {
         logger.log(Level.WARNING, String.format("Get Artifact Error: [%s]", httpRequest.getParametersAsString()), ex);
         httpResponse.outputStandardError(400, "Exception handling request");
      }
   }

   private Artifact getRequestedArtifact(HttpRequest httpRequest) throws Exception {
      String guidKey = httpRequest.getParameter(GUID_KEY);
      String branchIdKey = httpRequest.getParameter(BRANCH_ID_KEY);
      String branchNameKey = httpRequest.getParameter(BRANCH_NAME_KEY);
      String transactionKey = httpRequest.getParameter(TRANSACTION_NUMBER_KEY);
      Artifact toReturn;
      if (Strings.isValid(transactionKey)) {
         toReturn = getArtifactBasedOnTransactionNumber(guidKey, Integer.parseInt(transactionKey));
      } else {
         toReturn = getLatestArtifactForBranch(guidKey, branchIdKey, branchNameKey);
      }
      return toReturn;
   }

   private FormatEnums getFormatType(HttpRequest httpRequest) {
      String format = httpRequest.getParameter(FORMAT_KEY);
      FormatEnums toReturn = FormatEnums.HTML;
      try {
         toReturn = FormatEnums.valueOf(format.toUpperCase());
      } catch (Exception ex) {
         toReturn = FormatEnums.HTML;
      }
      return toReturn;
   }

   private void sendAsHTML(Artifact artifact, boolean updateCache, HttpResponse httpResponse) throws Exception {
      String html = ArtifactSnapshotManager.getInstance().getDataSnapshot(artifact, updateCache);
      httpResponse.getPrintStream().println(AHTML.pageEncoding(html));
   }

   private void sendAsNative(Artifact artifact, HttpResponse httpResponse) throws Exception {
      IRenderer render = RendererManager.getInstance().getBestRenderer(PresentationType.EDIT, artifact);
      if (render instanceof FileSystemRenderer) {
         FileSystemRenderer fileSystemRenderer = (FileSystemRenderer) render;
         Branch branch = artifact.getBranch();
         IFolder baseFolder = fileSystemRenderer.getRenderFolder(branch, PresentationType.EDIT);
         IFile iFile =
               fileSystemRenderer.renderToFileSystem(new NullProgressMonitor(), baseFolder, artifact, branch, null,
                     PresentationType.EDIT);

         File file = iFile.getLocation().toFile();
         String fileName = artifact.getDescriptiveName() + "." + iFile.getFileExtension();
         String encodedFileName = URLEncoder.encode(fileName, "UTF-8");

         httpResponse.setReponseHeader("Accept-Ranges", "bytes");
         httpResponse.setContentType("application");
         httpResponse.setContentEncoding(iFile.getCharset());
         httpResponse.setContentDisposition("attachment; filename=" + encodedFileName);
         httpResponse.sendResponseHeaders(200, file.length());

         httpResponse.sendBody(new FileInputStream(file));

         iFile.delete(true, new NullProgressMonitor());
      } else if (render instanceof Renderer) {
         sendAsHTML(artifact, false, httpResponse);
      }
   }

   private Artifact getLatestArtifactForBranch(String guid, String branchId, String branchName) throws SQLException {
      BranchPersistenceManager branchManager = BranchPersistenceManager.getInstance();
      final Branch branch;
      if (Strings.isValid(branchId)) {
         branch = branchManager.getBranch(Integer.parseInt(branchId));
      } else {
         branch = branchManager.getBranch(branchName);
      }
      return ArtifactPersistenceManager.getInstance().getArtifact(guid, branch);
   }

   private Artifact getArtifactBasedOnTransactionNumber(String guid, int transactioNumber) throws Exception {
      TransactionIdManager transactionIdManager = TransactionIdManager.getInstance();
      TransactionId transactionId = null;
      try {
         transactionId = transactionIdManager.getPossiblyEditableTransactionIfFromCache(transactioNumber);
      } catch (Exception ex) {
         try {
            Thread.sleep(1000);
         } catch (Exception ex1) {
         }
         transactionId = transactionIdManager.getPossiblyEditableTransactionId(transactioNumber);
      }
      return ArtifactPersistenceManager.getInstance().getArtifact(guid, transactionId);
   }
}
