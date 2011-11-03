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
package org.eclipse.osee.display.presenter.mocks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.api.view.ArtifactView;
import org.eclipse.osee.display.mvp.MessageType;
import org.eclipse.osee.logger.Log;

/**
 * @author John Misinco
 */
public class MockArtifactView implements ArtifactView {

   private String name, sideATitle, sideBTitle;
   private List<ViewId> crumbs;
   private final List<ViewId> relationTypes = new LinkedList<ViewId>();
   private final List<ViewId> sideARelated = new LinkedList<ViewId>();
   private final List<ViewId> sideBRelated = new LinkedList<ViewId>();
   boolean clearAllCalled = false, clearRelationsCalled = false;
   Map<String, String> attributes = new HashMap<String, String>();
   private ViewId selectedRelationType;
   private ViewId selectedRelatedArtifact;

   public void setSelectedRelatedArtifact(ViewId id) {
      selectedRelatedArtifact = id;
   }

   public void setSelectedRelationType(ViewId selected) {
      selectedRelationType = selected;
   }

   public List<ViewId> getSideAValues() {
      return sideARelated;
   }

   public List<ViewId> getSideBValues() {
      return sideBRelated;
   }

   public Map<String, String> getAttributes() {
      return attributes;
   }

   public String getSideATitle() {
      return sideATitle;
   }

   public String getSideBTitle() {
      return sideBTitle;
   }

   public List<ViewId> getRelationTypes() {
      return relationTypes;
   }

   public boolean isClearAllCalled() {
      return clearAllCalled;
   }

   public boolean isClearRelationsCalled() {
      return clearRelationsCalled;
   }

   public List<ViewId> getCrumbs() {
      return crumbs;
   }

   public String getName() {
      return name;
   }

   @Override
   public Log getLogger() {
      return null;
   }

   @Override
   public void setLogger(Log logger) {
   }

   @Override
   public void displayMessage(String caption) {
   }

   @Override
   public void dispose() {
   }

   @Override
   public boolean isDisposed() {
      return false;
   }

   @Override
   public void setName(String name) {
      this.name = name;
   }

   @Override
   public void setBreadCrumbs(List<ViewId> crumbs) {
      this.crumbs = crumbs;
   }

   @Override
   public void clearAll() {
      clearAllCalled = true;
   }

   @Override
   public void clearRelations() {
      clearRelationsCalled = true;
      sideARelated.clear();
      sideBRelated.clear();
      relationTypes.clear();
   }

   @Override
   public void addRelationType(ViewId relationType) {
      relationTypes.add(relationType);
   }

   @Override
   public void setSideATitle(String name) {
      sideATitle = name;
   }

   @Override
   public void setSideBTitle(String name) {
      sideBTitle = name;
   }

   @Override
   public void addSideAValue(ViewId id) {
      sideARelated.add(id);
   }

   @Override
   public void addSideBValue(ViewId id) {
      sideBRelated.add(id);
   }

   @Override
   public ViewId getSelectedRelationType() {
      return selectedRelationType;
   }

   @Override
   public ViewId getSelectedRelatedArtifact() {
      return selectedRelatedArtifact;
   }

   @Override
   public void addAttribute(String name, String type) {
      attributes.put(name, type);
   }

   @Override
   public void clearAttributes() {
   }

   @Override
   public void setViewListener(ArtifactViewListener listener) {
   }

   @Override
   public void displayMessage(String caption, String description, MessageType messageType) {
   }

   @Override
   public Object getContent() {
      return null;
   }

}