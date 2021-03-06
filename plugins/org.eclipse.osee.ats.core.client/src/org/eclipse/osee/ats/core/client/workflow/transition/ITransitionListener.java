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
package org.eclipse.osee.ats.core.client.workflow.transition;

import java.util.Collection;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Donald G. Dunne
 */
public interface ITransitionListener {

   public void transitioning(TransitionResults results, AbstractWorkflowArtifact sma, IStateToken fromState, IStateToken toState, Collection<? extends IAtsUser> toAssignees) throws OseeCoreException;

   public void transitioned(AbstractWorkflowArtifact sma, IStateToken fromState, IStateToken toState, Collection<? extends IAtsUser> toAssignees, SkynetTransaction transaction) throws OseeCoreException;

}
