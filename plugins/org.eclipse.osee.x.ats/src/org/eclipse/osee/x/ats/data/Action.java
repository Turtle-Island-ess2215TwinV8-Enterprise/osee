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

/**
 * Represents the Work to be done for related Team for related ActionableItems<br>
 * <br>
 * Client Model Correlation: TeamWorkflow
 * 
 * @author Roberto E. Escobar
 * @author Donald G. Dunne
 */
public interface Action extends WorkUnit, HasAssignees, HasTargetedVersion, HasProduct, HasTasks, HasActionableItems, HasTeam, HasWorkingBranch {

   @Override
   XXXVersion getTargetedVersion();

   XXXProduct getProduct();

   ActionGroup getAction();

   @Override
   XXXTeamDefinition getTeamDefinition();
}
