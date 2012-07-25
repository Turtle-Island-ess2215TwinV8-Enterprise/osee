/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.relation.experimental.interfaces;

import java.util.List;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Roberto E. Escobar
 */
public interface Graph {

   ArtifactReadable getParent(IOseeBranch branch, HasRelations child) throws OseeCoreException;

   List<ArtifactReadable> getChildren(IOseeBranch branch, HasRelations parent) throws OseeCoreException;

   List<ArtifactReadable> getRelated(IOseeBranch branch, HasRelations hasRelations, IRelationTypeSide relationTypeSide) throws OseeCoreException;

   void unrelate(IOseeBranch branch, HasRelations hasRelations) throws OseeCoreException;

}