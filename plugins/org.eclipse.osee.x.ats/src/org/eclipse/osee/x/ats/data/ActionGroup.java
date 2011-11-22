/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.x.ats.data;

import java.util.Collection;

/**
 * Client Model Correlation: Action
 * 
 * @author Roberto E. Escobar
 * @author Donald G. Dunne
 */
public interface ActionGroup extends AtsObject {

   Collection<Action> getTeams();
}
