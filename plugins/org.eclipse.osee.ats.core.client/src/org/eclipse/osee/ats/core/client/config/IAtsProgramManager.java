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
package org.eclipse.osee.ats.core.client.config;

import java.util.Collection;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.IOperation;

/**
 * @author Donald G. Dunne
 */
public interface IAtsProgramManager {

   public String getName(TeamWorkFlowArtifact teamArt);

   public boolean isApplicable(TeamWorkFlowArtifact teamArt);

   public IOperation createValidateReqChangesOp(TeamWorkFlowArtifact teamArt) throws OseeCoreException;

   public String getName();

   public Collection<? extends IAtsProgram> getPrograms() throws OseeCoreException;

   public void reloadCache() throws OseeCoreException;

   public String getXProgramComboWidgetName();

   public IAtsProgram getProgram(TeamWorkFlowArtifact teamArt) throws OseeCoreException;

   public IArtifactToken getWcafeReviewAssigneeUserGroup() throws OseeCoreException;

   public IArtifactToken getPidsReviewAssigneeUserGroup() throws OseeCoreException;

}
