/*
 * Created on Dec 15, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.workdef;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.eclipse.osee.ats.actions.wizard.IAtsTeamWorkflow;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.DecisionReviewArtifact;
import org.eclipse.osee.ats.artifact.GoalArtifact;
import org.eclipse.osee.ats.artifact.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.artifact.TaskArtifact;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkflowExtensions;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.DecisionOption;
import org.eclipse.osee.ats.util.widgets.XDecisionOptions;
import org.eclipse.osee.ats.workdef.provider.AtsWorkDefinitionProvider;
import org.eclipse.osee.ats.workflow.flow.DecisionWorkflowDefinition;
import org.eclipse.osee.ats.workflow.flow.GoalWorkflowDefinition;
import org.eclipse.osee.ats.workflow.flow.PeerToPeerWorkflowDefinition;
import org.eclipse.osee.ats.workflow.flow.TaskWorkflowDefinition;
import org.eclipse.osee.ats.workflow.item.AtsAddDecisionReviewRule;
import org.eclipse.osee.ats.workflow.item.AtsAddPeerToPeerReviewRule;
import org.eclipse.osee.ats.workflow.item.AtsStatePercentCompleteWeightRule;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.widgets.XOption;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayoutData;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkFlowDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkFlowDefinitionFactory;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkFlowDefinitionMatch;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinitionFactory;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkPageDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkRuleDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkWidgetDefinition;

public class WorkDefinitionFactory {

   private static final Map<String, RuleDefinition> idToRule = new HashMap<String, RuleDefinition>();
   private static final Map<Artifact, WorkDefinitionMatch> artToWorkDefinitions =
      new HashMap<Artifact, WorkDefinitionMatch>();
   private static final Map<String, WorkDefinitionMatch> idToWorkDefintion = new HashMap<String, WorkDefinitionMatch>();

   public static RuleDefinition getRuleById(String id) {
      ensureRulesLoaded();
      return idToRule.get(id);
   }

   private synchronized static void ensureRulesLoaded() {
      if (idToRule.isEmpty()) {
         try {
            for (WorkItemDefinition workItem : WorkItemDefinitionFactory.getWorkItemDefinitions()) {
               if (workItem instanceof WorkRuleDefinition) {
                  if (workItem.getName().startsWith("atsAddDecisionReview")) {
                     System.err.println("skipping rule " + workItem.getName());
                  } else if (workItem.getName().startsWith("atsAddPeerToPeerReview")) {
                     System.err.println("skipping rule " + workItem.getName());
                  } else if (workItem.getName().startsWith(AtsStatePercentCompleteWeightRule.ID)) {
                     System.err.println("skipping rule " + workItem.getName());
                  } else {
                     try {
                        WorkRuleDefinition workRule = (WorkRuleDefinition) workItem;
                        // All rules in DB should map to RuleDefinitionOption 
                        RuleDefinition ruleDef = null;
                        String workRuleName = workRule.getName().replaceFirst("^ats", "");
                        try {
                           RuleDefinitionOption ruleOption = RuleDefinitionOption.valueOf(workRuleName);
                           ruleDef = new RuleDefinition(ruleOption);
                        } catch (IllegalArgumentException ex) {
                           ruleDef = new RuleDefinition(workRuleName);
                        }
                        ruleDef.setDescription(workRule.getDescription());
                        copyKeyValuePair(ruleDef, workRule);
                        idToRule.put(ruleDef.getName(), ruleDef);
                     } catch (Exception ex) {
                        OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
                     }
                  }
               }
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         }
      }
   }

   public static void clearCaches() {
      artToWorkDefinitions.clear();
      idToRule.clear();
      idToWorkDefintion.clear();
   }

   public static WorkDefinitionMatch getWorkDefinition(Artifact artifact) throws OseeCoreException {
      if (!artToWorkDefinitions.containsKey(artifact)) {
         WorkDefinitionMatch match = getWorkDefinitionNew(artifact);
         if (!match.isMatched()) {
            WorkFlowDefinitionMatch flowMatch = WorkFlowDefinitionFactory.getWorkFlowDefinition(artifact);
            if (flowMatch.isMatched()) {
               WorkDefinition workDef = translateToWorkDefinition(flowMatch.getWorkFlowDefinition());
               match = new WorkDefinitionMatch(workDef, null);
               match.getTrace().addAll(flowMatch.getTrace());
            }
         }
         artToWorkDefinitions.put(artifact, match);
      }
      return artToWorkDefinitions.get(artifact);
   }

   public static WorkDefinitionMatch getWorkDefinition(String id) throws OseeCoreException {
      if (!idToWorkDefintion.containsKey(id)) {
         WorkDefinitionMatch match = new WorkDefinitionMatch();
         String translatedId = WorkDefinitionFactory.getOverrideWorkDefId(id);
         // Try to get from new DSL provider if configured to use it
         if (!match.isMatched() && AtsUtil.isUseNewWorkDefinitions()) {
            WorkDefinition workDef = AtsWorkDefinitionProvider.get().getWorkFlowDefinition(translatedId);
            if (workDef != null) {
               match.setWorkDefinition(workDef);
               match.getTrace().add(
                  (String.format("from DSL provider loaded id [%s] and override translated Id [%s]", id, translatedId)));
            }
         }
         // Otherwise, just translate legacy WorkFlowDefinition from artifact
         if (!match.isMatched()) {
            WorkFlowDefinition workFlowDef = (WorkFlowDefinition) WorkItemDefinitionFactory.getWorkItemDefinition(id);
            if (workFlowDef != null) {
               WorkDefinition workDef = translateToWorkDefinition(workFlowDef);
               if (workDef != null) {
                  match.setWorkDefinition(workDef);
                  match.getTrace().add(
                     String.format("from legacy WorkFlowDefinition [%s] translated for id [%s]", id, id));
               }
            }
         }
         // If still no match, configuration may have new workdef id but not set to use them
         // Attempt to get back to original id and load through WorkFlowDefinition translate
         if (!match.isMatched()) {
            String reverseId = AtsWorkDefinitionSheetProviders.getReverseOverrideId(id);
            if (reverseId != null) {
               WorkFlowDefinition workFlowDef =
                  (WorkFlowDefinition) WorkItemDefinitionFactory.getWorkItemDefinition(reverseId);
               if (workFlowDef != null) {
                  WorkDefinition workDef = translateToWorkDefinition(workFlowDef);
                  if (workDef != null) {
                     match.setWorkDefinition(workDef);
                     match.getTrace().add(
                        String.format(
                           "from legacy WorkFlowDefinition [%s] translated for id [%s] falling back to reverse id [%s]",
                           id, id, reverseId));
                  }
               }
            }
         }
         if (match.isMatched()) {
            System.out.println("Loaded " + match);
            idToWorkDefintion.put(id, match);
         }
      }
      WorkDefinitionMatch match = idToWorkDefintion.get(id);
      if (match == null) {
         match = new WorkDefinitionMatch();
      }
      return match;
   }

   public static WorkDefinition translateToWorkDefinition(WorkFlowDefinition workFlowDef) {
      ensureRulesLoaded();
      try {
         String startWorkPageName = workFlowDef.getResolvedStartPageId();
         WorkDefinition workDef = new WorkDefinition(workFlowDef.getId());
         addRulesToWorkDefinition(workFlowDef, workDef);
         for (WorkPageDefinition workPage : workFlowDef.getPages()) {
            // not using ids anymore for states, widgets or rules
            StateDefinition stateDef = workDef.getOrCreateState(workPage.getPageName());
            stateDef.setWorkDefinition(workDef);
            stateDef.setOrdinal(workPage.getWorkPageOrdinal());
            if (workPage.getId().equals(startWorkPageName)) {
               workDef.setStartState(stateDef);
            }
            // TODO get rid of this??
            copyKeyValuePair(stateDef, workPage);
            stateDef.setWorkPageType(workPage.getWorkPageType());

            for (WorkPageDefinition returnPageDefinition : workFlowDef.getReturnPages(workPage)) {
               // new model doesn't have AsReturn, instead it uses OverrideAttributeValidation
               StateDefinition returnStateDef = workDef.getOrCreateState(returnPageDefinition.getPageName());
               stateDef.getOverrideAttributeValidationStates().add(returnStateDef);
            }
            for (WorkPageDefinition toPageDefinition : workFlowDef.getToPages(workPage)) {
               StateDefinition toStateDef = workDef.getOrCreateState(toPageDefinition.getPageName());
               stateDef.getToStates().add(toStateDef);
            }
            WorkPageDefinition defaultToPageDefinition = workFlowDef.getDefaultToPage(workPage);
            if (defaultToPageDefinition != null) {
               StateDefinition defaultToStateDef = workDef.getOrCreateState(defaultToPageDefinition.getPageName());
               stateDef.setDefaultToState(defaultToStateDef);
            }

            CompositeStateItem compStateItem = null;
            for (WorkItemDefinition itemDef : workPage.getWorkItems(true)) {
               if (itemDef instanceof WorkWidgetDefinition) {
                  WorkWidgetDefinition workWidget = (WorkWidgetDefinition) itemDef;
                  DynamicXWidgetLayoutData data = workWidget.get();

                  // Check for being_composites and process as separate widgets
                  int numColumns = data.getBeginComposite();
                  if (numColumns > 0) {
                     compStateItem = new CompositeStateItem(numColumns);
                     stateDef.getStateItems().add(compStateItem);
                  }

                  // Create new Widget Definition for widget
                  WidgetDefinition widgetDef = new WidgetDefinition(workWidget.getName());
                  widgetDef.setDescription(workWidget.getDescription());
                  copyKeyValuePair(widgetDef, workWidget);

                  // Convert layout data to widget definition values
                  for (XOption option : data.getXOptionHandler().getXOptions()) {
                     WidgetOption widgetOpt = null;
                     try {
                        widgetOpt = WidgetOption.valueOf(option.name());
                     } catch (IllegalArgumentException ex) {
                        // do nothing
                     }
                     if (widgetOpt != null) {
                        widgetDef.set(widgetOpt);
                     }
                  }
                  // Old model only had required, new model has two types of required
                  if (data.isRequired()) {
                     widgetDef.set(WidgetOption.REQUIRED_FOR_TRANSITION);
                  }
                  if (Strings.isValid(data.getName()) && !widgetDef.getName().equals(data.getName())) {
                     widgetDef.setName(data.getName());
                  }
                  widgetDef.setAttributeName(data.getStoreName());
                  widgetDef.setDefaultValue(data.getDefaultValue());
                  if (data.isHeightSet()) {
                     widgetDef.setHeight(data.getHeight());
                  }
                  widgetDef.setToolTip(data.getToolTip());
                  widgetDef.setXWidgetName(data.getXWidgetName());

                  if (compStateItem != null) {
                     compStateItem.getStateItems().add(widgetDef);
                  } else {
                     stateDef.getStateItems().add(widgetDef);
                  }

                  // Check for end composite
                  boolean endComposite = data.isEndComposite();
                  if (endComposite) {
                     compStateItem = null;
                  }

               } else if (itemDef instanceof WorkRuleDefinition) {
                  WorkRuleDefinition workRule = (WorkRuleDefinition) itemDef;
                  if (workRule.getName().startsWith("atsAddDecisionReview")) {
                     DecisionReviewDefinition decRevDef = convertDecisionReviewRule(workRule);
                     if (!Strings.isValid(decRevDef.getRelatedToState())) {
                        decRevDef.setRelatedToState(stateDef.getName());
                     }
                     stateDef.getDecisionReviews().add(decRevDef);
                  } else if (workRule.getName().startsWith("atsAddPeerToPeerReview")) {
                     PeerReviewDefinition peerRevDef = convertPeerReviewRule(workRule);
                     if (!Strings.isValid(peerRevDef.getRelatedToState())) {
                        peerRevDef.setRelatedToState(stateDef.getName());
                     }
                     stateDef.getPeerReviews().add(peerRevDef);
                  } else {
                     RuleDefinition ruleDef = getRuleById(workRule.getId().replaceFirst("^ats", ""));
                     if (ruleDef == null) {
                        OseeLog.log(AtsPlugin.class, Level.SEVERE,
                           String.format("Null work rule for " + workRule.getId()));
                     } else {
                        stateDef.addRule(ruleDef, "from related WorkItemDefintion");
                     }
                  }
               } else {
                  OseeLog.log(AtsPlugin.class, Level.SEVERE,
                     "Unexpected item type as page child -> " + itemDef.getType());
               }
            }
         }

         // Process WeightDefinitions
         Artifact workDefArt = workFlowDef.getArtifact();
         for (Artifact workChild : workDefArt.getRelatedArtifacts(CoreRelationTypes.WorkItem__Child)) {
            if (workChild.getName().startsWith(AtsStatePercentCompleteWeightRule.ID)) {
               WorkRuleDefinition ruleDefinition = new WorkRuleDefinition(workChild);
               for (String stateName : ruleDefinition.getWorkDataKeyValueMap().keySet()) {
                  String value = ruleDefinition.getWorkDataValue(stateName);
                  try {
                     double percent = new Double(value).doubleValue();
                     if (percent < 0.0 || percent > 1) {
                        OseeLog.log(
                           AtsPlugin.class,
                           Level.SEVERE,
                           "Invalid percent value \"" + value + "\" (must be 0..1) for rule " + ruleDefinition.getName(),
                           new OseeArgumentException("state map exception"));
                     } else {
                        percent = percent * 100;
                        workDef.getStateByName(stateName).setPercentWeight(new Double(percent).intValue());
                     }
                  } catch (Exception ex) {
                     OseeLog.log(
                        AtsPlugin.class,
                        Level.SEVERE,
                        "Invalid percent value \"" + value + "\" (must be float 0..1) for rule " + ruleDefinition.getName(),
                        new OseeArgumentException("state map exception"));
                  }
               }
               Result result = workDef.validateStateWeighting();
               if (result.isFalse()) {
                  OseeLog.log(AtsPlugin.class, Level.SEVERE,
                     "Error translating weight definitions - " + result.getText());
               }
            }
         }
         return workDef;
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
      return null;
   }

   private static void addRulesToWorkDefinition(WorkFlowDefinition workFlowDef, WorkDefinition workDef) {
      try {
         for (WorkRuleDefinition workRule : workFlowDef.getWorkRules()) {
            RuleDefinition ruleDef = getRuleById(workRule.getId());
            workDef.getRules().add(ruleDef);
         }
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE,
            "Error processing WorkRuleDefinition for workflow " + workDef.getName(), ex);
      }
   }

   private static DecisionReviewDefinition convertDecisionReviewRule(WorkRuleDefinition workRule) throws OseeCoreException {
      DecisionReviewDefinition revDef = new DecisionReviewDefinition(workRule.getName());
      revDef.setBlockingType(AtsAddDecisionReviewRule.getReviewBlockTypeOrDefault(workRule));
      for (User user : AtsAddDecisionReviewRule.getAssigneesOrDefault(workRule)) {
         revDef.getAssignees().add(user.getUserId());
      }
      revDef.setReviewTitle(AtsAddDecisionReviewRule.getReviewTitle(workRule));
      revDef.setRelatedToState(AtsAddDecisionReviewRule.getRelatedToState(workRule));
      revDef.setDescription(workRule.getDescription());
      revDef.setAutoTransitionToDecision(true);
      revDef.setStateEventType(AtsAddDecisionReviewRule.getStateEventType(workRule));
      for (DecisionOption decOpt : XDecisionOptions.getDecisionOptions(AtsAddDecisionReviewRule.getDecisionOptionString(workRule))) {
         DecisionReviewOption revOpt = new DecisionReviewOption(decOpt.getName());
         revOpt.setFollowupRequired(decOpt.isFollowupRequired());
         for (User user : decOpt.getAssignees()) {
            revOpt.getUserIds().add(user.getUserId());
         }
         revDef.getOptions().add(revOpt);
      }
      return revDef;
   }

   private static PeerReviewDefinition convertPeerReviewRule(WorkRuleDefinition workRule) throws OseeCoreException {
      PeerReviewDefinition revDef = new PeerReviewDefinition(workRule.getName());
      revDef.setBlockingType(AtsAddPeerToPeerReviewRule.getReviewBlockTypeOrDefault(workRule));
      for (User user : AtsAddPeerToPeerReviewRule.getAssigneesOrDefault(workRule)) {
         revDef.getAssignees().add(user.getUserId());
      }
      revDef.setReviewTitle(AtsAddPeerToPeerReviewRule.getReviewTitle(workRule));
      revDef.setRelatedToState(AtsAddPeerToPeerReviewRule.getRelatedToState(workRule));
      revDef.setDescription(workRule.getDescription());
      revDef.setLocation(AtsAddPeerToPeerReviewRule.getLocation(workRule));
      revDef.setStateEventType(AtsAddPeerToPeerReviewRule.getStateEventType(workRule));
      return revDef;
   }

   private static void copyKeyValuePair(AbstractWorkDefItem itemDef, WorkItemDefinition workItem) {
      for (Entry<String, String> entry : workItem.getWorkDataKeyValueMap().entrySet()) {
         // XWidget is a key of WorkWidgetDefinition and should be handled through the normal conversion process
         if (!entry.getKey().equals("XWidget")) {
            itemDef.addWorkDataKeyValue(entry.getKey(), entry.getValue());
         }
      }
   }

   private static WorkDefinitionMatch getWorkDefinitionFromArtifactsAttributeValue(Artifact artifact) throws OseeCoreException {
      // If this artifact specifies it's own workflow definition, use it
      String workFlowDefId = artifact.getSoleAttributeValue(AtsAttributeTypes.WorkflowDefinition, null);
      if (Strings.isValid(workFlowDefId)) {
         String translatedId = WorkDefinitionFactory.getOverrideWorkDefId(workFlowDefId);
         WorkDefinitionMatch match = getWorkDefinition(translatedId);
         if (match.isMatched()) {
            match.getTrace().add(
               String.format("from artifact [%s] for id [%s] and override translated Id [%s]", artifact, workFlowDefId,
                  translatedId));
            return match;
         }
      }
      return new WorkDefinitionMatch();
   }

   private static WorkDefinitionMatch getTaskWorkDefinitionFromArtifactsAttributeValue(Artifact artifact) throws OseeCoreException {
      // If this artifact specifies it's own workflow definition, use it
      String workFlowDefId = artifact.getSoleAttributeValue(AtsAttributeTypes.RelatedTaskWorkflowDefinition, null);
      if (Strings.isValid(workFlowDefId)) {
         String translatedId = WorkDefinitionFactory.getOverrideWorkDefId(workFlowDefId);
         WorkDefinitionMatch match = getWorkDefinition(translatedId);
         if (match.isMatched()) {
            match.getTrace().add(
               String.format("from artifact [%s] for id [%s] and override translated Id [%s]", artifact, workFlowDefId,
                  translatedId));
            return match;
         }
      }
      return new WorkDefinitionMatch();
   }

   /**
    * Look at team def's attribute for Work Definition setting, otherwise, walk up team tree for setting
    */
   private static WorkDefinitionMatch getWorkDefinitionFromTeamDefinitionAttributeInherited(TeamDefinitionArtifact teamDef) throws OseeCoreException {
      WorkDefinitionMatch match = getWorkDefinitionFromArtifactsAttributeValue(teamDef);
      if (match.isMatched()) {
         return match;
      }
      Artifact parentArt = teamDef.getParent();
      if (parentArt != null && parentArt instanceof TeamDefinitionArtifact) {
         return getWorkDefinitionFromTeamDefinitionAttributeInherited((TeamDefinitionArtifact) parentArt);
      }
      return new WorkDefinitionMatch();
   }

   private static WorkDefinitionMatch getWorkDefinitionForTask(TaskArtifact taskArt) throws OseeCoreException {
      WorkDefinitionMatch match = new WorkDefinitionMatch();
      for (IAtsTeamWorkflow provider : TeamWorkflowExtensions.getAtsTeamWorkflowExtensions()) {
         String workFlowDefId = provider.getRelatedTaskWorkflowDefinitionId(taskArt.getParentSMA());
         if (Strings.isValid(workFlowDefId)) {
            String translatedId = getOverrideWorkDefId(workFlowDefId);
            match = WorkDefinitionFactory.getWorkDefinition(translatedId);
            match.getTrace().add(
               (String.format("from provider [%s] for id [%s] and override translated Id [%s]",
                  provider.getClass().getSimpleName(), workFlowDefId, translatedId)));
            break;
         }
      }
      if (!match.isMatched()) {
         // If task specifies it's own workflow id, use it
         match = getWorkDefinitionFromArtifactsAttributeValue(taskArt);
      }
      if (!match.isMatched()) {
         // Else If parent SMA has a related task definition workflow id specified, use it
         WorkDefinitionMatch match2 = getTaskWorkDefinitionFromArtifactsAttributeValue(taskArt.getParentSMA());
         if (match2.isMatched()) {
            match2.getTrace().add(String.format("from task parent SMA [%s]", taskArt.getParentSMA()));
            match = match2;
         }
      }
      if (!match.isMatched()) {
         // Else If parent TeamWorkflow's TeamDefinition has a related task definition workflow id, use it
         match = getTaskWorkDefinitionFromArtifactsAttributeValue(taskArt.getParentTeamWorkflow().getTeamDefinition());
      }
      if (!match.isMatched()) {
         // Else, use default Task workflow
         String translatedId = getOverrideWorkDefId(TaskWorkflowDefinition.ID);
         match = getWorkDefinition(translatedId);
         if (match.isMatched()) {
            match.getTrace().add(
               String.format("default TaskWorkflowDefinition ID [%s] and override translated Id [%s]",
                  TaskWorkflowDefinition.ID, translatedId));
         }
      }
      return match;
   }

   private static WorkDefinitionMatch getWorkDefinitionNew(Artifact artifact) throws OseeCoreException {
      WorkDefinitionMatch match = new WorkDefinitionMatch();
      if (artifact instanceof TaskArtifact) {
         match = getWorkDefinitionForTask((TaskArtifact) artifact);
      }
      if (!match.isMatched() && artifact instanceof AbstractWorkflowArtifact) {
         AbstractWorkflowArtifact aba = (AbstractWorkflowArtifact) artifact;
         // Check extensions for definition handling
         for (IAtsTeamWorkflow provider : TeamWorkflowExtensions.getAtsTeamWorkflowExtensions()) {
            String workFlowDefId = provider.getWorkflowDefinitionId(aba);
            if (Strings.isValid(workFlowDefId)) {
               match =
                  WorkDefinitionFactory.getWorkDefinition(WorkDefinitionFactory.getOverrideWorkDefId(workFlowDefId));
            }
         }
         if (!match.isMatched()) {
            // If this artifact specifies it's own workflow definition, use it
            match = getWorkDefinitionFromArtifactsAttributeValue(artifact);
            if (!match.isMatched()) {
               // Otherwise, use workflow defined by attribute of WorkflowDefinition
               // Note: This is new.  Old TeamDefs got workflow off relation
               if (artifact instanceof TeamWorkFlowArtifact) {
                  TeamDefinitionArtifact teamDef = ((TeamWorkFlowArtifact) artifact).getTeamDefinition();
                  match = getWorkDefinitionFromTeamDefinitionAttributeInherited(teamDef);
                  if (!match.isMatched()) {
                     match = ((TeamWorkFlowArtifact) artifact).getTeamDefinition().getWorkDefinition();
                  }
               } else if (artifact instanceof GoalArtifact) {
                  match = getWorkDefinition(getOverrideWorkDefId(GoalWorkflowDefinition.ID));
                  match.getTrace().add(String.format("Override translated from id [%s]", GoalWorkflowDefinition.ID));
               } else if (artifact instanceof PeerToPeerReviewArtifact) {
                  match = getWorkDefinition(getOverrideWorkDefId(PeerToPeerWorkflowDefinition.ID));
                  match.getTrace().add(
                     String.format("Override translated from id [%s]", PeerToPeerWorkflowDefinition.ID));
               } else if (artifact instanceof DecisionReviewArtifact) {
                  match = getWorkDefinition(getOverrideWorkDefId(DecisionWorkflowDefinition.ID));
                  match.getTrace().add(String.format("Override translated from id [%s]", DecisionWorkflowDefinition.ID));
               }
            }
         }
      }
      return match;
   }

   public static Collection<WorkDefinitionMatch> getWorkDefinitions() {
      return idToWorkDefintion.values();
   }

   public static String getOverrideWorkDefId(String id) {
      // Don't override if no providers available (dsl plugins not released)
      if (AtsUtil.isUseNewWorkDefinitions()) {

         String overrideId = AtsWorkDefinitionSheetProviders.getOverrideId(id);
         if (Strings.isValid(overrideId)) {
            OseeLog.log(AtsPlugin.class, Level.INFO,
               String.format("Override WorkDefinition [%s] with [%s]", id, overrideId));
            return overrideId;
         }
      }
      return id;
   }

}
