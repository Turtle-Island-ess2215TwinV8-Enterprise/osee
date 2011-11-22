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

package org.eclipse.osee.ats.world.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.core.config.ActionableItemArtifact;
import org.eclipse.osee.ats.core.util.AtsCacheManager;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.ats.shared.AtsAttributeTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.dialog.ActionActionableItemListDialog;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.AbstractArtifactSearchCriteria;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.search.AttributeCriteria;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;

/**
 * @author Donald G. Dunne
 */
public class ActionableItemWorldSearchItem extends WorldUISearchItem {

   private Collection<ActionableItemArtifact> actionItems;
   private Set<ActionableItemArtifact> selectedActionItems;
   private boolean recurseChildren;
   private boolean selectedRecurseChildren; // Used to not corrupt original values
   private boolean showFinished;
   private boolean selectedShowFinished; // Used to not corrupt original values
   private boolean showAction;
   private boolean selectedShowAction; // Used to not corrupt original values
   private final Collection<String> actionItemNames;

   public ActionableItemWorldSearchItem(Collection<String> actionItemNames, String displayName, boolean showFinished, boolean recurseChildren, boolean showAction) {
      super(displayName, AtsImage.ACTIONABLE_ITEM);
      this.actionItemNames = actionItemNames;
      this.showFinished = showFinished;
      this.selectedShowFinished = showFinished; // Set as default in case UI is not used
      this.recurseChildren = recurseChildren;
      this.selectedRecurseChildren = recurseChildren; // Set as default in case UI is not used
      this.showAction = showAction;
      this.selectedShowAction = showAction;
   }

   public ActionableItemWorldSearchItem(String displayName, Collection<ActionableItemArtifact> actionItems, boolean showFinished, boolean recurseChildren, boolean showAction) {
      super(displayName, AtsImage.ACTIONABLE_ITEM);
      this.actionItemNames = null;
      this.actionItems = actionItems;
      this.showFinished = showFinished;
      this.recurseChildren = recurseChildren;
      this.showAction = showAction;
   }

   public ActionableItemWorldSearchItem(ActionableItemWorldSearchItem item) {
      super(item, AtsImage.ACTIONABLE_ITEM);
      this.actionItemNames = item.actionItemNames;
      this.actionItems = item.actionItems;
      this.showFinished = item.showFinished;
      this.recurseChildren = item.recurseChildren;
      this.showAction = item.showAction;
   }

   public Collection<String> getProductSearchName() {
      if (actionItemNames != null) {
         return actionItemNames;
      } else if (actionItems != null) {
         return Artifacts.getNames(actionItems);
      } else if (selectedActionItems != null) {
         return Artifacts.getNames(selectedActionItems);
      }
      return new ArrayList<String>();
   }

   @Override
   public String getSelectedName(SearchType searchType) throws OseeCoreException {
      return String.format("%s - %s", super.getSelectedName(searchType), getProductSearchName());
   }

   public void getActionableItems() {
      if (actionItemNames != null && actionItems == null) {
         actionItems = new HashSet<ActionableItemArtifact>();
         for (String actionItemName : actionItemNames) {
            ActionableItemArtifact aia =
               (ActionableItemArtifact) AtsCacheManager.getSoleArtifactByName(AtsArtifactTypes.ActionableItem,
                  actionItemName);
            if (aia != null) {
               actionItems.add(aia);
            }
         }
      }
   }

   /**
    * @return All directly specified teamDefs plus if recurse, will get all children
    */
   private Set<ActionableItemArtifact> getSearchActionableItems() throws OseeCoreException {
      getActionableItems();
      Set<ActionableItemArtifact> srchTeamDefs = new HashSet<ActionableItemArtifact>();
      for (ActionableItemArtifact actionableItem : actionItems != null ? actionItems : selectedActionItems) {
         srchTeamDefs.add(actionableItem);
      }
      if (selectedRecurseChildren) {
         for (ActionableItemArtifact actionableItem : actionItems != null ? actionItems : selectedActionItems) {
            Artifacts.getChildrenOfType(actionableItem, srchTeamDefs, ActionableItemArtifact.class, true);
         }
      }
      return srchTeamDefs;
   }

   @Override
   public Collection<Artifact> performSearch(SearchType searchType) throws OseeCoreException {
      Set<ActionableItemArtifact> items = getSearchActionableItems();
      List<String> actionItemGuids = new ArrayList<String>(items.size());
      for (ActionableItemArtifact ai : items) {
         actionItemGuids.add(ai.getGuid());
      }
      List<AbstractArtifactSearchCriteria> criteria = new ArrayList<AbstractArtifactSearchCriteria>();

      criteria.add(new AttributeCriteria(AtsAttributeTypes.ActionableItem, actionItemGuids));
      // exclude completed or canceled
      if (!selectedShowFinished) {
         TeamWorldSearchItem.addIncludeCompletedCancelledCriteria(criteria, false, false);
      }
      Collection<Artifact> artifacts =
         ArtifactQuery.getArtifactListFromCriteria(AtsUtil.getAtsBranch(), 1000, criteria);
      // show as actions
      if (selectedShowAction) {
         Set<Artifact> arts = new HashSet<Artifact>();
         for (Artifact art : artifacts) {
            if (art.isOfType(AtsArtifactTypes.Action)) {
               arts.add(art);
            } else if (art instanceof AbstractWorkflowArtifact) {
               Artifact parentAction = ((AbstractWorkflowArtifact) art).getParentActionArtifact();
               if (parentAction != null) {
                  arts.add(parentAction);
               }
            }
         }
         return arts;
      } else {
         return artifacts;
      }
   }

   @Override
   public void performUI(SearchType searchType) throws OseeCoreException {
      super.performUI(searchType);
      if (actionItemNames != null) {
         return;
      }
      if (actionItems != null) {
         return;
      }
      if (searchType == SearchType.ReSearch && selectedActionItems != null) {
         return;
      }
      ActionActionableItemListDialog diag = new ActionActionableItemListDialog(Active.Both);
      diag.setShowFinished(showFinished);
      diag.setRecurseChildren(recurseChildren);
      diag.setShowAction(showAction);
      int result = diag.open();
      if (result == 0) {
         selectedShowFinished = diag.isShowFinished();
         selectedRecurseChildren = diag.isRecurseChildren();
         selectedShowAction = diag.isShowAction();
         if (selectedActionItems == null) {
            selectedActionItems = new HashSet<ActionableItemArtifact>();
         } else {
            selectedActionItems.clear();
         }
         for (Object obj : diag.getResult()) {
            selectedActionItems.add((ActionableItemArtifact) obj);
         }
         return;
      }
      cancelled = true;
   }

   /**
    * @param showFinished The showFinished to set.
    */
   public void setShowFinished(boolean showFinished) {
      this.showFinished = showFinished;
   }

   /**
    * @return the recurseChildren
    */
   public boolean isRecurseChildren() {
      return recurseChildren;
   }

   /**
    * @param recurseChildren the recurseChildren to set
    */
   public void setRecurseChildren(boolean recurseChildren) {
      this.recurseChildren = recurseChildren;
   }

   public void setShowAction(boolean showAction) {
      this.showAction = showAction;
   }

   /**
    * @param selectedActionItems the selectedActionItems to set
    */
   public void setSelectedActionItems(Set<ActionableItemArtifact> selectedActionItems) {
      this.selectedActionItems = selectedActionItems;
   }

   @Override
   public WorldUISearchItem copy() {
      return new ActionableItemWorldSearchItem(this);
   }

}
