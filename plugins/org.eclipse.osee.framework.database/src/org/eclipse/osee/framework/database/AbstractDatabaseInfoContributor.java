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
package org.eclipse.osee.framework.database;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.osee.framework.core.data.IDatabaseInfo;
import org.eclipse.osee.framework.database.core.IDbConnectionInformationContributor;
import org.eclipse.osee.framework.database.internal.parser.DbConfigParser;
import org.eclipse.osee.framework.jdk.core.util.xml.Jaxp;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractDatabaseInfoContributor implements IDbConnectionInformationContributor {

   protected IDatabaseInfo[] readFromXml(InputStream inputStream) throws IOException, ParserConfigurationException, SAXException {
      try {
         Document document = Jaxp.readXmlDocument(inputStream, "UTF-8");
         Element rootElement = document.getDocumentElement();
         return DbConfigParser.parse(rootElement);
      } finally {
         if (inputStream != null) {
            inputStream.close();
         }
      }
   }
}
