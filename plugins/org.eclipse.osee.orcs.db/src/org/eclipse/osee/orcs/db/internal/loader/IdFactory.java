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
package org.eclipse.osee.orcs.db.internal.loader;

import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public interface IdFactory {

   int getBranchId(IOseeBranch branch) throws OseeCoreException;

   int getNextArtifactId() throws OseeCoreException;

   int getNextAttributeId() throws OseeCoreException;

   int getNextRelationId() throws OseeCoreException;

   String getUniqueGuid(String guid) throws OseeCoreException;

   String getUniqueHumanReadableId(String humanReadableId) throws OseeCoreException;

   long getNextGammaId() throws OseeCoreException;

}
