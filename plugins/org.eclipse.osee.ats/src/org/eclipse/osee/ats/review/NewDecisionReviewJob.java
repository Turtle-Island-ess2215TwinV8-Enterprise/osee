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

package org.eclipse.osee.ats.review;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.ats.AtsOpenOption;
import org.eclipse.osee.ats.core.review.DecisionReviewArtifact;
import org.eclipse.osee.ats.core.review.DecisionReviewManager;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.workdef.DecisionReviewOption;
import org.eclipse.osee.ats.core.workdef.ReviewBlockType;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.model.IBasicUser;

/**
 * @author Donald G. Dunne
 */
public class NewDecisionReviewJob extends Job {
   private final TeamWorkFlowArtifact teamParent;
   private final ReviewBlockType reviewBlockType;
   private final String reviewTitle;
   private final String againstState;
   private final List<DecisionReviewOption> options;
   private final Collection<IBasicUser> assignees;
   private final String description;
   private final Date createdDate;
   private final IBasicUser createdBy;

   public NewDecisionReviewJob(TeamWorkFlowArtifact teamParent, ReviewBlockType reviewBlockType, String reviewTitle, String againstState, String description, List<DecisionReviewOption> options, Collection<IBasicUser> assignees, Date createdDate, IBasicUser createdBy) {
      super("Creating New Decision Review");
      this.teamParent = teamParent;
      this.reviewTitle = reviewTitle;
      this.againstState = againstState;
      this.reviewBlockType = reviewBlockType;
      this.description = description;
      this.options = options;
      this.assignees = assignees;
      this.createdDate = createdDate;
      this.createdBy = createdBy;
   }

   @Override
   public IStatus run(final IProgressMonitor monitor) {
      try {
         DecisionReviewArtifact decArt =
            DecisionReviewManager.createNewDecisionReview(teamParent, reviewBlockType, reviewTitle, againstState,
               description, options, assignees, createdDate, createdBy);
         decArt.persist(getClass().getSimpleName());
         AtsUtil.openATSAction(decArt, AtsOpenOption.OpenOneOrPopupSelect);
      } catch (Exception ex) {
         monitor.done();
         return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Error creating Decision Review", ex);
      } finally {
         monitor.done();
      }
      return Status.OK_STATUS;
   }

}