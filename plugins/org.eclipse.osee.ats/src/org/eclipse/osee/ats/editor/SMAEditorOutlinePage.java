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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.workdef.CompositeStateItem;
import org.eclipse.osee.ats.workdef.DecisionReviewDefinition;
import org.eclipse.osee.ats.workdef.DecisionReviewOption;
import org.eclipse.osee.ats.workdef.PeerReviewDefinition;
import org.eclipse.osee.ats.workdef.RuleDefinition;
import org.eclipse.osee.ats.workdef.StateDefinition;
import org.eclipse.osee.ats.workdef.StateItem;
import org.eclipse.osee.ats.workdef.WidgetDefinition;
import org.eclipse.osee.ats.workdef.WidgetOption;
import org.eclipse.osee.ats.workdef.WorkDefinition;
import org.eclipse.osee.ats.workdef.WorkDefinitionMatch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * @author Donald G. Dunne
 */
public class SMAEditorOutlinePage extends ContentOutlinePage {

   private SMAEditor editor;

   @Override
   public void createControl(Composite parent) {
      super.createControl(parent);

      Tree tree = getTreeViewer().getTree();
      tree.setLayout(new FillLayout(SWT.VERTICAL));
      getTreeViewer().setContentProvider(new InternalContentProvider(editor));
      getTreeViewer().setLabelProvider(new InternalLabelProvider());
      setInput(editor != null ? editor : "No Input Available");

      getSite().getActionBars().getToolBarManager().add(
         new Action("Refresh", ImageManager.getImageDescriptor(PluginUiImage.REFRESH)) {
            @Override
            public void run() {
               refresh();
            }
         });
      getSite().getActionBars().getToolBarManager().update(true);
   }

   @Override
   public void selectionChanged(SelectionChangedEvent event) {
      ISelection selection = event.getSelection();
      if (selection instanceof IStructuredSelection) {
         IStructuredSelection sSelection = (IStructuredSelection) selection;
         if (!sSelection.isEmpty()) {
            System.out.println("SMA Selection");
         }
      }
   }

   public void setInput(Object input) {
      if (input instanceof SMAEditor) {
         this.editor = (SMAEditor) input;
         if (getTreeViewer() != null) {
            if (editor != null) {
               getTreeViewer().setInput(editor);
               StateDefinition stateDef = (editor).getSma().getCurrentAtsWorkPage().getStateDefinition();
               StructuredSelection newSelection = new StructuredSelection(Arrays.asList(stateDef));
               getTreeViewer().expandToLevel((editor).getSma(), 2);
               getTreeViewer().expandToLevel(stateDef, 1);
               getTreeViewer().setSelection(newSelection);
            }
         }
      }
   }

   public void refresh() {
      TreeViewer viewer = getTreeViewer();
      if (viewer != null && Widgets.isAccessible(viewer.getTree())) {
         viewer.refresh();
      }
   }

   private final class InternalLabelProvider extends LabelProvider {

      @Override
      public String getText(Object element) {
         if (element instanceof SMAEditor) {
            return ((SMAEditor) element).getTitle();
         }
         return String.valueOf(element);
      }

