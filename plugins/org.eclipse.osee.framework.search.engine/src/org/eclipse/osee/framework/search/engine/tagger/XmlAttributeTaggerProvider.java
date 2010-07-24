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
package org.eclipse.osee.framework.search.engine.tagger;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.io.xml.XmlTextInputStream;
import org.eclipse.osee.framework.search.engine.MatchLocation;
import org.eclipse.osee.framework.search.engine.SearchOptions;
import org.eclipse.osee.framework.search.engine.attribute.AttributeData;
import org.eclipse.osee.framework.search.engine.utility.ITagCollector;
import org.eclipse.osee.framework.search.engine.utility.TagProcessor;
import org.eclipse.osee.framework.search.engine.utility.WordOrderMatcher;

/**
 * @author Roberto E. Escobar
 */
public class XmlAttributeTaggerProvider extends BaseAttributeTaggerProvider {

   @Override
   public List<MatchLocation> find(AttributeData attributeData, String toSearch, SearchOptions options) throws OseeCoreException {
      if (Strings.isValid(toSearch)) {
         InputStream inputStream = null;
         try {
            inputStream = new XmlTextInputStream(getValueAsStream(attributeData));
            return WordOrderMatcher.findInStream(inputStream, toSearch, options);
         } finally {
            Lib.close(inputStream);
         }
      }
      return Collections.emptyList();
   }

   public void tagIt(AttributeData attributeData, ITagCollector collector) throws OseeCoreException {
      InputStream inputStream = null;
      try {
         inputStream = getValueAsStream(attributeData);
         TagProcessor.collectFromInputStream(new XmlTextInputStream(inputStream), collector);
      } finally {
         Lib.close(inputStream);
      }
   }
}
