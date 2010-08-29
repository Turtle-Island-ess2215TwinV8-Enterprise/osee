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
package org.eclipse.osee.framework.resource.management;

/**
 * @author Roberto E. Escobar
 */
public interface IResourceListener {

   /**
    * Event triggered before a resource is deleted
    * 
    */
   public void onPreDelete(IResourceLocator locator);

   /**
    * Event triggered after a resource is deleted
    * 
    */
   public void onPostDelete(IResourceLocator locator);

   /**
    * Event triggered before a resource is saved
    * 
    */
   public void onPreSave(IResourceLocator locator, IResource resource, Options options);

   /**
    * Event triggered after a resource is saved
    * 
    */
   public void onPostSave(IResourceLocator locator, IResource resource, Options options);

   /**
    * Event triggered before a resource is acquired
    * 
    */
   public void onPreAcquire(IResourceLocator locator);

   /**
    * Event triggered after a resource is acquired
    * 
    */
   public void onPostAcquire(IResource resource);

}
