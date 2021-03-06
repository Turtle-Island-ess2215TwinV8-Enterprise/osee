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
package org.eclipse.osee.ats.impl.internal.convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.osee.ats.api.workdef.IAtsCompositeLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsDecisionReviewDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsDecisionReviewOption;
import org.eclipse.osee.ats.api.workdef.IAtsLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsPeerReviewDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.IAttributeResolver;
import org.eclipse.osee.ats.api.workdef.IUserResolver;
import org.eclipse.osee.ats.api.workdef.ReviewBlockType;
import org.eclipse.osee.ats.api.workdef.StateColor;
import org.eclipse.osee.ats.api.workdef.StateEventType;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workdef.WidgetOption;
import org.eclipse.osee.ats.dsl.BooleanDefUtil;
import org.eclipse.osee.ats.dsl.UserRefUtil;
import org.eclipse.osee.ats.dsl.atsDsl.AtsDsl;
import org.eclipse.osee.ats.dsl.atsDsl.AttrWidget;
import org.eclipse.osee.ats.dsl.atsDsl.Composite;
import org.eclipse.osee.ats.dsl.atsDsl.DecisionReviewDef;
import org.eclipse.osee.ats.dsl.atsDsl.DecisionReviewOpt;
import org.eclipse.osee.ats.dsl.atsDsl.DecisionReviewRef;
import org.eclipse.osee.ats.dsl.atsDsl.FollowupRef;
import org.eclipse.osee.ats.dsl.atsDsl.LayoutCopy;
import org.eclipse.osee.ats.dsl.atsDsl.LayoutDef;
import org.eclipse.osee.ats.dsl.atsDsl.LayoutItem;
import org.eclipse.osee.ats.dsl.atsDsl.LayoutType;
import org.eclipse.osee.ats.dsl.atsDsl.PeerReviewDef;
import org.eclipse.osee.ats.dsl.atsDsl.PeerReviewRef;
import org.eclipse.osee.ats.dsl.atsDsl.StateDef;
import org.eclipse.osee.ats.dsl.atsDsl.ToState;
import org.eclipse.osee.ats.dsl.atsDsl.UserByName;
import org.eclipse.osee.ats.dsl.atsDsl.UserByUserId;
import org.eclipse.osee.ats.dsl.atsDsl.UserRef;
import org.eclipse.osee.ats.dsl.atsDsl.WidgetDef;
import org.eclipse.osee.ats.dsl.atsDsl.WidgetRef;
import org.eclipse.osee.ats.impl.internal.model.CompositeLayoutItem;
import org.eclipse.osee.ats.impl.internal.model.DecisionReviewDefinition;
import org.eclipse.osee.ats.impl.internal.model.DecisionReviewOption;
import org.eclipse.osee.ats.impl.internal.model.PeerReviewDefinition;
import org.eclipse.osee.ats.impl.internal.model.StateDefinition;
import org.eclipse.osee.ats.impl.internal.model.WidgetDefinition;
import org.eclipse.osee.ats.impl.internal.model.WidgetDefinitionFloatMinMaxConstraint;
import org.eclipse.osee.ats.impl.internal.model.WidgetDefinitionIntMinMaxConstraint;
import org.eclipse.osee.ats.impl.internal.model.WidgetDefinitionListMinMaxSelectedConstraint;
import org.eclipse.osee.ats.impl.internal.model.WorkDefinition;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.util.Strings;

public class ConvertAtsDslToWorkDefinition {

   private final String name;
   private final AtsDsl atsDsl;
   private final XResultData resultData;
   private final IAttributeResolver attrResolver;
   private final IUserResolver userResolver;

   public ConvertAtsDslToWorkDefinition(String name, AtsDsl atsDsl, XResultData resultData, IAttributeResolver attrResolver, IUserResolver userResolver) {
      this.name = name;
      this.atsDsl = atsDsl;
      this.resultData = resultData;
      this.attrResolver = attrResolver;
      this.userResolver = userResolver;
   }

