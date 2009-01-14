package org.eclipse.osee.framework.ui.skynet.blam.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.client.server.HttpUrlBuilder;
import org.eclipse.osee.framework.core.data.OseeServerContext;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;
import org.eclipse.osee.framework.db.connection.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.db.connection.exception.OseeWrappedException;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.HttpProcessor;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.HttpProcessor.AcquireResult;
import org.eclipse.osee.framework.jdk.core.util.xml.Jaxp;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.attribute.WordAttribute;
import org.eclipse.osee.framework.ui.plugin.util.OseeData;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.render.WordTemplateRenderer;
import org.eclipse.osee.framework.ui.skynet.widgets.xresults.XResultData;
import org.eclipse.osee.framework.ui.skynet.widgets.xresults.XResultPage.Manipulations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Jeff C. Phillips
 * @author Theron Virgin
 */
public class FixTemplateContentArtifacts extends AbstractBlam {
   private static final boolean DEBUG =
         Boolean.parseBoolean(Platform.getDebugOption("org.eclipse.osee.framework.ui.skynet/debug/Blam"));

   private static final String GET_ATTRS =
         "SELECT * FROM osee_attribute t1, osee_artifact t3 WHERE t1.attr_type_id = ? AND t1.art_id = t3.art_id AND t1.uri is not null";
   private static final String GET_ATTRS_TEST = GET_ATTRS + " AND t1.art_id = 3894";
   private Collection<String> badData = new LinkedList<String>();
   private static final String[] columnHeaders = new String[] {"Corrupted Data"};

   private static File createTempFolder() {
      File rootDirectory = OseeData.getFile("FixTemplate_" + Lib.getDateTimeString() + File.separator);
      rootDirectory.mkdirs();
      return rootDirectory;
   }

