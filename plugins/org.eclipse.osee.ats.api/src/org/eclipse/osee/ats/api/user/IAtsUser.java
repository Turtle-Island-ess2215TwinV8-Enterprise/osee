/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.user;

import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public interface IAtsUser extends IAtsObject, Comparable<Object> {

   @Override
   public String getName();

   public String getUserId() throws OseeCoreException;

   public String getEmail() throws OseeCoreException;

   public boolean isActive() throws OseeCoreException;
}
