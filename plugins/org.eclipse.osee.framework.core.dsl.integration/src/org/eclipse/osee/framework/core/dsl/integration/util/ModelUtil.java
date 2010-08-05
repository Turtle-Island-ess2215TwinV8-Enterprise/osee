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
package org.eclipse.osee.framework.core.dsl.integration.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.diff.metamodel.ComparisonSnapshot;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.eclipse.osee.framework.core.dsl.OseeDslStandaloneSetup;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDsl;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.exception.OseeWrappedException;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import com.google.inject.Injector;

/**
 * @author Roberto E. Escobar
 */
public final class ModelUtil {

   private ModelUtil() {
      // Utility Class
   }

   public static OseeDsl loadModel(String uri, String xTextData) throws OseeCoreException {
      try {
         OseeDslStandaloneSetup setup = new OseeDslStandaloneSetup();
         Injector injector = setup.createInjectorAndDoEMFRegistration();
         XtextResourceSet set = injector.getInstance(XtextResourceSet.class);

         //         set.setClasspathURIContext(ModelUtil.class);
         set.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);

         Resource resource = set.createResource(URI.createURI(uri));
         resource.load(new ByteArrayInputStream(xTextData.getBytes("UTF-8")), set.getLoadOptions());
         OseeDsl model = (OseeDsl) resource.getContents().get(0);
         for (Diagnostic diagnostic : resource.getErrors()) {
            throw new OseeStateException(diagnostic.toString());
         }
         return model;
      } catch (IOException ex) {
         throw new OseeWrappedException(ex);
      }
   }

   public static void saveModel(OseeDsl model, String uri, OutputStream outputStream, boolean isZipped) throws IOException {
      OseeDslStandaloneSetup.doSetup();

      ResourceSet resourceSet = new ResourceSetImpl();
      Resource resource = resourceSet.createResource(URI.createURI(uri));
      resource.getContents().add(model);

      Map<String, Boolean> options = new HashMap<String, Boolean>();
      //		options.put(XtextResource.OPTION_FORMAT, Boolean.TRUE);
      if (isZipped) {
         options.put(Resource.OPTION_ZIP, Boolean.TRUE);
      }
      SaveOptions saveOptions = SaveOptions.getOptions(options);
      resource.save(outputStream, saveOptions.toOptionsMap());
   }

   private static void storeModel(Resource resource, OutputStream outputStream, EObject object, String uri, Map<String, Boolean> options) throws OseeCoreException {
      try {
         resource.setURI(URI.createURI(uri));
         resource.getContents().add(object);
         resource.save(outputStream, options);
      } catch (IOException ex) {
         throw new OseeWrappedException(ex);
      }
   }

   public static String modelToStringXML(EObject object, String uri, Map<String, Boolean> options) throws OseeCoreException {
      return modelToString(new XMLResourceImpl(), object, uri, options);
   }

   public static String modelToStringXText(EObject object, String uri, Map<String, Boolean> options) throws OseeCoreException {
      OseeDslStandaloneSetup setup = new OseeDslStandaloneSetup();
      Injector injector = setup.createInjectorAndDoEMFRegistration();
      Resource resource = injector.getInstance(XtextResource.class);
      Map<String, Boolean> options2 = new HashMap<String, Boolean>();
      options2.put(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
      return modelToString(resource, object, uri, options2);
   }

   private static String modelToString(Resource resource, EObject object, String uri, Map<String, Boolean> options) throws OseeCoreException {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      storeModel(resource, outputStream, object, uri, options);
      try {
         return outputStream.toString("UTF-8");
      } catch (UnsupportedEncodingException ex) {
         throw new OseeWrappedException(ex);
      }
   }

   public static ComparisonSnapshot loadComparisonSnapshot(String compareName, String compareData) throws OseeCoreException {
      ComparisonSnapshot snapshot = null;
      try {
         ResourceSet resourceSet = new ResourceSetImpl();
         Resource resource = resourceSet.createResource(URI.createURI(compareName));
         resource.load(new ByteArrayInputStream(compareData.getBytes("UTF-8")), resourceSet.getLoadOptions());
         snapshot = (ComparisonSnapshot) resource.getContents().get(0);
      } catch (IOException ex) {
         throw new OseeWrappedException(ex);
      }
      return snapshot;
   }

}