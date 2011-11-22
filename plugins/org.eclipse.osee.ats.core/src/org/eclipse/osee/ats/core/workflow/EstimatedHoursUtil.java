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
package org.eclipse.osee.ats.core.workflow;

import org.eclipse.osee.ats.core.action.ActionManager;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;

/**
 * @author Donald G. Dunne
 */
public class EstimatedHoursUtil {

   public static double getEstimatedHours(Object object) throws OseeCoreException {
      if (object instanceof AbstractWorkflowArtifact) {
         return ((AbstractWorkflowArtifact) object).getEstimatedHoursTotal();
      } else if (Artifacts.isOfType(object, AtsArtifactTypes.Action)) {
         double total = 0;
         for (Artifact team : ActionManager.getTeams(object)) {
            total += getEstimatedHours(team);
         }
         return total;
      }
      return 0.0;
   }

}