      @Override
      public Image getImage(Object element) {
         if (element instanceof SMAEditor) {
            return ((SMAEditor) element).getTitleImage();
         } else if (element instanceof AbstractWorkflowArtifact) {
            return ArtifactImageManager.getImage((AbstractWorkflowArtifact) element);
         } else if (element instanceof StateDefinition) {
            return ImageManager.getImage(AtsImage.STATE_DEFINITION);
         } else if (element instanceof WrappedTrace) {
            return ImageManager.getImage(AtsImage.TRACE);
         } else if (element instanceof WorkDefinitionMatch) {
            return ImageManager.getImage(AtsImage.WORKFLOW_CONFIG);
         } else if (element instanceof WidgetDefinition) {
            return ImageManager.getImage(FrameworkImage.GEAR);
         } else if (element instanceof CompositeStateItem || element instanceof WrappedLayout) {
            return ImageManager.getImage(AtsImage.COMPOSITE_STATE_ITEM);
         } else if (element instanceof String || element instanceof WidgetOption) {
            return ImageManager.getImage(AtsImage.RIGHT_ARROW_SM);
         } else if (element instanceof WrappedStates || element instanceof WrappedTransitions) {
            return ImageManager.getImage(AtsImage.TRANSITION);
         } else if (element instanceof WrappedRules) {
            return ImageManager.getImage(FrameworkImage.RULE);
         } else if (element instanceof RuleAndLocation) {
            return ImageManager.getImage(FrameworkImage.RULE);
         } else if (element instanceof User) {
            return ImageManager.getImage(FrameworkImage.USER);
         } else if (element instanceof DecisionReviewDefinition) {
            return ImageManager.getImage(AtsImage.REVIEW);
         } else if (element instanceof PeerReviewDefinition) {
            return ImageManager.getImage(AtsImage.REVIEW);
         } else if (element instanceof WrappedDecisionReviews) {
            return ImageManager.getImage(AtsImage.REVIEW);
         } else if (element instanceof WrappedPeerReviews) {
            return ImageManager.getImage(AtsImage.REVIEW);
         } else if (element instanceof DecisionReviewOption) {
            return ImageManager.getImage(FrameworkImage.QUESTION);
         }
         return null;
      }
   }

   private final class InternalContentProvider implements ITreeContentProvider {

      private final SMAEditor editor;
      private final AbstractWorkflowArtifact awa;

      private InternalContentProvider(SMAEditor editor) {
         this.editor = editor;
         this.awa = editor.getSma();
      }

      @Override
      public void dispose() {
         // do nothing
      }

      @Override
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         // do nothing
      }

      @Override
      public Object[] getChildren(Object element) {
         List<Object> items = new ArrayList<Object>();

         if (element instanceof SMAEditor) {
            items.add(((SMAEditor) element).getSma());
         } else if (element instanceof AbstractWorkflowArtifact) {
            items.add(((AbstractWorkflowArtifact) element).getWorkDefinitionMatch());
         } else if (element instanceof WrappedLayout) {
            items.addAll(((WrappedLayout) element).getStateItems());
         } else if (element instanceof WrappedTrace) {
            items.addAll(((WrappedTrace) element).getTrace());
         } else if (element instanceof WorkDefinitionMatch) {
            getChildrenFromWorkDefinitionMatch(element, items);
         } else if (element instanceof StateDefinition) {
            getChildrenFromStateDefinition(element, items);
         } else if (element instanceof CompositeStateItem) {
            items.addAll(((CompositeStateItem) element).getStateItems());
         } else if (element instanceof User) {
            items.add("Assignee: " + ((User) element).getName());
         } else if (element instanceof WrappedTransitions) {
            items.addAll(((WrappedTransitions) element).getTransitions());
         } else if (element instanceof DecisionReviewDefinition) {
            getChildrenFromDecisionReviewDefinition(element, items);
         } else if (element instanceof PeerReviewDefinition) {
            getChildrenFromPeerReviewDefinition(element, items);
         } else if (element instanceof DecisionReviewOption) {
            getUsersFromDecisionReviewOpt((DecisionReviewOption) element, items);
         } else if (element instanceof WrappedDecisionReviews) {
            items.addAll(((WrappedDecisionReviews) element).getDecisionReviews());
         } else if (element instanceof WrappedPeerReviews) {
            items.addAll(((WrappedPeerReviews) element).getPeerReviews());
         } else if (element instanceof WrappedRules) {
            items.addAll(((WrappedRules) element).getRuleAndLocations());
         } else if (element instanceof WidgetDefinition) {
            getChildrenFromWidgetDefinition(element, items);
         } else if (element instanceof String) {
            items.add(element);
         } else if (element instanceof WrappedStates) {
            items.addAll(((WrappedStates) element).getStates());
         }

         return items.toArray(new Object[items.size()]);
      }

