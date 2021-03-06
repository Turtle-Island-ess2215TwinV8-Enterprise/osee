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
package org.eclipse.osee.ats.editor;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.client.workflow.transition.TransitionStatusData;
import org.eclipse.osee.ats.util.widgets.dialog.TransitionStatusDialog;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;

/**
 * @author Donald G. Dunne
 */
public class SMAPromptChangeHoursSpent {

   private final Collection<? extends AbstractWorkflowArtifact> awas;

   public SMAPromptChangeHoursSpent(AbstractWorkflowArtifact awa) {
      this(Arrays.asList(awa));
   }

   public SMAPromptChangeHoursSpent(final Collection<? extends AbstractWorkflowArtifact> awas) {
      this.awas = awas;
   }

   public static boolean promptChangeStatus(Collection<? extends AbstractWorkflowArtifact> awas, boolean persist) throws OseeCoreException {
      SMAPromptChangeHoursSpent promptChangeStatus = new SMAPromptChangeHoursSpent(awas);
      return promptChangeStatus.promptChangeStatus(persist).isTrue();
   }

   public Result promptChangeStatus(boolean persist) throws OseeCoreException {
      Result result = SMAPromptChangeStatus.isValidToChangeStatus(awas);
      if (result.isFalse()) {
         AWorkbench.popup(result);
         return result;
      }

      TransitionStatusData data = new TransitionStatusData(awas, false);
      TransitionStatusDialog dialog =
         new TransitionStatusDialog("Enter Hours Spent",
            "Enter percent complete and number of hours you spent since last status.", data);
      if (dialog.open() == 0) {
         SMAPromptChangeStatus.performChangeStatus(awas, null, data.getAdditionalHours(), data.getPercent(),
            data.isSplitHoursBetweenItems(), persist);
         return Result.TrueResult;
      }
      return Result.FalseResult;
   }

}
