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
package org.eclipse.osee.x.ats.future.config;

import org.eclipse.osee.x.ats.data.HasVersions;
import org.eclipse.osee.x.ats.future.user.HasLeads;
import org.eclipse.osee.x.ats.future.user.HasMembers;

/**
 * Represents the Team responsible for performing IWork.<br>
 * <br>
 * Old Model: TeamDefinition
 * 
 * @author Donald G. Dunne
 */
public interface XXXTeamDefinition extends ConfigObject, HasVersions, HasWorkDefinition, HasMembers, HasLeads {
   //
}
