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
package org.eclipse.osee.orcs.db.internal.search.tagger;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.MatchLocation;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.data.AttributeReadable;

/**
 * @author Roberto E. Escobar
 */
public class DefaultAttributeTagger extends BaseAttributeTagger {

   public DefaultAttributeTagger(TagProcessor tagProcessor, StreamMatcher matcher) {
      super(tagProcessor, matcher);
   }

   @Override
   public void tagIt(AttributeReadable<?> attribute, TagCollector collector) throws OseeCoreException {
      InputStream inputStream = null;
      try {
         inputStream = getValueAsStream(attribute);
         getTagProcessor().collectFromInputStream(inputStream, collector);
      } finally {
         Lib.close(inputStream);
      }
   }

   @Override
   public List<MatchLocation> find(AttributeReadable<?> attribute, String toSearch, boolean matchAllLocations, QueryOption... options) throws OseeCoreException {
      List<MatchLocation> toReturn;
      if (Strings.isValid(toSearch)) {
         InputStream inputStream = null;
         try {
            inputStream = getValueAsStream(attribute);
            toReturn = getMatcher().findInStream(inputStream, toSearch, matchAllLocations, options);
         } finally {
            Lib.close(inputStream);
         }
      } else {
         toReturn = Collections.emptyList();
      }
      return toReturn;
   }
}
