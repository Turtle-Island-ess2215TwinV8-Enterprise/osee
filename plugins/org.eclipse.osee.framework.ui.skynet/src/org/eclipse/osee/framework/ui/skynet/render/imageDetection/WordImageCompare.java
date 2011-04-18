/*
 * Created on Apr 14, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.render.imageDetection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.DatatypeConverter;

/**
 * @author Jeff C. phillips
 */
public class WordImageCompare {

   /**
    * Compares images by detecting if they are compressed and using GZIPInputStream to deflate them or compares images
    * by using the equals method.
    */
   public boolean compareFiles(String firstFile, String secondFile) throws IOException {
      return handleCompare(firstFile, secondFile);
   }

   private boolean handleCompare(String firstFile, String secondFile) throws IOException {
      boolean isEqual = false;
      EMZHtmlImageHandler handler = new EMZHtmlImageHandler();

      InputStream firstStream = getInputStream(firstFile);
      InputStream secondStream = getInputStream(secondFile);

      if (handler.isValid(firstStream) && handler.isValid(secondStream)) {
         isEqual = compareGZIPImages(getUncompressedByteArray(firstStream), getUncompressedByteArray(secondStream));
      } else {
         isEqual = firstFile.equals(secondFile);
      }
      return isEqual;
   }

   private InputStream getInputStream(String file) {
      byte[] data = DatatypeConverter.parseBase64Binary(file);
      return new ByteArrayInputStream(data);
   }

   private byte[] getUncompressedByteArray(InputStream inputStream) throws IOException {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      EMZHtmlImageHandler handler = new EMZHtmlImageHandler();
      handler.convert(inputStream, outputStream);
      return outputStream.toByteArray();
   }

   private boolean compareGZIPImages(byte[] firstbytes, byte[] secondbytes) {
      boolean isEqual = false;

      if (firstbytes.length == secondbytes.length && secondbytes.length > 0) {
         isEqual = true;

         for (int i = 0; i < firstbytes.length; i++) {
            isEqual &= firstbytes[i] == secondbytes[i];
         }
      }
      return isEqual;
   }
}