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
package org.eclipse.osee.ote.core;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * @author Andrew M. Finkbeiner
 */
public class OseeURLClassLoader extends URLClassLoader {

   private final String name;

   public OseeURLClassLoader(String name, URL[] urls, ClassLoader parent) {
      super(urls, parent);
      this.name = name;
      GCHelper.getGCHelper().addRefWatch(this);

   }

   public OseeURLClassLoader(String name, URL[] urls) {
      super(urls);
      GCHelper.getGCHelper().addRefWatch(this);
      this.name = name;
   }

   public OseeURLClassLoader(String name, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
      super(urls, parent, factory);
      GCHelper.getGCHelper().addRefWatch(this);
      this.name = name;
   }

   @Override
   public String toString() {
      return this.getClass().getName() + " [ " + name + " ] ";
   }
}
