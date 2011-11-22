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
package org.eclipse.osee.ats.core.workflow;

import org.eclipse.osee.ats.shared.AtsAttributeTypes;

/**
 * @author Donald G. Dunne
 */
public class XStateDam extends XStateAssigneesDam {

   public XStateDam(AbstractWorkflowArtifact sma) {
      super(sma, AtsAttributeTypes.State);
   }

}