   public IAtsWorkDefinition convert() {
      if (atsDsl.getWorkDef() == null) {
         return null;
      }
      WorkDefinition workDef = new WorkDefinition(Strings.unquote(atsDsl.getWorkDef().getName()));
      workDef.setId(atsDsl.getWorkDef().getId().iterator().next());

      List<IAtsWidgetDefinition> widgetDefs = retrieveWigetDefs(atsDsl, name);
      Map<IAtsStateDefinition, String> copyLayoutFromMap = new HashMap<IAtsStateDefinition, String>();

      // Process and define all states
      for (StateDef dslState : atsDsl.getWorkDef().getStates()) {
         String stateName = Strings.unquote(dslState.getName());
         StateDefinition stateDef = new StateDefinition(stateName);
         workDef.addState(stateDef);

         stateDef.setWorkDefinition(workDef);

         // Process state settings
         stateDef.setOrdinal(dslState.getOrdinal());
         StateType stateType = StateType.Working;
         try {
            stateType = StateType.valueOf(dslState.getPageType());
         } catch (IllegalArgumentException ex) {
            // do nothing
         }
         stateDef.setStateType(stateType);
         stateDef.setStateWeight(dslState.getPercentWeight());
         stateDef.setRecommendedPercentComplete(dslState.getRecommendedPercentComplete());
         StateColor color = StateColor.BLACK;
         try {
            if (Strings.isValid(dslState.getColor())) {
               color = StateColor.valueOf(dslState.getColor());
            }
         } catch (IllegalArgumentException ex) {
            // do nothing
         }
         stateDef.setColor(color);

         // Process widgets
         LayoutType layout = dslState.getLayout();
         if (layout instanceof LayoutDef) {
            processLayoutItems(name, widgetDefs, stateDef.getLayoutItems(), ((LayoutDef) layout).getLayoutItems());
         } else if (layout instanceof LayoutCopy) {
            copyLayoutFromMap.put(stateDef, Strings.unquote(((LayoutCopy) layout).getState().getName()));
         }

         // process rules
         for (String ruleName : dslState.getRules()) {
            stateDef.addRule(Strings.unquote(ruleName));
         }

      }

      // Process States needing layoutCopy
      for (Entry<IAtsStateDefinition, String> entry : copyLayoutFromMap.entrySet()) {
         IAtsStateDefinition fromStateDef = workDef.getStateByName(entry.getValue());
         IAtsStateDefinition toStateDef = entry.getKey();
         for (IAtsLayoutItem item : fromStateDef.getLayoutItems()) {
            toStateDef.getLayoutItems().add(item);
         }
      }

      // Process and define all transitions
      for (StateDef dslState : atsDsl.getWorkDef().getStates()) {
         StateDefinition stateDef = (StateDefinition) workDef.getStateByName(Strings.unquote(dslState.getName()));
         // Process transitions
         for (ToState dslToState : dslState.getTransitionStates()) {
            IAtsStateDefinition toStateDef = workDef.getStateByName(Strings.unquote(dslToState.getState().getName()));
            stateDef.getToStates().add(toStateDef);
            for (String dslTransOption : dslToState.getOptions()) {
               if ("AsDefault".equals(dslTransOption)) {
                  stateDef.setDefaultToState(toStateDef);
               }
               if ("OverrideAttributeValidation".equals(dslTransOption)) {
                  stateDef.getOverrideAttributeValidationStates().add(toStateDef);
               }
            }
         }
      }

      // Process all decision reviews
      for (StateDef dslState : atsDsl.getWorkDef().getStates()) {
         IAtsStateDefinition stateDef = workDef.getStateByName(Strings.unquote(dslState.getName()));
         for (DecisionReviewRef dslRevRef : dslState.getDecisionReviews()) {
            DecisionReviewDef dslRevDef = dslRevRef.getDecisionReview();
            DecisionReviewDefinition revDef = (DecisionReviewDefinition) convertDslDecisionReview(dslRevDef);
            if (!Strings.isValid(revDef.getRelatedToState())) {
               revDef.setRelatedToState(stateDef.getName());
            }
            stateDef.getDecisionReviews().add(revDef);
         }
      }

      // Process all peer reviews
      for (StateDef dslState : atsDsl.getWorkDef().getStates()) {
         IAtsStateDefinition stateDef = workDef.getStateByName(Strings.unquote(dslState.getName()));
         for (PeerReviewRef peerRevRef : dslState.getPeerReviews()) {
            PeerReviewDef dslRevDef = peerRevRef.getPeerReview();
            PeerReviewDefinition revDef = (PeerReviewDefinition) convertDslPeerReview(dslRevDef);
            if (!Strings.isValid(revDef.getRelatedToState())) {
               revDef.setRelatedToState(stateDef.getName());
            }
            stateDef.getPeerReviews().add(revDef);
         }
      }

      // Set the start state
      workDef.setStartState(workDef.getStateByName(Strings.unquote(atsDsl.getWorkDef().getStartState().getName())));
      workDef.setName(name);
      workDef.setId(name);
      return workDef;
   }

