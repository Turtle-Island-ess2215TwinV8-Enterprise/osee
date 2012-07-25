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

import org.eclipse.osee.orcs.core.internal.relation.experimental.RelationCollection;
import org.eclipse.osee.orcs.data.CanDelete;
import org.eclipse.osee.orcs.data.HasLocalId;

/**
 * @author Roberto E. Escobar
 */
public interface HasRelations extends CanDelete, HasLocalId {

   RelationCollection getRelations();

}