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
package org.eclipse.osee.connection.service;

import java.io.File;

/**
 * @author Ken J. Aguilar
 */
class LocalFileKey implements IFileKey {

   private final File file;

   /**
    * @param file
    */
   LocalFileKey(File file) {
      this.file = file;
   }

   File getFile() {
      return file;
   }
}