   private IAtsDecisionReviewDefinition convertDslDecisionReview(DecisionReviewDef dslRevDef) {
      DecisionReviewDefinition revDef = new DecisionReviewDefinition(dslRevDef.getName());
      revDef.setReviewTitle(dslRevDef.getTitle());
      revDef.setDescription(dslRevDef.getDescription());

      String dslBlockType = dslRevDef.getBlockingType().getName();
      ReviewBlockType blockType = ReviewBlockType.None;
      try {
         blockType = ReviewBlockType.valueOf(dslBlockType);
      } catch (IllegalArgumentException ex) {
         throw new IllegalArgumentException(String.format("Unknown ReviewBlockType [%s]; Defaulting to None",
            dslBlockType));
      }
      revDef.setBlockingType(blockType);

      String dslEventType = dslRevDef.getStateEvent().getName();
      StateEventType eventType = StateEventType.None;
      try {
         eventType = StateEventType.valueOf(dslEventType);
      } catch (IllegalArgumentException ex) {
         throw new IllegalArgumentException(String.format("Unknown StateEventType [%s]; Defaulting to None",
            dslEventType));
      }
      revDef.setStateEventType(eventType);
      revDef.setAutoTransitionToDecision(BooleanDefUtil.get(dslRevDef.getAutoTransitionToDecision(), false));

      for (DecisionReviewOpt dslOpt : dslRevDef.getOptions()) {
         IAtsDecisionReviewOption revOpt = new DecisionReviewOption(Strings.unquote(dslOpt.getName()));
         FollowupRef followupRef = dslOpt.getFollowup();
         if (followupRef == null) {
            revOpt.setFollowupRequired(false);
         } else {
            revOpt.getUserIds().addAll(UserRefUtil.getUserIds(followupRef.getAssigneeRefs()));
            revOpt.getUserNames().addAll(UserRefUtil.getUserNames(followupRef.getAssigneeRefs()));
         }
         revDef.getOptions().add(revOpt);
      }

      Collection<String> userIds = getAssigneesFromUserRefs(dslRevDef.getAssigneeRefs());
      revDef.getAssignees().addAll(userIds);
      return revDef;
   }

   private void processLayoutItems(String SHEET_NAME, List<IAtsWidgetDefinition> widgetDefs, List<IAtsLayoutItem> stateItems, EList<LayoutItem> layoutItems) {
      for (LayoutItem layoutItem : layoutItems) {
         if (layoutItem instanceof WidgetDef) {
            IAtsWidgetDefinition widgetDef = convertDslWidgetDef((WidgetDef) layoutItem, SHEET_NAME);
            stateItems.add(widgetDef);
         } else if (layoutItem instanceof WidgetRef) {
            String widgetName = Strings.unquote(((WidgetRef) layoutItem).getWidget().getName());
            boolean found = false;
            for (IAtsWidgetDefinition wd : widgetDefs) {
               if (wd.getName().equals(widgetName)) {
                  stateItems.add(wd);
                  found = true;
               }
            }
            if (!found) {
               resultData.logErrorWithFormat("Could not find WidgetRef [%s] in WidgetDefs", widgetName);
            }
         } else if (layoutItem instanceof AttrWidget) {
            AttrWidget attrWidget = (AttrWidget) layoutItem;
            String attributeName = Strings.unquote(attrWidget.getAttributeName());
            try {
               if (!attrResolver.isAttributeNamed(attributeName)) {
                  resultData.logErrorWithFormat("Invalid attribute name [%s] in WorkDefinition [%s] (1)",
                     attributeName, SHEET_NAME);
               } else {
                  WidgetDefinition widgetDef = new WidgetDefinition(attrResolver.getUnqualifiedName(attributeName));
                  widgetDef.setAttributeName(attributeName);
                  attrResolver.setXWidgetNameBasedOnAttributeName(attributeName, widgetDef);
                  extractDslWidgetDefOptions(attrWidget.getOption(), SHEET_NAME, widgetDef);
                  stateItems.add(widgetDef);
               }
            } catch (Exception ex) {
               resultData.logErrorWithFormat("Error resolving attribute [%s] to WorkDefinition in [%s]", attributeName,
                  SHEET_NAME);
            }
         } else if (layoutItem instanceof Composite) {
            Composite composite = (Composite) layoutItem;
            IAtsCompositeLayoutItem compStateItem = new CompositeLayoutItem(composite.getNumColumns());
            if (!composite.getLayoutItems().isEmpty()) {
               processLayoutItems(SHEET_NAME, widgetDefs, compStateItem.getaLayoutItems(), composite.getLayoutItems());
            }
            stateItems.add(compStateItem);
         }
      }

   }

