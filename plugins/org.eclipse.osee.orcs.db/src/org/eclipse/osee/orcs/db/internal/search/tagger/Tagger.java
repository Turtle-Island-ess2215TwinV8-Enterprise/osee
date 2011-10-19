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

import java.util.List;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.MatchLocation;
import org.eclipse.osee.orcs.data.ReadableAttribute;
import org.eclipse.osee.orcs.search.CaseType;

/**
 * @author Roberto E. Escobar
 */
public interface Tagger {

   void tagIt(ReadableAttribute<?> attribute, TagCollector collector) throws OseeCoreException;

   List<MatchLocation> find(ReadableAttribute<?> attribute, String toSearch, CaseType caseType, boolean matchAllLocations) throws OseeCoreException;

}