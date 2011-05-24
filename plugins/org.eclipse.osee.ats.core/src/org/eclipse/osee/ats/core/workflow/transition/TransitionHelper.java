/*
 * Created on May 23, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.workflow.transition;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class TransitionHelper extends TransitionHelperAdapter {

   private final String cancellationReason;
   private final Collection<? extends AbstractWorkflowArtifact> awas;
   private final String name;
   private TransitionOption[] transitionOption;
   private final Collection<? extends IBasicUser> toAssignees;
   private String toStateName;

   public TransitionHelper(String name, Collection<? extends AbstractWorkflowArtifact> awas, String toStateName, Collection<? extends IBasicUser> toAssignees, String cancellationReason, TransitionOption... transitionOption) {
      this.name = name;
      this.awas = awas;
      this.toStateName = toStateName;
      this.toAssignees = toAssignees;
      this.cancellationReason = cancellationReason;
      this.transitionOption = transitionOption;
   }

   @Override
   public boolean isPriviledgedEditEnabled() {
      return Collections.getAggregate(transitionOption).contains(TransitionOption.PriviledgedEditEnabled);
   }

   @Override
   public boolean isOverrideAssigneeCheck() {
      return Collections.getAggregate(transitionOption).contains(TransitionOption.OverrideAssigneeCheck);
   }

   @Override
   public boolean isOverrideTransitionValidityCheck() {
      return Collections.getAggregate(transitionOption).contains(TransitionOption.OverrideTransitionValidityCheck);
   }

   @Override
   public Collection<? extends AbstractWorkflowArtifact> getAwas() {
      return awas;
   }

   @Override
   public Result getCompleteOrCancellationReason() {
      if (Strings.isValid(cancellationReason)) {
         return new Result(true, cancellationReason);
      }
      return Result.FalseResult;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public Collection<? extends IBasicUser> getToAssignees() {
      return toAssignees;
   }

   @Override
   public Result handleExtraHoursSpent() {
      return Result.TrueResult;
   }

   @Override
   public String getToStateName() {
      return toStateName;
   }

   public void addTransitionOption(TransitionOption transitionOption) {
      List<TransitionOption> options = Collections.getAggregate(this.transitionOption);
      if (!options.contains(transitionOption)) {
         options.add(transitionOption);
      }
      this.transitionOption = options.toArray(new TransitionOption[options.size()]);
   }

   public void removeTransitionOption(TransitionOption transitionOption) {
      List<TransitionOption> options = Collections.getAggregate(this.transitionOption);
      if (options.contains(transitionOption)) {
         options.remove(transitionOption);
      }
      this.transitionOption = options.toArray(new TransitionOption[options.size()]);
   }

   public void setToStateName(String toStateName) {
      this.toStateName = toStateName;
   }

}