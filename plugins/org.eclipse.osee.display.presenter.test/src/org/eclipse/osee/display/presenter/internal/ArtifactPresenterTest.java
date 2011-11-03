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

import java.util.LinkedList;
import java.util.List;
import junit.framework.Assert;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.presenter.mocks.MockArtifact;
import org.eclipse.osee.display.presenter.mocks.MockArtifactProvider;
import org.eclipse.osee.display.presenter.mocks.MockArtifactView;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.RelationTypeMultiplicity;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.junit.Test;

/**
 * @author John Misinco
 */
public class ArtifactPresenterTest {

   private void setupTestData(MockArtifactProvider provider, String artGuid) {
      MockArtifact testArt = new MockArtifact(artGuid, "name", CoreArtifactTypes.Artifact, CoreBranches.COMMON);
      MockArtifact parentArt = new MockArtifact(GUID.create(), "parent");
      MockArtifact grandParentArt = new MockArtifact(GUID.create(), "grandParent");
      parentArt.setParent(grandParentArt);
      testArt.setParent(parentArt);
      RelationType relType =
         new RelationType(0L, "typeName", "sideA", "sideB", CoreArtifactTypes.AbstractSoftwareRequirement,
            CoreArtifactTypes.AbstractTestResult, RelationTypeMultiplicity.ONE_TO_ONE, "");
      testArt.addRelationType(relType);
      provider.addArtifact(testArt);
   }

   @Test
   public void testOnArtifactSelected() {
      MockArtifactProvider provider = new MockArtifactProvider();
      String artGuid = GUID.create();
      String branchGuid = CoreBranches.COMMON.getGuid();
      setupTestData(provider, artGuid);

      ArtifactPresenter presenter = new ArtifactPresenter(provider);
      MockArtifactView view = new MockArtifactView();
      presenter.setView(view);
      presenter.onArtifactSelected(artGuid, branchGuid);

      Assert.assertEquals("name", view.getName());
      Assert.assertEquals(2, view.getCrumbs().size());
      Assert.assertEquals(1, view.getRelationTypes().size());
      Assert.assertEquals(1, view.getAttributes().keySet().size());
   }

   @Test
   public void testOnRelationTypeSelected() {
      RelationTypeArtifactProvider provider = new RelationTypeArtifactProvider();
      ArtifactPresenter presenter = new ArtifactPresenter(provider);
      MockArtifactView view = new MockArtifactView();
      presenter.setView(view);
      ViewId selectedType = new ViewId("0", "relTypeName");
      selectedType.setAttribute(ArtifactPresenter.SIDE_A_KEY, "sideAName");
      selectedType.setAttribute(ArtifactPresenter.SIDE_B_KEY, "sideBName");
      view.setSelectedRelationType(selectedType);
      presenter.setSelectedArtifact(new MockArtifact("", ""));

      List<ReadableArtifact> relatedArtifacts = new LinkedList<ReadableArtifact>();
      List<ReadableArtifact> emptyList = new LinkedList<ReadableArtifact>();
      relatedArtifacts.add(new MockArtifact("art1Guid", "art1Name"));
      relatedArtifacts.add(new MockArtifact("art2Guid", "art2Name"));

      // just test side A
      provider.setRelatedSideAArtifacts(relatedArtifacts);
      provider.setRelatedSideBArtifacts(emptyList);
      presenter.onRelationTypeSelected();

      Assert.assertEquals("SideAName", view.getSideATitle());
      Assert.assertEquals("SideBName", view.getSideBTitle());
      Assert.assertEquals(2, view.getSideAValues().size());
      Assert.assertNull(view.getSideBValues().get(0));

      // just test side B
      provider.setRelatedSideAArtifacts(emptyList);
      provider.setRelatedSideBArtifacts(relatedArtifacts);
      presenter.onRelationTypeSelected();

      Assert.assertEquals("SideAName", view.getSideATitle());
      Assert.assertEquals("SideBName", view.getSideBTitle());
      Assert.assertNull(view.getSideAValues().get(0));
      Assert.assertEquals(2, view.getSideBValues().size());

      // test both sides
      provider.setRelatedSideAArtifacts(relatedArtifacts);
      provider.setRelatedSideBArtifacts(relatedArtifacts);
      presenter.onRelationTypeSelected();

      Assert.assertEquals("SideAName", view.getSideATitle());
      Assert.assertEquals("SideBName", view.getSideBTitle());
      Assert.assertEquals(2, view.getSideAValues().size());
      Assert.assertEquals(2, view.getSideBValues().size());
   }

   @Test
   public void testOnRelatedArtifactSelected() {
      MockArtifactProvider provider = new MockArtifactProvider();
      String artGuid = GUID.create();
      setupTestData(provider, artGuid);
      ViewId selected = new ViewId(artGuid, "artName");

      ArtifactPresenter presenter = new ArtifactPresenter(provider);
      MockArtifactView view = new MockArtifactView();
      MockArtifact current = new MockArtifact("", "current");
      presenter.setSelectedArtifact(current);
      view.setSelectedRelatedArtifact(selected);

      presenter.setView(view);
      presenter.onRelatedArtifactSelected();

      Assert.assertEquals("name", view.getName());
      Assert.assertEquals(2, view.getCrumbs().size());
      Assert.assertEquals(1, view.getRelationTypes().size());
      Assert.assertEquals(1, view.getAttributes().keySet().size());
   }

   @Test
   public void testOnSendResultSelected() {
      MockArtifactProvider provider = new MockArtifactProvider();
      String artGuid = GUID.create();
      setupTestData(provider, artGuid);

      ArtifactPresenter presenter = new ArtifactPresenter(provider);
      MockArtifactView view = new MockArtifactView();

      presenter.setView(view);
      presenter.onSendResultSelected(artGuid, CoreBranches.COMMON.getGuid());

      Assert.assertEquals("name", view.getName());
      Assert.assertEquals(2, view.getCrumbs().size());
      Assert.assertEquals(1, view.getRelationTypes().size());
      Assert.assertEquals(1, view.getAttributes().keySet().size());
   }

   private class RelationTypeArtifactProvider extends MockArtifactProvider {

      private List<ReadableArtifact> relatedSideAArtifacts;
      private List<ReadableArtifact> relatedSideBArtifacts;

      public void setRelatedSideAArtifacts(List<ReadableArtifact> artifacts) {
         relatedSideAArtifacts = artifacts;
      }

      public void setRelatedSideBArtifacts(List<ReadableArtifact> artifacts) {
         relatedSideBArtifacts = artifacts;
      }

      @Override
      public List<ReadableArtifact> getRelatedArtifacts(ReadableArtifact art, IRelationTypeSide relationTypeSide) {
         if (relationTypeSide.getSide().isSideA()) {
            return relatedSideAArtifacts;
         } else {
            return relatedSideBArtifacts;
         }
      }

   }

}
