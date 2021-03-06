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
package org.eclipse.osee.ats.api.rule;

import java.util.Collection;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public interface IAtsRules {

   public Collection<String> getRules() throws OseeCoreException;

   public void addRule(String rule) throws OseeCoreException;

   public boolean hasRule(String rule) throws OseeCoreException;

   public void removeRule(String rule) throws OseeCoreException;

}
