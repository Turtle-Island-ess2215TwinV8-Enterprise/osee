/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.exchange.transform;

import java.util.HashMap;
import javax.xml.stream.XMLStreamException;
import org.eclipse.osee.framework.jdk.core.util.io.xml.SaxTransformer;
import org.xml.sax.Attributes;

/**
 * @author Ryan D. Brooks
 */
public class V0_9_0ItemTransformer extends SaxTransformer {
   private final HashMap<Integer, String> typeIdMap;
   private final String typeIdColumn;

   public V0_9_0ItemTransformer(HashMap<Integer, String> typeIdMap, String typeIdColumn) {
      this.typeIdMap = typeIdMap;
      this.typeIdColumn = typeIdColumn;
   }

   @Override
   public void startElementFound(String uri, String localName, String qName, Attributes attributes) throws XMLStreamException {
      writer.writeStartElement(localName);
      for (int i = 0; i < attributes.getLength(); i++) {
         if (attributes.getLocalName(i).equals(typeIdColumn)) {
            writer.writeAttribute("type_guid", typeIdMap.get(Integer.parseInt(attributes.getValue(i))));
         } else {
            writer.writeAttribute(attributes.getLocalName(i), attributes.getValue(i));
         }
      }
   }
}