      private void getChildrenFromWidgetDefinition(Object element, List<Object> items) {
         items.add("XWidget: " + ((WidgetDefinition) element).getXWidgetName());
         items.add("Attribute Name: " + ((WidgetDefinition) element).getAtrributeName());
         if (Strings.isValid(((WidgetDefinition) element).getDescription())) {
            items.add("Description: " + ((WidgetDefinition) element).getDescription());
         }
         if (((WidgetDefinition) element).getHeight() > 0) {
            items.add("Height: " + ((WidgetDefinition) element).getHeight());
         }
         if (Strings.isValid(((WidgetDefinition) element).getAtrributeName())) {
            items.add("Tooltip: " + ((WidgetDefinition) element).getAtrributeName());
         }
         if (!((WidgetDefinition) element).getOptions().getXOptions().isEmpty()) {
            items.addAll(((WidgetDefinition) element).getOptions().getXOptions());
         }
      }

      private void getChildrenFromPeerReviewDefinition(Object element, List<Object> items) {
         if (Strings.isValid(((PeerReviewDefinition) element).getDescription())) {
            items.add("Description: " + ((PeerReviewDefinition) element).getDescription());
         }
         if (Strings.isValid(((PeerReviewDefinition) element).getLocation())) {
            items.add("Description: " + ((PeerReviewDefinition) element).getLocation());
         }
         items.add("On Event: " + ((PeerReviewDefinition) element).getStateEventType().name());
         items.add("Related To State: " + ((PeerReviewDefinition) element).getRelatedToState());
         items.add("Review Blocks: " + ((PeerReviewDefinition) element).getBlockingType().name());
         try {
            items.addAll(UserManager.getUsersByUserId(((PeerReviewDefinition) element).getAssignees()));
         } catch (OseeCoreException ex) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         }
      }

      private void getChildrenFromDecisionReviewDefinition(Object element, List<Object> items) {
         if (Strings.isValid(((DecisionReviewDefinition) element).getDescription())) {
            items.add("Description: " + ((DecisionReviewDefinition) element).getDescription());
         }
         items.add("On Event: " + ((DecisionReviewDefinition) element).getStateEventType().name());
         items.add("Related To State: " + ((DecisionReviewDefinition) element).getRelatedToState());
         items.add("Review Blocks: " + ((DecisionReviewDefinition) element).getBlockingType().name());
         items.add("Auto Transition to Decision: " + ((DecisionReviewDefinition) element).isAutoTransitionToDecision());
         try {
            items.addAll(UserManager.getUsersByUserId(((DecisionReviewDefinition) element).getAssignees()));
         } catch (OseeCoreException ex) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         }
         items.addAll(((DecisionReviewDefinition) element).getOptions());
      }

      private void getChildrenFromStateDefinition(Object element, List<Object> items) {
         StateDefinition stateDef = (StateDefinition) element;
         items.add(new WrappedLayout(stateDef.getStateItems()));
         items.add(new WrappedRules(stateDef, awa));
         if (editor.getSma().isOfType(AtsArtifactTypes.TeamWorkflow)) {
            items.add(new WrappedDecisionReviews(stateDef.getDecisionReviews()));
            items.add(new WrappedPeerReviews(stateDef.getPeerReviews()));
         }
         items.add(new WrappedTransitions(stateDef));
      }

      private void getChildrenFromWorkDefinitionMatch(Object element, List<Object> items) {
         for (String id : ((WorkDefinitionMatch) element).getWorkDefinition().getIds()) {
            items.add("Id: " + id);
         }
         items.addAll(((WorkDefinitionMatch) element).getWorkDefinition().getStatesOrdered());
         items.addAll(((WorkDefinitionMatch) element).getWorkDefinition().getRules());
         items.add(new WrappedTrace(((WorkDefinitionMatch) element).getTrace()));
      }

      private void getUsersFromDecisionReviewOpt(DecisionReviewOption revOpt, List<Object> items) {
         for (String userId : revOpt.getUserIds()) {
            try {
               User user = UserManager.getUserByUserId(userId);
               items.add(user);
            } catch (OseeCoreException ex) {
               items.add("Erroring getting user: " + ex.getLocalizedMessage());
               OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
            }
         }
         for (String userName : revOpt.getUserNames()) {
            try {
               User user = UserManager.getUserByName(userName);
               items.add(user);
            } catch (OseeCoreException ex) {
               items.add(String.format("Erroring getting user by name [%s] : [%s]", userName, ex.getLocalizedMessage()));
               OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
            }
         }
      }

      @Override
      public Object getParent(Object element) {
         if (element instanceof AbstractWorkflowArtifact) {
            return editor;
         } else if (element instanceof WorkDefinition) {
            return editor;
         } else if (element instanceof StateDefinition) {
            return ((StateDefinition) element).getWorkDefinition();
         } else if (element instanceof RuleDefinition) {
            return editor;
         } else if (element instanceof String) {
            return editor;
         }
         return null;
      }

      @Override
      public boolean hasChildren(Object element) {
         if (element instanceof String) {
            return false;
         } else if (element instanceof AbstractWorkflowArtifact) {
            return true;
         } else if (element instanceof WorkDefinitionMatch) {
            return true;
         } else if (element instanceof StateDefinition) {
            return true;
         } else if (element instanceof RuleDefinition) {
            return false;
         } else if (element instanceof CompositeStateItem) {
            return true;
         } else if (element instanceof WidgetDefinition) {
            return true;
         } else if (element instanceof PeerReviewDefinition) {
            return true;
         } else if (element instanceof DecisionReviewDefinition) {
            return true;
         } else if (element instanceof DecisionReviewOption) {
            return !((DecisionReviewOption) element).getUserIds().isEmpty();
         } else if (element instanceof WrappedTransitions) {
            return true;
         } else if (element instanceof WrappedLayout) {
            return !((WrappedLayout) element).stateItems.isEmpty();
         } else if (element instanceof WrappedDecisionReviews) {
            return !((WrappedDecisionReviews) element).decReviews.isEmpty();
         } else if (element instanceof WrappedPeerReviews) {
            return !((WrappedPeerReviews) element).decReviews.isEmpty();
         } else if (element instanceof WrappedTrace) {
            return !((WrappedTrace) element).trace.isEmpty();
         } else if (element instanceof WrappedStates) {
            return !((WrappedStates) element).states.isEmpty();
         } else if (element instanceof RuleAndLocation) {
            return false;
         } else if (element instanceof WrappedRules) {
            return !((WrappedRules) element).getRuleAndLocations().isEmpty();
         }
         return false;
      }

      @Override
      public Object[] getElements(Object inputElement) {
         return getChildren(inputElement);
      }
   }
   public class WrappedRules {
      private final StateDefinition stateDef;
      private final AbstractWorkflowArtifact awa;

      public WrappedRules(StateDefinition stateDef, AbstractWorkflowArtifact awa) {
         this.stateDef = stateDef;
         this.awa = awa;
      }

      @Override
      public String toString() {
         return "Rules" + (getRuleAndLocations().isEmpty() ? " (Empty)" : "");
      }

      public Collection<RuleAndLocation> getRuleAndLocations() {
         List<RuleAndLocation> result = new ArrayList<SMAEditorOutlinePage.RuleAndLocation>();
         // get rules from stateDef
         for (RuleDefinition ruleDef : stateDef.getRules()) {
            for (String location : stateDef.getRuleLocations(ruleDef)) {
               result.add(new RuleAndLocation(ruleDef, location));
            }
         }
         // add rules from Team Definition
         if (awa instanceof TeamWorkFlowArtifact) {
            try {
               TeamDefinitionArtifact teamDef = ((TeamWorkFlowArtifact) awa).getTeamDefinition();
               for (RuleDefinition workRuleDef : teamDef.getWorkRules()) {
                  String location = String.format("Team Definition [%s]", teamDef);
                  result.add(new RuleAndLocation(workRuleDef, location));
                  if (workRuleDef.getName().startsWith("ats")) {
                     result.add(new RuleAndLocation(new RuleDefinition(workRuleDef.getName().replaceFirst("^ats", "")),
                        location + " translated from WorkRuleDefinition starting with ats"));
                  }
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
            }
         }
         return result;
      }
   }

   public class RuleAndLocation {
      private final RuleDefinition ruleDef;
      private final String location;

      public RuleAndLocation(RuleDefinition ruleDef, String location) {
         this.ruleDef = ruleDef;
         this.location = location;
      }

      @Override
      public String toString() {
         return String.format("%s [%s]", ruleDef.getName(), location);
      }

   }

   public class WrappedStates {
      private final String name;
      private final Collection<StateDefinition> states;

      public WrappedStates(String name, Collection<StateDefinition> states) {
         this.name = name;
         this.states = states;
      }

      @Override
      public String toString() {
         return name + (states.isEmpty() ? " (Empty)" : "");
      }

      public Collection<StateDefinition> getStates() {
         return states;
      }

   }
   public class WrappedDecisionReviews {
      private final Collection<DecisionReviewDefinition> decReviews;

      public WrappedDecisionReviews(Collection<DecisionReviewDefinition> decReviews) {
         this.decReviews = decReviews;
      }

      @Override
      public String toString() {
         return "Decision Reviews" + (decReviews.isEmpty() ? " (Empty)" : "");
      }

      public Collection<DecisionReviewDefinition> getDecisionReviews() {
         return decReviews;
      }

   }
   public class WrappedPeerReviews {
      private final Collection<PeerReviewDefinition> decReviews;

      public WrappedPeerReviews(Collection<PeerReviewDefinition> decReviews) {
         this.decReviews = decReviews;
      }

      @Override
      public String toString() {
         return "Peer Reviews" + (decReviews.isEmpty() ? " (Empty)" : "");
      }

      public Collection<PeerReviewDefinition> getPeerReviews() {
         return decReviews;
      }

   }
   public class WrappedTrace {
      private final Collection<String> trace;

      public WrappedTrace(Collection<String> trace) {
         this.trace = trace;
      }

      @Override
      public String toString() {
         return "From" + (trace.isEmpty() ? " (Empty)" : "");
      }

      public Collection<String> getTrace() {
         return trace;
      }

   }
   public class WrappedLayout {
      private final Collection<StateItem> stateItems;

      public WrappedLayout(Collection<StateItem> stateItems) {
         this.stateItems = stateItems;
      }

      @Override
      public String toString() {
         return "Layout" + (stateItems.isEmpty() ? " (Empty)" : "");
      }

      public Collection<StateItem> getStateItems() {
         return stateItems;
      }

   }

   public class WrappedTransitions {

      private final StateDefinition stateDef;

      public WrappedTransitions(StateDefinition stateDef) {
         this.stateDef = stateDef;
      }

      public Collection<Object> getTransitions() {
         List<StateDefinition> defaultToStates = new ArrayList<StateDefinition>();
         if (stateDef.getDefaultToState() != null) {
            defaultToStates.add(stateDef.getDefaultToState());
         }
         List<Object> items = new ArrayList<Object>();
         items.add(new WrappedStates("DefaultToState", defaultToStates));
         items.add(new WrappedStates("ToStates", stateDef.getToStates()));
         items.add(new WrappedStates("OverrideAttrValidationStates", stateDef.getOverrideAttributeValidationStates()));
         return items;
      }

      @Override
      public String toString() {
         return "Transitions" + (stateDef.getToStates().isEmpty() ? " (Empty)" : "");
      }

   }

}