   private IAtsPeerReviewDefinition convertDslPeerReview(PeerReviewDef dslRevDef) {
      PeerReviewDefinition revDef = new PeerReviewDefinition(Strings.unquote(dslRevDef.getName()));
      revDef.setReviewTitle(Strings.unquote(dslRevDef.getTitle()));
      revDef.setDescription(Strings.unquote(dslRevDef.getDescription()));
      revDef.setLocation(Strings.unquote(dslRevDef.getLocation()));

      String dslBlockType = dslRevDef.getBlockingType().getName();
      ReviewBlockType blockType = ReviewBlockType.None;
      try {
         blockType = ReviewBlockType.valueOf(dslBlockType);
      } catch (IllegalArgumentException ex) {
         resultData.logWarningWithFormat("Unknown ReviewBlockType [%s]; Defaulting to None", dslBlockType);
      }
      revDef.setBlockingType(blockType);

      String dslEventType = Strings.unquote(dslRevDef.getStateEvent().getName());
      StateEventType eventType = StateEventType.None;
      try {
         eventType = StateEventType.valueOf(dslEventType);
      } catch (IllegalArgumentException ex) {
         resultData.logWarningWithFormat("Unknown StateEventType [%s]; Defaulting to None", dslEventType);
      }
      revDef.setStateEventType(eventType);
      Collection<String> userIds = getAssigneesFromUserRefs(dslRevDef.getAssigneeRefs());
      revDef.getAssignees().addAll(userIds);
      return revDef;
   }

   private Collection<String> getAssigneesFromUserRefs(EList<UserRef> UserRefs) {
      Set<String> userIds = new HashSet<String>();
      for (UserRef UserRef : UserRefs) {
         if (UserRef instanceof UserByName) {
            UserByName byName = (UserByName) UserRef;
            String name = Strings.unquote(byName.getUserName());
            if (!Strings.isValid(name)) {
               resultData.logWarningWithFormat("Unhandled UserByName name [%s]", name);
               continue;
            }
            if (userResolver.isUserNameValid(name)) {
               userIds.add(userResolver.getUserIdByName(name));
            } else {
               resultData.logWarningWithFormat("No user by name [%s]", name);
            }
         } else if (UserRef instanceof UserByUserId) {
            UserByUserId byUserId = (UserByUserId) UserRef;
            String userId = Strings.unquote(byUserId.getUserId());
            if (!Strings.isValid(userId)) {
               resultData.logWarningWithFormat("Unhandled UserByUserId id [%s]", userId);
               continue;
            }
            if (userResolver.isUserIdValid(userId)) {
               userIds.add(userId);
            } else {
               resultData.logWarningWithFormat("No user by id [%s]", userId);
            }
         } else {
            resultData.logWarningWithFormat("Unhandled UserRef type [%s]", UserRef);
         }
      }
      return userIds;
   }

