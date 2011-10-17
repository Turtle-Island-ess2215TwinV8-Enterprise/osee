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
package org.eclipse.osee.orcs.search;

import java.util.List;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Ryan D. Brooks
 * @author Roberto E. Escobar
 */
public interface ResultSet<T> {

   T getOneOrNull() throws OseeCoreException;

   T getExactlyOne() throws OseeCoreException;

   List<T> getList() throws OseeCoreException;

   Iterable<T> getIterable(int fetchSize) throws OseeCoreException;
}