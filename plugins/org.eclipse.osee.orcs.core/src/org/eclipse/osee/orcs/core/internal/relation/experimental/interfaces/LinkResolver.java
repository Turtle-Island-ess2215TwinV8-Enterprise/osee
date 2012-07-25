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

import java.util.Collection;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public interface LinkResolver<S, T> {

   T resolve(S source, Linker<T> linker) throws OseeCoreException;

   void resolve(S source, HasLinkers<T>... links) throws OseeCoreException;

   void resolve(S source, RelationSide side, HasLinkers<T>... links) throws OseeCoreException;

   void resolve(S source, Collection<? extends HasLinkers<T>> links) throws OseeCoreException;

   void resolve(S source, RelationSide side, Collection<? extends HasLinkers<T>> links) throws OseeCoreException;

}