   private List<IAtsWidgetDefinition> retrieveWigetDefs(AtsDsl atsDsl, String SHEET_NAME) {
      List<IAtsWidgetDefinition> widgetDefs = new ArrayList<IAtsWidgetDefinition>();
      for (WidgetDef dslWidgetDef : atsDsl.getWorkDef().getWidgetDefs()) {
         IAtsWidgetDefinition widgetDef = convertDslWidgetDef(dslWidgetDef, SHEET_NAME);
         widgetDefs.add(widgetDef);
      }
      return widgetDefs;
   }

   private IAtsWidgetDefinition convertDslWidgetDef(WidgetDef dslWidgetDef, String SHEET_NAME) {
      WidgetDefinition widgetDef = new WidgetDefinition(Strings.unquote(dslWidgetDef.getName()));
      String attributeName = Strings.unquote(dslWidgetDef.getAttributeName());
      widgetDef.setAttributeName(attributeName);
      try {
         // Set description if model defines it
         if (Strings.isValid(dslWidgetDef.getDescription())) {
            widgetDef.setDescription(dslWidgetDef.getDescription());
         }
         // Else, set if AtsAttributeTypes defines it
         else if (Strings.isValid(attributeName) && attrResolver.isAttributeNamed(attributeName) && Strings.isValid(attrResolver.getDescription(attributeName))) {
            widgetDef.setDescription(attrResolver.getDescription(attributeName));
         }
      } catch (Exception ex) {
         resultData.logErrorWithFormat("Exception [%s] in WorkDefinition [%s]", ex.getLocalizedMessage(), SHEET_NAME);
      }

      String xWidgetName = Strings.unquote(dslWidgetDef.getXWidgetName());
      if (Strings.isValid(xWidgetName)) {
         widgetDef.setXWidgetName(xWidgetName);
      } else {
         if (Strings.isValid(attributeName)) {
            try {
               if (!attrResolver.isAttributeNamed(attributeName)) {
                  resultData.logErrorWithFormat("Invalid attribute name [%s] in WorkDefinition [%s] (2)",
                     attributeName, SHEET_NAME);
               } else {
                  attrResolver.setXWidgetNameBasedOnAttributeName(attributeName, widgetDef);
               }
            } catch (Exception ex) {
               resultData.logErrorWithFormat("Error resolving attribute name [%s] in WorkDefinition [%s]",
                  attributeName, SHEET_NAME);
            }
         } else {
            resultData.logErrorWithFormat("Invalid attribute name [%s] in WorkDefinition [%s] (3)", attributeName,
               SHEET_NAME);
         }
      }
      processMinMaxConstraints(widgetDef, dslWidgetDef.getMinConstraint(), dslWidgetDef.getMaxConstraint());

      widgetDef.setHeight(dslWidgetDef.getHeight());
      widgetDef.setDefaultValue(dslWidgetDef.getDefaultValue());
      extractDslWidgetDefOptions(dslWidgetDef.getOption(), SHEET_NAME, widgetDef);
      return widgetDef;
   }

   private void processMinMaxConstraints(IAtsWidgetDefinition widgetDef, String minConstraint, String maxConstraint) {
      if (!Strings.isValid(minConstraint) && !Strings.isValid(maxConstraint)) {
         return;
      }
      if (widgetDef.getXWidgetName().contains("Float")) {
         widgetDef.getConstraints().add(new WidgetDefinitionFloatMinMaxConstraint(minConstraint, minConstraint));
      }
      if (widgetDef.getXWidgetName().contains("Integer")) {
         widgetDef.getConstraints().add(new WidgetDefinitionIntMinMaxConstraint(minConstraint, minConstraint));
      }
      if (widgetDef.getXWidgetName().contains("List")) {
         widgetDef.getConstraints().add(new WidgetDefinitionListMinMaxSelectedConstraint(minConstraint, minConstraint));
      }
   }

   private void extractDslWidgetDefOptions(EList<String> options, String SHEET_NAME, IAtsWidgetDefinition widgetDef) {
      for (String value : options) {
         WidgetOption option = null;
         try {
            option = WidgetOption.valueOf(value);
            widgetDef.getOptions().add(option);
         } catch (IllegalArgumentException ex) {
            resultData.logWarningWithFormat("Unexpected value [%s] in WorkDefinition [%s]", value, SHEET_NAME);
         }
      }
   }

}
