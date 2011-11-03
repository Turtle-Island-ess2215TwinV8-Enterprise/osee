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
package org.eclipse.osee.display.presenter.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.api.view.ArtifactView;
import org.eclipse.osee.display.api.view.ArtifactView.ArtifactViewListener;
import org.eclipse.osee.display.mvp.BindException;
import org.eclipse.osee.display.mvp.event.annotation.EndPoint;
import org.eclipse.osee.display.mvp.presenter.AbstractPresenter;
import org.eclipse.osee.display.presenter.ArtifactProvider;
import org.eclipse.osee.display.presenter.AttributeTypeUtil;
import org.eclipse.osee.display.presenter.events.SearchEventBus;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.data.ReadableAttribute;

/**
 * @author John Misinco
 */
public class ArtifactPresenter extends AbstractPresenter<ArtifactView, SearchEventBus> implements ArtifactViewListener {

   private final ArtifactProvider artifactProvider;
   private ReadableArtifact selectedArtifact;

   final static String SIDE_A_KEY = "sideAName";
   final static String SIDE_B_KEY = "sideBName";

   public ArtifactPresenter(ArtifactProvider artifactProvider) {
      this.artifactProvider = artifactProvider;
   }

   @Override
   public void bind() throws BindException {
      getView().setViewListener(this);
   }

   public void setSelectedArtifact(ReadableArtifact selectedArtifact) {
      this.selectedArtifact = selectedArtifact;
   }

   public void onArtifactSelected(String artifactGuid, String branchGuid) {
      selectedArtifact = null;
      try {
         selectedArtifact = artifactProvider.getArtifactByGuid(TokenFactory.createBranch(branchGuid, ""), artifactGuid);
      } catch (Exception ex) {
         setErrorMessage("Error finding artifact", ex);
         return;
      }
      if (selectedArtifact == null) {
         setErrorMessage(String.format("No artifact[%s] found on branch:[%s]", artifactGuid, branchGuid), null);
         return;
      }

      getView().setName(selectedArtifact.getName());
      List<ViewId> ancestry = null;
      try {
         ancestry = ArtifactUtil.getAncestry(selectedArtifact, artifactProvider);
      } catch (OseeCoreException ex) {
         setErrorMessage("Error getting ancestry", ex);
      }
      getView().setBreadCrumbs(ancestry);

      getView().clearRelations();
      Collection<RelationType> relationTypes = null;
      try {
         relationTypes = artifactProvider.getValidRelationTypes(selectedArtifact);
      } catch (Exception ex) {
         setErrorMessage("Error in initArtifactPage:\n Cannot load valid relation types", ex);
         return;
      }
      for (RelationType relTypeSide : relationTypes) {
         ViewId toAdd = new ViewId(relTypeSide.getGuid().toString(), relTypeSide.getName());
         toAdd.setAttribute(SIDE_A_KEY, relTypeSide.getSideAName());
         toAdd.setAttribute(SIDE_B_KEY, relTypeSide.getSideBName());
         getView().addRelationType(toAdd);
      }

      getView().clearAttributes();
      Collection<IAttributeType> attributeTypes = null;
      try {
         attributeTypes = AttributeTypeUtil.getTypesWithData(selectedArtifact);
      } catch (Exception ex) {
         setErrorMessage("Error in initArtifactPage:\n Cannot load attribute types", ex);
         return;
      }
      for (IAttributeType attrType : attributeTypes) {
         List<ReadableAttribute<Object>> attributesValues = null;
         try {
            attributesValues = selectedArtifact.getAttributes(attrType);
            for (ReadableAttribute<Object> value : attributesValues) {
               getView().addAttribute(attrType.getName(), value.getDisplayableString());
            }
         } catch (Exception ex) {
            setErrorMessage("Error in initArtifactPage:\n Cannot load attribute values", ex);
            return;
         }
      }
   }

   @Override
   public void onRelationTypeSelected() {
      ViewId relation = getView().getSelectedRelationType();

      getView().clearRelations();
      if (selectedArtifact == null || relation == null) {
         setErrorMessage("Error: Null detected in selectRelationType parameters", null);
         return;
      }

      String relGuid = relation.getGuid();

      IRelationType type = TokenFactory.createRelationType(Long.parseLong(relGuid), relation.getName());
      Collection<ReadableArtifact> relatedSideA = Collections.emptyList();
      Collection<ReadableArtifact> relatedSideB = Collections.emptyList();
      try {
         relatedSideA =
            artifactProvider.getRelatedArtifacts(selectedArtifact,
               TokenFactory.createRelationTypeSide(RelationSide.SIDE_A, type.getGuid(), type.getName()));
         relatedSideB =
            artifactProvider.getRelatedArtifacts(selectedArtifact,
               TokenFactory.createRelationTypeSide(RelationSide.SIDE_B, type.getGuid(), type.getName()));
      } catch (Exception ex) {
         setErrorMessage("Error in selectRelationType", ex);
         return;
      }

      String leftSideName = Strings.capitalize(relation.getAttribute(SIDE_A_KEY));
      String rightSideName = Strings.capitalize(relation.getAttribute(SIDE_B_KEY));
      getView().setSideATitle(leftSideName);
      getView().setSideBTitle(rightSideName);

      if (relatedSideA.isEmpty()) {
         getView().addSideAValue(null);
      }
      if (relatedSideB.isEmpty()) {
         getView().addSideBValue(null);
      }

      try {
         for (ReadableArtifact rel : relatedSideA) {
            ViewId id = new ViewId(rel.getGuid(), rel.getName());
            getView().addSideAValue(id);
         }
         for (ReadableArtifact rel : relatedSideB) {
            ViewId id = new ViewId(rel.getGuid(), rel.getName());
            getView().addSideBValue(id);
         }
      } catch (Exception ex) {
         setErrorMessage("Error in selectRelationType", ex);
         return;
      }
   }

   @Override
   public void onRelatedArtifactSelected() {
      ViewId selectedRelatedArtifact = getView().getSelectedRelatedArtifact();
      onArtifactSelected(selectedRelatedArtifact.getGuid(), selectedArtifact.getBranch().getGuid());
   }

   @EndPoint
   public void onSendResultSelected(String artifactGuid, String branchGuid) {
      onArtifactSelected(artifactGuid, branchGuid);
   }

}