   @Override
   public void runOperation(VariableMap variableMap, IProgressMonitor monitor) throws Exception {
      if (true) {
         System.err.println("Only to be run by developer");
         return;
      }
      File backupFolder = createTempFolder();
      OseeLog.log(SkynetGuiPlugin.class, Level.INFO, String.format("Backup Folder location: [%s]",
            backupFolder.getAbsolutePath()));

      ArrayList<AttrData> attrDatas = loadAttrData();
      monitor.beginTask("Fix word template content", attrDatas.size());
      for (AttrData attrData : attrDatas) {
         monitor.subTask(attrData.getHrid());
         Resource resource = getResource(attrData.getUri());

         Element rootElement = null;
         if (DEBUG) {
            OseeLog.log(SkynetGuiPlugin.class, Level.INFO, String.format("Before Fix: %s", resource.data));
         }

         final Collection<Element> elements = new LinkedList<Element>();
         final Collection<Element> sectPr = new LinkedList<Element>();
         boolean fixedAttribute = false;
         try {
            Document doc = Jaxp.readXmlDocument("<ForFix>" + resource.data + "</ForFix>");
            rootElement = doc.getDocumentElement();

            NodeList nodeList = rootElement.getElementsByTagName("*");
            for (int i = 0; i < nodeList.getLength(); i++) {
               Element element = (Element) nodeList.item(i);
               if (element.getNodeName().endsWith("w:p")) {
                  elements.add(element);
               }
               if (element.getNodeName().endsWith("w:sectPr")) {
                  sectPr.add(element);
               }
            }
            for (Element paragraph : elements) {
               boolean badParagraph = isBadParagraph(paragraph);
               if (badParagraph) {
                  paragraph.getParentNode().removeChild(paragraph);
               }
               fixedAttribute = fixedAttribute || badParagraph;
            }
            for (Element sect : sectPr) {
               sect.getParentNode().removeChild(sect);
               fixedAttribute = true;
            }
         } catch (Exception ex) {
            badData.add(attrData.gammaId);
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, String.format("Skiping File %s because of exception %s",
                  attrData.getHrid(), ex));
         }

         if (fixedAttribute) {
            try {
               // Backup File
               backupResourceLocally(backupFolder, resource);

               // Perform Fix
               resource.data = new String(WordTemplateRenderer.getFormattedContent(rootElement), "UTF-8");
               resource.encoding = "UTF-8";

               // UploadResource
               uploadResource(attrData.getGammaId(), resource);

               if (DEBUG) {
                  OseeLog.log(SkynetGuiPlugin.class, Level.INFO, String.format(" After Fix : %s", resource.data));
               }
            } catch (Exception ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, String.format(
                     "Skiping File %s because of exception %s", attrData.getHrid(), ex));
            }
         }
         monitor.worked(1);
      }

      StringBuffer sbFull = new StringBuffer(AHTML.beginMultiColumnTable(100, 1));
      sbFull.append(AHTML.beginMultiColumnTable(100, 1));
      sbFull.append(AHTML.addHeaderRowMultiColumnTable(columnHeaders));
      for (String string : badData) {
         sbFull.append(AHTML.addRowMultiColumnTable(new String[] {string}));
      }
      sbFull.append(AHTML.endMultiColumnTable());
      XResultData rd = new XResultData();
      rd.addRaw(sbFull.toString());
      rd.report("Fix bad data", Manipulations.RAW_HTML);
   }

   private ArrayList<AttrData> loadAttrData() throws OseeDataStoreException, OseeTypeDoesNotExist {
      ArrayList<AttrData> attrData = new ArrayList<AttrData>();

      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
      try {
         chStmt.runPreparedQuery(GET_ATTRS,
               AttributeTypeManager.getType(WordAttribute.WORD_TEMPLATE_CONTENT).getAttrTypeId());
         while (chStmt.next()) {
            attrData.add(new AttrData(chStmt.getString("gamma_Id"), chStmt.getString("human_readable_id"),
                  chStmt.getString("uri")));
         }
      } finally {
         chStmt.close();
      }
      return attrData;
   }

   private static void uploadResource(String gammaId, Resource resource) throws Exception {
      String fileName = resource.resourceName;
      Map<String, String> parameterMap = new HashMap<String, String>();
      parameterMap.put("sessionId", ClientSessionManager.getSessionId());
      parameterMap.put("is.overwrite.allowed", String.valueOf(true));
      parameterMap.put("protocol", "attr");
      parameterMap.put("seed", gammaId);

      String extension = Lib.getExtension(fileName);
      if (Strings.isValid(extension)) {
         parameterMap.put("extension", extension);
         int charToRemove = extension.length() + 1;
         fileName = fileName.substring(0, fileName.length() - charToRemove);
      }
      parameterMap.put("name", fileName);

      byte[] toUpload = resource.data.getBytes(resource.encoding);
      if (resource.wasZipped) {
         toUpload = Lib.compressStream(new ByteArrayInputStream(toUpload), resource.entryName);
      }

      String urlString =
            HttpUrlBuilder.getInstance().getOsgiServletServiceUrl(OseeServerContext.RESOURCE_CONTEXT, parameterMap);
      HttpProcessor.put(new URL(urlString), new ByteArrayInputStream(toUpload), resource.result.getContentType(),
            resource.result.getEncoding());
   }

   private Resource getResource(String resourcePath) throws OseeCoreException {
      Resource toReturn = null;
      ByteArrayOutputStream sourceOutputStream = new ByteArrayOutputStream();
      try {
         Map<String, String> parameterMap = new HashMap<String, String>();
         parameterMap.put("sessionId", ClientSessionManager.getSessionId());
         parameterMap.put("uri", resourcePath);
         String urlString =
               HttpUrlBuilder.getInstance().getOsgiServletServiceUrl(OseeServerContext.RESOURCE_CONTEXT, parameterMap);

         AcquireResult result = HttpProcessor.acquire(new URL(urlString), sourceOutputStream);
         if (result.getCode() == HttpURLConnection.HTTP_OK) {
            toReturn = new Resource(resourcePath, result, sourceOutputStream.toByteArray());
         }
      } catch (Exception ex) {
         throw new OseeWrappedException(ex);
      } finally {
         try {
            sourceOutputStream.close();
         } catch (IOException ex) {
            throw new OseeWrappedException(ex);
         }
      }
      return toReturn;
   }

   private final class Resource {
      private final String entryName;
      private final String resourceName;
      private final AcquireResult result;
      private final byte[] rawBytes;
      private final boolean wasZipped;
      private final String sourcePath;

      private String data;
      private String encoding;

      private Resource(String sourcePath, AcquireResult result, byte[] rawBytes) throws IOException {
         this.rawBytes = rawBytes;
         this.result = result;
         int index = sourcePath.lastIndexOf('/');
         this.sourcePath = sourcePath;
         this.resourceName = sourcePath.substring(index + 1, sourcePath.length());
         this.wasZipped = result.getContentType().contains("zip");
         if (wasZipped) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            this.entryName = decompressStream(new ByteArrayInputStream(rawBytes), outputStream);
            this.encoding = "UTF-8";
            this.data = new String(outputStream.toByteArray(), encoding);
         } else {
            this.data = new String(rawBytes, result.getEncoding());
            this.entryName = null;
            this.encoding = result.getEncoding();
         }
      }
   }

   private static void backupResourceLocally(File backupFolder, Resource resource) throws IOException {
      InputStream inputStream = null;
      OutputStream outputStream = null;
      try {
         String path = resource.sourcePath.replace("attr://", "");
         path = path.replaceAll("/", File.separator);
         outputStream = new FileOutputStream(new File(backupFolder, path));

         inputStream = new ByteArrayInputStream(resource.rawBytes);
         Lib.inputStreamToOutputStream(inputStream, outputStream);
      } finally {
         if (inputStream != null) {
            inputStream.close();
         }
         if (outputStream != null) {
            outputStream.close();
         }
      }
   }

   private static String decompressStream(InputStream inputStream, OutputStream outputStream) throws IOException {
      String zipEntryName = null;
      ZipInputStream zipInputStream = null;
      try {
         zipInputStream = new ZipInputStream(inputStream);
         ZipEntry entry = zipInputStream.getNextEntry();
         zipEntryName = entry.getName();
         // Transfer bytes from the ZIP file to the output file
         byte[] buf = new byte[1024];
         int len;
         while ((len = zipInputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len);
         }
      } finally {
         if (zipInputStream != null) {
            zipInputStream.close();
         }
      }
      return zipEntryName;
   }

   //To handle the case of sub-sections
   private boolean isBadParagraph(Element paragraph) throws OseeCoreException {
      boolean badParagraph = false;
      String content = paragraph.getTextContent();
      if (content != null && content.contains("LISTNUM \"listreset\"")) {
         badParagraph = true;
      }

      return badParagraph;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#getXWidgetXml()
    */
   @Override
   public String getXWidgetsXml() {
      return emptyXWidgetsXml;
   }

   class AttrData {
      private String gammaId;
      private String hrid;
      private String uri;

      public AttrData(String gammaId, String hrid, String uri) {
         super();
         this.gammaId = gammaId;
         this.hrid = hrid;
         this.uri = uri;
      }

      public String getGammaId() {
         return gammaId;
      }

      public String getHrid() {
         return hrid;
      }

      public String getUri() {
         return uri;
      }
   }
}
