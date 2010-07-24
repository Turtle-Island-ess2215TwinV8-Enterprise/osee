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
package org.eclipse.osee.ote.core.framework.saxparse.elements;

import org.eclipse.osee.ote.core.framework.saxparse.ElementHandlers;
import org.xml.sax.Attributes;

/**
 * @author Andrew M. Finkbeiner
 *
 */
public class Summary extends ElementHandlers{

   /**
    * @param name
    */
   public Summary() {
      super("Summary");
   }

   @Override
   public Object createStartElementFoundObject(String uri, String localName, String name, Attributes attributes) {
      return new SummaryData(attributes.getValue("CRITICAL_COUNT"), attributes.getValue("EXCEPTION_COUNT"), attributes.getValue("INFORMATIONAL_COUNT"), attributes.getValue("MINOR_COUNT"), attributes.getValue("NODE_ID"), attributes.getValue("SERIOUS_COUNT"), attributes.getValue("START_NUMBER"));
   }

}
