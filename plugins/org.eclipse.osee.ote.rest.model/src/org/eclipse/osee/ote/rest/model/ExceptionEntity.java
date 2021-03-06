/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ote.rest.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author John Misinco
 */
@XmlRootElement
public class ExceptionEntity {

   private String exceptionString;

   public ExceptionEntity(String exceptionString) {
      this.exceptionString = exceptionString;
   }

   public ExceptionEntity() {
      // do nothing
   }

   public String getExceptionString() {
      return exceptionString;
   }

   public void setExceptionString(String exceptionString) {
      this.exceptionString = exceptionString;
   }

}
