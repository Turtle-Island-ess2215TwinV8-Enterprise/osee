package org.eclipse.osee.framework.ui.workspacebundleloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.osee.framework.jdk.core.util.ChecksumUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class BundleInfoLite {

//   enum OPERATION {
//	   NOOP,NEW,UPDATE,REMOVE 
//   }
	
	
   private final String symbolicName;
   private final String version;
   private final URL systemLocation;
   private final File file;
   private final Manifest manifest;
   private byte[] md5Digest;
//   private OPERATION operation;
private Bundle bundle;
   
   public BundleInfoLite(URL systemLocation) throws IOException {
      File tmpFile;
      try {
         tmpFile = new File(systemLocation.toURI());
      } catch (URISyntaxException ex) {
         tmpFile = new File(systemLocation.getPath());
      }
      this.file = tmpFile;

      JarFile jarFile = new JarFile(file);
      this.manifest = jarFile.getManifest();
      this.symbolicName = generateBundleName(manifest);
      this.version = manifest.getMainAttributes().getValue("Bundle-Version");

      this.systemLocation = systemLocation;
      this.md5Digest = null;
   }

   /**
    * @return the name of the bundle
    */
   private String generateBundleName(Manifest jarManifest) {
      String nameEntry = jarManifest.getMainAttributes().getValue("Bundle-SymbolicName");
	  if(nameEntry == null){
	     return "unknown";
	  }
      // Sometimes there's a semicolon then extra info - ignore this
      int index = nameEntry.indexOf(';');
      if (index != -1) {
         nameEntry = nameEntry.substring(0, index);
      }

      return nameEntry;
   }

   /**
    * @return the symbolicName
    */
   public String getSymbolicName() {
      return symbolicName;
   }

   /**
    * @return the version
    */
   public String getVersion() {
      return version;
   }

   /**
    * @return the location
    */
   public URL getSystemLocation() {
      return systemLocation;
   }

   @Override
   public String toString() {
      return getSymbolicName() + ":" + getVersion();
   }

   /**
    * @return the md5Digest
    */
   public byte[] getMd5Digest() {
      // Do lazy calculation of this since it can be costly
      // and does not get read for all bundle info's
      if (md5Digest == null) {
         try {
            InputStream in = systemLocation.openStream();

            md5Digest = ChecksumUtil.createChecksum(in, "MD5");

            in.close();
         } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Always expect MD5 to be available", ex);
         } catch (IOException ex) {
            throw new IllegalStateException("Always expect local jar file to be available", ex);
         }
      }
      return md5Digest;
   }
   
//   void setOperation(OPERATION operation){
//	   this.operation = operation;
//   }
//   
//   OPERATION getOperation(){
//	   return this.operation;
//   }
   
   public void install(BundleContext context) throws BundleException, IOException{
	   bundle = context.installBundle(this.systemLocation.getFile(), this.getSystemLocation().openStream());
   }
   
   public Bundle uninstall() throws BundleException{
	   bundle.uninstall();
	   System.out.println("uninstall" + toString());
	   return bundle;
   }
   
   public boolean isInstalled() {
	   if(bundle == null){
		   return false;
	   } else {
		   int state = bundle.getState();
		   return state != Bundle.UNINSTALLED;
	   }
   }
   
   public boolean isStarted() {
	   if(isInstalled()){
		   int state = bundle.getState();
		   return state == Bundle.ACTIVE || state == Bundle.STARTING;
	   } else {
		   return false;
	   }
   }
   
   public void start() throws BundleException{
	   if(bundle.getState() == Bundle.INSTALLED || bundle.getState() == Bundle.RESOLVED){
		   bundle.start();
	   }
   }
   
}