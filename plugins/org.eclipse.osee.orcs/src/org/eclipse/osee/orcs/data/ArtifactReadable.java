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
package org.eclipse.osee.orcs.data;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.Identifiable;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 * @author Andrew M. Finkbeiner
 */
public interface ArtifactReadable extends Identifiable, HasLocalId, AttributesReadable {

   IOseeBranch getBranch() throws OseeCoreException;

   String getHumanReadableId();

   boolean isDeleted();

   int getTransactionId();

   IArtifactType getArtifactType() throws OseeCoreException;

   boolean isOfType(IArtifactType... otherTypes) throws OseeCoreException;

}